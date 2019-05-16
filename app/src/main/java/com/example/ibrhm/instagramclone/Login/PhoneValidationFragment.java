package com.example.ibrhm.instagramclone.Login;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.EventBusDataEvents;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhoneValidationFragment extends Fragment {

    private View mView;
    private TextView mConfirmTitle;
    private TextView mErrorText;

    private String sentPhoneNumber;

    private EditText mCodeInput;
    private Button mNextConfirmBtn;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private String verificationID;
    private String sentCode;

    private TextView mSMSConfirm;
    private ProgressBar mProgressBar;

    public PhoneValidationFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_phone_validation, container, false);

        mCodeInput = mView.findViewById(R.id.code_input);
        mSMSConfirm = mView.findViewById(R.id.sent_sms_confirm_success);
        mNextConfirmBtn = mView.findViewById(R.id.confirm_phone_next_btn);
        mProgressBar = mView.findViewById(R.id.code_progress_bar);
        mErrorText = mView.findViewById(R.id.code_error_text);

        setupPhoneConfirm();
        setupCodeWatcher();

        setupNextCodeVerify();

        setupSMSCodeSent();

        return mView;
    }

    @Subscribe(sticky = true)
    public void onPhoneNumberEvent(EventBusDataEvents.SendValue value){
        sentPhoneNumber = value.getPhoneNumber();
        sentCode = value.getSentCode();
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

    @SuppressLint("ClickableViewAccessibility")
    private void setupCodeWatcher(){
        mCodeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mCodeInput.getText().length() == 6){
                    mNextConfirmBtn.setEnabled(true);
                }else{
                    mNextConfirmBtn.setEnabled(false);
                }

                if(mCodeInput.getText().length() > 0){
                    mCodeInput.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_clear_icon,0);
                }else{
                    mCodeInput.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                }
            }
        });

        mCodeInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP && mCodeInput.length() > 0) {
                    if(event.getRawX() >= (mCodeInput.getRight() - mCodeInput.getCompoundDrawables()[2].getBounds().width()) + 25) {
                        mCodeInput.getText().clear();
                    }
                }
                return false;
            }
        });
    }

    private void setupPhoneConfirm(){
        mConfirmTitle = mView.findViewById(R.id.confirmation_title);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            mConfirmTitle.setText(Html.fromHtml("Enter the 6-digit code we sent to +90 "+sentPhoneNumber+". <b>Request a new one.</b>", Html.FROM_HTML_MODE_COMPACT));
        }else{
            mConfirmTitle.setText(Html.fromHtml("Enter the 6-digit code we sent to +90 "+sentPhoneNumber+". <b>Request a new one.</b>"));
        }

        SpannableString ss = new SpannableString(mConfirmTitle.getText());

        ForegroundColorSpan fcsGray = new ForegroundColorSpan(getResources().getColor(R.color.colorDarkGray));

        ss.setSpan(fcsGray, 40 + sentPhoneNumber.length(), mConfirmTitle.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mConfirmTitle.setText(ss);
    }

    private void setupNextCodeVerify(){
        mNextConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                closeKeyboard(getActivity());
                mNextConfirmBtn.setEnabled(false);
                mNextConfirmBtn.setText(null);
                mProgressBar.setIndeterminate(true);
                mProgressBar.setVisibility(View.VISIBLE);
                mCodeInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_default_style));
                mErrorText.setVisibility(View.GONE);
                mCodeInput.setEnabled(false);

                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mCodeInput.getText().toString().equals(sentCode)){
                            FragmentManager manager = getActivity().getSupportFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            transaction.replace(R.id.register_container, new UserInformationFragment());
                            transaction.addToBackStack("User_Information");
                            transaction.commit();
                        }else{
                            mCodeInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_error_style));
                            mErrorText.setVisibility(View.VISIBLE);
                        }
                        mNextConfirmBtn.setText("Next");
                        mProgressBar.setIndeterminate(false);
                        mProgressBar.setVisibility(View.GONE);
                        mCodeInput.setEnabled(true);
                    }
                },2000);
            }
        });
    }

    private void setupSMSCodeSent(){
        mConfirmTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSMSConfirm.getVisibility() == View.GONE){
                    setupCallBack();
                    sendCode();
                }
            }
        });
    }

    private void setupCallBack(){
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                if(!phoneAuthCredential.getSmsCode().isEmpty()){
                    sentCode = phoneAuthCredential.getSmsCode();
                    setupSMSConfirm();
                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationID = s;
            }
        };
    }

    private void sendCode(){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+90"+sentPhoneNumber,
                120,
                TimeUnit.SECONDS,
                Objects.requireNonNull(getActivity()),
                mCallBacks
        );
    }

    private void setupSMSConfirm(){
        AlphaAnimation fadeIn = new AlphaAnimation(0f,1f);
        fadeIn.setDuration(400);
        mSMSConfirm.setVisibility(View.VISIBLE);
        mSMSConfirm.setAnimation(fadeIn);

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation fadeOut = new AlphaAnimation(1f,0f);
                fadeOut.setDuration(400);
                mSMSConfirm.setVisibility(View.GONE);
                mSMSConfirm.setAnimation(fadeOut);
            }
        }, 3000);
    }

    private void closeKeyboard(Activity activity){
        View view = activity.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}