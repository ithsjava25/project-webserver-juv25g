package org.example.httpparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpParser {
    private boolean debug = false;
    private Map<String, String> headersMap = new HashMap<>();
    private BufferedReader reader;
    public void parseHttp(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        setReader(br);
        Map<String, String> headers = new HashMap<>();
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

            headers.put(key, value);
        }
        headersMap = headers;
        if (debug) {
            System.out.println("Host: " + headersMap.get("Host"));
            for (String key : headersMap.keySet()) {
                System.out.println(key + ": " + headersMap.get(key));
            }
        }
    }

    public Map<String, String> getHeadersMap() {
        return headersMap;
    }

    private void setReader(BufferedReader reader) {
        this.reader = reader;
    }

}
