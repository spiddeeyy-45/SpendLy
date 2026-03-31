package Model.Vehicle

data class VehicleResponse(
    val success: Boolean,
    val vehicle: Vehicle
)
data class Vehicle(
    val id: String,
    val type: String,
    val company: String,
    val model: String,
    val number_plate: String,
    val year: Int?,
    val total_this_month: Double
)
