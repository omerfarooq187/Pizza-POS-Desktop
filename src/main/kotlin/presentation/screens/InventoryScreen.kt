package presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import data.model.RawItem
import org.koin.compose.koinInject
import presentation.viewmodel.InventoryViewModel

// presentation/screens/InventoryScreen.kt
@Composable
fun InventoryScreen(viewModel: InventoryViewModel = koinInject()) {

    var showAddDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Inventory", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Text("Add Item")
            }
        }

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Inventory", style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = { viewModel.loadInventory() }) {
                Icon(Icons.Default.Refresh, "Reload")
            }
        }

        // Error Messages
        if (viewModel.errors.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.1f))
            ) {
                viewModel.errors.forEach { error ->
                    Text(error, color = Color.Red)
                }
            }
        }

        // Content
        when {
//            viewModel.loading -> CircularProgressIndicator()
            viewModel.inventoryItems.isEmpty() -> Text("No inventory items found")
            else -> LazyColumn {
                items(viewModel.inventoryItems) { item ->
                    InventoryItemRow(item)
                }
            }
        }

        AddRawItemDialog(
            showDialog = showAddDialog,
            onDismiss = { showAddDialog = false },
            onCreate = { newItem ->
                viewModel.createRawItem(newItem)
            }
        )
    }
}

@Composable
fun InventoryItemRow(item: RawItem) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text("Stock: ${item.currentStock} ${item.unit}")
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = when {
                            item.alertThreshold == null -> Color.Gray
                            item.currentStock < item.alertThreshold -> Color.Red
                            else -> Color.Green
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}




// presentation/components/AddRawItemDialog.kt
@Composable
fun AddRawItemDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onCreate: (RawItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var currentStock by remember { mutableStateOf("") }
    var alertThreshold by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add New Raw Item") },
            text = {
                Column(modifier = Modifier.padding(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name*") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (kg, liters, etc.)*") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = currentStock,
                        onValueChange = { currentStock = it },
                        label = { Text("Initial Stock*") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = alertThreshold,
                        onValueChange = { alertThreshold = it },
                        label = { Text("Low Stock Alert Threshold") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = supplier,
                        onValueChange = { supplier = it },
                        label = { Text("Supplier") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newItem = RawItem(
                            name = name,
                            unit = unit,
                            currentStock = currentStock.toDoubleOrNull() ?: 0.0,
                            alertThreshold = alertThreshold.toDoubleOrNull(),
                            supplier = supplier.ifEmpty { null },
                            description = description.ifEmpty { null }
                        )
                        onCreate(newItem)
                        onDismiss()
                    },
                    enabled = name.isNotBlank() && unit.isNotBlank() && currentStock.isNotBlank()
                ) {
                    Text("Add Item")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}