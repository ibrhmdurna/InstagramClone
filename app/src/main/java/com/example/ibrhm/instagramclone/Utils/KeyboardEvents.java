package com.example.ibrhm.instagramclone.Utils;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardEvents {
    public static void closeKeyboard(AppCompatActivity activity){
        View view = activity.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
