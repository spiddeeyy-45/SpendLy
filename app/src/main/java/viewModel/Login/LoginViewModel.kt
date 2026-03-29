package viewModel.Login

import Model.Login.LoginRequest
import Model.Register.RegisterResultState
import Repository.Login.LoginRepository
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginViewModel : ViewModel() {

    private val repo = LoginRepository()

    private val _loginState = MutableLiveData<RegisterResultState>()
    val loginState: LiveData<RegisterResultState> = _loginState

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = RegisterResultState.Loading

            try {
                val response = repo.login(request)

                if (response.isSuccessful) {
                    _loginState.value = RegisterResultState.Success
                } else {
                    val errorBody = response.errorBody()?.string()

                    val message = try {
                        val json = JSONObject(errorBody ?: "")
                        json.optString("error", "Login failed")
                    } catch (e: Exception) {
                        "Login failed"
                    }

                    _loginState.value = RegisterResultState.Error(message)
                }

            } catch (e: Exception) {
                _loginState.value = RegisterResultState.Error(e.message ?: "Error")
            }
        }
    }
}