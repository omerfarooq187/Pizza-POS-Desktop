package presentation.theme

// Theme.kt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFFD32F2F),  // Red
            secondary = Color(0xFF1976D2), // Blue
            background = Color(0xFFF5F5F5) // Light gray
        ),
        content = content
    )
}