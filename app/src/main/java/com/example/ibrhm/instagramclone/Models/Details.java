package com.example.ibrhm.instagramclone.Models;

public class Details {
    private String profile_image;
    private String biography;
    private String web_site;

    public Details() {
    }

    public Details(String profile_image, String biography, String web_site) {
        this.profile_image = profile_image;
        this.biography = biography;
        this.web_site = web_site;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getWeb_site() {
        return web_site;
    }

    public void setWeb_site(String web_site) {
        this.web_site = web_site;
    }
}
