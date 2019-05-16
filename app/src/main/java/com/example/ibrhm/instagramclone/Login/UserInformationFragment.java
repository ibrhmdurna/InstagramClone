package com.example.ibrhm.instagramclone.Login;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.EventBusDataEvents;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserInformationFragment extends Fragment {

    private View mView;
    private EditText mFullNameInput,mPasswordInput;
    private Button mNextBtn;

    private LinearLayout mLoginLayout;

    private TextView mPasswordError;

    private String mPhoneNumber, mEmail, mVerificationID, mUserName;

    public UserInformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_user_information, container, false);

        mNextBtn = mView.findViewById(R.id.info_next_btn);
        mFullNameInput = mView.findViewById(R.id.user_full_name_input);
        mPasswordInput = mView.findViewById(R.id.user_password_input);
        mLoginLayout = getActivity().findViewById(R.id.reg_log_in_layout);

        mPasswordError = mView.findViewById(R.id.password_error_text);
        setupPasswordWatcher();
        setupFinishReg();

        return mView;
    }

    @Subscribe(sticky = true)
    public void onUserValueEvent(EventBusDataEvents.SendValue value){
        mPhoneNumber = value.getPhoneNumber();
        mEmail = value.getEmail();
        mVerificationID = value.getVerificationId();
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

    private void setupPasswordWatcher(){
        mPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mPasswordInput.getText().length() >= 6){
                    mNextBtn.setEnabled(true);
                }else{
                    mNextBtn.setEnabled(false);
                }
            }
        });
    }

    private void setupFinishReg(){
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard(getActivity());
                if(passwordControl()){
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.register_container, new FinishRegisterFragment());
                    transaction.addToBackStack("Finish_Register");
                    transaction.commit();
                    EventBusDataEvents.SendValue events = new EventBusDataEvents.SendValue(mPhoneNumber, mEmail, mVerificationID, null, mFullNameInput.getText().toString(), mUserName, mPasswordInput.getText().toString());
                    EventBus.getDefault().postSticky(events);
                }else{
                    mPasswordInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_error_style));
                    mPasswordError.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private boolean passwordControl(){
        try {
            Integer.valueOf(mPasswordInput.getText().toString());
            return false;
        }catch (Exception e){
            return true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mLoginLayout.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLoginLayout.setVisibility(View.VISIBLE);
    }

    private void closeKeyboard(Activity activity){
        View view = activity.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

