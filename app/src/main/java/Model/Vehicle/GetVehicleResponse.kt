package Model.Vehicle

data class GetVehiclesResponse(
    val success: Boolean,
    val vehicles: List<Vehicle>
)
