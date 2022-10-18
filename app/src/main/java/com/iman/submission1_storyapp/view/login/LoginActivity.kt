package com.iman.submission1_storyapp.view.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.iman.submission1_storyapp.*
import com.iman.submission1_storyapp.network.ApiConfig
import com.iman.submission1_storyapp.network.LoginResponse
import com.iman.submission1_storyapp.databinding.ActivityLoginBinding
import com.iman.submission1_storyapp.preference.UserPreference
import com.iman.submission1_storyapp.view.main.MainActivity
import com.iman.submission1_storyapp.view.signup.SignupActivity
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loading: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loading = showAlertLoading(this)
        setupViewModel()
        setupView()
        onAction()
        processedLogin()
    }

    private fun setupViewModel() {
        loginViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[LoginViewModel::class.java]
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    @SuppressLint("CheckResult")
    private fun processedLogin() {
        binding.apply {
            val emailStream = RxTextView.textChanges(edLoginEmail)
                .skipInitialValue()
                .map {
                    edLoginEmail.error != null
                }

            val passwordStream = RxTextView.textChanges(edLoginPassword)
                .skipInitialValue()
                .map {
                    edLoginPassword.error != null
                }

            val invalidFieldStream = Observable.combineLatest(
                emailStream,
                passwordStream
            ) { emailInvalid, passwordInvalid ->
                !emailInvalid && !passwordInvalid
            }

            invalidFieldStream.subscribe { isValid ->
                if (isValid) loginButton.enable() else loginButton.disable()
            }

            loginButton.setOnClickListener {
                if (validate()) {
                    loading.show()
                    login()
                }
            }

        }
    }

    private fun login() {
        hideSoftKeyboard(this@LoginActivity, binding.root)
        val email = binding.edLoginEmail.text?.trim().toString()
        val password = binding.edLoginPassword.text?.trim().toString()
        val service = ApiConfig().getApiService().login(email, password)
        service.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        loginViewModel.saveUserToken(responseBody.loginResult.token)
                        loginViewModel.saveUserSession(true)
                        Intent(this@LoginActivity, MainActivity::class.java).also { intent ->
                            startActivity(intent)
                            finishAffinity()
                        }
                    }
                    loading.dismiss()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                    loading.dismiss()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.network_unavailable),
                    Toast.LENGTH_SHORT
                )
                    .show()
                loading.dismiss()
            }
        })
    }

    private fun validate(): Boolean {
        val valid: Boolean?
        val email = binding.edLoginEmail.text?.trim().toString()
        val password = binding.edLoginPassword.text?.trim().toString()
        when {
            email.isEmpty() -> {
                binding.emailEditTextLayout.error = getString(R.string.enter_your_email)
                valid = FALSE
            }
            password.isEmpty() -> {
                binding.passwordEditTextLayout.error = getString(R.string.enter_your_password)
                valid = FALSE
            }
            else -> {
                valid = TRUE
                binding.emailEditTextLayout.error = null
                binding.passwordEditTextLayout.error = null
            }
        }
        return valid
    }

    private fun onAction() {
        binding.apply {
            btnRegister.setOnClickListener {
                Intent(this@LoginActivity, SignupActivity::class.java).also { intent ->
                    startActivity(intent)
                }
            }
        }
    }

    override fun onBackPressed() {
        val finish = Intent(Intent.ACTION_MAIN)
        finish.addCategory(Intent.CATEGORY_HOME)
        finish.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(finish)
    }
}