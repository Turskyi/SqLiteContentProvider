package io.github.turskyi.sqlitecontentprovider.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import io.github.turskyi.sqlitecontentprovider.data.HotelContract.GuestEntry

/**
 * Constructor of the [HotelDbHelper].
 *
 * @param context application context
 */
class HotelDbHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME,
    null,
    DATABASE_VERSION,
) {

    companion object {

        /**
         * Database file name
         */
        private const val DATABASE_NAME = "hotel.db"

        /**
         * Database version. When changing the schema, increase by one
         */
        private const val DATABASE_VERSION = 1
    }

    /**
     * Called when the database is created
     */
    override fun onCreate(db: SQLiteDatabase) {
        // The string to create the table
        val sqlCreateGuestsTable: String =
            ("CREATE TABLE " + GuestEntry.TABLE_NAME + " ("
                    + GuestEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + GuestEntry.COLUMN_NAME + " TEXT NOT NULL, "
                    + GuestEntry.COLUMN_CITY + " TEXT NOT NULL, "
                    + GuestEntry.COLUMN_GENDER + " INTEGER NOT NULL DEFAULT 3, "
                    + GuestEntry.COLUMN_AGE + " INTEGER NOT NULL DEFAULT 0);")

        // Start creating the table.
        db.execSQL(sqlCreateGuestsTable)
    }

    /**
     * Called when the database schema is updated
     */
    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Let's write to the log.
        Log.w(
            "SQLite",
            "Updating from version $oldVersion to version $newVersion."
        )

        // Delete the old table and create a new one
        db.execSQL("DROP TABLE IF IT EXISTS " + GuestEntry.TABLE_NAME)
        // Create a new table
        onCreate(db)
    }
}
