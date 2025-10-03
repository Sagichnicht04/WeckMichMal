package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import com.google.gson.Gson
import de.heinzenburger.g2_weckmichmal.specifications.InterfaceApplicationSettings
import de.heinzenburger.g2_weckmichmal.specifications.PersistenceException
import de.heinzenburger.g2_weckmichmal.specifications.SettingsEntity
import java.io.File

data class ApplicationSettingsHandler (
    val context: Context
) : InterfaceApplicationSettings{
    val gson = Gson()
    val logger = Logger(context)

    override fun saveOrUpdateApplicationSettings(settings: SettingsEntity) {
        try {
            var json = gson.toJson(settings)
            //context.filesDir is the apps personal data folder
            logger.log(Logger.Level.SEVERE, json.toString())
            File(context.filesDir, "settings.json").writeText(json.toString())
        }
        catch (e : Exception){
            logger.log(Logger.Level.SEVERE, e.message.toString())
            throw PersistenceException.WriteSettingsException(e)
        }
    }
    override fun getApplicationSettings() : SettingsEntity{
        try {
            return if(isApplicationOpenedFirstTime()){
                SettingsEntity()
            } else{
                gson.fromJson(File(context.filesDir,"settings.json").readText(), SettingsEntity::class.java)
            }
        }
        catch (e: Exception){
            throw PersistenceException.ReadSettingsException(e)
        }
    }

    override fun isApplicationOpenedFirstTime(): Boolean{
        //Application is considered opened first time if the configuration file doesn't yet exist
        return !File(context.filesDir, "settings.json").exists()
    }

    override fun updateDefaultAlarmValues(defaultAlarmValues: SettingsEntity.DefaultAlarmValues) {
        try {
            val settings = getApplicationSettings()
            settings.defaultValues = defaultAlarmValues
            saveOrUpdateApplicationSettings(settings)
        }
        catch (e: PersistenceException){
            throw PersistenceException.UpdateSettingsException(e)
        }
    }

    override fun updateExcludedCoursesList(excludedCoursesList: List<String>) {
        try {
            val settings = getApplicationSettings()
            settings.excludeCourses = excludedCoursesList
            saveOrUpdateApplicationSettings(settings)
        }
        catch (e: PersistenceException){
            throw PersistenceException.UpdateSettingsException(e)
        }
    }

    override fun updateCourseURL(url: String) {
        try {
            val settings = getApplicationSettings()
            settings.raplaURL = url
            saveOrUpdateApplicationSettings(settings)
        }
        catch (e: PersistenceException){
            throw PersistenceException.UpdateSettingsException(e)
        }
    }

    override fun updateIsGameMode(isGameMode: Boolean) {
        try {
            val settings = getApplicationSettings()
            settings.isGameMode = isGameMode
            saveOrUpdateApplicationSettings(settings)
        }
        catch (e: PersistenceException){
            throw PersistenceException.UpdateSettingsException(e)
        }
    }
}