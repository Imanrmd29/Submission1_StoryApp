package com.iman.submission1_storyapp.view.add

import androidx.lifecycle.ViewModel
import com.iman.submission1_storyapp.preference.UserPreference
import kotlinx.coroutines.flow.first

class AddStoryViewModel(private val pref: UserPreference) : ViewModel() {
    suspend fun getUserToken() = pref.getUserToken().first()
}