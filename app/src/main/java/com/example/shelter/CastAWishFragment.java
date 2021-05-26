package com.example.shelter;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.shelter.Data.SessionManager;
import com.example.shelter.Data.ShelterDBContract.HouseEntry;
import com.example.shelter.Data.ShelterDBContract.HouseTypeEntry;
import com.example.shelter.Data.ShelterDBContract.WishEntry;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.shelter.Data.ShelterDBHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;


public class CastAWishFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = CastAWishFragment.class.getName();
    //ID FOR LOADERS
    private static final int HOUSE_TYPE_LOADER = 734;
    //Session
    private SessionManager sessionManager;
    //Declare all views in this layout
    private TextInputEditText areaEditText;
    private TextInputLayout areaInputLayout;
    private TextInputLayout yearBuiltInputLayout;
    private TextInputEditText yearBuiltEditText;
    private TextInputLayout floorInputLayout;
    private TextInputEditText floorEditText;
    private TextInputLayout bathRoomsInputLayout;
    private TextInputEditText bathRoomsEditText;
    private TextInputLayout bedRoomsInputLayout;
    private TextInputEditText bedRoomsEditText;
    private TextInputLayout rentCostInputLayout;
    private TextInputEditText rentCostEditText;
    private TextInputLayout salePriceInputLayout;
    private TextInputEditText salePriceEditText;
    private TextInputLayout wishfulPointInputLayout;
    private TextInputEditText wishfulPointEditText;
    private AutoCompleteTextView placeText;
    private AutoCompleteTextView houseTypeText;
    private MaterialButton backButton;
    private MaterialButton castYourWishButton;

    private ArrayAdapter<String> placeAdapter;
    private ArrayAdapter<String> houseTypeAdapter;
    private List<String> houseTypeList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cast_a_wish, container, false);

        areaEditText = view.findViewById(R.id.area_edit_text);
        areaInputLayout = view.findViewById(R.id.area_text_input);
        yearBuiltEditText = view.findViewById(R.id.yearbuilt_edit_text);
        yearBuiltInputLayout = view.findViewById(R.id.yearbuilt_text_input);
        floorEditText = view.findViewById(R.id.floor_edit_text);
        floorInputLayout = view.findViewById(R.id.floor_text_input);
        bathRoomsEditText = view.findViewById(R.id.bathrooms_edit_text);
        bathRoomsInputLayout = view.findViewById(R.id.bathrooms_text_input);
        bedRoomsEditText = view.findViewById(R.id.bedrooms_edit_text);
        bedRoomsInputLayout = view.findViewById(R.id.bedrooms_text_input);
        rentCostEditText = view.findViewById(R.id.rent_cost_edit_text);
        salePriceEditText = view.findViewById(R.id.sale_price_edit_text);
        wishfulPointEditText = view.findViewById(R.id.wishful_point_edit_text);
        wishfulPointInputLayout = view.findViewById(R.id.wishful_point_text_input);

        castYourWishButton = view.findViewById(R.id.next_button);
        backButton = view.findViewById(R.id.back_button);


        placeText = view.findViewById(R.id.place_text);
        houseTypeText = view.findViewById(R.id.house_type_text);
        //we set its adapter later we query in database

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Init session manger
        sessionManager = new SessionManager(getContext());

        //Set  wishfulPoint text view on start icon click to navigate to MapFragment
        wishfulPointInputLayout.setStartIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If session has not yet located wishfulPoint we navigate to MapFragment
                //Else we clear it and set text to null
                if (sessionManager.getWishfulPointName() == null) {
                   double latPoint, lngPoint;
                    try {
                      latPoint = MainActivity.getUserLocation().getLatitude();
                      lngPoint = MainActivity.getUserLocation().getLongitude();
                    } catch (NullPointerException e) {
                       latPoint = MapsFragment.LAND_MARK_TOWER.latitude;
                       lngPoint = MapsFragment.LAND_MARK_TOWER.longitude;
                    }
                    Fragment mapFragment = MapsFragment.NewInstance(CastAWishFragment.TAG, latPoint, lngPoint);

                    ((MainActivity) getActivity()).navigateTo(mapFragment, true);
                    wishfulPointInputLayout.setStartIconDrawable(R.drawable.outline_clear_24);
                } else {
                    sessionManager.clearWishfulPoint();
                    wishfulPointEditText.setText(null);
                    wishfulPointInputLayout.setStartIconDrawable(R.drawable.outline_place_24);
                }
            }
        });

        //Set on click for the buttons
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStackImmediate();
            }
        });
        castYourWishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get place value from UI
                int place = placeAdapter.getPosition(placeText.getText().toString().trim());

                //get houseType value from UI then query its ID from database
                String houseTypeName = houseTypeAdapter.getItem(houseTypeAdapter.getPosition(houseTypeText.getText().toString().trim())).trim();
                int houseTypeId  = 0;
                Cursor houseTypeCursor = getContext().getContentResolver().query(HouseTypeEntry.CONTENT_URI,
                        null,
                        HouseTypeEntry.COLUMN_HOUSE_TYPE_NAME + " = ?",
                        new String[] {houseTypeName},
                        null);

                if (houseTypeCursor.moveToFirst()) {
                    houseTypeId = houseTypeCursor.getInt(houseTypeCursor.getColumnIndex(HouseTypeEntry._ID));
                }
                Log.d(TAG, "onClick: houseType Query check " + houseTypeId + " " + houseTypeName);

                houseTypeCursor.close();  //close the cursor

                //get the rest of values
                float area, rentCost, salePrice;
                int floor, bedrooms, bathrooms, yearBuilt;
                String areaString = areaEditText.getText().toString();
                String floorString = floorEditText.getText().toString();
                String bedroomsString = bedRoomsEditText.getText().toString();
                String bathroomsString = bathRoomsEditText.getText().toString();
                String yearBuiltString = yearBuiltEditText.getText().toString();
                String rentCostString = rentCostEditText.getText().toString();
                String salePriceString = salePriceEditText.getText().toString();

                //Check if text is empty, then set it to zero, else get the value
                if (TextUtils.isEmpty(areaString)) {
                    area = 0;
                } else {
                    area = Float.parseFloat(areaString);
                }

                if (TextUtils.isEmpty(floorString)) {
                    floor = 0;
                } else {
                    floor = Integer.parseInt(floorString);
                }

                if (TextUtils.isEmpty(bathroomsString)) {
                    bathrooms = 0;
                } else {
                    bathrooms = Integer.parseInt(bathroomsString);
                }
                if (TextUtils.isEmpty(bedroomsString)) {
                    bedrooms = 0;
                } else {
                    bedrooms = Integer.parseInt(bedroomsString);
                }
                if (TextUtils.isEmpty(yearBuiltString)) {
                    yearBuilt = 0;
                } else {
                    yearBuilt = Integer.parseInt(yearBuiltString);
                }
                if (TextUtils.isEmpty(salePriceString)) {
                    salePrice = 0;
                } else {
                    salePrice = Float.parseFloat(salePriceString);
                }
                if (TextUtils.isEmpty(rentCostString)) {
                    rentCost = 0;
                } else {
                    rentCost = Float.parseFloat(rentCostString);
                }


                //Wishful point name and LatLng is already put into session in maps fragment so we don't do it here anymore;

                //Put them into session
                sessionManager.initWishfulPointData(place, houseTypeId, area, yearBuilt, floor, bedrooms, bathrooms, salePrice, rentCost);
                //Add them to database
                ContentValues values = new ContentValues();
                values.put(WishEntry.COLUMN_WISH_PLACE, place);
                if (houseTypeId > 0) {
                    values.put(WishEntry.COLUMN_HOUSE_TYPE_ID, houseTypeId);
                }
                values.put(WishEntry.COLUMN_WISH_AREA, area);
                values.put(WishEntry.COLUMN_WISH_YEAR_BUILT, yearBuilt);
                values.put(WishEntry.COLUMN_WISH_FLOORS, floor);
                values.put(WishEntry.COLUMN_WISH_BED_ROOMS, bedrooms);
                values.put(WishEntry.COLUMN_WISH_BATH_ROOMS, bathrooms);
                values.put(WishEntry.COLUMN_NEAR_POINT_LATITUDE, sessionManager.getWishfulPointLatLng().latitude);
                values.put(WishEntry.COLUMN_NEAR_POINT_LONGITUDE, sessionManager.getWishfulPointLatLng().longitude);
                values.put(WishEntry.COLUMN_USER_ID, ContentUris.parseId(sessionManager.getUserUri()));
                getContext().getContentResolver().insert(WishEntry.CONTENT_URI, values);

                //Add a wish relative to the house type table
                ShelterDBHelper.increaseValueToOne(HouseTypeEntry.TABLE_NAME, HouseTypeEntry.COLUMN_HOUSE_COUNT_WISH,
                                    sessionManager.getWishfulPointHouseType(), getContext());

                //Back to house grid fragment
                if (CastAWishFragment.this.isAdded()) {
                    getParentFragmentManager().popBackStackImmediate();
                }

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //Kick the loader when the view is visible to the user
        LoaderManager.getInstance(this).initLoader(HOUSE_TYPE_LOADER, null ,this);

        //Display last user's wishful point  if exists
        String mapFragmentReturnPlaceName = sessionManager.getWishfulPointName();
        if (mapFragmentReturnPlaceName != null) {
            wishfulPointEditText.setText(mapFragmentReturnPlaceName);
            wishfulPointInputLayout.setStartIconDrawable(R.drawable.outline_clear_24);
        } else {
            wishfulPointEditText.setText(null);
            wishfulPointInputLayout.setStartIconDrawable(R.drawable.outline_place_24);
        }

        //Set adapter for the spinner
        placeAdapter = new ArrayAdapter<String>(getContext(), R.layout.dropdown_menu, HouseEntry.POSSIBLE_VALUE_PLACES);
        placeText.setAdapter(placeAdapter);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader cursorLoader = null;
        switch (id) {
            case HOUSE_TYPE_LOADER:

                cursorLoader = new CursorLoader(getContext(),
                        HouseTypeEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case HOUSE_TYPE_LOADER:
                houseTypeList = new ArrayList<>();
                houseTypeList.add("Any");
                if (data.moveToFirst()) {
                    do {
                        houseTypeList.add(data.getString(data.getColumnIndex(HouseTypeEntry.COLUMN_HOUSE_TYPE_NAME)));
                    } while (data.moveToNext());
                    String[] asArray = new String[houseTypeList.size()];
                    houseTypeList.toArray(asArray);
                    houseTypeAdapter = new ArrayAdapter<String>(getContext(), R.layout.dropdown_menu, asArray);
                    houseTypeText.setAdapter(houseTypeAdapter);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}