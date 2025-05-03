package data.model


data class Pizza(
    val id: Int = 0,
    val name: String,
    val price: Double,
    val size: PizzaSize
)

enum class PizzaSize {
    SMALL,
    MEDIUM,
    LARGE
}