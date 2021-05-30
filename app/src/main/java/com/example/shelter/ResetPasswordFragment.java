package com.example.shelter;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shelter.data.SessionManager;
import com.example.shelter.data.ShelterDBContract.UserEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class ResetPasswordFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    static public final String TAG = ResetPasswordFragment.class.getName();
    static private final int GET_USER_LOADER = 1765;

    private Context mContext;
    private Activity mActivity;

    //Data
    private SessionManager sessionManager;
    private Cursor getUser;
    private String userPhone;


    //Views
    private TextInputLayout phoneInputLayout;
    private TextInputEditText phoneEditText;

    private View renewContainer;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordEditText;
    private TextInputLayout confirmPasswordInputLayout;
    private TextInputEditText confirmPasswordEditText;

    private MaterialButton nextButton;
    private MaterialButton updateButton;


    private boolean toVerify = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mActivity = getActivity();
        sessionManager = new SessionManager(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Find all views
        passwordInputLayout = view.findViewById(R.id.password_text_input);
        passwordEditText = view.findViewById(R.id.password_edit_text);
        confirmPasswordInputLayout = view.findViewById(R.id.confirm_password_text_input);
        confirmPasswordEditText = view.findViewById(R.id.confirm_password_edit_text);
        renewContainer = view.findViewById(R.id.renew_password);

        phoneInputLayout = view.findViewById(R.id.phone_text_input);
        phoneEditText = view.findViewById(R.id.phone_edit_text);


        nextButton = view.findViewById(R.id.next_button);
        nextButton.setOnClickListener(v -> {
            if (UserEntry.isPhoneValid(phoneEditText.getText())) {
                toVerify = true;
                userPhone = phoneEditText.getText().toString().trim();
                LoaderManager.getInstance(ResetPasswordFragment.this).restartLoader(GET_USER_LOADER, null, ResetPasswordFragment.this);
                phoneInputLayout.setError(null);
            } else {
                phoneInputLayout.setError(getString(R.string.phone_error_check));
            }
        });

        updateButton = view.findViewById(R.id.update_button);
        updateButton.setOnClickListener(v -> {
            if (!UserEntry.isPasswordValid(passwordEditText.getText())) {
                passwordInputLayout.setError(getString(R.string.error_password));
            } else {
                passwordInputLayout.setError(null);
                if (!isConfirmPasswordValid(confirmPasswordEditText.getText())) {
                    confirmPasswordInputLayout.setError(getString(R.string.error_confirm_password));
                } else {
                    confirmPasswordInputLayout.setError(null);
                    update(confirmPasswordEditText.getText().toString().trim());
                }
            }
        });

        phoneInputLayout.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        renewContainer.setVisibility(View.GONE);
        updateButton.setVisibility(View.GONE);

    }

    private void update(String newPassword) {
        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USER_PASSWORD, UserEntry.md5Crypt(newPassword));
        Uri userUri = ContentUris.withAppendedId(UserEntry.CONTENT_URI, getUser.getLong(getUser.getColumnIndex(UserEntry._ID)));
        int rowUpdated = mContext.getContentResolver().update(userUri, values, null, null);

        if (rowUpdated > 0) {
            Toast.makeText(mContext, "Rest password successfully", Toast.LENGTH_SHORT).show();
            ((NavigationHost) mActivity).navigateTo(new LoginFragment(), true);
        } else {
            Toast.makeText(mContext, "Error when saving your account", Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    public void onResume() {
        super.onResume();
        //If phone was verified, display layout for user to change password
        if (sessionManager.getIsVerifyPhone()) {
            phoneInputLayout.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            renewContainer.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.VISIBLE);
            
            //Set verify phone in session to false
            sessionManager.setVerifyPhone(false);
        }

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable  Bundle args) {
        String selection = UserEntry.COLUMN_USER_PHONE + " = ?";
        return new CursorLoader(mContext,
                UserEntry.CONTENT_URI,
                null,
                selection,
                new String[] {userPhone},
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            if (data.moveToFirst()) {
                //Navigate to verify fragment
                if (toVerify) {
                    getUser = data;
                    ((NavigationHost) mActivity).navigateTo(VerifyFragment.NewInstance(userPhone,  ResetPasswordFragment.TAG), true);
                    toVerify = false;
                }

            } else {
                //Phone does not exists
                phoneInputLayout.setError(getString(R.string.phone_does_not_exists));
            }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    private boolean isConfirmPasswordValid(@Nullable Editable text) {
        return text.toString().trim().equals(passwordEditText.getText().toString().trim());
    }
}