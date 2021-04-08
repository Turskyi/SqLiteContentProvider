package io.github.turskyi.sqlitecontentprovider.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import io.github.turskyi.sqlitecontentprovider.data.HotelContract.GuestEntry
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.annotation.Nullable

class GuestProvider : ContentProvider() {
    companion object {
        const val TAG = "===>>>"

        /**
         * URI matcher code for the content URI for the guests table
         */
        private const val GUESTS = 100

        /**
         * URI matcher code for the content URI for a single guest in the guests table
         */
        private const val GUEST_ID = 101

        /**
         * UriMatcher object to match a content URI to a corresponding code.
         * The input passed into the constructor represents the code to return for the root URI.
         * It's common to use NO_MATCH as the input for this case.
         */
        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        // Static initializer. This is run the first time anything is called from this class.
        init {
            // The calls to addURI() go here, for all of the content URI patterns that the provider
            // should recognize. All paths added to the UriMatcher have a corresponding code to return
            // when a match is found.

            // The content URI of the form "content://com.example.android.guests/guests" will map to the
            // integer code {@link #GUESTS}. This URI is used to provide access to MULTIPLE rows
            // of the guests table.
            sUriMatcher.addURI(HotelContract.CONTENT_AUTHORITY, HotelContract.PATH_GUESTS, GUESTS)

            // The content URI of the form "content://com.example.android.guests/guests/#" will map to the
            // integer code {@link #GUEST_ID}. This URI is used to provide access to ONE single row
            // of the guests table.
            //
            // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
            // For example, "content://com.example.android.guests/guests/3" matches, but
            // "content://com.example.android.guests/guests" (without a number at the end) doesn't match.
            sUriMatcher.addURI(
                HotelContract.CONTENT_AUTHORITY,
                HotelContract.PATH_GUESTS + "/#",
                GUEST_ID
            )
        }
    }

    /**
     * Database helper object
     */
    private var mDbHelper: HotelDbHelper? = null
    override fun onCreate(): Boolean {
        mDbHelper = HotelDbHelper(requireContext())
        return true
    }

    @Nullable
    override fun query(
        uri: Uri,
        projection: Array<String?>?,
        querySelection: String?,
        querySelectionArgs: Array<String?>?,
        sortOrder: String?
    ): Cursor {
        // getting access to the database for reading
        var selection = querySelection
        var selectionArgs = querySelectionArgs
        val database = mDbHelper!!.readableDatabase

        // Cursor containing the query result
        val cursor: Cursor

        // Figure out if the URI matcher can match the URI to a specific code
        when (sUriMatcher.match(uri)) {
            GUESTS ->                 // For the GUESTS code, query the guests table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the guests table.
                cursor = database.query(
                    GuestEntry.TABLE_NAME, projection, selection, selectionArgs,
                    null, null, sortOrder
                )
            GUEST_ID -> {
                // For the GUEST_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.guests/guests/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = GuestEntry.ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())

                // This will perform a query on the guests table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(
                    GuestEntry.TABLE_NAME, projection, selection, selectionArgs,
                    null, null, sortOrder
                )
            }
            else -> throw IllegalArgumentException("Cannot query unknown URI $uri")
        }
        cursor.setNotificationUri(requireContext().contentResolver, uri)
        return cursor
    }

    @Nullable
    override fun getType(uri: Uri): String {
        return when (val match = sUriMatcher.match(uri)) {
            GUESTS -> GuestEntry.CONTENT_LIST_TYPE
            GUEST_ID -> GuestEntry.CONTENT_ITEM_TYPE
            else -> throw IllegalStateException("Unknown URI $uri with match $match")
        }
    }

    @Nullable
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val match = sUriMatcher.match(uri)
        return if (match == GUESTS && values != null) insertGuest(uri, values)
        else throw IllegalArgumentException("Insertion is not supported for $uri")
    }

    override fun delete(uri: Uri, querySelection: String?, args: Array<String?>?): Int {
        // Get writeable database
        var selection = querySelection
        var selectionArgs = args
        val database = mDbHelper!!.writableDatabase

        // Track the number of rows that were deleted
        val rowsDeleted: Int
        when (sUriMatcher.match(uri)) {
            GUESTS ->                 // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(GuestEntry.TABLE_NAME, selection, selectionArgs)
            GUEST_ID -> {
                // Delete a single row given by the ID in the URI
                selection = GuestEntry.ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                rowsDeleted = database.delete(GuestEntry.TABLE_NAME, selection, selectionArgs)
            }
            else -> throw IllegalArgumentException("Deletion is not supported for $uri")
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }

        // Return the number of rows deleted
        return rowsDeleted
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        querySelection: String?,
        args: Array<String?>?
    ): Int {
        var selection = querySelection
        var selectionArgs = args
        val match = sUriMatcher.match(uri)
        return if (match == GUESTS && values != null) updateGuest(
            uri,
            values,
            selection,
            selectionArgs
        )
        else if (match == GUEST_ID && values != null) {
            // For the GUEST_ID code, extract out the ID from the URI,
            // so we know which row to update. Selection will be "_id=?" and selection
            // arguments will be a String array containing the actual ID.
            selection = GuestEntry.ID + "=?"
            selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
            updateGuest(uri, values, selection, selectionArgs)
        } else throw IllegalArgumentException("Update is not supported for $uri")
    }

    /**
     * Insert a guest into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private fun insertGuest(uri: Uri, values: ContentValues): Uri? {
        // Check that the name is not null
        values.getAsString(GuestEntry.COLUMN_NAME)
            ?: throw IllegalArgumentException("Guest requires a name")

        // Check that the gender is valid
        val gender = values.getAsInteger(GuestEntry.COLUMN_GENDER)
        require(!(gender == null || !GuestEntry.isValidGender(gender))) { "Guest requires valid gender" }

        // If the age is provided, check that it's greater than or equal to 0 kg
        val age = values.getAsInteger(GuestEntry.COLUMN_AGE)
        require(!(age != null && age < 0)) { "Guest requires valid age" }

        // No need to check the city, any value is valid (including null).

        // Get writeable database
        val database = mDbHelper!!.writableDatabase

        // Insert the new guest with the given values
        val id = database.insert(GuestEntry.TABLE_NAME, null, values)
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1L) {
            Log.e(TAG, "Failed to insert row for $uri")
            return null
        }
        requireContext().contentResolver.notifyChange(uri, null)

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id)
    }

    /**
     * Update guests in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more guests).
     * Return the number of rows that were successfully updated.
     */
    private fun updateGuest(
        uri: Uri,
        values: ContentValues,
        selection: String?,
        selectionArgs: Array<String?>?
    ): Int {
        // If the {@link GuestEntry#COLUMN_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(GuestEntry.COLUMN_NAME)) {
            values.getAsString(GuestEntry.COLUMN_NAME)
                ?: throw IllegalArgumentException("Guest requires a name")
        }

        // If the {@link GuestEntry#COLUMN_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(GuestEntry.COLUMN_GENDER)) {
            val gender = values.getAsInteger(GuestEntry.COLUMN_GENDER)
            require(!(gender == null || !GuestEntry.isValidGender(gender))) { "Guest requires valid gender" }
        }

        // If the {@link GuestEntry#COLUMN_AGE} key is present,
        // check that the age value is valid.
        if (values.containsKey(GuestEntry.COLUMN_AGE)) {
            // Check that the age is greater than or equal to 0
            val age = values.getAsInteger(GuestEntry.COLUMN_AGE)
            require(!(age != null && age < 0)) { "Guest requires valid age" }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0
        }

        // Otherwise, get writeable database to update the data
        val database = mDbHelper!!.writableDatabase

        // Perform the update on the database and get the number of rows affected
        val rowsUpdated = database.update(GuestEntry.TABLE_NAME, values, selection, selectionArgs)

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            requireContext().contentResolver.notifyChange(uri, null)
        }

        // Return the number of rows updated
        return rowsUpdated
    }
}
