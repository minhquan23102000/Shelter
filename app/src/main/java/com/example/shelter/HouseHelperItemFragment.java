package com.example.shelter;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.shelter.data.SessionManager;
import com.example.shelter.data.ShelterDBContract.HouseTypeEntry;
import com.example.shelter.data.ShelterDBContract.RatingEntry;

import com.example.shelter.data.ShelterDBHelper;
import com.example.shelter.network.ImageNameGenerator;
import com.example.shelter.network.ImageRequester;
import com.example.shelter.data.ShelterDBContract.HouseEntry;
import com.example.shelter.adapter.ImageSliderAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.StorageReference;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;


public class HouseHelperItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = HouseHelperItemFragment.class.getName();
    private static final int HOUSE_DATA_LOADER = 349;
    private static final String KEY_ARGUMENT_1 = "houseId";

    //Context and Activity
    private Context mContext;
    private Activity mActivity;
    //Session manager
    private SessionManager sessionManager;

    //Init intent launcher
    private ActivityResultLauncher<Intent> someActivityResultLauncher;


    //All view needed in layout
    //Views
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
    private TextInputLayout housePointInputLayout;
    private TextInputEditText housePointEditText;
    private TextInputEditText houseNameEditText;
    private TextInputLayout houseNameInputLayout;
    private TextInputEditText yardSizeEditText;
    private TextInputLayout yardSizeInputLayout;
    private TextInputLayout moreInfoInputLayout;
    private TextInputEditText moreInfoEditText;

    private ImageView houseCloseImage;

    private AutoCompleteTextView placeText;
    private AutoCompleteTextView houseTypeText;

    private ArrayAdapter<String> placeAdapter;
    private ArrayAdapter<String> houseTypeAdapter;

    //Image Adapter
    private ImageSliderAdapter sliderAdapter;
    //Slider View
    private SliderView sliderView;


    //Handle on CLoud
    private ImageRequester imageRequester;
    private ImageNameGenerator imageNameGenerator;
    private List<StorageReference> tempRefs;

    //Value to confirm, check to control the flow of data.
    private int tempHouseId;
    private boolean clickUpdateAndDataIsValid = false;
    private boolean isNewHouse = false;
    private double houseTempLat;
    private double houseTempLng;
    private boolean toMapFragment = false;
    private boolean toGetImage = false;
    private boolean firstLoad = true;
    private int houseState;

    static public Fragment NewInstance(int houseId) {
        Fragment newFragment = new HouseHelperItemFragment();
        Bundle deliver = new Bundle();
        deliver.putInt(KEY_ARGUMENT_1, houseId);
        newFragment.setArguments(deliver);
        return newFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mActivity = getActivity();
        mContext = getContext();

        //Get argument
        if (getArguments() != null) {
            tempHouseId = getArguments().getInt(KEY_ARGUMENT_1, -1);
        }

        //Init image requester
        imageRequester = new ImageRequester(mActivity);
        //Init image name handler
        imageNameGenerator = new ImageNameGenerator(mActivity);

        //Init adapter
        sliderAdapter = new ImageSliderAdapter(mActivity, true);
        tempRefs = new ArrayList<>();

        // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }

                        ClipData clipDataUri = data.getClipData();
                        List<Uri> uriList = new ArrayList<>();


                        if (clipDataUri != null) {
                            //If user pick multiple images
                            for (int i = 0; i < clipDataUri.getItemCount(); i++) {
                                uriList.add(clipDataUri.getItemAt(i).getUri());
                            }
                        } else {
                            //Else user add one image
                            uriList.add(data.getData());
                        }

                        //Upload to cloud
                        List<StorageReference> newRef = imageRequester.updateListUriFileToCloud(tempHouseId, HouseEntry.TABLE_NAME, uriList);
                        tempRefs.addAll(newRef);

                        //Update on UI
                        sliderAdapter.addMultiItems(newRef);
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_house_helper_item, container, false);
        setUpToolbar(view);

        //Init session manager
        sessionManager = new SessionManager(mContext);


        //Spinner
        placeText = view.findViewById(R.id.place_text);
        houseTypeText = view.findViewById(R.id.house_type_text);

        moreInfoInputLayout = view.findViewById(R.id.more_info_text_input);
        moreInfoEditText = view.findViewById(R.id.more_info_edit_text);
        moreInfoEditText.setMovementMethod(new ScrollingMovementMethod());

        //First load is to check, if this fragment is first created and not by resume
        if (firstLoad) {
            //If temp house != -1 we kick the loader to fill data in UI. Else we insert a new house, so the data in UI will be blank.

            if (tempHouseId != -1) {
                LoaderManager.getInstance(HouseHelperItemFragment.this).initLoader(HOUSE_DATA_LOADER, null, HouseHelperItemFragment.this);

            } else {
                //Set temp house location
                try {
                    houseTempLat = MainActivity.getUserLocation().getLatitude();
                    houseTempLng = MainActivity.getUserLocation().getLongitude();
                } catch (NullPointerException e) {
                    Toast.makeText(mContext, R.string.set_land_mark_tower_default_location, Toast.LENGTH_LONG).show();
                    houseTempLat = MapsFragment.LAND_MARK_TOWER.latitude;
                    houseTempLng = MapsFragment.LAND_MARK_TOWER.longitude;
                }

                //We create a new temp house in database for get new id to use it for declare new image on cloud (Declare unique image need an unique id)
                //We put dummy data to the database, If later the user do not click update or the data in UI is not valid, it will be deleted
                ContentValues values = new ContentValues();
                values.put(HouseEntry.COLUMN_HOUSE_NAME, "temp house");
                values.put(HouseEntry.COLUMN_HOUSE_AREA, 0);
                values.put(HouseEntry.COLUMN_HOUSE_LATITUDE, houseTempLat);
                values.put(HouseEntry.COLUMN_HOUSE_LONGITUDE, houseTempLng);
                values.put(HouseEntry.COLUMN_HOUSE_ADDRESS, "temp house address");
                values.put(HouseEntry.COLUMN_HOUSE_FLOORS, 0);
                values.put(HouseEntry.COLUMN_HOUSE_PLACE, 1);
                values.put(HouseEntry.COLUMN_HOUSE_TYPE_ID, 1);
                Uri newHouseUri = mContext.getContentResolver().insert(HouseEntry.CONTENT_URI, values);
                tempHouseId = (int) ContentUris.parseId(newHouseUri);
                Log.d(TAG, "onCreateView: " + tempHouseId);

                //Set it to true to recognize its later to delete this house when update failed
                isNewHouse = true;
                firstLoad = false;

                //Set placeholder for moreInfoText
                String moreInfoPlaceHolder = getString(R.string.call_me_at_);
                moreInfoPlaceHolder += " " + sessionManager.getUserPhone();
                moreInfoEditText.setText(moreInfoPlaceHolder);
            }
        }


        //Init image slider
        sliderView = view.findViewById(R.id.image_slider);
        sliderView.setSliderAdapter(sliderAdapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setAutoCycle(true);

        //Init add Image Button, when on click, navigate to user gallery to pick images
        //Button
        ImageButton addImageBT = view.findViewById(R.id.add_image);
        addImageBT.setOnClickListener(v -> {
            toGetImage = true;
            if (ActivityCompat.checkSelfPermission(
                    mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.REQUEST_READ_EXTERNAl_FILE);
            } else {
                Intent pickImage = new Intent();
                pickImage.setType("image/*");
                pickImage.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                pickImage.setAction(Intent.ACTION_GET_CONTENT);
                someActivityResultLauncher.launch(Intent.createChooser(pickImage, "Pick yours images, maximum 15 items"));
            }
        });

        //Init update button, when on click it will check all data is valid and destroy this layout.
        // The update statement will be triggered onDestroyView Event.
        MaterialButton updateButton = view.findViewById(R.id.next_button);
        updateButton.setOnClickListener(v -> checkDataValid());


        //Init redo button to redo change
        MaterialButton redoButton = view.findViewById(R.id.back_button);
        redoButton.setOnClickListener(v -> {
            clickUpdateAndDataIsValid = false;
            if (!isNewHouse) {
                Fragment fragment = new HouseHelperItemFragment();
                Bundle saveState = new Bundle();
                saveState.putInt(KEY_ARGUMENT_1, tempHouseId);
                fragment.setArguments(saveState);


                //Reload fragment
                ((MainActivity) mActivity).navigateTo(fragment, false);
            } else {
                getParentFragmentManager().popBackStack();
            }

        });

        //Find all needed view, prepare data for layout
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
        rentCostInputLayout = view.findViewById(R.id.rent_cost_text_input);

        salePriceEditText = view.findViewById(R.id.sale_price_edit_text);
        salePriceInputLayout = view.findViewById(R.id.sale_price_text_input);

        housePointEditText = view.findViewById(R.id.house_point_edit_text);
        housePointInputLayout = view.findViewById(R.id.house_point_text_input);
        housePointEditText.setMovementMethod(new ScrollingMovementMethod());

        houseNameEditText = view.findViewById(R.id.house_name_edit_text);
        houseNameInputLayout = view.findViewById(R.id.house_name_text_input);

        yardSizeEditText = view.findViewById(R.id.yard_size_edit_text);
        yardSizeInputLayout = view.findViewById(R.id.yard_size_text_input);


        houseCloseImage = view.findViewById(R.id.house_close);

        //Set on icon click listener for housePointEditText, to navigate to map fragment and get house location
        housePointInputLayout.setStartIconOnClickListener(v -> {
            toMapFragment = true;
            Fragment mapFragment = MapsFragment.NewInstance(TAG, houseTempLat, houseTempLng);
            ((MainActivity) mActivity).navigateTo(mapFragment, true);
        });

        //Set on key listener to clear error
        //On Key listener
        View.OnKeyListener onKeyListener = (v, keyCode, event) -> {
            if (v.getId() == R.id.house_name_edit_text) {
                houseNameInputLayout.setError(null);
            } else if (v.getId() == R.id.house_point_edit_text) {
                housePointInputLayout.setError(null);
            } else if (v.getId() == R.id.area_edit_text) {
                areaInputLayout.setError(null);
            } else if (v.getId() == R.id.floor_edit_text) {
                floorInputLayout.setError(null);
            } else if (v.getId() == R.id.bedrooms_edit_text) {
                bedRoomsInputLayout.setError(null);
            } else if (v.getId() == R.id.bathrooms_edit_text) {
                bathRoomsInputLayout.setError(null);
            } else if (v.getId() == R.id.rent_cost_edit_text) {
                rentCostInputLayout.setError(null);
            } else if (v.getId() == R.id.sale_price_edit_text) {
                salePriceInputLayout.setError(null);
            } else if (v.getId() == R.id.yearbuilt_edit_text) {
                yearBuiltInputLayout.setError(null);
            }
            return false;
        };

        housePointEditText.setOnKeyListener(onKeyListener);
        houseNameEditText.setOnKeyListener(onKeyListener);
        areaEditText.setOnKeyListener(onKeyListener);
        floorEditText.setOnKeyListener(onKeyListener);
        bedRoomsEditText.setOnKeyListener(onKeyListener);
        bathRoomsEditText.setOnKeyListener(onKeyListener);
        salePriceEditText.setOnKeyListener(onKeyListener);
        rentCostEditText.setOnKeyListener(onKeyListener);
        yearBuiltEditText.setOnKeyListener(onKeyListener);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sessionManager.getHousePointAddress() != null) {
            housePointEditText.setText(sessionManager.getHousePointAddress());
            houseTempLat = sessionManager.getHousePointLatLng().latitude;
            houseTempLng = sessionManager.getHousePointLatLng().longitude;
        }


        //Init place adapter
        placeAdapter = new ArrayAdapter<>(mActivity, R.layout.dropdown_menu, HouseEntry.POSSIBLE_VALUE_PLACES);
        //House type adapter
        List<String> houseTypeList = new ArrayList<>();
        Cursor houseTypeCursor = mContext.getContentResolver().query(HouseTypeEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (houseTypeCursor.moveToFirst()) {
            do {
                houseTypeList.add(houseTypeCursor.getString(houseTypeCursor.getColumnIndex(HouseTypeEntry.COLUMN_HOUSE_TYPE_NAME)));
            } while (houseTypeCursor.moveToNext());

            String[] asArray = new String[houseTypeList.size()];
            houseTypeList.toArray(asArray);
            houseTypeAdapter = new ArrayAdapter<>(mActivity, R.layout.dropdown_menu, asArray);

        }
        houseTypeCursor.close();

        //Set adapter for the spinner
        placeText.setAdapter(placeAdapter);
        houseTypeText.setAdapter(houseTypeAdapter);
    }

    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.house_helper_tool_bar);
        AppCompatActivity activity = (AppCompatActivity) mActivity;
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }


        toolbar.inflateMenu(R.menu.house_helper_toolbar_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                if (houseState == HouseEntry.STATE_VISIBLE) {
                    new MaterialAlertDialogBuilder(mContext)
                            .setMessage(R.string.close_house_dialog)
                            .setNeutralButton(R.string.cancel, (dialog, which) -> {

                            })
                            .setPositiveButton(R.string.yes, (dialog, which) -> {
                                ShelterDBHelper.updateHouseState(tempHouseId, HouseEntry.STATE_ABANDONED, mContext);
                                houseCloseImage.setVisibility(View.VISIBLE);
                                houseState = HouseEntry.STATE_ABANDONED;
                            })
                            .show();


                } else {
                    Toast.makeText(mContext, R.string.house_already_closed, Toast.LENGTH_SHORT).show();
                }
            } else if (item.getItemId() == R.id.action_open) {
                if (houseState == HouseEntry.STATE_ABANDONED) {
                    new MaterialAlertDialogBuilder(mContext)
                            .setMessage(R.string.open_house_dialog)
                            .setNeutralButton(R.string.cancel, (dialog, which) -> {

                            })
                            .setPositiveButton(R.string.yes, (dialog, which) -> {
                                ShelterDBHelper.updateHouseState(tempHouseId, HouseEntry.STATE_VISIBLE, mContext);
                                houseCloseImage.setVisibility(View.GONE);
                                houseState = HouseEntry.STATE_VISIBLE;
                            })
                            .show();
                } else {
                    Toast.makeText(mContext, R.string.house_already_opened, Toast.LENGTH_SHORT).show();
                }

            }
            return true;
        });


    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menu.clear();
        menuInflater.inflate(R.menu.house_helper_toolbar_menu, menu);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(mContext,
                ContentUris.withAppendedId(HouseEntry.CONTENT_URI, tempHouseId),
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            String houseName = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_NAME));
            String houseAddress = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_ADDRESS));
            String houseRentCost = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_RENT_COST));
            String houseSalePrice = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_SALE_PRICE));
            String houseArea = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_AREA));
            String houseBedRooms = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_BED_ROOMS));
            String houseBathRooms = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_BATH_ROOMS));
            String houseFloors = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_FLOORS));
            String houseYearBuilt = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_YEAR_BUILT));
            String houseYardSize = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_YARD_SIZE));
            String moreInfo = data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_CONTENT));
            int housePlace = data.getInt(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_PLACE));
            int houseType = data.getInt(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_TYPE_ID));
            houseState = data.getInt(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_STATE));

            houseTempLat = data.getDouble(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_LATITUDE));
            houseTempLng = data.getDouble(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_LONGITUDE));

            houseNameEditText.setText(houseName);
            housePointEditText.setText(houseAddress);
            rentCostEditText.setText(houseRentCost);
            salePriceEditText.setText(houseSalePrice);
            areaEditText.setText(houseArea);
            bedRoomsEditText.setText(houseBedRooms);
            bathRoomsEditText.setText(houseBathRooms);
            floorEditText.setText(houseFloors);
            yearBuiltEditText.setText(houseYearBuilt);
            yardSizeEditText.setText(houseYardSize);
            houseTypeText.setText(houseTypeAdapter.getItem(houseType - 1), false);
            placeText.setText(HouseEntry.POSSIBLE_VALUE_PLACES[housePlace], false);
            if (houseState == HouseEntry.STATE_VISIBLE) {
                houseCloseImage.setVisibility(View.GONE);
            } else {
                houseCloseImage.setVisibility(View.VISIBLE);
            }

            Log.d(TAG, "onLoadFinished: " + "load again");
            if (firstLoad) {
                imageRequester.loadListRefToSliderAdapter(tempHouseId, HouseEntry.TABLE_NAME, sliderAdapter, sliderView);
                moreInfoEditText.setText(moreInfo);
                firstLoad = false;
            }
        }


    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public void onDestroyView() {
        if (!clickUpdateAndDataIsValid && (!toGetImage && !toMapFragment)) {
            //Update failed
            if (isNewHouse) {
                mContext.getContentResolver().delete(ContentUris.withAppendedId(HouseEntry.CONTENT_URI, tempHouseId), null, null);
            }
            Log.d(TAG, "onDestroyView: update failed " + toMapFragment + toGetImage);
            //Update failed delete all the ref user inserted in
            imageRequester.deleteAListOfRef(tempRefs);
        }
        if (!toGetImage) {
            //Clear house point data, not need to deliver it again
            sessionManager.clearHousePointData();
        }


        super.onDestroyView();
    }


    @Override
    public void onStop() {
        //Update failed. This trigger when User quit the app when on map fragment or on Get image Intent, But data is not valid or not click update.
        if (!clickUpdateAndDataIsValid && (!toGetImage && !toMapFragment)) {
            //Update failed
            if (isNewHouse) {
                mContext.getContentResolver().delete(ContentUris.withAppendedId(HouseEntry.CONTENT_URI, tempHouseId), null, null);
            }

            //Update failed delete all the ref user inserted in
            imageRequester.deleteAListOfRef(tempRefs);
        }

        super.onStop();
    }

    @Override
    public void onPause() {
        //Update failed. This trigger when User quit the app when on map fragment or on Get image Intent, But data is not valid or not click update.
        if (!clickUpdateAndDataIsValid && (!toGetImage && !toMapFragment)) {
            //Update failed
            if (isNewHouse) {
                mContext.getContentResolver().delete(ContentUris.withAppendedId(HouseEntry.CONTENT_URI, tempHouseId), null, null);
            }

            //Update failed delete all the ref user inserted in
            imageRequester.deleteAListOfRef(tempRefs);
        }
        super.onPause();
    }

    private void update() {
        //All value from UI
        String houseName = houseNameEditText.getText().toString().trim();
        float houseArea = Float.parseFloat(areaEditText.getText().toString());
        int houseFloor = Integer.parseInt(floorEditText.getText().toString());
        int bedrooms = Integer.parseInt(bedRoomsEditText.getText().toString());
        int bathrooms = Integer.parseInt(bathRoomsEditText.getText().toString());
        float yardSize;
        try {
            yardSize = Float.parseFloat(yardSizeEditText.getText().toString());
        } catch (NumberFormatException e) {
            yardSize = 0;
        }
        int yearBuilt;
        try {
            yearBuilt = Integer.parseInt(yearBuiltEditText.getText().toString());
        } catch (NumberFormatException e) {
            yearBuilt = 0;
        }

        float salePrice;
        try {
            salePrice = Float.parseFloat(salePriceEditText.getText().toString());
        } catch (NumberFormatException e) {
            salePrice = -1;
        }

        float rentCost;
        try {
            rentCost = Float.parseFloat(rentCostEditText.getText().toString());
        } catch (NumberFormatException e) {
            rentCost = -1;
        }


        int houseType = houseTypeAdapter.getPosition(houseTypeText.getText().toString().trim()) + 1;
        // +1 because array start from zero,
        // but in database index start from one

        int place = placeAdapter.getPosition(placeText.getText().toString().trim());


        LatLng housePoint = sessionManager.getHousePointLatLng();
        double pointLat = housePoint.latitude;
        double pointLng = housePoint.longitude;
        String houseAddress = housePointEditText.getText().toString().trim();

        String moreInfo = null;
        if (moreInfoEditText.getText() != null) {
            moreInfo = moreInfoEditText.getText().toString().trim();
        }


        //Put values to the rating deliver, to know who is the owner of this house
        if (isNewHouse) {
            ContentValues ratingValues = new ContentValues();
            ratingValues.put(RatingEntry.COLUMN_HOUSE_ID, tempHouseId);
            ratingValues.put(RatingEntry.COLUMN_USER_ID, ContentUris.parseId(sessionManager.getUserUri()));
            ratingValues.put(RatingEntry.COLUMN_STARS, RatingEntry.HOUSE_OWNER);
            mContext.getContentResolver().insert(RatingEntry.CONTENT_URI, ratingValues);
            ratingValues.clear();
        }

        //Put values to the house deliver
        ContentValues houseValues = new ContentValues();
        houseValues.put(HouseEntry.COLUMN_HOUSE_NAME, houseName);
        houseValues.put(HouseEntry.COLUMN_HOUSE_AREA, houseArea);
        houseValues.put(HouseEntry.COLUMN_HOUSE_FLOORS, houseFloor);
        houseValues.put(HouseEntry.COLUMN_HOUSE_BED_ROOMS, bedrooms);
        houseValues.put(HouseEntry.COLUMN_HOUSE_BATH_ROOMS, bathrooms);
        houseValues.put(HouseEntry.COLUMN_HOUSE_YARD_SIZE, yardSize);
        if (yearBuilt != 0) {
            houseValues.put(HouseEntry.COLUMN_HOUSE_YEAR_BUILT, yearBuilt);
        }

        if (salePrice != -1) {
            houseValues.put(HouseEntry.COLUMN_HOUSE_SALE_PRICE, salePrice);
        }
        if (rentCost != -1) {
            houseValues.put(HouseEntry.COLUMN_HOUSE_RENT_COST, rentCost);
        }

        houseValues.put(HouseEntry.COLUMN_HOUSE_LATITUDE, pointLat);
        houseValues.put(HouseEntry.COLUMN_HOUSE_LONGITUDE, pointLng);
        houseValues.put(HouseEntry.COLUMN_HOUSE_ADDRESS, houseAddress);
        houseValues.put(HouseEntry.COLUMN_HOUSE_TYPE_ID, houseType);
        houseValues.put(HouseEntry.COLUMN_HOUSE_PLACE, place);
        houseValues.put(HouseEntry.COLUMN_HOUSE_CONTENT, moreInfo);

        Log.d(TAG, "update: house id " + tempHouseId);
        //Time to call the provider to transport to the deliver to the mother database
        mContext.getContentResolver().update(ContentUris.withAppendedId(HouseEntry.CONTENT_URI, tempHouseId),
                houseValues,
                null,
                null);

        //If this is new house we create a new collection on cloud to store image name.
        if (isNewHouse) {
            imageNameGenerator.generateCollectionOfImageNamesToFireStore(HouseEntry.TABLE_NAME, tempHouseId, tempRefs);
        } else {
            //If image data has change, we renew this old collection on cloud, and delete abandon Ref. Else do nothing
            if (sliderAdapter.isItemsChange()) {
                imageNameGenerator.renewCollection(HouseEntry.TABLE_NAME, tempHouseId, sliderAdapter.getSliderItems());

                //Delete abandon Refs
                List<StorageReference> abandonRefs = sliderAdapter.getAbandonRefs();
                if (!abandonRefs.isEmpty()) {
                    imageRequester.deleteAListOfRef(abandonRefs);
                }
            }
        }
        clickUpdateAndDataIsValid = false;
        Toast.makeText(mContext, "Update successfully", Toast.LENGTH_SHORT).show();
    }

    private void checkDataValid() {



        clickUpdateAndDataIsValid = false;
        boolean flag = true;

        if (sliderAdapter.getCount() == 0) {
            Toast.makeText(mContext, "Please insert some image for this house", Toast.LENGTH_SHORT).show();
            flag = false;
        }

        if (houseNameEditText.getText() == null || houseNameEditText.getText().toString().equals("")) {
            houseNameInputLayout.setError(getString(R.string.house_name_null_check));
            flag = false;
        }

        if (areaEditText.getText() == null || areaEditText.getText().toString().equals("")) {
            areaInputLayout.setError(getString(R.string.house_area_check));
            flag = false;
        }

        if (floorEditText.getText() == null || floorEditText.getText().toString().equals("")) {
            floorInputLayout.setError(getString(R.string.house_floor_check));
            flag = false;
        }

        if (bedRoomsEditText.getText() == null || bedRoomsEditText.getText().toString().equals("")) {
            bedRoomsInputLayout.setError(getString(R.string.bed_rooms_house_check));
            flag = false;
        }

        if (bathRoomsEditText.getText() == null || bathRoomsEditText.getText().toString().equals("")) {
            bathRoomsInputLayout.setError(getString(R.string.bath_rooms_house_check));
            flag = false;
        }

        if (housePointEditText.getText() == null || housePointEditText.getText().toString().equals("")) {
            housePointInputLayout.setError(getString(R.string.address_house_check));
            flag = false;
        }

        if ((salePriceEditText.getText() == null || salePriceEditText.getText().toString().equals(""))
                && (rentCostEditText.getText() == null || rentCostEditText.getText().toString().equals(""))) {
            salePriceInputLayout.setError(getString(R.string.price_house_check));
            flag = false;
        }


        int yearBuilt;
        try {
            yearBuilt = Integer.parseInt(yearBuiltEditText.getText().toString());
        } catch (NumberFormatException e) {
            yearBuilt = 0;
        }

        if ((yearBuilt < 1960 || yearBuilt > YearMonth.now().getYear()) && yearBuilt != 0) {
            yearBuiltInputLayout.setError(getString(R.string.check_year_built_house) + YearMonth.now().getYear());
        }

        if (flag) {
            clickUpdateAndDataIsValid = true;

            new MaterialAlertDialogBuilder(mContext)
                    .setMessage("Are you sure you want to update this house? You can't redo after update.")
                    .setNeutralButton(R.string.cancel, (dialog, which) -> {

                    })
                    .setPositiveButton(R.string.yes, (dialog, which) -> update())
                    .show();

        }
    }

}