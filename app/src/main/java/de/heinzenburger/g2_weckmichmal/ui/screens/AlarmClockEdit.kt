package de.heinzenburger.g2_weckmichmal.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.I_Core
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurButtonInEditAlarm
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.NavBar
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurTextField
import de.heinzenburger.g2_weckmichmal.ui.components.PickerDialogs.Companion.TimePickerDialogContainer
import de.heinzenburger.g2_weckmichmal.ui.components.PickerDialogs.Companion.MinutePickerDialog
import de.heinzenburger.g2_weckmichmal.ui.components.PickerDialogs.Companion.StationPickerDialog
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

class AlarmClockEditScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        setContent {
            G2_WeckMichMalTheme {
                val context = LocalContext.current
                BackHandler {
                    //Go to Overview Screen without animation
                    val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    context.startActivity(intent)
                    (context as ComponentActivity).finish()
                }
                EditComposable(modifier = Modifier, core)
            }
        }
    }
    companion object{
        private var openArrivalTimePickerDialog : MutableState<Boolean> = mutableStateOf(false)
        private var openTravelTimePickerDialog : MutableState<Boolean> = mutableStateOf(false)
        private var openStartBufferPickerDialog : MutableState<Boolean> = mutableStateOf(false)
        private var openEndBufferPickerDialog : MutableState<Boolean> = mutableStateOf(false)
        private var openStartStationDialog : MutableState<Boolean> = mutableStateOf(false)
        private var openEndStationDialog : MutableState<Boolean> = mutableStateOf(false)

        //These are static. They are storing all necessary information for an alarm configuration
        //Everytime an alarm is created, the reset function has to be called before calling this view so all configurations are set to default
        //If an alarm is to be updated, the current configuration is passed as parameter to reset function
        private lateinit var alarmName : MutableState<String>
        private lateinit var manuallySetArrivalTime: MutableState<LocalTime> //Set if arrival time shouldnt be dependent on lecture plan
        private lateinit var isManualArrivalTime : MutableState<Boolean>
        private lateinit var manuallySetTravelTime: MutableIntState //Set if travel time shouldnt be dependent on Deutsche Bahn
        private lateinit var isManualTravelTime : MutableState<Boolean>
        private lateinit var enforceStartBuffer : MutableState<Boolean>
        private lateinit var setStartBufferTime: MutableIntState //Set if arrival time shouldnt be dependent on lecture plan
        private lateinit var setEndBufferTime: MutableIntState //Time between arrival and lecture start
        private lateinit var startStation: MutableState<String>
        private lateinit var endStation: MutableState<String>
        private lateinit var selectedDays: MutableState<List<Boolean>> //All days where this alarm applies to
        private lateinit var configuration : Configuration //The configuration stored in the database

        fun reset(configuration: Configuration?){
            if(configuration == null){
                //All default values
                alarmName = mutableStateOf("")
                manuallySetArrivalTime = mutableStateOf(LocalTime.NOON)
                manuallySetTravelTime = mutableIntStateOf(15)
                setStartBufferTime = mutableIntStateOf(30)
                setEndBufferTime = mutableIntStateOf(5)
                startStation = mutableStateOf("Startbahnhof")
                endStation = mutableStateOf("Endbahnhof")
                selectedDays = mutableStateOf(listOf(true,true,true,true,true,false,false))
                isManualArrivalTime = mutableStateOf(false)
                isManualTravelTime = mutableStateOf(false)
                enforceStartBuffer = mutableStateOf(true)
                AlarmClockEditScreen.configuration = Configuration(
                    name = "Wecker",
                    days = setOf(),
                    fixedArrivalTime = null,
                    fixedTravelBuffer = null,
                    startBuffer = 30,
                    endBuffer = 10,
                    startStation = null,
                    endStation = null,
                    isActive = true,
                    enforceStartBuffer = true
                )
            }
            else{
                //Set Alarm Name
                alarmName = mutableStateOf(configuration.name)

                //BufferTimes
                setStartBufferTime = mutableIntStateOf(configuration.startBuffer)
                setEndBufferTime = mutableIntStateOf(configuration.endBuffer)

                //Convert Day of Week to Boolean List
                var days = mutableListOf<Boolean>()
                DayOfWeek.entries.forEach { day ->
                    days.add(configuration.days.contains(day))
                }
                selectedDays = mutableStateOf(days)

                //Manual set Arrival Time
                isManualArrivalTime = mutableStateOf(configuration.fixedArrivalTime != null)
                manuallySetArrivalTime = if(isManualArrivalTime.value){
                    mutableStateOf(configuration.fixedArrivalTime!!)
                } else{
                    mutableStateOf(LocalTime.NOON)
                }

                enforceStartBuffer = mutableStateOf(configuration.enforceStartBuffer)

                //Manual set Travel Time
                isManualTravelTime = mutableStateOf(configuration.fixedTravelBuffer != null)
                manuallySetTravelTime = if(isManualTravelTime.value){
                    mutableIntStateOf(configuration.fixedTravelBuffer!!)
                } else{
                    mutableIntStateOf(15)
                }
                //Stations if needed
                if(isManualTravelTime.value){
                    startStation = mutableStateOf("Startbahnhof")
                    endStation = mutableStateOf("Endbahnhof")
                }
                else{
                    startStation = mutableStateOf(configuration.startStation!!)
                    endStation = mutableStateOf(configuration.endStation!!)
                }

                AlarmClockEditScreen.configuration = Configuration(
                    uid = configuration.uid, //Use the same uid as the configuration that is changed
                    name = "Wecker",
                    days = setOf(),
                    fixedArrivalTime = null,
                    fixedTravelBuffer = null,
                    startBuffer = 30,
                    endBuffer = 10,
                    startStation = null,
                    endStation = null,
                    isActive = true, //Even if the alarm was initially inactive, it will be set as active again
                    enforceStartBuffer = true
                )
            }
        }

        fun saveConfiguration(core: I_Core, context: Context){
            thread{
                var validation = true
                //Name of Alarm
                if(alarmName.value == ""){
                    validation = false
                    core.showToast("Wecker Name fehlt")
                }
                else{
                    configuration.name = alarmName.value
                }
                //fixed arrival time if selected, else null
                if(isManualArrivalTime.value){
                    configuration.fixedArrivalTime = manuallySetArrivalTime.value
                }
                else if(!core.isInternetAvailable()){
                    validation = false
                    core.showToast("Für diese Konfiguration ist eine Internetverbindung nötig")
                }
                else if(core.getRaplaURL() == "" || core.getRaplaURL() == null){
                    validation = false
                    core.showToast("Ankunft nach Vorlesungsplan nicht möglich. URL fehlt.")
                }
                //fixed travel time if selected, else null
                if(isManualTravelTime.value){
                    configuration.fixedTravelBuffer = manuallySetTravelTime.intValue
                }
                else if(!core.isInternetAvailable()){
                    validation = false
                    core.showToast("Für diese Konfiguration ist eine Internetverbindung nötig")
                }
                //stations needed, else null
                else{
                    if(startStation.value == "Startbahnhof"){
                        validation = false
                        core.showToast("Startbahnhof setzen")
                    }
                    else if(endStation.value == "Endbahnhof"){
                        validation = false
                        core.showToast("Endbahnhof setzen")
                    }
                    else{
                        configuration.startStation = startStation.value
                        configuration.endStation = endStation.value
                    }
                }
                //set required start and endbuffer
                configuration.startBuffer = setStartBufferTime.intValue
                configuration.endBuffer = setEndBufferTime.intValue

                configuration.enforceStartBuffer = enforceStartBuffer.value

                //Setting days parameter
                var days = mutableSetOf<DayOfWeek>()
                selectedDays.value.forEachIndexed { index, active ->
                    if(active) days.add(DayOfWeek.entries[index])
                }
                if(days.isEmpty()){
                    validation = false
                    core.showToast("Mindestens einen Tag auswählen")
                }
                else{
                    configuration.days = days
                }
                if(validation){
                    core.generateOrUpdateAlarmConfiguration(configuration)
                    core.runUpdateLogic()

                    val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    context.startActivity(intent)
                    (context as ComponentActivity).finish()
                }
            }
        }

        //All components are stored as variables so they can be used as callbacks if needed
        private val innerDatengrundlageComposable : @Composable (PaddingValues, I_Core) -> Unit =
        { innerPadding: PaddingValues, core: I_Core ->
            val arrivalOptions = listOf("Manuelle Ankuftszeit", "Ankuftszeit nach Vorlesungsplan")
            //Dont really know what this is doing
            val (arrivalSelectedOption, onArrivalOptionSelected) = remember {
                if(isManualArrivalTime.value){
                    mutableStateOf(arrivalOptions[0])
                }
                else{
                    mutableStateOf(arrivalOptions[1])
                }
            }

            OurText(
                text = "Datengrundlage",
                modifier = Modifier.padding(top = 24.dp, start = 24.dp)
            )
            Row{
                Column(
                    Modifier
                        .fillMaxWidth()
                        .selectableGroup() //All radio buttons in this column correspond to one group
                ) {



                    Row(
                        Modifier
                            .selectable(
                                selected = (arrivalOptions[0] == arrivalSelectedOption),
                                onClick = {
                                    onArrivalOptionSelected(arrivalOptions[0])
                                    isManualArrivalTime.value = true
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (arrivalOptions[0] == arrivalSelectedOption),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        OurText(
                            text = arrivalOptions[0],
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )
                        OurButtonInEditAlarm(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp),
                            enabled = arrivalSelectedOption == arrivalOptions[0],
                            onClick = {
                                openArrivalTimePickerDialog.value = true
                            },
                            text = manuallySetArrivalTime.value.format(
                                DateTimeFormatter.ofPattern(
                                    "HH:mm"
                                )
                            ),
                        )

                    }
                    Row(
                        Modifier
                            .selectable(
                                selected = (arrivalOptions[1] == arrivalSelectedOption),
                                onClick = {
                                    onArrivalOptionSelected(arrivalOptions[1])
                                    isManualArrivalTime.value = false
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (arrivalOptions[1] == arrivalSelectedOption),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        OurText(
                            text = arrivalOptions[1],
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )
                    }
                }

            }
        }
        private val innerFahrtwegComposable : @Composable (PaddingValues, I_Core) -> Unit =
            { innerPadding: PaddingValues, core: I_Core ->
                val rideOptions = listOf("Bahnverbindung", "Manuelle Fahrtzeit")
                val (rideSelectedOption, onRideOptionSelected) = remember {
                    if(isManualTravelTime.value){
                        mutableStateOf(rideOptions[1])
                    }
                    else{
                        mutableStateOf(rideOptions[0])
                    }
                }

                Column (
                    modifier = Modifier.selectableGroup()
                ){
                    OurText(
                        text = "Fahrtweg",
                        modifier = Modifier.padding(top = 24.dp, start = 24.dp)
                    )
                    Row{
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .selectableGroup()
                        ) {
                            Row(
                                Modifier
                                    .selectable(
                                        selected = (rideOptions[0] == rideSelectedOption),
                                        onClick = {
                                            onRideOptionSelected(rideOptions[0])
                                            isManualTravelTime.value = false
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (rideOptions[0] == rideSelectedOption),
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.onBackground
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                OurText(
                                    text = rideOptions[0],
                                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                )
                            }
                            Column{
                                OurButtonInEditAlarm(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(48.dp, 0.dp),
                                    onClick = {
                                        openStartStationDialog.value = true
                                    },
                                    text = startStation.value,
                                    enabled = rideSelectedOption == rideOptions[0]
                                )
                                OurButtonInEditAlarm(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(48.dp, 0.dp),
                                    onClick = {
                                        openEndStationDialog.value = true
                                    },
                                    text = endStation.value,
                                    enabled = rideSelectedOption == rideOptions[0]
                                )
                            }

                            Row(
                                Modifier
                                    .selectable(
                                        selected = (rideOptions[1] == rideSelectedOption),
                                        onClick = {
                                            onRideOptionSelected(rideOptions[1])
                                            isManualTravelTime.value = true
                                        },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (rideOptions[1] == rideSelectedOption),
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.onBackground
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                OurText(
                                    text = rideOptions[1],
                                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                )
                                OurButtonInEditAlarm(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 48.dp),
                                    onClick = {
                                        openTravelTimePickerDialog.value = true
                                    },
                                    enabled = rideSelectedOption == rideOptions[1],
                                    text = manuallySetTravelTime.intValue.toString() + "min",
                                )
                            }
                        }
                    }
                }
            }
        private val innerZeitaufwandComposable : @Composable (PaddingValues, I_Core) -> Unit =
            { innerPadding: PaddingValues, core: I_Core ->
                OurText(
                    text = "Zeitaufwand",
                    modifier = Modifier.padding(top = 24.dp, start = 24.dp)
                )
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    OurText(
                        text = "Puffer vor Fahrt",
                        modifier = Modifier.padding(top = 24.dp, start = 24.dp)
                    )
                    OurButtonInEditAlarm(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp),
                        onClick = {
                            openStartBufferPickerDialog.value = true
                        },
                        text = setStartBufferTime.intValue.toString() + "min",
                    )
                }
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    OurText(
                        text = "Puffer vor Fahrt erzwingen",
                        modifier = Modifier.padding(24.dp, 16.dp, 0.dp, 0.dp)
                    )
                    Switch(
                        checked = enforceStartBuffer.value,
                        //Configuration will always be reset to active when edited in AlarmClockEdit
                        onCheckedChange = {
                            enforceStartBuffer.value = it
                        },
                        enabled = true,
                        colors = SwitchDefaults.colors(
                            checkedBorderColor = MaterialTheme.colorScheme.background,
                            uncheckedBorderColor = MaterialTheme.colorScheme.background,
                            checkedIconColor = MaterialTheme.colorScheme.primary,
                            uncheckedIconColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.padding(16.dp, 0.dp)
                    )
                }
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp)

                ){
                    OurText(
                        text = "Puffer nach Ankunft",
                        modifier = Modifier.padding(top = 24.dp, start = 24.dp)
                    )
                    OurButtonInEditAlarm(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp),
                        onClick = {
                            openEndBufferPickerDialog.value = true
                        },
                        text = setEndBufferTime.intValue.toString() + "min"
                    )
                }
            }
        private val innerGueltigkeitComposable : @Composable (PaddingValues, I_Core) -> Unit =
            { innerPadding: PaddingValues, core: I_Core ->
                OurText(
                    text = "Gültig für",
                    modifier = Modifier.padding(top = 24.dp, start = 24.dp)
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ){
                    selectedDays.value.forEachIndexed { index, active ->

                        TextButton(
                            modifier = Modifier
                                .padding(start = 0.dp)
                                .width(50.dp)
                                .align(
                                    BiasAlignment(
                                        (2 * (index / 6f) - 1),
                                        verticalBias = 0f
                                    )
                                ),
                            onClick = {
                                var days = selectedDays.value.toMutableList()
                                days[index] = !days[index]
                                selectedDays.value = days
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            val day = DayOfWeek.entries[index].name
                            OurText(
                                text = day[0]+day[1].lowercase(),
                                color = if(active){
                                    MaterialTheme.colorScheme.secondary
                                } else{
                                    MaterialTheme.colorScheme.onBackground
                                },
                                modifier = Modifier.padding(0.dp)
                            )
                        }
                    }
                }
            }

        //Main component that is sent as navbar as callback
        @OptIn(ExperimentalMaterial3Api::class) //Needed because Time Picker is yet experimental
        val innerEditComposable : @Composable (PaddingValues, I_Core) -> Unit =
        { innerPadding: PaddingValues, core: I_Core ->
            val context = LocalContext.current
            //Open time picker dialogs when corresponding boolean set to true
            when {
                openArrivalTimePickerDialog.value -> {
                    TimePickerDialogContainer(
                        onConfirm =
                        { timePickerState: TimePickerState ->
                            openArrivalTimePickerDialog.value = false
                            manuallySetArrivalTime.value = LocalTime.of(
                                timePickerState.hour, timePickerState.minute
                            )
                        },
                        onDismiss = {
                            openArrivalTimePickerDialog.value = false
                        })
                }
            }
            when {
                openTravelTimePickerDialog.value -> {
                    MinutePickerDialog(
                        onConfirm =
                            { minutes: Int ->
                                openTravelTimePickerDialog.value = false
                                manuallySetTravelTime.intValue = minutes
                            },
                        onDismiss = {
                            openTravelTimePickerDialog.value = false
                        },
                        default = manuallySetTravelTime.intValue
                    )
                }
            }
            when {
                openStartBufferPickerDialog.value -> {
                    MinutePickerDialog(
                        onConfirm =
                            { minutes: Int ->
                                openStartBufferPickerDialog.value = false
                                setStartBufferTime.intValue = minutes
                            },
                        onDismiss = {
                            openStartBufferPickerDialog.value = false
                        },
                        default = setStartBufferTime.intValue
                    )
                }
            }
            when {
                openEndBufferPickerDialog.value -> {
                    MinutePickerDialog(
                        onConfirm =
                            { minutes: Int ->
                                openEndBufferPickerDialog.value = false
                                setEndBufferTime.intValue = minutes
                            },
                        onDismiss = {
                            openEndBufferPickerDialog.value = false
                        },
                        default = setEndBufferTime.intValue
                    )
                }
            }
            when {
                openStartStationDialog.value -> {
                    StationPickerDialog(
                        onConfirm =
                            { station: String ->
                                openStartStationDialog.value = false
                                startStation.value = station
                            },
                        onDismiss = {
                            openStartStationDialog.value = false
                        },
                        core
                    )
                }
            }
            when {
                openEndStationDialog.value -> {
                    StationPickerDialog(
                        onConfirm =
                            { station: String ->
                                openEndStationDialog.value = false
                                endStation.value = station
                            },
                        onDismiss = {
                            openEndStationDialog.value = false
                        },
                        core
                    )
                }
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    OurText(
                        text = "Wecker bearbeiten",
                        modifier = Modifier.padding(16.dp)
                    )
                    TextButton(
                        //Save to database when clicked
                        onClick = {
                            saveConfiguration(core, context)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp),

                    ) {
                        OurText(
                            text = "Speichern",
                            textAlign = TextAlign.Right,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                OurTextField(
                    value = alarmName.value,
                    onValueChange = {alarmName.value = it},
                    modifier = Modifier
                        .padding(24.dp, 8.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    placeholderText = "Wecker Name",
                )

                innerDatengrundlageComposable(innerPadding,core)
                innerFahrtwegComposable(innerPadding,core)
                innerZeitaufwandComposable(innerPadding,core)
                innerGueltigkeitComposable(innerPadding,core)
            }
        }
    }
}

@Composable
fun EditComposable(modifier: Modifier, core: I_Core) {
    NavBar.Companion.NavigationBar(modifier, core, AlarmClockEditScreen.innerEditComposable, caller = AlarmClockEditScreen::class)
}


@Preview(showBackground = true)
@Composable
fun EditPreview() {
    AlarmClockEditScreen.reset(null)
    G2_WeckMichMalTheme {
        EditComposable(
            modifier = Modifier,
            core = MockupCore()
        )
    }
}