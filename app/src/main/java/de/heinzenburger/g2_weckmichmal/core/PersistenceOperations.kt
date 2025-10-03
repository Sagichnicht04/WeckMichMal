package de.heinzenburger.g2_weckmichmal.core

import de.heinzenburger.g2_weckmichmal.api.courses.RaplaFetcher
import de.heinzenburger.g2_weckmichmal.api.routes.DBRoutePlanner
import de.heinzenburger.g2_weckmichmal.calculation.WakeUpCalculator
import de.heinzenburger.g2_weckmichmal.persistence.ApplicationSettingsHandler
import de.heinzenburger.g2_weckmichmal.persistence.ConfigurationHandler
import de.heinzenburger.g2_weckmichmal.persistence.EventHandler
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.PersistenceException
import de.heinzenburger.g2_weckmichmal.specifications.SettingsEntity.DefaultAlarmValues
import de.heinzenburger.g2_weckmichmal.specifications.WakeUpCalculatorException
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

class PersistenceOperations(
    val core: Core
){
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

    fun saveRaplaURL(urlString : String){
        if(urlString == "" || core.isValidCourseURL(urlString)){
            val applicationSettingsHandler = ApplicationSettingsHandler(core.context)
            applicationSettingsHandler.updateCourseURL(urlString)
            core.log(Logger.Level.INFO, "Rapla URL saved: $urlString")
            if(urlString == ""){
                core.getAllConfigurationAndEvent()?.forEach{
                    core.updateConfigurationActive(it.configuration.isActive, it.configuration)
                }
            }
            core.runUpdateLogic()
        }
        else{
            core.log(Logger.Level.INFO, "Invalid URL provided: $urlString")
            core.showToast("Invalid URL")
        }
    }
    fun getRaplaURL(): String? {
        core.log(Logger.Level.INFO, "getRaplaURL called")
        val applicationSettingsHandler = ApplicationSettingsHandler(core.context)
        val url = applicationSettingsHandler.getApplicationSettings().raplaURL
        core.log(Logger.Level.INFO, "getRaplaURL result: $url")
        return url
    }

    fun getAllConfigurationAndEvent(): List<ConfigurationWithEvent>? {
        core.log(Logger.Level.INFO, "getAllConfigurationAndEvent called")
        val configurationHandler = ConfigurationHandler(core.context)
        val result = configurationHandler.getAllConfigurationAndEvent()
        core.log(Logger.Level.INFO, "getAllConfigurationAndEvent result: $result")
        return result
    }

    fun updateConfigurationActive(
        isActive: Boolean,
        configuration: Configuration
    ) {
        core.log(Logger.Level.INFO, "updateConfigurationActive called with isActive: $isActive, configuration: $configuration")
        val configurationHandler = ConfigurationHandler(core.context)
        if(isActive == true && getRaplaURL() == "" && configuration.fixedArrivalTime == null){
            configurationHandler.updateConfigurationActive(false, configuration.uid)
            core.showToast("RAPLA URL fehlt, Wecker deaktiviert")
        }
        else{
            configurationHandler.updateConfigurationActive(isActive, configuration.uid)
        }
        core.log(Logger.Level.INFO, "Configuration active state updated")
    }

    fun getListOfExcludedCourses(): List<String> {
        core.log(Logger.Level.INFO, "getListOfExcludedCourses called")
        val applicationSettingsHandler = ApplicationSettingsHandler(core.context)
        val excludedCoursesList = applicationSettingsHandler.getApplicationSettings().excludeCourses
        return excludedCoursesList
    }

    fun updateListOfExcludedCourses(excludedCoursesList: List<String>) {
        val applicationSettingsHandler = ApplicationSettingsHandler(core.context)
        applicationSettingsHandler.updateExcludedCoursesList(excludedCoursesList)
    }

    fun getDefaultAlarmValues(): DefaultAlarmValues {
        val applicationSettingsHandler = ApplicationSettingsHandler(core.context)
        return applicationSettingsHandler.getApplicationSettings().defaultValues
    }

    fun updateDefaultAlarmValues(defaultAlarmValues: DefaultAlarmValues) {
        val applicationSettingsHandler = ApplicationSettingsHandler(core.context)
        applicationSettingsHandler.updateDefaultAlarmValues(defaultAlarmValues)
    }

    fun updateConfigurationIchHabGeringt(
        date: LocalDate,
        uid: Long
    ) {
        core.log(Logger.Level.INFO, "updateConfigurationIchHabGeringt called with date: $date, configuration: $uid")
        val configurationHandler = ConfigurationHandler(core.context)
        configurationHandler.updateConfigurationIchHabGeringt(date, uid)
        core.log(Logger.Level.INFO, "Configuration active state updated")
    }

    fun deleteAlarmConfiguration(uid: Long) {
        core.log(Logger.Level.INFO, "deleteAlarmConfiguration called with uid: $uid")
        val eventHandler = EventHandler(core.context)
        val configurationHandler = ConfigurationHandler(core.context)
        //Always remove all associated events when removing an alarm configuration
        eventHandler.removeEvent(uid)
        configurationHandler.removeAlarmConfiguration(uid)
        core.log(Logger.Level.INFO, "Alarm configuration and associated events deleted for uid: $uid")
    }

    fun getWakeUpCalculator(): WakeUpCalculator {
        val url = getRaplaURL()
        return WakeUpCalculator(
            routePlanner = DBRoutePlanner(),
            courseFetcher = RaplaFetcher(
                raplaUrl = URL(
                    if(url == ""){
                        "http://example.com"
                    }else{
                        url
                    }
                ),
                excludedCourseNames = getListOfExcludedCourses().toSet()
            )
        )
    }

    fun generateOrUpdateAlarmConfiguration(configuration: Configuration) {
        core.log(Logger.Level.INFO, "generateOrUpdateAlarmConfiguration called with configuration: $configuration")
        try {
            val configurationHandler = ConfigurationHandler(core.context)
            if (core.validateConfiguration(configuration)){
                core.log(Logger.Level.INFO, "Configuration is valid, saving.")
                val wakeUpCalculator = getWakeUpCalculator()

                configuration.ichHabGeringt = LocalDate.MIN
                //Prevent instant ringing
                val configurationWithEvent = core.calculateNextEventForConfiguration(ConfigurationWithEvent(configuration,null),wakeUpCalculator)?.event
                if(configurationWithEvent == null || configurationWithEvent.getLocalDateTime().minusMinutes(2)?.isBefore(LocalDateTime.now()) == true){
                    configuration.ichHabGeringt = LocalDate.now()
                    core.showToast("Alarm für heute ausgesetzt")
                }

                configurationHandler.saveOrUpdate(configuration)

                val eventHandler = EventHandler(core.context)
                val event = wakeUpCalculator.calculateNextEvent(configuration)
                core.log(Logger.Level.INFO, "Calculated event: $event")
                eventHandler.saveOrUpdate(event)

                configuration.log(core)
                event.log(core)
            }
            else{
                core.log(Logger.Level.INFO, "Configuration is not valid")
                core.showToast("Configuration is not valid")
            }
        }
        catch (e: PersistenceException){
            core.log(Logger.Level.SEVERE, e.message.toString())
            core.log(Logger.Level.SEVERE, e.stackTraceToString())
            core.showToast("Error communicating with database. Try reinstalling the app.")
        }
        catch (e: WakeUpCalculatorException.EventDoesNotYetExist){
            core.showToast("No Courses found for this configuration.")
            core.log(Logger.Level.SEVERE, e.message.toString())
            core.log(Logger.Level.SEVERE, e.stackTraceToString())
        }
        catch (e: WakeUpCalculatorException){
            core.log(Logger.Level.SEVERE, e.message.toString())
            core.log(Logger.Level.SEVERE, e.stackTraceToString())
            core.showToast("Error calculating next Event.")
        }
        catch (e: MalformedURLException){
            core.log(Logger.Level.SEVERE, e.message.toString())
            core.log(Logger.Level.SEVERE, e.stackTraceToString())
            core.showToast("Course URL is not valid.")
        }
        catch (e: Exception) {
            core.log(Logger.Level.SEVERE, e.message.toString())
            core.log(Logger.Level.SEVERE, e.stackTraceToString())
        }
    }
}