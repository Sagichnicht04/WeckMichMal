package de.heinzenburger.g2_weckmichmal.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.LoadingScreen
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurTextField
import de.heinzenburger.g2_weckmichmal.ui.screens.WelcomeScreen
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import kotlin.concurrent.thread


class SaveURL {
    var openLoadingScreen =  mutableStateOf(false)

    val innerSettingsComposable : @Composable (PaddingValues, CoreSpecification, () -> Unit) -> Unit = { innerPadding, core, onSave ->
        when {
            openLoadingScreen.value ->{
                LoadingScreen()
            }
        }

        var tempURL = core.getRaplaURL()
        var url = remember { mutableStateOf(tempURL ?: "") }
        var director = remember { mutableStateOf("") }
        var course = remember { mutableStateOf("") }

        Row(modifier = Modifier.fillMaxWidth()){
            OurTextField(
                value = director.value,
                onValueChange = {director.value = it.replace("\n","")},
                modifier = Modifier.padding(4.dp).fillMaxWidth(0.6f).padding(start = 8.dp, end=8.dp, top = 8.dp),
                placeholderText = "Studiengangsleiter",
            )
            OurTextField(
                value = course.value,
                onValueChange = {course.value = it.replace("\n","")},
                modifier = Modifier.padding(4.dp).fillMaxWidth(1f).padding(top = 8.dp,end=8.dp),
                placeholderText = "Kursname",
            )
        }

        OurText(
            text = "Oder",
            modifier = Modifier.padding(top = 8.dp)
        )


        OurTextField(
            value = url.value,
            onValueChange = {url.value = it.replace("\n","")},
            modifier = Modifier.padding(16.dp),
            placeholderText = "URL beginnend mit https:// oder http://",
        )

        Button(
            onClick = {
                onclickSaveButton(core, onSave, url.value, director.value, course.value)
            },
            colors = ButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.error,
                disabledContentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.padding(16.dp)
        ) {
            OurText(
                text = "Vorlesungsplan speichern",
                modifier = Modifier.padding(8.dp),
            )
        }
    }

    // Handles save button logic: validation, saving, and feedback
    fun onclickSaveButton(core: CoreSpecification, onSave: () -> Unit, url: String, director: String, course: String){
        //Inconsistency in RAPLA-URL for TINF23B2, is actually TINF23BN2...
        if(core.isInternetAvailable()){
            thread {
                // If all fields are empty, delete the saved URL
                if(url == "" && director == "" && course == ""){
                    openLoadingScreen.value = true
                    core.showToast("Vorlesungsplan gelöscht")
                    core.saveRaplaURL("")
                    onSave()
                }
                // If director and course are provided, validate and save
                else if (director != "" || course != ""){
                    if (director != "" && course != ""){
                        var realCourse = course.uppercase().replace(" ", "")
                        var realDirector = director.lowercase().replace(" ", "")
                        if(realCourse == "TINF23B2"){
                            realCourse = "TINF23BN2"
                        }
                        if(core.isValidCourseURL(realDirector, realCourse)){
                            openLoadingScreen.value = true
                            core.saveRaplaURL(realDirector, realCourse)
                            onSave()
                            core.showToast("Passt")
                        }
                        else{
                            core.showToast("Fehler in der Eingabe")
                        }
                    }
                    else{
                        core.showToast("Bitte Studiengangsleiter und Kursname angeben.")
                    }
                }
                // Otherwise, validate and save the direct URL
                else {
                    var updatedUrl = url.replace("page=calendar","page=ical")
                    updatedUrl = updatedUrl.replace("TINF23B2","TINF23BN2")
                    if (updatedUrl == "" || core.isValidCourseURL(updatedUrl)) {
                        openLoadingScreen.value = true
                        core.saveRaplaURL(updatedUrl)
                        onSave()
                        core.showToast("Passt")
                    } else {
                        core.showToast("Fehler in der Eingabe")
                    }
                }

            }
        }
        else{
            core.showToast("Aufgrund der Validierung ist eine Internetverbindung nötig")
        }
    }
}

// Preview for Compose UI
@Preview(showBackground = true)
@Composable
fun SaveURLPreview() {
    val welcomeScreen = WelcomeScreen()
    G2_WeckMichMalTheme {
        welcomeScreen.Greeting(
            modifier = Modifier,
            core = MockupCore()
        )
    }
}