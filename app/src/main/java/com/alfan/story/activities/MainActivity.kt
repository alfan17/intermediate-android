package com.alfan.story.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.alfan.story.R
import com.alfan.story.adapters.ItemAdapter
import com.alfan.story.adapters.LoadingAdapter
import com.alfan.story.data.response.LoginResponse
import com.alfan.story.data.response.StoriesResponse
import com.alfan.story.databinding.ActivityMainBinding
import com.alfan.story.library.PreferencesExt.delete
import com.alfan.story.library.PreferencesExt.getObject
import com.alfan.story.viewmodels.StoryViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val sharedPreferences: SharedPreferences by inject()

    private val storyVM: StoryViewModel by inject()

    private lateinit var mainBind: ActivityMainBinding

    private lateinit var itemAdapter: ItemAdapter

    private val listItem = ArrayList<StoriesResponse.Story>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBind.root)

        if (sharedPreferences.getObject<LoginResponse.Result>("login") == null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finishAffinity()
        }

        itemAdapter = ItemAdapter { bind, item ->
            val intent = Intent(this, DetailActivity::class.java)

            intent.putExtra("item;", item)

            startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                Pair(bind.banner, "banner_detail"),
                Pair(bind.banner, "name_detail")
            ).toBundle())
        }

        mainBind.recyclerview.layoutManager = GridLayoutManager(this@MainActivity, 2)
        mainBind.recyclerview.adapter = itemAdapter.withLoadStateFooter(
            footer = LoadingAdapter {
                itemAdapter.retry()
            }
        )

        storyVM.getList().observe(this@MainActivity) {
            itemAdapter.submitData(lifecycle, it)
        }

        lifecycleScope.launch {
            itemAdapter.loadStateFlow.collect { stateAdapter ->
                when(stateAdapter.append) {
                    LoadState.Loading -> {
                        mainBind.progressBar.visibility = View.VISIBLE
                    }
                    is LoadState.NotLoading -> {
                        mainBind.progressBar.visibility = View.INVISIBLE
                    }
                    is LoadState.Error -> {
                        mainBind.progressBar.visibility = View.INVISIBLE
                        Toast.makeText(this@MainActivity, (stateAdapter.append as LoadState.Error).error.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }

                listItem.clear()
                listItem.addAll(itemAdapter.snapshot().items)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        itemAdapter.refresh()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionmapview -> {
                val intent = Intent(this@MainActivity, MapviewActivity::class.java)
                intent.putParcelableArrayListExtra("locations", listItem)
                startActivity(intent)
            }
            R.id.actionadd -> {
                startActivity(Intent(this@MainActivity, CreateActivity::class.java))
            }
            R.id.actionlanguage -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
            R.id.actionlogout -> {
                sharedPreferences.delete("login")
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finishAffinity()
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }
}