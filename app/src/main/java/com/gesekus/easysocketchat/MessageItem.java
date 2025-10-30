package com.gesekus.easysocketchat;

import java.time.LocalDateTime;

public class MessageItem {
    private String message;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    private String user;
    private LocalDateTime localDateTime;

    public boolean isFromServer() {
        return fromServer;
    }

    public void setFromServer(boolean fromServer) {
        this.fromServer = fromServer;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private boolean fromServer;

    public MessageItem(String message, LocalDateTime localDateTime, boolean fromServer, String user) {
        this.message = message;
        this.localDateTime = localDateTime;
        this.fromServer = fromServer;
        this.user = user;
    }

    public MessageItem(String message, LocalDateTime localDateTime, boolean fromServer) {
        this.message = message;
        this.localDateTime = localDateTime;
        this.fromServer = fromServer;
        this.user = "System";
    }

}
