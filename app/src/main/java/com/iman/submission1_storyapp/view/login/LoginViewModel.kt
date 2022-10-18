package com.iman.submission1_storyapp.view.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iman.submission1_storyapp.preference.UserPreference
import kotlinx.coroutines.launch

class LoginViewModel(private val pref: UserPreference) : ViewModel() {
    fun saveUserToken(token: String) {
        viewModelScope.launch {
            pref.saveUserToken(token)
        }
    }

    fun saveUserSession(isLogin: Boolean) {
        viewModelScope.launch {
            pref.saveUserIsLogin(isLogin)
        }
    }
}