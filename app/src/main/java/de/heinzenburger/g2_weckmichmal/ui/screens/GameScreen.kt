package de.heinzenburger.g2_weckmichmal.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.ExceptionHandler
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.NavBar
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import kotlin.concurrent.thread

// Main Activity for the Settings screen
class GameScreen : ComponentActivity() {
    private var coins = mutableIntStateOf(0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        ExceptionHandler(core).runWithUnexpectedExceptionHandler("Error displaying Game screen",true) {
            // Load courses and excluded courses in a background thread
            thread {
                coins.intValue = core.getCoins() ?: 0
            }
            setContent {
                val context = LocalContext.current
                // Handle back navigation to overview screen
                BackHandler {
                    //Go to Overview Screen without animation
                    val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    finish()
                }
                G2_WeckMichMalTheme {
                    GameComposable(modifier = Modifier, core)
                }
            }
        }
    }

    // Wrapper function for the Settings Composable with NavBar
    @Composable
    fun GameComposable(modifier: Modifier, uiActions: CoreSpecification) {
        NavBar.Companion.NavigationBar(modifier, uiActions, innerGameComposable, GameScreen::class)
    }

    //Main component
    val innerGameComposable : @Composable (PaddingValues, CoreSpecification) -> Unit = { innerPadding: PaddingValues, core: CoreSpecification ->

        Column(
            Modifier
                .padding(24.dp)
                .background(MaterialTheme.colorScheme.background)) {
            OurText(
                text = "Belohnungen: " + coins.intValue.toString(),
                modifier = Modifier,
            )
        }
    }
}

//Preview UI in Android Studio
@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    val gameScreen = GameScreen()
    G2_WeckMichMalTheme {
        gameScreen.GameComposable(modifier = Modifier, MockupCore())
    }
}
