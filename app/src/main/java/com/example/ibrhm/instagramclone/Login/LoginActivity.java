package com.example.ibrhm.instagramclone.Login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.Home.HomeActivity;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
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

public class LoginActivity extends AppCompatActivity {

    private TextView mForgotText;
    private LinearLayout mSignUpLayout;
    private EditText mInputInfo, mInputPassword;
    private Button mLoginBtn;
    private ProgressBar mLoginProgressBar;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mForgotText = findViewById(R.id.log_forgot_text);
        mSignUpLayout = findViewById(R.id.sign_up_layout);
        mInputInfo = findViewById(R.id.log_input_info);
        mInputPassword = findViewById(R.id.log_input_password);
        mLoginBtn = findViewById(R.id.login_btn);
        mLoginProgressBar = findViewById(R.id.login_progress_bar);

        setupForgotText();
        setupSignUpView();

        mInputInfo.addTextChangedListener(setupLoginWatcher);
        mInputPassword.addTextChangedListener(setupLoginWatcher);

        setupLogin();
    }

    private TextWatcher setupLoginWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(mInputInfo.getText().length() > 0 && mInputPassword.getText().length() > 0){
                mLoginBtn.setEnabled(true);
            }else{
                mLoginBtn.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void setupForgotText(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mForgotText.setText(Html.fromHtml("Forgot your login details? <b>Get help signing in.</b>", Html.FROM_HTML_MODE_COMPACT));
        }else{
            mForgotText.setText(Html.fromHtml("Forgot your login details? <b>Get help signing in.</b>"));
        }
    }

    private void setupSignUpView(){
        mSignUpLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(regIntent);
            }
        });
    }

    private void setupLogin(){
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard(LoginActivity.this);
                mInputInfo.setEnabled(false);
                mInputPassword.setEnabled(false);
                mLoginBtn.setEnabled(false);
                mLoginBtn.setText(null);
                mLoginProgressBar.setIndeterminate(true);
                mLoginProgressBar.setVisibility(View.VISIBLE);

                mRootRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean isNotValid = true;

                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            Users user = ds.getValue(Users.class);
                            if(user.getPhone_number().equals(mInputInfo.getText().toString())){
                                login(user.getEmail(), mInputPassword.getText().toString());
                                isNotValid = false;
                                break;
                            }
                            else if(user.getEmail().equals(mInputInfo.getText().toString())){
                                login(user.getEmail(), mInputPassword.getText().toString());
                                isNotValid = false;
                                break;
                            }
                            else if(user.getUser_name().equals(mInputInfo.getText().toString())){
                                login(user.getEmail(), mInputPassword.getText().toString());
                                isNotValid = false;
                                break;
                            }
                        }

                        if(isNotValid){
                            login(mInputInfo.getText().toString(), mInputPassword.getText().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void login(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(homeIntent);
                    finish();
                    fcmToken();
                }else{
                    Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
                }
                mInputInfo.setEnabled(true);
                mInputPassword.setEnabled(true);
                mLoginBtn.setText("Log In");
                mLoginProgressBar.setIndeterminate(false);
                mLoginProgressBar.setVisibility(View.GONE);
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
