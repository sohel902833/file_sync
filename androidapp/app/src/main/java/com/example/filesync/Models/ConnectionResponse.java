package com.example.filesync.Models;

public class ConnectionResponse {
    String message;
    boolean connected;

    public ConnectionResponse() {
    }

    public ConnectionResponse(String message, boolean connected) {
        this.message = message;
        this.connected = connected;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
