package de.heinzenburger.g2_weckmichmal.ui.components

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Info
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

class NavBar : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    companion object{
        //Navbar Component at the bottom of the screen. All other components should be displayed inside of this component.
        //Therefore, the components shall be passed as function parameter (callback)
        @Composable
        fun <T> NavigationBar(modifier: Modifier, core: CoreSpecification, callback: @Composable ((PaddingValues, CoreSpecification) -> Unit), caller : T) {
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
                                val intent = Intent(context, SettingsScreen::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                context.startActivity(intent)
                            }
                                , modifier.size(90.dp)) {
                                Icon(Icons.Filled.Settings,
                                    contentDescription = "Localized description",
                                    modifier.size(iconSize),
                                    tint = if(caller == SettingsScreen::class){iconSelectedColor}else{iconColor},
                                    )
                            }
                            IconButton(onClick = {
                                val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                context.startActivity(intent)
                            }, modifier.size(90.dp)) {
                                Icon(
                                    Icons.Filled.AccessAlarm,
                                    contentDescription = "Localized description",
                                    modifier.size(iconSize),
                                    tint = if(caller == AlarmClockOverviewScreen::class || caller == AlarmClockEditScreen::class){iconSelectedColor}else{iconColor},
                                    )
                            }
                            if(core.getIsGameMode() == true){
                                IconButton(onClick = {
                                    val intent = Intent(context, GameScreen::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                    context.startActivity(intent)
                                }, modifier.size(90.dp)) {
                                    Icon(
                                        Icons.Filled.SportsEsports,
                                        contentDescription = "Game Icon",
                                        modifier.size(iconSize),
                                        tint = if(caller == GameScreen::class){iconSelectedColor}else{iconColor},
                                    )
                                }
                            }
                            IconButton(onClick = {
                                val intent = Intent(context, InformationScreen::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                context.startActivity(intent)
                            }, modifier.size(90.dp)) {
                                Icon(
                                    Icons.Filled.School,
                                    contentDescription = "Localized description",
                                    modifier.size(iconSize),
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

