package com.example.ibrhm.instagramclone.Share;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.ibrhm.instagramclone.R;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareCameraFragment extends Fragment {

    private View mView;
    private CameraView mCameraView;

    private ImageView mRotateView, mFlashView;
    private RelativeLayout mTakePhoto;

    public ShareCameraFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_share_camera, container, false);

        mCameraView = mView.findViewById(R.id.share_camera_view);
        mRotateView = mView.findViewById(R.id.camera_rotate_view);
        mFlashView = mView.findViewById(R.id.camera_flash_view);
        mTakePhoto = mView.findViewById(R.id.camera_take_photo_view);

        mCameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM);
        mCameraView.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER);
        flashActive();
        rotate();
        takePhoto();

        return mView;
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

    private void takePhoto(){
        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraView.getFacing() == Facing.BACK){
                    mCameraView.capturePicture();
                }else{
                    mCameraView.captureSnapshot();
                }
            }
        });

        mCameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);

                long photoName = System.currentTimeMillis();
                File takePhoto = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Instagram Clone/"+photoName+".jpg");

                try {
                        FileOutputStream fileStream = new FileOutputStream(takePhoto);
                        fileStream.write(jpeg);
                        fileStream.close();

                    Intent shareToIntent = new Intent(getActivity(), ShareToActivity.class);
                    shareToIntent.putExtra("share_photo", takePhoto.getAbsolutePath());
                    shareToIntent.putExtra("share_type", "Photo");
                    startActivity(shareToIntent);
                } catch (IOException e) {
                    e.printStackTrace();
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
