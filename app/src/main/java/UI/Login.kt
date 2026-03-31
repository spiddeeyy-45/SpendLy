package UI

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

import Model.Login.LoginRequest
import Model.Login.LoginResultState
import viewModel.Login.LoginViewModel
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.spendly.databinding.ActivityLoginBinding
import com.google.firebase.messaging.FirebaseMessaging

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private var fcmToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        getFcmToken()
        setupUI()
        observeVM()
    }

    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            fcmToken = it
        }
    }

    private fun setupUI() {

        binding.tvShowPassword.setOnClickListener {
            val etPassword = binding.etPassword

            if (etPassword.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.tvShowPassword.text = "Hide"
            } else {
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.tvShowPassword.text = "Show"
            }
            etPassword.setSelection(etPassword.text.length)
        }
        binding.btnLogin.setOnClickListener {

            val request = LoginRequest(
                email = binding.etEmail.text.toString().trim(),
                password = binding.etPassword.text.toString().trim(),
                fcmToken = fcmToken
            )
            viewModel.login(request)
        }
    }

    private fun observeVM() {
        viewModel.loginState.observe(this) { state ->

            when (state) {
                is LoginResultState.Loading -> {
                    binding.tvLoginError.visibility = View.VISIBLE
                    binding.tvLoginError.text = "Logging in..."
                }

                is LoginResultState.Success -> {

                    val response = state.data
                    val token = response.token

                    if (!token.isNullOrEmpty()) {
                        val prefs = getSharedPreferences("app", Context.MODE_PRIVATE)
                        prefs.edit().putString("token", token).apply()
                        Log.d("LOGIN_SUCCESS", "Token saved: $token")
                        val intent = Intent(this, addVehicle::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        binding.tvLoginError.visibility = View.VISIBLE
                        binding.tvLoginError.text = "Login failed: token missing"
                    }
                }

                is LoginResultState.Error -> {
                    binding.tvLoginError.visibility = View.VISIBLE
                    binding.tvLoginError.text = state.message
                }
            }
        }
    }
}