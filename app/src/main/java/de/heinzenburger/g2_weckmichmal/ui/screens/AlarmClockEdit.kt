package de.heinzenburger.g2_weckmichmal.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
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
import de.heinzenburger.g2_weckmichmal.core.ExceptionHandler
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.specifications.SettingsEntity.DefaultAlarmValues
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.LoadingScreen
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurButtonInEditAlarm
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.NavBar
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurTextField
import de.heinzenburger.g2_weckmichmal.ui.components.PickerDialogs.Companion.GrantPermissions
import de.heinzenburger.g2_weckmichmal.ui.components.PickerDialogs.Companion.TimePickerDialogContainer
import de.heinzenburger.g2_weckmichmal.ui.components.PickerDialogs.Companion.MinutePickerDialog
import de.heinzenburger.g2_weckmichmal.ui.components.PickerDialogs.Companion.StationPickerDialog
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

// Activity for editing or creating an alarm clock configuration
class AlarmClockEditScreen : ComponentActivity() {
    // Registers for permission result callback
    private val registerForActivityResult = registerForActivityResult(RequestPermission()){ }

    // State variables for dialog visibility and alarm configuration fields
    private var openLoadingScreen = mutableStateOf(false)
    private var openArrivalTimePickerDialog = mutableStateOf(false)
    private var openTravelTimePickerDialog = mutableStateOf(false)
    private var openStartBufferPickerDialog = mutableStateOf(false)
    private var openEndBufferPickerDialog = mutableStateOf(false)
    private var openStartStationDialog = mutableStateOf(false)
    private var openEndStationDialog = mutableStateOf(false)
    private var openPermissionDialog = mutableStateOf(false)

    //Everytime an alarm is created, the reset function has to be called before calling this view so all configurations are set to default
    //If an alarm should be updated, the current configuration is passed as parameter to reset function
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

    private lateinit var defaultAlarmValues: DefaultAlarmValues
    private lateinit var core: CoreSpecification

    // Resets all state variables to default or given configuration
    fun reset(configuration: Configuration?){
        openLoadingScreen.value = false
        if(configuration == null){
            //All default values
            alarmName = mutableStateOf(defaultAlarmValues.name)
            manuallySetArrivalTime = mutableStateOf(LocalTime.parse(defaultAlarmValues.manuallySetArrivalTime))
            manuallySetTravelTime = mutableIntStateOf(defaultAlarmValues.manuallySetTravelTime)
            setStartBufferTime = mutableIntStateOf(defaultAlarmValues.setStartBufferTime)
            setEndBufferTime = mutableIntStateOf(defaultAlarmValues.setEndBufferTime)
            startStation = mutableStateOf(defaultAlarmValues.startStation)
            endStation = mutableStateOf(defaultAlarmValues.endStation)
            selectedDays = mutableStateOf(listOf(false,false,false,false,false,false,false))
            isManualArrivalTime = mutableStateOf(false)
            isManualTravelTime = mutableStateOf(false)
            enforceStartBuffer = mutableStateOf(defaultAlarmValues.enforceStartBuffer)
            this.configuration = Configuration(
                name = defaultAlarmValues.name,
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
                mutableIntStateOf(defaultAlarmValues.manuallySetTravelTime)
            }
            //Stations if needed
            if(isManualTravelTime.value){
                startStation = mutableStateOf(defaultAlarmValues.startStation)
                endStation = mutableStateOf(defaultAlarmValues.endStation)
            }
            else{
                startStation = mutableStateOf(configuration.startStation!!)
                endStation = mutableStateOf(configuration.endStation!!)
            }

            this.configuration = Configuration(
                uid = configuration.uid, //Use the same uid as the configuration that is changed
                name = defaultAlarmValues.name,
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

    // Initializes core logic, loads default values, and sets up the UI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        core = Core(context = applicationContext)

        ExceptionHandler(core as Core).runWithUnexpectedExceptionHandler("Error displaying AlarmClockEdit",true){
            defaultAlarmValues = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("defaultAlarmValues", DefaultAlarmValues::class.java)!!
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<DefaultAlarmValues>("defaultAlarmValues")!!
            }

            reset(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("configuration", Configuration::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<Configuration>("configuration")
            })

            setContent {
                G2_WeckMichMalTheme {
                    val context = LocalContext.current
                    BackHandler {
                        //Go to Overview Screen without animation
                        val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intent)
                        finish()
                    }
                    EditComposable(modifier = Modifier, core)
                }
            }
        }
    }

    fun validateConfiguration(): Boolean{
        var validation = true

        if(!isManualArrivalTime.value){
            if(!core.isInternetAvailable()){
                validation = false
                core.showToast("Für diese Konfiguration ist eine Internetverbindung nötig")
            }
            else if(core.getRaplaURL() == "" || core.getRaplaURL() == null){
                validation = false
                core.showToast("Ankunft nach Vorlesungsplan nicht möglich. URL fehlt.")
            }
        }


        if(!isManualTravelTime.value){
            if(!core.isInternetAvailable()){
                validation = false
                core.showToast("Für diese Konfiguration ist eine Internetverbindung nötig")
            }
            else{
                if(startStation.value == "Startbahnhof"){
                    validation = false
                    core.showToast("Startbahnhof setzen")
                }
                else if(endStation.value == "Endbahnhof"){
                    validation = false
                    core.showToast("Endbahnhof setzen")
                }
            }
        }

        var days = mutableSetOf<DayOfWeek>()
        selectedDays.value.forEachIndexed { index, active ->
            if(active) days.add(DayOfWeek.entries[index])
        }
        if(days.isEmpty()){
            validation = false
            core.showToast("Mindestens einen Tag auswählen")
        }

        return validation
    }

    fun validatePermissions(): Boolean {
        val permissions = core.getGrantedPermissions()
        if((permissions?.contains("Notifications") == false || permissions?.contains("Alarm") == false)){
            core.showToast("Berechtigungen fehlen")
            openPermissionDialog.value = true
            return false
        }
        return true
    }

    fun inputFieldsToConfiguration(){
        configuration.name = alarmName.value

        //fixed arrival time if selected, else null
        if(isManualArrivalTime.value){
            configuration.fixedArrivalTime = manuallySetArrivalTime.value
        }

        //fixed travel time if selected, else null
        if(isManualTravelTime.value){
            defaultAlarmValues.manuallySetTravelTime = manuallySetTravelTime.intValue
            configuration.fixedTravelBuffer = manuallySetTravelTime.intValue
        }
        //stations needed, else null
        else{
            configuration.startStation = startStation.value
            configuration.endStation = endStation.value
            defaultAlarmValues.startStation = startStation.value
            defaultAlarmValues.endStation = endStation.value
        }
        //set required start and endbuffer
        configuration.startBuffer = setStartBufferTime.intValue
        configuration.endBuffer = setEndBufferTime.intValue
        defaultAlarmValues.setStartBufferTime = setStartBufferTime.intValue
        defaultAlarmValues.setEndBufferTime = setEndBufferTime.intValue

        configuration.enforceStartBuffer = enforceStartBuffer.value
        defaultAlarmValues.enforceStartBuffer = enforceStartBuffer.value

        //Setting days parameter
        var days = mutableSetOf<DayOfWeek>()
        selectedDays.value.forEachIndexed { index, active ->
            if(active) days.add(DayOfWeek.entries[index])
        }
        configuration.days = days
    }

    // Saves the current configuration to the database and handles validation
    fun saveConfiguration(core: CoreSpecification, context: Context){
        thread{
            if(validateConfiguration() && validatePermissions()){
                //Retrieve parameters from input fields and convert to configuration
                inputFieldsToConfiguration()

                openLoadingScreen.value = true //Show loading screen
                //Update configuration in database and generate event
                core.generateOrUpdateAlarmConfiguration(configuration)
                //Update default values in database
                core.updateDefaultAlarmValues(defaultAlarmValues)

                //Navigate to overview screen
                val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()

                core.runUpdateLogic()
            }
        }
    }

    // Composable for selecting arrival time source (manual or lecture plan)
    @Composable
    private fun InnerDatengrundlageComposable () {
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

    // Composable for selecting travel method (train or manual time)
    @Composable
    private fun InnerFahrtwegComposable() {
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
                                if(core.isInternetAvailable()){
                                    openStartStationDialog.value = true
                                }
                                else{
                                    core.showToast("Dafür ist eine Internetverbindung nötig")
                                }
                            },
                            text = startStation.value,
                            enabled = rideSelectedOption == rideOptions[0]
                        )
                        OurButtonInEditAlarm(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp, 0.dp),
                            onClick = {
                                if(core.isInternetAvailable()){
                                    openEndStationDialog.value = true
                                }
                                else{
                                    core.showToast("Dafür ist eine Internetverbindung nötig")
                                }
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

    // Composable for setting buffer times and enforcing start buffer
    @Composable
    private fun InnerZeitaufwandComposable() {
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

    // Composable for selecting valid days for the alarm
    @Composable
    private fun InnerGueltigkeitComposable(){
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
    val innerEditComposable : @Composable (PaddingValues, CoreSpecification) -> Unit =
    { innerPadding: PaddingValues, core: CoreSpecification ->
        val context = LocalContext.current
        //Open time picker dialogs when corresponding boolean set to true
        when {
            openLoadingScreen.value ->{
                LoadingScreen()
            }
        }
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
        when {
            openPermissionDialog.value -> {
                GrantPermissions(
                    onDismiss = { openPermissionDialog.value = false },
                    isFirstTime = false,
                    core = core,
                    registerForActivityResult = registerForActivityResult
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

            InnerDatengrundlageComposable()
            InnerFahrtwegComposable()
            InnerZeitaufwandComposable()
            InnerGueltigkeitComposable()
        }
    }

    // Entry point composable for the screen, includes navigation bar
    @Composable
    fun EditComposable(modifier: Modifier, core: CoreSpecification) {
        NavBar.Companion.NavigationBar(core, innerEditComposable, caller = AlarmClockEditScreen::class)
    }

    // Preview for Compose UI in Android Studio
    @Preview(showBackground = true)
    @Composable
    fun EditPreview() {
        core = MockupCore()
        defaultAlarmValues = core.getDefaultAlarmValues()!!
        reset(null)
            G2_WeckMichMalTheme {
            EditComposable(
                modifier = Modifier,
                core = MockupCore()
            )
        }
    }
}




