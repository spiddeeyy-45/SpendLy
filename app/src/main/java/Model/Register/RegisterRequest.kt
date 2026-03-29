package Model.Register

data class RegisterRequest(
    val name: String,
    val location: String,
    val email: String,
    val phone: String,
    val password: String
)


