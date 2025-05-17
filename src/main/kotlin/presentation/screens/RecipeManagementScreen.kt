package presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import presentation.viewmodel.RecipeViewModel

@Composable
fun RecipeManagementScreen(viewModel: RecipeViewModel = koinInject()) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Error messages - moved outside main content
        if (viewModel.errors.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(MaterialTheme.colorScheme.errorContainer)
            ) {
                viewModel.errors.forEach { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        // Main content - always visible
        Text("Recipe Management", style = MaterialTheme.typography.headlineMedium)

        // Menu Item Selection
        DropdownMenuComponent(
            label = "Select Menu Item Variant",
            items = viewModel.menuItemsWithVariants.flatMap { it.variants },
            itemToString = { "${it.size} (${it.itemName})" },
            onItemSelected = { viewModel.selectedVariant = it }
        )

        // Raw Item Selection
        DropdownMenuComponent(
            label = "Select Ingredient",
            items = viewModel.rawItems,
            itemToString = { it.name },
            onItemSelected = { viewModel.selectedRawItem = it }
        )

        // Quantity Input
        OutlinedTextField(
            value = viewModel.quantity,
            onValueChange = { viewModel.quantity = it },
            label = { Text("Quantity Needed") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.addRecipe() },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text("Add to Recipe")
        }
    }
}

@Composable
fun <T> DropdownMenuComponent(
    label: String,
    items: List<T>,
    itemToString: (T) -> String,
    onItemSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<T?>(null) }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedItem?.let { itemToString(it) } ?: label)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth() // Add width modifier
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemToString(item)) },
                    onClick = {
                        selectedItem = item
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}