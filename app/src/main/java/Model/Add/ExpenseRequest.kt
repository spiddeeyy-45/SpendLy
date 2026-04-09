package Model.Add

data class ExpenseRequest(
    val type:String,
    val amount:Double,
    val note:String,
    val date:Long
)
