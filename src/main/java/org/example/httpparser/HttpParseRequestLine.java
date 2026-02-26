package org.example.httpparser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

abstract class HttpParseRequestLine {
    private String method;
    private String uri;
    private String version;

    private static final Logger log = LoggerFactory.getLogger(HttpParseRequestLine.class);

    public void parseHttpRequest(BufferedReader br) throws IOException {
        BufferedReader reader = br;
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("HTTP Request Line is Null or Empty");
        }

        String[] requestLineArray = requestLine.trim().split(" ", 3);

        if (requestLineArray.length <= 2) {
            throw new IOException("HTTP Request Line is not long enough");
        } else {
            setMethod(requestLineArray[0]);
            if (!getMethod().matches("^[A-Z]+$")){
                throw new IOException("Invalid HTTP method");
            }
            setUri(requestLineArray[1]);
            setVersion(requestLineArray[2]);
        }

        log.debug("METHOD: {} | URI: {} | VERSION: {}",
                getMethod(), getUri(), getVersion());
    }



    public String getMethod() {
        return method;
    }

    private void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    private void setUri(String uri) {
        this.uri = uri;
    }

    public String getVersion() {
        return version;
    }

    private void setVersion(String version) {
        this.version = version;
    }
}
