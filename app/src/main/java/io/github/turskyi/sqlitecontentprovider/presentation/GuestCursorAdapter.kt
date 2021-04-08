package io.github.turskyi.sqlitecontentprovider.presentation

import android.content.Context
import android.database.Cursor
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import io.github.turskyi.sqlitecontentprovider.R
import io.github.turskyi.sqlitecontentprovider.data.HotelContract.GuestEntry

/**
 * GuestCursorAdapter is an adapter for a list or grid view
 * that uses a Cursor of guest data as its data source. This adapter knows
 * how to create list items for each row of guest data in the Cursor.
 */
/**
 * Constructs a new [GuestCursorAdapter].
 *
 * @param context The context
 * @param cursor       The cursor from which to get the data.
 */
class GuestCursorAdapter(context: Context?, cursor: Cursor?) :
    CursorAdapter(context, cursor, 0) {
    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     * moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
    }

    /**
     * This method binds the guest data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current guest can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     * correct row.
     */
    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val nameTextView = view.findViewById<View>(R.id.name) as TextView
        val summaryTextView = view.findViewById<View>(R.id.summary) as TextView

        // Find the columns of guest attributes that we're interested in
        val nameColumnIndex = cursor.getColumnIndex(GuestEntry.COLUMN_NAME)
        val cityColumnIndex = cursor.getColumnIndex(GuestEntry.COLUMN_CITY)

        // Read the guest attributes from the Cursor for the current guest
        val guestName = cursor.getString(nameColumnIndex)
        var guestCity = cursor.getString(cityColumnIndex)

        // If the city is empty string or null, then use some default text
        // that says "Unknown", so the TextView isn't blank.
        if (TextUtils.isEmpty(guestCity)) {
            guestCity = "unknown"
        }

        // Update the TextViews with the attributes for the current guest
        nameTextView.text = guestName
        summaryTextView.text = guestCity
    }
}