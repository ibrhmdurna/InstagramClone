package com.example.ibrhm.instagramclone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.Models.UserPosts;
import com.example.ibrhm.instagramclone.R;

import java.util.HashMap;
import java.util.List;

public class ProfilePostsAdapter extends RecyclerView.Adapter<ProfilePostsAdapter.ImageViewHolder> {

    private LayoutInflater layoutInflater;
    private Context mContext;
    private List<UserPosts> mPostsList;
    private OnItemClickListener mListener;

    public ProfilePostsAdapter(List<UserPosts> postsList, Context context){
        mContext = context;
        mPostsList = postsList;
        if(context != null){
            layoutInflater = LayoutInflater.from(context);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.grid_image_profile_layout, viewGroup, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, int i) {
        if(mPostsList.get(i).getType().equals("Video")){
            imageViewHolder.mVideoView.setVisibility(View.VISIBLE);
            UniversalImageLoader.setImage(mPostsList.get(i).getThumb_image(), imageViewHolder.mImageView, null, "");
        }else{
            imageViewHolder.mVideoView.setVisibility(View.GONE);
            UniversalImageLoader.setImage(mPostsList.get(i).getPost_url(), imageViewHolder.mImageView, null, "");
        }
    }

    @Override
    public int getItemCount() {
        return mPostsList.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder{

        GridImageView mImageView;
        ImageView mVideoView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.profile_image_container);
            mVideoView = itemView.findViewById(R.id.profile_video_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
