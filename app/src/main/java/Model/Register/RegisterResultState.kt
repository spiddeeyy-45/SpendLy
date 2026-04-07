package Model.Register

sealed class RegisterResultState {
    object Idle : RegisterResultState()
    object Loading : RegisterResultState()

    data class Success(val userId: String) : RegisterResultState()

    data class Error(val message: String) : RegisterResultState()
}