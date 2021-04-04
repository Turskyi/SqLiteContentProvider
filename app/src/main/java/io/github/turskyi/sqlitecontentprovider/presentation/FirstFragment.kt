package io.github.turskyi.sqlitecontentprovider.presentation

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.github.turskyi.sqlitecontentprovider.R
import io.github.turskyi.sqlitecontentprovider.data.HotelContract.GuestEntry
import io.github.turskyi.sqlitecontentprovider.data.HotelDbHelper


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onStart() {
        super.onStart()
        displayDatabaseInfo()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_insert_new_data -> {
                insertGuest()
                displayDatabaseInfo()
                return true
            }
            R.id.action_delete_all_entries -> {
                deleteTable()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun insertGuest() {
        /** method without content provider */
//        val mDbHelper = HotelDbHelper(requireContext())
        // Gets the database in write mode
//        val db = mDbHelper.writableDatabase
        /********/
        /* Create a ContentValues object where the column names are keys
         and the guest information is the key values. */
        val values = ContentValues()
        values.put(GuestEntry.COLUMN_NAME, "Murzik")
        values.put(GuestEntry.COLUMN_CITY, "Kyiv")
        values.put(GuestEntry.COLUMN_GENDER, GuestEntry.GENDER_MALE)
        values.put(GuestEntry.COLUMN_AGE, 7)

        /** method without content provider */
//        val newRowId = db.insert(GuestEntry.TABLE_NAME, null, values).toInt()

        /** another method without content provider */
//        db = DatabaseHelper(this)
//        val sqdb: SQLiteDatabase = db.getWritableDatabase()
//
//        val insertQuery = "INSERT INTO " +
//                DatabaseHelper.DATABASE_TABLE.toString() +
//                " (" + DatabaseHelper.CAT_NAME_COLUMN.toString() + ") VALUES ('Васька')"
//        sqdb.execSQL(insertQuery)

        // We display a message in a successful case or in case of an error
//        if (newRowId == -1) {
//            // Если ID  -1, значит произошла ошибка
//            Toast.makeText(requireContext(), "Error while adding a guest", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(
//                requireContext(),
//                "The guest is registered under the number: " + newRowId,
//                Toast.LENGTH_SHORT
//            ).show();
//        }

        /********/
        val newUri: Uri? = requireContext().contentResolver.insert(GuestEntry.CONTENT_URI, values)
    }

    private fun deleteTable() {
        val mDbHelper = HotelDbHelper(requireContext())
        // Gets the database in write mode
        val db = mDbHelper.writableDatabase
        db.delete(GuestEntry.TABLE_NAME, null, null)
        // refresh view
        parentFragmentManager
            .beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
    }

    private fun displayDatabaseInfo() {

        /** method without content provider */
        // Create and open a database for reading
//        val mDbHelper = HotelDbHelper(requireContext())
//        val db = mDbHelper.readableDatabase

        val selection: String = GuestEntry.ID + ">?"
        val selectionArgs = arrayOf("1")
/*
        // Making a request
        val cursor: Cursor = db.query(
            HotelContract.GuestEntry.TABLE_NAME,  // таблица
            projection,  // столбцы
            selection,             // столбцы для условия WHERE
            selectionArgs,         // значения для условия WHERE
            null,  // Don't group the rows
            null,  // Don't filter by row groups
            HotelContract.GuestEntry.COLUMN_AGE + " DESC" // the sort order
        )
        */
        /** another method without content provider */
// Method 2: Raw SQL-query
//        val query = ("SELECT " + DatabaseHelper.COLUMN_ID.toString() + ", "
//                + DatabaseHelper.CAT_NAME_COLUMN.toString() + " FROM " + DatabaseHelper.TABLE_NAME)
//        val cursor2: Cursor = mDatabase.rawQuery(query, null)
//        while (cursor2.moveToNext()) {
//            val id = cursor2.getInt(
//                cursor2
//                    .getColumnIndex(DatabaseHelper.COLUMN_ID)
//            )
//            val name = cursor2.getString(
//                cursor2
//                    .getColumnIndex(DatabaseHelper.CAT_NAME_COLUMN)
//            )
//            Log.i("LOG_TAG", "ROW $id HAS NAME $name")
//        }
//        cursor2.close()

        /********/

        // Set a condition for the selection - a list of columns
        val projection = arrayOf(
            GuestEntry.ID,
            GuestEntry.COLUMN_NAME,
            GuestEntry.COLUMN_CITY,
            GuestEntry.COLUMN_GENDER,
            GuestEntry.COLUMN_AGE
        )

        val displayTextView = requireView().findViewById<View>(R.id.textview_first) as TextView

        requireContext().contentResolver.query(
            GuestEntry.CONTENT_URI,
            projection,
            null,
            null,
            null
        ).use { cursor ->
            if (cursor != null) {
                val guestCount: Int = cursor.count
                val title = String.format("The table contains %1$2d guests.\n\n", guestCount)
                displayTextView.text = title

                displayTextView.append(
                    GuestEntry.ID + " - " +
                            GuestEntry.COLUMN_NAME + " - " +
                            GuestEntry.COLUMN_CITY + " - " +
                            GuestEntry.COLUMN_GENDER + " - " +
                            GuestEntry.COLUMN_AGE + "\n"
                )

                // Find out the index of each column
                val idColumnIndex: Int = cursor.getColumnIndex(GuestEntry.ID)
                val nameColumnIndex: Int = cursor.getColumnIndex(GuestEntry.COLUMN_NAME)
                val cityColumnIndex: Int = cursor.getColumnIndex(GuestEntry.COLUMN_CITY)
                val genderColumnIndex: Int = cursor.getColumnIndex(GuestEntry.COLUMN_GENDER)
                val ageColumnIndex: Int = cursor.getColumnIndex(GuestEntry.COLUMN_AGE)

                // We go through all the rows
                while (cursor.moveToNext()) {
                    // Using an index to get a string or number
                    val currentID: Int = cursor.getInt(idColumnIndex)
                    val currentName: String = cursor.getString(nameColumnIndex)
                    val currentCity: String = cursor.getString(cityColumnIndex)
                    val currentGender: Int = cursor.getInt(genderColumnIndex)
                    val currentAge: Int = cursor.getInt(ageColumnIndex)
                    // Displaying the values of each column
                    displayTextView.append(
                        (("\n" + currentID + " - " +
                                currentName + " - " +
                                currentCity + " - " +
                                currentGender + " - " +
                                currentAge))
                    )
                }
            } else {
//                TODO: handle error
                throw Exception("cursor is null ")
            }
        }
    }
}