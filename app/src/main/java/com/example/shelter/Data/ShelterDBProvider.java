package com.example.shelter.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.shelter.Data.ShelterDBContract.HouseEntry;
import com.example.shelter.Data.ShelterDBContract.UserEntry;
import com.example.shelter.Data.ShelterDBContract.HouseTypeEntry;
import com.example.shelter.Data.ShelterDBContract.RatingEntry;
import com.example.shelter.Data.ShelterDBContract.WishEntry;
import com.example.shelter.Data.ShelterDBContract.AlertEntry;


public class ShelterDBProvider extends ContentProvider {
    /** Tag for the log messages */
    public static final String LOG_TAG = ShelterDBProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the house table */
    private static final int HOUSES = 100;

    /** URI matcher code for the content URI for a single house in the house table */
    private static final int HOUSE_ID = 101;

    /** URI matcher code for the content URI for the user table */
    private static final int USER = 200;

    /** URI matcher code for the content URI for a single user in the user_ table */
    private static final int USER_ID = 201;

    /** URI matcher code for the content URI for the house type table */
    private static final int HOUSE_TYPE = 110;

    /** URI matcher code for the content URI for a single house type of the house type table */
    private static final int HOUSE_TYPE_ID = 111;

    /** URI matcher code for the content URI for the WISH table */
    private static final int WISH = 150;

    /** URI matcher code for the content URI for a single WISH in the WISH table */
    private static final int WISH_ID = 151;

    /** URI matcher code for the content URI for the RATING table */
    private static final int RATING = 300;

    /** URI matcher code for the content URI for a single RATING in the rating table */
    private static final int RATING_ID = 301;

    /** URI matcher code for the content URI for the RATING table */
    private static final int ALERT = 350;

    /** URI matcher code for the content URI for a single RATING in the rating table */
    private static final int ALERT_ID = 351;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.HOUSEs/HOUSEs" will map to the
        // integer code {@link #HOUSES}. This URI is used to provide access to MULTIPLE rows
        // of the HOUSEs table.
        sUriMatcher.addURI(ShelterDBContract.CONTENT_AUTHORITY, ShelterDBContract.PATH_HOUSE, HOUSES);

        // The content URI of the form "content://com.example.android.HOUSEs/HOUSEs/#" will map to the
        // integer code {@link #HOUSE_ID}. This URI is used to provide access to ONE single row
        // of the HOUSEs table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.HOUSEs/HOUSEs/3" matches, but
        // "content://com.example.android.HOUSEs/HOUSEs" (without a number at the end) doesn't match.
        sUriMatcher.addURI(ShelterDBContract.CONTENT_AUTHORITY, ShelterDBContract.PATH_HOUSE + "/#", HOUSE_ID);

        //So on we have
        //USER
        sUriMatcher.addURI(ShelterDBContract.CONTENT_AUTHORITY, ShelterDBContract.PATH_USER, USER);
        sUriMatcher.addURI(ShelterDBContract.CONTENT_AUTHORITY, ShelterDBContract.PATH_USER + "/#", USER_ID);

        //HOUSE_TYPE
        sUriMatcher.addURI(ShelterDBContract.CONTENT_AUTHORITY, ShelterDBContract.PATH_HOUSE_TYPE, HOUSE_TYPE);
        sUriMatcher.addURI(ShelterDBContract.CONTENT_AUTHORITY, ShelterDBContract.PATH_HOUSE_TYPE + "/#", HOUSE_TYPE_ID);

        //RATING
        sUriMatcher.addURI(ShelterDBContract.CONTENT_AUTHORITY, ShelterDBContract.PATH_RATING, RATING);
        sUriMatcher.addURI(ShelterDBContract.CONTENT_AUTHORITY, ShelterDBContract.PATH_RATING + "/#", RATING_ID);

        //WISH
        sUriMatcher.addURI(ShelterDBContract.CONTENT_AUTHORITY, ShelterDBContract.PATH_WISH, WISH);
        sUriMatcher.addURI(ShelterDBContract.CONTENT_AUTHORITY, ShelterDBContract.PATH_WISH + "/#", WISH_ID);

        //ALERT
        sUriMatcher.addURI(ShelterDBContract.CONTENT_AUTHORITY, ShelterDBContract.PATH_ALERT, ALERT);
        sUriMatcher.addURI(ShelterDBContract.CONTENT_AUTHORITY, ShelterDBContract.PATH_ALERT + "/#", ALERT_ID);
    }

    /** Database helper object */
    private ShelterDBHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ShelterDBHelper(getContext());
        return true;
    }



    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                         String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case HOUSES:
                // For the HOUSES code, query the HOUSEs table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the HOUSEs table.
                cursor = database.query(HouseEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case HOUSE_ID:
                // For the HOUSE_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.HOUSEs/HOUSEs/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = HouseEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the HOUSEs table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(HouseEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case USER:
                cursor = database.query(UserEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case USER_ID:
                selection = UserEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(UserEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case HOUSE_TYPE:
                cursor = database.query(HouseTypeEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case HOUSE_TYPE_ID:
                selection = HouseTypeEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(HouseTypeEntry.TABLE_NAME, projection,selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case RATING:
                cursor = database.query(RatingEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case RATING_ID:
                selection = RatingEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(RatingEntry.TABLE_NAME, projection,selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case WISH:
                cursor = database.query(WishEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case WISH_ID:
                selection = WishEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(WishEntry.TABLE_NAME, projection,selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ALERT:
                cursor = database.query(AlertEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ALERT_ID:
                selection = AlertEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(AlertEntry.TABLE_NAME, projection,selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HOUSES:
                return goInsert(uri, contentValues, HouseEntry.TABLE_NAME);
            case USER:
                return goInsert(uri, contentValues, UserEntry.TABLE_NAME);
            case RATING:
                return goInsert(uri, contentValues, RatingEntry.TABLE_NAME);
            case WISH:
                return goInsert(uri, contentValues, WishEntry.TABLE_NAME);
            case ALERT:
                return goInsert(uri, contentValues, AlertEntry.TABLE_NAME);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }



    /**
     * Insert a HOUSE into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri goInsert(Uri uri, ContentValues values, String table_name) {

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new HOUSE with the given values
        long id = database.insert(table_name, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the HOUSE content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }



    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HOUSES:
                return goUpdate(uri, contentValues, selection, selectionArgs, HouseEntry.TABLE_NAME);
            case HOUSE_ID:
                // For the HOUSE_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = HouseEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return goUpdate(uri, contentValues, selection, selectionArgs, HouseEntry.TABLE_NAME);

            case USER:
                return goUpdate(uri, contentValues, selection, selectionArgs, UserEntry.TABLE_NAME);
            case USER_ID:
                selection = UserEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return goUpdate(uri,contentValues, selection, selectionArgs, UserEntry.TABLE_NAME);

            case HOUSE_TYPE:
                return goUpdate(uri, contentValues, selection, selectionArgs, HouseTypeEntry.TABLE_NAME);
            case HOUSE_TYPE_ID:
                selection = HouseTypeEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return goUpdate(uri,contentValues, selection, selectionArgs, HouseTypeEntry.TABLE_NAME);

            case RATING:
                return goUpdate(uri, contentValues, selection, selectionArgs, RatingEntry.TABLE_NAME);
            case RATING_ID:
                selection = RatingEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return goUpdate(uri,contentValues, selection, selectionArgs, RatingEntry.TABLE_NAME);

            case WISH:
                return goUpdate(uri, contentValues, selection, selectionArgs, WishEntry.TABLE_NAME);
            case WISH_ID:
                selection = WishEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return goUpdate(uri,contentValues, selection, selectionArgs, WishEntry.TABLE_NAME);
            case ALERT:
                return goUpdate(uri, contentValues, selection, selectionArgs, AlertEntry.TABLE_NAME);
            case ALERT_ID:
                selection = AlertEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return goUpdate(uri,contentValues, selection, selectionArgs, AlertEntry.TABLE_NAME);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update HOUSEs in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more HOUSEs).
     * Return the number of rows that were successfully updated.
     */
    private int goUpdate(Uri uri, ContentValues values, String selection, String[] selectionArgs, String table_name) {

        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(table_name, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HOUSES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(HouseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case HOUSE_ID:
                // Delete a single row given by the ID in the URI
                selection = HouseEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(HouseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case USER:
                rowsDeleted = database.delete(UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case USER_ID:
                selection = UserEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case RATING:
                rowsDeleted = database.delete(RatingEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case RATING_ID:
                selection = RatingEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(RatingEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case WISH:
                rowsDeleted = database.delete(WishEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case WISH_ID:
                selection = WishEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(WishEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ALERT:
                rowsDeleted = database.delete(AlertEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ALERT_ID:
                selection = AlertEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(AlertEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HOUSES:
                return HouseEntry.CONTENT_LIST_TYPE;
            case HOUSE_ID:
                return HouseEntry.CONTENT_ITEM_TYPE;
            case USER:
                return UserEntry.CONTENT_LIST_TYPE;
            case USER_ID:
                return UserEntry.CONTENT_ITEM_TYPE;
            case RATING:
                return RatingEntry.CONTENT_LIST_TYPE;
            case RATING_ID:
                return RatingEntry.CONTENT_ITEM_TYPE;
            case WISH:
                return WishEntry.CONTENT_LIST_TYPE;
            case WISH_ID:
                return WishEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    // Check if the value if valid before insert
    boolean isValidHouseValue(ContentValues values) {
        if (values.containsKey(HouseEntry.COLUMN_HOUSE_NAME)) {
            String name = values.getAsString(HouseEntry.COLUMN_HOUSE_NAME);
            if (name == null || name.equals("")) {
                throw new IllegalArgumentException("House requires a name");
            }
        }

        if (values.containsKey(HouseEntry.COLUMN_HOUSE_PLACE)) {
            Integer place = values.getAsInteger(HouseEntry.COLUMN_HOUSE_PLACE);
            if (place == null || !HouseEntry.isValidPlace(place)) {
                throw new IllegalArgumentException("HOUSE requires valid place");
            }
        }

        if (values.containsKey(HouseEntry.COLUMN_HOUSE_PLACE)) {
            Integer area = values.getAsInteger(HouseEntry.COLUMN_HOUSE_AREA);
            if (area != null && area < 0) {
                throw new IllegalArgumentException("HOUSE requires valid weight");
            }
        }

        if (values.containsKey(HouseEntry.COLUMN_HOUSE_LATITUDE)) {
            Float latitude = values.getAsFloat(HouseEntry.COLUMN_HOUSE_LATITUDE);
            if (latitude != null && latitude < 0) {
                throw new IllegalArgumentException("HOUSE requires valid latitude");
            }
        }

        if (values.containsKey(HouseEntry.COLUMN_HOUSE_LONGITUDE)) {
            Float longitude = values.getAsFloat(HouseEntry.COLUMN_HOUSE_LONGITUDE);
            if (longitude != null && longitude < 0) {
                throw new IllegalArgumentException("HOUSE requires valid longitude");
            }
        }

        if (values.containsKey(HouseEntry.COLUMN_HOUSE_YARD_SIZE)) {
            Float yard_size = values.getAsFloat(HouseEntry.COLUMN_HOUSE_YARD_SIZE);
            if (yard_size != null && yard_size < 0) {
                throw new IllegalArgumentException("HOUSE requires valid yard_size");
            }
        }

        if (values.containsKey(HouseEntry.COLUMN_HOUSE_RENT_COST) && values.containsKey(HouseEntry.COLUMN_HOUSE_SALE_PRICE)) {
            Float rent_cost = values.getAsFloat(HouseEntry.COLUMN_HOUSE_RENT_COST);
            Float sale_price = values.getAsFloat(HouseEntry.COLUMN_HOUSE_SALE_PRICE);
            if (rent_cost == null && sale_price == null) {
                throw new IllegalArgumentException("HOUSE requires a price");
            }
            if (rent_cost < 0 || sale_price < 0) {
                throw new IllegalArgumentException("HOUSE requires valid price");
            }
        }

        if (values.containsKey(HouseEntry.COLUMN_HOUSE_BED_ROOMS)) {
            Integer bed_rooms = values.getAsInteger(HouseEntry.COLUMN_HOUSE_BED_ROOMS);
            if (bed_rooms != null || bed_rooms < 0) {
                throw new IllegalArgumentException("HOUSE requires valid bed_rooms");
            }
        }

        if (values.containsKey(HouseEntry.COLUMN_HOUSE_BATH_ROOMS)) {
            Integer bath_rooms = values.getAsInteger(HouseEntry.COLUMN_HOUSE_BATH_ROOMS);
            if (bath_rooms != null || bath_rooms < 0) {
                throw new IllegalArgumentException("HOUSE requires valid bath_rooms");
            }
        }

        if (values.containsKey(HouseEntry.COLUMN_HOUSE_YEAR_BUILT)) {
            Integer year_built = values.getAsInteger(HouseEntry.COLUMN_HOUSE_YEAR_BUILT);
            if (year_built != null || year_built < 0) {
                throw new IllegalArgumentException("HOUSE requires valid year_built");
            }
        }
           /*  NOT NEED TO CHECK HOUSE TYPE ID, BECAUSE IT HAS CONSTRAINT OF FOREIGN KEY
        Also not need to check create_day, because it automatically get current date*/
        return true;
    }


}
