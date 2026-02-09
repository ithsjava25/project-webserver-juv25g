package org.example.httpparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class HttpParseRequestLine {
    private String method;
    private String uri;
    private String version;

    Logger logger = Logger.getLogger(HttpParseRequestLine.class.getName());

    public void parseHttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        String requestLine = br.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("HTTP Request Line is Empty");
        }

        String[] requestLineArray = requestLine.split(" ", 3);

        setMethod(requestLineArray[0]);
        if (!getMethod().matches("^[A-Z]+$")){
            throw new IOException("Invalid HTTP method");
        }
        setUri(requestLineArray[1]);
        setVersion(requestLineArray[2]);


        logger.info(getMethod());
        logger.info(getUri());
        logger.info(getVersion());
    }



    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
