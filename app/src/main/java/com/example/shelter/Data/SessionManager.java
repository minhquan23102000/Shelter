package com.example.shelter.Data;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.shelter.MainActivity;
import com.example.shelter.MapsFragment;
import com.example.shelter.MyAccountFragment;
import com.example.shelter.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class SessionManager {
    public static final String TAG = SessionManager.class.getName();
    private final SharedPreferences userSession;
    private final SharedPreferences deliverGlobalDataSession;
    private final SharedPreferences.Editor userDataEditor;
    private final SharedPreferences.Editor globalDataEditor;
    private final Context mContext;

    static final public String KEY_USER_PHONE = "userPhone";
    static final public String KEY_USER_EMAIL = "userEmail";
    static final public String KEY_USER_URI = "userUri";
    static final public String KEY_USER_FULLNAME = "userFullName";
    static final public String KEY_USER_ROLE = "userRole";
    static final public String KEY_USER_INCOME = "userIncome";
    static final public String KEY_USER_GENDER = "userGender";
    static final public String KEY_USER_DAY_BIRTH = "userDayBirth";

    //Temp data for update account in my account fragment
    static final public String KEY_USER_PHONE_VERIFY = "userPhoneVerify";
    static final public String KEY_USER_TEMP_FULLNAME = "userTempFullName";
    static final public String KEY_USER_TEMP_INCOME = "userTempInCome";
    static final public String KEY_USER_TEMP_GENDER = "userTempGender";
    static final public String KEY_USER_TEMP_DAY_BIRTH = "userTempDayBirth";
    static final public String KEY_USER_TEMP_EMAIL = "userTempEmail";
    //For verify phone
    static final public String KEY_IS_VERIFY = "phoneVerify";

    //For cast a wish
    static final public String KEY_WISHFUL_POINT_LAT = "wishfulPointLat";
    static final public String KEY_WISHFUL_POINT_LONG = "wishfulPointLong";
    static final public String KEY_WISHFUL_POINT_NAME = "wishfulPointName";
    static final public String KEY_WISHFUL_POINT_AREA = "wishfulPointArea";
    static final public String KEY_WISHFUL_POINT_PLACE = "wishfulPointPlace";
    static final public String KEY_WISHFUL_POINT_HOUSETYPE = "wishfulPointHouseType";
    static final public String KEY_WISHFUL_POINT_BEDROOMS = "wishfulPointBedRooms";
    static final public String KEY_WISHFUL_POINT_BATHROOMS = "wishfulPointBathRooms";
    static final public String KEY_WISHFUL_POINT_FLOORS = "wishfulPointFloor";
    static final public String KEY_WISHFUL_POINT_RENT_COST = "wishfulPointRentCost";
    static final public String KEY_WISHFUL_POINT_SALE_PRICE = "wishfulPointSalePrice";
    static final public String KEY_WISHFUL_POINT_YEAR_BUILT = "wishfulPointYearBuilt";
    static final public String KEY_HAS_WISHFUL_POINT = "hasWishfulPoint";

    //Deliver Data for House Helper Items Fragment
    static final public String KEY_HOUSE_POINT_LAT = "housePointLat";
    static final public String KEY_HOUSE_POINT_LONG = "housePointLong";
    static final public String KEY_HOUSE_POINT_ADDRESS = "housePointAddress";

    //Helper function
    private void putDouble(SharedPreferences.Editor editor, final String key, final double value) {
        editor.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(SharedPreferences session, final String key, final double defaultValue) {
        return Double.longBitsToDouble(session.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    public SessionManager(Context context) {
        mContext = context;
        userSession = mContext.getSharedPreferences("userSession", Context.MODE_PRIVATE);
        deliverGlobalDataSession = mContext.getSharedPreferences("deliverGlobalDataSession", Context.MODE_PRIVATE);
        userDataEditor = userSession.edit();
        globalDataEditor = deliverGlobalDataSession.edit();
    }

    public void initUserSession(String userPhone, String userEmail, String userUri, String userFullName, int userRole) {
        if (userPhone != null) {
            userDataEditor.putString(KEY_USER_PHONE, userPhone);
        }

        if (userEmail != null) {
            userDataEditor.putString(KEY_USER_EMAIL, userEmail);
        }
        if (userFullName != null) {
            userDataEditor.putString(KEY_USER_FULLNAME, userFullName);
        }
        if (userUri != null) {
            userDataEditor.putString(KEY_USER_URI, userUri);
        }
        userDataEditor.putInt(KEY_USER_ROLE, userRole);
        userDataEditor.commit();
    }

    public void initUserSession(String userPhone, String userEmail, String userUri, String userFullName, float userIncome,
                                String userDateBirth, int userGender, int userRole) {
        if (userPhone != null) {
            userDataEditor.putString(KEY_USER_PHONE, userPhone);
        }

        if (userEmail != null) {
            userDataEditor.putString(KEY_USER_EMAIL, userEmail);
        }
        if (userFullName != null) {
            userDataEditor.putString(KEY_USER_FULLNAME, userFullName);
        }
        if (userUri != null) {
            userDataEditor.putString(KEY_USER_URI, userUri);
        }
        if (userDateBirth != null) {
            userDataEditor.putString(KEY_USER_DAY_BIRTH, userDateBirth);
        }
        userDataEditor.putFloat(KEY_USER_INCOME, userIncome);
        userDataEditor.putInt(KEY_USER_GENDER, userGender);
        userDataEditor.putInt(KEY_USER_ROLE, userRole);
        userDataEditor.commit();
    }

    public void initUserTempData(String userPhone, String userEmail, String userFullName, float userIncome,
                                  String userDateBirth, int userGender) {
        Log.d(MyAccountFragment.LOG_TAG, "initUserTempData: " + userPhone + userEmail + userFullName + userIncome + userDateBirth + userGender);
        if (userPhone != null) {
            userDataEditor.putString(KEY_USER_PHONE_VERIFY, userPhone);
        }

        if (userEmail != null) {
            userDataEditor.putString(KEY_USER_TEMP_EMAIL, userEmail);
        }
        if (userFullName != null) {
            userDataEditor.putString(KEY_USER_TEMP_FULLNAME, userFullName);
        }

        if (userDateBirth != null) {
            userDataEditor.putString(KEY_USER_TEMP_DAY_BIRTH, userDateBirth);
        }

        userDataEditor.putFloat(KEY_USER_TEMP_INCOME, userIncome);
        userDataEditor.putInt(KEY_USER_TEMP_GENDER, userGender);
        userDataEditor.commit();
    }
    public void setVerifyPhone(boolean isVerify) {
        userDataEditor.putBoolean(KEY_IS_VERIFY, isVerify);
        userDataEditor.commit();
    }


    public String getVerifyPhone() {return userSession.getString(KEY_USER_PHONE_VERIFY, null);}
    public String getUserTempName() {return userSession.getString(KEY_USER_TEMP_FULLNAME, null);}
    public String getKeyUserTempEmail() {
        return  userSession.getString(KEY_USER_TEMP_EMAIL, null);
    }
    public String getKeyUserTempDayBirth() {
        return userSession.getString(KEY_USER_TEMP_DAY_BIRTH, null);
    }
    public float getUserTempInCome() {
        return userSession.getFloat(KEY_USER_TEMP_INCOME, 0);
    }
    public int getUserTempGender() {
        return userSession.getInt(KEY_USER_TEMP_GENDER, 0);
    }

    public boolean getIsVerifyPhone() {
        return userSession.getBoolean(KEY_IS_VERIFY, false);
    }

    public HashMap<String, String> getUserSession() {
        HashMap <String, String> userData = new HashMap<>();
        userData.put(KEY_USER_PHONE, userSession.getString(KEY_USER_PHONE, null));
        userData.put(KEY_USER_FULLNAME, userSession.getString(KEY_USER_FULLNAME, null));
        userData.put(KEY_USER_URI, userSession.getString(KEY_USER_URI, null));
        userData.put(KEY_USER_EMAIL, userSession.getString(KEY_USER_EMAIL, null));
        return userData;
    }

    public String getUserPhone() {
        return userSession.getString(KEY_USER_PHONE, null);
    }

    public HashMap<String, String> getUserTempData() {
        HashMap <String, String> userData = new HashMap<>();
        userData.put(KEY_USER_PHONE, userSession.getString(KEY_USER_PHONE_VERIFY, null));
        userData.put(KEY_USER_FULLNAME, userSession.getString(KEY_USER_TEMP_FULLNAME, null));
        userData.put(KEY_USER_EMAIL, userSession.getString(KEY_USER_TEMP_EMAIL, null));
        userData.put(KEY_USER_TEMP_DAY_BIRTH, userSession.getString(KEY_USER_TEMP_DAY_BIRTH, null));
        userData.put(KEY_USER_TEMP_INCOME, String.valueOf(userSession.getFloat(KEY_USER_TEMP_INCOME, 0)));
        userData.put(KEY_USER_TEMP_GENDER, String.valueOf(userSession.getInt(KEY_USER_TEMP_GENDER, 0)));
        return userData;
    }

    public void clearUserTempData() {
        userDataEditor.remove(KEY_USER_TEMP_GENDER);
        userDataEditor.remove(KEY_USER_TEMP_INCOME);
        userDataEditor.remove(KEY_USER_TEMP_DAY_BIRTH);
        userDataEditor.remove(KEY_USER_TEMP_EMAIL);
        userDataEditor.remove(KEY_USER_TEMP_FULLNAME);
        userDataEditor.remove(KEY_USER_PHONE_VERIFY);
        userDataEditor.commit();
    }

    public Uri getUserUri() {
        String userUri = userSession.getString(KEY_USER_URI, null);
        if ( userUri != null)
            return Uri.parse(userUri);
        else
            return null;
    }

    public Integer getUserRole() {
        Integer userRole = userSession.getInt(KEY_USER_ROLE, 2);
        return userRole;
    }
    public boolean didUserLogin() {
        if (userSession.getString(KEY_USER_URI, null) != null) {
            return true;
        }
        return false;
    }

    public void clearUserSession() {
        userDataEditor.clear();
        userDataEditor.commit();
    }


    //Deliver Global Data Session
    public void storeWishfulPoint(LatLng wishfulPoint, String name) {
        putDouble(globalDataEditor, KEY_WISHFUL_POINT_LAT, wishfulPoint.latitude);
        putDouble(globalDataEditor, KEY_WISHFUL_POINT_LONG, wishfulPoint.longitude);
        globalDataEditor.putString(KEY_WISHFUL_POINT_NAME, name);
        globalDataEditor.commit();

    }
    public void clearWishfulPoint() {
        globalDataEditor.remove(KEY_WISHFUL_POINT_LONG);
        globalDataEditor.remove(KEY_WISHFUL_POINT_LAT);
        globalDataEditor.remove(KEY_WISHFUL_POINT_NAME);
        globalDataEditor.commit();
    }

    public LatLng getWishfulPointLatLng() {

        //Lat and Lng default
        double lat = MapsFragment.LAND_MARK_TOWER.latitude;
        double lng = MapsFragment.LAND_MARK_TOWER.longitude;

        return new LatLng(getDouble(deliverGlobalDataSession, KEY_WISHFUL_POINT_LAT, lat),
                getDouble(deliverGlobalDataSession, KEY_WISHFUL_POINT_LONG, lng));
    }

    public String getWishfulPointName() {
        return deliverGlobalDataSession.getString(KEY_WISHFUL_POINT_NAME, null);
    }
    public float getWishfulPointArea() {
        return deliverGlobalDataSession.getFloat(KEY_WISHFUL_POINT_AREA, 0);
    }

    public int getWishfulPointPlace() {
        return deliverGlobalDataSession.getInt(KEY_WISHFUL_POINT_PLACE, 0);
    }
    public int getWishfulPointHouseType() {
        return deliverGlobalDataSession.getInt(KEY_WISHFUL_POINT_HOUSETYPE, 0);
    }
    public int getWishfulPointFloor() {
        return deliverGlobalDataSession.getInt(KEY_WISHFUL_POINT_FLOORS, 0);
    }
    public int getWishfulPointBedRooms() {
        return deliverGlobalDataSession.getInt(KEY_WISHFUL_POINT_BEDROOMS, 0);
    }
    public int getWishfulPointBathRooms() {
        return deliverGlobalDataSession.getInt(KEY_WISHFUL_POINT_BATHROOMS, 0);
    }
    public float getWishfulPointSalePrice() {
        return deliverGlobalDataSession.getFloat(KEY_WISHFUL_POINT_SALE_PRICE, 0);
    }
    public float getWishfulPointRentCost() {
        return deliverGlobalDataSession.getFloat(KEY_WISHFUL_POINT_RENT_COST, 0);
    }

    public int getWishFulPointYearBuilt() {
        return deliverGlobalDataSession.getInt(KEY_WISHFUL_POINT_YEAR_BUILT, 0);
    }

    public void initWishfulPointData(int place, int houseType, float area, int yearBuilt, int floor, int bedrooms, int bathrooms,
                                     LatLng wishfulPoint, String pointName, float salePrice, float rentCost) {
        storeWishfulPoint(wishfulPoint, pointName);
        globalDataEditor.putInt(KEY_WISHFUL_POINT_PLACE, place);
        globalDataEditor.putInt(KEY_WISHFUL_POINT_HOUSETYPE, houseType);
        globalDataEditor.putFloat(KEY_WISHFUL_POINT_AREA, area);
        globalDataEditor.putInt(KEY_WISHFUL_POINT_FLOORS, floor);
        globalDataEditor.putInt(KEY_WISHFUL_POINT_BEDROOMS, bedrooms);
        globalDataEditor.putInt(KEY_WISHFUL_POINT_BATHROOMS, bathrooms);
        globalDataEditor.putInt(KEY_WISHFUL_POINT_YEAR_BUILT, yearBuilt);
        globalDataEditor.putFloat(KEY_WISHFUL_POINT_SALE_PRICE, salePrice);
        globalDataEditor.putFloat(KEY_WISHFUL_POINT_RENT_COST, rentCost);
        globalDataEditor.putBoolean(KEY_HAS_WISHFUL_POINT, true);
        globalDataEditor.commit();
    }

    public void initWishfulPointData(int place, int houseType, float area, int yearBuilt,
                                     int floor, int bedrooms, int bathrooms, float salePrice, float rentCost) {
        globalDataEditor.putInt(KEY_WISHFUL_POINT_PLACE, place);
        globalDataEditor.putInt(KEY_WISHFUL_POINT_HOUSETYPE, houseType);
        globalDataEditor.putFloat(KEY_WISHFUL_POINT_AREA, area);
        globalDataEditor.putInt(KEY_WISHFUL_POINT_FLOORS, floor);
        globalDataEditor.putInt(KEY_WISHFUL_POINT_BEDROOMS, bedrooms);
        globalDataEditor.putInt(KEY_WISHFUL_POINT_BATHROOMS, bathrooms);
        globalDataEditor.putInt(KEY_WISHFUL_POINT_YEAR_BUILT, yearBuilt);
        globalDataEditor.putFloat(KEY_WISHFUL_POINT_SALE_PRICE, salePrice);
        globalDataEditor.putFloat(KEY_WISHFUL_POINT_RENT_COST, rentCost);
        globalDataEditor.putBoolean(KEY_HAS_WISHFUL_POINT, true);
        globalDataEditor.apply();
    }

    public boolean haveWishPointData() {
        return deliverGlobalDataSession.getBoolean(KEY_HAS_WISHFUL_POINT, false);
    }

    public void clearWishfulPointData() {
        clearWishfulPoint();
        globalDataEditor.remove(KEY_WISHFUL_POINT_PLACE);
        globalDataEditor.remove(KEY_WISHFUL_POINT_HOUSETYPE);
        globalDataEditor.remove(KEY_WISHFUL_POINT_AREA);
        globalDataEditor.remove(KEY_WISHFUL_POINT_FLOORS);
        globalDataEditor.remove(KEY_WISHFUL_POINT_BEDROOMS);
        globalDataEditor.remove(KEY_WISHFUL_POINT_BATHROOMS);
        globalDataEditor.remove(KEY_WISHFUL_POINT_YEAR_BUILT);
        globalDataEditor.remove(KEY_WISHFUL_POINT_SALE_PRICE);
        globalDataEditor.remove(KEY_WISHFUL_POINT_RENT_COST);
        expireWishfulPointData();
        globalDataEditor.commit();
    }

    public void expireWishfulPointData() {
        globalDataEditor.putBoolean(KEY_HAS_WISHFUL_POINT, false);
        globalDataEditor.commit();
    }

    //House Helper Items Fragment
    public void storeHousePointData(LatLng housePoint, String houseAddress) {
        putDouble(globalDataEditor, KEY_HOUSE_POINT_LAT, housePoint.latitude);
        putDouble(globalDataEditor, KEY_HOUSE_POINT_LONG, housePoint.longitude);
        globalDataEditor.putString(KEY_HOUSE_POINT_ADDRESS, houseAddress);
        globalDataEditor.commit();
    }

    public String getHousePointAddress() {
        return deliverGlobalDataSession.getString(KEY_HOUSE_POINT_ADDRESS, null);
    }

    public LatLng getHousePointLatLng() {
        return new LatLng(getDouble(deliverGlobalDataSession, KEY_HOUSE_POINT_LAT, 0),
                getDouble(deliverGlobalDataSession, KEY_HOUSE_POINT_LONG, 0));
    }

    public void clearHousePointData() {
        globalDataEditor.remove(KEY_HOUSE_POINT_LAT);
        globalDataEditor.remove(KEY_HOUSE_POINT_LONG);
        globalDataEditor.remove(KEY_HOUSE_POINT_ADDRESS);
        globalDataEditor.commit();
    }

    public void clearGlobalDataSession() {
        globalDataEditor.clear();
        globalDataEditor.commit();
    }



}
