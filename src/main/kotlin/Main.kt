// Main.kt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.singleWindowApplication
import di.appModule
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import presentation.screens.CategoryScreen
import presentation.screens.MenuItemListScreen
import presentation.screens.MenuItemScreen
import presentation.theme.AppTheme
import presentation.viewmodel.MenuItemViewModel


fun main() = application {

    startKoin {
        modules(appModule)
    }
    Window(
        title = "Pizza POS System",
        onCloseRequest = ::exitApplication
    ) {
        AppTheme {
            TabbedInterface()
        }
    }
}



@Composable
fun TabbedInterface() {
    var selectedTab by remember { mutableStateOf(Tab.CATEGORIES) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = "Logo",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Pizza Hut POS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
        }

        // Tab Navigation
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.White,
            edgePadding = 16.dp
        ) {
            Tab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title
                        )
                    },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            when (selectedTab) {
                Tab.CATEGORIES -> {
                    CategoryScreen (
                        onSelectedCategory = { categoryId ->
                            selectedCategoryId = categoryId
                            selectedTab = Tab.MENU_ITEMS
                        }
                    )
                }
                Tab.MENU_ITEMS -> MenuItemScreen(selectedCategoryId)
                Tab.ORDERS -> OrderScreen()
                Tab.REPORTS -> ReportScreen()
            }
        }
    }
}

enum class Tab(
    val title: String,
    val icon: ImageVector
) {
    CATEGORIES("Categories", Icons.Default.Category),
    MENU_ITEMS("Menu Items", Icons.Default.Restaurant),
    ORDERS("Orders", Icons.Default.PointOfSale),
    REPORTS("Reports", Icons.Default.Analytics)
}

// Placeholder screens with enhanced styling
//@Composable
//fun Category() {
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        shape = MaterialTheme.shapes.medium,
//        tonalElevation = 4.dp
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(
//                text = "Categories Management",
//                style = MaterialTheme.typography.headlineSmall,
//                color = MaterialTheme.colorScheme.primary
//            )
//            CategoryScreen()
//        }
//    }
//}

@Composable
fun MenuScreen(selectedCategoryId: Int?) {
    val viewModel: MenuItemViewModel = koinInject()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with category name
        selectedCategoryId?.let { categoryId ->
            val category = viewModel.categories.find { it.id == categoryId }
            Text(
                text = "Menu Items for: ${category?.name ?: "Unknown Category"}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Content switcher
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
}

@Composable
fun OrderScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order Management",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            // Your order content here
        }
    }
}

@Composable
fun ReportScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sales Reports",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            // Your report content here
        }
    }
}