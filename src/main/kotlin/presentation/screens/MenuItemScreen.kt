package presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.model.Category
import data.model.ItemVariant
import data.model.MenuItem
import org.koin.compose.koinInject
import presentation.viewmodel.CategoryViewModel
import presentation.viewmodel.MenuItemViewModel

// presentation/screen/MenuItemScreen.kt
@Composable
fun MenuItemScreen(selectedCategoryId: Int?) {
    val viewModel: MenuItemViewModel = koinInject()
    val categories = viewModel.categories

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {


        // Header with category name
        LaunchedEffect(categories) {
            if (viewModel.selectedCategory == null && categories.isNotEmpty()) {
                viewModel.selectedCategory = categories.last()
                viewModel.loadItems(viewModel.selectedCategory!!.id)
            }
        }

        // Show item list or message
        if (selectedCategoryId != null) {
            MenuItemListScreen(categoryId = selectedCategoryId)
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select a category from the Categories tab",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    // Dialog for create/edit item
    if (viewModel.showEditDialog || viewModel.showCreateItemDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.clearForm() },
            title = {
                if (viewModel.showEditDialog) {
                    "Edit Menu Item"
                } else {
                    "Add Menu Item"
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.showEditDialog) viewModel.updateItem()
                        else viewModel.saveItem()
                    },
                    enabled = viewModel.itemName.isNotBlank() &&
                            viewModel.variants.isNotEmpty()
                ) {
                    Text(if (viewModel.showEditDialog) "Update Item" else "Create Menu Item")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearForm() }) {
                    Text("Cancel")
                }
            },
            text = {
                Column {
                    // Error
                    viewModel.error?.let {
                        Text(it, color = Color.Red)
                    }

                    // Item name
                    OutlinedTextField(
                        value = viewModel.itemName,
                        onValueChange = { viewModel.itemName = it },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Description
                    OutlinedTextField(
                        value = viewModel.description,
                        onValueChange = { viewModel.description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Variants
                    VariantManager(
                        variants = viewModel.variants,
                        onVariantsChanged = { viewModel.variants = it }
                    )
                }
            }
        )
    }
}


// presentation/components/CategoryDropdown.kt
@Composable
fun CategoryDropdown(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedCategory?.name ?: "Select Category")
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Open categories",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                ) {
                    Text(text = category.name)
                }
            }
        }
    }
}

@Composable
fun VariantManager(
    variants: List<ItemVariant>,
    onVariantsChanged: (List<ItemVariant>) -> Unit
) {
    Column {
        Text("Variants", style = MaterialTheme.typography.headlineSmall)

        variants.forEachIndexed { index, variant ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = variant.size,
                    onValueChange = { newSize ->
                        val updated = variants.toMutableList()
                        updated[index] = variant.copy(size = newSize)
                        onVariantsChanged(updated)
                    },
                    label = { Text("Size") },
                    modifier = Modifier.width(100.dp)
                )

                OutlinedTextField(
                    value = variant.price.toString(),
                    onValueChange = { newPrice ->
                        val updated = variants.toMutableList()
                        updated[index] = variant.copy(price = newPrice.toDoubleOrNull() ?: 0.0)
                        onVariantsChanged(updated)
                    },
                    label = { Text("Price") },
                    modifier = Modifier.width(150.dp)
                )

                IconButton(onClick = {
                    val updated = variants.toMutableList()
                    updated.removeAt(index)
                    onVariantsChanged(updated)
                }) {
                    Icon(Icons.Default.Delete, "Remove variant")
                }
            }
        }

        Button(onClick = {
            onVariantsChanged(variants + ItemVariant(size = "", price = 0.0))
        }) {
            Text("Add Variant")
        }
    }
}



// presentation/screen/MenuItemListScreen.kt
@Composable
fun MenuItemListScreen(categoryId: Int) {
    val viewModel: MenuItemViewModel = koinInject()

    LaunchedEffect(categoryId) {
        viewModel.loadItems(categoryId)
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Manage Menu Items", style = MaterialTheme.typography.headlineSmall)
            Button(
                onClick = { viewModel.showCreateItemDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("Add New Item")
            }
        }

        // List of items
        when {
            viewModel.loading -> CircularProgressIndicator()
            viewModel.error != null -> Text(viewModel.error!!, color = Color.Red)
            viewModel.menuItems.isEmpty() -> Text("No items found in this category")
            else -> LazyColumn {
                items(viewModel.menuItems) { item ->
                    MenuItemCard(
                        item = item,
                        onEdit = { viewModel.startEditItem(item) },
                        onDelete = { viewModel.deleteItem(item.id) },
                        onToggle = { viewModel.toggleActive(it)}
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header with name and active switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = item.isActive,
                    onCheckedChange = {
                        onToggle(item.id)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            item.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Variant Pricing
            Text(
                text = "Pricing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            item.variants.forEach { variant ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${variant.size}: $${variant.price}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    variant.memberPrice?.let {
                        Text(
                            text = "Member: $$it",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions: Edit and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
