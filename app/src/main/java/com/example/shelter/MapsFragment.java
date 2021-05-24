package com.example.shelter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shelter.Data.SessionManager;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;

public class MapsFragment extends Fragment {
    public static final String TAG = MapsFragment.class.getName();
    public static final LatLng LAND_MARK_TOWER = new LatLng(10.794890, 106.722113);

    private String lastMarkerName;
    private LatLng lastMarkerLatLng;
    private GoogleMap mMap;


    private SessionManager sessionManager;
    private String fatherContext;

    //
    private MaterialButton hereButton;
    private TextInputEditText wishPointNameEditText;
    private TextInputLayout wishPointNameInputLayout;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            googleMap.addMarker(new MarkerOptions().position(lastMarkerLatLng).title("Here"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastMarkerLatLng,17));

            //Set on map click
            mMap.setOnMapClickListener(latLng -> {
                //If father fragment is House Detail Fragment then set on map click able = false
                if (!fatherContext.equals(HouseDetailFragment.TAG)) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(latLng.latitude + ", " + latLng.longitude);
                    mMap.clear();
                    mMap.addMarker(markerOptions);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                    lastMarkerLatLng = latLng;
                }

            });
        }


    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        Bundle deliver = this.getArguments();
        if (deliver != null) {
            //Get data from the deliver
            fatherContext = deliver.getString("fragment", HouseDetailFragment.TAG);
            //Location Point on map has default value is Landmark tower 81
            lastMarkerLatLng = new LatLng(deliver.getDouble("pointLatitude", LAND_MARK_TOWER.latitude),
                    deliver.getDouble("pointLongitude", LAND_MARK_TOWER.longitude));
        } else {
            throw new NullPointerException("The deliver to map fragment is is null");
        }


        hereButton = view.findViewById(R.id.here);
        wishPointNameEditText = view.findViewById(R.id.wish_point_name_edit_text);
        wishPointNameInputLayout = view.findViewById(R.id.wish_point_name_text_input);

        //Expression of map fragment for house detail fragment
        if (fatherContext.equals(HouseDetailFragment.TAG)) {
            hereButton.setVisibility(View.GONE);
            wishPointNameInputLayout.setVisibility(View.GONE);
            wishPointNameEditText.setVisibility(View.GONE);
        }

        //Expression of map fragment for house helper item fragment

        if (fatherContext.equals(HouseHelperItemFragment.TAG)) {
            wishPointNameInputLayout.setHint(getContext().getString(R.string.address));
            wishPointNameInputLayout.setPlaceholderText(null);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(getContext());
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);


        }



        if (!Places.isInitialized())
            Places.initialize(getContext().getApplicationContext(), getContext().getString(R.string.google_maps_key));

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);


        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(getContext());

        //Set type filter and location filter
        autocompleteFragment.setTypeFilter(TypeFilter.ESTABLISHMENT);
        autocompleteFragment.setCountry("VN");


        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 17.0f));
                lastMarkerLatLng = place.getLatLng();
                Log.d(TAG, "Place: " + place.getName() + ", " + place.getId() + place.getLatLng());
            }


            @Override
            public void onError(@NonNull Status status) {
                Log.d(TAG, "onError: " + status.getStatusMessage());
            }
        });

        //Set locate button click
        hereButton.setOnClickListener(v -> {


            if (fatherContext.equals(HouseHelperItemFragment.TAG)) {
                if (wishPointNameEditText.getText().length() == 0 || wishPointNameEditText.getText() == null) {
                    Toast.makeText(getContext(), R.string.this_place_need_an_address, Toast.LENGTH_SHORT).show();
                }
                else {
                    lastMarkerName = wishPointNameEditText.getText().toString();
                    sessionManager.storeHousePointData(lastMarkerLatLng, lastMarkerName);
                    getParentFragmentManager().popBackStackImmediate();
                }
            }


            //Expression when click here button for cast wish fragment
            if (fatherContext.equals(CastAWishFragment.TAG)) {
                if (wishPointNameEditText.getText().length() == 0 || wishPointNameEditText.getText() == null) {
                    Toast.makeText(getContext(), R.string.this_place_need_a_name, Toast.LENGTH_SHORT).show();
                }
                else {
                    lastMarkerName = wishPointNameEditText.getText().toString();
                    sessionManager.storeWishfulPoint(lastMarkerLatLng, lastMarkerName);
                    getParentFragmentManager().popBackStackImmediate();
                }
            }


        });



    }


}