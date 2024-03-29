package com.iman.submission1_storyapp.view.main

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Pair
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.iman.submission1_storyapp.R
import com.iman.submission1_storyapp.ViewModelFactory
import com.iman.submission1_storyapp.databinding.ActivityMainBinding
import com.iman.submission1_storyapp.network.ApiConfig
import com.iman.submission1_storyapp.network.GetAllStoriesRespone
import com.iman.submission1_storyapp.preference.UserPreference
import com.iman.submission1_storyapp.showToast
import com.iman.submission1_storyapp.view.add.AddStoryActivity
import com.iman.submission1_storyapp.view.detail.DetailActivity
import com.iman.submission1_storyapp.view.login.LoginActivity
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: StoriesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rvStory.layoutManager = LinearLayoutManager(this)
        binding.rvStory.setHasFixedSize(true)
        adapter = StoriesAdapter().apply {
            onClick { story, itemListStoryBinding ->
                val optionsCompat = ActivityOptions.makeSceneTransitionAnimation(
                    this@MainActivity,
                    Pair(itemListStoryBinding.imgPoster, "image"),
                    Pair(itemListStoryBinding.tvName, "name")
                )
                Intent(this@MainActivity, DetailActivity::class.java).also { intent ->
                    intent.putExtra(DetailActivity.DATA_STORY, story)
                    startActivity(intent, optionsCompat.toBundle())
                }
            }
        }
        setupViewModel()
        setupAction()
        getNewStory()
        loadStories()
    }

    private fun setupViewModel() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[MainViewModel::class.java]
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                return true
            }
            R.id.action_logout -> {
                val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
                val scope = CoroutineScope(dispatcher)
                scope.launch {
                    mainViewModel.removeUserIsLogin()
                    mainViewModel.removeUserToken()
                    withContext(Dispatchers.Main) {
                        Intent(this@MainActivity, LoginActivity::class.java).also { intent ->
                            startActivity(intent)
                            finish()
                        }
                    }
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupAction() {
        binding.apply {
            swipeMain.setOnRefreshListener {
                swipeMain.isRefreshing = true
                loadStories()
            }
            fabAddNewStory.setOnClickListener {
                val intent = Intent(this@MainActivity, AddStoryActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun getNewStory() {
        binding.apply {
            if (intent != null) {
                val isNewStory = intent.extras?.getBoolean(SUCCESS_UPLOAD_STORY)
                if (isNewStory != null && isNewStory) {
                    swipeMain.isRefreshing = true
                    loadStories()
                    this@MainActivity.showToast(getString(R.string.story_uploaded))
                }
            }
        }
    }

    private fun loadStories() {
        binding.apply {
            val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            val scope = CoroutineScope(dispatcher)
            scope.launch {
                val token = "Bearer ${mainViewModel.getUserToken()}"
                withContext(Dispatchers.Main) {
                    val service = ApiConfig().getApiService().getAllStories(token)
                    service.enqueue(object : Callback<GetAllStoriesRespone> {
                        override fun onResponse(
                            call: Call<GetAllStoriesRespone>,
                            response: Response<GetAllStoriesRespone>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                if (responseBody != null && !responseBody.error) {
                                    adapter.stories = responseBody.stories
                                }
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    response.message(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<GetAllStoriesRespone>, t: Throwable) {
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.network_unavailable),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    })
                    rvStory.adapter = adapter
                    swipeMain.isRefreshing = false
                }

            }
        }
    }

    companion object {
        const val SUCCESS_UPLOAD_STORY = "success upload story"
    }
}