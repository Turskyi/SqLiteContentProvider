package io.github.turskyi.sqlitecontentprovider.presentation

import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.View.OnTouchListener
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
import io.github.turskyi.sqlitecontentprovider.R
import io.github.turskyi.sqlitecontentprovider.data.HotelContract.GuestEntry
import io.github.turskyi.sqlitecontentprovider.presentation.FirstFragment.Companion.ARG_URI


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    companion object {
        /**
         * Identifier for the guest data loader
         */
        private const val EXISTING_GUEST_LOADER = 0
    }

    private var mNameEditText: EditText? = null
    private var mCityEditText: EditText? = null
    private var mAgeEditText: EditText? = null
    private var mGenderSpinner: Spinner? = null

    /**
     * Guest floor. Possible options:
     * 0 for a cat female, 1 for a cat male, 2 - undefined.
     */
    private var mGender = 2

    /**
     * Content URI for the existing guest (null if it's a new guest)
     */
    private var mCurrentGuestUri: Uri? = null


    /** Boolean flag that keeps track of whether the guest has been edited (true) or not (false)  */
    private var mGuestHasChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            val uriString = bundle.getString(ARG_URI, "")
            val myUri = Uri.parse(uriString)

            mCurrentGuestUri = myUri

            if (mCurrentGuestUri == null) {
                setTitle("New guest")

                // Invalidate the options menu, so the "Delete" menu option can be hidden.
                // (It doesn't make sense to delete a guest that hasn't been created yet.)
                requireActivity().invalidateOptionsMenu()
            } else {
                setTitle("Data change")
                // Initialize a loader to read the guest data from the database
                // and display the current values in the editor
                LoaderManager.getInstance(this).initLoader(
                    EXISTING_GUEST_LOADER,
                    null,
                    this
                )
            }
        }

        mNameEditText = view.findViewById(R.id.edit_guest_name)
        mCityEditText = view.findViewById(R.id.edit_guest_city)
        mAgeEditText = view.findViewById(R.id.edit_guest_age)
        mGenderSpinner = view.findViewById(R.id.spinner_gender)

        initListeners(view)
        setupSpinner()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        requireActivity().menuInflater.inflate(R.menu.menu_editor, menu)
        menu.findItem(R.id.action_delete_all_entries).isVisible = false
        menu.findItem(R.id.action_insert_new_data).isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        // If this is a new guest, hide the "Delete" menu item.
        if (mCurrentGuestUri == null) {
            val menuItem = menu.findItem(R.id.action_delete)
            menuItem.isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // User clicked on a menu option in the app bar overflow menu
        when (item.itemId) {
            R.id.action_save -> {
                saveGuest()
                // Close the fragment
                findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                return true
            }
            R.id.action_delete -> {
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        val projection = arrayOf(
            GuestEntry.ID,
            GuestEntry.COLUMN_NAME,
            GuestEntry.COLUMN_CITY,
            GuestEntry.COLUMN_GENDER,
            GuestEntry.COLUMN_AGE
        )

        // This loader will execute the ContentProvider's query method on a background thread
        return CursorLoader(
            requireContext(),  // Parent activity context
            mCurrentGuestUri!!,  // Query the content URI for the current guest
            projection,  // Columns to include in the resulting Cursor
            null,  // No selection clause
            null,  // No selection arguments
            null
        ) // Default sort order
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, cursor: Cursor?) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.count < 1) {
            return
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of guest attributes that we're interested in
            val nameColumnIndex = cursor.getColumnIndex(GuestEntry.COLUMN_NAME)
            val cityColumnIndex = cursor.getColumnIndex(GuestEntry.COLUMN_CITY)
            val genderColumnIndex = cursor.getColumnIndex(GuestEntry.COLUMN_GENDER)
            val ageColumnIndex = cursor.getColumnIndex(GuestEntry.COLUMN_AGE)

            // Extract out the value from the Cursor for the given column index
            val name = cursor.getString(nameColumnIndex)
            val city = cursor.getString(cityColumnIndex)
            val gender = cursor.getInt(genderColumnIndex)
            val age = cursor.getInt(ageColumnIndex)

            // Update the views on the screen with the values from the database
            mNameEditText!!.setText(name)
            mCityEditText!!.setText(city)
            mAgeEditText!!.setText(age.toString())
            when (gender) {
                GuestEntry.GENDER_MALE -> mGenderSpinner!!.setSelection(1)
                GuestEntry.GENDER_FEMALE -> mGenderSpinner!!.setSelection(0)
                else -> mGenderSpinner!!.setSelection(2)
            }
        }
    }

   override fun onLoaderReset(loader: Loader<Cursor?>) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText!!.setText("")
        mCityEditText!!.setText("")
        mAgeEditText!!.setText("")
        mGenderSpinner!!.setSelection(2) // Select "Unknown" gender
    }

    /**
     * We configure the spinner to select the gender for the guest.
     */
    private fun setupSpinner() {
        val genderSpinnerAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.array_gender_options, android.R.layout.simple_spinner_item
        )
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        if (mGenderSpinner != null) {
            setListAdapter(genderSpinnerAdapter)
        } else {
            mGenderSpinner = requireView().findViewById(R.id.spinner_gender)
            setListAdapter(genderSpinnerAdapter)
        }
    }


    private fun initListeners(view: View) {
        view.findViewById<Button>(R.id.button_second).setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        mNameEditText!!.setOnTouchListener(mTouchListener)
        mCityEditText!!.setOnTouchListener(mTouchListener)
        mAgeEditText!!.setOnTouchListener(mTouchListener)
        mGenderSpinner!!.setOnTouchListener(mTouchListener)

        //handle on back pressed in fragment
        initOnBackPressedListener(view)
    }

    private fun initOnBackPressedListener(view: View) {
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    // If the pet hasn't changed, continue with handling back button press
                    return if (!mGuestHasChanged) {
                        true
                    } else {
                        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                        // Create a click listener to handle the user confirming that changes should be discarded.
                        val discardButtonClickListener =
                            DialogInterface.OnClickListener { _, i -> // User clicked "Discard" button, close the current activity.
                                findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                            }
                        // Show dialog that there are unsaved changes
                        showUnsavedChangesDialog(discardButtonClickListener)
                        true
                    }
                } else {
                    return false
                }
            }
        })
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     * the user confirms they want to discard their changes
     */
    private fun showUnsavedChangesDialog(
        discardButtonClickListener: DialogInterface.OnClickListener
    ) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.unsaved_changes_dialog_msg)
        builder.setPositiveButton(R.string.discard, discardButtonClickListener)
        builder.setNegativeButton(
            R.string.keep_editing
        ) { dialog, _ -> // User clicked the "Keep editing" button, so dismiss the dialog
            // and continue editing the pet.
            dialog?.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.delete_dialog_msg)
        builder.setPositiveButton(
            R.string.delete
        ) { _, _ -> // User clicked the "Delete" button, so delete the guest.
            deleteGuest()
        }
        builder.setNegativeButton(
            R.string.cancel
        ) { dialog, _ -> // User clicked the "Cancel" button, so dismiss the dialog
            // and continue editing the guest.
            dialog?.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog = builder.create()
        alertDialog.show()
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private fun deleteGuest() {
        // Only perform the delete if this is an existing guest.
        if (mCurrentGuestUri != null) {
            // Call the ContentResolver to delete the guest at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentGuestUri
            // content URI already identifies the guest that we want.
            val rowsDeleted: Int = requireContext().contentResolver.delete(
                mCurrentGuestUri!!,
                null,
                null
            )

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(
                    requireContext(), "Error while deleting guest",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(), "Guest successfully deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // close fragment
        findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
    }

    private fun setTitle(newTitle: String) {
        (activity as AppCompatActivity?)!!.supportActionBar!!.title = newTitle
    }

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mGuestHasChanged boolean to true.
     */
    private val mTouchListener = OnTouchListener { view, _ ->
        mGuestHasChanged = true
        view.performClick()
        false
    }

    private fun setListAdapter(genderSpinnerAdapter: ArrayAdapter<*>) {
        mGenderSpinner!!.adapter = genderSpinnerAdapter
        mGenderSpinner!!.setSelection(2)
        mGenderSpinner!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val selection = parent.getItemAtPosition(position) as String
                if (!TextUtils.isEmpty(selection)) {
                    mGender = when (selection) {
                        // female cat
                        getString(R.string.gender_female) -> GuestEntry.GENDER_FEMALE
                        // Male cat
                        getString(R.string.gender_male) -> GuestEntry.GENDER_MALE
                        // undefined
                        else -> GuestEntry.GENDER_UNKNOWN
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                mGender = 2 // Unknown
            }
        }
    }

    /**
     * Receiving data and save the guest in the database
     */
    private fun saveGuest() {

        // Reading data from text fields
        val name = mNameEditText!!.text.toString().trim { it <= ' ' }
        val city = mCityEditText!!.text.toString().trim { it <= ' ' }
        val ageString = mAgeEditText!!.text.toString().trim { it <= ' ' }
        //        int age = Integer.parseInt(ageString);

        // Check if this is supposed to be a new guest
        // and check if all the fields in the editor are blank
        if (mCurrentGuestUri == null &&
            TextUtils.isEmpty(name) && TextUtils.isEmpty(city) &&
            TextUtils.isEmpty(ageString) && mGender == GuestEntry.GENDER_UNKNOWN
        ) {
            // Since no fields were modified, we can return early without creating a new guest.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return
        }

        // Create a ContentValues object where column names are the keys,
        // and guest attributes from the editor are the values.
        val values = ContentValues()
        values.put(GuestEntry.COLUMN_NAME, name)
        values.put(GuestEntry.COLUMN_CITY, city)
        values.put(GuestEntry.COLUMN_GENDER, mGender)
        //        values.put(GuestEntry.COLUMN_AGE, age);


        // If the age is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        var age = 0
        if (!TextUtils.isEmpty(ageString)) {
            age = ageString.toInt()
        }
        values.put(GuestEntry.COLUMN_AGE, age)

        // Determine if this is a new or existing guest by checking if mCurrentGuestUri is null or not
        if (mCurrentGuestUri == null) {
            // This is a NEW guest, so insert a new guest into the provider,
            // returning the content URI for the new guest.
            val newUri: Uri? = requireContext().contentResolver.insert(
                GuestEntry.CONTENT_URI,
                values
            )
            if (newUri == null) {
                // Если null, значит ошибка при вставке.
                Toast.makeText(requireContext(), "Error while adding a guest", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    requireContext(), "Guest was added successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            // Otherwise this is an EXISTING guest, so update the guest with content URI: mCurrentGuestUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentGetUri will already identify the correct row in the database that
            // we want to modify.
            val rowsAffected: Int =
                requireContext().contentResolver.update(mCurrentGuestUri!!, values, null, null)

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(
                    requireContext(),
                    "Error editing guest",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(
                    requireContext(), "Data corrected successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}