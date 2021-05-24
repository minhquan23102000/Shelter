package com.example.shelter.Data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.Editable;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.shelter.MainActivity;
import com.google.android.gms.maps.model.LatLng;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.regex.Pattern;

public class ShelterDBContract {

    private ShelterDBContract() {
    }

    public static final String LOG_TAG = ShelterDBContract.class.getSimpleName();
    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.shelter";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.Shelter/house/ is a valid path for
     * looking at USER data. content://com.example.Shelter/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_HOUSE = "house";
    public static final String PATH_USER = "user_";
    public static final String PATH_HOUSE_TYPE = "houseType";
    public static final String PATH_RATING = "rating";
    public static final String PATH_WISH = "wish";

    public static final class HouseEntry implements BaseColumns {
        /**
         * The content URI to access the USER data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_HOUSE);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of USERs.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HOUSE;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single USER.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HOUSE;

        /**
         * Name of database table for USER
         */
        public final static String TABLE_NAME = "house";



        /**
         * MAXIMUM NUMBER OF IMAGES THAT THE HOUSE HAS.
         */
        public final static int LIMIT_IMAGES = 15;

        /**
         * Unique ID number for the HOUSE (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the HOUSE.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_HOUSE_NAME = "house_name";

        /**
         * Address of the HOUSE.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_HOUSE_ADDRESS = "address";

        /**
         * Latitude and longitude of the HOUSE.
         * <p>
         * Type: REAL
         */
        public final static String COLUMN_HOUSE_LATITUDE = "latitude";
        public final static String COLUMN_HOUSE_LONGITUDE = "longitude";
        public final static float MAX_NEARBY_RADIUS = 7.5f; //MAX DISTANCE FOR THE WISHED HOUSE TO THE POINTER

        /**
         * Area of the HOUSE.
         * <p>
         * Type: REAL
         */
        public final static String COLUMN_HOUSE_AREA = "area";

        /**
         * Yard_size of the HOUSE.
         * DEFAULT = 0
         * Type: REAL
         */
        public final static String COLUMN_HOUSE_YARD_SIZE = "yard_size";

        /**
         * Floors of the HOUSE.
         * <p>
         * Type: INT
         */
        public final static String COLUMN_HOUSE_FLOORS = "floors";
        /**
         * bed_rooms of the HOUSE.
         * <p>
         * Type: INT
         */
        public final static String COLUMN_HOUSE_BED_ROOMS = "bed_rooms";
        /**
         * bath_rooms of the HOUSE.
         * <p>
         * Type: INT
         */
        public final static String COLUMN_HOUSE_BATH_ROOMS = "bath_rooms";
        /**
         * Year_built of the HOUSE.
         * <p>
         * Type: INT
         */
        public final static String COLUMN_HOUSE_YEAR_BUILT = "year_built";


        /**
         * rent cost and sale price of the HOUSE.
         * <p>
         * Type: REAL
         */
        public final static String COLUMN_HOUSE_RENT_COST = "rent_cost";
        public final static String COLUMN_HOUSE_SALE_PRICE = "sale_price";

        /**
         * content of the HOUSE.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_HOUSE_CONTENT = "more_info_writing";
        /**
         * day insert row of the HOUSE.
         * AUTOMATICALLY get current day
         * Type: TEXT
         */
        public final static String COLUMN_HOUSE_CREATE_DAY = "create_day";

        /**
         * HOUSE type_id of the HOUSE.
         * FOREIGN KEY OF table HouseType
         * Type: INT
         */
        public final static String COLUMN_HOUSE_TYPE_ID = "houseType_id";

        /**
         * Place of the HOUSE.
         * <p>
         * Type: INT
         */
        public final static String COLUMN_HOUSE_PLACE = "place";

        /**
         * Possible values for places of the HOUSE.
         */
        public static final int NEAR_RIVER = 1;
        public static final int IN_ALLEY = 2;
        public static final int NEAR_ROAD = 3;
        public static final int NEAR_PARK = 4;
        public static final int NEAR_MALL = 5;

        public static final String[] POSSIBLE_VALUE_PLACES = {
                "Khác",
                "Gần sông",
                "Trong hẻm",
                "Cạnh Đường",
                "Gần công viên",
                "Gần siêu thị"
        };



        /**
         * State of this house in database
         * Possible value for state
         * STATE_VISIBLE = 1
         * STATE_ABANDONED = 0
         * STATE_TRUE_DEATH = -1
         * <p>
         * Type: INT
         */
        public final static String COLUMN_HOUSE_STATE = "state";
        public final static int STATE_VISIBLE = 1;
        public final static int STATE_ABANDONED = 0;
        public final static int STATE_TRUE_DEATH = -1;

        public static String getPlaceName(int place) {
            switch (place) {
                case NEAR_RIVER:
                    return "Gần sông";
                case IN_ALLEY:
                    return "Trong hẻm";
                case NEAR_ROAD:
                    return "Cạnh đường";
                case NEAR_PARK:
                    return "Gần công viên";
                case NEAR_MALL:
                    return "Gần siêu thị";
                default:
                    return "";
            }
        }

        public static boolean isValidPlace(int place) {
            return place >= 1 && place <= 5;
        }


    }

    public static final class UserEntry implements BaseColumns {
        /**
         * The content URI to access the USER data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_USER);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of USERs.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single USER.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USER;

        /**
         * Name of database table for USER
         */
        public final static String TABLE_NAME = "user_";


        /**
         * Unique ID number for the USER (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the USER.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_USER_NAME = "name";

        /**
         * Email of the USER.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_USER_EMAIL = "email";

        /**
         * Phone of the USER.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_USER_PHONE = "phone";


        /**
         * Password of the USER.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_USER_PASSWORD = "password";


        /**
         * Social id of the USER.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_USER_SOCIAL_ID = "social_id";

        /**
         * Gender of the USER.
         * <p>
         * Type: INT
         */
        public final static String COLUMN_USER_GENDER = "gender";
        public final static int GENDER_MALE = 0;
        public final static int GENDER_FEMALE = 1;
        public final static int GENDER_OTHER = 2;

        /**
         * Date Birth of the USER.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_USER_DATE_BIRTH = "day_birth";

        /**
         * Address of the USER.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_USER_ADDRESS = "address";

        /**
         * Income of the USER.
         * <p>
         * Type: REAL, (UNIT: MILLION VND DONG)
         */
        public final static String COLUMN_USER_INCOME = "income";

        /**
         * day insert row of the USER.
         * AUTOMATICALLY get current date
         * Type: TEXT
         */
        public final static String COLUMN_USER_CREATE_DAY = "create_day";

        /**
         * Role of the USER.
         * FOREIGN KEY OF table UserRole
         * Type: INT
         */
        public final static String COLUMN_USER_ROLE_ID = "role_id";
        public final static Integer HOUSE_OWNER = 1;
        public final static Integer VIEWER = 2;


        public static String md5Crypt(final String s) {
            final String MD5 = "MD5";
            try {
                // Create MD5 Hash
                MessageDigest digest = java.security.MessageDigest
                        .getInstance(MD5);
                digest.update(s.getBytes());
                byte[] messageDigest = digest.digest();

                // Create Hex String
                StringBuilder hexString = new StringBuilder();
                for (byte aMessageDigest : messageDigest) {
                    StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                    while (h.length() < 2)
                        h.insert(0, "0");
                    hexString.append(h);
                }
                return hexString.toString();

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return "";
        }

        public static boolean isValidGender(int gender) {
            return gender >= 0 && gender <= 2;
        }

        public static boolean checkIfIsExists(String data, String column, Context context) {
            if (data == null || data.equals("") || data.length() == 0) {
                return false;
            }

            if (column.equals(COLUMN_USER_PASSWORD)) {
                data = md5Crypt(data);
            }
            Cursor cursor = null;
            ShelterDBHelper dbHelper = new ShelterDBHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String[] selectionArgs = new String[]{data};
            cursor = db.query(UserEntry.TABLE_NAME, new String[]{column}, column + "=?", selectionArgs, null, null, null);
            db.close();
            dbHelper.close();
            return cursor != null && cursor.moveToFirst();
        }

        public static boolean checkIfIsExists(String data, String column, Context context, Uri[] uri) {
            if (data == null || data.equals("") || data.length() == 0) {
                return false;
            }
            if (column.equals(COLUMN_USER_PASSWORD)) {
                data = md5Crypt(data);
            }

            Cursor cursor;
            ShelterDBHelper dbHelper = new ShelterDBHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String[] selectionArgs = new String[]{data};
            cursor = db.query(UserEntry.TABLE_NAME, new String[]{_ID, column}, column + "=?", selectionArgs, null, null, null);

            if (cursor.moveToFirst()) {
                uri[0] = ContentUris.withAppendedId(UserEntry.CONTENT_URI, cursor.getInt(cursor.getColumnIndex(_ID)));
            }
            db.close();
            dbHelper.close();
            return cursor.moveToFirst();
        }

        static public boolean isPasswordValid(@Nullable Editable text) {
            return text != null && text.length() >= 8;
        }

        static public boolean isNameValid(@Nullable Editable text) {
            return text != null && text.length() > 0;
        }

        static public boolean isEmailValid(@Nullable Editable text) {
            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                    "[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                    "A-Z]{2,7}$";

            Pattern pat = Pattern.compile(emailRegex);
            if (text == null)
                return false;
            return pat.matcher(text).matches();
        }


        static public boolean isDateBirthValid(@Nullable Editable text) {
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            String dateStr = text.toString();
            try {
                sdf.parse(dateStr);
                int currentYear = Year.now().getValue();
                String[] str = dateStr.split("/");
                int year = Integer.parseInt(str[2]);
                Log.d("SignUpFragment", "year_date_picker" + year);
                if (year > currentYear - 8 || year < currentYear - 100) {
                    return false;
                }

            } catch (NumberFormatException | ParseException ex) {

                return false;  // Returns false if parsing fails (in case of bad input).
            }

            return true; // Returns true for valid date Strings
        }

        static public boolean isIncomeValid(@Nullable Editable text) {
            if (text != null && text.length() > 0) {
                return Float.parseFloat(text.toString()) >= 0;
            }
            return false;
        }

        static public boolean isPhoneValid(@Nullable Editable text) {
            return text != null && text.length() >= 10;
        }

    }

    public static final class HouseTypeEntry implements BaseColumns {
        /**
         * The content URI to access the USER data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_HOUSE_TYPE);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of USERs.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HOUSE_TYPE;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single USER.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HOUSE_TYPE;

        /**
         * Name of database table for House
         */
        public final static String TABLE_NAME = "houseType";


        /**
         * Unique ID number for the HOUSE Type (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * House's Type name.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_HOUSE_TYPE_NAME = "name";

        /**
         * Number of house's type wishes.
         * <p>
         * Type: INT
         */
        public final static String COLUMN_HOUSE_COUNT_WISH = "count_wish";

    }

    public static final class RatingEntry implements BaseColumns {
        /**
         * The content URI to access the USER data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_RATING);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of USERs.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RATING;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single USER.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_RATING;

        /**
         * Name of database table for House
         */
        public final static String TABLE_NAME = "rating";

        /**
         * Possible value for rating
         */
        public final static Integer FAVOURITE = 20;
        public final static Integer HOUSE_OWNER = 30;
        public final static Integer SEND_CONTACT = 40;



        /**
         * Unique ID number for the rating (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * ID OF THE USER WHO RATED
         * <p>
         * Type: INT
         */
        public final static String COLUMN_USER_ID = "user_id";

        /**
         * HOUSE_ID WAS RATED
         * <p>
         * Type: INT
         */
        public final static String COLUMN_HOUSE_ID = "house_id";

        /**
         * RATING VALUE COUNT AS STAR
         * <p>
         * Type: INT
         */
        public final static String COLUMN_STARS = "stars";

        /**
         * RATING'S CONTENT
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_CONTENT = "content";

        /**
         * DATE CREATED (IT"S AUTOMATICALLY GET CURRENT DAY)
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_CREATE_DAY = "create_day";
    }

    public static final class WishEntry implements BaseColumns {
        /**
         * The content URI to access the USER data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_WISH);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of USERs.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WISH;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single USER.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WISH;

        /**
         * Name of database table for House
         */
        public final static String TABLE_NAME = "wish";



        /**
         * Unique ID number for the rating (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * ID OF THE USER WHO WISH
         * <p>
         * Type: INT
         */
        public final static String COLUMN_USER_ID = "user_id";


        /**
         * Latitude and longitude of the wish point.
         * <p>
         * Type: REAL
         */
        public final static String COLUMN_NEAR_POINT_LATITUDE = "near_point_latitude";
        public final static String COLUMN_NEAR_POINT_LONGITUDE = "near_point_longitude";

        /**
         * Area of the HOUSE.
         * <p>
         * Type: REAL
         */
        public final static String COLUMN_WISH_AREA = "area";

        /**
         * Yard_size of the HOUSE.
         * DEFAULT = 0
         * Type: REAL
         */
        public final static String COLUMN_WISH_YARD_SIZE = "yard_size";

        /**
         * Floors of the HOUSE.
         * <p>
         * Type: INT
         */
        public final static String COLUMN_WISH_FLOORS = "floors";
        /**
         * bed_rooms of the HOUSE.
         * <p>
         * Type: INT
         */
        public final static String COLUMN_WISH_BED_ROOMS = "bed_rooms";
        /**
         * bath_rooms of the HOUSE.
         * <p>
         * Type: INT
         */
        public final static String COLUMN_WISH_BATH_ROOMS = "bath_rooms";
        /**
         * Year_built of the HOUSE.
         * <p>
         * Type: INT
         */
        public final static String COLUMN_WISH_YEAR_BUILT = "year_built";




        /**
         * day insert row of the WISH.
         * AUTOMATICALLY get current DAY
         * Type: TEXT
         */
        public final static String COLUMN_WISH_CREATE_DAY = "create_day";

        /**
         * HOUSE type_id of the HOUSE.
         * FOREIGN KEY OF table HouseType
         * Type: INT
         */
        public final static String COLUMN_HOUSE_TYPE_ID = "houseType_id";

        /**
         * Place of the HOUSE.
         * <p>
         * Type: INT
         */
        public final static String COLUMN_WISH_PLACE = "place";
    }
}
