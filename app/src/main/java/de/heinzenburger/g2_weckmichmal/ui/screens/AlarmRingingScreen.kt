package de.heinzenburger.g2_weckmichmal.ui.screens

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import de.heinzenburger.g2_weckmichmal.background.ForegroundService
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.ExceptionHandler
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Event
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.specifications.Route
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmRingingScreen : ComponentActivity(){
    // Reference to the foreground service handling alarm logic
    private lateinit var foregroundService: ForegroundService
    // Indicates if the screen is shown as a preview
    private var isPreview = false
    // List of courses to be excluded from the calculation
    var listOfExcludedCourses = emptyList<String>()
    // Mutable states to hold the current event and configuration
    var event = mutableStateOf(Event.emptyEvent)
    var configuration = mutableStateOf(Configuration.emptyConfiguration)
    // State to track swipe direction (left/right)
    var swipedLeft = mutableStateOf(false)

    // Handles connection to the ForegroundService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ForegroundService.MyBinder
            foregroundService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }
    // Binds to the running ForegroundService
    fun bindService(){
        Intent(this, ForegroundService::class.java).also { intent ->
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    // Activity entry point: sets up UI and binds service if not preview
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        ExceptionHandler(core).runWithUnexpectedExceptionHandler("Error displaying AlarmRinging",true) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)

            isPreview = intent.getBooleanExtra("isPreview", false)
            if (!isPreview) {
                bindService()
            }

            // Retrieve configuration and event from intent
            val configurationWithEvent =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        "configurationWithEvent",
                        ConfigurationWithEvent::class.java
                    )!!
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<ConfigurationWithEvent>("configurationWithEvent")!!
                }
            if (configurationWithEvent.event != null) {
                event.value = configurationWithEvent.event
            }
            configuration.value = configurationWithEvent.configuration

            setContent {
                G2_WeckMichMalTheme {
                    val context = LocalContext.current
                    // Handle back button: return to overview
                    BackHandler {
                        val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                        startActivity(intent)
                        finish()
                    }
                    innerAlarmRingingScreenComposable(core)
                }
            }
        }
    }

    // Composable: Shows alarm overview and route/course details
    @Composable fun OverviewComposable(){
        val detailedViewForRoute = remember { mutableStateOf<Route?>(null) }
        if(isPreview){
            Text(
                text = "In " + Duration.between(LocalDateTime.now(),event.value.getLocalDateTime()).toMinutes().toString() + " Minuten",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )
        }
        else{
            Text(
                text = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )
        }
        var text = ""
        var isCoursesEmpty = true
        event.value.courses?.forEachIndexed { index, it ->
            if(!listOfExcludedCourses.contains(it.name)){
                text += it.name + " - " + it.startDate.format(DateTimeFormatter.ofPattern("HH:mm")) + " bis " + it.endDate.format(DateTimeFormatter.ofPattern("HH:mm"))
                if (event.value.courses!!.size - 1 != index) {
                    text += "\n"
                }
                isCoursesEmpty = false
            }
        }
        text = if (!isCoursesEmpty) {
            "Mental vorbereiten auf: \n$text"
        } else {
            "Hallo :D"
        }
        Text(
            text = text,
            modifier = Modifier
                .padding(top = 20.dp, start = 20.dp, end = 20.dp)
                .background(MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(20))
                .fillMaxWidth()
                .padding(10.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
        )
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            event.value.routes?.fastForEachReversed {
                val bestRoute =
                    it.startTime.minusMinutes(configuration.value.startBuffer.toLong())
                        .format(DateTimeFormatter.ofPattern("HH:mm")) == event.value.wakeUpTime
                        .format(DateTimeFormatter.ofPattern(("HH:mm")))
                Button(
                    onClick = {
                        if(detailedViewForRoute.value == it){
                            detailedViewForRoute.value = null
                        }
                        else{
                            detailedViewForRoute.value = it
                        }
                              },
                    contentPadding = PaddingValues(),
                    colors = ButtonDefaults.buttonColors(Color.Transparent)
                ) {

                    Column(
                        modifier = Modifier
                            .padding(top = 20.dp, start = 40.dp, end = 40.dp)
                            .background(
                                if (bestRoute) {
                                    MaterialTheme.colorScheme.onBackground
                                } else {
                                    MaterialTheme.colorScheme.onPrimary
                                },RoundedCornerShape(48.dp)
                            )
                            .fillMaxWidth()
                            .padding(10.dp),
                    ) {
                        if (detailedViewForRoute.value == it) {
                            it.sections.forEach { section ->
                                OurText(
                                    text = section.startTime.format(DateTimeFormatter.ofPattern("HH:mm ")) + section.startStation,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                OurText(
                                    text = section.vehicleName,
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                Text(
                                    text = "|",
                                    color =
                                        if (bestRoute) {
                                            MaterialTheme.colorScheme.secondary
                                        } else {
                                            MaterialTheme.colorScheme.onBackground
                                        },
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OurText(
                                    text = section.endTime.format(DateTimeFormatter.ofPattern("HH:mm ")) + section.endStation,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            OurText(
                                text = it.startTime.format(DateTimeFormatter.ofPattern("HH:mm ")) + it.startStation,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            for (i in 0..1) {
                                i
                                Text(
                                    text = "|",
                                    color =
                                        if (bestRoute) {
                                            MaterialTheme.colorScheme.secondary
                                        } else {
                                            MaterialTheme.colorScheme.onBackground
                                        },
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            OurText(
                                text = it.endTime.format(DateTimeFormatter.ofPattern("HH:mm ")) + it.endStation,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    // Main composable for the alarm ringing screen, handles swipe and actions
    val innerAlarmRingingScreenComposable : @Composable (CoreSpecification) -> Unit = { core: CoreSpecification ->
        Box(
            modifier = Modifier.pointerInput(Unit) {
                // Detect swipe gestures to switch between overview and mensa plan
                detectDragGestures { change, dragAmount ->
                    val (x, y) = dragAmount
                    when {
                        x > 50 -> swipedLeft.value = false
                        x < -50 -> swipedLeft.value = true
                    }
                }
            }
        ){
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
            )
            {
                if(!swipedLeft.value) {
                    OverviewComposable()
                }
                else{
                    if(core.isInternetAvailable()){
                        val informationScreen = InformationScreen()
                        informationScreen.core = core
                        informationScreen.InnerMensaComposable(PaddingValues(top = 32.dp))
                    }
                    else{
                        core.showToast("Mensa Essensplan kann nur bei aktiver Internetverbindung angezeigt werden.")
                        swipedLeft.value = false
                    }
                }
            }
            OurText(
                text = if(!swipedLeft.value){
                    "Nach links swipen für Mensaplan"
                }else{
                    "Nach rechts swipen für Übersicht"
                },
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(BiasAlignment(0f, 0.75f))
            )
            if(!isPreview){
                val context = LocalContext.current
                Button(
                    onClick = {
                        foregroundService.sleepWithPerry(isForeverSleep = true)
                        unbindService(serviceConnection)
                        val stopIntent = Intent(context, ForegroundService::class.java)
                        stopService(stopIntent)
                    },
                    modifier = Modifier
                        .align(BiasAlignment(0.9f, 0.9f))
                        .fillMaxWidth(0.4f),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.background,
                        containerColor = MaterialTheme.colorScheme.secondary,
                    ),
                ) {
                    OurText(
                        text = "Stop",
                        modifier = Modifier
                    )
                }
                Button(
                    onClick = {
                        foregroundService.sleepWithPerry(isForeverSleep = false)
                    },
                    modifier = Modifier
                        .align(BiasAlignment(-0.9f, 0.9f))
                        .fillMaxWidth(0.4f),
                    colors = ButtonDefaults.buttonColors(
                        contentColor = MaterialTheme.colorScheme.background,
                        containerColor = MaterialTheme.colorScheme.secondary,
                    ),
                ) {
                    OurText(
                        text = "Snooze 5m",
                        modifier = Modifier
                    )
                }
            }
        }
    }

    // Preview for Compose UI in Android Studio
    @Preview(showBackground = true)
    @Composable
    fun AlarmRingingScreenPreview() {
        val alarmRingingScreen = AlarmRingingScreen()
        val core = MockupCore()
        alarmRingingScreen.event.value = MockupCore.mockupEvents[0]
        alarmRingingScreen.configuration.value = MockupCore.mockupConfigurations[0]
        G2_WeckMichMalTheme {
            alarmRingingScreen.innerAlarmRingingScreenComposable(core)
        }
    }
}

