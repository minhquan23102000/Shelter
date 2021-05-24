package com.example.shelter;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.shelter.Data.SessionManager;
import com.example.shelter.Data.ShelterDBContract.RatingEntry;
import com.example.shelter.Data.ShelterDBContract.HouseTypeEntry;
import com.example.shelter.Data.ShelterDBContract.HouseEntry;
import com.example.shelter.Data.ShelterDBHelper;
import com.example.shelter.Network.ImageRequester;
import com.example.shelter.adapter.ImageSliderAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.StorageReference;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.List;

public class HouseDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    static final public String TAG = HouseDetailFragment.class.getSimpleName();
    private static final int HOUSE_DETAIL_LOADER = 901;
    private static final int IS_FAVOURITE_LOADER = 121;
    private static final int CHECK_CONTACT_SENT = 131;

    //Data needed for this fragment
    private Bundle bundle;
    private Uri mHouseUri;
    private boolean moreInfoTVIsExpanded = false;
    private boolean isFavourite;
    private boolean contactSent;

    //All text view in this fragment
    private TextView houseNameTV;
    private TextView houseTypeTV;
    private TextView rentCostTV;
    private TextView houseAddressTV;
    private TextView houseAddressLabelTV;
    private TextView nearPointDistanceTV;
    private TextView salePriceTV;
    private TextView moreInfoTV;
    private TextView moreInfoTVLabel;
    private TextView houseAreaTV;
    private ImageButton isFavouriteButton;
    private MaterialButton sendContactButton;

    private SliderView sliderView;
    private ImageSliderAdapter imageSliderAdapter;
    private ImageRequester imageRequester;

    private Cursor cursorIsFavourite = null;
    private SessionManager sessionManager;


    //House location
    private LatLng houseLatLng;

    @Override
    public void onCreate(@Nullable  Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageRequester = new ImageRequester(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.house_detail_fragment, container, false);
        bundle = this.getArguments();
        mHouseUri = Uri.parse(bundle.getString("houseUri", null));
        Log.d(TAG, "House Uri: " + bundle.getString("houseUri"));

        //Find all needed view to display data
        houseNameTV = (TextView) view.findViewById(R.id.house_name);
        houseTypeTV = (TextView) view.findViewById(R.id.house_type);
        rentCostTV = (TextView) view.findViewById(R.id.rent_price);
        houseAddressTV = (TextView) view.findViewById(R.id.house_address);
        houseAddressLabelTV = view.findViewById(R.id.house_address_label);
        nearPointDistanceTV = (TextView) view.findViewById(R.id.near_point_distance);
        salePriceTV = (TextView) view.findViewById(R.id.sale_price);
        sendContactButton = (MaterialButton) view.findViewById(R.id.send_contact_button);
        moreInfoTV = (TextView) view.findViewById(R.id.more_detail);
        moreInfoTVLabel = (TextView) view.findViewById(R.id.mote_detail_label);
        houseAreaTV = (TextView) view.findViewById(R.id.house_area);

        //Button
        sendContactButton = view.findViewById(R.id.send_contact_button);
        sendContactButton.setText(R.string.send_contact);
        contactSent = false;

        isFavouriteButton = (ImageButton) view.findViewById(R.id.favourite);
        isFavouriteButton.setTag(R.drawable.outline_favorite_border_24);
        isFavouriteButton.setImageResource(R.drawable.outline_favorite_border_24);
        isFavourite = false;


        sessionManager = new SessionManager(getContext());
        Log.d(TAG, "onCreate: didUserLogin() " + sessionManager.didUserLogin());
        Log.d(TAG, "onCreate: userUri " + sessionManager.getUserUri());
        //Set image slider
        sliderView = view.findViewById(R.id.image_slider);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);


        //Set on click listener for more info TV, for expandable
        moreInfoTVLabel.setOnClickListener(v -> {
            if (!moreInfoTVIsExpanded) {
                moreInfoTV.setMaxLines(40);
                moreInfoTVLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.outline_expand_less_24, 0);
                moreInfoTVIsExpanded = true;
            } else {
                moreInfoTV.setMaxLines(0);
                moreInfoTVLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.outline_expand_more_24, 0);
                moreInfoTVIsExpanded = false;
            }

        });

        //Set on click listener for address text view to display current location of the house on google map
        houseAddressLabelTV.setOnClickListener(v -> {
            Fragment mapFragment = new MapsFragment();
            Bundle deliver = new Bundle();
            deliver.putDouble("pointLatitude", houseLatLng.latitude);
            deliver.putDouble("pointLongitude", houseLatLng.longitude);
            mapFragment.setArguments(deliver);
            ((NavigationHost) getActivity()).navigateTo(mapFragment, true);
        });

        isFavouriteButton.setOnClickListener(v -> {
            if (isFavourite) {
                isFavourite = false;
                isFavouriteButton.setImageResource(R.drawable.outline_favorite_border_24);
            } else {
                isFavourite = true;
                isFavouriteButton.setImageResource(R.drawable.outline_favorite_24);
            }
        });
        sendContactButton.setOnClickListener(v -> {
            if (!contactSent) {
                Long userId = ContentUris.parseId(sessionManager.getUserUri());
                Long houseId = ContentUris.parseId(mHouseUri);
                ContentValues values = new ContentValues();
                values.put(RatingEntry.COLUMN_USER_ID, userId);
                values.put(RatingEntry.COLUMN_HOUSE_ID, houseId);
                values.put(RatingEntry.COLUMN_STARS, RatingEntry.SEND_CONTACT);
                values.put("topic_select", "1");
                getContext().getContentResolver().insert(RatingEntry.CONTENT_URI, values);
                sendContactButton.setText(R.string.contact_sent);
                sendContactButton.setBackgroundColor(getResources().getColor(R.color.colorAccent, null));
                contactSent = true;
                Toast.makeText(getContext(), R.string.send_contact_buton_press, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), R.string.contact_sent_press, Toast.LENGTH_SHORT).show();
            }

        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Kick off the loader
        LoaderManager.getInstance(this).initLoader(HOUSE_DETAIL_LOADER, null, this);
        LoaderManager.getInstance(this).initLoader(IS_FAVOURITE_LOADER, null, this);
        LoaderManager.getInstance(this).initLoader(CHECK_CONTACT_SENT, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection;
        CursorLoader cursorLoader = null;
        String selection;
        String selectionArgs[];
        long userId = ContentUris.parseId(sessionManager.getUserUri());
        long houseId = ContentUris.parseId(mHouseUri);
        switch (id) {
            case HOUSE_DETAIL_LOADER:
                cursorLoader = new CursorLoader(getContext(),   // Parent activity context
                        mHouseUri,   // Provider content URI to query
                        null,             // Columns to include in the resulting Cursor
                        null,                   // No selection clause
                        null,                   // No selection arguments
                        null);// Default sort order
                break;
            case IS_FAVOURITE_LOADER:
                projection = new String[]{
                        RatingEntry._ID,
                        RatingEntry.COLUMN_HOUSE_ID,
                        RatingEntry.COLUMN_USER_ID,
                        RatingEntry.COLUMN_STARS
                };
                selection = "user_id = ? AND stars = ? AND house_id = ?";
                selectionArgs = new String[]{Long.toString(userId), RatingEntry.FAVOURITE.toString(), Long.toString(houseId)};
                cursorLoader = new CursorLoader(getContext(),   // Parent activity context
                        RatingEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selection,                   // No selection clause
                        selectionArgs,                   // No selection arguments
                        null);// Default sort order
                break;
            case CHECK_CONTACT_SENT:
                projection = new String[]{
                        RatingEntry._ID,
                        RatingEntry.COLUMN_HOUSE_ID,
                        RatingEntry.COLUMN_USER_ID,
                        RatingEntry.COLUMN_STARS
                };
                selection = "user_id = ? AND stars = ? AND house_id = ?";
                selection += " " + HouseEntry.COLUMN_HOUSE_STATE + " == " + HouseEntry.STATE_VISIBLE;
                selectionArgs = new String[]{Long.toString(userId), RatingEntry.SEND_CONTACT.toString(), Long.toString(houseId)};
                cursorLoader = new CursorLoader(getContext(),   // Parent activity context
                        RatingEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selection,                   // No selection clause
                        selectionArgs,                   // No selection arguments
                        null);// Default sort order
                break;

        }


        // This loader will execute the ContentProvider's query method on a background thread
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)

        if (data.moveToFirst()) {
            switch (loader.getId()) {
                case HOUSE_DETAIL_LOADER:
                    String houseName = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_NAME));
                    String houseAddress = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_ADDRESS));
                    String houseRentCost = ShelterDBHelper.formatPrice(data.getFloat(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_RENT_COST)), getContext());
                    String houseSalePrice = ShelterDBHelper.formatPrice(data.getFloat(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_SALE_PRICE)), getContext());
                    String houseArea = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_AREA));
                    String houseBedRooms = getString(R.string.number_of_bed_rooms);
                    houseBedRooms += data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_BED_ROOMS));
                    String houseBathRooms = getString(R.string.num_of_bath_rooms);
                    houseBathRooms += data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_BATH_ROOMS));
                    String houseFloors = getString(R.string.num_of_floors_floor);
                    houseFloors += data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_FLOORS));
                    String houseYearBuilt = getString(R.string._year_built);
                    houseYearBuilt += data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_YEAR_BUILT));
                    String houseYardSize = getString(R.string._yard_size);
                    houseYardSize += data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_YARD_SIZE)) + " m2";
                    String housePlace = getString(R.string._place);
                    housePlace += HouseEntry.getPlaceName(data.getInt(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_PLACE)));

                    //Query house type name
                    Integer houseType = data.getInt(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_TYPE_ID));
                    String houseTypeName;
                    String[] projection = {HouseTypeEntry._ID, HouseTypeEntry.COLUMN_HOUSE_TYPE_NAME};
                    Cursor houseTypeCursor = getContext().getContentResolver().query(ContentUris.withAppendedId(HouseTypeEntry.CONTENT_URI, houseType.longValue()),
                            projection,
                            null,
                            null,
                            null);
                    // Proceed with moving to the first row of the cursor and reading data from it
                    // (This should be the only row in the cursor)
                    if (houseTypeCursor.moveToFirst()) {
                        houseTypeName = houseTypeCursor.getString(houseTypeCursor.getColumnIndex(HouseTypeEntry.COLUMN_HOUSE_TYPE_NAME));
                    } else {
                        houseTypeName = "";
                    }
                    //close the cursor
                    houseTypeCursor.close();

                    //format Price
                    if (!houseRentCost.equals(getString(R.string.sale_only))) {
                        if (houseType == 5 || houseType == 8) {
                            houseRentCost += "/Đêm";
                        } else {
                            houseRentCost += "/Tháng";
                        }
                    }
                    if (houseSalePrice.equals(getContext().getString(R.string.sale_only))) {
                        houseSalePrice = getString(R.string.rent_only);
                    }

                    //Update correspond text view
                    houseNameTV.setText(houseName);
                    houseAddressTV.setText(houseAddress);
                    rentCostTV.setText(houseRentCost);
                    salePriceTV.setText(houseSalePrice);
                    houseTypeTV.setText(houseTypeName);
                    houseAreaTV.setText(getString(R.string.area) + " " + houseArea + " m2");

                    String moreInfo = houseBedRooms + houseBathRooms + houseFloors +
                            houseYearBuilt + houseYardSize + housePlace;
                    moreInfoTV.setText(moreInfo);


                    //Calculate and display distance between the pointer location and the house
                    float distance = ShelterDBHelper.getDistanceFromHouseToThePointer(sessionManager, data);
                    if (distance != -1) {
                        String formatDistanceString = "From";
                        if (sessionManager.haveWishPointData()) {
                            formatDistanceString += " " + sessionManager.getWishfulPointName();
                        } else {
                            formatDistanceString += " you";
                        }
                        formatDistanceString += ": " + distance + " km";
                        nearPointDistanceTV.setText(formatDistanceString);
                    } else {
                        //Request permission to access user location
                        if (ActivityCompat.checkSelfPermission(
                                getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.REQUEST_LOCATION);
                        } else {
                            Toast.makeText(getContext(), getString(R.string.unabale_to_locate_user_location), Toast.LENGTH_LONG).show();
                        }
                        nearPointDistanceTV.setText(R.string.unlocatable);
                    }
                    //Get houseLatLng for the deliver to locate house on google map fragment
                    houseLatLng = new LatLng (data.getDouble(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_LATITUDE)),
                            data.getDouble(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_LONGITUDE)));

                    //Init image adapter
                    imageSliderAdapter = new ImageSliderAdapter(getContext());

                    //Set adapter to slider view
                    sliderView.setSliderAdapter(imageSliderAdapter);


                    //Load list house's images
                    imageRequester.loadListRefToSliderAdapter(data.getInt(data.getColumnIndex(HouseEntry._ID)),
                            HouseEntry.TABLE_NAME,
                            imageSliderAdapter,
                            sliderView);



                    break;
                case IS_FAVOURITE_LOADER:
                    isFavourite = true;
                    isFavouriteButton.setImageResource(R.drawable.outline_favorite_24);
                    cursorIsFavourite = data;
                    break;
                case CHECK_CONTACT_SENT:
                    contactSent = true;
                    sendContactButton.setBackgroundColor(getResources().getColor(R.color.colorAccent, null));
                    sendContactButton.setText(R.string.contact_sent);
                    break;
            }
        }


    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public void onDestroyView() {
        Long userID = ContentUris.parseId(sessionManager.getUserUri());
        Long houseID = ContentUris.parseId(mHouseUri);
        if (isFavourite && cursorIsFavourite == null) {
            ContentValues values = new ContentValues();
            values.put(RatingEntry.COLUMN_USER_ID, userID);
            values.put(RatingEntry.COLUMN_HOUSE_ID, houseID);
            values.put(RatingEntry.COLUMN_STARS, RatingEntry.FAVOURITE);
            getContext().getContentResolver().insert(RatingEntry.CONTENT_URI, values);
        } else if (!isFavourite && cursorIsFavourite != null) {
            String selection = "user_id = ? AND stars = ? AND house_id = ?";
            String[] selectionArgs = {userID.toString(), RatingEntry.FAVOURITE.toString(), houseID.toString()};
            getContext().getContentResolver().delete(RatingEntry.CONTENT_URI, selection, selectionArgs);
        }
        super.onDestroyView();
    }


}
