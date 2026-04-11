package Model.Vehicle

data class VehicleExpenseRequest(
    val vehicle_id: String,
    val type: String,
    val amount: Double,
    val selectedDate:Long,
    val note:String,
    val createdAt: Long? = null
)