package de.heinzenburger.g2_weckmichmal.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var images = mutableStateListOf<android.graphics.Bitmap?>()
    var positions = mutableStateListOf<Pair<Float,Float>>()

    var aquariumState = mutableListOf<Boolean>(true)

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
                    animate("color_0_0", context)
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
        thread{
            context.assets.open("fish/$color/animation_0/0.png").use { inputStream ->
                images.add(BitmapFactory.decodeStream(inputStream))
            }
            positions.add(Pair(0f,0f))
            val index = images.size-1
            val random = Random()

            while (true){
                val newPositions = mutableListOf<Float>()
                newPositions.add(positions[index].first)
                newPositions.add(positions[index].second)
                for(i in 0..1){
                    newPositions[i] = if(newPositions[i]<-0.69f){
                        newPositions[i] + (random.nextFloat() * 0.2f + 0.2f)
                    } else if(newPositions[i]>0.69f){
                        newPositions[i] - (random.nextFloat() * 0.2f + 0.2f)
                    } else if(newPositions[i]>0){
                        if(random.nextInt(10) > 8){
                            newPositions[i] + (random.nextFloat() * 0.2f + 0.1f)
                        } else{
                            newPositions[i] - (random.nextFloat() * 0.2f + 0.5f)
                        }
                    } else {
                        if(random.nextInt(10) > 8){
                            newPositions[i] - (random.nextFloat() * 0.2f + 0.1f)
                        } else{
                            newPositions[i] + (random.nextFloat() * 0.2f + 0.5f)
                        }
                    }
                }
                positions[index] = Pair(newPositions.first(), newPositions.last())

                val animation = "animation_${random.nextInt(8)}"
                for(j in 0 .. 3){
                    for(i in 0..3) {
                        context.assets.open("fish/$color/$animation/$i.png").use { inputStream ->
                            images[index] = BitmapFactory.decodeStream(inputStream)
                        }
                        Thread.sleep(250)
                    }
                }
            }
        }
    }

    // Wrapper function for the Settings Composable with NavBar
    @Composable
    fun GameComposable(modifier: Modifier, uiActions: CoreSpecification) {
        NavBar.Companion.NavigationBar(uiActions, remember{innerGameComposable}, GameScreen::class)
    }

    //Main component
    val innerGameComposable : @Composable (PaddingValues, CoreSpecification) -> Unit = { innerPadding: PaddingValues, core: CoreSpecification ->

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(top=48.dp,start=8.dp,end=8.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            OurText(
                text = "Belohnungen: " + coins.intValue.toString(),
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ){
                Image(
                    modifier = Modifier.fillMaxWidth(),
                    painter = rememberDrawablePainter(
                        drawable = getDrawable(
                            LocalContext.current,
                            if(aquariumState.first()){
                                    R.drawable.aquarium
                                }
                                else{
                                    R.drawable.aquariumoff
                                }
                        )
                    ),
                    contentDescription = "Loading animation",
                    contentScale = ContentScale.FillWidth,
                )
                Box(
                    modifier = Modifier
                        .align(alignment = BiasAlignment(0f,-0.75f))
                        .background(Color(60,138,200))
                        .border(2.dp, color = Color(59,56,164))
                ){
                    Text(
                        text = "08:30",
                        color = Color(59,56,164),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(4.dp, 2.dp),
                        fontFamily = FontFamily(Font(R.font.digital))
                    )
                }
                Box(
                    modifier = Modifier
                        .align(alignment = BiasAlignment(0f,-0.95f))
                        .size(50.dp,15.dp)
                        .clickable(
                            indication = LocalIndication.current,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            aquariumState[0] = !aquariumState.first()
                        }
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .align(alignment = BiasAlignment(0f,0.85f))
                        .background(Color(201,201,201))
                        .border(2.dp, color = Color(130,130,130), shape=RoundedCornerShape(8.dp))
                ){
                    Text(
                        text = "DHBW",
                        fontSize = 15.sp,
                        color = Color(130,130,130),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(6.dp, 1.dp),
                    )
                }
                images.forEachIndexed { index, image ->
                    if(image != null) {
                        val animatedXPosition by animateFloatAsState(
                            targetValue = positions[index].first,
                            animationSpec = tween(
                                durationMillis = 4000,
                                easing = LinearEasing
                            ),
                        )
                        val animatedYPosition by animateFloatAsState(
                            targetValue = positions[index].second,
                            animationSpec = tween(
                                durationMillis = 4000,
                                easing = LinearEasing
                            ),
                        )
                        Image(
                            bitmap = image.asImageBitmap(),
                            contentDescription = "Fish",
                            modifier = Modifier
                                .align(BiasAlignment(animatedXPosition, animatedYPosition))
                                .clickable(
                                    indication = LocalIndication.current,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                }
                            ,
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
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
}

//Preview UI in Android Studio
@Preview(name = "NEXUS_7", device = Devices.NEXUS_6P)
@Composable
fun GameScreenPreview() {
    val gameScreen = GameScreen()
    G2_WeckMichMalTheme {
        LocalContext.current.assets.open("fish/color_0_0/animation_0/0.png").use { inputStream ->
            gameScreen.images.add(BitmapFactory.decodeStream(inputStream))
            gameScreen.positions.add(Pair(0f,0f))
        }
        gameScreen.GameComposable(modifier = Modifier, MockupCore())
    }
}
