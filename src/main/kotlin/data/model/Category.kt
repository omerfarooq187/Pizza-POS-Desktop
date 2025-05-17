package data.model

data class Category(
    val id: Int = 0,
    val name: String
)

data class CategoryWithItems(
    val category: Category,
    val items: List<MenuItem>
)