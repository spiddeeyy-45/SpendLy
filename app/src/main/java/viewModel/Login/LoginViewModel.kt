package viewModel.Login

import Model.Login.LoginRequest
import Model.Login.LoginResultState
import Repository.Login.LoginRepository
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repo = LoginRepository()

    private val _loginState = MutableLiveData<LoginResultState>(LoginResultState.Idle)
    val loginState: LiveData<LoginResultState> = _loginState

    fun login(request: LoginRequest) {

        if (request.email.isBlank()) {
            _loginState.value = LoginResultState.Error("Email cannot be empty")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(request.email).matches()) {
            _loginState.value = LoginResultState.Error("Invalid email format")
            return
        }

        if (request.password.length < 6) {
            _loginState.value = LoginResultState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginResultState.Loading

            val result = repo.login(request)

            result.onSuccess { (uid, token) ->
                _loginState.value = LoginResultState.Success(uid, token)
            }.onFailure {
                _loginState.value = LoginResultState.Error(mapFirebaseError(it))
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {

            if (email.isBlank()) {
                _loginState.value = LoginResultState.Error("Enter email first")
                return@launch
            }

            val result = repo.sendPasswordReset(email)

            result.onSuccess {
                _loginState.value = LoginResultState.Error("Reset link sent to email")
            }.onFailure {
                _loginState.value = LoginResultState.Error(mapFirebaseError(it))
            }
        }
    }

    private fun mapFirebaseError(e: Throwable): String {
        return when {
            e.message?.contains("password is invalid") == true -> "Wrong password"
            e.message?.contains("no user record") == true -> "User not found"
            e.message?.contains("badly formatted") == true -> "Invalid email"
            else -> e.message ?: "Login failed"
        }
    }
}