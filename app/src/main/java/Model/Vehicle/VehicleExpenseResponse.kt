package Model.Vehicle

data class VehicleExpenseResponse(
    val success: Boolean,
    val expense: Expense?
)

data class Expense(
    val id: String ="",
    val vehicle_id: String="",
    val type: String = "",
    val note:String = "",
    val selectedDate:Long = 0L,
    val amount: Double =0.0,
    val createdAt: Long? = null
)
