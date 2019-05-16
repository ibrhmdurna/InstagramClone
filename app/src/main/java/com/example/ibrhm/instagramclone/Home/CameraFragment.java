package com.example.ibrhm.instagramclone.Home;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.ibrhm.instagramclone.R;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {

    private View mView;
    private CameraView mCameraView;
    private ImageView mRotateView, mFlashView;
    private RelativeLayout mTakeView;

    public static boolean permission = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_camera, container, false);

        mCameraView = mView.findViewById(R.id.camera_view);
        mRotateView = mView.findViewById(R.id.rotate_view);
        mFlashView = mView.findViewById(R.id.flash_view);
        mTakeView = mView.findViewById(R.id.camera_take_view);

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
                    mCameraView.setFlash(Flash.ON);
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
        mTakeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraView.getFacing() == Facing.BACK){
                    mCameraView.capturePicture();
                }else{
                    mCameraView.captureSnapshot();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(permission){
            mCameraView.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraView.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mCameraView!= null){
            mCameraView.destroy();
        }
    }
}
