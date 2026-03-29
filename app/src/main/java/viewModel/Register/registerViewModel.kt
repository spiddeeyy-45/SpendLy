package viewModel.Register

import Model.Register.RegisterRequest
import Model.Register.RegisterResultState
import Repository.Register.authRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class registerViewModel : ViewModel() {

    private val repository = authRepository()

    private val _registerState = MutableLiveData<RegisterResultState>()
    val registerState: LiveData<RegisterResultState> = _registerState

    fun registerUser(request: RegisterRequest) {
        viewModelScope.launch {
            _registerState.value = RegisterResultState.Loading

            try {
                val response = repository.register(request)

                if (response.isSuccessful) {
                    _registerState.value = RegisterResultState.Success
                } else {
                    val errorBody = response.errorBody()?.string()

                    val message = try {
                        val json = JSONObject(errorBody ?: "")
                        json.optString("error", "Something went wrong")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    _registerState.value = RegisterResultState.Error(message)
                }

            } catch (e: Exception) {
                _registerState.value = RegisterResultState.Error(e.message ?: "Error")
            }
        }
    }
}