package de.heinzenburger.g2_weckmichmal.persistence

import android.content.Context
import de.heinzenburger.g2_weckmichmal.specifications.PersistenceException
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Logger(
    val context: Context?
){
    enum class Level(
    ) { INFO(), WARNING(), SEVERE() }

    val logger: java.util.logging.Logger = java.util.logging.Logger.getLogger(ConfigurationHandler::class.java.name)
    fun updateNextAlarmFile(date : LocalDateTime, type: String){
        if(context != null){
            val logFile = File(context.filesDir, "next_alarm")
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile()
                }
                catch (e: IOException) {
                    throw PersistenceException.CreateLogFileException(e)
                }
            }
            try {
                val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")
                logFile.writeText(type + " at " + date.format(formatter))
            }
            catch (e: IOException) {
                throw PersistenceException.WriteLogException(e)
            }
        }
    }

    fun getNextAlarm() : String{
        return try{
            if(context != null){
                if(File(context.filesDir,"next_alarm").exists()){
                    "Next: "+File(context.filesDir,"next_alarm").readText()
                } else{
                    "No alarm scheduled"
                }
            } else{
                "Called getLogs without context"
            }
        } catch (e: Exception){
            throw PersistenceException.ReadLogException(e)
        }
    }

    @Throws(PersistenceException.CreateLogFileException::class, PersistenceException.WriteLogException::class)
    fun log(level: Level, text: String) {
        val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")
        var modifiedText = LocalDateTime.now().format(formatter) + " " + text
        if (level == Level.INFO){
            logger.info(text)
            modifiedText = "INFO: $modifiedText"
        }
        else if (level == Level.WARNING) {
            logger.warning(text)
            modifiedText = "WARNING: $modifiedText"
        }
        else if (level == Level.SEVERE) {
            logger.severe(text)
            modifiedText = "SEVERE: $modifiedText"
        }

        if(context != null){
            val logFile = File(context.filesDir, "log")
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile()
                }
                catch (e: IOException) {
                    throw PersistenceException.CreateLogFileException(e)
                }
            }
            try {
                logFile.appendText(modifiedText)
                logFile.appendText(System.lineSeparator())
            }
            catch (e: IOException) {
                throw PersistenceException.WriteLogException(e)
            }
        }
    }
    @Throws(PersistenceException.ReadLogException::class)
    fun getLogs() : String{
        try{
            if(context != null){
                var text = File(context.filesDir,"log").readText().split("\n").reversed().joinToString("\n")
                return text
            } else{
                return "Called getLogs without context"
            }
        }
        catch (e: Exception){
            throw PersistenceException.ReadLogException(e)
        }
    }
}