package presentation.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.model.Order
import org.joda.time.format.DateTimeFormat
import org.koin.compose.koinInject
import presentation.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(onBack: () -> Unit) {
    val viewModel: ReportViewModel = koinInject()
    val orders = viewModel.orders

    var selectedRange by remember { mutableStateOf("Today") }

    // Trigger filtering when selectedRange changes
    LaunchedEffect(selectedRange) {
        viewModel.filterOrders(selectedRange)
    }

    if (viewModel.exportMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            title = { Text("Export Status") },
            text = { Text(viewModel.exportMessage!!) },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearMessage() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFE724C)
                    )
                ) {
                    Text("OK")
                }
            }
        )
    }


    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Reports", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Range Selector
            val ranges = listOf("Today", "This Week", "This Month")
            ranges.forEach { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { selectedRange = range },
                    label = { Text(range) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { viewModel.exportToPdf(orders) }
            ) {
                Text(
                    text = "Export to PDF"
                )
            }
            Spacer(modifier = Modifier.width(25.dp))
            Button(
                onClick = { viewModel.exportToExcel(orders) }
            ) {
                Text(
                    text = "Export to EXCEL"
                )
            }
        }

        // Table Header
        HeaderRow()

        // Orders List
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(orders.reversed()) { order ->
                OrderRow(order)
                Divider()
            }
        }
    }
}





@Composable
fun HeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        TableCell("ID", 0.1f, isHeader = true)
        TableCell("Customer", 0.2f, isHeader = true)
        TableCell("Contact", 0.25f, isHeader = true)
        TableCell("Item Names",0.30f, isHeader = true)
        TableCell("Total", 0.15f, isHeader = true)
        TableCell("Date", 0.1f, isHeader = true)
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = if (isHeader) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}



@Composable
fun OrderRow(order: Order) {
    val formatter = DateTimeFormat.forPattern("dd MMM yyyy")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell("#${order.id}", 0.1f)
        TableCell(order.customerName, 0.2f)

        Column(modifier = Modifier.weight(0.25f)) {
            Text(order.phone, style = MaterialTheme.typography.bodyMedium)
            Text(order.email, style = MaterialTheme.typography.bodySmall)
        }

        Column(modifier = Modifier.weight(0.30f)) {
            order.items.forEach {
                Text(it.itemName, style = MaterialTheme.typography.bodyMedium)
            }
        }

        TableCell("Rs. ${"%.2f".format(order.totalAmount)}", 0.15f)
        TableCell(formatter.print(order.createdAt), 0.1f)
    }
}

