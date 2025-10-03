package de.heinzenburger.g2_weckmichmal.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


class BasicElements {
    companion object{
        @Composable
        // Custom TextField with rounded corners and themed colors
        fun OurTextField(value: String, onValueChange: (String)->Unit, modifier: Modifier, placeholderText: String) {
            TextField(
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.background,
                    unfocusedTextColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = Color.Companion.Transparent,
                    unfocusedIndicatorColor = Color.Companion.Transparent,
                    disabledIndicatorColor = Color.Companion.Transparent,
                    cursorColor = MaterialTheme.colorScheme.secondary,
                    selectionColors = TextSelectionColors(
                        handleColor = MaterialTheme.colorScheme.secondary,
                        backgroundColor = MaterialTheme.colorScheme.onBackground
                    ),
                    focusedTrailingIconColor = MaterialTheme.colorScheme.onBackground
                ),
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = modifier,
                placeholder = {
                    OurText(
                        text = placeholderText,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier,
                    )
                }
            )
        }

        // Custom Button for the Edit Alarm screen with primary color and centered text
        @Composable fun OurButtonInEditAlarm(modifier: Modifier, onClick: ()->Unit, text: String, enabled: Boolean = true){
            Button(
                modifier = modifier,
                enabled = enabled,
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = text,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(0.dp)
                )
            }
        }

        // Custom Text composable with configurable color and alignment
        @Composable fun OurText(color: Color = MaterialTheme.colorScheme.primary,
                                text: String,
                                modifier: Modifier,
                                textAlign: TextAlign = TextAlign.Center){
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier,
                textAlign = textAlign
            )
        }
        // Numeric input field that only allows integer values and null
        @Composable
        fun NumberField(text: MutableState<Int?>, modifier: Modifier) {
            fun extractIntFromString(input: String): Int? {
                val digits = input.filter { it.isDigit() }
                return if (digits.isNotEmpty() && input.length < 4) digits.toInt() else null
            }

            val change : (String) -> Unit = { it ->
                text.value = extractIntFromString(it)
            }
            TextField(
                value = if(text.value == null){
                            ""
                        }
                        else{
                            text.value.toString()
                        },
                modifier = modifier,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                onValueChange = change
            )
        }
        // Simple loading dialog
        @Composable
        fun LoadingScreen() {
            Dialog(onDismissRequest = { }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight(0.1f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    )
                    {
                        OurText(
                            text = "Lade...",
                            modifier = Modifier.align(BiasAlignment(0f,0f)),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}