package com.example.ibrhm.instagramclone.Models;

public class Comments {

    private String user_id;
    private String comment_id;
    private String comment;
    private CommentLike comment_like;
    private long time;
    private boolean caption, send, focus;

    public Comments() {
        focus = false;
    }

    public Comments(String user_id, String comment_id, String comment, CommentLike comment_like, long time, boolean caption, boolean send) {
        this.user_id = user_id;
        this.comment_id = comment_id;
        this.comment = comment;
        this.comment_like = comment_like;
        this.time = time;
        this.caption = caption;
        this.send = send;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public CommentLike getComment_like() {
        return comment_like;
    }

    public void setComment_like(CommentLike comment_like) {
        this.comment_like = comment_like;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isCaption() {
        return caption;
    }

    public void setCaption(boolean caption) {
        this.caption = caption;
    }

    public boolean isSend() {
        return send;
    }

    public void setSend(boolean send) {
        this.send = send;
    }

    public boolean isFocus() {
        return focus;
    }

    public void setFocus(boolean focus) {
        this.focus = focus;
    }
}
