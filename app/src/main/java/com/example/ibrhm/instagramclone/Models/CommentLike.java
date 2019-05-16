package com.example.ibrhm.instagramclone.Models;

public class CommentLike {

    private String user_id;

    public CommentLike() {
    }

    public CommentLike(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
