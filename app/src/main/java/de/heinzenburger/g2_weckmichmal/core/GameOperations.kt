package de.heinzenburger.g2_weckmichmal.core

import de.heinzenburger.g2_weckmichmal.persistence.ApplicationSettingsHandler
import de.heinzenburger.g2_weckmichmal.persistence.GamePersistency
import de.heinzenburger.g2_weckmichmal.persistence.Logger

class GameOperations(
    val core: Core
) {
    fun updateIsGameMode(isGameMode: Boolean){
        val applicationSettingsHandler = ApplicationSettingsHandler(core.context)
        applicationSettingsHandler.updateIsGameMode(isGameMode)
        core.log(Logger.Level.INFO, "IsGameMode saved: $isGameMode")
    }

    fun getIsGameMode():Boolean{
        val applicationSettingsHandler = ApplicationSettingsHandler(core.context)
        core.log(Logger.Level.INFO, "Reading IsGameMode")
        return applicationSettingsHandler.getApplicationSettings().isGameMode
    }

    fun getCoins():Int{
        val gamePersistency = GamePersistency(core.context)
        core.log(Logger.Level.INFO, "Reading coins")
        return gamePersistency.getGameEntity().coins
    }

    fun updateCoins(coins: Int){
        val gamePersistency = GamePersistency(core.context)
        gamePersistency.updateCoins(coins)
        core.log(Logger.Level.INFO, "Updating coins: $coins")
    }
}
