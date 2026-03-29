package Model.Login

data class LoginResponse(
    val message: String?,
    val token: String?,
    val user: Any?,
    val error: String? = null
)
