package com.example.ibrhm.instagramclone.Profile;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.Login.LoginActivity;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.DialogHelper;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.App;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileSettingsFragment extends Fragment {

    private View mView;
    private ImageView mBackView;

    private TextView mProfileEdit;
    private TextView mPostsLiked;
    private TextView mPassword;
    private RelativeLayout mPrivateAccount;
    private TextView mLogOutView;
    private SwitchCompat mPrivateSwitch;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;

    public ProfileSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView =  inflater.inflate(R.layout.fragment_profile_settings, container, false);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = mAuth.getCurrentUser();

        setupView();
        setupOnBack();

        isPrivateAccount();

        // ----- Settings ------
        profileEdit();
        postsYouLiked();
        changePassword();
        privateAccount();
        logOut();

        return mView;
    }

    private void changePassword(){
        mPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new ChangePasswordFragment(), "Change_Password");
            }
        });
    }

    private void postsYouLiked() {
        mPostsLiked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new PostsYouLikedFragment(), "Posts_Liked");
            }
        });
    }

    private void isPrivateAccount(){
        mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("private_account").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    boolean isPrivate = (boolean) dataSnapshot.getValue();
                    mPrivateSwitch.setChecked(isPrivate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void privateAccount() {
        mPrivateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPrivateSwitch.isChecked()){
                    mPrivateSwitch.setChecked(false);
                    dialogPrivate(false);
                }
                else{
                    mPrivateSwitch.setChecked(true);
                    dialogPrivate(true);
                }
            }
        });
    }

    private void dialogPrivate(final boolean isChecked){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_private_account, null);
        mBuilder.setView(view);
        final AlertDialog dialog = mBuilder.create();

        TextView mTitle = view.findViewById(R.id.dialog_private_title);
        TextView mBody = view.findViewById(R.id.dialog_private_body);
        if(isChecked){
            mTitle.setText(getString(R.string.dialog_private_title));
            mBody.setText(getString(R.string.dialog_private_body));
        }
        else{
            mTitle.setText(getString(R.string.change_to_public_account));
            mBody.setText(getString(R.string.dialog_public_body));
        }


        Button mCancelBtn = view.findViewById(R.id.dialog_private_cancel_btn);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(isChecked){
                    mPrivateSwitch.setChecked(false);
                }
                else{
                    mPrivateSwitch.setChecked(true);
                }
            }
        });

        Button mOkBtn = view.findViewById(R.id.dialog_private_ok_btn);
        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                setPrivateAccount(isChecked);
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(isChecked){
                    mPrivateSwitch.setChecked(false);
                }
                else{
                    mPrivateSwitch.setChecked(true);
                }
            }
        });
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void setPrivateAccount(boolean isPrivate){
        if(!isPrivate){
            mRootRef.child("Follow_req").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            String user_id = ds.getKey();
                            Map followReqMap = new HashMap();
                            followReqMap.put("News/Follow_req/"+mCurrentUser.getUid()+"/"+user_id+"/user_id", null);
                            followReqMap.put("News/Follow_req/"+mCurrentUser.getUid()+"/"+user_id+"/time", null);
                            followReqMap.put("Follow_req/"+user_id+"/"+mCurrentUser.getUid()+"/user_id", null);
                            mRootRef.updateChildren(followReqMap);

                            String currentDate = java.text.DateFormat.getDateInstance().format(new Date());
                            Map followMap = new HashMap();
                            followMap.put("Followers/"+mCurrentUser.getUid()+"/"+user_id+"/user_id", user_id);
                            followMap.put("Followers/"+mCurrentUser.getUid()+"/"+user_id+"/date", currentDate);
                            followMap.put("Following/"+user_id+"/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                            followMap.put("Following/"+user_id+"/"+mCurrentUser.getUid()+"/date", currentDate);
                            mRootRef.updateChildren(followMap);

                            Map newsMap = new HashMap();
                            newsMap.put("News/Follow_interactions/"+mCurrentUser.getUid()+"/"+user_id+"/user_id", user_id);
                            newsMap.put("News/Follow_interactions/"+mCurrentUser.getUid()+"/"+user_id+"/date", currentDate);
                            newsMap.put("News/Follow_interactions/"+mCurrentUser.getUid()+"/"+user_id+"/time", ServerValue.TIMESTAMP);
                            mRootRef.updateChildren(newsMap);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("private_account").setValue(isPrivate);
    }

    private void setupOnBack(){
        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void profileEdit(){
        mProfileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(new ProfileEditFragment(), "Edit_Profile");
            }
        });
    }

    private void showFragment(Fragment fragment, String tag){
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.profile_container, fragment);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    private void logOut(){
        mLogOutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogLogOut();
            }
        });
    }

    private void dialogLogOut(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_log_out, null);
        mBuilder.setView(view);
        final AlertDialog dialog = mBuilder.create();

        Button mCancelBtn = view.findViewById(R.id.dialog_log_out_cancel_btn);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button mLogOutBtn = view.findViewById(R.id.dialog_log_out_btn);
        mLogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                final AlertDialog alertDialog = DialogHelper.dialogLoading(getActivity(), "Logging out...");
                alertDialog.show();
                mRootRef.child("Users").child(mCurrentUser.getUid()).child("fcm_token").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Handler h = new Handler();
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mAuth.signOut();
                                    alertDialog.dismiss();
                                    App.background.getBackgroundList().clear();
                                    sendToStart();
                                }
                            },1500);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Couldn't refresh feed", Toast.LENGTH_LONG).show();
                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mAuth.signOut();
                                alertDialog.dismiss();
                                App.background.getBackgroundList().clear();
                                sendToStart();
                            }
                        },1500);
                    }
                });
            }
        });

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void sendToStart(){
        Intent startIntent = new Intent(getActivity(), LoginActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);
        getActivity().finish();
    }

    private void setupView(){
        mBackView = mView.findViewById(R.id.settings_back_button);
        mLogOutView = mView.findViewById(R.id.log_out_view);
        mProfileEdit = mView.findViewById(R.id.profile_edit_view);
        mPrivateAccount = mView.findViewById(R.id.private_account_view);
        mPrivateSwitch = mView.findViewById(R.id.private_switch);
        mPostsLiked = mView.findViewById(R.id.posts_liked_view);
        mPassword = mView.findViewById(R.id.password_view);
    }
}
