package com.example.ibrhm.instagramclone.Utils;

import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.Local.PostActivity;
import com.example.ibrhm.instagramclone.Models.UserPosts;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.CameraAnimation;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.Video;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.VideoView;
import com.hoanganhtuan95ptit.autoplayvideorecyclerview.VideoHolder;

import java.util.ArrayList;
import java.util.HashMap;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ExploreViewHolder> {

    private LayoutInflater layoutInflater;
    private Context mContext;
    private ArrayList<UserPosts> mLayoutList[];
    //private List<UserPosts> mPostsList;

    public ExploreAdapter(ArrayList postsList[], Context context){
        mContext = context;
        mLayoutList = postsList;
        if(context != null){
            layoutInflater = LayoutInflater.from(context);
        }
    }

    @NonNull
    @Override
    public ExploreViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.explore_layout, viewGroup, false);
        return new ExploreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExploreViewHolder exploreViewHolder, int i) {

        if(i % 2 == 0){
            exploreViewHolder.setDataDefault(i);
        }
        else{
            exploreViewHolder.setDataLeft(i);
        }

    }

    @Override
    public int getItemCount() {
        return mLayoutList.length;
    }

    public class ExploreViewHolder extends VideoHolder {

        LinearLayout mBigContainer, mTopContainer, mCenterContainer, mBottomContainer, mDefaultLayout, mLeftLayout;
        GridImageView mPost0, mPost1, mPost2, mPost3, mPost4, mPost5, mPost6, mPost7, mPost8, mPostLeft0, mPostLeft1;
        VideoView mVideoView;
        CameraAnimation mCameraAnimation;
        ImageView mPost0C, mPost1C, mPost3C, mPost4C, mPost5C, mPost6C, mPost7C, mPost8C, mPost0LC, mPost1LC;
        RelativeLayout mBigContentVideo, mBigContentPhoto;

        boolean isVideo = false;

        public ExploreViewHolder(@NonNull View itemView) {
            super(itemView);

            mBigContainer = itemView.findViewById(R.id.explore_big_container);
            mTopContainer = itemView.findViewById(R.id.explore_top_container);
            mCenterContainer = itemView.findViewById(R.id.explore_center_container);
            mBottomContainer = itemView.findViewById(R.id.explore_bottom_container);
            mDefaultLayout = itemView.findViewById(R.id.default_post_layout);
            mLeftLayout = itemView.findViewById(R.id.left_post_layout);
            mPost0 = itemView.findViewById(R.id.explorePost0);
            mPost1 = itemView.findViewById(R.id.explorePost1);
            mPost2 = itemView.findViewById(R.id.explorePost2);
            mPost3 = itemView.findViewById(R.id.explorePost3);
            mPost4 = itemView.findViewById(R.id.explorePost4);
            mPost5 = itemView.findViewById(R.id.explorePost5);
            mPost6 = itemView.findViewById(R.id.explorePost6);
            mPost7 = itemView.findViewById(R.id.explorePost7);
            mPost8 = itemView.findViewById(R.id.explorePost8);
            mPost0C = itemView.findViewById(R.id.explorePost0_camera);
            mPost1C = itemView.findViewById(R.id.explorePost1_camera);
            mPost3C = itemView.findViewById(R.id.explorePost3_camera);
            mPost4C = itemView.findViewById(R.id.explorePost4_camera);
            mPost5C = itemView.findViewById(R.id.explorePost5_camera);
            mPost6C = itemView.findViewById(R.id.explorePost6_camera);
            mPost7C = itemView.findViewById(R.id.explorePost7_camera);
            mPost8C = itemView.findViewById(R.id.explorePost8_camera);
            mPost0LC = itemView.findViewById(R.id.explorePost0_left_camera);
            mPost1LC = itemView.findViewById(R.id.explorePost1_left_camera);
            mPostLeft0 = itemView.findViewById(R.id.explorePost0_left);
            mPostLeft1 = itemView.findViewById(R.id.explorePost1_left);
            mVideoView = itemView.findViewById(R.id.explore_video_view);
            mCameraAnimation = itemView.findViewById(R.id.cameraAnimation);
            mBigContentVideo = itemView.findViewById(R.id.big_content_video_view);
            mBigContentPhoto = itemView.findViewById(R.id.big_content_photo_view);
        }

        @Override
        public View getVideoLayout() {
            return mVideoView;
        }

        @Override
        public void playVideo() {
            if(isVideo){
                mBigContentVideo.startAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in));
                mBigContentVideo.setVisibility(View.VISIBLE);
                mBigContentPhoto.setVisibility(View.GONE);

                mCameraAnimation.setVisibility(View.VISIBLE);
                mCameraAnimation.start();

                mVideoView.play(new VideoView.OnPreparedListener() {
                    @Override
                    public void onPrepared() {
                        mCameraAnimation.setVisibility(View.GONE);
                        mCameraAnimation.stop();
                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(mBigContentVideo.getVisibility() == View.VISIBLE){
                                    mBigContentVideo.startAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out));
                                    mBigContentVideo.setVisibility(View.GONE);
                                }
                            }
                        },3000);
                    }
                });
            }
        }

        @Override
        public void stopVideo() {
            mCameraAnimation.stop();
            mVideoView.stop();
        }

        public void setDataDefault(final int position){

            if(mLayoutList[position].size() < 3){
                // Post sayısı 3 den az
                mBigContainer.setVisibility(View.GONE);
                return;
            }

            if(mLayoutList[position].size() == 9){
                // Full dolu ise
                mBigContainer.setVisibility(View.VISIBLE);

                if(mLayoutList[position].get(2).getType().equals("Photo")){
                    for(int i = 0; i < 9; i++){
                        if(mLayoutList[position].get(i).getType().equals("Video")){
                            UserPosts tempPost = mLayoutList[position].get(2);
                            mLayoutList[position].remove(2);
                            mLayoutList[position].add(2, mLayoutList[position].get(i));
                            mLayoutList[position].remove(i);
                            mLayoutList[position].add(i, tempPost);
                            isVideo = true;
                            break;
                        }
                    }
                }
                else{
                    isVideo = true;
                }

                if(isVideo){
                    mPost2.setVisibility(View.GONE);
                    mVideoView.setVisibility(View.VISIBLE);
                    mCameraAnimation.setVisibility(View.VISIBLE);
                    mVideoView.setVideo(new Video(mLayoutList[position].get(2).getPost_url(), 0));
                }
                else{
                    UniversalImageLoader.setImage(mLayoutList[position].get(2).getPost_url(), mPost2, null,"");
                }

                for(int i = 0; i < 9; i ++){
                    if(i != 2){
                        if(mLayoutList[position].get(i).getType().equals("Video")){
                            switch (i){
                                case 0:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost0, null, "");
                                    mPost0C.setVisibility(View.VISIBLE);
                                    break;
                                case 1:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost1, null, "");
                                    mPost1C.setVisibility(View.VISIBLE);
                                    break;
                                case 3:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost3, null, "");
                                    mPost3C.setVisibility(View.VISIBLE);
                                    break;
                                case 4:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost4, null, "");
                                    mPost4C.setVisibility(View.VISIBLE);
                                    break;
                                case 5:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost5, null, "");
                                    mPost5C.setVisibility(View.VISIBLE);
                                    break;
                                case 6:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost6, null, "");
                                    mPost6C.setVisibility(View.VISIBLE);
                                    break;
                                case 7:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost7, null, "");
                                    mPost7C.setVisibility(View.VISIBLE);
                                    break;
                                case 8:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost8, null, "");
                                    mPost8C.setVisibility(View.VISIBLE);
                                    break;
                            }
                        }
                        else{
                            switch (i){
                                case 0:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost0, null, "");
                                    break;
                                case 1:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost1, null, "");
                                    break;
                                case 3:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost3, null, "");
                                    break;
                                case 4:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost4, null, "");
                                    break;
                                case 5:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost5, null, "");
                                    break;
                                case 6:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost6, null, "");
                                    break;
                                case 7:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost7, null, "");
                                    break;
                                case 8:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost8, null, "");
                                    break;
                            }
                        }
                    }
                }

                clickOptions(position);
            }
            else if(mLayoutList[position].size() > 5){
                // Post sayısı 7 ile 9 arasında
                mBigContainer.setVisibility(View.VISIBLE);
                mTopContainer.setVisibility(View.VISIBLE);
                mCenterContainer.setVisibility(View.VISIBLE);
                mBottomContainer.setVisibility(View.GONE);

                if(mLayoutList[position].get(2).getType().equals("Photo")){
                    for(int i = 0; i < 6; i++){
                        if(mLayoutList[position].get(i).getType().equals("Video")){
                            UserPosts tempPost = mLayoutList[position].get(2);
                            mLayoutList[position].remove(2);
                            mLayoutList[position].add(2, mLayoutList[position].get(i));
                            mLayoutList[position].remove(i);
                            mLayoutList[position].add(i, tempPost);
                            isVideo = true;
                            break;
                        }
                    }
                }
                else{
                    isVideo = true;
                }

                if(isVideo){
                    mPost2.setVisibility(View.GONE);
                    mVideoView.setVisibility(View.VISIBLE);
                    mCameraAnimation.setVisibility(View.VISIBLE);
                    mVideoView.setVideo(new Video(mLayoutList[position].get(2).getPost_url(), 0));
                }
                else{
                    UniversalImageLoader.setImage(mLayoutList[position].get(2).getPost_url(), mPost2, null,"");
                }

                for(int i = 0; i < 6; i ++){
                    if(i != 2){
                        if(mLayoutList[position].get(i).getType().equals("Video")){
                            switch (i){
                                case 0:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost0, null, "");
                                    mPost0C.setVisibility(View.VISIBLE);
                                    break;
                                case 1:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost1, null, "");
                                    mPost1C.setVisibility(View.VISIBLE);
                                    break;
                                case 3:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost3, null, "");
                                    mPost3C.setVisibility(View.VISIBLE);
                                    break;
                                case 4:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost4, null, "");
                                    mPost4C.setVisibility(View.VISIBLE);
                                    break;
                                case 5:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost5, null, "");
                                    mPost5C.setVisibility(View.VISIBLE);
                                    break;
                            }
                        }
                        else{
                            switch (i){
                                case 0:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost0, null, "");
                                    break;
                                case 1:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost1, null, "");
                                    break;
                                case 3:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost3, null, "");
                                    break;
                                case 4:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost4, null, "");
                                    break;
                                case 5:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost5, null, "");
                                    break;
                            }
                        }
                    }
                }

                clickOptions(position);
            }
            else if(mLayoutList[position].size() > 2){
                // Post sayısı 3 ile 6 arasında
                mBigContainer.setVisibility(View.VISIBLE);
                mTopContainer.setVisibility(View.VISIBLE);
                mBottomContainer.setVisibility(View.GONE);
                mCenterContainer.setVisibility(View.GONE);

                if(mLayoutList[position].get(2).getType().equals("Photo")){
                    for(int i = 0; i < 3; i++){
                        if(mLayoutList[position].get(i).getType().equals("Video")){
                            UserPosts tempPost = mLayoutList[position].get(2);
                            mLayoutList[position].remove(2);
                            mLayoutList[position].add(2, mLayoutList[position].get(i));
                            mLayoutList[position].remove(i);
                            mLayoutList[position].add(i, tempPost);
                            isVideo = true;
                            break;
                        }
                    }
                }
                else{
                    isVideo = true;
                }

                if(isVideo){
                    mPost2.setVisibility(View.GONE);
                    mVideoView.setVisibility(View.VISIBLE);
                    mCameraAnimation.setVisibility(View.VISIBLE);
                    mVideoView.setVideo(new Video(mLayoutList[position].get(2).getPost_url(), 0));
                }
                else{
                    UniversalImageLoader.setImage(mLayoutList[position].get(2).getPost_url(), mPost2, null,"");
                }

                for(int i = 0; i < 3; i ++){
                    if(i != 2){
                        if(mLayoutList[position].get(i).getType().equals("Video")){
                            switch (i){
                                case 0:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost0, null, "");
                                    mPost0C.setVisibility(View.VISIBLE);
                                    break;
                                case 1:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost1, null, "");
                                    mPost1C.setVisibility(View.VISIBLE);
                                    break;
                            }
                        }
                        else{
                            switch (i){
                                case 0:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost0, null, "");
                                    break;
                                case 1:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost1, null, "");
                                    break;
                            }
                        }
                    }
                }

                clickOptions(position);
            }

            if(!isVideo){
                mBigContentPhoto.startAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in));
                mBigContentVideo.setVisibility(View.GONE);
                mBigContentPhoto.setVisibility(View.VISIBLE);
            }
        }

        public void setDataLeft(final int position){

            if(mLayoutList[position].size() < 3){
                // Post sayısı 3 den az
                mBigContainer.setVisibility(View.GONE);
                return;
            }

            mDefaultLayout.setVisibility(View.GONE);
            mLeftLayout.setVisibility(View.VISIBLE);
            if(mLayoutList[position].size() == 9){
                // Full dolu ise
                mBigContainer.setVisibility(View.VISIBLE);

                if(mLayoutList[position].get(2).getType().equals("Photo")){
                    for(int i = 0; i < 9; i++){
                        if(mLayoutList[position].get(i).getType().equals("Video")){
                            UserPosts tempPost = mLayoutList[position].get(2);
                            mLayoutList[position].remove(2);
                            mLayoutList[position].add(2, mLayoutList[position].get(i));
                            mLayoutList[position].remove(i);
                            mLayoutList[position].add(i, tempPost);
                            isVideo = true;
                            break;
                        }
                    }
                }
                else{
                    isVideo = true;
                }

                if(isVideo){
                    mPost2.setVisibility(View.GONE);
                    mVideoView.setVisibility(View.VISIBLE);
                    mCameraAnimation.setVisibility(View.VISIBLE);
                    mVideoView.setVideo(new Video(mLayoutList[position].get(2).getPost_url(), 0));
                }
                else{
                    UniversalImageLoader.setImage(mLayoutList[position].get(2).getPost_url(), mPost2, null,"");
                }

                for(int i = 0; i < 9; i ++){
                    if(i != 2){
                        if(mLayoutList[position].get(i).getType().equals("Video")){
                            switch (i){
                                case 0:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPostLeft0, null, "");
                                    mPost0LC.setVisibility(View.VISIBLE);
                                    break;
                                case 1:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPostLeft1, null, "");
                                    mPost1LC.setVisibility(View.VISIBLE);
                                    break;
                                case 3:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost3, null, "");
                                    mPost3C.setVisibility(View.VISIBLE);
                                    break;
                                case 4:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost4, null, "");
                                    mPost4C.setVisibility(View.VISIBLE);
                                    break;
                                case 5:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost5, null, "");
                                    mPost5C.setVisibility(View.VISIBLE);
                                    break;
                                case 6:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost6, null, "");
                                    mPost6C.setVisibility(View.VISIBLE);
                                    break;
                                case 7:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost7, null, "");
                                    mPost7C.setVisibility(View.VISIBLE);
                                    break;
                                case 8:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost8, null, "");
                                    mPost8C.setVisibility(View.VISIBLE);
                                    break;
                            }
                        }
                        else{
                            switch (i){
                                case 0:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPostLeft0, null, "");
                                    break;
                                case 1:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPostLeft1, null, "");
                                    break;
                                case 3:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost3, null, "");
                                    break;
                                case 4:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost4, null, "");
                                    break;
                                case 5:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost5, null, "");
                                    break;
                                case 6:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost6, null, "");
                                    break;
                                case 7:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost7, null, "");
                                    break;
                                case 8:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost8, null, "");
                                    break;
                            }
                        }
                    }
                }

                clickOptions(position);
            }
            else if(mLayoutList[position].size() > 5){
                // Post sayısı 7 ile 9 arasında
                mBigContainer.setVisibility(View.VISIBLE);
                mTopContainer.setVisibility(View.VISIBLE);
                mCenterContainer.setVisibility(View.VISIBLE);
                mBottomContainer.setVisibility(View.GONE);

                if(mLayoutList[position].get(2).getType().equals("Photo")){
                    for(int i = 0; i < 6; i++){
                        if(mLayoutList[position].get(i).getType().equals("Video")){
                            UserPosts tempPost = mLayoutList[position].get(2);
                            mLayoutList[position].remove(2);
                            mLayoutList[position].add(2, mLayoutList[position].get(i));
                            mLayoutList[position].remove(i);
                            mLayoutList[position].add(i, tempPost);
                            isVideo = true;
                            break;
                        }
                    }
                }
                else{
                    isVideo = true;
                }

                if(isVideo){
                    mPost2.setVisibility(View.GONE);
                    mVideoView.setVisibility(View.VISIBLE);
                    mCameraAnimation.setVisibility(View.VISIBLE);
                    mVideoView.setVideo(new Video(mLayoutList[position].get(2).getPost_url(), 0));
                }
                else{
                    UniversalImageLoader.setImage(mLayoutList[position].get(2).getPost_url(), mPost2, null,"");
                }

                for(int i = 0; i < 6; i ++){
                    if(i != 2){
                        if(mLayoutList[position].get(i).getType().equals("Video")){
                            switch (i){
                                case 0:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPostLeft0, null, "");
                                    mPost0LC.setVisibility(View.VISIBLE);
                                    break;
                                case 1:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPostLeft1, null, "");
                                    mPost1LC.setVisibility(View.VISIBLE);
                                    break;
                                case 3:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost3, null, "");
                                    mPost3C.setVisibility(View.VISIBLE);
                                    break;
                                case 4:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost4, null, "");
                                    mPost4C.setVisibility(View.VISIBLE);
                                    break;
                                case 5:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPost5, null, "");
                                    mPost5C.setVisibility(View.VISIBLE);
                                    break;
                            }
                        }
                        else{
                            switch (i){
                                case 0:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPostLeft0, null, "");
                                    break;
                                case 1:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPostLeft1, null, "");
                                    break;
                                case 3:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost3, null, "");
                                    break;
                                case 4:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost4, null, "");
                                    break;
                                case 5:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPost5, null, "");
                                    break;
                            }
                        }
                    }
                }

                clickOptions(position);
            }
            else if(mLayoutList[position].size() > 2){
                // Post sayısı 3 ile 6 arasında
                mBigContainer.setVisibility(View.VISIBLE);
                mTopContainer.setVisibility(View.VISIBLE);
                mBottomContainer.setVisibility(View.GONE);
                mCenterContainer.setVisibility(View.GONE);

                if(mLayoutList[position].get(2).getType().equals("Photo")){
                    for(int i = 0; i < 3; i++){
                        if(mLayoutList[position].get(i).getType().equals("Video")){
                            UserPosts tempPost = mLayoutList[position].get(2);
                            mLayoutList[position].remove(2);
                            mLayoutList[position].add(2, mLayoutList[position].get(i));
                            mLayoutList[position].remove(i);
                            mLayoutList[position].add(i, tempPost);
                            isVideo = true;
                            break;
                        }
                    }
                }
                else{
                    isVideo = true;
                }

                if(isVideo){
                    mPost2.setVisibility(View.GONE);
                    mVideoView.setVisibility(View.VISIBLE);
                    mCameraAnimation.setVisibility(View.VISIBLE);
                    mVideoView.setVideo(new Video(mLayoutList[position].get(2).getPost_url(), 0));
                }
                else{
                    UniversalImageLoader.setImage(mLayoutList[position].get(2).getPost_url(), mPost2, null,"");
                }

                for(int i = 0; i < 3; i ++){
                    if(i != 2){
                        if(mLayoutList[position].get(i).getType().equals("Video")){
                            switch (i){
                                case 0:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPostLeft0, null, "");
                                    mPost0LC.setVisibility(View.VISIBLE);
                                    break;
                                case 1:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getThumb_image(), mPostLeft1, null, "");
                                    mPost1LC.setVisibility(View.VISIBLE);
                                    break;
                            }
                        }
                        else{
                            switch (i){
                                case 0:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPostLeft0, null, "");
                                    break;
                                case 1:
                                    UniversalImageLoader.setImage(mLayoutList[position].get(i).getPost_url(), mPostLeft1, null, "");
                                    break;
                            }
                        }
                    }
                }

                clickOptions(position);
            }

            if(!isVideo){
                mBigContentPhoto.startAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in));
                mBigContentVideo.setVisibility(View.GONE);
                mBigContentPhoto.setVisibility(View.VISIBLE);
            }
        }

        private void clickOptions(final int position){
            mPost0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postIntent = new Intent(mContext, PostActivity.class);
                    postIntent.putExtra("activity_no", 1);
                    postIntent.putExtra("user_post", mLayoutList[position].get(0));
                    postIntent.putExtra("is_explore", true);
                    mContext.startActivity(postIntent);
                }
            });

            mPost1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postIntent = new Intent(mContext, PostActivity.class);
                    postIntent.putExtra("activity_no", 1);
                    postIntent.putExtra("user_post", mLayoutList[position].get(1));
                    postIntent.putExtra("is_explore", true);
                    mContext.startActivity(postIntent);
                }
            });

            mPost2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postIntent = new Intent(mContext, PostActivity.class);
                    postIntent.putExtra("activity_no", 1);
                    postIntent.putExtra("user_post", mLayoutList[position].get(2));
                    postIntent.putExtra("is_explore", true);
                    mContext.startActivity(postIntent);
                }
            });

            mPost3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postIntent = new Intent(mContext, PostActivity.class);
                    postIntent.putExtra("activity_no", 1);
                    postIntent.putExtra("user_post", mLayoutList[position].get(3));
                    postIntent.putExtra("is_explore", true);
                    mContext.startActivity(postIntent);
                }
            });

            mPost4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postIntent = new Intent(mContext, PostActivity.class);
                    postIntent.putExtra("activity_no", 1);
                    postIntent.putExtra("user_post", mLayoutList[position].get(4));
                    postIntent.putExtra("is_explore", true);
                    mContext.startActivity(postIntent);
                }
            });

            mPost5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postIntent = new Intent(mContext, PostActivity.class);
                    postIntent.putExtra("activity_no", 1);
                    postIntent.putExtra("user_post", mLayoutList[position].get(5));
                    postIntent.putExtra("is_explore", true);
                    mContext.startActivity(postIntent);
                }
            });

            mPost6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postIntent = new Intent(mContext, PostActivity.class);
                    postIntent.putExtra("activity_no", 1);
                    postIntent.putExtra("user_post", mLayoutList[position].get(6));
                    postIntent.putExtra("is_explore", true);
                    mContext.startActivity(postIntent);
                }
            });

            mPost7.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postIntent = new Intent(mContext, PostActivity.class);
                    postIntent.putExtra("activity_no", 1);
                    postIntent.putExtra("user_post", mLayoutList[position].get(7));
                    postIntent.putExtra("is_explore", true);
                    mContext.startActivity(postIntent);
                }
            });

            mPost8.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postIntent = new Intent(mContext, PostActivity.class);
                    postIntent.putExtra("activity_no", 1);
                    postIntent.putExtra("user_post", mLayoutList[position].get(8));
                    postIntent.putExtra("is_explore", true);
                    mContext.startActivity(postIntent);
                }
            });

            mPostLeft0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postIntent = new Intent(mContext, PostActivity.class);
                    postIntent.putExtra("activity_no", 1);
                    postIntent.putExtra("user_post", mLayoutList[position].get(0));
                    postIntent.putExtra("is_explore", true);
                    mContext.startActivity(postIntent);
                }
            });

            mPostLeft1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postIntent = new Intent(mContext, PostActivity.class);
                    postIntent.putExtra("activity_no", 1);
                    postIntent.putExtra("user_post", mLayoutList[position].get(1));
                    postIntent.putExtra("is_explore", true);
                    mContext.startActivity(postIntent);
                }
            });

            mVideoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postIntent = new Intent(mContext, PostActivity.class);
                    postIntent.putExtra("activity_no", 1);
                    postIntent.putExtra("user_post", mLayoutList[position].get(2));
                    postIntent.putExtra("is_explore", true);
                    mContext.startActivity(postIntent);
                }
            });
        }
    }
}
