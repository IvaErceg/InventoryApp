package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class InventoryDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Store.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + InventoryContract.InventoryEntry.TABLE_NAME + " ("
                    + InventoryContract.InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + InventoryContract.InventoryEntry.COLUMN_ITEM_NAME + TEXT_TYPE + " NOT NULL, "
                    + InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY + INT_TYPE + " NOT NULL DEFAULT 0, "
                    + InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE + REAL_TYPE + " NOT NULL DEFAULT 0.0, "
                    + InventoryContract.InventoryEntry.COLUMN_ITEM_DESCRIPTION + TEXT_TYPE + " NOT NULL, "
                    + InventoryContract.InventoryEntry.COLUMN_ITEM_SUPPLIER + TEXT_TYPE + " NOT NULL, "
                    + InventoryContract.InventoryEntry.COLUMN_ITEM_SOLD + INT_TYPE + " NOT NULL DEFAULT 0, "
                    + InventoryContract.InventoryEntry.COLUMN_ITEM_IMAGE + TEXT_TYPE + " NOT NULL"
                    + ");";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + InventoryContract.InventoryEntry.TABLE_NAME;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}