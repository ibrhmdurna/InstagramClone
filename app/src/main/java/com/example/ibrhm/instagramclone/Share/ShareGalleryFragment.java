package com.example.ibrhm.instagramclone.Share;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.FileOperations;
import com.example.ibrhm.instagramclone.Utils.ShareGalleryAdapter;
import com.example.ibrhm.instagramclone.Utils.UniversalImageLoader;
import com.naver.android.helloyako.imagecrop.view.ImageCropView;
import com.universalvideoview.UniversalVideoView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareGalleryFragment extends Fragment {

    private View mView;
    private List<String> mFiles;
    private List<String> mFilesTitle;

    private Spinner mSpinner;
    private RecyclerView mGalleryContainer;
    private ShareGalleryAdapter mGalleryAdapter;
    private GridLayoutManager mLayoutManager;

    private ImageCropView mCropImageView;
    private UniversalVideoView mVideoView;

    private Button mNextBtn;

    private String selectPhotoPath;
    private String isFileType;

    public ShareGalleryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_share_gallery, container, false);

        mSpinner = getActivity().findViewById(R.id.share_toolbar_spinner);
        mNextBtn = getActivity().findViewById(R.id.share_toolbar_next_btn);
        mGalleryContainer = mView.findViewById(R.id.gallery_container);
        mCropImageView = mView.findViewById(R.id.share_crop_image_view);
        mVideoView = mView.findViewById(R.id.share_video_view);

        setupGalleryPath();
        setupShareTo();
        return mView;
    }

    private void setupGalleryPath(){
        mFiles = new ArrayList<>();
        mFilesTitle = new ArrayList<>();

        String root = Environment.getExternalStorageDirectory().getPath();
        String cameraPath = root + "/DCIM/Camera";
        String downloadPath = root + "/Download";
        String whatsAppPath = root + "/WhatsApp/Media/WhatsApp Images";
        String whatsAppVideoPath = root + "/WhatsApp/Media/WhatsApp Video";
        String screenShotPath = root + "/DCIM/Screenshots";
        String twitterPath = root + "/Pictures/Twitter";

        mFilesTitle.add("Gallery");

        if(FileOperations.doYouHaveFile(cameraPath)){
            mFiles.add(cameraPath);
            mFilesTitle.add("Camera");
        }
        if(FileOperations.doYouHaveFile(downloadPath)){
            mFiles.add(downloadPath);
            mFilesTitle.add("Download");
        }
        if(FileOperations.doYouHaveFile(twitterPath)){
            mFiles.add(twitterPath);
            mFilesTitle.add("Twitter");
        }
        if(FileOperations.doYouHaveFile(screenShotPath)){
            mFiles.add(screenShotPath);
            mFilesTitle.add("ScreenShots");
        }
        if(FileOperations.doYouHaveFile(whatsAppPath)){
            mFiles.add(whatsAppPath);
            mFilesTitle.add("WhatsApp Images");
        }
        if(FileOperations.doYouHaveFile(whatsAppVideoPath)){
            mFiles.add(whatsAppVideoPath);
            mFilesTitle.add("WhatsApp Video");
        }

        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, mFilesTitle);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(spinnerArrayAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if(position == 0){
                    setupRecyclerView(FileOperations.getAllGallery(getActivity()));
                    if(mFiles.size() > 0){
                        if(FileOperations.getFolderFile(mFiles.get(position)).size() > 0){
                            showPhotoOrVideo(FileOperations.getFolderFile(mFiles.get(position)).get(0));
                            selectPhotoPath = FileOperations.getFolderFile(mFiles.get(position)).get(0);
                        }
                    }
                }
                else{
                    setupRecyclerView(FileOperations.getFolderFile(mFiles.get(position - 1)));
                    if(FileOperations.getFolderFile(mFiles.get(position - 1)).size() > 0){
                        showPhotoOrVideo(FileOperations.getFolderFile(mFiles.get(position - 1)).get(0));
                        selectPhotoPath = FileOperations.getFolderFile(mFiles.get(position - 1)).get(0);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupRecyclerView(final ArrayList<String> selectFolderFiles) {
        mGalleryAdapter = new ShareGalleryAdapter(selectFolderFiles, getActivity());
        mGalleryContainer.setAdapter(mGalleryAdapter);

        mLayoutManager = new GridLayoutManager(getActivity(), 4);
        mGalleryContainer.setLayoutManager(mLayoutManager);

        mGalleryContainer.setHasFixedSize(true);
        mGalleryContainer.setItemViewCacheSize(30);
        mGalleryContainer.setDrawingCacheEnabled(true);
        mGalleryContainer.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

        mGalleryAdapter.setOnItemClickListener(new ShareGalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showPhotoOrVideo(selectFolderFiles.get(position));
                selectPhotoPath = selectFolderFiles.get(position);
                mGalleryAdapter.selectedPosition = position;
                mGalleryAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showPhotoOrVideo(String path) {

        String fileType = path.substring(path.lastIndexOf("."));

        if(!fileType.isEmpty()){
            if(fileType.equals(".mp4") || fileType.equals(".3gp")){
                mVideoView.setVisibility(View.VISIBLE);
                mCropImageView.setVisibility(View.GONE);
                mVideoView.setVideoURI(Uri.parse("file://"+path));
                mVideoView.start();
                isFileType = "Video";
            }else{
                mVideoView.setVisibility(View.GONE);
                mCropImageView.setVisibility(View.VISIBLE);
                UniversalImageLoader.setImage(path, mCropImageView, null, "file://");
                isFileType = "Photo";
            }
        }
    }

    private void setupShareTo(){
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isFileType.equals("Photo")){

                    Bitmap cropImage = mCropImageView.getCroppedImage();
                    String cropImagePath = FileOperations.cropImage(cropImage);
                    Intent shareToIntent = new Intent(getActivity(), ShareToActivity.class);
                    shareToIntent.putExtra("share_photo", cropImagePath);
                    shareToIntent.putExtra("share_type", isFileType);
                    startActivity(shareToIntent);

                }
                else{
                    mVideoView.stopPlayback();
                    Intent shareToIntent = new Intent(getActivity(), ShareToActivity.class);
                    shareToIntent.putExtra("share_photo", selectPhotoPath);
                    shareToIntent.putExtra("share_type", isFileType);
                    startActivity(shareToIntent);
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mVideoView.stopPlayback();
    }
}
