package com.example.ibrhm.instagramclone.Profile;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.DialogHelper;
import com.example.ibrhm.instagramclone.Utils.EventBusDataEvents;
import com.example.ibrhm.instagramclone.Utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileEditFragment extends Fragment {

    private View mView;
    private ImageView mCloseView, mDoneView;
    private ProgressBar mProgressBar;
    private CircleImageView mProfileImage;
    private EditText mUsername, mWebSite;
    private EmojiEditText mName;
    private EmojiTextView mBiography;

    private String mProfileUrl, mUserName, mFullName, mBio, mWebsite;

    private TextView mChangePhotoView;

    private static int GALLERY_PICK = 0;
    private Uri photoUri;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private StorageReference mImageStorage;

    private AlertDialog dialogBio;

    public ProfileEditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mDoneView = mView.findViewById(R.id.edit_done_button);
        mProgressBar = mView.findViewById(R.id.edit_progress_bar);
        mProfileImage = mView.findViewById(R.id.edit_profile_image);
        mUsername = mView.findViewById(R.id.edit_username_input);
        mWebSite = mView.findViewById(R.id.edit_website_input);
        mName = mView.findViewById(R.id.edit_name_input);
        mBiography = mView.findViewById(R.id.edit_bio_input);
        mChangePhotoView = mView.findViewById(R.id.change_photo_view);

        setupOnBack();
        setupSaveInformation();
        getEditInfo();
        setupChangePhoto();
        userNameWatcher();
        setupDialogBio();
        setupSaveChange();

        return mView;
    }

    private void setupOnBack(){
        mCloseView = mView.findViewById(R.id.edit_close_button);

        mCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard(getActivity());
                if(mName.isSaveEnabled() || mUsername.isSaveEnabled() || mWebSite.isSaveEnabled()){
                    dialogUnSaved();
                }else{
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    private void setupSaveChange(){
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mName.getText().toString().equals(mFullName)){
                    mName.setSaveEnabled(false);
                }else{
                    mName.setSaveEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mWebSite.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mWebSite.getText().toString().equals(mWebsite)){
                    mWebSite.setSaveEnabled(false);
                }else{
                    mWebSite.setSaveEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupSaveInformation(){
        mDoneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard(getActivity());
                mDoneView.setVisibility(View.GONE);
                mProgressBar.setIndeterminate(true);
                mProgressBar.setVisibility(View.VISIBLE);

                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mUsername.isSaveEnabled()){
                            isValidUserName(mUsername.getText().toString());
                        }
                        else{
                            Map userMap = new HashMap();
                            userMap.put("user_name", mUsername.getText().toString());
                            userMap.put("full_name", mName.getText().toString());
                            userMap.put("details/web_site", mWebSite.getText().toString());

                            mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        mDoneView.setVisibility(View.VISIBLE);
                                        mProgressBar.setIndeterminate(false);
                                        mProgressBar.setVisibility(View.GONE);
                                        Objects.requireNonNull(getActivity()).onBackPressed();
                                    }
                                    else{
                                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                }, 1200);
            }
        });
    }

    private void isValidUserName(final String username){
        mRootRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isValid = false;

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.exists()){
                        if(ds.hasChild("user_name")){
                            if(ds.child("user_name").getValue().toString().equals(username)){
                                isValid = true;
                                break;
                            }
                        }
                    }
                }

                if(isValid){
                    Toast.makeText(getContext(), "This username isn't available. Please try another.", Toast.LENGTH_LONG).show();
                    mDoneView.setVisibility(View.VISIBLE);
                    mProgressBar.setIndeterminate(false);
                    mProgressBar.setVisibility(View.GONE);
                }
                else{
                    Map userMap = new HashMap();
                    userMap.put("user_name", mUsername.getText().toString());
                    userMap.put("full_name", mName.getText().toString());
                    userMap.put("details/web_site", mWebSite.getText().toString());
                    userMap.put("details/biography", mBiography.getText().toString());

                    mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                mDoneView.setVisibility(View.VISIBLE);
                                mProgressBar.setIndeterminate(false);
                                mProgressBar.setVisibility(View.GONE);
                                Objects.requireNonNull(getActivity()).onBackPressed();
                            }
                            else{
                                Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void userNameWatcher(){
        mUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mUsername.getText().toString().equals(mUserName)){
                    mUsername.setSaveEnabled(false);
                }else{
                    mUsername.setSaveEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mUsername.getText().length() > 0){
                    mDoneView.setEnabled(true);
                }else{
                    mDoneView.setEnabled(false);
                }
            }
        });
    }

    @Subscribe(sticky = true)
    public void onUserValueEvent(EventBusDataEvents.SendUserInfo value){
        mProfileUrl = value.getUser().getDetails().getProfile_image();
        mUserName = value.getUser().getUser_name();
        mFullName = value.getUser().getFull_name();
        mBio = value.getUser().getDetails().getBiography();
        mWebsite = value.getUser().getDetails().getWeb_site();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    private void getEditInfo(){
        UniversalImageLoader.setImage(mProfileUrl, mProfileImage, null, "");
        mName.setText(mFullName);
        mUsername.setText(mUserName);
        mBiography.setText(mBio);
        mWebSite.setText(mWebsite);
    }

    private void setupChangePhoto(){
        mChangePhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, GALLERY_PICK);
            }
        });
    }

    private void uploadPhoto(Uri uri){
        final AlertDialog loading = DialogHelper.dialogLoading(getActivity(), null);
        loading.show();

        final StorageReference filepath = mImageStorage.child("Users").child(mAuth.getCurrentUser().getUid()).child("profile_image.jpg");

        filepath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();

                            mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("details").child("profile_image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        loading.dismiss();
                                        Toast.makeText(getContext(), "Profile photo changed.", Toast.LENGTH_SHORT).show();
                                        mDoneView.setVisibility(View.VISIBLE);
                                        mProgressBar.setIndeterminate(false);
                                        mProgressBar.setVisibility(View.GONE);
                                        Objects.requireNonNull(getActivity()).onBackPressed();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loading.dismiss();
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    mDoneView.setVisibility(View.VISIBLE);
                                    mProgressBar.setIndeterminate(false);
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loading.dismiss();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            mDoneView.setVisibility(View.VISIBLE);
                            mProgressBar.setIndeterminate(false);
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loading.dismiss();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                mDoneView.setVisibility(View.VISIBLE);
                mProgressBar.setIndeterminate(false);
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void setupDialogBio(){
        mBiography.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBio = dialogBio();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == AppCompatActivity.RESULT_OK && data.getData() != null){

            photoUri = data.getData();

            mProfileImage.setImageURI(photoUri);
            mProfileImage.setSaveEnabled(true);
            uploadPhoto(photoUri);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ProfileActivity.editFragment = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        closeKeyboard(getActivity());
        ProfileActivity.editFragment = false;
    }

    private void closeKeyboard(Activity activity){
        View view = activity.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private AlertDialog dialogBio(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_bio, null);
        mBuilder.setView(view);
        final AlertDialog dialog = mBuilder.create();

        final ProgressBar mProgressBar = view.findViewById(R.id.dialog_bio_progress_bar);
        final EmojiEditText mBioText = view.findViewById(R.id.dialog_bio_input);

        mBioText.setText(mBio);

        mBioText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mBioText.getText().toString().equals(mBio)){
                    mBioText.setSaveEnabled(false);
                }else{
                    mBioText.setSaveEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        final Button mCancelBtn = view.findViewById(R.id.dialog_bio_cancel_btn);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBioText.isSaveEnabled()){
                    dialog.cancel();
                    dialogBioUnSaved();
                }else{
                    dialog.dismiss();
                }
            }
        });

        final Button mBioBtn = view.findViewById(R.id.dialog_bio_btn);
        mBioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard(getActivity());
                dialog.setCancelable(false);
                mCancelBtn.setEnabled(false);
                mBioText.setEnabled(false);
                mBioBtn.setEnabled(false);
                mProgressBar.setIndeterminate(true);
                mProgressBar.setVisibility(View.VISIBLE);
                mBioBtn.setVisibility(View.INVISIBLE);

                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("details").child("biography").setValue(mBioText.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                dialog.dismiss();
                                mBio = mBioText.getText().toString();
                                mBiography.setText(mBioText.getText().toString());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                },1000);

            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                closeKeyboard(getActivity());
            }
        });
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        return dialog;
    }

    private void dialogUnSaved(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_unsaved, null);
        mBuilder.setView(view);
        final AlertDialog dialog = mBuilder.create();

        Button mNoBtn = view.findViewById(R.id.dialog_unsaved_no_btn);
        mNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button mYesBtn = view.findViewById(R.id.dialog_unsaved_yes_btn);
        mYesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void dialogBioUnSaved(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_bio_unsaved, null);
        mBuilder.setView(view);
        final AlertDialog dialog = mBuilder.create();

        Button mNoBtn = view.findViewById(R.id.dialog_bio_unsaved_no_btn);
        mNoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialogBio.show();
            }
        });

        Button mYesBtn = view.findViewById(R.id.dialog_bio_unsaved_yes_btn);
        mYesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
}
