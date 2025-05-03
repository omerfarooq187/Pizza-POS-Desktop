package data.model

// data/model/ItemVariant.kt
data class ItemVariant(
    val id: Int = 0,
    val itemId: Int,
    val size: String,
    val price: Double,
    val memberPrice: Double? = null
)
