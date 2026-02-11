package org.example.httpparser;

import java.util.Collections;
import java.util.Map;

/*
*
*This class groups together all information about a request that the server needs
 */

public class HttpRequest {

        private final String method;
        private final String path;
        private final String version;
        private final Map<String, String> headers;
        private final String body;

        public HttpRequest(String method,
                           String path,
                           String version,
                           Map<String, String> headers,
                           String body) {
            this.method = method;
            this.path = path;
            this.version = version;
            this.headers = headers != null ? Map.copyOf(headers) : Collections.emptyMap();
            this.body = body;
        }

        public String getMethod() {
            return method; }
        public String getPath() {
            return path; }
        public String getVersion() {
            return version; }
        public Map<String, String> getHeaders() {
            return headers; }
        public String getBody() {
            return body; }
    }
