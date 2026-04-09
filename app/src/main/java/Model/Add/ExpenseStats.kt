package Model.Add

data class ExpenseStats(
    val total: Double,
    val totalLastMonth: Double,

    val grocery: Double,
    val groceryToday: Double,
    val groceryLastMonth: Double,

    val shopping: Double,
    val shoppingToday: Double,
    val shoppingLastMonth: Double,

    val food:Double,
    val foodToday: Double,
    val foodLastMonth: Double
)
