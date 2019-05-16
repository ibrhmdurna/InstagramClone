package com.example.ibrhm.instagramclone.Models;

import java.io.Serializable;

public class Interactions implements Serializable {

    private String user_id;
    private String post_id;
    private String post_user_id;
    private String action;
    private String content;
    private long time;

    public Interactions() {
    }

    public Interactions(String user_id, String post_id, String post_user_id, String action, String content, long time) {
        this.user_id = user_id;
        this.post_id = post_id;
        this.post_user_id = post_user_id;
        this.action = action;
        this.content = content;
        this.time = time;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPost_user_id() {
        return post_user_id;
    }

    public void setPost_user_id(String post_user_id) {
        this.post_user_id = post_user_id;
    }
}
