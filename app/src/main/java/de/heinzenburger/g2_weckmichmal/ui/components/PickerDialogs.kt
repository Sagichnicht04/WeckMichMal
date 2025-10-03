package de.heinzenburger.g2_weckmichmal.ui.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.NumberField
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurTextField
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import java.util.Calendar
import kotlin.concurrent.thread

// PickerDialogs provides various dialog components for user input and permissions
class PickerDialogs {
    companion object{
        //Dialogs for Picking LocalTime and Minutes
        @Composable
        fun MinutePickerDialog(
            onConfirm: (Int) -> Unit,
            onDismiss: () -> Unit,
            default: Int?
        ) {
            val selectedNumber = remember { mutableStateOf(default) }
            Dialog(onDismissRequest = { onDismiss() }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(375.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Zeit in Minuten:",
                            modifier = Modifier.padding(16.dp),
                        )

                        Box(
                            Modifier.fillMaxWidth(0.5f)
                        ){
                            NumberField(
                                text = selectedNumber,
                                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.2f)
                            )
                            Column(
                                Modifier.align(BiasAlignment(1f,0f))
                            )
                            {
                                IconButton(
                                    onClick = {
                                                if(selectedNumber.value == null){
                                                    selectedNumber.value = 1
                                                }
                                                else{
                                                    selectedNumber.value = (selectedNumber.value as Int)+1
                                                }
                                              },
                                    modifier = Modifier.fillMaxHeight(0.1f)
                                ) {
                                    Icon(Icons.Filled.ArrowDropUp,
                                        contentDescription = "Add 1",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if(selectedNumber.value == null){
                                            selectedNumber.value = -1
                                        }
                                        else{
                                            selectedNumber.value = (selectedNumber.value as Int)-1
                                        }
                                    },
                                    modifier = Modifier.fillMaxHeight(0.12f)
                                )
                                {
                                    Icon(Icons.Filled.ArrowDropDown,
                                        contentDescription = "Substract 1",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            TextButton(
                                onClick = { onDismiss() },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text("Dismiss", color = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(
                                onClick = {
                                    onConfirm(selectedNumber.value ?: 0)
                                          },
                                modifier = Modifier.padding(8.dp),
                            ) {
                                Text("Confirm", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // Dialog for picking a station name with suggestions
        @Composable
        fun StationPickerDialog(
            onConfirm: (String) -> Unit,
            onDismiss: () -> Unit,
            core: CoreSpecification
        ) {
            Dialog(onDismissRequest = { onDismiss() }) {
                val station = remember { mutableStateOf("") }
                val stationPrediction = remember { mutableStateOf("") }
                val stationPredictions = remember { mutableStateOf(listOf("--")) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(375.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        OurTextField(
                            value = station.value,
                            onValueChange = {
                                station.value = it.replace("\n","")
                                thread{
                                    val currentStation = station.value
                                    Thread.sleep(500)
                                    if(currentStation == station.value){
                                        if(station.value.length > 2){
                                            try {
                                                stationPredictions.value = core.deriveStationName(it)!!
                                            }
                                            catch (e: Exception){
                                                core.log(Logger.Level.SEVERE, e.message.toString())
                                                core.log(Logger.Level.SEVERE, e.stackTraceToString())
                                                core.showToast("Da hat etwas nicht geklappt")
                                            }
                                        }
                                    }
                                } },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            placeholderText = "Start eingeben"
                        )
                        Text(
                            text = "Vorschläge:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 16.dp),
                        )

                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ){
                            stationPredictions.value.forEachIndexed {
                                    index, it ->
                                Button(
                                    onClick = {
                                        stationPrediction.value = it
                                        if(stationPrediction.value != "" && stationPrediction.value != "--"){
                                            onConfirm(stationPrediction.value)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.onBackground
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                ){
                                    OurText(
                                        text = it,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog for picking a time using TimePicker
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun TimePickerDialogContainer(
            onConfirm: (TimePickerState) -> Unit,
            onDismiss: () -> Unit,
        ) {
            val currentTime = Calendar.getInstance()

            val timePickerState = rememberTimePickerState(
                initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
                initialMinute = currentTime.get(Calendar.MINUTE),
                is24Hour = true,
            )

            AlertDialog(
                containerColor = MaterialTheme.colorScheme.background,
                onDismissRequest = onDismiss,
                dismissButton = {
                    TextButton(
                        onClick = { onDismiss() }
                    ) {
                        Text("Abbrechen", color = MaterialTheme.colorScheme.secondary)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onConfirm(timePickerState) }) {
                        Text("Bestätigen", color = MaterialTheme.colorScheme.secondary)
                    }
                },
                text = { TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.onBackground,
                        selectorColor = MaterialTheme.colorScheme.secondary,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.primary,
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.primary,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.onBackground,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.onBackground,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.primary,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) }
            )
        }

        // Confirmation dialog for sharing sensitive data
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun ConfirmDialog(
            onConfirm: () -> Unit,
            onDismiss: () -> Unit,
        ) {
            AlertDialog(
                containerColor = MaterialTheme.colorScheme.background,
                onDismissRequest = onDismiss,
                dismissButton = {
                    TextButton(
                        onClick = { onDismiss() }
                    ) {
                        Text("Abbrechen", color = MaterialTheme.colorScheme.secondary)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { onConfirm() }) {
                        Text("Bestätigen", color = MaterialTheme.colorScheme.secondary)
                    }
                },
                text = {
                    OurText(
                        text = "Bist du sicher, dass du diese Daten mit den Entwicklern teilen möchtest? Sie könnten sensible Daten enthalten, z.B. deine Startstation, deine Kurse und Aufwachzeiten. Es werden keine Daten weitergegeben, jedoch kann kein vollständiger Schutz vor unbefugtem Zugriff gewährt werden.",
                        modifier = Modifier
                    )
                }
            )
        }


        // Dialog for excluding courses from a list
        @Composable
        fun ExcludeCourseDialog(
            onDismiss: (List<String>) -> Unit,
            listOfCourses: List<String>,
            listOfExcludedCourses: List<String>,
        ) {
            val excludeCourses = remember { mutableStateListOf<String>()}
            listOfExcludedCourses.forEach {
                excludeCourses.add(it)
            }
            val excludeCourse = remember { mutableStateOf("") }
            val proposeCourses = remember { mutableStateOf(listOfCourses) }

            fun excludeCourse(it: String){
                var isCourseAlreadyExcluded = false
                excludeCourses.forEach {
                        excludeCourse ->
                    if(it == excludeCourse){
                        isCourseAlreadyExcluded = true
                    }
                }
                if(!isCourseAlreadyExcluded){
                    excludeCourses.add(it)
                }
            }

            Dialog(
                onDismissRequest = { onDismiss(excludeCourses) }
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    if(excludeCourses.isNotEmpty()){
                        Text(
                            text = "Ausgeschlossen:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                            .fillMaxWidth(0.95f),
                    ){

                        excludeCourses.forEach {
                            Button(
                                onClick = {
                                    excludeCourses.remove(it)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.padding(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ){
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        OurTextField(
                            value = excludeCourse.value,
                            onValueChange = {
                                excludeCourse.value = it
                                val newProposeCourseList = mutableListOf<String>()
                                listOfCourses.forEach {
                                    if(it.lowercase().contains(excludeCourse.value.lowercase()) == true){
                                        newProposeCourseList.add(it)
                                    }
                                }
                                proposeCourses.value = newProposeCourseList
                                            },
                            modifier = Modifier.fillMaxWidth(0.8f).padding(top = 24.dp),
                            placeholderText = "Kursname eingeben"
                        )
                        Text(
                            text = "Vorschläge:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 16.dp),
                        )

                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ){
                            proposeCourses.value.forEachIndexed {
                                    index, it ->
                                if(!excludeCourses.contains(it)) {
                                    Button(
                                        onClick = {
                                            excludeCourse(it)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onBackground
                                        ),
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        OurText(
                                            text = it,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog for requesting and explaining permissions
        @Composable
        fun GrantPermissions(
            onDismiss: () -> Unit,
            isFirstTime: Boolean,
            core: CoreSpecification,
            registerForActivityResult: ActivityResultLauncher<String>
        ) {
            // State for granted permissions and dialog dismissal
            val permissions = remember { mutableStateOf(core.getGrantedPermissions()) }
            val dismiss = remember { mutableStateOf(false) }
            val ignorePermission = remember { mutableStateListOf("") }
            val context = LocalContext.current
            Dialog(
                onDismissRequest = {
                    dismiss.value = true
                    onDismiss()
                }
            ) {
                // Background thread updates permission state
                thread {
                    while(!dismiss.value){
                        Thread.sleep(500)
                        permissions.value = core.getGrantedPermissions()
                    }
                }
                Card(
                    // UI for requesting each required permission
                    // Shows explanation and launches appropriate system intent
                    // User can skip a permission
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isFirstTime) "Berechtigungen erteilen" else "Berechtigungen fehlen!",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(8.dp).fillMaxWidth().padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        var currentPermission = ""
                        var title = ""
                        var text = ""
                        var buttonAction = {
                            onDismiss()
                        }
                        if (ignorePermission.contains(Manifest.permission.SCHEDULE_EXACT_ALARM) == false &&
                            permissions.value?.contains("Alarm") == false) {
                            currentPermission = Manifest.permission.SCHEDULE_EXACT_ALARM
                            title = "Alarme setzen"
                            text = "Diese App benötigt zwingend die Berechtigung Alarme im System zu erstellen."
                            buttonAction = {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = "package:${context.packageName}".toUri()
                                }
                                context.startActivity(intent)
                            }
                        } else if (ignorePermission.contains(Manifest.permission.ACCESS_NOTIFICATION_POLICY) == false &&
                            permissions.value?.contains("Notifications") == false) {
                            currentPermission = Manifest.permission.ACCESS_NOTIFICATION_POLICY
                            title = "Benachrichtigungen senden"
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val isPermanentlyDenied =
                                    !ActivityCompat.shouldShowRequestPermissionRationale(
                                        context as Activity, Manifest.permission.POST_NOTIFICATIONS
                                    )
                                if(!isPermanentlyDenied){
                                    text = "Diese App benötigt zwingend die Berechtigung Benachrichtigungen zu senden."
                                    buttonAction = {
                                        registerForActivityResult.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                                else{
                                    text = "Erlaube Benachrichtigungen in den Einstellungen - Ansonsten kann der Wecker nicht richtig funktionieren."
                                    buttonAction = {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        } else if (ignorePermission.contains(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == false &&
                            permissions.value?.contains("Battery") == false) {
                            currentPermission = Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                            title = "Uneingeschränkte Hintergrundnutzung"
                            text = "Die App läuft am zuverlässigsten, wenn uneingeschränkte Hintergrundnutzung erlaubt ist."
                            buttonAction = {
                                val intent = Intent()
                                intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                                context.startActivity(intent)
                            }
                        }
                        else{
                            onDismiss()
                        }
                        Text(
                            text = title,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(8.dp).fillMaxWidth().padding(8.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = text,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(8.dp).fillMaxWidth().padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onBackground
                            ),
                            onClick = {
                                buttonAction()
                            }
                        ) {
                            OurText(
                                text = "Berechtigung erteilen",
                                modifier = Modifier
                            )
                        }

                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            onClick = {
                                ignorePermission.add(currentPermission)
                            }
                        ) {
                            OurText(
                                text = "Überspringen",
                                modifier = Modifier
                            )
                        }
                    }
                }
            }
        }
    }
}


// Preview for the desired dialog
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    G2_WeckMichMalTheme {
        PickerDialogs.MinutePickerDialog(
            { minutes: Int ->
            },
            {
            },
            default = 0,
        )
    }
}