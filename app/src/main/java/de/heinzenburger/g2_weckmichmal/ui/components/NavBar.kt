package de.heinzenburger.g2_weckmichmal.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.screens.AlarmClockEditScreen
import de.heinzenburger.g2_weckmichmal.ui.screens.AlarmClockOverviewScreen
import de.heinzenburger.g2_weckmichmal.ui.screens.GameScreen
import de.heinzenburger.g2_weckmichmal.ui.screens.InformationScreen
import de.heinzenburger.g2_weckmichmal.ui.screens.SettingsScreen
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.ui.game.BetterGameScreen

class NavBar : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    companion object{
        fun <T> switchActivity(context: Context, cls : Class<T>){
            val intent = Intent(context, cls)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(intent)
            (context as? Activity)?.finish()
        }
        //Navbar Component at the bottom of the screen. All other components should be displayed inside of this component.
        //Therefore, the components shall be passed as function parameter (callback)
        @Composable
        fun <T> NavigationBar(core: CoreSpecification, callback: @Composable ((PaddingValues, CoreSpecification) -> Unit), caller : T) {
            val iconSize = 35.dp
            val iconColor = MaterialTheme.colorScheme.primary
            val iconSelectedColor = MaterialTheme.colorScheme.secondary
            val context = LocalContext.current

            Scaffold(
                bottomBar = {
                    BottomAppBar(containerColor = MaterialTheme.colorScheme.onPrimary) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = {
                                switchActivity(context, SettingsScreen::class.java)
                            }
                                , Modifier.size(90.dp)) {
                                Icon(Icons.Filled.Settings,
                                    contentDescription = "Localized description",
                                    Modifier.size(iconSize),
                                    tint = if(caller == SettingsScreen::class){iconSelectedColor}else{iconColor},
                                    )
                            }
                            IconButton(onClick = {
                                switchActivity(context, AlarmClockOverviewScreen::class.java)
                            }, Modifier.size(90.dp)) {
                                Icon(
                                    Icons.Filled.AccessAlarm,
                                    contentDescription = "Localized description",
                                    Modifier.size(iconSize),
                                    tint = if(caller == AlarmClockOverviewScreen::class || caller == AlarmClockEditScreen::class){iconSelectedColor}else{iconColor},
                                    )
                            }
                            if(caller == BetterGameScreen::class || core.getIsGameMode() == true){
                                IconButton(onClick = {
                                    switchActivity(context, BetterGameScreen::class.java)
                                }, Modifier.size(90.dp)) {
                                    Icon(
                                        Icons.Filled.SportsEsports,
                                        contentDescription = "Game Icon",
                                        Modifier.size(iconSize),
                                        tint = if(caller == BetterGameScreen::class){iconSelectedColor}else{iconColor},
                                    )
                                }
                            }
                            IconButton(onClick = {
                                switchActivity(context, InformationScreen::class.java)
                            }, Modifier.size(90.dp)) {
                                Icon(
                                    Icons.Filled.School,
                                    contentDescription = "Localized description",
                                    Modifier.size(iconSize),
                                    tint = if(caller == InformationScreen::class){iconSelectedColor}else{iconColor},
                                    )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                callback(innerPadding, core)
            }
        }
    }
}

