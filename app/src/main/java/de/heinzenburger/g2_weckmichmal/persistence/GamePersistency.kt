package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import com.google.gson.Gson
import de.heinzenburger.g2_weckmichmal.specifications.GameEntity
import de.heinzenburger.g2_weckmichmal.specifications.InterfaceApplicationSettings
import de.heinzenburger.g2_weckmichmal.specifications.InterfaceGamePersistency
import de.heinzenburger.g2_weckmichmal.specifications.PersistenceException
import de.heinzenburger.g2_weckmichmal.specifications.SettingsEntity
import java.io.File

data class GamePersistency (
    val context: Context
) : InterfaceGamePersistency {
    val gson = Gson()
    val logger = Logger(context)

    override fun saveOrUpdateGamePersistency(gameEntity: GameEntity) {
        try {
            var json = gson.toJson(gameEntity)
            //context.filesDir is the apps personal data folder
            logger.log(Logger.Level.SEVERE, json.toString())
            File(context.filesDir, "game.json").writeText(json.toString())
        }
        catch (e : Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            throw PersistenceException.WriteGameException(e)
        }
    }
    override fun getGameEntity() : GameEntity{
        try {
            return if(isGameNotInitialized()){
                GameEntity()
            } else{
                gson.fromJson(File(context.filesDir,"game.json").readText(), GameEntity::class.java)
            }
        }
        catch (e: Exception){
            throw PersistenceException.ReadGameException(e)
        }
    }

    override fun isGameNotInitialized(): Boolean{
        return !File(context.filesDir, "game.json").exists()
    }

    override fun updateCoins(coins: Int) {
        try {
            val gameEntity = getGameEntity()
            gameEntity.coins = coins
            saveOrUpdateGamePersistency(gameEntity)
        }
        catch (e: PersistenceException){
            throw PersistenceException.UpdateGameException(e)
        }
    }
}