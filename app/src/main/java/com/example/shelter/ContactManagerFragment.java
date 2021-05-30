package com.example.shelter;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.shelter.data.SessionManager;
import com.example.shelter.data.ShelterDBContract.HouseEntry;
import com.example.shelter.data.ShelterDBContract.UserEntry;
import com.example.shelter.data.ShelterDBContract.RatingEntry;
import com.example.shelter.network.ImageRequester;
import com.example.shelter.adapter.ContactsAdapter;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactManagerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = ContactManagerFragment.class.getName();

    private static final int GET_LIST_HOUSE_OWNERS_LOADER = 555;
    private static final int GET_LIST_HOUSE_NAMES_OWNERS_LOADER = 999;
    private static final int GET_LIST_USERS_ID_CONTACT_LOADER = 777;
    private static final int GET_LIST_USERS_DATA_LOADER = 888;
    
    //Context and Activity
    private Context mContext;
    private Activity mActivity;

    //Data
    private SessionManager sessionManager;
    private ImageRequester imageRequester;

    private List<String> housesId;
    private List<String> housesName;
    private HashMap<String, Boolean> usersIsSolved;

    private int currentHouseId;
    private int countContactsSolved = 0;
    private int countContactsAlive = 0;

    //Views
    private ImageView contactHouseImageView;
    private TextView houseNameTV;
    private TextView countContactsSolvedTV;
    private TextView countContactsAliveTV;
    private AutoCompleteTextView houseNameSpinner;
    private TextInputLayout houseNameSpinnerInputLayout;
    private ListView contactsListView;

    //Adapter
    ArrayAdapter<String> spinnerHouseNameAdapter;
    ContactsAdapter listContactsAdapter;

    private boolean firstLoad = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mContext = getContext();

        sessionManager = new SessionManager(mContext);
        imageRequester = new ImageRequester(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact_manager, container, false);

        //Find all created view
        houseNameTV = view.findViewById(R.id.house_name);
        contactHouseImageView = view.findViewById(R.id.contact_house_image);
        countContactsAliveTV = view.findViewById(R.id.count_contacts_alive);
        countContactsSolvedTV = view.findViewById(R.id.count_contacts_solved);
        houseNameSpinner = view.findViewById(R.id.spinner_house_name);
        houseNameSpinnerInputLayout = view.findViewById(R.id.spinner_house_name_text_input);

        contactsListView = view.findViewById(R.id.contacts_list);
        listContactsAdapter = new ContactsAdapter(mContext, null);
        contactsListView.setAdapter(listContactsAdapter);
        contactsListView.setEmptyView(null);

        //Set event on item change for houseNameSpinner


        houseNameSpinner.setOnItemClickListener((parent, view1, position, id) -> {
            currentHouseId = Integer.parseInt(housesId.get(position));
            LoaderManager.getInstance(ContactManagerFragment.this).restartLoader(GET_LIST_USERS_ID_CONTACT_LOADER, null, ContactManagerFragment.this);
            imageRequester.loadHeaderImage(currentHouseId, HouseEntry.TABLE_NAME, contactHouseImageView);
            houseNameTV.setText(housesName.get(position));
            countContactsAlive = 0;
            countContactsSolved = 0;
        });

        return view;
    }

    @Override
    public void onResume() {
        //kick the loader
        LoaderManager.getInstance(this).initLoader(GET_LIST_HOUSE_OWNERS_LOADER, null, this);
        super.onResume();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection;
        CursorLoader cursorLoader = null;
        String selection;
        String[] selectionArgs;
        Long userId = ContentUris.parseId(sessionManager.getUserUri());

        switch (id) {
            case GET_LIST_HOUSE_OWNERS_LOADER:
                projection = new String[]{
                        RatingEntry._ID,
                        RatingEntry.COLUMN_HOUSE_ID,
                        RatingEntry.COLUMN_USER_ID,
                        RatingEntry.COLUMN_STARS
                };
                selection = RatingEntry.COLUMN_USER_ID + " = ? AND " + RatingEntry.COLUMN_STARS + " = ?";
                selectionArgs = new String[]{userId.toString(), RatingEntry.HOUSE_OWNER.toString()};
                cursorLoader = new CursorLoader(mContext,   // Parent activity context
                        RatingEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selection,                   // No selection clause
                        selectionArgs,                   // No selection arguments
                        null);// Default sort order
                break;
            case GET_LIST_HOUSE_NAMES_OWNERS_LOADER:
                projection = new String[]{
                        HouseEntry._ID,
                        HouseEntry.COLUMN_HOUSE_NAME
                };
                selection = housesId.toString();
                selection = selection.replace("[", "(");
                selection = selection.replace("]", ")");
                selection = HouseEntry._ID + " IN " + selection;
                selection += " AND " + HouseEntry.COLUMN_HOUSE_STATE + " != " + HouseEntry.STATE_TRUE_DEATH;
                Log.d(TAG, "onCreateLoader: " + selection);
                cursorLoader = new CursorLoader(mContext,   // Parent activity context
                        HouseEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selection,                   // No selection clause
                        null,                   // No selection arguments
                        null);// Default sort order
                break;
            case GET_LIST_USERS_ID_CONTACT_LOADER:
                projection = new String[]{
                        RatingEntry._ID,
                        RatingEntry.COLUMN_HOUSE_ID,
                        RatingEntry.COLUMN_USER_ID,
                        RatingEntry.COLUMN_STARS
                };
                selection = RatingEntry.COLUMN_HOUSE_ID + " =  " + currentHouseId;
                selection += " AND (" + RatingEntry.COLUMN_STARS + " = " + RatingEntry.SEND_CONTACT;
                selection += " OR " + RatingEntry.COLUMN_STARS + " = " + RatingEntry.CONTACT_SOLVED + ")";

                cursorLoader = new CursorLoader(mContext,   // Parent activity context
                        RatingEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selection,                   // No selection clause
                        null,                   // No selection arguments
                        null);// Default sort order
                break;

            case GET_LIST_USERS_DATA_LOADER:
                projection = new String[]{
                        UserEntry._ID,
                        UserEntry.COLUMN_USER_NAME,
                        UserEntry.COLUMN_USER_EMAIL,
                        UserEntry.COLUMN_USER_INCOME,
                        UserEntry.COLUMN_USER_PHONE
                };
                selection = usersIsSolved.keySet().toString();
                selection = selection.replace("[", "(");
                selection = selection.replace("]", ")");
                selection = UserEntry._ID + " IN " + selection;


                cursorLoader = new CursorLoader(mContext,   // Parent activity context
                        UserEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selection,                   // No selection clause
                        null,                   // No selection arguments
                        null);// Default sort order
                break;
            default:
                throw  new IllegalArgumentException("Illegal Loader ID for " + id);
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case GET_LIST_HOUSE_OWNERS_LOADER:
                housesId = new ArrayList<>();
                if (data.moveToFirst()) {
                    do {
                        housesId.add(data.getString(data.getColumnIndex(RatingEntry.COLUMN_HOUSE_ID)));
                    } while (data.moveToNext());
                }
                if (firstLoad) {
                    LoaderManager.getInstance(this).initLoader(GET_LIST_HOUSE_NAMES_OWNERS_LOADER, null, this);
                    firstLoad = false;
                }

                break;

            case GET_LIST_HOUSE_NAMES_OWNERS_LOADER:
                if (data.moveToFirst()) {
                    housesName = new ArrayList<>();
                    do {
                        housesName.add(data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_NAME)));
                    } while (data.moveToNext());
                    Log.d(TAG, "onLoadFinished: GET_LIST_HOUSE_NAMES_OWNERS_LOADER -- " + housesName.toString());


                    String[] asArray = new String[housesName.size()];
                    housesName.toArray(asArray);
                    spinnerHouseNameAdapter = new ArrayAdapter<>(mContext, R.layout.dropdown_menu, asArray);
                    houseNameSpinner.setText(spinnerHouseNameAdapter.getItem(0));
                    houseNameSpinner.setAdapter(spinnerHouseNameAdapter);
                    houseNameTV.setText(spinnerHouseNameAdapter.getItem(0));
                    imageRequester.loadHeaderImage(Integer.parseInt(housesId.get(0)), HouseEntry.TABLE_NAME, contactHouseImageView);
                    currentHouseId = Integer.parseInt(housesId.get(0));
                }
                LoaderManager.getInstance(this).restartLoader(GET_LIST_USERS_ID_CONTACT_LOADER, null, this);
                break;
            case GET_LIST_USERS_ID_CONTACT_LOADER:
                usersIsSolved = new HashMap<>();
                countContactsSolved = 0;
                countContactsAlive = 0;
                if (data.moveToFirst()) {
                    do {
                        int ratingStar = data.getInt(data.getColumnIndex(RatingEntry.COLUMN_STARS));
                        String userId = data.getString(data.getColumnIndex(RatingEntry.COLUMN_USER_ID));
                        if (ratingStar == RatingEntry.CONTACT_SOLVED) {
                            countContactsSolved++;
                            usersIsSolved.put(userId, true);
                        } else if (ratingStar == RatingEntry.SEND_CONTACT) {
                            countContactsAlive++;
                            usersIsSolved.put(userId, false);
                        }
                    } while (data.moveToNext());
                }
                String countSolve = countContactsSolved + " ";
                countSolve += getString(R.string.contact_solved);
                countContactsSolvedTV.setText(countSolve);

                String countAlive = countContactsAlive + " ";
                countAlive += getString(R.string.contacts_alive);
                countContactsAliveTV.setText(countAlive);
                Log.d(TAG, "onLoadFinished: hasMap Userid  " + usersIsSolved.keySet());

                LoaderManager.getInstance(this).restartLoader(GET_LIST_USERS_DATA_LOADER, null, this);
                break;
            case GET_LIST_USERS_DATA_LOADER:
                listContactsAdapter.swapCursor(data);
                listContactsAdapter.renewContactData(currentHouseId, usersIsSolved);
                listContactsAdapter.notifyDataSetChanged();
                break;

        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == GET_LIST_USERS_DATA_LOADER) {
            listContactsAdapter.swapCursor(null);
        }
    }
}