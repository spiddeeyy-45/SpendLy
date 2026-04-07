package viewModel.Register

import Model.Register.RegisterRequest
import Model.Register.RegisterResultState
import Repository.Register.authRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class registerViewModel : ViewModel() {

    private val repository = authRepository()

    private val _registerState = MutableLiveData<RegisterResultState>(RegisterResultState.Idle)
    val registerState: LiveData<RegisterResultState> = _registerState

    fun registerUser(request: RegisterRequest) {

        if (!request.name.matches(Regex("^[A-Za-z ]{3,50}$"))) {
            _registerState.value =
                RegisterResultState.Error("Name should contain only letters (min 3 chars)")
            return
        }
        if (!request.location.matches(Regex("^[A-Za-z ]{2,50}$"))) {
            _registerState.value =
                RegisterResultState.Error("Enter valid city name (India only)")
            return
        }
        if (!isValidEmail(request.email)) {
            _registerState.value =
                RegisterResultState.Error("Enter a valid email address")
            return
        }
        val cleanPhone = request.phone.replace("\\s".toRegex(), "")

        if (!cleanPhone.matches(Regex("^(\\+91)?[6-9][0-9]{9}$"))) {
            _registerState.value =
                RegisterResultState.Error("Enter valid Indian phone number")
            return
        }
        val passwordValidation = validatePassword(request.password)
        if (passwordValidation != null) {
            _registerState.value = RegisterResultState.Error(passwordValidation)
            return
        }
        viewModelScope.launch {

            _registerState.value = RegisterResultState.Loading

            val result = repository.register(request)

            result.onSuccess { uid ->
                _registerState.value = RegisterResultState.Success(uid)
            }.onFailure {
                _registerState.value =
                    RegisterResultState.Error(mapFirebaseError(it))
            }
        }
    }
    private fun validatePassword(password: String): String? {

        if (password.length < 8) return "Password must be at least 8 characters"

        if (!password.contains(Regex("[A-Z]")))
            return "Password must contain at least 1 uppercase letter"

        if (!password.contains(Regex("[a-z]")))
            return "Password must contain at least 1 lowercase letter"

        if (!password.contains(Regex("[0-9]")))
            return "Password must contain at least 1 number"

        if (!password.contains(Regex("[@#\$%^&+=!]")))
            return "Password must contain at least 1 special character"

        return null
    }
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        )
        return email.matches(emailRegex)
    }
    private fun mapFirebaseError(e: Throwable): String {
        return when {
            e.message?.contains("email address is already in use") == true ->
                "This email is already registered"

            e.message?.contains("weak-password") == true ->
                "Password is too weak"

            e.message?.contains("badly formatted") == true ->
                "Invalid email format"

            e.message?.contains("network error") == true ->
                "Check your internet connection"

            else -> e.message ?: "Registration failed. Try again"
        }
    }
}