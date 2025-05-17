package presentation.viewmodel

import androidx.compose.runtime.*
import data.model.*
import data.repository.CategoryRepository
import data.repository.MenuRepository
import data.repository.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OrderViewModel : KoinComponent {
    // Current Order State
    private val _currentOrder = mutableStateOf(Order())
    val currentOrder: State<Order> = _currentOrder

    var menuCategories by mutableStateOf(emptyList<CategoryWithItems>())

    private val orderRepo: OrderRepository by inject()
    val menuRepo: MenuRepository by inject()
    private val categoryRepo: CategoryRepository by inject()
    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    // Member state (simple flag since we don't have member class)

    private val _isMember = mutableStateOf(false)
    val isMember: State<Boolean> get() = _isMember


    init {
        loadMenuCategories()
    }
    fun addItemToOrder(item: MenuItem, variant: ItemVariant, newQuantity: Int) {
        val finalPrice = calculateMemberPrice(item, variant)

        val existingItemIndex = _currentOrder.value.items.indexOfFirst {
            it.itemId == item.id && it.variantSize == variant.size
        }

        val updatedItems = if (existingItemIndex != -1) {
            // ADD TO EXISTING QUANTITY
            _currentOrder.value.items.toMutableList().apply {
                val existing = this[existingItemIndex]
                this[existingItemIndex] = existing.copy(
                    quantity = existing.quantity + newQuantity, // Add new quantity to existing
                    price = finalPrice
                )
            }
        } else {
            // Add new item
            _currentOrder.value.items + OrderItem(
                itemId = item.id,
                variantSize = variant.size,
                quantity = newQuantity,
                price = finalPrice,
                memberPriceApplied = isMember.value && isItemEligibleForDiscount(item),
                itemName = item.name
            )
        }

        // Calculate new total and update state
        val newTotal = updatedItems.sumOf { it.price * it.quantity }
        _currentOrder.value = _currentOrder.value.copy(
            items = updatedItems,
            totalAmount = newTotal
        )

        // Reset quantity for this variant
        updateQuantity(item.id, variant.size, 1)
    }

    private fun calculateMemberPrice(item: MenuItem, variant: ItemVariant): Double {
        return if (_isMember.value && isItemEligibleForDiscount(item)) {
            variant.memberPrice ?: (variant.price * 0.5) // Use memberPrice if available
        } else {
            variant.price
        }
    }



    private fun isItemEligibleForDiscount(item: MenuItem): Boolean {
        // Add your business logic for eligible items
        // Example: Only apply discount to specific categories
        return item.categoryId == 1 // Example: Pizza category
    }

    private suspend fun recalculatePrices() {
        try {
            val updatedItems = _currentOrder.value.items.mapNotNull { orderItem ->
                try {
                    val menuItem = menuRepo.getItemById(orderItem.itemId)
                    val variant = menuItem.variants.first { it.size == orderItem.variantSize }

                    orderItem.copy(
                        price = calculateMemberPrice(menuItem, variant),
                        memberPriceApplied = isMember.value && isItemEligibleForDiscount(menuItem)
                    )
                } catch (e: Exception) {
                    // Handle missing items gracefully
                    null
                }
            }

            _currentOrder.value = _currentOrder.value.copy(
                items = updatedItems,
                totalAmount = updatedItems.sumOf { it.price * it.quantity }
            )
        } catch (e: Exception) {
            // Handle any errors in price recalculation
            e.printStackTrace()
        }
    }

    fun finalizeOrder() {
        coroutineScope.launch {
            // Add contact info with defaults
            val finalOrder = _currentOrder.value.copy(
                isMember = isMember.value,
                phone = _currentOrder.value.phone.trim().ifEmpty { "N/B" },
                email = _currentOrder.value.email.trim().ifEmpty { "N/A" },
                customerName = _currentOrder.value.customerName.trim().ifEmpty { "N/A" }
            )

            orderRepo.createOrder(finalOrder)
            _currentOrder.value = Order() // Reset order
            _isMember.value = false
        }
    }

    fun updateContactInfo(name: String, phone: String, email: String) {
        _currentOrder.value = _currentOrder.value.copy(
            customerName = name,
            phone = phone,
            email = email
        )
    }

    fun loadMenuCategories() {
        coroutineScope.launch {
            val categories = categoryRepo.getAllCategories()
            val categoriesWithItems = categories.map { category ->
                CategoryWithItems(
                    category = category,
                    items = menuRepo.getItemsByCategory(category.id)
                )
            }
            menuCategories = categoriesWithItems
        }
    }

    suspend fun setMemberStatus(isMember: Boolean) {
        _isMember.value = isMember
        recalculatePrices()
        refreshQuantities()
    }

    private fun refreshQuantities() {
        _variantQuantities.keys.forEach { key ->
            _variantQuantities[key] = _variantQuantities[key] ?: 1
        }
    }

    private val _variantQuantities = mutableStateMapOf<String, Int>() // key = itemId + size
    var variantQuantities: Map<String, Int> = _variantQuantities

    fun getQuantity(itemId: Int, variantSize: String): Int {
        return _variantQuantities["$itemId-$variantSize"] ?: 1
    }

    fun incrementQuantity(itemId: Int, variantSize: String) {
        val key = "$itemId-$variantSize"
        _variantQuantities[key] = (_variantQuantities[key] ?: 1) + 1
    }

    fun decrementQuantity(itemId: Int, variantSize: String) {
        val key = "$itemId-$variantSize"
        val current = _variantQuantities[key] ?: 1
        if (current > 1) _variantQuantities[key] = current - 1
    }

    fun updateQuantity(itemId: Int, variantSize: String, newQuantity: Int) {
        val key = "${itemId}-${variantSize}"
        _variantQuantities[key] = newQuantity.coerceAtLeast(1)
    }
}