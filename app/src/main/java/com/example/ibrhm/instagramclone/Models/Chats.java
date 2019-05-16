package com.example.ibrhm.instagramclone.Models;

public class Chats {

    private String user_id;
    private String last_message;
    private long time;
    private boolean seen;
    private boolean typing;

    public Chats() {
    }

    public Chats(String user_id, String last_message, long time, boolean seen, boolean typing) {
        this.user_id = user_id;
        this.last_message = last_message;
        this.time = time;
        this.seen = seen;
        this.typing = typing;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
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

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }
}
