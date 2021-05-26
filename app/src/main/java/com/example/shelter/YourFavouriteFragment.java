package com.example.shelter;


import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.shelter.Data.ShelterDBContract.RatingEntry;
import com.example.shelter.Data.ShelterDBContract.HouseEntry;
import com.example.shelter.Data.SessionManager;
import com.example.shelter.adapter.YourFavouriteHouseCursorAdapter;

import java.util.ArrayList;
import java.util.List;

public class YourFavouriteFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int GET_LIST_HOUSE_FAVOURITE_ID_LOADER = 999;
    private static final int GET_LIST_DATA_HOUSE_FAVOURITE_LOADER = 1000;

    public static final String TAG = YourFavouriteFragment.class.getName();
    private SessionManager sessionManager;

    private List<String> houseFavouriteID;
    private List<String> parameterFavouriteQuery;

    //Count Favourite Items TextView
    private TextView countFavouriteTV;
    //TurnBack Button
    private ImageButton turnBackBT;

    //Favourite House Cursor Adapter
    private YourFavouriteHouseCursorAdapter mCursorAdapter;
    //List View
    private ListView favouriteHouseListView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favourite_house_fragment, container, false);
        sessionManager = new SessionManager(getContext());
        houseFavouriteID = new ArrayList<>();
        parameterFavouriteQuery = new ArrayList<>();

        countFavouriteTV = (TextView) view.findViewById(R.id.count_items_favourite);
        turnBackBT = view.findViewById(R.id.close_favourite);
        final ImageButton addAHouse = view.findViewById(R.id.add_a_house);
        addAHouse.setVisibility(View.GONE);

        //Init Adapter and Listview
        favouriteHouseListView = view.findViewById(R.id.list_item_favourite);
        View emptyView = inflater.inflate(R.layout.empty_view, container, false);
        mCursorAdapter = new YourFavouriteHouseCursorAdapter(getContext(), null);
        favouriteHouseListView.setAdapter(mCursorAdapter);
        favouriteHouseListView.setEmptyView(emptyView);


        //Set Item click listener
        favouriteHouseListView.setOnItemClickListener((parent, view1, position, id) -> {
            String houseUri = ContentUris.withAppendedId(HouseEntry.CONTENT_URI, id).toString();
            ((NavigationHost) getActivity()).navigateTo(HouseDetailFragment.NewInstance(houseUri), true);
        });

        //Set close Fragment Listener
        turnBackBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LoaderManager.getInstance(this).initLoader(GET_LIST_HOUSE_FAVOURITE_ID_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection;
        CursorLoader cursorLoader = null;
        String selection;
        String selectionArgs[];
        Long userId = ContentUris.parseId(sessionManager.getUserUri());
        switch (id) {
            case GET_LIST_HOUSE_FAVOURITE_ID_LOADER:
                projection = new String[]{
                        RatingEntry._ID,
                        RatingEntry.COLUMN_HOUSE_ID,
                        RatingEntry.COLUMN_USER_ID,
                        RatingEntry.COLUMN_STARS
                };
                selection = RatingEntry.COLUMN_USER_ID + " = ? AND " + RatingEntry.COLUMN_STARS + " = ?";
                selectionArgs = new String[]{userId.toString(), RatingEntry.FAVOURITE.toString()};
                cursorLoader = new CursorLoader(getContext(),   // Parent activity context
                        RatingEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selection,                   // No selection clause
                        selectionArgs,                   // No selection arguments
                        null);// Default sort order
                break;
            case GET_LIST_DATA_HOUSE_FAVOURITE_LOADER:
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
                selection = HouseEntry._ID + " in (" + TextUtils.join(",", parameterFavouriteQuery) + ")";
                selection += " AND " + HouseEntry.COLUMN_HOUSE_STATE + " != " + HouseEntry.STATE_TRUE_DEATH;
                selectionArgs = houseFavouriteID.toArray(new String[houseFavouriteID.size()]);

                cursorLoader = new CursorLoader(getContext(),   // Parent activity context
                        HouseEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selection,                   // No selection clause
                        selectionArgs,                   // No selection arguments
                        HouseEntry.COLUMN_HOUSE_STATE + " DESC");// Default sort order
                break;
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case GET_LIST_HOUSE_FAVOURITE_ID_LOADER:
                if (data.moveToFirst()) {
                    String countItems = data.getCount() + " ITEMS";
                    countFavouriteTV.setText(countItems);
                    do {
                        houseFavouriteID.add(data.getString(data.getColumnIndex(RatingEntry.COLUMN_HOUSE_ID)));
                        parameterFavouriteQuery.add("?");
                    } while (data.moveToNext());
                    LoaderManager.getInstance(this).initLoader(GET_LIST_DATA_HOUSE_FAVOURITE_LOADER, null, this);
                }
                break;
            case GET_LIST_DATA_HOUSE_FAVOURITE_LOADER:
                mCursorAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case GET_LIST_DATA_HOUSE_FAVOURITE_LOADER:
                mCursorAdapter.swapCursor(null);
                break;
        }
    }


}
