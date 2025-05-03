package data.model

import java.time.LocalDateTime

data class Order(
    val id: Int = 0,
    val items: List<OrderItem> = emptyList(),
    val total: Double = 0.0,
    val discount: Double = 0.0,
    val isMember: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
