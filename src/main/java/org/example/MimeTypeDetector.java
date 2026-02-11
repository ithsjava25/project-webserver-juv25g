package org.example;

import java.util.HashMap;
import java.util.Map;

public class MimeTypeDetector {
    private Map<String, String> mimeTypes;

    public MimeTypeDetector() {
        mimeTypes = new HashMap<>();
        initializeMimeTypes();
    }


    private void initializeMimeTypes(){
        mimeTypes.put("html", "text/html");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("txt", "text/plain");
        mimeTypes.put("json", "application/json");
        mimeTypes.put("pdf", "application/pdf");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("mp4", "video/mp4");
        mimeTypes.put("mp3", "audio/mpeg");
        mimeTypes.put("zip", "application/zip");
    }

    public String detect(String filename){
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1){
            // detta är en Standard för typer som är okända
            return "application/octet-stream";
        }
        String extension = filename.substring(lastDot+1).toLowerCase();
        return mimeTypes.getOrDefault(extension, "application/octet-stream");
    }

    public static void main(String[] args) {
        MimeTypeDetector detector = new MimeTypeDetector();

        // för att testa att programmet funkar =)

        System.out.println(detector.detect("document.pdf"));      // application/pdf
        System.out.println(detector.detect("photo.jpg"));         // image/jpeg
        System.out.println(detector.detect("index.html"));        // text/html
        System.out.println(detector.detect("unknown.xyz"));       // application/octet-stream
    }
}
