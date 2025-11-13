package de.heinzenburger.g2_weckmichmal.ui.game

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Random
import kotlin.time.Duration.Companion.seconds

class Pets {
    companion object {
        @Composable
        fun Fish(animations: List<List<Bitmap>>) {
            val random = Random()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .height(150.dp)
            ) {
                var position by remember { mutableStateOf(Pair(random.nextInt(100)/100f -0.5f, random.nextInt(100)/100f -0.5f)) }
                var currentImage by remember { mutableStateOf(animations[0][0]) }
                var isLookingLeft by remember { mutableStateOf(true) }
                val animatedXPosition by animateFloatAsState(
                    targetValue = position.first,
                    animationSpec = tween(
                        durationMillis = 4000,
                        easing = LinearEasing
                    ),
                )
                val animatedYPosition by animateFloatAsState(
                    targetValue = position.second,
                    animationSpec = tween(
                        durationMillis = 4000,
                        easing = LinearEasing
                    ),
                )
                Image(
                    bitmap = currentImage.asImageBitmap(),
                    contentDescription = "Fish",
                    modifier = Modifier
                        .align(BiasAlignment(animatedXPosition, animatedYPosition))
                        .clickable(
                            indication = LocalIndication.current,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                        }
                        .graphicsLayer(
                            scaleX = if (isLookingLeft){1f} else {-1f}
                        ),
                    contentScale = ContentScale.Fit,
                )

                LaunchedEffect(Unit) {
                    while (true) {

                        if(random.nextInt(5)>2) {
                            val newPosition = position.toList().toMutableList()
                            for (i in 0..1) {
                                newPosition[i] = if (newPosition[i] < -0.69f) {
                                    newPosition[i] + (random.nextFloat() * 0.2f + 0.2f)
                                } else if (newPosition[i] > 0.69f) {
                                    newPosition[i] - (random.nextFloat() * 0.2f + 0.2f)
                                } else if (newPosition[i] > 0) {
                                    if (random.nextInt(10) > 8) {
                                        newPosition[i] + (random.nextFloat() * 0.2f + 0.1f)
                                    } else {
                                        newPosition[i] - (random.nextFloat() * 0.2f + 0.5f)
                                    }
                                } else {
                                    if (random.nextInt(10) > 8) {
                                        newPosition[i] - (random.nextFloat() * 0.2f + 0.1f)
                                    } else {
                                        newPosition[i] + (random.nextFloat() * 0.2f + 0.5f)
                                    }
                                }
                            }
                            isLookingLeft = newPosition.first() < position.first
                            position = Pair(newPosition.first(), newPosition.last())

                            for (j in 0..3) {
                                for (i in 0..3) {
                                    currentImage = animations[1][i]
                                    delay(250L)
                                }
                            }
                        }
                        else if (random.nextInt(5)>2){
                            val animation = random.nextInt(3) + 2
                            for (i in 0..3) {
                                currentImage = animations[animation][i]
                                delay(250L)
                            }
                        }

                        for(j in 0..random.nextInt(10) + 3){
                                currentImage = animations[0][j%4]
                                delay(250L)
                        }
                    }
                }
            }
        }
    }
}