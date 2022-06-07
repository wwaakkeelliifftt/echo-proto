package com.example.echo_proto

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.echo_proto.databinding.ActivityMainBinding
import com.example.echo_proto.ui.viewmodels.FeedViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

//    private lateinit var binding: ActivityMainCoordinatorBinding
    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<FeedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
//        binding = ActivityMainCoordinatorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = this.findNavController(R.id.nav_host_fragment_container)
        binding.bottomNavigationView.setupWithNavController(navController)

        val floatingStroke = findViewById<TextView>(R.id.babTvEpisodeInfo).apply {
            // need "activate" to launch text move
            isSelected = true
        }

        val bs = BottomSheetBehavior.from(binding.bottomSheetContainer).apply {
            peekHeight = resources.getDimension(R.dimen.echo_bottom_sheet_peekHeight).toInt()
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
        binding.bottomSheetContainer.setOnClickListener {
            bs.state = BottomSheetBehavior.STATE_EXPANDED
        }


        val searchItems = resources.getStringArray(R.array.autoCompleteSearchItems)
//        (binding.etSearchPlaceholder.editText as? MaterialAutoCompleteTextView)?.setSimpleItems(searchItems)
        // another way with adapter
//        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_items_search, searchItems)
//        binding.etSearchListAutoComplete.setAdapter(arrayAdapter)

    }

    // menu appbar setup
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_top_host_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mabSettings -> Toast.makeText(this, "menu: Settings", Toast.LENGTH_SHORT).show()
            R.id.mabAbout -> Toast.makeText(this, "menu: About", Toast.LENGTH_SHORT).show()

            R.id.mabFixQuery -> {
                Toast.makeText(this, "FixQuery", Toast.LENGTH_SHORT).show()
                item.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_lock_close, null)
            }
//            R.id.mabUpdateFeed -> Toast.makeText(this, "Update list", Toast.LENGTH_SHORT).show()
        }
        return true
    }

}
