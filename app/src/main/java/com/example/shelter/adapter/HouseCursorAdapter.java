package com.example.shelter.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.shelter.Data.SessionManager;
import com.example.shelter.Data.ShelterDBContract.HouseEntry;

import com.example.shelter.Data.ShelterDBHelper;
import com.example.shelter.Network.ImageRequester;
import com.example.shelter.R;

public class HouseCursorAdapter extends CursorAdapter {
    public final String LOG_TAG = HouseCursorAdapter.class.getSimpleName();
    private ImageRequester imageRequester;
    private SessionManager sessionManager;
    private int layoutId;
    private Context context;



    public HouseCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.context = context;
        imageRequester = new ImageRequester(context);
        sessionManager = new SessionManager(this.context);
    }


    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Find Wishful icon image, we set it visible when user cast a wish
        ImageView wishfulIcon = view.findViewById(R.id.wished_icon);


        if (sessionManager.haveWishPointData()) {
            wishfulIcon.setVisibility(View.VISIBLE);
        } else {
            wishfulIcon.setVisibility(View.GONE);
        }

        //Find individual views need to be modified
        TextView nameTextView = (TextView) view.findViewById(R.id.house_card_name);
        TextView areaTextView = (TextView) view.findViewById(R.id.house_card_area);
        TextView rentCostTextView = (TextView) view.findViewById(R.id.house_card_rent_cost);
        TextView distanceTextView = (TextView) view.findViewById(R.id.house_card_distance);
        ImageView houseImage = (ImageView) view.findViewById(R.id.house_image);

        // Find the columns of House attributes that we're interested in
        int _idColumnIndex = cursor.getColumnIndex(HouseEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_NAME);
        int areaColumnIndex = cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_AREA);
        int rentCostColumnIndex = cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_RENT_COST);
        int latitudeColumnIndex = cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_LATITUDE);
        int longitudeColumnIndex = cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_LONGITUDE);
        int houseTypeColumnIndex = cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_TYPE_ID);

        // Read the House attributes from the Cursor for the current House
        String name = cursor.getString(nameColumnIndex);
        String area = "" + cursor.getString(areaColumnIndex) + " m2";

        //format Price
        String rentCost = ShelterDBHelper.formatPrice(cursor.getFloat(rentCostColumnIndex), context);
        int houseType = cursor.getInt(houseTypeColumnIndex);

        if (!rentCost.equals(context.getString(R.string.sale_only))) {
            if (houseType == 5 || houseType == 8) {
                rentCost += "/Đêm";
            } else {
                rentCost += "/Tháng";
            }
        }
        // Update the TextViews with the attributes for the current House
        nameTextView.setText(name);
        areaTextView.setText(area);
        rentCostTextView.setText(rentCost);

        //Load image View From Cloud
        imageRequester.loadHeaderImage(cursor.getInt(_idColumnIndex), HouseEntry.TABLE_NAME, houseImage);


        //Calculate and display distance between the pointer location and the house
        float distance = ShelterDBHelper.getDistanceFromHouseToThePointer(sessionManager, cursor);
        if (distance > 0) {
            distanceTextView.setText(distance + " km");
        } else {
            Toast.makeText(context, context.getString(R.string.unabale_to_locate_user_location), Toast.LENGTH_LONG).show();
            distanceTextView.setText(R.string.unlocatable);
        }

    }




}
