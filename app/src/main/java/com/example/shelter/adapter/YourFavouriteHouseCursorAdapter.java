package com.example.shelter.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.shelter.Data.SessionManager;
import com.example.shelter.Data.ShelterDBContract.HouseEntry;
import com.example.shelter.Data.ShelterDBHelper;
import com.example.shelter.Network.ImageRequester;
import com.example.shelter.R;

public class YourFavouriteHouseCursorAdapter extends CursorAdapter {
    static final public String TAG = YourFavouriteHouseCursorAdapter.class.getName();
    private Context mContext;
    private ImageRequester imageRequester;

    /**
     * Constructs a new {@link YourFavouriteHouseCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public YourFavouriteHouseCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        mContext = context;
        imageRequester = new ImageRequester(mContext);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.house_item_list_view, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView favouriteHouseNameTV = view.findViewById(R.id.favourite_house_name);
        TextView favouriteHouseRentCostTV = view.findViewById(R.id.favourite_house_rent_cost);
        TextView favouriteHouseSalePriceTV = view.findViewById(R.id.favourite_house_sale_price);
        TextView favouriteHouseAreaTV = view.findViewById(R.id.favourite_house_area);
        TextView favouriteHouseDistanceTV = view.findViewById(R.id.favourite_house_distance);
        ImageView favouriteHouseImageView = view.findViewById(R.id.favourite_house_image);
        ImageButton closedHouseItem = view.findViewById(R.id.closed_favourite_item);


        // Find the columns of House attributes that we're interested in
        int houseNameColumnIndex = cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_NAME);
        int houseIdColumnIndex = cursor.getColumnIndex(HouseEntry._ID);
        int houseAreaColumnIndex = cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_AREA);
        int houseRentCostColumnIndex = cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_RENT_COST);
        int houseSalePriceColumnIndex = cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_SALE_PRICE);
        int houseTypeColumnIndex = cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_TYPE_ID);

        // Read the House attributes from the Cursor for the current House
        String name = cursor.getString(houseNameColumnIndex);
        String area = "" + cursor.getString(houseAreaColumnIndex) + " m2";

        //format Price
        String rentCost = ShelterDBHelper.formatPrice(cursor.getFloat(houseRentCostColumnIndex), context);
        String salePrice = ShelterDBHelper.formatPrice(cursor.getFloat(houseSalePriceColumnIndex), context);
        int houseType = cursor.getInt(houseTypeColumnIndex);

        if (!rentCost.equals(context.getString(R.string.sale_only))) {
            if (houseType == 5 || houseType == 8) {
                rentCost += "/Đêm";
            } else {
                rentCost += "/Tháng";
            }
        }
        if (salePrice.equals(context.getString(R.string.sale_only))) {
            salePrice = context.getString(R.string.rent_only);
        }
        // Update the TextViews with the attributes of the current House
        favouriteHouseNameTV.setText(name);
        favouriteHouseAreaTV.setText(area);
        favouriteHouseRentCostTV.setText(rentCost);
        favouriteHouseSalePriceTV.setText(salePrice);

        //Load House Image
        Glide.with(context)
                .load(imageRequester.getRefHeaderImageOnCloud(cursor.getInt(houseIdColumnIndex), HouseEntry.TABLE_NAME))
                .into(favouriteHouseImageView);

        //Calculate and display distance between user's location and the house
        float distance = ShelterDBHelper.getDistanceFromHouseToThePointer(new SessionManager(context), cursor);
        if (distance == -1) {
            favouriteHouseDistanceTV.setText(R.string.unlocatable);
        } else {
            favouriteHouseDistanceTV.setText(distance + " km");
        }

        if (cursor.getInt(cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_STATE)) == HouseEntry.STATE_VISIBLE) {
            closedHouseItem.setVisibility(View.INVISIBLE);
        }
    }


}
