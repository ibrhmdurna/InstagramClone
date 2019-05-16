package com.example.ibrhm.instagramclone.Home;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.Login.LoginActivity;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.HomePagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static int ACTIVITY_NO = 0;
    private static String TAG = "HomeActivity";

    private ViewPager mViewPager;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        if(mCurrentUser != null){
            setupHomeViewPager();
        }
    }

    private void setupHomeViewPager(){
        mViewPager = findViewById(R.id.homeViewPager);

        final HomePagerAdapter homePagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        homePagerAdapter.addFragment(new CameraFragment());
        homePagerAdapter.addFragment(new HomeFragment());
        homePagerAdapter.addFragment(new DirectFragment());

        mViewPager.setAdapter(homePagerAdapter);

        mViewPager.setCurrentItem(1);

        homePagerAdapter.selectRemoveFragment(mViewPager, 0);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if(i == 0){
                    HomeActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    HomeActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    HomeActivity.this.setTheme(R.style.CustomBlackActivity);

                    getRequestPermissions();
                    DirectFragment.isOpenDirect = false;

                    homePagerAdapter.selectAddFragment(mViewPager, 0);
                }
                if(i == 1){
                    HomeActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    HomeActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    HomeActivity.this.setTheme(R.style.CustomHomeActivity);

                    homePagerAdapter.selectRemoveFragment(mViewPager, 0);
                    DirectFragment.isOpenDirect = false;
                }
                if(i == 2){
                    HomeActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    HomeActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    HomeActivity.this.setTheme(R.style.CustomHomeActivity);

                    homePagerAdapter.selectRemoveFragment(mViewPager, 0);
                    DirectFragment.isOpenDirect = true;
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(2);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private void getRequestPermissions() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if(report.areAllPermissionsGranted()){
                    CameraFragment.permission = true;
                }
                else if(report.isAnyPermissionPermanentlyDenied()){
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(HomeActivity.this);
                    View view = getLayoutInflater().inflate(R.layout.dialog_permission, null);
                    mBuilder.setView(view);
                    final AlertDialog dialog = mBuilder.create();

                    Button mCancelBtn = view.findViewById(R.id.dialog_permission_cancel_btn);
                    mCancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            mViewPager.setCurrentItem(1,true);
                        }
                    });

                    Button mSettingsBtn = view.findViewById(R.id.dialog_permission_settings_btn);
                    mSettingsBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            settingsIntent.setData(uri);
                            startActivity(settingsIntent);
                        }
                    });

                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mViewPager.setCurrentItem(1,true);
                        }
                    });
                    dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                }
                else{
                    mViewPager.setCurrentItem(1,true);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                mViewPager.setCurrentItem(1,true);
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }).check();;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrentUser == null){
            sendToStart();
        }
    }

    private void sendToStart(){
        Intent startIntent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem() != 1){
            mViewPager.setCurrentItem(1, true);
        }else{
            super.onBackPressed();
        }
    }
}
