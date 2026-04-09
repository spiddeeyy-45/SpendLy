package Model.Vehicle

data class VehicleExpenseRequest(
    val vehicle_id: String,
    val type: String,
    val amount: Double,
    val date:Long,
    val note:String
)