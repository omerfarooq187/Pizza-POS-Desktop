package data.model

import java.time.LocalDateTime

// data/model/MenuItem.kt
data class MenuItem(
    val id: Int = 0,
    val categoryId: Int,
    val name: String,
    val description: String? = null,
    val variants: List<ItemVariant> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

