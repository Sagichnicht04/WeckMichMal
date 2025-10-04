package de.heinzenburger.g2_weckmichmal.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.ExceptionHandler
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.NavBar
import de.heinzenburger.g2_weckmichmal.ui.components.PickerDialogs.Companion.ConfirmDialog
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

//Screen to display and upload the log of the app
class LogScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        ExceptionHandler(core).runWithUnexpectedExceptionHandler("Error displaying Log",true) {
            setContent {
                val context = LocalContext.current
                // Handles back navigation to the overview screen
                BackHandler {
                    val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    finish()
                }
                G2_WeckMichMalTheme {
                    LogComposable(modifier = Modifier, core)
                }
            }
        }
    }
    private val showConfirmDialog = mutableStateOf(false)

    // Composable function to display the log and a button to upload it
    val innerLogComposable: @Composable (PaddingValues, CoreSpecification) -> Unit =
        { innerPadding: PaddingValues, core: CoreSpecification ->
            val context = LocalContext.current
            when {
                showConfirmDialog.value -> {
                    // Show confirmation dialog for uploading logs
                    ConfirmDialog(
                        onConfirm = {
                            if(core.isInternetAvailable()){
                                // Upload the log file to the server in a background thread
                                thread {
                                    try {
                                        val file = File(context.filesDir, "log")

                                        val requestBody = MultipartBody.Builder()
                                            .setType(MultipartBody.FORM)
                                            .addFormDataPart("logFile", "file", file.asRequestBody())
                                            .build()

                                        val request = Request.Builder()
                                            .url("https://log.heinzenburger.de/submit")
                                            .post(requestBody)
                                            .build()

                                        OkHttpClient().newCall(request).execute().use { response ->
                                            if (!response.isSuccessful) throw IOException("Unexpected code $response")
                                            val responseText = response.body?.string()
                                            core.showToast(responseText!!)
                                        }
                                    }
                                    catch (e: Exception){
                                        core.log(Logger.Level.SEVERE, e.message.toString())
                                        core.showToast("Das hat nicht geklappt")
                                    }
                                }
                            }
                            else{
                                core.showToast("Dafür ist eine Internetverbindung nötig")
                            }
                            showConfirmDialog.value = false
                        },
                        onDismiss = {
                            showConfirmDialog.value = false
                        }
                    )
                }
            }

            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
            ) {


                // Button to show privacy policy and enable easter egg
                Button(
                    modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    onClick = {
                    }
                ) {
                    Text(
                        style = MaterialTheme.typography.titleSmall,
                        text = "Datenschutzerklärung unter fzuerner.com/privacy",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                // Button to trigger log upload confirmation dialog
                Button(
                    onClick = {
                        showConfirmDialog.value = true
                    },
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 10.dp)
                        .align(Alignment.CenterHorizontally),
                    contentPadding = PaddingValues(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    OurText(text="Logs zur Analyse an Server senden", modifier = Modifier.padding(16.dp))
                }

                // Displays the log content
                OurText(
                    text = core.getLog(),
                    modifier = Modifier,
                )
            }
        }
    // Wrapper composable for navigation bar and log content
    @Composable
    fun LogComposable(modifier: Modifier, core: CoreSpecification) {
        NavBar.Companion.NavigationBar(
            modifier,
            core,
            innerLogComposable,
            caller = SettingsScreen::class
        )
    }
}


// Preview UI in Android Studio
@Preview(showBackground = true)
@Composable
fun LogPreview() {
    val logScreen = LogScreen()
    G2_WeckMichMalTheme {
        logScreen.LogComposable(
            modifier = Modifier,
            core = MockupCore()
        )
    }
}
