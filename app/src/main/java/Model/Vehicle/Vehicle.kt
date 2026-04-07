package Model.Vehicle
data class Vehicle(
    val id: String = "",
    val type: String = "",
    val company: String = "",
    val model: String = "",
    val number_plate: String = "",
    val year: Int? = null,
    val total_this_month: Double = 0.0
)