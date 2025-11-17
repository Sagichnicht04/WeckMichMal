package de.heinzenburger.g2_weckmichmal.ui.game

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.ui.screens.AlarmClockEditScreen
import kotlin.concurrent.thread

class ScreenModel : ViewModel(){
    private lateinit var core: CoreSpecification
    var configurations = mutableStateListOf<ConfigurationWithEvent>()
    var coins = mutableIntStateOf(0)
    var openShop by mutableStateOf(false)

    var boughtFish = mutableStateListOf<Int>()
    fun setEditScreen(context: Context, configuration: Configuration?){
        val core = Core(context)
        val intent = Intent(context, AlarmClockEditScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        intent.putExtra("defaultAlarmValues", core.getDefaultAlarmValues())
        intent.putExtra("configuration", configuration)
        context.startActivity(intent)
        (context as ComponentActivity).finish()
    }

    fun loadAquariumAnimations(context: Context): List<List<List<Bitmap>>> {
        val colors = listOf("0_0","0_1","0_2","1_0","1_1","1_2","2_0","2_1","2_2","3_0","3_1","3_2")
        val result = mutableListOf<MutableList<MutableList<Bitmap>>>()
        colors.forEachIndexed{ colorIndex, color ->
            result.add(mutableListOf())
            for(animation in 0 .. 7){
                result[colorIndex].add(mutableListOf())
                for(i in 0..3) {
                    context.assets.open("fish/color_$color/animation_$animation/$i.png").use { inputStream ->
                         result[colorIndex][animation].add(BitmapFactory.decodeStream(inputStream))
                    }
                }
            }
        }
        return result
    }

    fun buyFish(color: Int){
        thread{
            boughtFish.add(color)
            core.buyFish(color)
            loadCoins()
        }
    }

    fun loadBoughtFish(){
        thread {
            val shoppingEntity = core.getShoppingList()
            shoppingEntity?.boughtFish?.forEach {
                boughtFish.add(it.color)
            }
        }
    }

    fun updateConfigurationActive(isConfigurationActive: Boolean, configuration: Configuration){
        thread{
            core.updateConfigurationActive(isConfigurationActive, configuration)
        }
    }
    private fun loadConfigurations() {
        thread{
            configurations.removeAll(configurations)
            core.getAllConfigurationAndEvent()?.forEach { config ->
                configurations.add(config)
            }
        }
    }
    private fun loadCoins() {
        thread{
            coins.intValue = core.getCoins() ?: 0
        }
    }

    fun initialize(core: CoreSpecification) {
        this.core = core
        loadConfigurations()
        loadCoins()
        loadBoughtFish()
    }
}