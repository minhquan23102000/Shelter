package com.example.shelter;

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

import com.example.shelter.Data.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyFragment extends Fragment {
    static final public String TAG = VerifyFragment.class.getSimpleName();
    private String userPhone;
    private String verificationCodeGenerated;
    private MaterialButton verifyButton;
    private TextInputEditText verifyEditText;
    private TextInputLayout verifyInputLayout;
    private TextView timer;
    private Bundle bundle;
    private SessionManager sessionManager;
    private boolean isVerify = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.verify_phone_fragment, container, false);
        bundle = this.getArguments();
        userPhone = bundle.getString("userPhone");
        verifyButton = view.findViewById(R.id.verify_button);
        verifyEditText = view.findViewById(R.id.verify_edit_text);
        verifyInputLayout = view.findViewById(R.id.verify_text_input);
        sessionManager = new SessionManager(getContext());
        timer = view.findViewById(R.id.timer);

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String code = verifyEditText.getText().toString();

                if (code.isEmpty() || code.length() < 6) {
                    verifyInputLayout.setError("Wrong OTP...");
                    verifyInputLayout.requestFocus();
                    return;
                }
                verifyCode(code);
            }
        });

        sendVerificationCodeToUser(userPhone);
        return view;
    }

    private void sendVerificationCodeToUser(String userPhone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder()
                        .setPhoneNumber("+84" + userPhone)       // Phone number to verify
                        .setTimeout(120L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(getActivity())                 // Activity (for callback binding)
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
                getActivity().getSupportFragmentManager().popBackStack();
                Toast.makeText(getContext(), R.string.verify_time_out, Toast.LENGTH_LONG).show();
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
            Toast.makeText(getActivity(), getString(R.string.phone_is_not_valid), Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
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
        MainActivity.mAuth.signInWithCredential(credential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    isVerify = true;
                    //if this is my account fragment, then we navigate back to my account fragment
                    if (bundle.getString("fragment", null).equals(MyAccountFragment.LOG_TAG)) {
                        sessionManager.setVerifyPhone(isVerify);
                        getFragmentManager().popBackStack();
                    } else {
                        // if this is sign up fragment the nwe navigate to house grid fragment
                        Toast.makeText(getContext().getApplicationContext(), getString(R.string.sign_up_successfully), Toast.LENGTH_SHORT).show();
                        // Sign up successfully, pop all the fragment in backStack then navigate to HouseGridFragment
                        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        ((NavigationHost) getActivity()).navigateTo(new HouseGridFragment(), false);
                    }

                } else {
                    Toast.makeText(getContext(), R.string.verify_code_is_not_valid, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onDestroyView() {
        if (!isVerify && !bundle.getString("fragment", null).equals(MyAccountFragment.LOG_TAG)) {
            Uri userUri = sessionManager.getUserUri();
            if (userUri != null) {
                //delete data and navigate back to sign up fragment
                getActivity().getContentResolver().delete(userUri, null, null);
            }
            sessionManager.clearUserSession();
        }
        super.onDestroyView();
    }
}
