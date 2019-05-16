package com.example.ibrhm.instagramclone.Utils;

import com.example.ibrhm.instagramclone.Models.Users;

public class EventBusDataEvents {

    public static class SendValue{
        private String phoneNumber;
        private String sentCode;
        private String email;
        private String verificationId;
        private String fullName;
        private String userName;
        private String password;

        public SendValue(String phoneNumber, String email, String verificationId, String sentCode, String fullName, String userName, String password){
            this.phoneNumber = phoneNumber;
            this.sentCode = sentCode;
            this.verificationId = verificationId;
            this.email = email;
            this.fullName = fullName;
            this.userName = userName;
            this.password = password;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getSentCode() {
            return sentCode;
        }

        public void setSentCode(String sentCode) {
            this.sentCode = sentCode;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getVerificationId() {
            return verificationId;
        }

        public void setVerificationId(String verificationId) {
            this.verificationId = verificationId;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }

    public static class SendUserInfo{
        private Users user;

        public SendUserInfo() {
        }

        public SendUserInfo(Users user) {
            this.user = user;
        }

        public Users getUser() {
            return user;
        }

        public void setUser(Users user) {
            this.user = user;
        }
    }

    public static class SendUID{
        private String user_id;

        public SendUID() {
        }

        public SendUID(String user_id) {
            this.user_id = user_id;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }
    }
}
