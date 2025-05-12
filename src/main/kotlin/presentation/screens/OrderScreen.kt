package presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import data.model.CategoryWithItems
import data.model.ItemVariant
import data.model.MenuItem
import data.model.OrderItem
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import presentation.viewmodel.OrderViewModel

@Composable
fun OrderScreen(
    onShowReports: () -> Unit
) {
    val viewModel: OrderViewModel = koinInject()
    val currentOrder by viewModel.currentOrder
    val isMember by viewModel.isMember
    var customerName by remember { mutableStateOf(currentOrder.customerName) }
    var phone by remember { mutableStateOf(currentOrder.phone) }
    var email by remember { mutableStateOf(currentOrder.email) }
    val coroutineScope = rememberCoroutineScope()

    var showAddItemsDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Customer Information Section
        Text(
            "Customer Details",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name") },
            placeholder = { Text("N/A")},
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            placeholder = { Text("N/A")},
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            placeholder = { Text("N/A")},
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.updateContactInfo(customerName, phone, email) },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Save Contact Info")
        }

        // Member Validation Section
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isMember,
                onCheckedChange = {
                    coroutineScope.launch {
                        viewModel.setMemberStatus(it)
                    }
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFFE724C),
                    checkmarkColor = Color.White
                )
            )
            Text(
                "Is this customer a member?",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Text(
            if (isMember) "✅ Member discounts applied!"
            else "⚠️ Regular pricing applied",
            color = if (isMember) Color.Green else Color.Red,
            modifier = Modifier.padding(top = 4.dp)
        )


        // Order Items Section
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                "Order Items",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                onClick = { showAddItemsDialog = true },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Add Items")
            }
        }

        if (showAddItemsDialog) {
            MenuItemSelectionDialog(
                viewModel = viewModel,
                onDismiss = { showAddItemsDialog = false },
            )
        }

        if (currentOrder.items.isEmpty()) {
            Text(
                "No items in order",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(currentOrder.items) { item ->
                    OrderItemRow(item, viewModel)
                }
            }
        }

        // Total Section
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Total:", style = MaterialTheme.typography.headlineMedium)
            Text(
                "$${"%.2f".format(currentOrder.totalAmount)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Action Buttons
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.finalizeOrder()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFE724C)
                )
            ) {
                Text("Finalize Order")
            }

            Button(onClick = onShowReports) {
                Text("View Reports")
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: OrderItem, viewModel: OrderViewModel) {
    // Load menu item details asynchronously
    var menuItem by remember { mutableStateOf<MenuItem?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(item.itemId) {
        menuItem = try {
            viewModel.menuRepo.getItemById(item.itemId)
        } catch (e: Exception) {
            null
        }
        loading = false
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            menuItem?.name ?: "Unknown Item",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Size: ${item.variantSize}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "$${"%.2f".format(item.price)} x ${item.quantity}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Subtotal: $${"%.2f".format(item.price * item.quantity)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (item.memberPriceApplied) {
                    Text(
                        "Member discount applied!",
                        color = Color.Green,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuItemSelectionDialog(
    viewModel: OrderViewModel,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val categoriesWithItems = viewModel.menuCategories

    val filteredCategories by remember(searchQuery) {
        derivedStateOf {
            categoriesWithItems.map { category ->
                val filteredItems = category.items.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.description?.contains(searchQuery, ignoreCase = true) == true
                }
                category.copy(items = filteredItems)
            }.filter { it.items.isNotEmpty() }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(MaterialTheme.shapes.extraLarge),
            shadowElevation = 16.dp,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Menu Items",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFFE724C)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Search items...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFFFE724C),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )

                // Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(filteredCategories) { category ->
                        CategorySection(category, viewModel)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySection(
    category: CategoryWithItems,
    viewModel: OrderViewModel
) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = category.category.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFFFE724C),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
            ) {
                category.items.forEach { item ->
                    MenuItemCard(item, viewModel)
                    if (item != category.items.last()) {
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemCard(item: MenuItem, viewModel: OrderViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    item.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFFFE724C)
                    )
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle variants"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    item.variants.forEach { variant ->
                        VariantRow(item, variant, viewModel)
                        if (variant != item.variants.last()) {
                            Divider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VariantRow(item: MenuItem, variant: ItemVariant, viewModel: OrderViewModel) {
    var quantity by remember { mutableStateOf(1) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = variant.size,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$${"%.2f".format(variant.price)}",
                    style = MaterialTheme.typography.bodyLarge
                )

                variant.memberPrice?.let {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$${"%.2f".format(it)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Green,
                        modifier = Modifier
                            .background(
                                color = Color.Green.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuantitySelector(
                quantity = quantity,
                onDecrement = { if (quantity > 1) quantity-- },
                onIncrement = { quantity++ },
                modifier = Modifier.width(120.dp)
            )

            FilledTonalButton(
                onClick = {
                    viewModel.addItemToOrder(item, variant, quantity)
                    quantity = 1
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFE724C),
                    contentColor = Color.White
                )
            ) {
                Text("Add")
            }
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        IconButton(
            onClick = onDecrement,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color(0xFFFE724C).copy(alpha = 0.1f),
                contentColor = Color(0xFFFE724C)
            ),
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.Remove, "Decrease")
        }

        Text(
            text = "$quantity",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        IconButton(
            onClick = onIncrement,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color(0xFFFE724C).copy(alpha = 0.1f),
                contentColor = Color(0xFFFE724C)
            ),
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.Add, "Increase")
        }
    }
}