package com.example.ibrhm.instagramclone.Login;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * A simple {@link Fragment} subclass.
 */
public class FinishRegisterFragment extends Fragment {

    private View mView;

    private LinearLayout mLoginLayout;
    private TextView mPolicyText;
    private TextView mUserNameC;
    private TextView mChangeUsername;
    private Button mBeginBtn;
    private ProgressBar mProgressBar;

    private String mPhoneNumber, mEmail, mVerificationID, mFullName, mUserName, mPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;

    public FinishRegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_finish_register, container, false);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mLoginLayout = getActivity().findViewById(R.id.reg_log_in_layout);
        mPolicyText = mView.findViewById(R.id.policy_text);
        mUserNameC = mView.findViewById(R.id.username_text);
        mChangeUsername = mView.findViewById(R.id.change_username);
        mBeginBtn = mView.findViewById(R.id.reg_finish_btn);
        mProgressBar = mView.findViewById(R.id.finish_progress_bar);

        mUserNameC.setText(mUserName);

        setupPolicy();
        changeUsername();

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

    @Override
    public void onStart() {
        super.onStart();
        mLoginLayout.setVisibility(View.GONE);
        RegisterActivity.finishFragment = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        RegisterActivity.finishFragment = false;
    }

    private void setupPolicy(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mPolicyText.setText(Html.fromHtml("By clicking Next, you agree to out <b>Terms, Data Policy</b> and <b>Cookies Policy.</b>", Html.FROM_HTML_MODE_COMPACT));
        }else{
            mPolicyText.setText(Html.fromHtml("By clicking Next, you agree to out <b>Terms, Data Policy</b> and <b>Cookies Policy.</b>"));
        }
    }

    private void changeUsername(){
        mChangeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.register_container, new ChangeUsernameFragment());
                transaction.addToBackStack("Change_Username");
                transaction.commit();
                EventBusDataEvents.SendValue events = new EventBusDataEvents.SendValue(mPhoneNumber, mEmail, mVerificationID, null, mFullName, mUserName, mPassword);
                EventBus.getDefault().postSticky(events);
            }
        });
    }

    private void setupRegisterFinish(){
        mBeginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog dialog = DialogHelper.dialogLoading(getActivity(), "Registering...");
                dialog.show();

                if(!TextUtils.isEmpty(mEmail)){

                    mAuth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Details details = new Details("","","");
                                Users user = new Users(mEmail, mUserName, mFullName, "", mAuth.getUid(), false, details);

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

                    final String default_email = mUserName + "@instagram.com";

                    mAuth.createUserWithEmailAndPassword(default_email,mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Details details = new Details("","","");
                                Users user = new Users(default_email, mUserName, mFullName, mPhoneNumber, mAuth.getUid(), false, details);

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
                }
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
}
