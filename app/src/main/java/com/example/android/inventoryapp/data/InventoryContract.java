package com.example.android.inventoryapp.data;

import android.provider.BaseColumns;

/**
 * Created by Iva on 27.12.2016..
 */

public class InventoryContract {
    private InventoryContract(){
        throw new AssertionError("This class shouldn't be instantiated");
    }
    /* Inner class that defines the table contents */
    public static final class InventoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_QUANTITY = "quantity";
        public static final String COLUMN_ITEM_DESCRIPTION = "description";
        public static final String COLUMN_ITEM_IMAGE = "image";
    }
}

