package com.example.shelter;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.shelter.Data.SessionManager;
import com.example.shelter.Data.ShelterDBContract.HouseTypeEntry;
import com.example.shelter.Network.ImageRequester;
import com.example.shelter.Data.ShelterDBContract.HouseEntry;
import com.example.shelter.adapter.ImageSliderAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.StorageReference;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;


public class HouseHelperItemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = HousesHelperFragment.class.getName();
    private static final int HOUSE_DATA_LOADER = 349;

    //Session manager
    private SessionManager sessionManager;

    //Init intent launcher
    ActivityResultLauncher<Intent> someActivityResultLauncher;

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

    private AutoCompleteTextView placeText;
    private AutoCompleteTextView houseTypeText;

    private ArrayAdapter<String> placeAdapter;
    private ArrayAdapter<String> houseTypeAdapter;
    private List<String> houseTypeList;

    //Button
    private ImageButton addImageBT;
    private MaterialButton updateButton;
    private MaterialButton redoButton;



    private ProgressBar progressBar;

    //Image Adapter
    private ImageSliderAdapter sliderAdapter;
    //Slider View
    private SliderView sliderView;

    private ImageRequester imageRequester;


    //Value to confirm everything is ok
    private int oldCountImageHouse = 0;
    private int tempHouseId;
    private boolean clickUpdateAndDataIsValid = false;
    private boolean isNewHouse = false;
    //On Key listener
    private View.OnKeyListener onKeyListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //Init image requester
        imageRequester = new ImageRequester(getContext());

        // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
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
                            if (uriList.size() + oldCountImageHouse <= HouseEntry.LIMIT_IMAGES) {
                                progressBar.setVisibility(View.VISIBLE);
                                imageRequester.uploadListImagesUriToCloud(uriList, HouseEntry.TABLE_NAME, tempHouseId, progressBar, oldCountImageHouse);
                            } else {
                                Toast.makeText(getContext(), "House's images count should be <= 15", Toast.LENGTH_SHORT).show();
                            }

                            List<StorageReference> referenceList = imageRequester.getListRefImageOnCloud(tempHouseId, HouseEntry.TABLE_NAME, uriList.size());
                            sliderAdapter.addMultiItems(referenceList);
                        }
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
        sessionManager = new SessionManager(getContext());

        //Get item from deliver
        Bundle deliver = new Bundle();
        deliver = this.getArguments();

        tempHouseId = deliver.getInt("houseId", -1);
        //If temp house != -1 we kick the loader to fill data in UI. Else we insert a new house, so the data in UI will be blank.
        if (tempHouseId != -1) {
            LoaderManager.getInstance(this).initLoader(HOUSE_DATA_LOADER, null, this);

        } else {
            //We create a new temp house in database for get new id to use it for declare new image on cloud (Declare unique image need an unique id)
            ContentValues values = new ContentValues();
            values.put(HouseEntry.COLUMN_HOUSE_NAME, "temp house");
            values.put(HouseEntry.COLUMN_HOUSE_AREA, 0);
            values.put(HouseEntry.COLUMN_HOUSE_LATITUDE, -1);
            values.put(HouseEntry.COLUMN_HOUSE_LONGITUDE, -1);
            values.put(HouseEntry.COLUMN_HOUSE_ADDRESS, "temp house address");
            values.put(HouseEntry.COLUMN_HOUSE_FLOORS, 0);
            tempHouseId = (int)ContentUris.parseId(getContext().getContentResolver().insert(HouseEntry.CONTENT_URI, values));

            //Set it to true to recognize its later to delete this house when update failed
            isNewHouse = true;
        }

        //Init image slider
        sliderView = view.findViewById(R.id.image_slider);
        sliderAdapter = new ImageSliderAdapter(getContext(), true);
        sliderView.setSliderAdapter(sliderAdapter);


        //Init progress Bar
        progressBar = view.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);


        //Init add Image Button, when on click, navigate to user gallery to pick images
        addImageBT = view.findViewById(R.id.add_image);
        addImageBT.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(
                    getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MainActivity.REQUEST_READ_EXTERNAl_FILE);
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
        updateButton = view.findViewById(R.id.next_button);
        updateButton.setOnClickListener( v-> {
            checkDataValid();
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

        houseNameEditText = view.findViewById(R.id.house_name_edit_text);
        houseNameInputLayout = view.findViewById(R.id.wishful_point_text_input);

        yardSizeEditText = view.findViewById(R.id.yard_size_edit_text);
        yardSizeInputLayout = view.findViewById(R.id.yard_size_text_input);

        placeText = view.findViewById(R.id.place_text);
        houseTypeText = view.findViewById(R.id.house_type_text);

        //Set adapter for the spinner
        placeAdapter = new ArrayAdapter<String>(getContext(), R.layout.dropdown_menu, HouseEntry.POSSIBLE_VALUE_PLACES);
        placeText.setText(placeAdapter.getItem(0), false);
        placeText.setAdapter(placeAdapter);

        houseTypeList = new ArrayList<>();
        Cursor houseTypeCursor = getContext().getContentResolver().query(HouseTypeEntry.CONTENT_URI,
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
            houseTypeAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_menu, asArray);
            houseTypeText.setText(houseTypeAdapter.getItem(0), false);
            houseTypeText.setAdapter(houseTypeAdapter);
        }
        houseTypeCursor.close();


        //Set on key listener to clear error
        onKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (v.getId() == R.id.house_name_edit_text) {
                    houseNameInputLayout.setError(null);
                }

                else if (v.getId() == R.id.house_point_edit_text) {
                    housePointInputLayout.setError(null);
                }

                else if (v.getId() == R.id.area_edit_text) {
                    areaInputLayout.setError(null);
                }

                else if (v.getId() == R.id.floor_edit_text) {
                    floorInputLayout.setError(null);
                }

                else if (v.getId() == R.id.bedrooms_edit_text) {
                    bedRoomsInputLayout.setError(null);
                }

                else if (v.getId() == R.id.bathrooms_edit_text) {
                    bathRoomsInputLayout.setError(null);
                }

                else if (v.getId() == R.id.rent_cost_edit_text) {
                    salePriceInputLayout.setError(null);
                }

                else if (v.getId() == R.id.sale_price_edit_text) {
                    salePriceInputLayout.setError(null);
                }


                return false;
            }

        };



        return view;
    }



    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }


        toolbar.inflateMenu(R.menu.house_helper_toolbar_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                }
                return true;
            }
        });


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(getContext(),
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
            String houseBathRooms =  data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_BATH_ROOMS));
            String houseFloors =  data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_FLOORS));
            String houseYearBuilt =  data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_YEAR_BUILT));
            String houseYardSize =  data.getString(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_YARD_SIZE));
            int housePlace = data.getInt(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_PLACE));
            int houseType = data.getInt(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_TYPE_ID));
            int images_count = data.getInt(data.getColumnIndex(HouseEntry.COLUMN_HOUSE_COUNT_IMAGES));

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
            houseTypeText.setText(houseTypeAdapter.getItem(houseType-1), false);
            placeText.setText(placeAdapter.getItem(housePlace), false);

            List<StorageReference> houseImages = imageRequester.getListRefImageOnCloud(tempHouseId, HouseEntry.TABLE_NAME, images_count);
            sliderAdapter.addMultiItems(houseImages);
            oldCountImageHouse = images_count;
        }


    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public void onDestroyView() {
        if (clickUpdateAndDataIsValid) {
            //All value from UI
            String houseName = houseNameEditText.getText().toString().trim();
            float houseArea = Float.parseFloat(areaEditText.getText().toString());
            int houseFloor = Integer.parseInt(floorEditText.getText().toString());
            int bedrooms = Integer.parseInt(bedRoomsEditText.getText().toString());
            int bathrooms = Integer.parseInt(bathRoomsEditText.getText().toString());
            float yardSize = Float.parseFloat(yardSizeEditText.getText().toString());
            int yearBuilt = Integer.parseInt(yearBuiltEditText.getText().toString());
            float salePrice = Float.parseFloat(salePriceEditText.getText().toString());
            float rentCost = Float.parseFloat(rentCostEditText.getText().toString());

            int houseType = houseTypeAdapter.getPosition(houseTypeText.getText().toString().trim()) + 1;
            // +1 because array start from zero,
            // but in database index start from one
            int place = placeAdapter.getPosition(placeText.getText().toString().trim()) + 1;
            // +1 like above, value of place start from one

            LatLng housePoint = sessionManager.getHousePointLatLng();
            double pointLat = housePoint.latitude;
            double pointLng = housePoint.longitude;
            String houseAddress = sessionManager.getHousePointAddress();


            //Put values to the deliver
            ContentValues values = new ContentValues();
            values.put(HouseEntry.COLUMN_HOUSE_NAME, houseName);
            values.put(HouseEntry.COLUMN_HOUSE_AREA, houseArea);
            values.put(HouseEntry.COLUMN_HOUSE_FLOORS, houseFloor);
            values.put(HouseEntry.COLUMN_HOUSE_BED_ROOMS, bedrooms);
            values.put(HouseEntry.COLUMN_HOUSE_BATH_ROOMS, bathrooms);
            values.put(HouseEntry.COLUMN_HOUSE_YARD_SIZE, yardSize);
            values.put(HouseEntry.COLUMN_HOUSE_YEAR_BUILT, yearBuilt);
            values.put(HouseEntry.COLUMN_HOUSE_SALE_PRICE, salePrice);
            values.put(HouseEntry.COLUMN_HOUSE_RENT_COST, rentCost);
            values.put(HouseEntry.COLUMN_HOUSE_LATITUDE, pointLat);
            values.put(HouseEntry.COLUMN_HOUSE_LONGITUDE, pointLng);
            values.put(HouseEntry.COLUMN_HOUSE_ADDRESS, houseAddress);
            values.put(HouseEntry.COLUMN_HOUSE_TYPE_ID, houseType);
            values.put(HouseEntry.COLUMN_HOUSE_PLACE, place);
            values.put(HouseEntry.COLUMN_HOUSE_COUNT_IMAGES, sliderAdapter.getSliderItems().size()); //Put count image size == size of the slider adapter

            //Time to call the provider to transport to the deliver to the mother database
            getContext().getContentResolver().update(ContentUris.withAppendedId(HouseEntry.CONTENT_URI, tempHouseId),
                    values,
                    null,
                    null);

            //Here we come to the complicated part, update image to cloud. Because of our image name system,
            // that declare image name by its position and id. So the order of list image that need to put on cloud is very important.
            //But when user change list image by delete some item at middle of the list. The order name of image will be broken.

            // We come to an easy solution but it is not quite elegant. But there are no time left for we to think about it.
            // The Dead Line is on our neck. Try to let the app run is more important.
            //Solution is replace all the exists image on cloud by upload all the images we have in sliders adapter.

            //First we go to cloud to get all the images and wrap it to the bytes package
            //Thanks to the imageRequester i created. We got this short code.
            List<byte[]> bytesPackage = imageRequester.downloadListImageFromRefs(sliderAdapter.getSliderItems());

            //Second, update this package to the cloud
            imageRequester.uploadListImagesBytesToCloud(bytesPackage, HouseEntry.TABLE_NAME, tempHouseId, progressBar);

            //Lastly releases the extra images. When the size of items changes is smaller than the old size
            int sizeOfCurrentListImage = sliderAdapter.getSliderItems().size();
            if (sizeOfCurrentListImage < oldCountImageHouse) {
                imageRequester.deleteImagesFromCloud(HouseEntry.TABLE_NAME, tempHouseId, sizeOfCurrentListImage + 1, oldCountImageHouse);
            }

            //Done we safely manage to escape from the DeadLine. Maybe I code ten thousands lines of code already.


        } else {
            //Update failed
            if (isNewHouse) {
                getContext().getContentResolver().delete(ContentUris.withAppendedId(HouseEntry.CONTENT_URI, tempHouseId), null, null);
            }

            imageRequester.deleteImagesFromCloud(HouseEntry.TABLE_NAME, tempHouseId, oldCountImageHouse+1, sliderAdapter.getSliderItems().size());
        }

        super.onDestroyView();
    }

    private void checkDataValid() {
        boolean flag = true;

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

        if (salePriceEditText.getText() == null || salePriceEditText.getText().toString().equals("")
        || rentCostEditText.getText() == null || rentCostEditText.getText().toString().equals("")) {
            salePriceInputLayout.setError(getString(R.string.price_house_check));
            flag = false;
        }

        if (flag) {
            clickUpdateAndDataIsValid = true;
            Bundle deliver = new Bundle();
            deliver.putInt("houseId", tempHouseId);
            Fragment fragment = new HouseHelperItemFragment();
            fragment.setArguments(deliver);
            getParentFragmentManager().beginTransaction().replace(R.id.main_container, fragment).commit();
        }
    }

}