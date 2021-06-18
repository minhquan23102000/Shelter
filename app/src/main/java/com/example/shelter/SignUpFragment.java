package com.example.shelter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.shelter.data.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.example.shelter.data.ShelterDBContract.UserEntry;

public class SignUpFragment extends Fragment {
    static final public String LOG_TAG = SignUpFragment.class.getSimpleName();
    static final public String[] dropdownMenuItems = {
            "Male",
            "Female",
    };
    
    
    private Context mContext;
    private Activity mActivity;
    
    
    private MaterialButton nextButton;
    private MaterialButton backButton;
    private TextInputLayout dateInputLayout;
    private TextInputEditText dateEditText;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordEditText;
    private TextInputLayout confirmPasswordInputLayout;
    private TextInputEditText confirmPasswordEditText;
    private TextInputLayout phoneInputLayout;
    private TextInputEditText phoneEditText;
    private TextInputLayout emailInputLayout;
    private TextInputEditText emailEditText;
    private TextInputLayout incomeInputLayout;
    private TextInputEditText incomeEditText;
    private TextInputLayout nameInputLayout;
    private TextInputEditText nameEditText;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sign_up_fragment, container, false);
        //Find correspond view in layout
        nextButton = view.findViewById(R.id.next_button);
        backButton = view.findViewById(R.id.back_button);
        dateInputLayout = view.findViewById(R.id.date_of_birth_text_input);
        dateEditText = view.findViewById(R.id.date_of_birth_edit_text);
        passwordInputLayout = view.findViewById(R.id.password_text_input);
        passwordEditText = view.findViewById(R.id.password_edit_text);
        confirmPasswordInputLayout = view.findViewById(R.id.confirm_password_text_input);
        confirmPasswordEditText = view.findViewById(R.id.confirm_password_edit_text);
        phoneInputLayout = view.findViewById(R.id.phone_text_input);
        phoneEditText = view.findViewById(R.id.phone_edit_text);
        emailInputLayout = view.findViewById(R.id.email_text_input);
        emailEditText = view.findViewById(R.id.email_edit_text);
        incomeInputLayout = view.findViewById(R.id.income_text_input);
        incomeEditText = view.findViewById(R.id.income_edit_text);
        nameInputLayout = view.findViewById(R.id.name_text_input);
        nameEditText = view.findViewById(R.id.name_edit_text);


        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Happy Birth Day");
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());
        builder.setTheme(R.style.ThemeOverlay_App_DatePicker);


        final MaterialDatePicker<Long> materialDatePicker = builder.build();
        dateInputLayout.setStartIconOnClickListener(v -> materialDatePicker.show(getChildFragmentManager(), "DATE_PICKER"));

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            // Get the offset from our timezone and UTC.
            TimeZone timeZoneUTC = TimeZone.getDefault();
            // It will be negative, so that's the -1
            int offsetFromUTC = timeZoneUTC.getOffset(new Date().getTime()) * -1;
            // Create a date format, then a date object with our offset
            SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date(selection + offsetFromUTC);

            dateEditText.setText(simpleFormat.format(date));
        });

        //Set item for the drop down menu
        autoCompleteTextView = view.findViewById(R.id.gender_menu);
        arrayAdapter = new ArrayAdapter<String>(mContext, R.layout.dropdown_menu, dropdownMenuItems);
        autoCompleteTextView.setText(arrayAdapter.getItem(0).toString(), false);
        autoCompleteTextView.setAdapter(arrayAdapter);

        //Set up input valid check
        setErrorInputCheck();

        backButton.setOnClickListener(v -> mActivity.onBackPressed());

        return view;
    }



    private void setErrorInputCheck() {
        nextButton.setOnClickListener(v -> {
            boolean flag = true;
            Editable phone = phoneEditText.getText();
            Editable email = emailEditText.getText();
            if (!UserEntry.isPasswordValid(passwordEditText.getText())) {
                passwordInputLayout.setError(getString(R.string.error_password));
                flag = false;
            } else {
                passwordInputLayout.setError(null);
            }

            if (!isConfirmPasswordValid(confirmPasswordEditText.getText())) {
                confirmPasswordInputLayout.setError(getString(R.string.error_confirm_password));
                flag = false;
            } else {
                confirmPasswordInputLayout.setError(null);
            }

            if (!UserEntry.isNameValid(nameEditText.getText())) {
                nameInputLayout.setError(getString(R.string.name_error_check));
                flag = false;
            } else {
                nameInputLayout.setError(null);
            }

            if (!UserEntry.isEmailValid(email)) {
                emailInputLayout.setError(getString(R.string.email_error_check));
                flag = false;
            } else if (UserEntry.checkIfIsExists(email.toString().trim(), UserEntry.COLUMN_USER_EMAIL, mContext.getApplicationContext())) {
                emailInputLayout.setError(getString(R.string.email_already_exists));
                flag = false;
            } else {
                emailInputLayout.setError(null);

            }

            if (!UserEntry.isPhoneValid(phone)) {
                phoneInputLayout.setError(getString(R.string.phone_error_check));
                flag = false;
            } else if (UserEntry.checkIfIsExists(phone.toString().trim(), UserEntry.COLUMN_USER_PHONE, mContext.getApplicationContext())) {
                phoneInputLayout.setError(getString(R.string.phone_already_exists));
                flag = false;
            } else {
                phoneInputLayout.setError(null);
            }

            if (!UserEntry.isDateBirthValid(dateEditText.getText())) {
                dateInputLayout.setError(getString(R.string.date_birth_error_check));
                flag = false;
            } else {
                dateInputLayout.setError(null);
            }

            if (!UserEntry.isIncomeValid(incomeEditText.getText())) {
                incomeInputLayout.setError(getString(R.string.income_error_check));
                flag = false;
            } else {
                incomeInputLayout.setError(null);
            }

            //If all data are valid, navigate to verify fragment
            if (flag) {
                saveAccount();
            }
        });

        // Listen user's typing. Clear the error when valid
        View.OnKeyListener onKeyListener = (v, keyCode, event) -> {
            int id = v.getId();
            if (id == R.id.password_edit_text) {
                if (UserEntry.isPasswordValid(passwordEditText.getText())) {
                    passwordInputLayout.setError(null); //Clear the error
                }
            } else if (id == R.id.confirm_password_edit_text) {
                if (isConfirmPasswordValid(confirmPasswordEditText.getText())) {
                    confirmPasswordInputLayout.setError(null);
                }
            } else if (id == R.id.name_edit_text) {
                if (UserEntry.isNameValid(nameEditText.getText())) {
                    nameInputLayout.setError(null); //Clear the error
                }
            } else if (id == R.id.phone_edit_text) {
                if (UserEntry.isPhoneValid(phoneEditText.getText()) &&
                        !UserEntry.checkIfIsExists(phoneEditText.getText().toString().trim(), UserEntry.COLUMN_USER_PHONE, mContext.getApplicationContext())) {
                    phoneInputLayout.setError(null);
                }
            } else if (id == R.id.date_of_birth_edit_text) {
                if (UserEntry.isDateBirthValid(dateEditText.getText())) {
                    dateInputLayout.setError(null); //Clear the error
                }
            } else if (id == R.id.email_edit_text) {
                if (UserEntry.isEmailValid(emailEditText.getText()) &&
                        !UserEntry.checkIfIsExists(emailEditText.getText().toString().trim(), UserEntry.COLUMN_USER_EMAIL, mContext.getApplicationContext())) {
                    emailInputLayout.setError(null);
                }
            } else if (id == R.id.income_edit_text) {
                if (UserEntry.isIncomeValid(incomeEditText.getText())) {
                    incomeInputLayout.setError(null);
                }
            }
            return false;
        };
        passwordEditText.setOnKeyListener(onKeyListener);
        confirmPasswordEditText.setOnKeyListener(onKeyListener);
        emailEditText.setOnKeyListener(onKeyListener);
        nameEditText.setOnKeyListener(onKeyListener);
        phoneEditText.setOnKeyListener(onKeyListener);
        incomeEditText.setOnKeyListener(onKeyListener);
        dateEditText.setOnKeyListener(onKeyListener);

    }

    private void saveAccount() {
        //Get values to insert
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String date_birth = dateEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        Float income = Float.valueOf(incomeEditText.getText().toString().trim());
        int gender = arrayAdapter.getPosition(autoCompleteTextView.getText().toString().trim());


        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USER_NAME, name);
        values.put(UserEntry.COLUMN_USER_PHONE, phone);
        values.put(UserEntry.COLUMN_USER_EMAIL, email);
        values.put(UserEntry.COLUMN_USER_DATE_BIRTH, date_birth);
        values.put(UserEntry.COLUMN_USER_PASSWORD, UserEntry.md5Crypt(password));
        values.put(UserEntry.COLUMN_USER_INCOME, income);
        values.put(UserEntry.COLUMN_USER_GENDER, gender);
        values.put(UserEntry.COLUMN_USER_ROLE_ID, UserEntry.VIEWER);

        //Time to insert
        // This is a NEW user, so insert a new user into the provider,
        // returning the content URI for the user.
        Uri newUri = mContext.getApplicationContext().getContentResolver().insert(UserEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful.
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(mContext.getApplicationContext(), getString(R.string.error_saving_user_account), Toast.LENGTH_SHORT).show();

        } else {
            // Otherwise, the insertion was successful and we can navigate to verify Fragment
            SessionManager sessionManager = new SessionManager(mContext);
            sessionManager.initUserSession(phone, email, newUri.toString(), name, 2);
            Fragment toFragment = VerifyFragment.NewInstance(phone, LOG_TAG);
            ((NavigationHost) mActivity).navigateTo(toFragment, true);
        }
    }


    private boolean isConfirmPasswordValid(@Nullable Editable text) {
        return text.toString().trim().equals(passwordEditText.getText().toString().trim());
    }


}
