package Model.Register

sealed class RegisterResultState {
    object Loading : RegisterResultState()
    object Success : RegisterResultState()
    data class Error(val message: String) : RegisterResultState()
}