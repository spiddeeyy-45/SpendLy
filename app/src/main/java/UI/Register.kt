package UI

import Model.Register.RegisterResultState
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import viewModel.Register.registerViewModel
import Model.Register.RegisterRequest
import android.content.Intent
import com.example.spendly.databinding.ActivityRegisterBinding
import com.google.firebase.messaging.FirebaseMessaging


class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: registerViewModel
    private var fcmToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[registerViewModel::class.java]

        setupUI()
        observeViewModel()
        getFcmToken()
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
        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this,Login::class.java))
            finish()
        }
        binding.alreadyacc.setOnClickListener {
            startActivity(Intent(this,Login::class.java))
            finish()
        }

        binding.btnRegister.setOnClickListener {

            val request = RegisterRequest(
                name = binding.etFullName.text.toString().trim(),
                location = binding.etLocation.text.toString().trim(),
                email = binding.etEmail.text.toString().trim(),
                phone = binding.etPhone.text.toString().trim(),
                password = binding.etPassword.text.toString().trim(),
                fcmToken=fcmToken
            )

            viewModel.registerUser(request)
        }
    }

    private fun observeViewModel() {

        viewModel.registerState.observe(this) { state ->

            when (state) {

                is RegisterResultState.Loading -> {
                    binding.btnRegister.isEnabled = false
                    binding.tvRegisterError.visibility = View.VISIBLE
                    binding.tvRegisterError.text = "Creating account..."
                }
                is RegisterResultState.Success -> {

                    binding.btnRegister.isEnabled = true

                    binding.tvRegisterError.text = "Account created successfully"
                    binding.root.postDelayed({
                        startActivity(Intent(this, AddVehicle::class.java))
                        finish()
                    }, 800)
                }

                is RegisterResultState.Error -> {
                    binding.btnRegister.isEnabled = true
                    binding.tvRegisterError.visibility = View.VISIBLE
                    binding.tvRegisterError.text = state.message
                }

                else -> Unit
            }
        }
    }
}