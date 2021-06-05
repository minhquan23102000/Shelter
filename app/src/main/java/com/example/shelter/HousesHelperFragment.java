package com.example.shelter;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.shelter.data.SessionManager;
import com.example.shelter.data.ShelterDBContract.HouseEntry;
import com.example.shelter.data.ShelterDBContract.RatingEntry;
import com.example.shelter.adapter.YourFavouriteHouseCursorAdapter;

import java.util.ArrayList;
import java.util.List;

public class HousesHelperFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int GET_LIST_HOUSES_OWNER_ID_LOADER = 1989;
    private static final int GET_DATA_HOUSES = 1990;

    public static final String TAG = HousesHelperFragment.class.getName();
    
    
    //Context and Activity
    private Context mContext;
    private Activity mActivity;
    
    //Data
    private SessionManager sessionManager;
    private List<String> housesOwnerID;

    //Count  Items TextView
    private TextView countItemsTV;
    //Favourite House Cursor Adapter
    private YourFavouriteHouseCursorAdapter mCursorAdapter;


    @Override
    public void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mContext = getContext();
        sessionManager = new SessionManager(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.favourite_house_fragment, container, false);
        //TurnBack Button
        final ImageButton turnBackBT = view.findViewById(R.id.close_favourite);
        // Title TextView
        final TextView title = view.findViewById(R.id.title);
        countItemsTV = view.findViewById(R.id.count_items_favourite);
        //Add a house Button
        final ImageButton addAHouse = view.findViewById(R.id.add_a_house);
        //Set title
        title.setText(R.string.house_helper);

        //Set close Fragment Listener
        turnBackBT.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        addAHouse.setOnClickListener(v -> {
            Fragment fragment = HouseHelperItemFragment.NewInstance(-1);
            ((NavigationHost) mActivity).navigateTo(fragment, true);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        housesOwnerID = new ArrayList<>();
        //Init Adapter and Listview
        //List View
        final ListView houseListView = view.findViewById(R.id.list_item_favourite);
        mCursorAdapter = new YourFavouriteHouseCursorAdapter(mContext, null);
        houseListView.setAdapter(mCursorAdapter);

        //Set On Item click listener
        houseListView.setOnItemClickListener((parent, view1, position, id) ->
                ((NavigationHost) mActivity).navigateTo(HouseHelperItemFragment.NewInstance((int)id), true));

        //Set swipe up to refresh layout
        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            LoaderManager.getInstance(HousesHelperFragment.this)
                    .restartLoader(GET_LIST_HOUSES_OWNER_ID_LOADER, null, HousesHelperFragment.this);
            swipeRefreshLayout.setRefreshing(false);
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
                cursorLoader = new CursorLoader(mContext,   // Parent activity context
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

                cursorLoader = new CursorLoader(mContext,   // Parent activity context
                        HouseEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selection,                   // No selection clause
                        null,                   // No selection arguments
                        HouseEntry.COLUMN_HOUSE_STATE + " DESC");// Default sort order
                break;
            default:
                throw  new IllegalArgumentException("Illegal Loader ID for " + id);
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
