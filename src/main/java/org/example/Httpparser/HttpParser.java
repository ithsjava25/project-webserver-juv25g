package org.example.Httpparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpParser {
    public void parseHttpHeader(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        Map<String, String> headers = new HashMap<>();
        String headerLine = br.readLine();

        while ((headerLine = br.readLine()) != null) {
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
        //DEBUG:
//        for (String key : headers.values()) {
//            System.out.println(key + ": " + headers.get(key));
//        }
    }
}
