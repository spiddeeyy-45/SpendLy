package Model.Vehicle

data class AddVehicleRequest(
    val type: String,
    val company: String,
    val model: String,
    val number_plate: String,
    val year: Int?
)
