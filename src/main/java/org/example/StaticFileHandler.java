package org.example;

import org.example.http.HttpResponseBuilder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Map;

public class StaticFileHandler {
    private final String WEB_ROOT;
    private byte[] fileBytes;
    private int statusCode;

    //Constructor for production
    public StaticFileHandler() {
        WEB_ROOT = "www";
    }

    //Constructor for tests, otherwise the www folder won't be seen
    public StaticFileHandler(String webRoot){
        WEB_ROOT = webRoot;
    }

    private void handleGetRequest(String uri) throws IOException {

        File file = new File(WEB_ROOT, uri);
        if(file.exists()) {
            fileBytes = Files.readAllBytes(file.toPath());
            statusCode = 200;
        } else {
            File errorFile = new File(WEB_ROOT, "pageNotFound.html");
            if(errorFile.exists()) {
                fileBytes = Files.readAllBytes(errorFile.toPath());
            } else {
                fileBytes = "404 Not Found".getBytes();
            }
            statusCode = 404;
        }
    }

    public void sendGetRequest(OutputStream outputStream, String uri) throws IOException{
        handleGetRequest(uri);

        HttpResponseBuilder response = new HttpResponseBuilder();
        response.setStatusCode(statusCode);
        response.setHeaders(Map.of("Content-Type", "text/html; charset=utf-8"));
        response.setBody(fileBytes);
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println(response.build());

    }

}
