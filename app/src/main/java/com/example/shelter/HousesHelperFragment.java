package com.example.shelter;

import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.shelter.Data.SessionManager;
import com.example.shelter.Data.ShelterDBContract.HouseEntry;
import com.example.shelter.Data.ShelterDBContract.RatingEntry;
import com.example.shelter.adapter.YourFavouriteHouseCursorAdapter;

import java.util.ArrayList;
import java.util.List;

public class HousesHelperFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int GET_LIST_HOUSES_OWNER_ID_LOADER = 1989;
    private static final int GET_DATA_HOUSES = 1990;

    public static final String TAG = HousesHelperFragment.class.getName();
    private SessionManager sessionManager;

    private List<String> housesOwnerID;

    // Title TextView
    private TextView title;
    //Count  Items TextView
    private TextView countItemsTV;
    //TurnBack Button
    private ImageButton turnBackBT;
    //Add a house Button
    private ImageButton addAHouse;
    //Favourite House Cursor Adapter
    private YourFavouriteHouseCursorAdapter mCursorAdapter;
    //List View
    private ListView houseListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.favourite_house_fragment, container, false);
        turnBackBT = view.findViewById(R.id.close_favourite);
        title = view.findViewById(R.id.title);
        countItemsTV = view.findViewById(R.id.count_items_favourite);
        addAHouse = view.findViewById(R.id.add_a_house);
        //Set title
        title.setText(R.string.house_helper);

        //Init session manager
        sessionManager = new SessionManager(getContext());

        //Set close Fragment Listener
        turnBackBT.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        addAHouse.setOnClickListener(v -> {
            Bundle deliver = new Bundle();
            deliver.putInt("houseId", -1);
            HouseHelperItemFragment fragment = new HouseHelperItemFragment();
            fragment.setArguments(deliver);
            ((NavigationHost) getActivity()).navigateTo(fragment, true);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        housesOwnerID = new ArrayList<>();
        //Init Adapter and Listview
        houseListView = view.findViewById(R.id.list_item_favourite);
        mCursorAdapter = new YourFavouriteHouseCursorAdapter(getContext(), null);
        houseListView.setAdapter(mCursorAdapter);

        //Set On Item click listener
        houseListView.setOnItemClickListener((parent, view1, position, id) -> {
            Bundle deliver = new Bundle();
            deliver.putInt("houseId", (int)id);
            HouseHelperItemFragment fragment = new HouseHelperItemFragment();
            fragment.setArguments(deliver);
            ((NavigationHost) getActivity()).navigateTo(fragment, true);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //kick of loader
        LoaderManager.getInstance(this).initLoader(GET_LIST_HOUSES_OWNER_ID_LOADER, null, this);
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
            case GET_LIST_HOUSES_OWNER_ID_LOADER:
                projection = new String[]{
                        RatingEntry._ID,
                        RatingEntry.COLUMN_HOUSE_ID,
                        RatingEntry.COLUMN_USER_ID,
                        RatingEntry.COLUMN_STARS
                };
                selection = RatingEntry.COLUMN_USER_ID + " = ? AND " + RatingEntry.COLUMN_STARS + " = ?";
                selectionArgs = new String[]{userId.toString(), RatingEntry.HOUSE_OWNER.toString()};
                cursorLoader = new CursorLoader(getContext(),   // Parent activity context
                        RatingEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selection,                   // No selection clause
                        selectionArgs,                   // No selection arguments
                        null);// Default sort order
                break;
            case GET_DATA_HOUSES:
                projection = new String[]{
                        HouseEntry._ID,
                        HouseEntry.COLUMN_HOUSE_NAME,
                        HouseEntry.COLUMN_HOUSE_AREA,
                        HouseEntry.COLUMN_HOUSE_LATITUDE,
                        HouseEntry.COLUMN_HOUSE_LONGITUDE,
                        HouseEntry.COLUMN_HOUSE_RENT_COST,
                        HouseEntry.COLUMN_HOUSE_SALE_PRICE,
                        HouseEntry.COLUMN_HOUSE_TYPE_ID,
                        HouseEntry.COLUMN_HOUSE_STATE
                };
                selection = housesOwnerID.toString();
                selection = selection.replace("[", "(");
                selection = selection.replace("]", ")");
                selection = HouseEntry._ID + " IN " + selection;
                selection += " AND " + HouseEntry.COLUMN_HOUSE_STATE + " != " + HouseEntry.STATE_TRUE_DEATH;
                Log.d(TAG, "onCreateLoader: GET_DATA_HOUSES selection: " + selection);

                cursorLoader = new CursorLoader(getContext(),   // Parent activity context
                        HouseEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selection,                   // No selection clause
                        null,                   // No selection arguments
                        HouseEntry.COLUMN_HOUSE_STATE + " DESC");// Default sort order
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case GET_LIST_HOUSES_OWNER_ID_LOADER:
                if (data.moveToFirst()) {
                    String countItems = data.getCount() + " ITEMS";
                    countItemsTV.setText(countItems);
                    do {
                        housesOwnerID.add(data.getString(data.getColumnIndex(RatingEntry.COLUMN_HOUSE_ID)));
                    } while (data.moveToNext());
                    LoaderManager.getInstance(this).restartLoader(GET_DATA_HOUSES, null, this);
                }
                break;
            case GET_DATA_HOUSES:
                mCursorAdapter.swapCursor(data);
                mCursorAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
