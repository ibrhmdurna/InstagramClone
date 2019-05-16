package com.example.ibrhm.instagramclone.Share;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.ibrhm.instagramclone.R;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Flash;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareVideoFragment extends Fragment {

    private View mView;
    private CameraView mCameraView;
    private RelativeLayout mTakeView;
    private ImageView mFlashView, mRotateView;

    public ShareVideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_share_video, container, false);

        mCameraView = mView.findViewById(R.id.share_video_view);
        mTakeView = mView.findViewById(R.id.video_container);
        mFlashView = mView.findViewById(R.id.video_flash_view);
        mRotateView = mView.findViewById(R.id.video_rotate_view);

        takeVideo();
        flashActive();
        rotate();

        return mView;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void takeVideo(){

        long fileName = System.currentTimeMillis();
        final File filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Instagram Clone/Video/"+fileName+".mp4");

        mTakeView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){

                    mCameraView.startCapturingVideo(filePath);

                    return true;
                }else if(event.getAction() == MotionEvent.ACTION_UP){

                    mCameraView.stopCapturingVideo();

                    return true;
                }
                return false;
            }
        });

        mCameraView.addCameraListener(new CameraListener() {
            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);

                Intent shareToIntent = new Intent(getActivity(), ShareToActivity.class);
                shareToIntent.putExtra("share_photo", filePath.getAbsolutePath());
                shareToIntent.putExtra("share_type", "Video");
                startActivity(shareToIntent);
            }
        });
    }
    private void flashActive(){
        mFlashView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFlashView.isActivated()){
                    mFlashView.setActivated(false);
                    mCameraView.setFlash(Flash.OFF);
                }else{
                    mFlashView.setActivated(true);
                    mCameraView.setFlash(Flash.TORCH);
                }
            }
        });
    }

    private void rotate(){
        mRotateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraView.getFacing().equals(Facing.BACK)){
                    mCameraView.setFacing(Facing.FRONT);
                    mFlashView.setEnabled(false);
                    mFlashView.setActivated(false);
                }else{
                    mCameraView.setFacing(Facing.BACK);
                    mFlashView.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraView.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mCameraView != null){
            mCameraView.destroy();
        }
    }
}
