package de.heinzenburger.g2_weckmichmal.ui.game

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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

class PickerDialogs {
    companion object{
        @Composable
        fun ShopDialog(
            onDismiss: () -> Unit,
            coins: Int,
            fishImages: List<Bitmap>
        ) {
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
                        OurText(
                            text = "Shop",
                            modifier = Modifier
                        )
                        OurText(
                            text = "$coins Wbugs",
                            modifier = Modifier
                        )
                        for(second in 0 .. 2){
                            Row(){
                                for(first in 0 ..  3){
                                    Image(
                                        bitmap = fishImages[second*first + first].asImageBitmap(),
                                        contentDescription = "Fish",
                                        modifier = Modifier
                                            .clickable(
                                                indication = LocalIndication.current,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) {
                                            },
                                        contentScale = ContentScale.Fit,
                                    )
                                }
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
    fun ShopPreview() {
        G2_WeckMichMalTheme {
            ShopDialog(
                onDismiss = {},
                coins = TODO(),
                fishImages = TODO(),
            )
        }
    }
}