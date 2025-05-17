package presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.model.ItemVariant
import data.model.MenuItemWithVariants
import data.model.RawItem
import data.model.Recipe
import data.repository.MenuRepository
import data.repository.RawItemRepository
import data.repository.RecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// presentation/viewmodel/RecipeViewModel.kt
class RecipeViewModel : KoinComponent {
    private val menuRepo: MenuRepository by inject()
    private val rawItemRepo: RawItemRepository by inject()
    private val recipeRepo: RecipeRepository by inject()

    private val _menuItemsWithVariants = mutableStateListOf<MenuItemWithVariants>()
    val menuItemsWithVariants: List<MenuItemWithVariants> get() = _menuItemsWithVariants

    private val _rawItems = mutableStateListOf<RawItem>()
    val rawItems: List<RawItem> get() = _rawItems

    private val _loading = mutableStateOf(true)
    val loading: Boolean get() = _loading.value

    private val _errors = mutableStateListOf<String>()
    val errors: List<String> get() = _errors

    var selectedVariant by mutableStateOf<ItemVariant?>(null)
    var selectedRawItem by mutableStateOf<RawItem?>(null)
    var quantity by mutableStateOf("")

    init {
        loadData()
    }

    fun loadData() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            try {
                _menuItemsWithVariants.clear()
                _menuItemsWithVariants.addAll(menuRepo.getAllMenuItemsWithVariants())

                _rawItems.clear()
                _rawItems.addAll(rawItemRepo.getAllRawItems())
            } catch (e: Exception) {
                _errors.add("Failed to load data: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun addRecipe() {
        if (selectedVariant == null || selectedRawItem == null || quantity.isBlank()) {
            _errors.add("Please fill all fields")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                recipeRepo.addRecipe(
                    Recipe(
                        variantId = selectedVariant!!.id,
                        rawItemId = selectedRawItem!!.id,
                        quantityNeeded = quantity.toDouble()
                    )
                )
                clearForm()
            } catch (e: Exception) {
                _errors.add("Failed to add recipe: ${e.message}")
            }
        }
    }

    private fun clearForm() {
        selectedVariant = null
        selectedRawItem = null
        quantity = ""
    }
}