package Model.Login

sealed class LoginResultState {
    object Idle : LoginResultState()
    object Loading : LoginResultState()

    data class Success(
        val userId: String,
        val idToken: String
    ) : LoginResultState()

    data class Error(val message: String) : LoginResultState()
}