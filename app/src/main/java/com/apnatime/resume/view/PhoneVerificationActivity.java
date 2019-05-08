package com.apnatime.resume.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apnatime.resume.R;
import com.apnatime.resume.utils.Constants;
import com.apnatime.resume.utils.Utility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

/**
 * Created by senthilc on 23/02/2016.
 */
public class PhoneVerificationActivity extends AppCompatActivity {

    EditText mobileNoEditTxt;
    Toolbar mToolbar;
    EditText verificationCodeEditTxt;
    Button submitBtn;
    String mMobileNo = "", mOTPCode = "";
    ProgressBar progressBar;
    TextView resendOTP;
    RelativeLayout topLayout;
    TextView label;
    private static final String TAG = "MobileNoVerifyActivity";
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utility.setStatusBarColor(this);
        setContentView(R.layout.mobile_verificaltion_view);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        initViews();

        mMobileNo=getIntent().getStringExtra(Constants.MOBILE_NUMBER);
        mMobileNo="+91"+mMobileNo;

        mAuth = FirebaseAuth.getInstance();

        System.out.println("mMobileNo ::: " + mMobileNo + "\t\t" + mOTPCode);
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                verificationCodeEditTxt.setText(credential.getSmsCode());
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }
                progressBar.setVisibility(View.GONE);
                //label.setVisibility(View.VISIBLE);
                submitBtn.setEnabled(true);
                resendOTP.setEnabled(true);
                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);
                Toast.makeText(PhoneVerificationActivity.this, "Verification code sent to your mobile number", Toast.LENGTH_LONG).show();
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                progressBar.setVisibility(View.GONE);
                label.setVisibility(View.VISIBLE);
                submitBtn.setEnabled(true);
                resendOTP.setEnabled(true);
                // ...
            }
        };
        progressBar.setVisibility(View.VISIBLE);
        submitBtn.setEnabled(false);
        resendOTP.setEnabled(false);
        label.setVisibility(View.GONE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mMobileNo,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                PhoneVerificationActivity.this,               // Activity (for callback binding)
                mCallbacks);

    }


    private void initViews() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Resume Creator");
        mobileNoEditTxt = (EditText) findViewById(R.id.mobile_no);
        verificationCodeEditTxt = (EditText) findViewById(R.id.verification_code);

        submitBtn = (Button) findViewById(R.id.submit);
        resendOTP = (TextView) findViewById(R.id.resend_otp);
        resendOTP.setPaintFlags(resendOTP.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        submitBtn.setEnabled(true);
        label = (TextView) findViewById(R.id.txt3);
        resendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!Utility.checkInternet(PhoneVerificationActivity.this)) {
                    Utility.showToastMessage(PhoneVerificationActivity.this, getResources().getString(R.string.no_internet));
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    resendOTP.setEnabled(false);
                    submitBtn.setEnabled(false);
                    //mMobileNo = countryCode + mMobileNo;
                    // new SendOTPAsynTask().execute();

                    resendVerificationCode(mMobileNo, mResendToken);
                }
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = verificationCodeEditTxt.getText().toString();
                if (code.isEmpty()) {
                    Utility.showToastMessage(PhoneVerificationActivity.this, "Please enter verification code");
                    //Toast.makeText(WelcomeActivity1.this, getResources().getString(R.string.mobile_no_empty), Toast.LENGTH_LONG).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    submitBtn.setEnabled(false);
                    resendOTP.setEnabled(false);
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });
        mobileNoEditTxt.setEnabled(false);

        topLayout = (RelativeLayout) findViewById(R.id.top_layout);
        // topLayout.setBackgroundColor(Color.parseColor(PreferenceSettings.getPrimaryColor()));

    }


    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            callGetUserInfoActivity(user.getUid());
                        } else {
                            submitBtn.setEnabled(true);
                            resendOTP.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(PhoneVerificationActivity.this, "Invalid verification code", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void callGetUserInfoActivity(String id) {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        submitBtn.setEnabled(true);
        resendOTP.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        Intent intent =new Intent(PhoneVerificationActivity.this,GetUserInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.MOBILE_NUMBER,mMobileNo);
        intent.putExtra(Constants.USER_ID,id);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);
    }


    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}

