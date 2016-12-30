package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;


public class InventoryCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
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
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView nameView = (TextView) view.findViewById(R.id.name);
        TextView priceView = (TextView) view.findViewById(R.id.price);
        TextView quantityView = (TextView) view.findViewById(R.id.quantity);
        TextView soldView = (TextView) view.findViewById(R.id.sold);
        Button sell = (Button) view.findViewById(R.id.sell_button);
        // Extract properties from cursor
        int id = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));
        final Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME));
        float price = cursor.getFloat(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE));
        String productPrice = context.getString(R.string.dollar_sign) + price + context.getString(R.string.per_item);
        final int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY));
        final int sold = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_ITEM_SOLD));

        String quantityInStock = quantity + context.getString(R.string.in_stock);
        String itemsSold = context.getString(R.string.sold) + sold;

        nameView.setText(name);
        quantityView.setText(quantityInStock);
        priceView.setText(productPrice);
        soldView.setText(itemsSold);
        //update quantity and sold on button click
        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                if (quantity > 0) {
                    int newQuantity = quantity - 1;
                    int soldItems = sold + 1;
                    values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY, newQuantity);
                    values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_SOLD, soldItems);
                    view.getContext().getContentResolver().update(
                            currentItemUri,
                            values,
                            null,
                            null
                    );
                    context.getContentResolver().notifyChange(currentItemUri, null);
                } else {
                    Toast.makeText(context, R.string.out_of_stock, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}