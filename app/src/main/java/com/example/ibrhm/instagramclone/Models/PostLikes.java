package com.example.ibrhm.instagramclone.Models;

import java.io.Serializable;

public class PostLikes implements Serializable {

    private String user_id;

    public PostLikes() {
    }

    public PostLikes(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
