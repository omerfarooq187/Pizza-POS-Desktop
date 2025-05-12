package data.model

import org.joda.time.DateTime

// data/model/Order.kt
data class Order(
    val id: Int = 0,
    val customerName: String = "",
    val phone: String = "",
    val email: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val memberId: Int? = null,
    val isMember: Boolean = false,
    val status: OrderStatus = OrderStatus.ACTIVE,
    val createdAt: DateTime = DateTime.now()
)

data class OrderItem(
    val id: Int = 0,
    val itemId: Int,
    val variantSize: String,
    val quantity: Int,
    val price: Double,
    val memberPriceApplied: Boolean = false,
    val discountApplied: Double = 0.0
)

enum class OrderStatus { ACTIVE, COMPLETED, CANCELLED }
