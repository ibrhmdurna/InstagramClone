package com.example.ibrhm.instagramclone.Models;

import java.io.Serializable;

public class UserPosts implements Serializable {

    private String profile_image;
    private String user_id;
    private String user_name;
    private String caption;
    private String post_url;
    private String post_id;
    private String type;
    private String thumb_image;
    private long time;
    private PostLikes likes;

    public UserPosts() {
    }

    public UserPosts(String profile_image, String user_id, String user_name, String caption, String post_url, String post_id, String type, String thumb_image, long time, PostLikes likes) {
        this.profile_image = profile_image;
        this.user_id = user_id;
        this.user_name = user_name;
        this.caption = caption;
        this.post_url = post_url;
        this.post_id = post_id;
        this.type = type;
        this.thumb_image = thumb_image;
        this.time = time;
        this.likes = likes;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getPost_url() {
        return post_url;
    }

    public void setPost_url(String post_url) {
        this.post_url = post_url;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public PostLikes getLikes() {
        return likes;
    }

    public void setLikes(PostLikes likes) {
        this.likes = likes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}
