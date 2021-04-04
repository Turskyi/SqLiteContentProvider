package io.github.turskyi.sqlitecontentprovider.presentation

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.github.turskyi.sqlitecontentprovider.R
import io.github.turskyi.sqlitecontentprovider.data.HotelContract.GuestEntry


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var mNameEditText: EditText? = null
    private var mCityEditText: EditText? = null
    private var mAgeEditText: EditText? = null
    private var mGenderSpinner: Spinner? = null

    /**
     * Guest floor. Possible options:
     * 0 for a cat female, 1 for a cat male, 2 - undefined.
     */
    private var mGender = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button_second).setOnClickListener {
            insertGuest()
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        mNameEditText =  view.findViewById(R.id.edit_guest_name)
        mCityEditText =  view.findViewById(R.id.edit_guest_city)
        mAgeEditText = view.findViewById(R.id.edit_guest_age)
        mGenderSpinner =  view.findViewById(R.id.spinner_gender)

        setupSpinner()
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
        if (mGenderSpinner != null){
            setListAdapter(genderSpinnerAdapter)
        } else {
            mGenderSpinner =  requireView().findViewById(R.id.spinner_gender)
            setListAdapter(genderSpinnerAdapter)
        }
    }

    private fun setListAdapter(genderSpinnerAdapter: ArrayAdapter<*>) {
        mGenderSpinner!!.adapter = genderSpinnerAdapter
        mGenderSpinner!!.setSelection(2)
        mGenderSpinner!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
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
     * save the entered data in the database.
     */
    private fun insertGuest() {
        // Reading data from text fields
        val name = mNameEditText!!.text.toString().trim { it <= ' ' }

        if (name.isNotBlank()){
            val city = mCityEditText!!.text.toString().trim { it <= ' ' }
            val ageString = mAgeEditText!!.text.toString().trim { it <= ' ' }
            val age = if (ageString.isNotBlank()) ageString.toInt() else 0

            /** without content provider */
//        val mDbHelper = HotelDbHelper(requireContext())
//        val db = mDbHelper.writableDatabase
            /*****/

            val values = ContentValues()
            values.put(GuestEntry.COLUMN_NAME, name)
            values.put(GuestEntry.COLUMN_CITY, city)
            values.put(GuestEntry.COLUMN_GENDER, mGender)
            values.put(GuestEntry.COLUMN_AGE, age)

            /** without content provider */
            // insert a new row into the database and remember its identifier
//        val newRowId = db.insert(GuestEntry.TABLE_NAME, null, values)
            /*****/

            // inject a new row into the provider, returning the URI for the new guest.
            val newUri: Uri? = requireContext().contentResolver.insert(GuestEntry.CONTENT_URI, values)

            // We display a message in a successful case or in case of an error
//        if (newRowId == -1L) {
//            // If ID  -1, then there was an error
//            Toast.makeText(requireContext(), "Error while adding a guest", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(requireContext(), "The guest is registered under the number: $newRowId", Toast.LENGTH_SHORT).show()
//        }

            if (newUri == null) {
                // If null, then an error occurred while inserting.
                Toast.makeText(requireContext(), "Error while adding a guest", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(
                    requireContext(), "Guest was added successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}