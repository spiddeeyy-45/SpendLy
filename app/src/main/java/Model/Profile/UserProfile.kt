package Model.Profile

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val location: String = "",
    val profileImage: String = "",
    val isPremium: Boolean = false,
    val createdAt: Long = 0L
)