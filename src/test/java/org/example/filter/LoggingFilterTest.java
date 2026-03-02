package org.example.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    @Mock HttpRequest request;
    @Mock HttpResponseBuilder response;
    @Mock FilterChain chain;

    LoggingFilter filter = new LoggingFilter();
    ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setup(){
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingFilter.class);

        appender = new ListAppender<>();
        appender.start();

        logger.addAppender(appender);

        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/index.html");
    }

    @Test
    void loggingWorksWhenChainWorks(){
        when(response.getStatusCode()).thenReturn(HttpResponseBuilder.SC_OK);

        filter.doFilter(request, response, chain);

        verifyLogContent("REQUEST: GET /index.html | STATUS: 200 | TIME: ");
    }

    @Test
    void loggingWorksWhenErrorOccurs(){
        when(response.getStatusCode()).thenReturn(HttpResponseBuilder.SC_NOT_FOUND);

        doThrow(new RuntimeException()).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);

        verifyLogContent("REQUEST: GET /index.html | STATUS: 404 | TIME: ");
    }

    @Test
    void statusChangesFrom200To500WhenErrorOccurs(){
        //Return status 200 first time.
        //When error is thrown status should switch to 500 (if it was originally 200)
        when(response.getStatusCode())
                .thenReturn(HttpResponseBuilder.SC_OK)
                        .thenReturn(HttpResponseBuilder.SC_INTERNAL_SERVER_ERROR);

        doThrow(new RuntimeException()).when(chain).doFilter(request, response);

        filter.doFilter(request, response, chain);
        verify(response).setStatusCode(HttpResponseBuilder.SC_INTERNAL_SERVER_ERROR);

        verifyLogContent("REQUEST: GET /index.html | STATUS: 500 | TIME: ");
    }

    private void verifyLogContent(String expectedMessage){
        List<ILoggingEvent> logs = appender.list;

        assertThat(logs).isNotEmpty();
        String message = logs.getFirst().getFormattedMessage();

        assertThat(message).contains(expectedMessage);
    }

}
