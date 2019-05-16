package com.example.ibrhm.instagramclone.Utils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.ibrhm.instagramclone.R;

public class DialogHelper {

    public static AlertDialog dialogLoading(Activity activity, String title){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_loading, null);
        mBuilder.setView(view);
        AlertDialog dialog = mBuilder.create();

        if(title != null)
        {
            TextView mTitle = view.findViewById(R.id.loading_title);
            mTitle.setText(title);
        }

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        return dialog;
    }

    public static void dialogGoBack(final AppCompatActivity activity, String title, String body){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(activity);
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_go_back, null);
        mBuilder.setView(view);
        final AlertDialog dialog = mBuilder.create();

        if(title != null){
            TextView mTitle = view.findViewById(R.id.dialog_go_back_title);
            mTitle.setText(title);
        }
        if(body != null){
            TextView mBody = view.findViewById(R.id.dialog_go_back_body);
            mBody.setText(body);
        }

        Button mCancelBtn = view.findViewById(R.id.dialog_go_back_cancel_btn);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button mGoBackBtn = view.findViewById(R.id.dialog_go_back_btn);
        mGoBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                activity.getSupportFragmentManager().popBackStack();
            }
        });

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
}
