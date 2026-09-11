import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.veloce.app.ui.App

fun main() = application {
    val windowState = rememberWindowState(size = DpSize(420.dp, 840.dp))
    Window(
        onCloseRequest = ::exitApplication,
        title = "Veloce Telemetry & BoardComputer",
        state = windowState,
        resizable = true
    ) {
        App()
    }
}
