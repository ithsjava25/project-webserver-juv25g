package org.example;

import org.example.http.HttpResponseBuilder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Map;

public class StaticFileHandler {
    private static final String WEB_ROOT = "www";
    private byte[] fileBytes;

    public StaticFileHandler(){}

    private void handleGetRequest(String uri) throws IOException {

        File file = new File(WEB_ROOT, uri);
        fileBytes = Files.readAllBytes(file.toPath());

    }

    public void sendGetRequest(OutputStream outputStream, String uri) throws IOException{
        handleGetRequest(uri);
        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setHeaders(Map.of("Content-Type", "text/html; charset=utf-8"));
        response.setBody(fileBytes);
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println(response.build());

    }

}
