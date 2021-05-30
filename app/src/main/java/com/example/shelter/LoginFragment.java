package com.example.shelter;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.shelter.Data.SessionManager;
import com.example.shelter.Data.ShelterDBContract.UserEntry;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class LoginFragment extends Fragment {
    static final public String TAG = LoginFragment.class.getSimpleName();

    private Context mContext;
    private Activity mActivity;

    //Init intent launcher
    private ActivityResultLauncher<Intent> someActivityResultLauncher;

    //Third party sign in
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    //Data
    private Uri userUri;
    private SessionManager sessionManager;
    private Cursor currentUser;

    //Views
    private TextInputEditText phoneEditText;
    private TextInputLayout phoneTextInput;
    private TextInputLayout passwordTextInput;
    private TextInputEditText passwordEditText;
    private TextView resetPasswordTV;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mContext = getContext();
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // There are no request codes
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(TAG, "Google sign in failed", e);
                        Toast.makeText(mContext, R.string.google_sign_in_failed, Toast.LENGTH_SHORT).show();
                    }

                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);

        passwordTextInput = view.findViewById(R.id.password_text_input);
        passwordEditText = view.findViewById(R.id.password_edit_text);
        phoneTextInput = view.findViewById(R.id.phone_text_input);
        phoneEditText = view.findViewById(R.id.phone_edit_text);
        MaterialButton nextButton = view.findViewById(R.id.next_button);
        resetPasswordTV = view.findViewById(R.id.reset_password);

        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(mContext);

        nextButton.setOnClickListener(v -> checkLogin());

        MaterialButton signUpButton = view.findViewById(R.id.sign_up_button);

        signUpButton.setOnClickListener(v -> ((NavigationHost) mActivity).navigateTo(new SignUpFragment(), true));

        resetPasswordTV.setOnClickListener(v -> ((NavigationHost) mActivity).navigateTo(new ResetPasswordFragment(), true));

        phoneEditText.setOnKeyListener((view1, i, keyEvent) -> {
            phoneTextInput.setError(null);
            return false;
        });
        passwordEditText.setOnKeyListener((v, keyCode, event) -> {
            passwordTextInput.setError(null);
            return false;
        });

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestProfile()
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);

        view.findViewById(R.id.google_login_button).setOnClickListener(v -> googleSignIn());

        return view;
    }

    private void checkLogin() {
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String projection[] = {
                UserEntry._ID,
                UserEntry.COLUMN_USER_EMAIL,
                UserEntry.COLUMN_USER_PHONE,
                UserEntry.COLUMN_USER_ROLE_ID,
                UserEntry.COLUMN_USER_NAME,
                UserEntry.COLUMN_USER_PASSWORD
        };
        String selection = UserEntry.COLUMN_USER_PHONE + "=?";
        String selectionArgs[] = {phone};

        currentUser = mContext.getContentResolver().query(UserEntry.CONTENT_URI, projection, selection, selectionArgs, null);
        if (currentUser.moveToFirst()) {
            String passwordData = currentUser.getString(currentUser.getColumnIndex(UserEntry.COLUMN_USER_PASSWORD));

            if (!passwordData.equals(UserEntry.md5Crypt(password))) {
                passwordTextInput.setError(getString(R.string.incorrect_password));
            } else {
                Uri currentUserUri = ContentUris.withAppendedId(UserEntry.CONTENT_URI, currentUser.getLong(currentUser.getColumnIndex(UserEntry._ID)));
                sessionManager.initUserSession(currentUser.getString(currentUser.getColumnIndex(UserEntry.COLUMN_USER_PHONE)),
                        currentUser.getString(currentUser.getColumnIndex(UserEntry.COLUMN_USER_EMAIL)),
                        currentUserUri.toString(),
                        currentUser.getString(currentUser.getColumnIndex(UserEntry.COLUMN_USER_NAME)),
                        currentUser.getInt(currentUser.getColumnIndex(UserEntry.COLUMN_USER_ROLE_ID)));
                ((NavigationHost) mActivity).navigateTo(new HouseGridFragment(), false); // Navigate to the next Fragment

            }
        } else {
            phoneTextInput.setError(getString(R.string.phone_does_not_exists));
        }

    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        someActivityResultLauncher.launch(signInIntent);
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            saveAccountWhenSignInWithGoogle();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:Failed");
                        }
                    }
                });
    }

    protected void saveAccountWhenSignInWithGoogle() {
        //Get values to insert

        String name = mAuth.getCurrentUser().getDisplayName();
        String phone = mAuth.getCurrentUser().getPhoneNumber();
        String email = mAuth.getCurrentUser().getEmail();

        Uri[] getUserUriByEmail = new Uri[1];
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USER_NAME, name);
        values.put(UserEntry.COLUMN_USER_PHONE, phone);
        values.put(UserEntry.COLUMN_USER_EMAIL, email);
        values.put(UserEntry.COLUMN_USER_ROLE_ID, UserEntry.VIEWER);

        // If user does not exists, we insert else do nothing
        if (!UserEntry.checkIfIsExists(email, UserEntry.COLUMN_USER_EMAIL, mContext.getApplicationContext(), getUserUriByEmail)
                && !UserEntry.checkIfIsExists(phone, UserEntry.COLUMN_USER_PHONE, mContext.getApplicationContext())) {

            //Time to insert
            // This is a NEW user, so insert a new user into the provider,
            // returning the content URI for the new user.
            userUri = mContext.getApplicationContext().getContentResolver().insert(UserEntry.CONTENT_URI, values);
        } else {
            //If email or phone exists we update it
            userUri = getUserUriByEmail[0];
            mContext.getContentResolver().update(userUri, values, null, null);
            Log.d(TAG, "saveAccount: " + userUri);
        }

        // Show a toast message depending on whether or not the insertion was successful.
        if (userUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(mContext.getApplicationContext(), getString(R.string.error_saving_user_account), Toast.LENGTH_SHORT).show();

        } else {
            sessionManager.initUserSession(phone, email, userUri.toString(), name, 2);
            ((NavigationHost) mActivity).navigateTo(new HouseGridFragment(), false); // Navigate to the next Fragment
        }
    }


}
