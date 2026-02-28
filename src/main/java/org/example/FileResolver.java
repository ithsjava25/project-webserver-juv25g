package org.example;

import org.example.config.ConfigLoader;
import org.example.filter.FilterChain;
import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.io.File;

public class FileResolver {


    public static String resolvePath(String requestPath) {
        String path = requestPath.equals("/") ? "index.html" : requestPath.substring(1);
        String rootDir = ConfigLoader.get().server().rootDir();

        return path;
    }



}


