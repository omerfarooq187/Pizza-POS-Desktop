package service

import data.model.OrderItem
import data.model.RawItem
import data.repository.RawItemRepository
import data.repository.RecipeRepository

// domain/service/InventoryService.kt
class InventoryService(
    private val recipeRepo: RecipeRepository,
    private val rawItemRepo: RawItemRepository
) {
    suspend fun getIngredientsForVariant(variantId: Int): List<Pair<RawItem, Double>> {
        return recipeRepo.getRecipesForVariant(variantId).map { recipe ->
            val rawItem = rawItemRepo.getRawItem(recipe.rawItemId)
                ?: throw Exception("Raw item not found")
            rawItem to recipe.quantityNeeded
        }
    }

    suspend fun updateStockForOrder(orderItems: List<OrderItem>) {
        orderItems.forEach { item ->
            val recipes = recipeRepo.getRecipesForVariant(item.variantId)
            recipes.forEach { recipe ->
                val totalNeeded = recipe.quantityNeeded * item.quantity
                rawItemRepo.updateStock(
                    recipe.rawItemId,
                    -totalNeeded,
                    "Order #${item.orderId}"
                )
            }
        }
    }
}