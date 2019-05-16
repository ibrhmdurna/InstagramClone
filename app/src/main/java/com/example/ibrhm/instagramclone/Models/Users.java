package com.example.ibrhm.instagramclone.Models;

public class Users {

    private String email;
    private String user_name;
    private String full_name;
    private String phone_number;
    private String user_id;
    private boolean private_account;
    private Details details;

    public Users() {
    }

    public Users(String email, String user_name, String full_name, String phone_number, String user_id, boolean private_account, Details details) {
        this.email = email;
        this.user_name = user_name;
        this.full_name = full_name;
        this.phone_number = phone_number;
        this.user_id = user_id;
        this.private_account = private_account;
        this.details = details;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public boolean isPrivate_account() {
        return private_account;
    }

    public void setPrivate_account(boolean private_account) {
        this.private_account = private_account;
    }
}
