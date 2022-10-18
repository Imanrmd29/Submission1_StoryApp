package com.iman.submission1_storyapp.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.iman.submission1_storyapp.network.ApiConfig
import com.iman.submission1_storyapp.network.SignUpResponse
import com.iman.submission1_storyapp.CustomDialog
import com.iman.submission1_storyapp.R
import com.iman.submission1_storyapp.databinding.ActivitySignupBinding
import com.iman.submission1_storyapp.hideSoftKeyboard
import com.iman.submission1_storyapp.showAlertLoading
import com.iman.submission1_storyapp.view.login.LoginActivity
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {
    private lateinit var loading: AlertDialog
    private lateinit var binding: ActivitySignupBinding
    private val customDialog by lazy { CustomDialog() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loading = showAlertLoading(this)
        processedAccount()
        setupView()
        onAction()
        playAnimation()
    }

    private fun playAnimation() {
        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(300)
        val nameTxtView =
            ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(300)
        val nameEditTxt =
            ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(300)
        val emailTxtView =
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(300)
        val emailEditTxt =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(300)
        val passwordTxtView =
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(300)
        val passwordEditTxt =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(300)
        val signUpButton =
            ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(300)
        val accountTxtView =
            ObjectAnimator.ofFloat(binding.haveAccountTextView, View.ALPHA, 1f).setDuration(300)
        val loginButton = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(300)

        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 5000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
        AnimatorSet().apply {
            playSequentially(
                title,
                nameTxtView,
                nameEditTxt,
                emailTxtView,
                emailEditTxt,
                passwordTxtView,
                passwordEditTxt,
                signUpButton,
                accountTxtView,
                loginButton
            )
            startDelay = 600
            start()
        }
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
    private fun processedAccount() {
        binding.apply {
            val nameStream = RxTextView.textChanges(edRegisterName)
                .skipInitialValue()
                .map {
                    edRegisterName.error != null
                }

            val emailStream = RxTextView.textChanges(edRegisterEmail)
                .skipInitialValue()
                .map {
                    edRegisterEmail.error != null
                }

            val passwordStream = RxTextView.textChanges(edRegisterPassword)
                .skipInitialValue()
                .map {
                    edRegisterPassword.error != null
                }

            val invalidFieldStream = Observable.combineLatest(
                nameStream,
                emailStream,
                passwordStream
            ) { nameInvalid, emailInvalid, passwordInvalid ->
                !nameInvalid && !emailInvalid && !passwordInvalid
            }
            invalidFieldStream.subscribe { isValid ->
                signupButton.isEnabled = isValid
            }
            signupButton.setOnClickListener { signUp() }
        }
    }

    private fun signUp() {
        binding.apply {
            hideSoftKeyboard(this@SignupActivity, binding.root)
            val name = edRegisterName.text?.trim().toString()
            val email = edRegisterEmail.text?.trim().toString()
            val password = edRegisterPassword.text?.trim().toString()
            when {
                name.isEmpty() -> {
                    nameEditTextLayout.error = getString(R.string.enter_your_name)
                }
                email.isEmpty() -> {
                    emailEditTextLayout.error = getString(R.string.enter_your_email)
                }
                password.isEmpty() -> {
                    passwordEditTextLayout.error = getString(R.string.enter_your_password)
                }
                else -> {
                    loading.show()
                    signUptoServer(name, email, password)
                }
            }
        }
    }

    private fun signUptoServer(name: String, email: String, password: String) {
        val service = ApiConfig().getApiService().signUp(name, email, password)
        service.enqueue(object : Callback<SignUpResponse> {
            override fun onResponse(
                call: Call<SignUpResponse>,
                response: Response<SignUpResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        customDialog.showSuccessCreateAccount(this@SignupActivity) {
                            Intent(
                                this@SignupActivity, LoginActivity::class.java
                            ).also { intent ->
                                startActivity(intent)
                                finishAffinity()
                            }
                        }
                        loading.dismiss()
                    }
                } else {
                    Log.e(TAG, "onResponse: " + response.message())
                    Toast.makeText(
                        this@SignupActivity,
                        getString(R.string.signup_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                    loading.dismiss()
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                Toast.makeText(
                    this@SignupActivity,
                    getString(R.string.network_unavailable),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun onAction() {
        binding.apply {
            btnLogin.setOnClickListener {
                Intent(this@SignupActivity, LoginActivity::class.java).also { intent ->
                    startActivity(intent)
                    finishAffinity()
                }
            }
        }
    }
}