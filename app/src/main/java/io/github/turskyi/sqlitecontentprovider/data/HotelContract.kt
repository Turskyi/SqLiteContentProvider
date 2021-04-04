package io.github.turskyi.sqlitecontentprovider.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns

object HotelContract {
    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    const val CONTENT_AUTHORITY = "io.github.turskyi.sqlitecontentprovider"

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    val BASE_CONTENT_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.guests/guests/ is a valid path for
     * looking at guest data. content://com.example.android.guests/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    const val PATH_GUESTS = "guests"

    object GuestEntry : BaseColumns {
        /** The content URI to access the guest data in the provider  */
        val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GUESTS)

        /**
         * The MIME type of the [.CONTENT_URI] for a list of pets.
         */
        const val CONTENT_LIST_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GUESTS

        /**
         * The MIME type of the [.CONTENT_URI] for a single pet.
         */
        const val CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GUESTS
        const val TABLE_NAME = "guests"
        const val ID = BaseColumns._ID
        const val COLUMN_NAME = "name"
        const val COLUMN_CITY = "city"
        const val COLUMN_GENDER = "gender"
        const val COLUMN_AGE = "age"
        const val GENDER_FEMALE = 0
        const val GENDER_MALE = 1
        const val GENDER_UNKNOWN = 2

        /**
         * Returns whether or not the given gender is [.GENDER_UNKNOWN], [.GENDER_MALE],
         * or [.GENDER_FEMALE].
         */
        fun isValidGender(gender: Int): Boolean {
            return gender == GENDER_UNKNOWN || gender == GENDER_MALE || gender == GENDER_FEMALE
        }
    }
}
