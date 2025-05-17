package presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import data.model.OrderItem
import kotlinx.coroutines.launch
import org.joda.time.format.DateTimeFormat
import org.koin.compose.koinInject
import presentation.viewmodel.DashboardViewModel
import presentation.viewmodel.ReportViewModel

@Composable
fun DashboardScreen() {
    // Get ViewModel from Koin
    val dashboardViewModel: DashboardViewModel = koinInject()
    val reportViewModel: ReportViewModel = koinInject()

    val coroutineScope = rememberCoroutineScope()

    // Observe StateFlow values
    val todayOrders = dashboardViewModel.todayOrderCount
    val todaySales = dashboardViewModel.todayTotalSales

    val recentOrders = reportViewModel.orders.takeLast(20).reversed()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            dashboardViewModel.fetchDashboardData()
            reportViewModel.getAllOrders()
        }
    }



    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            "Dashboard Overview",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            DashboardCard(
                title = "Daily Total Sales",
                value = "Rs. ${String.format("%.2f", todaySales)}",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            DashboardCard(
                title = "Daily Orders",
                value = todayOrders.toString(),
                color = Color.Red,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Recent Orders", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        recentOrders.forEach {
            OrderRow(
                time = it.createdAt.toString(DateTimeFormat.forPattern("hh:mm a")),
                items = it.items,
                total = it.totalAmount.toString(),
                status = "Paid"
            )
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = Color.White)
        }
    }
}

@Composable
fun OrderRow(time: String, items: List<OrderItem>, total: String, status: String) {
    val itemSummary = items.joinToString(", ") { "${it.quantity} x ${it.itemName}" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFFF5F5F5)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(time, modifier = Modifier.weight(1f))
        Text(itemSummary, modifier = Modifier.weight(2f))
        Text("Rs $total", modifier = Modifier.weight(1f))
        Text(status, color = Color.Green, modifier = Modifier.weight(1f))
    }
}

