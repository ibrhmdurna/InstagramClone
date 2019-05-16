package com.example.ibrhm.instagramclone.Login;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.DialogHelper;

public class RegisterActivity extends AppCompatActivity {

    public static boolean finishFragment = false;
    private LinearLayout mLoginLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mLoginLayout = findViewById(R.id.reg_log_in_layout);

        setupRegisterView();
        setupLoginView();
    }

    private void setupRegisterView(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.register_container, new RegisterFragment());
        transaction.commit();
    }

    private void setupLoginView(){
        mLoginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(finishFragment){
            DialogHelper.dialogGoBack(this, null, null);
        }else{
            super.onBackPressed();
        }
    }
}
