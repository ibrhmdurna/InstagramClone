package com.example.ibrhm.instagramclone.Profile;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChangePasswordFragment extends Fragment {

    private View mView;

    private ImageView mBackView;
    private EditText mCurrentPassword, mNewPassword, mNewPasswordAgain;
    private ImageView mDoneBtn;
    private ProgressBar mLoadingBar;

    private FirebaseUser mCurrentUser;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_change_password, container, false);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        setupView();
        onBack();
        mDoneBtn.setEnabled(false);
        mCurrentPassword.addTextChangedListener(inputWatcher);
        mNewPassword.addTextChangedListener(inputWatcher);
        mNewPasswordAgain.addTextChangedListener(inputWatcher);

        checkPassword();

        mNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mNewPassword.getText().length() > 5 && mNewPassword.isSaveEnabled()){
                    mNewPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock_icon, 0, 0, 0);
                    mNewPassword.setSaveEnabled(false);
                }
            }
        });
        mNewPasswordAgain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mNewPassword.getText().length() > 5 && mNewPasswordAgain.getText().length() > 5 &&
                        mNewPassword.getText().toString().equals(mNewPasswordAgain.getText().toString())){
                    mNewPasswordAgain.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock_icon, 0, 0, 0);
                    mNewPasswordAgain.setSaveEnabled(false);
                }
            }
        });

        changePassword();

        return mView;
    }

    private void changePassword(){
        mDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoneBtn.setVisibility(View.GONE);
                mLoadingBar.setVisibility(View.VISIBLE);
                mLoadingBar.setIndeterminate(true);

                if(mNewPassword.getText().length() > 5 && mNewPasswordAgain.getText().length() > 5){
                    if(mNewPassword.getText().toString().equals(mNewPasswordAgain.getText().toString())){
                        if(mCurrentUser != null){
                            AuthCredential credential = EmailAuthProvider.getCredential(mCurrentUser.getEmail(), mCurrentPassword.getText().toString());

                            mCurrentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        mCurrentUser.updatePassword(mNewPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    getFragmentManager().popBackStack();
                                                    mDoneBtn.setVisibility(View.VISIBLE);
                                                    mLoadingBar.setVisibility(View.GONE);
                                                    mLoadingBar.setIndeterminate(false);
                                                }
                                                else{
                                                    dialogError();
                                                    mDoneBtn.setVisibility(View.VISIBLE);
                                                    mLoadingBar.setVisibility(View.GONE);
                                                    mLoadingBar.setIndeterminate(false);
                                                }
                                            }
                                        });

                                    }
                                    else{
                                        dialogError();
                                        mDoneBtn.setVisibility(View.VISIBLE);
                                        mLoadingBar.setVisibility(View.GONE);
                                        mLoadingBar.setIndeterminate(false);
                                    }
                                }
                            });
                        }
                    }
                    else{
                        Toast.makeText(getContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show();
                        mDoneBtn.setVisibility(View.VISIBLE);
                        mLoadingBar.setVisibility(View.GONE);
                        mLoadingBar.setIndeterminate(false);
                    }
                }
                else{
                    Toast.makeText(getContext(), "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                    mDoneBtn.setVisibility(View.VISIBLE);
                    mLoadingBar.setVisibility(View.GONE);
                    mLoadingBar.setIndeterminate(false);
                }
            }
        });
    }

    private void checkPassword(){
        mNewPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(mNewPassword.getText().length() > 5){
                        mNewPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock_icon, 0, 0, 0);
                        mNewPassword.setSaveEnabled(false);
                        mNewPassword.setActivated(true);
                    }
                    else{
                        mNewPassword.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock_red_icon, 0, 0, 0);
                        if(getFragmentManager().getBackStackEntryCount() > 1){
                            Toast.makeText(getContext(), "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                        }
                        mNewPassword.setSaveEnabled(true);
                    }
                }
            }
        });

        mNewPasswordAgain.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(mNewPassword.isActivated() && !hasFocus){
                    if(mNewPassword.getText().toString().equals(mNewPasswordAgain.getText().toString())){
                        mNewPasswordAgain.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock_icon, 0, 0, 0);
                        mNewPasswordAgain.setSaveEnabled(false);
                    }
                    else{
                        mNewPasswordAgain.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock_red_icon, 0, 0, 0);
                        if(getFragmentManager().getBackStackEntryCount() > 1){
                            Toast.makeText(getContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show();
                        }
                        mNewPasswordAgain.setSaveEnabled(true);
                    }
                }
            }
        });
    }

    private void dialogError(){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_password_error, null);
        mBuilder.setView(view);
        final AlertDialog dialog = mBuilder.create();

        Button mDismissBtn = view.findViewById(R.id.dialog_error_dismiss_btn);
        mDismissBtn.setOnClickListener(new View.OnClickListener() {
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

    private TextWatcher inputWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(mCurrentPassword.getText().length() > 0 && mNewPassword.getText().length() > 0 && mNewPasswordAgain.getText().length() > 0){
                mDoneBtn.setEnabled(true);
            }
            else{
                mDoneBtn.setEnabled(false);
            }
        }
    };

    private void onBack(){
        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
    }

    private void setupView(){
        mBackView = mView.findViewById(R.id.password_close_button);
        mCurrentPassword = mView.findViewById(R.id.current_password_input);
        mNewPassword = mView.findViewById(R.id.new_password_input);
        mNewPasswordAgain = mView.findViewById(R.id.new_password_again_input);
        mDoneBtn = mView.findViewById(R.id.password_done_button);
        mLoadingBar = mView.findViewById(R.id.password_progress_bar);
    }
}
