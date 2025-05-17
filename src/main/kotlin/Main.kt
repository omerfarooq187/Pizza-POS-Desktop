// Main.kt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import di.appModule
import org.koin.core.context.startKoin
import presentation.screens.*
import presentation.theme.customTypography

fun main() = application {
    startKoin { modules(appModule) }

    Window(title = "Pizza POS System", onCloseRequest = ::exitApplication, icon = painterResource(resourcePath = "logo.jpg")) {
        MaterialTheme(
            typography = customTypography
        ) {
            TabbedInterface()
        }
    }
}

@Composable
fun TabbedInterface() {
    var selectedTab by remember { mutableStateOf(Tab.DASHBOARD) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    Row(modifier = Modifier.fillMaxSize()) {

        // Sidebar
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(220.dp)
                .background(Color(0xFFFE724C)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource("logo.jpg"),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            Tab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val background = if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent
                val textColor = if (isSelected) Color.White else Color.LightGray
                val iconTint = if (isSelected) Color.White else Color.LightGray

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(background)
                        .clickable { selectedTab = tab }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = tab.icon, contentDescription = tab.title, tint = iconTint)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = tab.title, color = textColor, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Main Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            when (selectedTab) {
                Tab.DASHBOARD -> DashboardScreen()
                Tab.CATEGORIES -> CategoryScreen {
                    selectedCategoryId = it
                    selectedTab = Tab.MENU_ITEMS
                }
                Tab.MENU_ITEMS -> MenuItemScreen(selectedCategoryId)
                Tab.ORDERS -> OrderScreen {
                    selectedTab = Tab.REPORTS
                }
                Tab.REPORTS -> ReportScreen { }
                Tab.INVENTORY -> {
                    InventoryScreen()
                }
                Tab.RECIPES -> {
                    RecipeManagementScreen()
                }
            }
        }
    }
}

// New Tab enum with Dashboard
enum class Tab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    CATEGORIES("Categories", Icons.Default.Category),
    MENU_ITEMS("Menu Items", Icons.Default.Restaurant),
    ORDERS("Orders", Icons.Default.PointOfSale),
    REPORTS("Reports", Icons.Default.Analytics),
    INVENTORY("Inventory", Icons.Default.Inventory),
    RECIPES("Recipes", Icons.Default.RestaurantMenu)
}
