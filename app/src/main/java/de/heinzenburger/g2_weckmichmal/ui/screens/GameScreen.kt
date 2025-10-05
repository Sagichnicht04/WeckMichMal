package de.heinzenburger.g2_weckmichmal.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getDrawable
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import de.heinzenburger.g2_weckmichmal.R
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.ExceptionHandler
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.NavBar
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import java.util.Random
import kotlin.concurrent.thread

// Main Activity for the Settings screen
class GameScreen : ComponentActivity() {
    private var coins = mutableIntStateOf(0)
    private var images = mutableStateListOf<android.graphics.Bitmap?>()
    private var positions = mutableStateListOf<Pair<Int,Int>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val core = Core(context = applicationContext)
        ExceptionHandler(core).runWithUnexpectedExceptionHandler("Error displaying Game screen",true) {
            // Load courses and excluded courses in a background thread
            thread {
                coins.intValue = core.getCoins() ?: 0
            }
            setContent {
                val context = LocalContext.current
                if(images.isEmpty()){
                    thread {
                        animate("color_0_0", context)
                    }
                }
                // Handle back navigation to overview screen
                BackHandler {
                    //Go to Overview Screen without animation
                    val intent = Intent(context, AlarmClockOverviewScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    finish()
                }
                G2_WeckMichMalTheme {
                    GameComposable(modifier = Modifier, core)
                }
            }
        }
    }

    fun animate(color: String,context: Context){
        context.assets.open("fish/$color/animation_0/0.png").use { inputStream ->
            images.add(BitmapFactory.decodeStream(inputStream))
        }
        val index = images.size-1
        for (i in 0..10){
            Thread.sleep(3000)
            val random = Random()
            val animation = "animation_${random.nextInt(8)}"
            for(i in 0..3) {
                context.assets.open("fish/$color/$animation/$i.png").use { inputStream ->
                    images[index] = BitmapFactory.decodeStream(inputStream)
                }
                Thread.sleep(500)
            }
        }
    }

    // Wrapper function for the Settings Composable with NavBar
    @Composable
    fun GameComposable(modifier: Modifier, uiActions: CoreSpecification) {
        NavBar.Companion.NavigationBar(modifier, uiActions, innerGameComposable, GameScreen::class)
    }

    //Main component
    val innerGameComposable : @Composable (PaddingValues, CoreSpecification) -> Unit = { innerPadding: PaddingValues, core: CoreSpecification ->

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(top=48.dp,start=8.dp,end=8.dp)
                .background(MaterialTheme.colorScheme.background)) {
            OurText(
                text = "Belohnungen: " + coins.intValue.toString(),
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = rememberDrawablePainter(
                    drawable = getDrawable(
                        LocalContext.current,
                        R.drawable.aquarium
                    )
                ),
                contentDescription = "Loading animation",
                contentScale = ContentScale.FillWidth,
            )
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
            images.forEach { image ->
                if(image != null) {
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = "Fish",
                        modifier = Modifier,
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

//Preview UI in Android Studio
@Preview(name = "NEXUS_7", device = Devices.NEXUS_7)
@Composable
fun GameScreenPreview() {
    val gameScreen = GameScreen()
    G2_WeckMichMalTheme {
        gameScreen.GameComposable(modifier = Modifier, MockupCore())
    }
}
