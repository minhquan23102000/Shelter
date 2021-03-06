package com.example.shelter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.shelter.data.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyFragment extends Fragment {
    static final public String TAG = VerifyFragment.class.getSimpleName();
    static final public String KEY_USER_PHONE = "userPhone";
    static final public String KEY_PRE_FRAGMENT = "fragment";
    
    private Context mContext;
    private Activity mActivity;
    
    private String userPhone;
    private String preFragmentTag = "";
    private String verificationCodeGenerated;
    private TextInputEditText verifyEditText;
    private TextInputLayout verifyInputLayout;
    private TextView timer;
    private SessionManager sessionManager;
    private boolean isVerify = false;

    static public Fragment NewInstance(String userPhone, String preFragmentTag) {
        Fragment newFragment = new VerifyFragment();
        Bundle deliver = new Bundle();
        deliver.putString(KEY_USER_PHONE, userPhone);
        deliver.putString(KEY_PRE_FRAGMENT, preFragmentTag);
        newFragment.setArguments(deliver);
        return newFragment;
    }


    @Override
    public void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mActivity = getActivity();
        if (getArguments() != null) {
            userPhone = getArguments().getString(KEY_USER_PHONE, null);
            preFragmentTag = getArguments().getString(KEY_PRE_FRAGMENT, null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.verify_phone_fragment, container, false);
        MaterialButton verifyButton = view.findViewById(R.id.verify_button);
        verifyEditText = view.findViewById(R.id.verify_edit_text);
        verifyInputLayout = view.findViewById(R.id.verify_text_input);
        sessionManager = new SessionManager(mContext);
        timer = view.findViewById(R.id.timer);
        Log.d(TAG, "onCreateView: userPhone " + userPhone);
        verifyButton.setOnClickListener(view1 -> {

            String code = verifyEditText.getText().toString();

            if (code.isEmpty() || code.length() < 6) {
                verifyInputLayout.setError("Wrong OTP...");
                verifyInputLayout.requestFocus();
                return;
            }
            verifyCode(code);
        });

        sendVerificationCodeToUser(userPhone);
        return view;
    }

    private void sendVerificationCodeToUser(String userPhone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder()
                        .setPhoneNumber("+84" + userPhone)       // Phone number to verify
                        .setTimeout(120L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(mActivity)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timer.setText((millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                if (!isVerify) {
                    getParentFragmentManager().popBackStack();
                    Toast.makeText(mContext, R.string.verify_time_out, Toast.LENGTH_LONG).show();
                }

            }
        }.start();
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:" + credential);

            String code = credential.getSmsCode();
            verifyCode(code);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e);
            Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            super.onCodeSent(verificationId, token);
            Log.d(TAG, "onCodeSent:" + verificationId);

            verificationCodeGenerated = verificationId;
        }
    };


    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeGenerated, code);
        MainActivity.mAuth.signInWithCredential(credential).addOnCompleteListener(mActivity, task -> {
            if (task.isSuccessful()) {
                isVerify = true;
                //if this is my account fragment, then we navigate back to my account fragment
                if (preFragmentTag.equals(MyAccountFragment.LOG_TAG) || preFragmentTag.equals(ResetPasswordFragment.TAG)) {
                    sessionManager.setVerifyPhone(isVerify);
                    getParentFragmentManager().popBackStack();
                } else {
                    // if this is sign up fragment the nwe navigate to house grid fragment
                    Toast.makeText(mContext.getApplicationContext(), getString(R.string.sign_up_successfully), Toast.LENGTH_SHORT).show();
                    // Sign up successfully, pop all the fragment in backStack then navigate to HouseGridFragment
                    getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    ((NavigationHost) mActivity).navigateTo(new HouseGridFragment(), false);
                }

            } else {
                Toast.makeText(mContext, R.string.verify_code_is_not_valid, Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onDestroyView() {
        if (!isVerify && !preFragmentTag.equals(MyAccountFragment.LOG_TAG)) {
            Uri userUri = sessionManager.getUserUri();
            if (userUri != null) {
                //delete data and navigate back to sign up fragment
                mActivity.getContentResolver().delete(userUri, null, null);
            }
            sessionManager.clearUserSession();
        }
        super.onDestroyView();
    }
}
