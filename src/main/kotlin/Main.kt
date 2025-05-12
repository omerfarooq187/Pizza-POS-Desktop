// Main.kt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import di.appModule
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import presentation.screens.CategoryScreen
import presentation.screens.MenuItemListScreen
import presentation.screens.MenuItemScreen
import presentation.screens.OrderScreen
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

        // Top App Bar with Logo and Title
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp),
            color = Color(0xFFFE724C) // Soft Pizza-Themed Orange
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource("logo.jpg"),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Pizza Hut POS",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color(0xFFFFF3E0), // Light Cream Background
            contentColor = Color(0xFFFE724C),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab.ordinal])
                        .height(3.dp),
                    color = Color(0xFFFE724C)
                )
            },
            divider = {
                Divider(color = Color.LightGray, thickness = 1.dp)
            }
        ) {
            Tab.entries.forEach { tab ->
                key(tab) { // Key for stability
                    val isSelected = selectedTab == tab
                    // Memoize NON-COMPOSABLE values only
                    val iconTint = remember(isSelected) {
                        if (isSelected) Color(0xFFFE724C) else Color.Gray
                    }
                    val textStyle =
                        if (isSelected) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        else MaterialTheme.typography.bodyLarge

                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon( // Composable called OUTSIDE remember
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = iconTint // Use memoized value
                            )
                        },
                        text = {
                            Text( // Composable called OUTSIDE remember
                                text = tab.title,
                                color = iconTint,
                                style = textStyle
                            )
                        }
                    )
                }
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            when (selectedTab) {
                Tab.CATEGORIES -> {
                    if (selectedTab == Tab.CATEGORIES) {
                        CategoryScreen(
                            onSelectedCategory = { categoryId ->
                                selectedCategoryId = categoryId
                                selectedTab = Tab.MENU_ITEMS
                            }
                        )
                    }
                }

                Tab.MENU_ITEMS -> {
                    if (selectedTab == Tab.MENU_ITEMS) {
                        MenuItemScreen(selectedCategoryId)
                    }
                }
                Tab.ORDERS -> {
                    if (selectedTab == Tab.ORDERS) {
                        OrderScreen {

                        }
                    }
                }
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
fun CustomTabs(tabs: List<Tab>, selectedTab: Tab, onTabSelected: (Tab) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(tabs, key = { it }) { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .clickable { onTabSelected(tab) }
                    .padding(16.dp)
                    .background(
                        color = if (isSelected) Color(0xFFFE724C) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.title,
                    color = if (isSelected) Color.White else Color.Gray
                )
            }
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