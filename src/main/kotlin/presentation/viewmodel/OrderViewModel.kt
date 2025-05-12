package presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    val isMember: State<Boolean> = _isMember


    init {
        loadMenuCategories()
    }
    fun addItemToOrder(item: MenuItem, variant: ItemVariant, quantity: Int) {
        val finalPrice = calculateMemberPrice(item, variant)

        val orderItem = OrderItem(
            itemId = item.id,
            variantSize = variant.size,
            quantity = quantity,
            price = finalPrice,
            memberPriceApplied = _isMember.value && isItemEligibleForDiscount(item)
        )

        _currentOrder.value = _currentOrder.value.copy(
            items = _currentOrder.value.items + orderItem,
            totalAmount = _currentOrder.value.totalAmount + (finalPrice * quantity)
        )
    }

    private fun calculateMemberPrice(item: MenuItem, variant: ItemVariant): Double {
        return if (_isMember.value && isItemEligibleForDiscount(item)) {
            // Apply 50% discount for members on eligible items
            variant.price * 0.5
        } else {
            variant.price
        }
    }

    private fun isItemEligibleForDiscount(item: MenuItem): Boolean {
        // Add your business logic for eligible items
        // Example: Only apply discount to specific categories
        return item.categoryId == 1 // Example: Pizza category
    }

    fun validateMember(phone: String) {
        coroutineScope.launch {
            // Simple validation (replace with actual implementation)
            _isMember.value = orderRepo.isPhoneNumberRegistered(phone)

            if (_isMember.value) {
                recalculatePrices()
            }
        }
    }

    private suspend fun recalculatePrices() {
        val updatedItems = _currentOrder.value.items.map { item ->
            val menuItem = menuRepo.getItemById(item.itemId)
            val variant = menuItem.variants.first { it.size == item.variantSize }

            item.copy(
                price = calculateMemberPrice(menuItem, variant),
                memberPriceApplied = _isMember.value && isItemEligibleForDiscount(menuItem)
            )
        }

        _currentOrder.value = _currentOrder.value.copy(
            items = updatedItems,
            totalAmount = updatedItems.sumOf { it.price * it.quantity }
        )
    }

    fun finalizeOrder() {
        coroutineScope.launch {
            // Add contact info with defaults
            val finalOrder = _currentOrder.value.copy(
                isMember = _isMember.value,
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

    private fun loadMenuCategories() {
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
    }
}