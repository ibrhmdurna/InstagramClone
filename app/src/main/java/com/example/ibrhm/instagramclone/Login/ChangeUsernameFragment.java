package com.example.ibrhm.instagramclone.Login;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.Home.HomeActivity;
import com.example.ibrhm.instagramclone.Models.Details;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.DialogHelper;
import com.example.ibrhm.instagramclone.Utils.EventBusDataEvents;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChangeUsernameFragment extends Fragment {

    private View mView;

    private String mPhoneNumber, mEmail, mVerificationID, mFullName,mUserName, mPassword;

    private EditText mUserNameInput;
    private Button mNextBtn;
    private TextView mUserNameErrorText;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;

    public ChangeUsernameFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_change_username, container, false);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUserNameInput = mView.findViewById(R.id.username_input);
        mNextBtn = mView.findViewById(R.id.username_next_btn);
        mUserNameErrorText = mView.findViewById(R.id.username_error_text);

        setupUserName();
        setupUserNameWatcher();
        setupRegisterFinish();

        return mView;
    }

    @Subscribe(sticky = true)
    public void onUserValueEvent(EventBusDataEvents.SendValue value){
        mPhoneNumber = value.getPhoneNumber();
        mEmail = value.getEmail();
        mVerificationID = value.getVerificationId();
        mFullName = value.getFullName();
        mPassword = value.getPassword();
        mUserName = value.getUserName();
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

    private void setupUserName(){
        mUserNameInput.setText(mUserName);
    }

    private void setupUserNameWatcher(){
        mUserNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isValidUserName();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mUserNameInput.getText().length() > 0){
                    mNextBtn.setEnabled(true);
                }else{
                    mNextBtn.setEnabled(false);
                }
            }
        });
    }

    private void isValidUserName(){
        if(TextUtils.isEmpty(mUserNameInput.getText())){
            mUserNameInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_default_style));
            mUserNameInput.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            mUserNameErrorText.setVisibility(View.GONE);
            return;
        }

        mUserNameInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_default_style));
        mUserNameInput.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        mUserNameErrorText.setVisibility(View.GONE);

        mRootRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                boolean isValid = false;

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.exists()){
                        if(ds.hasChild("user_name")){
                            if(ds.child("user_name").getValue().toString().equals(mUserNameInput.getText().toString())){
                                isValid = true;
                                break;
                            }
                        }
                    }
                }

                Handler h = new Handler();
                final boolean finalIsValid = isValid;
                h.postDelayed(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        if(TextUtils.isEmpty(mUserNameInput.getText())){
                            mUserNameInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_default_style));
                            mUserNameInput.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                            mUserNameErrorText.setVisibility(View.GONE);
                        }else{
                            if(finalIsValid){
                                mUserNameInput.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                                mUserNameInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_error_style));
                                mUserNameErrorText.setVisibility(View.VISIBLE);
                                mUserNameErrorText.setText("This username " + mUserNameInput.getText().toString() + " is not available.");
                            }else{
                                if(!finalIsValid)
                                    mUserNameInput.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_done_green_icon,0);
                                mUserNameInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_default_style));
                                mUserNameErrorText.setVisibility(View.GONE);
                            }
                        }
                    }
                }, 2500);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupRegisterFinish(){
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                closeKeyboard(getActivity());
                final AlertDialog dialog = DialogHelper.dialogLoading(getActivity(), "Registering...");
                dialog.show();

                if(TextUtils.isEmpty(mUserNameInput.getText())){
                    mUserNameInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_default_style));
                    mUserNameInput.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                    mUserNameErrorText.setVisibility(View.GONE);
                    return;
                }

                mUserNameInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_profile_skin));
                mUserNameInput.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                mUserNameErrorText.setVisibility(View.GONE);

                mRootRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        boolean control = false;

                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            if(ds.exists()){
                                if(ds.hasChild("user_name")){
                                    if(ds.child("user_name").getValue().toString().equals(mUserNameInput.getText().toString())){
                                        control = true;
                                    }
                                }
                            }
                        }

                        try {
                            Handler h = new Handler();
                            final boolean finalControl = control;
                            h.postDelayed(new Runnable() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void run() {
                                    if(TextUtils.isEmpty(mUserNameInput.getText())){
                                        mUserNameInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_default_style));
                                        mUserNameInput.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                                        mUserNameErrorText.setVisibility(View.GONE);
                                        dialog.dismiss();
                                    }else{
                                        if(finalControl){
                                            mUserNameInput.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                                            mUserNameInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_error_style));
                                            mUserNameErrorText.setVisibility(View.VISIBLE);
                                            mUserNameErrorText.setText("This username " + mUserNameInput.getText().toString() + " is not available.");
                                            dialog.dismiss();
                                        }else{

                                            if(!TextUtils.isEmpty(mEmail)){

                                                mAuth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if(task.isSuccessful()){
                                                            Details details = new Details("","","");
                                                            Users user = new Users(mEmail, mUserNameInput.getText().toString(), mFullName, "", mAuth.getUid(), false ,details);

                                                            mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull final Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        fcmToken();
                                                                        Intent homeIntent = new Intent(getActivity(), HomeActivity.class);
                                                                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(homeIntent);
                                                                        getActivity().finish();
                                                                    }else{
                                                                        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task2) {
                                                                                if(task2.isSuccessful()){
                                                                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                                }else{
                                                                                    Toast.makeText(getContext(), task2.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                    dialog.dismiss();
                                                                }
                                                            });
                                                        }else{
                                                            dialog.dismiss();
                                                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });

                                            }else{

                                                final String default_email = mUserNameInput.getText() + "@instagram.com";

                                                mAuth.createUserWithEmailAndPassword(default_email,mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if(task.isSuccessful()){
                                                            Details details = new Details("","","");
                                                            Users user = new Users(default_email, mUserNameInput.getText().toString(), mFullName, mPhoneNumber, mAuth.getUid(), false, details);

                                                            mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull final Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        fcmToken();
                                                                        Intent homeIntent = new Intent(getActivity(), HomeActivity.class);
                                                                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(homeIntent);
                                                                        getActivity().finish();
                                                                    }else{
                                                                        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task2) {
                                                                                if(task2.isSuccessful()){
                                                                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                                }else{
                                                                                    Toast.makeText(getContext(), task2.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                                                }
                                                                                dialog.dismiss();
                                                                            }
                                                                        });
                                                                    }
                                                                    dialog.dismiss();
                                                                }
                                                            });
                                                        }else{
                                                            dialog.dismiss();
                                                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            }, 2500);
                        }catch (Exception e){
                            Log.d("Error", e.getMessage());
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void fcmToken() {

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                onCreateNewToken(token);
            }
        });

    }

    private void onCreateNewToken(String value) {
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("fcm_token").setValue(value);
        }
    }

    private void closeKeyboard(Activity activity){
        View view = activity.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
