package viewModel.Login

import Model.Login.LoginRequest
import Model.Login.LoginResultState
import Model.Register.RegisterResultState
import Repository.Login.LoginRepository
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginViewModel : ViewModel() {

    private val repo = LoginRepository()

    private val _loginState = MutableLiveData<LoginResultState>()
    val loginState: LiveData<LoginResultState> = _loginState

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = LoginResultState.Loading

            try {
                val response = repo.login(request)

                if (response.isSuccessful &&response.body()!=null) {
                    val data =response.body()!!
                    _loginState.value = LoginResultState.Success(data)
                } else {
                    val errorBody = response.errorBody()?.string()

                    val message = try {
                        val json = JSONObject(errorBody ?: "")
                        json.optString("error", "Login failed")
                    } catch (e: Exception) {
                        "Login failed"
                    }

                    _loginState.value = LoginResultState.Error(message)
                }

            } catch (e: Exception) {
                _loginState.value = LoginResultState.Error(e.message ?: "Error")
            }
        }
    }
}