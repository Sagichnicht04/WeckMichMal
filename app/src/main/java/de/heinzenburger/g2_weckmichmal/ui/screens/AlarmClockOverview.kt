package de.heinzenburger.g2_weckmichmal.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.ExceptionHandler
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.LoadingScreen
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.NavBar
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

// Main screen activity for displaying and managing alarm clock configurations
class AlarmClockOverviewScreen : ComponentActivity(){
    private lateinit var core: CoreSpecification
    private var openLoadingScreen = mutableStateOf(false)
    private var configurationAndEventEntities = mutableStateOf(emptyList<ConfigurationWithEvent>())
    private lateinit var context : Context

    private var aPlatypus = true //Variable to set Platypus mode
    private var coins = mutableIntStateOf(5000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        context = applicationContext
        core = Core(context = applicationContext)

        ExceptionHandler(core as Core).runWithUnexpectedExceptionHandler("Error displaying AlarmClockOverview",true) {
            //Background loading of alarm configurations
            thread {
                configurationAndEventEntities.value = core.getAllConfigurationAndEvent()!!
                aPlatypus = core.getIsGameMode() == true
                coins.intValue = core.getCoins() ?: 0
            }

            setContent {
                G2_WeckMichMalTheme {
                    BackHandler {
                        // Exits the app when back is pressed
                        finishAffinity()
                    }
                    AlarmClockOverviewComposable(modifier = Modifier, core)
                }
            }
        }
    }


    // Deletes an alarm configuration and refreshes the screen
    private fun deleteConfiguration(
        core: CoreSpecification,
        properties: ConfigurationWithEvent,
        context: Context
    ){
        thread {
            core.deleteAlarmConfiguration(properties.configuration.uid)

            val intent = Intent(context, AlarmClockOverviewScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
        }
    }

    // Navigates to the alarm edit screen, passing configuration data
    private fun setEditScreen(context: Context, configuration: Configuration?){
        val core = Core(context)
        val intent = Intent(context, AlarmClockEditScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        intent.putExtra("defaultAlarmValues", core.getDefaultAlarmValues())
        intent.putExtra("configuration", configuration)
        startActivity(intent)
        finish()
    }

    // Navigates to the alarm ringing preview screen
    private fun setRingingScreen(context: Context, configurationWithEvent: ConfigurationWithEvent){
        val intent = Intent(context, AlarmRingingScreen::class.java)
        intent.putExtra("configurationWithEvent", configurationWithEvent)
        intent.putExtra("isPreview", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        finish()
    }

    //Elements in Configuration Component that stay the same, regardless of Platypus
    @Composable
    private fun InnerSingleAlarmConfiguration(properties: ConfigurationWithEvent) {
        var userActivated by remember { mutableStateOf(properties.configuration.isActive) }

        Column(
            Modifier
                .padding(0.dp, 16.dp)
                .clip(RoundedCornerShape(15))
                .background(MaterialTheme.colorScheme.onBackground)
                .fillMaxWidth(0.8f)
        ){
            Row(
                Modifier
                    .padding()
                    .background(Color.Transparent)
                    .fillMaxWidth(1f)
            ) {
                Switch(
                    checked = userActivated,
                    //Configuration will always be reset to active when edited in AlarmClockEdit
                    onCheckedChange = {
                        userActivated = it
                        thread {
                            core.updateConfigurationActive(userActivated, properties.configuration)
                            core.runUpdateLogic()
                        }
                    },
                    enabled = true,
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = MaterialTheme.colorScheme.background,
                        uncheckedBorderColor = MaterialTheme.colorScheme.background,
                        checkedIconColor = MaterialTheme.colorScheme.primary,
                        uncheckedIconColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.padding(10.dp, 10.dp, 0.dp, 0.dp)
                )
                OurText(
                    text = properties.configuration.name,
                    modifier = Modifier.padding(16.dp, 24.dp, 0.dp, 0.dp)
                )
            }
            Row(
                Modifier
                    .background(Color.Transparent)
                    .fillMaxWidth(1f)
            ) {
                Row(
                    modifier = Modifier.padding(2.dp, 0.dp),
                ){
                    properties.configuration.days.forEach {
                        OurText(
                            text = it.name[0] +""+ it.name[1].lowercase(),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(5.dp, 0.dp, 0.dp, 3.dp),
                        )
                    }
                }

                Button(
                    modifier = Modifier,
                    onClick = {
                        setRingingScreen(context, properties)
                    },
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    OurText(
                        text = "Geplant um: ${properties.event?.wakeUpTime?.format(DateTimeFormatter.ofPattern("HH:mm"))} \uD83D\uDCC4",
                        textAlign = TextAlign.Right,
                        modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 3.dp).fillMaxWidth(1f)
                    )
                }
            }
        }
    }

    //UI Arrangement for Components when Platypus mode is off
    @Composable
    private fun SingleAlarmConfiguration(properties: ConfigurationWithEvent){
        Box(
            contentAlignment = Alignment.TopEnd
        ){
            InnerSingleAlarmConfiguration(properties)
            Button(
                onClick = {
                    deleteConfiguration(core, properties, context)
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.background,
                    containerColor = MaterialTheme.colorScheme.secondary,
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .border(2.dp, Color.Transparent,
                        RoundedCornerShape(50))
                    .size(40.dp)

            ){
                Text(
                    text = "⨯",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.background,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    //UI Arrangement for Components when Platypus mode is activated
    @Composable
    private fun APlatypus(properties: ConfigurationWithEvent){
        Column(
            verticalArrangement = Arrangement.spacedBy((-18).dp),
        ){
            Box(
                modifier = Modifier
                    .align(BiasAlignment.Horizontal(-0.6f))
                    .zIndex(2f)
                    .size(50.dp)
                    .clip(GenericShape { size, _ ->
                        moveTo(size.width*0.3f, size.height*0.5f)
                        lineTo(size.width*0.7f, size.height*0.5f)
                        lineTo(size.width*0.7f, size.height*0.9f)
                        lineTo(size.width, size.height*0.9f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        lineTo(0f, size.height*0.9f)
                        lineTo(size.width*0.3f, size.height*0.9f)
                    })
                    .background(color =
                        if(coins.intValue > 1000){
                            Color(55, 34, 87)
                        }
                        else if(coins.intValue > 500 ){
                            Color(66, 156, 119)
                        }
                        else if(coins.intValue > 100 ){
                            Color(231, 199, 81)
                        }
                        else if(coins.intValue > 20){
                            Color(182, 183, 188)
                        }
                        else{
                            Color(66, 48, 11)
                        }
                    )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((-10).dp)
            ){
                Button(
                    onClick = {
                        deleteConfiguration(core, properties, context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.background,
                        containerColor = MaterialTheme.colorScheme.secondary,
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .zIndex(1f)
                        .border(2.dp, Color.Transparent,
                            RoundedCornerShape(50))

                ){
                    Text(
                        text = "⨯",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.background,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                InnerSingleAlarmConfiguration(properties)

                Button(
                    onClick = {
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(150,100,0),
                    ),
                    modifier = Modifier
                        .zIndex(-1f)
                        .size(50.dp, 40.dp)
                        .padding(0.dp,20.dp,0.dp,0.dp)
                ){

                }
            }
            Box(
                modifier = Modifier
                    .align(BiasAlignment.Horizontal(0f))
                    .zIndex(2f)
                    .width(200.dp)
                    .height(30.dp)
                    .clip(GenericShape { size, _ ->
                        moveTo(size.width*0.17f, 0f)
                        lineTo(size.width*0.17f, size.height)
                        lineTo(size.width*0.05f, size.height)
                        lineTo(size.width*0.05f, size.height*0.9f)
                        lineTo(size.width*0.13f, size.height*0.9f)
                        lineTo(size.width*0.13f, size.height*0f)

                        moveTo(size.width*0.87f, 0f)
                        lineTo(size.width*0.87f, size.height)
                        lineTo(size.width*0.75f, size.height)
                        lineTo(size.width*0.75f, size.height*0.9f)
                        lineTo(size.width*0.83f, size.height*0.9f)
                        lineTo(size.width*0.83f, size.height*0f)
                    })
                    .background(color = Color(251, 176, 51))
            )
        }
    }

    //Main Component, is passed to Navbar. Contains all configuration components and plus icon
    val innerAlarmClockOverviewComposable : @Composable (PaddingValues, CoreSpecification) -> Unit = { innerPadding: PaddingValues, core: CoreSpecification ->
        when {
            openLoadingScreen.value ->{
                LoadingScreen()
            }
        }
        Box {
            Column(
                Modifier
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                OurText(
                    text = "Wecker",
                    modifier = Modifier.padding(16.dp)
                )
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(weight = 1f, fill = true)
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    configurationAndEventEntities.value.forEach {
                        configurationAndEvent ->
                        Button(
                            onClick = {
                                setEditScreen(context, configurationAndEvent.configuration)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(0.dp)
                        ){
                            if (aPlatypus) {
                                APlatypus(
                                    configurationAndEvent
                                )
                            } else {
                                SingleAlarmConfiguration(
                                    configurationAndEvent
                                )
                            }
                        }
                    }
                    Button(
                        onClick = {
                            setEditScreen(context, null)
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.secondary,
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .size(69.dp)
                            .border(2.dp, Color.Transparent,
                                RoundedCornerShape(50))

                    ) {
                        Text(text = "+", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

        }
    }

    // Entry point composable for the screen, wrapped with navigation bar
    @Composable
    fun AlarmClockOverviewComposable(modifier: Modifier, core: CoreSpecification) {
        NavBar.Companion.NavigationBar(
            core, innerAlarmClockOverviewComposable,
            AlarmClockOverviewScreen::class)
    }

    // Preview for Compose UI in Android Studio
    @Preview(showBackground = true)
    @Composable
    fun AlarmClockOverviewScreenPreview() {
        val core = MockupCore()
        context = LocalContext.current
        configurationAndEventEntities.value = core.getAllConfigurationAndEvent()!!
        G2_WeckMichMalTheme {
            AlarmClockOverviewComposable(modifier = Modifier, core = core)
        }
    }
}



