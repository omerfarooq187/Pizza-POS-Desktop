package data.model

import org.jetbrains.exposed.dao.id.EntityID

// data/model/MenuItem.kt
data class MenuItem(
    val id: Int = 0,
    val categoryId: Int,
    val name: String,
    val description: String? = null,
    val variants: List<ItemVariant> = emptyList(),
    val isActive: Boolean = true,
    val discountType: DiscountType? = null,
    val discountValue: Double? = null
)

// data/model/ItemVariant.kt
data class ItemVariant(
    val size: String,  // "Small", "Medium", etc.
    val price: Double,
    val memberPrice: Double? = null
)

enum class DiscountType {
    PERCENTAGE,
    FIXED
}