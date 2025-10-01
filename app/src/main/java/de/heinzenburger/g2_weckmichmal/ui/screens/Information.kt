package de.heinzenburger.g2_weckmichmal.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.specifications.MensaMeal
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.NavBar
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import kotlin.concurrent.thread
import androidx.core.net.toUri
import de.heinzenburger.g2_weckmichmal.core.ExceptionHandler

// Activity displaying the Information screen
class InformationScreen : ComponentActivity() {
    lateinit var core: CoreSpecification
    private lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        context = applicationContext
        core = Core(context)
        ExceptionHandler(core as Core).runWithUnexpectedExceptionHandler("Error displaying Information",true) {
            setContent {
                val context = LocalContext.current
                // Custom back navigation to Overview screen
                BackHandler {
                    //Go to Overview Screen without animation
                    val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    finish()
                }
                G2_WeckMichMalTheme {
                    InformationComposable(modifier = Modifier)
                }
            }
        }
    }

    // Composable for the main content of the Information screen
    private val innerInformationComposable: @Composable (PaddingValues, CoreSpecification) -> Unit =
        { innerPadding: PaddingValues, core: CoreSpecification ->
            Column(modifier = Modifier.padding(innerPadding).fillMaxWidth()) {
                OurText(
                    text = "Info",
                    modifier = Modifier.padding(16.dp)
                )
                // Button to open course schedule in browser
                Button(
                    modifier = Modifier.align(BiasAlignment.Horizontal(0f)).padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    onClick = {
                        openCoursesInBrowser()
                    }
                ) {
                    OurText(text = "Vorlesungsplan im Browser öffnen", modifier = Modifier)
                }
                // Button to open mensa menu in browser
                Button(
                    modifier = Modifier.align(BiasAlignment.Horizontal(0f)).padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    onClick = {
                        openMensaInBrowser()
                    }
                ) {
                    OurText(text = "Mensaplan im Browser öffnen", modifier = Modifier)
                }
                if(core.isInternetAvailable()){
                    InnerMensaComposable(innerPadding)
                }
                else{
                    core.showToast("Mensa Essensplan kann nur bei aktiver Internetverbindung angezeigt werden.")
                }

            }
        }



    // Composable to display the list of upcoming mensa meals
    @SuppressLint("DefaultLocale")
    @Composable
    fun InnerMensaComposable(innerPadding: PaddingValues){
        val mensaMeals = remember { mutableStateOf(emptyList<MensaMeal>()) }
        thread {
            mensaMeals.value = core.nextMensaMeals()!!
        }
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.onBackground,
                    RoundedCornerShape(16.dp)
                )
                .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                style = MaterialTheme.typography.titleSmall,
                text = "Mensa Essensplan",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(10.dp).align(BiasAlignment.Horizontal(0f))
            )
            Column {
                mensaMeals.value.forEach { meal ->
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        OurText(
                            text = meal.name,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.padding(16.dp).fillMaxWidth(0.65f),
                        )
                        OurText(
                            text =
                                if(meal.price > 0){
                                    String.format("%.2f €", meal.price)
                                }
                                else{
                                    ""
                                },
                            textAlign = TextAlign.Right,
                            modifier = Modifier.padding(top=16.dp, bottom = 16.dp, end = 16.dp).fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
    // Composable wrapper for the screen with navigation bar
    @Composable
    fun InformationComposable(modifier: Modifier) {
        NavBar.Companion.NavigationBar(
            modifier,
            core,
            innerInformationComposable,
            caller = InformationScreen::class
        )
    }
    // Preview for UI design in Android Studio
    @Preview(showBackground = true)
    @Composable
    fun InformationPreview() {
        core = MockupCore()
        G2_WeckMichMalTheme {
            InformationComposable(
                modifier = Modifier,
            )
        }
    }

    // Opens the mensa menu in the browser
    fun openMensaInBrowser(){
        var url = "https://www.sw-ka.de/de/hochschulgastronomie/speiseplan/mensa_erzberger/?view=ok&c=erzberger&STYLE=popup_plain"
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    // Opens the course schedule in the browser, adjusting the URL if needed
    private fun openCoursesInBrowser() {
        var url = core.getRaplaURL()
        if (url != null && url != ""){
            url = url.replace("page=ical","page=calendar")
            if(!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://$url"
            }
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
}



