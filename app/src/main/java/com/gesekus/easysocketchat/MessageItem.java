package com.gesekus.easysocketchat;

import java.time.LocalDateTime;
import java.util.Date;

public class MessageItem {
    private String message;
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

    public MessageItem(String message, LocalDateTime localDateTime, boolean fromServer) {
        this.message = message;
        this.localDateTime = localDateTime;
        this.fromServer = fromServer;
    }

}
