package com.example.ibrhm.instagramclone.Share;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.PagerAdapter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

public class ShareActivity extends AppCompatActivity {

    private static String TAG = "ShareActivity";

    private ViewPager mShareContainer;
    private TabLayout mTabLayout;

    private ImageView mCloseView;

    private Spinner mToolbarSpinner;
    private TextView mPhotoText, mVideoText;
    private Button mNextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_share);

        mToolbarSpinner = findViewById(R.id.share_toolbar_spinner);
        mPhotoText = findViewById(R.id.share_toolbar_photo_text);
        mVideoText = findViewById(R.id.share_toolbar_video_text);
        mNextBtn = findViewById(R.id.share_toolbar_next_btn);

        goBack();

        Dexter.withActivity(this)
                .withPermissions(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.RECORD_AUDIO
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if(report.areAllPermissionsGranted()){
                    setupShareViewPager();
                }
                else if(report.isAnyPermissionPermanentlyDenied()){
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(ShareActivity.this);
                    View view = getLayoutInflater().inflate(R.layout.dialog_permission, null);
                    mBuilder.setView(view);
                    AlertDialog dialog = mBuilder.create();

                    Button mCancelBtn = view.findViewById(R.id.dialog_permission_cancel_btn);
                    mCancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ShareActivity.super.onBackPressed();
                        }
                    });

                    Button mSettingsBtn = view.findViewById(R.id.dialog_permission_settings_btn);
                    mSettingsBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            settingsIntent.setData(uri);
                            startActivity(settingsIntent);
                        }
                    });

                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            ShareActivity.super.onBackPressed();
                        }
                    });
                    dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                }
                else {
                    ShareActivity.super.onBackPressed();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                ShareActivity.super.onBackPressed();
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }).check();
    }

    private void setupShareViewPager() {
        mShareContainer = findViewById(R.id.share_container);
        mTabLayout = findViewById(R.id.share_tab_layout);

        ArrayList<String> mTabList = new ArrayList<>();
        mTabList.add(getString(R.string.gallery));
        mTabList.add(getString(R.string.photo));
        mTabList.add(getString(R.string.video));

        final PagerAdapter sharePagerAdapter = new PagerAdapter(getSupportFragmentManager(), mTabList);
        sharePagerAdapter.addFragment(new ShareGalleryFragment());
        sharePagerAdapter.addFragment(new ShareCameraFragment());
        sharePagerAdapter.addFragment(new ShareVideoFragment());

        mShareContainer.setAdapter(sharePagerAdapter);
        mShareContainer.setOffscreenPageLimit(1);

        sharePagerAdapter.selectRemoveFragment(mShareContainer, 1);
        sharePagerAdapter.selectRemoveFragment(mShareContainer, 2);

        mTabLayout.setupWithViewPager(mShareContainer);

        mShareContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if(i == 0){

                    sharePagerAdapter.selectRemoveFragment(mShareContainer, 1);
                    sharePagerAdapter.selectRemoveFragment(mShareContainer, 2);
                    sharePagerAdapter.selectAddFragment(mShareContainer, 0);

                    if(mToolbarSpinner.getVisibility() == View.INVISIBLE){
                        AlphaAnimation fadeIn = new AlphaAnimation(0f,1f);
                        fadeIn.setDuration(300);
                        fadeIn.setFillAfter(true);
                        mToolbarSpinner.setVisibility(View.VISIBLE);
                        mToolbarSpinner.setAnimation(fadeIn);
                    }
                    if(mNextBtn.getVisibility() == View.INVISIBLE){
                        AlphaAnimation fadeIn = new AlphaAnimation(0f,1f);
                        fadeIn.setDuration(300);
                        fadeIn.setFillAfter(true);
                        mNextBtn.setVisibility(View.VISIBLE);
                        mNextBtn.setAnimation(fadeIn);
                    }
                    if(mPhotoText.getVisibility() == View.VISIBLE){
                        AlphaAnimation fadeOut = new AlphaAnimation(1f,0f);
                        fadeOut.setDuration(300);
                        mPhotoText.setVisibility(View.INVISIBLE);
                        mPhotoText.setAnimation(fadeOut);
                    }
                    if(mVideoText.getVisibility() == View.VISIBLE){
                        AlphaAnimation fadeOut = new AlphaAnimation(1f,0f);
                        fadeOut.setDuration(300);
                        mVideoText.setVisibility(View.INVISIBLE);
                        mVideoText.setAnimation(fadeOut);
                    }
                }
                if(i == 1){

                    sharePagerAdapter.selectRemoveFragment(mShareContainer, 0);
                    sharePagerAdapter.selectRemoveFragment(mShareContainer, 2);
                    sharePagerAdapter.selectAddFragment(mShareContainer, 1);

                    if(mToolbarSpinner.getVisibility() == View.VISIBLE){
                        AlphaAnimation fadeOut = new AlphaAnimation(1f,0f);
                        fadeOut.setDuration(300);
                        mToolbarSpinner.setVisibility(View.INVISIBLE);
                        mToolbarSpinner.setAnimation(fadeOut);
                    }
                    if(mNextBtn.getVisibility() == View.VISIBLE){
                        AlphaAnimation fadeOut = new AlphaAnimation(1f,0f);
                        fadeOut.setDuration(300);
                        mNextBtn.setVisibility(View.INVISIBLE);
                        mNextBtn.setAnimation(fadeOut);
                    }
                    if(mPhotoText.getVisibility() == View.INVISIBLE){
                        AlphaAnimation fadeIn = new AlphaAnimation(0f,1f);
                        fadeIn.setDuration(300);
                        fadeIn.setFillAfter(true);
                        mPhotoText.setVisibility(View.VISIBLE);
                        mPhotoText.setAnimation(fadeIn);
                    }
                    if(mVideoText.getVisibility() == View.VISIBLE){
                        AlphaAnimation fadeOut = new AlphaAnimation(1f,0f);
                        fadeOut.setDuration(300);
                        mVideoText.setVisibility(View.INVISIBLE);
                        mVideoText.setAnimation(fadeOut);
                    }
                }
                if(i == 2){

                    sharePagerAdapter.selectRemoveFragment(mShareContainer, 1);
                    sharePagerAdapter.selectRemoveFragment(mShareContainer, 0);
                    sharePagerAdapter.selectAddFragment(mShareContainer, 2);

                    if(mToolbarSpinner.getVisibility() == View.VISIBLE){
                        AlphaAnimation fadeOut = new AlphaAnimation(1f,0f);
                        fadeOut.setDuration(300);
                        mToolbarSpinner.setVisibility(View.INVISIBLE);
                        mToolbarSpinner.setAnimation(fadeOut);
                    }
                    if(mNextBtn.getVisibility() == View.VISIBLE){
                        AlphaAnimation fadeOut = new AlphaAnimation(1f,0f);
                        fadeOut.setDuration(300);
                        mNextBtn.setVisibility(View.INVISIBLE);
                        mNextBtn.setAnimation(fadeOut);
                    }
                    if(mPhotoText.getVisibility() == View.VISIBLE){
                        AlphaAnimation fadeOut = new AlphaAnimation(1f,0f);
                        fadeOut.setDuration(300);
                        mPhotoText.setVisibility(View.INVISIBLE);
                        mPhotoText.setAnimation(fadeOut);
                    }
                    if(mVideoText.getVisibility() == View.INVISIBLE){
                        AlphaAnimation fadeIn = new AlphaAnimation(0f,1f);
                        fadeIn.setDuration(300);
                        fadeIn.setFillAfter(true);
                        mVideoText.setVisibility(View.VISIBLE);
                        mVideoText.setAnimation(fadeIn);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    private void goBack(){
        mCloseView = findViewById(R.id.share_close_button);

        mCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareActivity.super.onBackPressed();
            }
        });
    }
}
