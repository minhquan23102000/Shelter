package com.example.shelter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.shelter.data.SessionManager;
import com.example.shelter.data.ShelterDBContract;
import com.example.shelter.data.ShelterDBContract.UserEntry;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MyAccountFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    final static public String LOG_TAG = MyAccountFragment.class.getSimpleName();

    //Loader ID
    static final private int USER_LOADER = 199;
    
    final public String[] dropdownMenuItems = {
            "Male",
            "Female",
    };
    
    
    //Context and Activity
    private Context mContext;
    private Activity mActivity;

    //All views in my account fragment
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

    //User's data
    private Cursor userData = null;
    private SessionManager sessionManager;

    private ArrayAdapter<String> genderAdapter;

    @Override
    public void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mContext = getContext();

        //Init session manger for this fragment
        sessionManager = new SessionManager(mContext);
        
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sign_up_fragment, container, false);


        Log.d(LOG_TAG, "onCreateView: " + sessionManager.getUserUri());
        //Find correspond view in layout
        nextButton = (MaterialButton) view.findViewById(R.id.next_button);
        backButton = (MaterialButton) view.findViewById(R.id.back_button);
        dateInputLayout = (TextInputLayout) view.findViewById(R.id.date_of_birth_text_input);
        dateEditText = (TextInputEditText) view.findViewById(R.id.date_of_birth_edit_text);
        passwordInputLayout = (TextInputLayout) view.findViewById(R.id.password_text_input);
        passwordEditText = (TextInputEditText) view.findViewById(R.id.password_edit_text);
        confirmPasswordInputLayout = (TextInputLayout) view.findViewById(R.id.confirm_password_text_input);
        confirmPasswordEditText = (TextInputEditText) view.findViewById(R.id.confirm_password_edit_text);
        phoneInputLayout = (TextInputLayout) view.findViewById(R.id.phone_text_input);
        phoneEditText = (TextInputEditText) view.findViewById(R.id.phone_edit_text);
        emailInputLayout = (TextInputLayout) view.findViewById(R.id.email_text_input);
        emailEditText = (TextInputEditText) view.findViewById(R.id.email_edit_text);
        incomeInputLayout = (TextInputLayout) view.findViewById(R.id.income_text_input);
        incomeEditText = (TextInputEditText) view.findViewById(R.id.income_edit_text);
        nameInputLayout = (TextInputLayout) view.findViewById(R.id.name_text_input);
        nameEditText = (TextInputEditText) view.findViewById(R.id.name_edit_text);

        //Reset view for my account fragment
        nextButton.setText(R.string.change);
        backButton.setText(R.string.redo);

        confirmPasswordInputLayout.setVisibility(View.GONE);
        passwordInputLayout.setVisibility(View.GONE);

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
        genderAdapter = new ArrayAdapter<>(mContext, R.layout.dropdown_menu, dropdownMenuItems);
        autoCompleteTextView.setText(genderAdapter.getItem(0), false);
        autoCompleteTextView.setAdapter(genderAdapter);

        setErrorInputCheck();

        backButton.setOnClickListener(v -> LoaderManager.getInstance(MyAccountFragment.this).restartLoader(USER_LOADER, null, MyAccountFragment.this));

        return view;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                UserEntry._ID,
                UserEntry.COLUMN_USER_NAME,
                UserEntry.COLUMN_USER_PHONE,
                UserEntry.COLUMN_USER_EMAIL,
                UserEntry.COLUMN_USER_DATE_BIRTH,
                UserEntry.COLUMN_USER_INCOME,
                UserEntry.COLUMN_USER_GENDER};

        return new CursorLoader(mContext,   // Parent activity context
                sessionManager.getUserUri(),   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);// Default sort order
    }

    @Override
    public void onResume() {
        super.onResume();
        LoaderManager.getInstance(this).initLoader(USER_LOADER, null, this);
        //If verify phone pass back that is verify phone is true, than we save this account;
        if (sessionManager.getIsVerifyPhone()) {
            saveAccount();
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        //Check cursor if is null and move cursor to the first row
        if (data.moveToFirst()) {
            userData = data;
            String userName = userData.getString(data.getColumnIndex(UserEntry.COLUMN_USER_NAME));
            String userPhone = userData.getString(userData.getColumnIndex(UserEntry.COLUMN_USER_PHONE));
            String userBirthDay = userData.getString(userData.getColumnIndex(UserEntry.COLUMN_USER_DATE_BIRTH));
            String userEmail = userData.getString(data.getColumnIndex(UserEntry.COLUMN_USER_EMAIL));
            Integer userGender = userData.getInt(userData.getColumnIndex(UserEntry.COLUMN_USER_GENDER));
            String userIncome = userData.getString(userData.getColumnIndex(UserEntry.COLUMN_USER_INCOME));

            nameEditText.setText(userName);
            phoneEditText.setText(userPhone);
            emailEditText.setText(userEmail);
            dateEditText.setText(userBirthDay);
            autoCompleteTextView.setText(genderAdapter.getItem(userGender), false);
            incomeEditText.setText(userIncome);
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        userData= null;
    }

    private void setErrorInputCheck() {
        nextButton.setOnClickListener(v -> {
            boolean flag = true;

            //Get data from UI
            Editable phone = phoneEditText.getText();
            String phoneString = null;
            if (phone != null && phone.length() > 0) {
                phoneString = phone.toString().trim();
            }
            Editable email = emailEditText.getText();

            //Get phone and email from database
            String phoneInData = userData.getString(userData.getColumnIndex(UserEntry.COLUMN_USER_PHONE));
            String emailInData = userData.getString(userData.getColumnIndex(UserEntry.COLUMN_USER_EMAIL));


            if (!UserEntry.isNameValid(nameEditText.getText())) {
                nameInputLayout.setError(getString(R.string.name_error_check));
                flag = false;
            } else {
                nameInputLayout.setError(null);
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

            if (!UserEntry.isEmailValid(email)) {
                emailInputLayout.setError(getString(R.string.email_error_check));
                flag = false;
            } else if (UserEntry.checkIfIsExists(email.toString().trim(), UserEntry.COLUMN_USER_EMAIL, mContext)
                    && !email.toString().trim().equals(emailInData)) {
                emailInputLayout.setError(getString(R.string.email_already_exists));
                flag = false;
            }else {
                emailInputLayout.setError(null);
            }

            if (!UserEntry.isPhoneValid(phone) ) {
                phoneInputLayout.setError(getString(R.string.phone_error_check));
                flag = false;
            } else if (UserEntry.checkIfIsExists(phoneString, UserEntry.COLUMN_USER_PHONE, mContext)
                    && !phoneString.equals(phoneInData) ) {
                phoneInputLayout.setError(getString(R.string.phone_already_exists));
                flag = false;

            } else {
                phoneInputLayout.setError(null);
            }


            //If all data are valid,
            if (flag) {
                //Init temp data for deliver to another fragment
                sessionManager.initUserTempData(phoneString, email.toString().trim(),
                        nameEditText.getText().toString().trim(),
                        Float.parseFloat(incomeEditText.getText().toString().trim()),
                        dateEditText.getText().toString().trim(),
                        genderAdapter.getPosition(autoCompleteTextView.getText().toString().trim()));

                //If user input a new phone we navigate to verify fragment else we save account directly
                if (!phoneString.equals(phoneInData) ) {
                    sessionManager.setVerifyPhone(false);
                    Fragment toFragment = VerifyFragment.NewInstance(phoneString, LOG_TAG);
                    ((NavigationHost) mActivity).navigateTo(toFragment, true);
                } else {
                    saveAccount();
                }

            }
        });



        // Listen user's typing. Clear the error when valid
        //Clear the error
        //Clear the error
        View.OnKeyListener onKeyListener = (v, keyCode, event) -> {
            int id = v.getId();
            if (id == R.id.name_edit_text) {
                if (UserEntry.isNameValid(nameEditText.getText())) {
                    nameInputLayout.setError(null); //Clear the error
                }
            } else if (id == R.id.phone_edit_text) {
                if (UserEntry.isPhoneValid(phoneEditText.getText())) {
                    phoneInputLayout.setError(null);
                }
            } else if (id == R.id.date_of_birth_edit_text) {
                if (UserEntry.isDateBirthValid(dateEditText.getText())) {
                    dateInputLayout.setError(null); //Clear the error
                }
            } else if (id == R.id.email_edit_text) {
                if (UserEntry.isEmailValid(emailEditText.getText())) {
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
        //Get values to update
        Log.d(LOG_TAG, "saveAccount: " + nameEditText.getText() + phoneEditText.getText() + incomeEditText.getText());

        String name = sessionManager.getUserTempName();
        String phone = sessionManager.getVerifyPhone();
        String email = sessionManager.getKeyUserTempEmail();
        String date_birth = sessionManager.getKeyUserTempDayBirth();
        float income = sessionManager.getUserTempInCome();
        int gender = sessionManager.getUserTempGender();


        ContentValues values = new ContentValues();
        values.put(UserEntry.COLUMN_USER_NAME, name);
        values.put(UserEntry.COLUMN_USER_PHONE, phone);
        values.put(UserEntry.COLUMN_USER_EMAIL, email);
        values.put(UserEntry.COLUMN_USER_DATE_BIRTH, date_birth);
        values.put(UserEntry.COLUMN_USER_INCOME, income);
        values.put(UserEntry.COLUMN_USER_GENDER, gender);


        int row_effect = mContext.getContentResolver().update(sessionManager.getUserUri(), values, null, null);

        // Show a toast message depending on whether or not the update was successful.
        if (row_effect < 1) {
            // If the new content URI is null, then there was an error with update.
            Toast.makeText(mContext.getApplicationContext(), getString(R.string.error_saving_user_account), Toast.LENGTH_SHORT).show();

        } else {
            // Otherwise, the update was successful and we can update session and refresh layout
            SessionManager sessionManager = new SessionManager(mContext);
            sessionManager.initUserSession(phone, email, sessionManager.getUserUri().toString(), name, income, date_birth, gender, sessionManager.getUserRole());
            Toast.makeText(mContext, "Save successfully!", Toast.LENGTH_SHORT).show();
            final FragmentTransaction ft = getParentFragmentManager().beginTransaction();
            ft.detach(MyAccountFragment.this).attach(MyAccountFragment.this).commit();
        }
        //set verify phone to false so it will not trigger again when this fragment resume
        sessionManager.setVerifyPhone(false);
        //Clear User Temp Data From Session
        sessionManager.clearUserTempData();
    }

}
