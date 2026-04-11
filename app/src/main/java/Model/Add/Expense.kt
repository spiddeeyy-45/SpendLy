package Model.Add
data class Expense(
    val id: String = "",
    val type: String = "",
    val amount: Double = 0.0,
    val note: String = "",
    val selectedDate: Long = 0L,
    val createdAt: Long? = null
)
