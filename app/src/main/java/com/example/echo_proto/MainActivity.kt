package com.example.echo_proto

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.echo_proto.databinding.ActivityMainCoordinatorBinding
import com.example.echo_proto.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel by viewModels<MainViewModel>()
        val binding = ActivityMainCoordinatorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = getNavController()
        binding.bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _controller, destination, _args ->
            when (destination.id) {
                R.id.queueFragment -> Toast.makeText(this, "Queue Fragment", Toast.LENGTH_SHORT).show()
            }
        }

        binding.progressBar.visibility = View.VISIBLE
        val floatingStroke = findViewById<TextView>(R.id.babTvEpisodeInfo).apply {
            // need "activate" to launch text move
            isSelected = true
        }

        val searchItems = resources.getStringArray(R.array.autoCompleteSearchItems)
//        (binding.etSearchPlaceholder.editText as? MaterialAutoCompleteTextView)?.setSimpleItems(searchItems)
        // another way with adapter
//        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_items_search, searchItems)
//        binding.etSearchListAutoComplete.setAdapter(arrayAdapter)




        viewModel.rssFeed.observe(this) {
            if (it.isNotEmpty()) {
                binding.progressBar.visibility = View.GONE
                it[0].also { episode ->
                    Timber.d("item --->>>")
                    Timber.d("item.title = ${episode.title}")
                    Timber.d("item.audio = ${episode.audioLink}")
                    Timber.d("item.video = ${episode.videoLink}")
                    Timber.d("i.duration = ${episode.duration}")
                }
            }

        }

    }

    private fun getNavController(): NavController =
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment)
            .findNavController()

    // menu appbar setup
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_top_appbar, menu)

        // run search field when click at @mabSearch
        val searchItem: MenuItem = menu.findItem(R.id.mabSearch)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        val editText = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        editText.addTextChangedListener {
            val message = it.toString()
            if (message.isNotBlank()) {
                Toast.makeText(this@MainActivity, "query: $message", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mabSettings -> Toast.makeText(this, "menu: Settings", Toast.LENGTH_SHORT).show()
            R.id.mabAbout -> {}
            R.id.mabFixQuery -> {
                item.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_lock_close, null)
            }
            R.id.mabUpdateFeed -> Toast.makeText(this, "menu: Update Feed", Toast.LENGTH_SHORT).show()
            R.id.mabSearch -> {}
        }
        return true
    }

}
