package data.model

import java.time.LocalDate

data class DailySale(
    val date: LocalDate,
    val totalSales: Double,
    val totalOrders: Int,
    val mostSoldItem: String
)
