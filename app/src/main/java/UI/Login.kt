package UI

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

import Model.Login.LoginRequest
import viewModel.Login.LoginViewModel
import Model.Register.RegisterResultState
import android.content.Intent
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
                is RegisterResultState.Loading -> {
                    binding.tvLoginError.visibility = View.VISIBLE
                    binding.tvLoginError.text = "Logging in..."
                }

                is RegisterResultState.Success -> {
                    binding.tvLoginError.text = "Login Success"
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()

                }

                is RegisterResultState.Error -> {
                    binding.tvLoginError.visibility = View.VISIBLE
                    binding.tvLoginError.text = state.message
                }
            }
        }
    }
}