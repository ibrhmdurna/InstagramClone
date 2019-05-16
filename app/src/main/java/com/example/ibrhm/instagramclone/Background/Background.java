package com.example.ibrhm.instagramclone.Background;

import com.example.ibrhm.instagramclone.Models.UserPosts;
import com.example.ibrhm.instagramclone.Models.Users;

import java.util.ArrayList;
import java.util.List;

public class Background {

    private List<String> backgroundList;

    public Background() {
        backgroundList = new ArrayList<>();
    }

    public List<String> getBackgroundList() {
        return backgroundList;
    }

    public void setBackgroundList(List<String> backgroundList) {
        this.backgroundList = backgroundList;
    }

    public boolean isThereBackground(String intent){
        for(int i = 0; i < backgroundList.size(); i++){
            if(backgroundList.get(i).equals(intent)){
                return true;
            }
        }

        return false;
    }

    public static class Profile{

        private String postCount;
        private String followersCount;
        private String followingCount;
        private String postLayoutState;

        public Profile() {
            postLayoutState = "Grid";
        }

        public Profile(String postCount, String followersCount, String followingCount, String postLayoutState) {
            this.postCount = postCount;
            this.followersCount = followersCount;
            this.followingCount = followingCount;
            this.postLayoutState = postLayoutState;
        }

        public String getPostCount() {
            return postCount;
        }

        public void setPostCount(String postCount) {
            this.postCount = postCount;
        }

        public String getFollowersCount() {
            return followersCount;
        }

        public void setFollowersCount(String followersCount) {
            this.followersCount = followersCount;
        }

        public String getFollowingCount() {
            return followingCount;
        }

        public void setFollowingCount(String followingCount) {
            this.followingCount = followingCount;
        }

        public String getPostLayoutState() {
            return postLayoutState;
        }

        public void setPostLayoutState(String postLayoutState) {
            this.postLayoutState = postLayoutState;
        }
    }

    public static class News{
        private String newsLayoutState;

        public News() {
            newsLayoutState = "You";
        }

        public News(String newsLayoutState) {
            this.newsLayoutState = newsLayoutState;
        }

        public String getNewsLayoutState() {
            return newsLayoutState;
        }

        public void setNewsLayoutState(String newsLayoutState) {
            this.newsLayoutState = newsLayoutState;
        }
    }
}
