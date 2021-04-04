package io.github.turskyi.sqlitecontentprovider.presentation

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import io.github.turskyi.sqlitecontentprovider.R
import io.github.turskyi.sqlitecontentprovider.data.HotelDbHelper

class MainActivity : AppCompatActivity() {
    private var mDbHelper: HotelDbHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        mDbHelper =  HotelDbHelper(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

}