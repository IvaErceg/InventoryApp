package com.example.android.inventoryapp.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry.isEmailValid;

public class InventoryProvider extends ContentProvider {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the inventory table
     */
    private static final int ITEMS = 100;

    /**
     * URI matcher code for the content URI for a single item in the inventory table
     */
    private static final int ITEM_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        //whole table
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS, ITEMS);
        //specific item, # is replaced by id
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                cursor = db.query(InventoryContract.InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case ITEM_ID:

                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(InventoryContract.InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return saveItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    public Uri saveItem(Uri uri, ContentValues values) {

        // Check that the name is not null or empty
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException("Item requires a name");
        }

        Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Item requires valid quantity");
        }

        Float price = values.getAsFloat(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
        if (price == null || price < 0.0) {
            throw new IllegalArgumentException("Item requires valid price");
        }

        String description = values.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_DESCRIPTION);
        if (description == null || description.equals("")) {
            throw new IllegalArgumentException("Item requires valid description");
        }

        String supplier = values.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_SUPPLIER);
        if (supplier == null || supplier.equals("")) {
            throw new IllegalArgumentException("Item requires a supplier");
        }
        if (!isEmailValid(supplier)) {
            Toast.makeText(getContext(), "Please provide valid email", Toast.LENGTH_SHORT).show();
        }


        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();


        // Insert the new product with the given values
        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the inventory content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case ITEMS:
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
            if (name == null || name.equals("")) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // Check that the count is non-negative
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Item requires valid quantity");
            }
        }

        // Check that the price is non-negative
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE)) {
            Float price = values.getAsFloat(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
            if (price == null || price < 0.00) {
                throw new IllegalArgumentException("Item requires valid price");
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_DESCRIPTION)) {
            String description = values.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_DESCRIPTION);
            if (description == null || description.equals("")) {
                throw new IllegalArgumentException("Item requires valid description");
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_ITEM_SUPPLIER)) {
            String supplier = values.getAsString(InventoryContract.InventoryEntry.COLUMN_ITEM_SUPPLIER);
            if (supplier == null || supplier.equals("")) {
                throw new IllegalArgumentException("Item requires a supplier");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }


}
