package io.github.turskyi.sqlitecontentprovider.presentation

import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
import io.github.turskyi.sqlitecontentprovider.R
import io.github.turskyi.sqlitecontentprovider.data.HotelContract.GuestEntry

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    companion object {
        private const val GUEST_LOADER = 0
        const val ARG_URI = "io.github.turskyi.sqlitecontentprovider.URI"
    }

    /** Adapter for ListView  */
    var mCursorAdapter: GuestCursorAdapter? = null

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
        val guestListView: ListView = view.findViewById(R.id.list) as ListView
        initAdapter(view, guestListView)
        LoaderManager.getInstance(this).initLoader(GUEST_LOADER, null, this)
        initListeners(guestListView, view)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        // setting the required columns
        val projection = arrayOf(
            GuestEntry.ID,
            GuestEntry.COLUMN_NAME,
            GuestEntry.COLUMN_CITY
        )

        // The loader starts the ContentProvider request on a background thread
        return CursorLoader(
            requireContext(),
            GuestEntry.CONTENT_URI,  // Content provider URI for the request
            projection,  // columns that will fall into the resulting cursor
            null,  // no WHERE clause
            null,  // no arguments
            null
        ) // сортировка по умолчанию
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, data: Cursor?) {
        // Updating CursorAdapter with a new cursor that contains updated data
        mCursorAdapter!!.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {
        // Freeing up resources
        mCursorAdapter!!.swapCursor(null)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_insert_new_data -> {
                insertGuest()
                return true
            }
            R.id.action_delete_all_entries -> {
                deleteAllGuests()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initAdapter(view: View, guestListView: ListView) {
        // if list is empty
        val emptyView: View = view.findViewById(R.id.empty_view)
        guestListView.emptyView = emptyView

        // Adapter
        // if data is empty using null
        mCursorAdapter = GuestCursorAdapter(requireContext(), null)
        guestListView.adapter = mCursorAdapter
    }

    private fun insertGuest() {
        /* Create a ContentValues object where the column names are keys
         and the guest information is the key values. */
        val values = ContentValues()
        values.put(GuestEntry.COLUMN_NAME, "Murzik")
        values.put(GuestEntry.COLUMN_CITY, "Kyiv")
        values.put(GuestEntry.COLUMN_GENDER, GuestEntry.GENDER_MALE)
        values.put(GuestEntry.COLUMN_AGE, 7)
       requireContext().contentResolver.insert(GuestEntry.CONTENT_URI, values)
    }

    private fun deleteAllGuests() {
        requireContext().contentResolver.delete(GuestEntry.CONTENT_URI, null, null)
        // refresh view
        parentFragmentManager
            .beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
    }

    private fun initListeners(guestListView: ListView, view: View) {
        guestListView.onItemClickListener =
            OnItemClickListener { _, _, _, id ->

                val currentGuestUri = ContentUris.withAppendedId(GuestEntry.CONTENT_URI, id)
                val args = Bundle()
                args.putString(ARG_URI, currentGuestUri.toString())
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, args)
            }
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }
}