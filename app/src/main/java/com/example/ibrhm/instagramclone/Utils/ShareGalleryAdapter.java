package com.example.ibrhm.instagramclone.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ibrhm.instagramclone.R;

import java.util.List;

public class ShareGalleryAdapter extends RecyclerView.Adapter<ShareGalleryAdapter.ImageViewHolder> {

    private LayoutInflater layoutInflater;
    private Context mContext;
    private List<String> mFolderFiles;
    private OnItemClickListener mListener;
    public int selectedPosition;

    public ShareGalleryAdapter(List<String> folderFiles, Context context){
        mContext = context;
        mFolderFiles = folderFiles;
        layoutInflater = LayoutInflater.from(context);
        selectedPosition = 0;
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
        View view = layoutInflater.inflate(R.layout.grid_image_layout, viewGroup, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, int i) {
        String filePath = mFolderFiles.get(i);
        String fileType = filePath.substring(filePath.lastIndexOf("."));

        if(fileType.equals(".mp4") || fileType.equals(".3gp")){
            imageViewHolder.mVideoTimeView.setVisibility(View.VISIBLE);
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mContext, Uri.parse("file://"+filePath));

            String videoTime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long longTime = Long.parseLong(videoTime);
            imageViewHolder.mVideoTimeView.setText(convertDuration(longTime));
        }else{
            imageViewHolder.mVideoTimeView.setVisibility(View.GONE);
        }

        UniversalImageLoader.setImage(mFolderFiles.get(i), imageViewHolder.mImageView, null, "file:/");

        if(i == selectedPosition){
            imageViewHolder.mImageView.setSelected(true);
        }else{
            imageViewHolder.mImageView.setSelected(false);
        }

    }

    @SuppressLint("DefaultLocale")
    private String convertDuration(long duration) {
        long second = duration / 1000 % 60;
        long minute = duration / (1000 * 60) % 60;
        long hour = duration / (1000 * 60 * 60) % 24;

        String time;
        if(hour > 0){
            time = String.format("%02d:%02d,%02d", hour, minute, second);
        }else{
            time = String.format("%02d:%02d", minute, second);
        }

        return time;
    }

    @Override
    public int getItemCount() {
        return mFolderFiles.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder{

        GridImageView mImageView;
        TextView mVideoTimeView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.share_image_container);
            mVideoTimeView = itemView.findViewById(R.id.share_video_time);

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
