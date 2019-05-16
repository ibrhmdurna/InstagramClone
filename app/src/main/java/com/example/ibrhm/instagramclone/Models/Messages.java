package com.example.ibrhm.instagramclone.Models;

public class Messages {

    private String from;
    private String message;
    private String type;
    private long time;
    private boolean seen, visibility, timeVisibility, send;
    private boolean isSeenLayout = false;
    private boolean isTypingLayout = false;

    public Messages() {
    }

    public Messages(String from, String message, String type, long time, boolean seen, boolean visibility, boolean timeVisibility, boolean send, boolean isSeenLayout, boolean isTypingLayout) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
        this.visibility = visibility;
        this.timeVisibility = timeVisibility;
        this.send = send;
        this.isSeenLayout = isSeenLayout;
        this.isTypingLayout = isTypingLayout;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public boolean isTimeVisibility() {
        return timeVisibility;
    }

    public void setTimeVisibility(boolean timeVisibility) {
        this.timeVisibility = timeVisibility;
    }

    public boolean isSend() {
        return send;
    }

    public void setSend(boolean send) {
        this.send = send;
    }

    public boolean isSeenLayout() {
        return isSeenLayout;
    }

    public void setSeenLayout(boolean seenLayout) {
        isSeenLayout = seenLayout;
    }

    public boolean isTypingLayout() {
        return isTypingLayout;
    }

    public void setTypingLayout(boolean typingLayout) {
        isTypingLayout = typingLayout;
    }
}
