package Model.Vehicle

data class VehicleStatsResponse(
    val success: Boolean,
    val stats: Stats
)

data class Stats(
    val total: Double,
    val totalLastMonth: Double,

    val fuel: Double,
    val fuelToday: Double,
    val fuelLastMonth: Double,

    val service: Double,
    val serviceToday: Double,
    val serviceLastMonth: Double,

    val avgPerDay:Double
)
