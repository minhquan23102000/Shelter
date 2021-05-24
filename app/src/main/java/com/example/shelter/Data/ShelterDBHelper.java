package com.example.shelter.Data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.shelter.BuildConfig;
import com.example.shelter.MainActivity;
import com.example.shelter.Data.ShelterDBContract.HouseEntry;
import com.example.shelter.R;
import com.google.android.gms.maps.model.LatLng;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;

public class ShelterDBHelper extends SQLiteAssetHelper {
    public static final String LOG_TAG = ShelterDBHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "shelter.db";
    private static int DATABASE_VERSION = 1;

    private Context mContext;


    public ShelterDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DATABASE_VERSION = newVersion;
    }


    public static float getDistanceFromHouseToThePointer(SessionManager sessionManager, Cursor cursor) {
        float distance = 0;
        int latitudeColumnIndex = cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_LATITUDE);
        int longitudeColumnIndex = cursor.getColumnIndex(HouseEntry.COLUMN_HOUSE_LONGITUDE);
        //Get House and point location
        Location pointLocation = new Location("");
        if (sessionManager.haveWishPointData()) {
            LatLng tempPoint = sessionManager.getWishfulPointLatLng();
            pointLocation.setLatitude(tempPoint.latitude);
            pointLocation.setLongitude(tempPoint.longitude);
        } else {
            pointLocation = MainActivity.getUserLocation();
        }

        Location houseLocation = new Location("");
        houseLocation.setLatitude(cursor.getDouble(latitudeColumnIndex));
        houseLocation.setLongitude(cursor.getDouble(longitudeColumnIndex));

        //Calculate and display distance between user's location and the house
        try {
            distance = round(pointLocation.distanceTo(houseLocation) / 1000, 1);

        } catch (Exception e) {
            distance = -1;
        }
        return distance;

    }

    public static void increaseValueToOne(String table, String column, int id,  Context context) {
        ShelterDBHelper dbHelper = new ShelterDBHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String updateQuery = "UPDATE " + table
                            + " SET " + column + " = " + column + " + 1"
                            + " WHERE _id = " + id;
        database.execSQL(updateQuery);
        database.close();
        dbHelper.close();
    }

    public static void updateHouseState(int id, int state, Context context) {
        ShelterDBHelper dbHelper = new ShelterDBHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String updateQuery = "UPDATE " + HouseEntry.TABLE_NAME
                + " SET " + HouseEntry.COLUMN_HOUSE_STATE + " = " + state
                + " WHERE _id = " + id;
        database.execSQL(updateQuery);
        database.close();
        dbHelper.close();
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        if (decimalPlace == 0) {
            return bd.intValue();
        }
        return bd.floatValue();
    }

    public static String formatPrice(Float price, Context context) {
        if (price <= 0) {
            return context.getString(R.string.sale_only);
        }
        String priceFormat = "";

        if (price < 1.0) {
            price*=1000;
            price = round(price, 0);
            priceFormat = price.intValue() + "Ngàn";
        } else if (price < 1000) {
            price = round(price, 1);
            priceFormat = price + "Triệu";
        } else {
            price/=1000;
            price = round(price, 2);
            priceFormat = price + "Tỷ";
        }

        return priceFormat;
    }
}
