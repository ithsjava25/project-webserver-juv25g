package org.example.httpparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpParser extends HttpParseRequestLine {
    private boolean debug = false;
    private Map<String, String> headersMap = new HashMap<>();
    private BufferedReader reader;

    protected void setReader(InputStream in) {
        if (this.reader == null) {
            this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    public void parseHttp() throws IOException {
        String headerLine;

        while ((headerLine = reader.readLine()) != null) {
            if (headerLine.isEmpty()) {
                break;
            }

            int valueSeparator = headerLine.indexOf(':');
            if (valueSeparator <= 0) {
                continue;
            }

            String key = headerLine.substring(0, valueSeparator).trim();
            String value = headerLine.substring(valueSeparator + 1).trim();

            headersMap.merge(key, value, (existing, incoming) -> existing +", " + incoming);
        }
        if (debug) {
            System.out.println("Host: " + headersMap.get("Host"));
            for (String key : headersMap.keySet()) {
                System.out.println(key + ": " + headersMap.get(key));
            }
        }
    }


    public void parseRequest() throws IOException {
        parseHttpRequest(reader);
    }

    public Map<String, String> getHeadersMap() {
        return headersMap;
    }

    public BufferedReader getHeaderReader() {
        return reader;
    }
}
