package de.veloce.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import de.veloce.app.presentation.navigation.VeloceNavHost
import de.veloce.app.presentation.theme.VeloceTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VeloceTheme {
                VeloceNavHost()
            }
        }
    }
}
