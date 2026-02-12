package org.example;

import org.example.httpparser.HttpParser;

import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler implements AutoCloseable {

    Socket client;
    String uri;

    public ConnectionHandler(Socket client) {
        this.client = client;
    }

    public void runConnectionHandler() throws IOException {
        StaticFileHandler sfh = new StaticFileHandler();
        HttpParser parser = new HttpParser();
        parser.setReader(client.getInputStream());
        parser.parseRequest();
        parser.parseHttp();
        resolveTargetFile(parser.getUri());
        sfh.sendGetRequest(client.getOutputStream(), uri);
    }

    private void resolveTargetFile(String uri) {
        if (uri.matches("/$")) { //matches(/)
            this.uri = "index.html";
        } else if (uri.matches("^(?!.*\\.html$).*$")) {
            this.uri = uri.concat(".html");
        } else {
            this.uri = uri;
        }

    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}
