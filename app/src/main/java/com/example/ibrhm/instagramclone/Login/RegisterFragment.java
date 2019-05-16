package com.example.ibrhm.instagramclone.Login;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.EventBusDataEvents;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment {

    private View mView;
    private LinearLayout mPhoneLayout, mEmailLayout;
    private View mPhoneView, mEmailView;
    private LinearLayout mPhoneContainer, mEmailContainer;
    private EditText mPhoneInput, mEmailInput;
    private Button mNextPhoneBtn, mNextEmailBtn;
    private ProgressBar mPhoneProgressBar, mEmailProgressBar;
    private LinearLayout mPhoneInputLayout;
    private TextView mPhoneErrorText, mEmailErrorText;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private String verificationID;
    private String sentCode;

    private DatabaseReference mRootRef;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_register, container, false);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mPhoneInput = mView.findViewById(R.id.phone_input);
        mNextPhoneBtn = mView.findViewById(R.id.reg_phone_next_btn);
        mEmailInput = mView.findViewById(R.id.email_input);
        mNextEmailBtn = mView.findViewById(R.id.reg_email_next_btn);
        mPhoneProgressBar = mView.findViewById(R.id.phone_progress_bar);
        mPhoneInputLayout = mView.findViewById(R.id.phone_input_layout);
        mPhoneErrorText = mView.findViewById(R.id.phone_error_text);
        mEmailProgressBar = mView.findViewById(R.id.email_progress_bar);
        mEmailErrorText = mView.findViewById(R.id.email_error_text);

        setupTabNavigation();
        setupPhoneWatcher();
        setupEmailWatcher();
        setupPhoneRegister();
        setupEmailRegister();

        return mView;
    }

    private void setupTabNavigation(){
        mPhoneLayout = mView.findViewById(R.id.phone_layout);
        mEmailLayout = mView.findViewById(R.id.email_layout);

        mPhoneView = mView.findViewById(R.id.phone_tab_view);
        mEmailView = mView.findViewById(R.id.email_tab_view);

        mPhoneContainer = mView.findViewById(R.id.phone_container);
        mEmailContainer = mView.findViewById(R.id.email_container);

        mPhoneLayout.setSelected(true);

        mPhoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mPhoneLayout.isSelected()){
                    mPhoneLayout.setSelected(true);
                    mEmailLayout.setSelected(false);
                    mPhoneView.setVisibility(View.VISIBLE);
                    mEmailView.setVisibility(View.GONE);
                    mPhoneContainer.setVisibility(View.VISIBLE);
                    mEmailContainer.setVisibility(View.GONE);
                }
            }
        });

        mEmailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mEmailLayout.isSelected()){
                    mPhoneLayout.setSelected(false);
                    mEmailLayout.setSelected(true);
                    mPhoneView.setVisibility(View.GONE);
                    mEmailView.setVisibility(View.VISIBLE);
                    mPhoneContainer.setVisibility(View.GONE);
                    mEmailContainer.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPhoneWatcher(){
        mPhoneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mPhoneInput.getText().length() > 0){
                    mNextPhoneBtn.setEnabled(true);
                    mPhoneInput.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_clear_icon,0);
                }else{
                    mNextPhoneBtn.setEnabled(false);
                    mPhoneInput.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                }
            }
        });

        mPhoneInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP && mPhoneInput.length() > 0) {
                    if(event.getRawX() >= (mPhoneInput.getRight() - mPhoneInput.getCompoundDrawables()[2].getBounds().width()) + 25) {
                        mPhoneInput.getText().clear();
                    }
                }
                return false;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupEmailWatcher(){
        mEmailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mEmailInput.getText().length() > 0){
                    mNextEmailBtn.setEnabled(true);
                    mEmailInput.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_clear_icon,0);
                }else{
                    mNextEmailBtn.setEnabled(false);
                    mEmailInput.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                }
            }
        });

        mEmailInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP && mEmailInput.length() > 0) {
                    if(event.getRawX() >= (mEmailInput.getRight() - mEmailInput.getCompoundDrawables()[2].getBounds().width()) + 25) {
                        mEmailInput.getText().clear();
                    }
                }
                return false;
            }
        });
    }

    private void setupCallBack(){
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                sentCode = phoneAuthCredential.getSmsCode();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.register_container, new PhoneValidationFragment());
                transaction.addToBackStack("Phone_Validation");
                transaction.commit();
                EventBusDataEvents.SendValue phoneEvents = new EventBusDataEvents.SendValue(mPhoneInput.getText().toString(), null, verificationID, sentCode, null, randomUserName(), null);
                EventBus.getDefault().postSticky(phoneEvents);

                mNextPhoneBtn.setEnabled(true);
                mNextPhoneBtn.setText("Next");
                mPhoneProgressBar.setIndeterminate(false);
                mPhoneProgressBar.setVisibility(View.GONE);
                mPhoneInput.setEnabled(true);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mNextPhoneBtn.setEnabled(true);
                mNextPhoneBtn.setText("Next");
                mPhoneProgressBar.setIndeterminate(false);
                mPhoneProgressBar.setVisibility(View.GONE);
                mPhoneInputLayout.setBackground(getActivity().getDrawable(R.drawable.custom_edit_error_style));
                mPhoneErrorText.setText("Looks like your phone number may be incorrect. Please try entering your full number, including the country code.");
                mPhoneErrorText.setVisibility(View.VISIBLE);
                mPhoneInput.setEnabled(true);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationID = s;
            }
        };
    }

    private void setupPhoneRegister(){
        mPhoneInputLayout.setBackground(getActivity().getDrawable(R.drawable.custom_edit_default_style));
        mPhoneErrorText.setVisibility(View.GONE);

        mNextPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard(getActivity());
                mNextPhoneBtn.setEnabled(false);
                mNextPhoneBtn.setText(null);
                mPhoneProgressBar.setIndeterminate(true);
                mPhoneProgressBar.setVisibility(View.VISIBLE);
                mPhoneInput.setEnabled(false);

                isAvailablePhoneNumber(mPhoneInput.getText().toString());
            }
        });
    }

    private void sendCode(){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+90"+mPhoneInput.getText(),
                120,
                TimeUnit.SECONDS,
                Objects.requireNonNull(getActivity()),
                mCallBacks
        );
    }

    private void setupEmailRegister(){
        mNextEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyboard(getActivity());
                mNextEmailBtn.setEnabled(false);
                mNextEmailBtn.setText(null);
                mEmailProgressBar.setIndeterminate(true);
                mEmailProgressBar.setVisibility(View.VISIBLE);
                mEmailInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_default_style));
                mEmailErrorText.setVisibility(View.GONE);
                mEmailInput.setEnabled(false);

                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(isValidEmail(mEmailInput.getText().toString())){
                            isAvailableEmail(mEmailInput.getText().toString());
                        }else{
                            mEmailInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_error_style));
                            mEmailErrorText.setText("Please enter a valid email.");
                            mEmailErrorText.setVisibility(View.VISIBLE);
                            mEmailInput.setEnabled(true);
                        }
                        mNextEmailBtn.setEnabled(true);
                        mNextEmailBtn.setText("Next");
                        mEmailProgressBar.setIndeterminate(false);
                        mEmailProgressBar.setVisibility(View.GONE);
                    }
                }, 3000);
            }
        });
    }

    private boolean isValidEmail(String email){
        if(email == null)
            return false;

        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void isAvailablePhoneNumber(final String phoneNumber){
        mRootRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                boolean control = false;

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.exists()){
                        if(ds.hasChild("phone_number")){
                            if(ds.child("phone_number").getValue().toString().equals(phoneNumber)){
                                control = true;
                            }
                        }
                    }
                }

                if(control){
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPhoneInputLayout.setBackground(getActivity().getDrawable(R.drawable.custom_edit_error_style));
                            mPhoneErrorText.setText("This phone number is taken by another account.");
                            mPhoneErrorText.setVisibility(View.VISIBLE);

                            mNextPhoneBtn.setEnabled(true);
                            mNextPhoneBtn.setText("Next");
                            mPhoneProgressBar.setIndeterminate(false);
                            mPhoneProgressBar.setVisibility(View.GONE);
                            mPhoneInput.setEnabled(true);
                        }
                    }, 2000);
                }else{
                    setupCallBack();
                    sendCode();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isAvailableEmail(final String email){
        mRootRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                boolean control = false;

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.exists()){
                        if(ds.hasChild("email")){
                            if(ds.child("email").getValue().toString().equals(email)) {
                                control = true;
                            }
                        }
                    }
                }

                if(control){
                    mEmailInput.setBackground(getResources().getDrawable(R.drawable.custom_edit_error_style));
                    mEmailErrorText.setText("This email is taken by another account.");
                    mEmailErrorText.setVisibility(View.VISIBLE);
                }else{
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.register_container, new UserInformationFragment());
                    transaction.addToBackStack("Email_Validation");
                    transaction.commit();
                    EventBusDataEvents.SendValue emailEvents = new EventBusDataEvents.SendValue(null, mEmailInput.getText().toString(), verificationID, null, null, randomUserName(), null);
                    EventBus.getDefault().postSticky(emailEvents);
                }

                mEmailInput.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String randomUserName() {
        String ALLOWED_CHARACTERS ="qwertyuiopasdfghjklzxcvbnm";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(27);
        for(int i = 0; i < 12; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString() + Calendar.getInstance().get(Calendar.YEAR);
    }

    private void closeKeyboard(Activity activity){
        View view = activity.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
