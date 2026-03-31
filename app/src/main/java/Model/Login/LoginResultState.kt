package Model.Login

sealed class LoginResultState {

    object Loading : LoginResultState()

    data class Success(val data: LoginResponse) : LoginResultState()

    data class Error(val message: String) : LoginResultState()
}