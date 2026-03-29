package Model.Login

data class LoginRequest(
    val email: String,
    val password: String,
    val fcmToken: String? = null
)
