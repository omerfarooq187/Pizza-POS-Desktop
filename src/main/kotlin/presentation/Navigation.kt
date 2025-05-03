package presentation


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class NavController {
    private val _screen = mutableStateOf<Screen>(Screen.List)
    val screen: State<Screen> = _screen
    fun navigateTo(screen: Screen) {
        this._screen.value = screen
    }
}

sealed class Screen {
    object List: Screen()
    object Form: Screen()
}