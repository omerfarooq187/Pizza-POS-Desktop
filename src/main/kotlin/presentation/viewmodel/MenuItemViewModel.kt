package presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import data.model.Category
import data.model.ItemVariant
import data.model.MenuItem
import data.repository.CategoryRepository
import data.repository.MenuRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MenuItemViewModel: KoinComponent {
    val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    private val menuRepo: MenuRepository by inject()
    private val categoryRepo: CategoryRepository by inject()

    var categories by mutableStateOf(emptyList<Category>())
    var menuItems by mutableStateOf(emptyList<MenuItem>())
    var currentCategoryId by mutableStateOf<Int?>(null)

    var selectedCategory by mutableStateOf<Category?>(null)
    var itemName by mutableStateOf("")
    var description by mutableStateOf("")
    var variants by mutableStateOf<List<ItemVariant>>(emptyList())
    var error by mutableStateOf<String?>(null)
    var loading by mutableStateOf(true)
//    var discountValue by mutableStateOf(0.0)

    var showCreateItemDialog by mutableStateOf(false)
    var editingItem by mutableStateOf<MenuItem?>(null)
    var showEditDialog by mutableStateOf(false)

    init {
        loadCategories()
    }

    fun loadCategories() {
        coroutineScope.launch {
            try {
                categories = (categoryRepo.getAllCategories())
                loading = false
            } catch (e: Exception) {
                error = "Failed to load categories "+e.message
                loading = false
            }
        }
    }

    fun loadItems(categoryId: Int) {
        currentCategoryId = categoryId
        coroutineScope.launch {
            try {
                menuItems = menuRepo.getItemsByCategory(categoryId)
                error = null
            } catch (e: Exception) {
                error = "Failed to load items ${e.message}"
            }
        }
    }

    fun saveItem() {
        coroutineScope.launch {
            try {
                if (selectedCategory == null) {
                    error = "Please select a category"
                    return@launch
                }
                if (itemName.isBlank()) {
                    error = "Item name is required"
                    return@launch
                }
                if (variants.isEmpty()) {
                    error = "At least one variant is required"
                    return@launch
                }
                if (menuRepo.itemExists(itemName, selectedCategory!!.id)) {
                    error = "Item already exists in the category"
                    return@launch
                }

//                val category = selectedCategory
//                if (category == null) {
//                    error = "Please select a category"
//                    return@launch
//                }
                menuRepo.createItem(
                    MenuItem(
                        categoryId = currentCategoryId!!,
                        name = itemName,
                        description = description,
                        variants = variants,
                    )
                )
                error = null
                clearForm()
                showCreateItemDialog = false
                showEditDialog = false
                loadItems(currentCategoryId!!)
            } catch (e: Exception) {
                e.printStackTrace()  // <--- Logs the full stacktrace in Logcat
                error = "Failed to save item: ${e.message ?: "Unknown error"}"
            }
        }
    }

    fun clearForm() {
        itemName = ""
        description = ""
        variants = emptyList()
        selectedCategory = null
        editingItem = null
        showCreateItemDialog = false // Add this
        showEditDialog = false
        error = null
    }


    fun startEditItem(item: MenuItem) {
        editingItem = item
        showEditDialog = true
        showCreateItemDialog = false // Ensure create dialog is closed
        // Populate form fields
        itemName = item.name
        description = item.description ?: ""
        variants = item.variants
        selectedCategory = categories.find { it.id == item.categoryId }
    }
    fun updateItem() {
        coroutineScope.launch {
            try {
                val currentItem = editingItem ?: return@launch
                val updatedItem = currentItem.copy(
                    name = itemName,
                    description = description.ifBlank { null },
                    variants = variants
                )

                menuRepo.updateItem(updatedItem)
                // Refresh items list
                loadItems(selectedCategory?.id ?: return@launch)
                clearForm()
                error = null
            } catch (e: Exception) {
                error = "Failed to update item: ${e.message}"
            }
        }
    }

    fun deleteItem(itemId: Int) {
        coroutineScope.launch {
            try {
                menuRepo.deleteItem(itemId)
                menuItems.filter {
                    it.id != itemId
                }
//                val category = selectedCategory
                loadItems(currentCategoryId!!)
                error = null
            } catch (e: Exception) {
                error = "Delete failed: ${e.message}"
            }
        }
    }
}