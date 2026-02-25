package org.example.filter;

import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    @Mock Handler handler;
    @Mock HttpRequest request;
    @Mock HttpResponseBuilder response;
    @Mock FilterChain chain;

    LoggingFilter filter = new LoggingFilter();
    Logger logger;

    @BeforeEach
    void setup(){
        logger = Logger.getLogger(LoggingFilter.class.getName());
        logger.addHandler(handler);

        when(request.getMethod()).thenReturn("GET");
        when(request.getPath()).thenReturn("/index.html");
    }

    @AfterEach
    void tearDown(){
        logger.removeHandler(handler);
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
        //Use ArgumentCaptor to capture the actual message in the log
        ArgumentCaptor<LogRecord> logCaptor = ArgumentCaptor.forClass(LogRecord.class);
        verify(handler).publish(logCaptor.capture());

        String message = logCaptor.getValue().getMessage();

        assertThat(message).contains(expectedMessage);
    }

}
