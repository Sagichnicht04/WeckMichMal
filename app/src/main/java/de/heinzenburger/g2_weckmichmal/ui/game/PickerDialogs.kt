package de.heinzenburger.g2_weckmichmal.ui.game

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.components.BasicElements.Companion.OurText
import de.heinzenburger.g2_weckmichmal.ui.components.PickerDialogs
import de.heinzenburger.g2_weckmichmal.ui.theme.G2_WeckMichMalTheme
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.floor

class PickerDialogs {
    companion object{
        @Composable
        fun ShopDialog(
            onDismiss: () -> Unit,
            coins: Int,
            fishImages: List<Bitmap>,
            onBuy: (color: Int) -> Unit
        ) {
            var selectedFish by remember { mutableIntStateOf(0) }
            Dialog(onDismissRequest = { onDismiss() }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(375.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    OurText(
                        text = "WBugs: $coins",
                        modifier = Modifier.padding(8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        OurText(
                            text = "Shop",
                            modifier = Modifier
                        )
                        for(first in 0 .. 3){
                            Row {
                                for(second in 0 ..  2){
                                    Image(
                                        bitmap = fishImages[first*3 + second].asImageBitmap(),
                                        contentDescription = "Fish",
                                        modifier = Modifier
                                            .clickable(
                                                indication = LocalIndication.current,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) {
                                                selectedFish = first*3 + second
                                            }
                                            .size(48.dp)
                                            .border(
                                                if(first*3 + second == selectedFish){
                                                    2.dp
                                                } else {
                                                    0.dp
                                                }
                                                , MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)
                                            ),
                                        contentScale = ContentScale.Fit,
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if(coins >= 3) {
                                    onBuy(selectedFish)
                                }
                                      },
                            modifier = Modifier.align(BiasAlignment.Horizontal(0f)).padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    if(coins >= 3){
                                        MaterialTheme.colorScheme.secondary
                                    }
                                    else{
                                        MaterialTheme.colorScheme.error
                                    }
                            ),
                        ) {
                            OurText(text = "Kaufen (3 WBugs)", modifier = Modifier)
                        }
                    }
                }
            }
        }
    }
    // Preview for the desired dialog
    @Preview(showBackground = true)
    @Composable
    fun ShopPreview() {
        val fishImages = mutableListOf<Bitmap>()
        val colors = listOf("0_0","0_1","0_2","1_0","1_1","1_2","2_0","2_1","2_2","3_0","3_1","3_2")
        colors.forEachIndexed{ colorIndex, color ->
            LocalContext.current.assets.open("fish/color_$color/animation_1/1.png").use { inputStream ->
                fishImages.add(BitmapFactory.decodeStream(inputStream))
            }
        }
        G2_WeckMichMalTheme {
            ShopDialog(
                onDismiss = {},
                coins = 3,
                fishImages = fishImages,
                onBuy = {}
            )
        }
    }
}