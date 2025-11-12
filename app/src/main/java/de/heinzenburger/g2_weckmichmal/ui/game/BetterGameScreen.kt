package de.heinzenburger.g2_weckmichmal.ui.game

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getDrawable
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import de.heinzenburger.g2_weckmichmal.R
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.NavBar
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import java.time.format.DateTimeFormatter
import java.util.Random
import androidx.lifecycle.viewmodel.compose.viewModel
import de.heinzenburger.g2_weckmichmal.ui.game.Pets.Companion.Fish
import de.heinzenburger.g2_weckmichmal.ui.screens.AlarmClockOverviewScreen
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

// Main Activity for the Settings screen
class BetterGameScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BackHandler {
                val intent = Intent(applicationContext, AlarmClockOverviewScreen::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }
            G2_WeckMichMalTheme {
                GameComposable(modifier = Modifier, Core(applicationContext))
            }
        }
    }


    // Wrapper function for the Settings Composable with NavBar
    @Composable
    fun GameComposable(modifier: Modifier, uiActions: CoreSpecification) {
        NavBar.Companion.NavigationBar(uiActions, remember{innerGameComposable}, BetterGameScreen::class)
    }

    //Main component
    val innerGameComposable : @Composable (PaddingValues, CoreSpecification) -> Unit = { innerPadding: PaddingValues, core: CoreSpecification ->
        val viewModel = viewModel<ScreenModel>()
        LaunchedEffect(Unit) {
            viewModel.initialize(core)
        }
        val context = LocalContext.current

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(top=48.dp,start=8.dp,end=8.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            OurText(
                text = "Belohnungen: " + viewModel.coins.intValue,
//                text = "Belohnungen: " + coins.intValue.toString(),
                modifier = Modifier.padding(bottom = 16.dp),
            )

            viewModel.configurations.forEachIndexed { index, configurationWithEvent ->
                Aquarium(configurationWithEvent,
                    animations = viewModel.loadAquariumAnimations(context, "color_0_1"),
                    onEnter = {viewModel.setEditScreen(context, configurationWithEvent.configuration)},
                    onConfigurationActiveUpdate = {isConfigurationActive -> viewModel.updateConfigurationActive(isConfigurationActive, configurationWithEvent.configuration)}
                )
            }

            Button(
                onClick = {
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.secondary,
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .padding(top=16.dp)
                    .size(69.dp)
                    .align(alignment = Alignment.CenterHorizontally)
                    .border(2.dp, Color.Transparent,
                        RoundedCornerShape(50))

            ) {
                Text(text = "+", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }


    @Composable
    fun Aquarium(configurationWithEvent: ConfigurationWithEvent,
                 animations: List<List<Bitmap>>,
                 onEnter: () -> Unit,
                 onConfigurationActiveUpdate: (isConfigurationActive: Boolean) -> Unit){
        Box(
            modifier = Modifier.fillMaxWidth()
        ){
            EmptyAquarium(configurationWithEvent, onEnter, onConfigurationActiveUpdate)
            Fish(animations)
            Fish(animations)
            Fish(animations)
        }
    }



    @Composable
    fun EmptyAquarium(configurationWithEvent: ConfigurationWithEvent,
                      onEnter: () -> Unit,
                      onConfigurationActiveUpdate: (isConfigurationActive: Boolean) -> Unit){
        var isAquariumOn by remember { mutableStateOf(configurationWithEvent.configuration.isActive) }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            Image(
                modifier = Modifier.fillMaxWidth()
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onEnter()
                    },
                painter = rememberDrawablePainter(
                    drawable = getDrawable(
                        LocalContext.current,
                        if (isAquariumOn) {
                            R.drawable.aquarium
                        } else {
                            R.drawable.aquariumoff
                        }
                    )
                ),
                contentDescription = "Loading animation",
                contentScale = ContentScale.FillWidth,
            )
            Box(
                modifier = Modifier
                    .align(alignment = BiasAlignment(0f, -0.75f))
                    .background(Color(60, 138, 200))
                    .border(2.dp, color = Color(59, 56, 164))
            ) {
                Text(
                    text = "${
                        configurationWithEvent.event?.wakeUpTime?.format(
                            DateTimeFormatter.ofPattern(
                                "HH:mm"
                            )
                        )
                    }",
                    color = Color(59, 56, 164),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp, 2.dp),
                    fontFamily = FontFamily(Font(R.font.digital))
                )
            }
            Box(
                modifier = Modifier
                    .align(alignment = BiasAlignment(0f, -0.95f))
                    .size(50.dp, 15.dp)
                    .clickable(
                        indication = LocalIndication.current,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        isAquariumOn = !isAquariumOn
                        onConfigurationActiveUpdate(isAquariumOn)
                    }
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .align(alignment = BiasAlignment(0f, 0.85f))
                    .background(Color(201, 201, 201))
                    .border(2.dp, color = Color(130, 130, 130), shape = RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = configurationWithEvent.configuration.name,
                    fontSize = 15.sp,
                    color = Color(130, 130, 130),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(6.dp, 1.dp),
                )
            }
        }
    }

    //Preview UI in Android Studio
    @Preview()
    @Composable
    fun GameScreenPreview() {
        val gameScreen = BetterGameScreen()
        val core = MockupCore()
        G2_WeckMichMalTheme {
            gameScreen.GameComposable(modifier = Modifier, core)
        }
    }
}


