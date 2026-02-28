package org.example;

import org.example.config.ConfigLoader;
import org.example.filter.FilterChain;
import org.example.http.HttpResponseBuilder;
import org.example.httpparser.HttpRequest;

import java.io.File;

public class FileResolver {


    /**
     * Helper class to resolve HTTP request paths to actual file system paths.
     *  This method processes incoming HTTP request paths and converts them into
     *  absolute file paths that can be used to locate and serve static files.
     * @param requestPath the HTTP request path (e.g., "/", "/index.html"
     * @return the complete file system path combining the root directory with the processed request path
     */

    public static String resolvePath(String requestPath) {
        String path = requestPath.equals("/") ? "index.html" : requestPath.substring(1);
        String rootDir = ConfigLoader.get().server().rootDir();

        return rootDir + "/" + path;

    }



}


