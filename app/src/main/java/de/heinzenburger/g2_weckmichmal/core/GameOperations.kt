package de.heinzenburger.g2_weckmichmal.core

import de.heinzenburger.g2_weckmichmal.game.CoinManager
import de.heinzenburger.g2_weckmichmal.persistence.ApplicationSettingsHandler
import de.heinzenburger.g2_weckmichmal.persistence.GamePersistency
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.GameEntity
import java.time.LocalDate
import java.time.LocalTime

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

    fun getLastTimeCoinsReceived():LocalDate{
        val gamePersistency = GamePersistency(core.context)
        core.log(Logger.Level.INFO, "Reading coins")
        return gamePersistency.getGameEntity().getLastTimeCoinsReceived()
    }

    fun updateLastTimeCoinsReceived(lastTimeCoinsReceived: LocalDate){
        val gamePersistency = GamePersistency(core.context)
        gamePersistency.updateLastTimeCoinsReceived(lastTimeCoinsReceived)
        core.log(Logger.Level.INFO, "Updating last time coins received: $lastTimeCoinsReceived")
    }

    fun alarmRinging(){
        val coinManager = CoinManager(core)
        coinManager.ringRingRing()
    }

    fun getLastConfigurationChanged(): LocalDate{
        val gamePersistency = GamePersistency(core.context)
        core.log(Logger.Level.INFO, "Reading lastConfigurationChange")
        return gamePersistency.getGameEntity().getLastConfigurationChange()
    }

    fun updateLastConfiguratoinChanged(lastConfigurationChanged: LocalDate){
        val gamePersistency = GamePersistency(core.context)
        gamePersistency.updateLastConfigurationChange(lastConfigurationChanged)
        core.log(Logger.Level.INFO, "Updating lastConfigurationChange: $lastConfigurationChanged")
    }

    fun getGoodWakeTimeStart(): LocalTime{
        val gamePersistency = GamePersistency(core.context)
        core.log(Logger.Level.INFO, "Reading goodWakeTimeStart")
        return gamePersistency.getGameEntity().getGoodWakeTimeStart()
    }

    fun updateGoodWakeTimeStart(goodWakeTimeStart: LocalTime){
        val gamePersistency = GamePersistency(core.context)
        gamePersistency.updateGoodWakeTimeStart(goodWakeTimeStart)
        core.log(Logger.Level.INFO, "Updating goodWakeTimeStart: $goodWakeTimeStart")
    }

    fun getGoodWakeTimeEnd(): LocalTime{
        val gamePersistency = GamePersistency(core.context)
        core.log(Logger.Level.INFO, "Reading goodWakeTimeEnd")
        return gamePersistency.getGameEntity().getGoodWakeTimeEnd()
    }

    fun updateGoodWakeTimeEnd(goodWakeTimeEnd: LocalTime){
        val gamePersistency = GamePersistency(core.context)
        gamePersistency.updateGoodWakeTimeEnd(goodWakeTimeEnd)
        core.log(Logger.Level.INFO, "Updating goodWakeTimeEnd: $goodWakeTimeEnd")
    }

    fun getShoppingList(): GameEntity.ShoppingEntity{
        val gamePersistency = GamePersistency(core.context)
        core.log(Logger.Level.INFO, "Reading shopping list")
        return gamePersistency.getGameEntity().shoppingList
    }

    fun updateShoppingList(shoppingEntity: GameEntity.ShoppingEntity){
        val gamePersistency = GamePersistency(core.context)
        gamePersistency.updateShoppingList(shoppingEntity)
        core.log(Logger.Level.INFO, "Updating coins: $shoppingEntity")
    }
}
