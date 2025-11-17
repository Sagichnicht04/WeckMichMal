package de.heinzenburger.g2_weckmichmal.ui.models

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.heinzenburger.g2_weckmichmal.core.Core
import de.heinzenburger.g2_weckmichmal.core.MockupCore
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class GameScreenModel : ViewModel() {
    private lateinit var core: CoreSpecification
    var configurations = mutableStateListOf<ConfigurationWithEvent>()
    var coins = mutableIntStateOf(0)

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

    fun initialize(context: Context, mockup: Boolean) {
        core = if(mockup) {
            MockupCore()
        } else{
            Core(context)
        }
        loadConfigurations()
        loadCoins()
    }
}