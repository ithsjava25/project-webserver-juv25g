package org.example.server;

public class RedirectResponse {
    private String location;
    private int statusCode;

    public RedirectResponse(String location, int statusCode) {
        this.location = location;
        this.statusCode = statusCode;
    }

    public String getLocation() {
        return location;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
