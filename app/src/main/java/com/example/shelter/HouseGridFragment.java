package com.example.shelter;

import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.shelter.Data.SessionManager;
import com.example.shelter.Data.ShelterDBContract;
import com.example.shelter.Data.ShelterDBContract.HouseEntry;
import com.example.shelter.Data.ShelterDBHelper;
import com.example.shelter.staggeredgridlayout.StaggeredHouseCardRecyclerViewAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.roacult.backdrop.BackdropLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HouseGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    /**
     * Identifier for the House data loader
     */
    public static final String TAG = HouseGridFragment.class.getName();
    private static final int HOUSE_LOADER = 0;
    private static final int GET_WISHED_HOUSE_ID_LOADER = 684;
    private static final int WISHED_HOUSE_LOADER = 666;
    private static boolean isFirstLoad = true;

    private RecyclerView recyclerView;
    private StaggeredHouseCardRecyclerViewAdapter.RecyclerViewOnClickListener listener;
    /**
     * Adapter for the RecycleView
     */
    private StaggeredHouseCardRecyclerViewAdapter recyclerViewAdapter;
    private Cursor cursor;

    //Session Manager
    private SessionManager sessionManager;

    //Swipe to refresh fragment
    private SwipeRefreshLayout swipeRefreshLayout;

    //EmptyView
    private View emptyView;
    //BackLayout Container
    private BackdropLayout backdropLayout;
    //Product Grid
    private View productGrid;
    //Menu container
    private View menuLayout;
    //Menu Items
    private MaterialButton castAWishMenu;
    private MaterialButton signOutMenu;
    private MaterialButton shelterStatisticMenu;
    private MaterialButton houseHelperMenu;
    private MaterialButton myAccountMenu;
    private MaterialButton yourFavouriteMenu;

    //Query value

    private String selectionForWishLoader = null;
    private String selectionForHouseLoader = null;


    private List<String> selectionArgsForWishLoader = null;
    private List<String> selectionArgsForHouseLoader = null;
    private List<String> wishedHouses = null;

    private String sortOrder = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.house_grid_fragment, container, false);
        //Init Session Data for this context
        sessionManager = new SessionManager(getContext());

        //Init backdropLayout
        backdropLayout = view.findViewById(R.id.menu_container);
        productGrid = backdropLayout.getFrontLayout();
        menuLayout = backdropLayout.getBackLayout();
        //init menu item
        createMenuItem(menuLayout);
        setUpToolbar(view);

        //Find House grid card view
        recyclerView = view.findViewById(R.id.recycler_view);
        // Set up the RecyclerView
        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position % 3 == 2 ? 2 : 1;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);


        // Setup an Adapter for each row of House data in the Cursor.
        // There is no House data yet (until the loader finishes) so pass in null for the Cursor.
        setItemOnClickListener();
        cursor = null;
        recyclerViewAdapter = new StaggeredHouseCardRecyclerViewAdapter(getContext(), cursor, listener);
        recyclerView.setAdapter(recyclerViewAdapter);

        //Padding items in recycler View
        int largePadding = getResources().getDimensionPixelSize(R.dimen.shr_staggered_product_grid_spacing_large);
        int smallPadding = getResources().getDimensionPixelSize(R.dimen.shr_staggered_product_grid_spacing_small);
        recyclerView.addItemDecoration(new HouseGridItemDecoration(largePadding, smallPadding));

        //Set Swipe up to refreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            //Set sort order to default
            sortOrder = null;
            //Clear search selection
            if (selectionArgsForHouseLoader != null)
                selectionArgsForHouseLoader.clear();
            selectionForHouseLoader = null;
            LoaderManager.getInstance(HouseGridFragment.this).restartLoader(HOUSE_LOADER, null, HouseGridFragment.this);
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set empty view on the recycler it only shows when the cursor has 0 items.
        //Init empty view
        emptyView = view.findViewById(R.id.emptyView);

    }

    //On House card click
    private void setItemOnClickListener() {
        listener = (v, position, id) -> {
            String houseUri = ContentUris.withAppendedId(HouseEntry.CONTENT_URI, id).toString();
            ((NavigationHost) getActivity()).navigateTo(HouseDetailFragment.NewInstance(houseUri), true);
        };

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: " + isFirstLoad);
        if (sessionManager.haveWishPointData() && isFirstLoad) {
            //Kick off wished house loader
            constructCastWishQuery();
            LoaderManager.getInstance(this).destroyLoader(HOUSE_LOADER);
            LoaderManager.getInstance(this).restartLoader(GET_WISHED_HOUSE_ID_LOADER, null, this);
            isFirstLoad = false;
        } else {
            // Kick off the house loader without any condition
            LoaderManager.getInstance(this).initLoader(HOUSE_LOADER, null, this);
        }
        //Close menu backdrop anytime when this layout is visible to user
        backdropLayout.close();
    }


    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }


        toolbar.inflateMenu(R.menu.shr_toolbar_menu);
        toolbar.setOverflowIcon(AppCompatResources.getDrawable(getContext(), R.drawable.shr_filter));
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.cheaper:
                        sortOrder = HouseEntry.COLUMN_HOUSE_RENT_COST + " ASC";
                        LoaderManager.getInstance(HouseGridFragment.this).restartLoader(HOUSE_LOADER, null, HouseGridFragment.this);
                        break;
                    case R.id.name_a_z:
                        sortOrder = HouseEntry.COLUMN_HOUSE_NAME + " ASC";
                        LoaderManager.getInstance(HouseGridFragment.this).restartLoader(HOUSE_LOADER, null, HouseGridFragment.this);
                        break;
                    case R.id.expire_wish:
                        sessionManager.expireWishfulPointData();
                        if (wishedHouses != null) {
                            wishedHouses.clear();
                            wishedHouses = null;
                        }
                        selectionForHouseLoader = null;
                        LoaderManager.getInstance(HouseGridFragment.this).restartLoader(HOUSE_LOADER, null, HouseGridFragment.this);
                        break;
                }
                return true;
            }
        });


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menu.clear();
        menuInflater.inflate(R.menu.shr_toolbar_menu, menu);

        //Init searchView
        MenuItem item = menu.findItem(R.id.action_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        final SearchView searchView = (SearchView) item.getActionView();
        item.setActionView(searchView);

        //Handle search event
        searchView.setQueryHint(getContext().getString(R.string.search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                searchView.setQuery("", false);
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                item.collapseActionView();

                Toast.makeText(getContext(), "query search:" + query, Toast.LENGTH_SHORT).show();
                //We add it three time for 3 filter conditions
                selectionArgsForHouseLoader = new ArrayList<>();
                String likeQuery = "%" + query + "%";
                selectionArgsForHouseLoader.add(likeQuery);
                selectionArgsForHouseLoader.add(likeQuery);
                selectionArgsForHouseLoader.add(likeQuery);
                //Restart Loader
                LoaderManager.getInstance(HouseGridFragment.this).restartLoader(HOUSE_LOADER, null, HouseGridFragment.this);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });


    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = null;
        String[] selectionArgsArray = null;
        CursorLoader cursorLoader = null;
        switch (id) {
            case HOUSE_LOADER:
                projection = new String[]{
                        HouseEntry._ID,
                        HouseEntry.COLUMN_HOUSE_NAME,
                        HouseEntry.COLUMN_HOUSE_AREA,
                        HouseEntry.COLUMN_HOUSE_RENT_COST,
                        HouseEntry.COLUMN_HOUSE_LATITUDE,
                        HouseEntry.COLUMN_HOUSE_LONGITUDE,
                        HouseEntry.COLUMN_HOUSE_ADDRESS,
                        HouseEntry.COLUMN_HOUSE_TYPE_ID};

                String selectionForSearch = null;
                selectionArgsArray = null;
                if (selectionArgsForHouseLoader != null && selectionArgsForHouseLoader.size() == 3) {
                    //Get Data in search view
                    selectionArgsArray = new String[selectionArgsForHouseLoader.size()];
                    selectionArgsForHouseLoader.toArray(selectionArgsArray);

                    //Selection string
                    selectionForSearch = "(" + HouseEntry.COLUMN_HOUSE_NAME + " LIKE ? ";
                    selectionForSearch += " OR " + HouseEntry.COLUMN_HOUSE_ADDRESS + " LIKE ? ";

                    String queryHouseTypeName = " = (SELECT " + ShelterDBContract.HouseTypeEntry._ID
                            + " FROM " + ShelterDBContract.HouseTypeEntry.TABLE_NAME
                            + " WHERE " + ShelterDBContract.HouseTypeEntry.COLUMN_HOUSE_TYPE_NAME + " LIKE ? )";

                    selectionForSearch += " OR " + HouseEntry.COLUMN_HOUSE_TYPE_ID + queryHouseTypeName + ")";
                }

                if (sessionManager.haveWishPointData() && (wishedHouses == null || wishedHouses.isEmpty())) {
                    //query to ensure this loader return zero items. Because there are no available houses for this wish
                    selectionForHouseLoader = HouseEntry._ID + " IS NULL";
                } else if (wishedHouses != null && !wishedHouses.isEmpty()) {
                    String inClause = HouseEntry._ID + " IN " + wishedHouses.toString();
                    inClause = inClause.replace('[', '(');
                    inClause = inClause.replace(']', ')');
                    selectionForHouseLoader = inClause;


                }
                Log.d(TAG, "onCreateLoader: wishedHouseQuery selection" + selectionForHouseLoader);
                String finalSelectionString = null;
                if (selectionForSearch != null && selectionForHouseLoader != null) {
                    finalSelectionString = selectionForSearch + " AND " + selectionForHouseLoader /**/;
                } else if (selectionForSearch != null) {
                    finalSelectionString = selectionForSearch;
                } else if (selectionForHouseLoader != null) {
                    finalSelectionString = selectionForHouseLoader;
                }


                // This loader will execute the ContentProvider's query method on a background thread
                cursorLoader = new CursorLoader(getContext(),   // Parent activity context
                        HouseEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        finalSelectionString,                   // selection clause
                        selectionArgsArray,                   // selection arguments
                        sortOrder);      //  sort order
                break;

            case GET_WISHED_HOUSE_ID_LOADER:
                projection = new String[]{HouseEntry._ID,
                        HouseEntry.COLUMN_HOUSE_LATITUDE,
                        HouseEntry.COLUMN_HOUSE_LONGITUDE};

                selectionArgsArray = null;
                if (selectionArgsForWishLoader != null) {
                    if (selectionArgsForWishLoader.size() > 0) {
                        selectionArgsArray = new String[selectionArgsForWishLoader.size()];
                        selectionArgsForWishLoader.toArray(selectionArgsArray);
                    }
                }

                // This loader will execute the ContentProvider's query method on a background thread
                cursorLoader = new CursorLoader(getContext(),   // Parent activity context
                        HouseEntry.CONTENT_URI,   // Provider content URI to query
                        projection,             // Columns to include in the resulting Cursor
                        selectionForWishLoader,                   // selection clause
                        selectionArgsArray,                   // selection arguments
                        null);      //  sort order
                break;

            default:
                throw new IllegalArgumentException("Invalid ID LOADER AT " + TAG);
        }


        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case GET_WISHED_HOUSE_ID_LOADER:
                wishedHouses = new ArrayList<>();
                if (data.moveToFirst()) {
                    do {
                        if (ShelterDBHelper.getDistanceFromHouseToThePointer(sessionManager, data) <= HouseEntry.MAX_NEARBY_RADIUS
                                || sessionManager.getWishfulPointLatLng().latitude == MapsFragment.LAND_MARK_TOWER.latitude
                                || sessionManager.getWishfulPointLatLng().longitude == MapsFragment.LAND_MARK_TOWER.longitude) {

                            wishedHouses.add(data.getString(data.getColumnIndex(HouseEntry._ID)));
                        }
                    } while (data.moveToNext());

                }
                LoaderManager.getInstance(this).restartLoader(HOUSE_LOADER, null, this);
                break;
            case HOUSE_LOADER:
                if (data != null && data.getCount() > 0) {
                    cursor = data;
                    recyclerViewAdapter.mCursorAdapter.swapCursor(cursor);
                    recyclerViewAdapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);

                } else {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }

                break;

        }


    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case HOUSE_LOADER:
                cursor = null;
                recyclerViewAdapter.mCursorAdapter.swapCursor(null);
                if (selectionArgsForHouseLoader != null)
                    selectionArgsForHouseLoader.clear();
                if (wishedHouses != null) {
                    wishedHouses.clear();
                }
                selectionForHouseLoader = null;
                break;
            case GET_WISHED_HOUSE_ID_LOADER:
                selectionForWishLoader = null;
                if (selectionArgsForWishLoader != null)
                    selectionArgsForWishLoader.clear();
                break;

        }


    }

    private void createMenuItem(View view) {

        //House Viewer
        signOutMenu = (MaterialButton) view.findViewById(R.id.sign_out_menu);
        castAWishMenu = (MaterialButton) view.findViewById(R.id.cast_a_wish_menu);
        yourFavouriteMenu = (MaterialButton) view.findViewById(R.id.your_favourite_menu);
        myAccountMenu = (MaterialButton) view.findViewById(R.id.my_account_menu);
        final MaterialButton termPrivacyMenu = view.findViewById(R.id.privacy_terms);

        signOutMenu.setOnClickListener(this);

        yourFavouriteMenu.setOnClickListener(this);
        yourFavouriteMenu.onCancelPendingInputEvents();

        castAWishMenu.setOnClickListener(this);
        myAccountMenu.setOnClickListener(this);

        termPrivacyMenu.setOnClickListener(this);

        //House Owner
        houseHelperMenu = (MaterialButton) view.findViewById(R.id.house_helper_menu);
        shelterStatisticMenu = (MaterialButton) view.findViewById(R.id.shelter_statistics_menu);
        final MaterialButton contactManagerMenu = view.findViewById(R.id.contact_manager_menu);

        if (sessionManager.getUserRole() == ShelterDBContract.UserEntry.VIEWER) {
            houseHelperMenu.setVisibility(View.GONE);
            shelterStatisticMenu.setVisibility(View.GONE);
        } else {
            houseHelperMenu.setOnClickListener(this);
            shelterStatisticMenu.setOnClickListener(this);
            contactManagerMenu.setOnClickListener(this);
        }

    }

    //Menu Items On Click
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.sign_out_menu) {

            sessionManager.clearUserSession();
            sessionManager.clearGlobalDataSession();
            ((NavigationHost) getActivity()).navigateTo(new LoginFragment(), false);
        } else if (id == R.id.your_favourite_menu) {
            ((NavigationHost) getActivity()).navigateTo(new YourFavouriteFragment(), true);
        } else if (id == R.id.my_account_menu) {
            ((NavigationHost) getActivity()).navigateTo(new MyAccountFragment(), true);
        } else if (id == R.id.cast_a_wish_menu) {
            isFirstLoad = true;
            ((NavigationHost) getActivity()).navigateTo(new CastAWishFragment(), true);
        } else if (id == R.id.house_helper_menu) {
            ((NavigationHost) getActivity()).navigateTo(new HousesHelperFragment(), true);
        } else if (id == R.id.contact_manager_menu) {
            ((NavigationHost) getActivity()).navigateTo(new ContactManagerFragment(), true);
        } else if (id == R.id.privacy_terms) {
            ((NavigationHost) getActivity()).navigateTo(new TermPrivacyFragment(), true);
        }
    }

    private void constructCastWishQuery() {
        boolean hadFirstParameter = false;
        selectionArgsForWishLoader = new ArrayList<>();
        selectionForWishLoader = null;
        if (sessionManager.getWishfulPointPlace() > 0) {
            selectionForWishLoader = "" + HouseEntry.COLUMN_HOUSE_PLACE + "= ?";
            hadFirstParameter = true;
            selectionArgsForWishLoader.add(String.valueOf(sessionManager.getWishfulPointPlace()));
        }
        if (sessionManager.getWishfulPointHouseType() > 0) {
            if (!hadFirstParameter) {
                selectionForWishLoader = "";
                hadFirstParameter = true;
            } else {
                selectionForWishLoader += " AND ";
            }
            selectionForWishLoader += HouseEntry.COLUMN_HOUSE_TYPE_ID + " = ?";
            selectionArgsForWishLoader.add(String.valueOf(sessionManager.getWishfulPointHouseType()));
        }

        float area = sessionManager.getWishfulPointArea();
        if (area > 0) {
            if (!hadFirstParameter) {
                selectionForWishLoader = "";
                hadFirstParameter = true;
            } else {
                selectionForWishLoader += " AND ";
            }
            selectionForWishLoader += HouseEntry.COLUMN_HOUSE_AREA + " BETWEEN ? AND ?";
            selectionArgsForWishLoader.add(String.valueOf(area - area * 0.5));
            selectionArgsForWishLoader.add(String.valueOf(area + area * 0.5));
        }
        if (sessionManager.getWishfulPointFloor() > 0) {
            if (!hadFirstParameter) {
                selectionForWishLoader = "";
                hadFirstParameter = true;
            } else {
                selectionForWishLoader += " AND ";
            }
            selectionForWishLoader += HouseEntry.COLUMN_HOUSE_FLOORS + " BETWEEN ? AND ?";
            selectionArgsForWishLoader.add(String.valueOf(sessionManager.getWishfulPointFloor() - 1));
            selectionArgsForWishLoader.add(String.valueOf(sessionManager.getWishfulPointFloor() + 1));
        }

        float rentCost = sessionManager.getWishfulPointRentCost();
        if (rentCost > 0) {
            if (!hadFirstParameter) {
                selectionForWishLoader = "";
                hadFirstParameter = true;
            } else {
                selectionForWishLoader += " AND ";
            }
            selectionForWishLoader += HouseEntry.COLUMN_HOUSE_RENT_COST + " BETWEEN ? AND ?";
            selectionArgsForWishLoader.add(String.valueOf(rentCost - rentCost * 0.25));
            selectionArgsForWishLoader.add(String.valueOf(rentCost + rentCost * 0.25));
        }

        float salePrice = sessionManager.getWishfulPointSalePrice();
        if (salePrice > 0) {
            if (!hadFirstParameter) {
                selectionForWishLoader = "";
                hadFirstParameter = true;
            } else {
                selectionForWishLoader += " AND ";
            }
            selectionForWishLoader += HouseEntry.COLUMN_HOUSE_SALE_PRICE + " BETWEEN ? AND ?";
            selectionArgsForWishLoader.add(String.valueOf(salePrice - salePrice * 0.25));
            selectionArgsForWishLoader.add(String.valueOf(salePrice + salePrice * 0.25));
        }

        if (sessionManager.getWishfulPointBedRooms() > 0) {
            if (!hadFirstParameter) {
                selectionForWishLoader = "";
                hadFirstParameter = true;
            } else {
                selectionForWishLoader += " AND ";
            }
            selectionForWishLoader += HouseEntry.COLUMN_HOUSE_BED_ROOMS + " BETWEEN ? AND ?";
            selectionArgsForWishLoader.add(String.valueOf(sessionManager.getWishfulPointBedRooms()));
            selectionArgsForWishLoader.add(String.valueOf(sessionManager.getWishfulPointBedRooms() + 2));
        }
        if (sessionManager.getWishfulPointBathRooms() > 0) {
            if (!hadFirstParameter) {
                selectionForWishLoader = "";
                hadFirstParameter = true;
            } else {
                selectionForWishLoader += " AND ";
            }
            selectionForWishLoader += HouseEntry.COLUMN_HOUSE_BATH_ROOMS + " BETWEEN ? AND ?";
            selectionArgsForWishLoader.add(String.valueOf(sessionManager.getWishfulPointBathRooms()));
            selectionArgsForWishLoader.add(String.valueOf(sessionManager.getWishfulPointBathRooms() + 2));
        }

        int yearBuilt = sessionManager.getWishFulPointYearBuilt();
        if (yearBuilt > 0) {
            if (!hadFirstParameter) {
                selectionForWishLoader = "";
            } else {
                selectionForWishLoader += " AND ";
            }
            selectionForWishLoader += HouseEntry.COLUMN_HOUSE_YEAR_BUILT + " BETWEEN ? AND ?";
            selectionArgsForWishLoader.add(String.valueOf(yearBuilt - 7));
            selectionArgsForWishLoader.add(String.valueOf(yearBuilt + 10));
        }


        String debugSelectionArgs = "";
        if (selectionArgsForWishLoader != null) {
            for (String s : selectionArgsForWishLoader) {
                debugSelectionArgs += " " + s;
            }
        }

        Log.d(TAG, "constructCastWishQuery: query: " + selectionForWishLoader + " --- data: + " + debugSelectionArgs);
    }


}
