package com.example.shelter.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.shelter.Data.ShelterDBContract.RatingEntry;
import com.example.shelter.Data.ShelterDBContract.UserEntry;
import com.example.shelter.Data.ShelterDBHelper;
import com.example.shelter.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class ContactsAdapter extends CursorAdapter {

    static final public String TAG = ContactsAdapter.class.getName();
    private Context mContext;

    //Data
    private int contactsHouseId;
    private List<Boolean> contactIsSolved;

    public ContactsAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        mContext = context;
    }

    public ContactsAdapter(Context context, Cursor c, int currentHouseId, List<Boolean> contactIsSolved) {
        super(context, c, 0 /* flags */);
        mContext = context;
        this.contactIsSolved = contactIsSolved;
        contactsHouseId = currentHouseId;
    }

    public void renewContactData(int contactsHouseId, List<Boolean> contactIsSolved) {
        this.contactsHouseId = contactsHouseId;
        this.contactIsSolved = contactIsSolved;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.contacts_list_items, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        view.setVisibility(View.VISIBLE);
        //All view needed
        final TextView userNameTV = view.findViewById(R.id.viewer_name);
        final TextView userEmailTV = view.findViewById(R.id.viewer_email);
        final TextView userIncomeTV = view.findViewById(R.id.viewer_income);
        final TextView userPhoneTV = view.findViewById(R.id.viewer_phone);
        final MaterialButton solveButton = view.findViewById(R.id.solve_button);
        final ImageButton deleteContactButton = view.findViewById(R.id.delete_item);

        //Get data form cursor
        String userId = cursor.getString(cursor.getColumnIndex(UserEntry._ID));
        String userPhone = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_USER_PHONE));
        String userEmail = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_USER_EMAIL));
        String userName = cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_USER_NAME));
        Float userIncome = cursor.getFloat(cursor.getColumnIndex(UserEntry.COLUMN_USER_INCOME));
        boolean contactSolve = false;
        if (contactIsSolved != null) {
            contactSolve = contactIsSolved.get(cursor.getPosition());
        }

        Log.d(TAG, "bindView: contact solve " + contactIsSolved.toString());
        //Display data to layout
        userNameTV.setText(userName);
        userEmailTV.setText(userEmail);
        if (userIncome > 0) {
            userIncomeTV.setText(ShelterDBHelper.formatPrice(userIncome, mContext));
        } else {
            userIncomeTV.setText("No data");
        }

        userPhoneTV.setText(userPhone);


        if (!contactSolve) {
            solveButton.setText(R.string.contact_solve);
            solveButton.setBackgroundColor(mContext.getColor(R.color.colorPrimaryDark));
        } else {
            solveButton.setText(R.string.solved);
            solveButton.setBackgroundColor(mContext.getColor(R.color.secondaryColor));
        }

        boolean finalContactSolve = contactSolve;
        solveButton.setOnClickListener(v -> {
            if (!finalContactSolve) {
                ContentValues values = new ContentValues();
                values.put(RatingEntry.COLUMN_STARS, RatingEntry.CONTACT_SOLVED);
                String where = RatingEntry.COLUMN_HOUSE_ID + " = " + contactsHouseId;
                where += " AND " + RatingEntry.COLUMN_USER_ID + " = " + userId;
                where += " AND " + RatingEntry.COLUMN_STARS + " = " + RatingEntry.SEND_CONTACT;
                mContext.getContentResolver().update(RatingEntry.CONTENT_URI, values, where, null);
                Log.d(TAG, "bindView: update contact " + contactsHouseId + " " + userId );
            }
            solveButton.setText(R.string.solved);
            solveButton.setBackgroundColor(mContext.getColor(R.color.secondaryColor));
        });

        deleteContactButton.setOnClickListener( v -> {
            new MaterialAlertDialogBuilder(mContext)
                    .setMessage(R.string.delete_contact_warning)
                    .setNeutralButton("Cancel", (dialog, which) -> {

                    })
                    .setPositiveButton("Yes", (dialog, which) -> {
                        String where = RatingEntry.COLUMN_HOUSE_ID + " = " + contactsHouseId;
                        where += " AND " + RatingEntry.COLUMN_USER_ID + " = " + userId;
                        where += " AND (" + RatingEntry.COLUMN_STARS + " = " + RatingEntry.SEND_CONTACT;
                        where += " OR " + RatingEntry.COLUMN_STARS + " = " + RatingEntry.CONTACT_SOLVED + " )";
                        mContext.getContentResolver().delete(RatingEntry.CONTENT_URI, where, null);
                        view.setVisibility(View.GONE);
                    })
                    .show();
        });
    }
}
