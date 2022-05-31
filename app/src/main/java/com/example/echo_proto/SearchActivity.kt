package com.example.echo_proto

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView

//class SearchActivity: AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
//        super.onCreate(savedInstanceState, persistentState)
//        setContentView(R.layout.activity_search)
//        setSupportActionBar(findViewById(R.id.searchToolbar))
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // search
//        val searchItem: MenuItem = menu.findItem(R.id.mabSearch)
//        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        val searchView: SearchView = searchItem.actionView as SearchView
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
//
//        return super.onCreateOptionsMenu(menu)
//    }
//
//}