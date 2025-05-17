package service

import data.model.InventoryTransaction
import data.model.OrderItem
import data.model.RawItem
import data.repository.RawItemRepository
import data.repository.RecipeRepository
import database.InventoryTransactions
import org.jetbrains.exposed.sql.transactions.transaction

class InventoryService(
    private val recipeRepo: RecipeRepository,
    private val rawItemRepo: RawItemRepository
) {
    suspend fun updateStockForOrder(orderItems: List<OrderItem>, orderId: Int? = null): List<String> {
        val warnings = mutableListOf<String>()

        orderItems.forEach { item ->
            try {
                // Use variantId instead of itemId
                val recipes = recipeRepo.getRecipesForVariant(item.variantId)

                if (recipes.isEmpty()) {
                    warnings += "No recipe found for ${item.itemName} (${item.variantSize})"
                    return@forEach
                }

                recipes.forEach { recipe ->
                    val rawItem = rawItemRepo.getRawItem(recipe.rawItemId)
                        ?: throw Exception("Raw item ${recipe.rawItemId} not found")

                    val required = recipe.quantityNeeded * item.quantity

                    if (rawItem.currentStock < required) {
                        warnings += "Insufficient ${rawItem.name} (Need: $required, Have: ${rawItem.currentStock})"
                    }

                    rawItemRepo.updateStock(
                        rawItemId = recipe.rawItemId,
                        delta = -required,
                        reason = "Order: ${item.itemName} (${item.variantSize})",
                        orderId = orderId // Update this when you implement order persistence
                    )
                }
            } catch (e: Exception) {
                warnings += "Failed to process ${item.itemName}: ${e.message}"
            }
        }

        return warnings
    }

    suspend fun validateOrderInventory(orderItems: List<OrderItem>): List<String> {
        val errors = mutableListOf<String>()

        orderItems.forEach { item ->
            try {
                // Use variantId here
                val recipes = recipeRepo.getRecipesForVariant(item.variantId)

                if (recipes.isEmpty()) {
                    errors += "No recipe available for ${item.itemName} (${item.variantSize})"
                    return@forEach
                }

                recipes.forEach { recipe ->
                    val rawItem = rawItemRepo.getRawItem(recipe.rawItemId)
                    val required = recipe.quantityNeeded * item.quantity
                    val available = rawItem?.currentStock ?: 0.0

                    if (available < required) {
                        errors += "Not enough ${rawItem?.name ?: "ingredient#${recipe.rawItemId}"} " +
                                "(Need: $required ${rawItem?.unit}, Available: $available)"
                    }
                }
            } catch (e: Exception) {
                errors += "Validation failed for ${item.itemName}: ${e.message}"
            }
        }

        return errors
    }

    suspend fun checkLowStock(): List<RawItem> {
        return rawItemRepo.getAllRawItemsBelowThreshold()
    }

    suspend fun getInventoryItems(): List<RawItem> {
        return rawItemRepo.getAllRawItems()
    }

}