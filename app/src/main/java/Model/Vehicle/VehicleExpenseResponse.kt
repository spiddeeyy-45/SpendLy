package Model.Vehicle

data class VehicleExpenseResponse(
    val success: Boolean,
    val expense: Expense?
)

data class Expense(
    val id: String,
    val vehicle_id: String,
    val type: String,
    val amount: Double
)
