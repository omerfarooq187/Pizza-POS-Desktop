import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import database.PosDatabase
import di.appModule
import org.koin.core.context.startKoin
import presentation.screens.CategoryScreen


fun main() {

    try {
        startKoin {
            modules(appModule)
        }
        PosDatabase.init()

        application {
            Window(title = "Pizza POS", onCloseRequest = ::exitApplication) {
                CategoryScreen()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
