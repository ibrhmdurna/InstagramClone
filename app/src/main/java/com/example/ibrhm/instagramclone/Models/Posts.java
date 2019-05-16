package com.example.ibrhm.instagramclone.Models;

public class Posts {

    private String user_id;
    private String post_id;
    private long time;
    private String caption;
    private String post_url;
    private String type;
    private String thumb_image;
    private PostLikes likes;

    public Posts() {
    }

    public Posts(String user_id, String post_id, long time, String caption, String post_url, String type, String thumb_image, PostLikes likes) {
        this.user_id = user_id;
        this.post_id = post_id;
        this.time = time;
        this.caption = caption;
        this.post_url = post_url;
        this.type = type;
        this.thumb_image = thumb_image;
        this.likes = likes;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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
