package Util.Home

data class TransactionUIModel(
    val id: String,
    val title: String,
    val type: String,
    val amount: Double,
    val date: Long
)