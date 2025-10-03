package de.heinzenburger.g2_weckmichmal.core

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import de.heinzenburger.g2_weckmichmal.calculation.WakeUpCalculator
import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.specifications.PersistenceException
import de.heinzenburger.g2_weckmichmal.api.courses.deriveValidCourseURL
import de.heinzenburger.g2_weckmichmal.persistence.GamePersistency
import de.heinzenburger.g2_weckmichmal.specifications.GameEntity
import de.heinzenburger.g2_weckmichmal.specifications.MensaMeal
import de.heinzenburger.g2_weckmichmal.specifications.SettingsEntity.DefaultAlarmValues
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

//For description of specific method, see I_Core in specifications
data class Core(
    val context: Context,
) : CoreSpecification {
    val logger = Logger(context)
    private val persistenceOperations = PersistenceOperations(this)
    private val gameOperations = GameOperations(this)
    private val validations = Validations(this)
    private val exceptionHandler = ExceptionHandler(this)
    private val backgroundOperations = BackgroundOperations(this)
    private val apiOperations = APIOperations(this)

    /******************** API ********************/
    override fun deriveStationName(input: String): List<String>? {
        return exceptionHandler.runWithUnexpectedExceptionHandler("Error getting Mensa Plan"){
            apiOperations.deriveStationName(input)
        }
    }

    override fun nextMensaMeals(): List<MensaMeal>? {
        return exceptionHandler.runWithUnexpectedExceptionHandler("Error getting Mensa Plan"){
            apiOperations.nextMensaMeals()
        }
    }

    override fun getListOfNameOfCourses(): List<String>? {
        return exceptionHandler.runWithUnexpectedExceptionHandler("Error getting list of Courses"){
            apiOperations.getListOfNameOfCourses()
        }
    }

    /******************** BACKGROUND ********************/
    override fun runUpdateLogic() {
        exceptionHandler.runWithUnexpectedExceptionHandler("Error running update logic"){
            backgroundOperations.runUpdateLogic()
        }
    }
    fun calculateNextEventForConfiguration(it: ConfigurationWithEvent, wakeUpCalculator: WakeUpCalculator) : ConfigurationWithEvent?{
        return exceptionHandler.runWithUnexpectedExceptionHandler("Could not get Internet state"){
            backgroundOperations.calculateNextEventForConfiguration(it, wakeUpCalculator)
        }
    }

    /******************** VALIDATIONS ********************/
    override fun isInternetAvailable(): Boolean {
        return exceptionHandler.runWithUnexpectedExceptionHandler("Could not get Internet state"){
            validations.isInternetAvailable()
        } == true
    }
    override fun isApplicationOpenedFirstTime(): Boolean {
        return exceptionHandler.runWithUnexpectedExceptionHandler("Could not retrieve isApplicationOpenedFirstTime."){
            validations.isApplicationOpenedFirstTime()
        } == true
    }
    override fun isValidCourseURL(urlString: String): Boolean {
        return exceptionHandler.runWithUnexpectedExceptionHandler("Can not validate Course-URL.", true){
            validations.isValidCourseURL(urlString)
        } == true
    }
    override fun isValidCourseURL(director: String, course: String): Boolean {
        return exceptionHandler.runWithUnexpectedExceptionHandler("Can not validate Course-URL.", true){
            isValidCourseURL(deriveValidCourseURL(director, course).toString())
        } == true
    }
    override fun validateConfiguration(configuration: Configuration): Boolean {
        return exceptionHandler.runWithUnexpectedExceptionHandler("Can not validate Course-URL.", true){
            validations.validateConfiguration(configuration)
        } == true
    }

    override fun getGrantedPermissions(): List<String>? {
        return exceptionHandler.runWithUnexpectedExceptionHandler("Can not get granted permissions.", true){
            validations.getGrantedPermissions()
        }
    }


    /******************** PERSISTENCE ********************/
    /**Configurations**/
    override fun getAllConfigurationAndEvent(): List<ConfigurationWithEvent>? {
        return exceptionHandler.runWithUnexpectedExceptionHandler("Could not load alarms from database. Try reinstalling the app.", true){
            persistenceOperations.getAllConfigurationAndEvent()
        }
    }
    override fun updateConfigurationActive(isActive: Boolean, configuration: Configuration) {
        exceptionHandler.runWithUnexpectedExceptionHandler("Could not update active state of configuration."){
            persistenceOperations.updateConfigurationActive(isActive,configuration)
        }
    }
    override fun updateConfigurationIchHabGeringt(date: LocalDate, uid: Long) {
        exceptionHandler.runWithUnexpectedExceptionHandler("Could not update ichhabgeringt state of configuration."){
            persistenceOperations.updateConfigurationIchHabGeringt(date,uid)
        }
    }
    override fun deleteAlarmConfiguration(uid: Long) {
        exceptionHandler.runWithUnexpectedExceptionHandler("Could not delete from database. Try reinstalling the app."){
            persistenceOperations.deleteAlarmConfiguration(uid)
        }
    }
    override fun generateOrUpdateAlarmConfiguration(configuration: Configuration) {
        exceptionHandler.runWithUnexpectedExceptionHandler("Could not generate alarm configuration"){
            persistenceOperations.generateOrUpdateAlarmConfiguration(configuration)
        }
    }

    /**Settings**/
    override fun saveRaplaURL(url : String){
        exceptionHandler.runWithUnexpectedExceptionHandler("Error saving URL to database. Try reinstalling the app."){
            persistenceOperations.saveRaplaURL(url)
        }
    }

    override fun saveRaplaURL(director: String, course: String) {
        exceptionHandler.runWithUnexpectedExceptionHandler("Error saving URL to database. Try reinstalling the app."){
            saveRaplaURL(deriveValidCourseURL(director, course).toString())
        }
    }

    override fun getRaplaURL(): String? {
        return exceptionHandler.runWithUnexpectedExceptionHandler("Error retrieving URL from database. Try reinstalling the app.", true){
            persistenceOperations.getRaplaURL()
        }
    }

    override fun getListOfExcludedCourses(): List<String>? {
        return exceptionHandler.runWithUnexpectedExceptionHandler("Error retrieving URL from database. Try reinstalling the app.", true){
            persistenceOperations.getListOfExcludedCourses()
        }
    }

    override fun updateListOfExcludedCourses(excludedCoursesList: List<String>) {
        exceptionHandler.runWithUnexpectedExceptionHandler("Could not update default alarm values"){
            persistenceOperations.updateListOfExcludedCourses(excludedCoursesList)
        }
    }

    override fun getDefaultAlarmValues(): DefaultAlarmValues? {
        return exceptionHandler.runWithUnexpectedExceptionHandler("Could not update default alarm values",true){
            persistenceOperations.getDefaultAlarmValues()
        }
    }

    override fun updateDefaultAlarmValues(defaultAlarmValues: DefaultAlarmValues) {
        exceptionHandler.runWithUnexpectedExceptionHandler("Could not update default alarm values"){
            persistenceOperations.updateDefaultAlarmValues(defaultAlarmValues)
        }
    }
    /******************** Game ********************/
    override fun getIsGameMode():Boolean?{
        return exceptionHandler.runWithUnexpectedExceptionHandler("Error retrieving mode from database. Try reinstalling the app."){
            gameOperations.getIsGameMode()
        }
    }
    override fun updateIsGameMode(isGameMode : Boolean){
        exceptionHandler.runWithUnexpectedExceptionHandler("Error updating mode. Try reinstalling the app."){
            gameOperations.updateIsGameMode(isGameMode)
        }
    }
    override fun getCoins():Int?{
        return exceptionHandler.runWithUnexpectedExceptionHandler("Error retrieving coins from database"){
            gameOperations.getCoins()
        }
    }
    override fun updateCoins(coins: Int){
        exceptionHandler.runWithUnexpectedExceptionHandler("Error updating coins."){
            gameOperations.updateCoins(coins)
        }
    }
    override fun getLastConfigurationChanged(): LocalDate?{
        return exceptionHandler.runWithUnexpectedExceptionHandler("Error retrieving last configuration changed from database"){
            gameOperations.getLastConfigurationChanged()
        }
    }
    override fun updateLastConfiguratoinChanged(lastConfigurationChanged: LocalDate){
        exceptionHandler.runWithUnexpectedExceptionHandler("Error updating last configuration changed."){
            gameOperations.updateLastConfiguratoinChanged(lastConfigurationChanged)
        }
    }
    override fun getGoodWakeTimeStart(): LocalTime?{
        return exceptionHandler.runWithUnexpectedExceptionHandler("Error retrieving good wake time start from database"){
            gameOperations.getGoodWakeTimeStart()
        }
    }
    override fun updateGoodWakeTimeStart(goodWakeTimeStart: LocalTime){
        exceptionHandler.runWithUnexpectedExceptionHandler("Error updating good wake time start."){
            gameOperations.updateGoodWakeTimeStart(goodWakeTimeStart)
        }
    }
    override fun getGoodWakeTimeEnd(): LocalTime?{
        return exceptionHandler.runWithUnexpectedExceptionHandler("Error retrieving good wake time end from database"){
            gameOperations.getGoodWakeTimeEnd()
        }
    }
    override fun updateGoodWakeTimeEnd(goodWakeTimeEnd: LocalTime){
        exceptionHandler.runWithUnexpectedExceptionHandler("Error updating goodWakeTimeEnd."){
            gameOperations.updateGoodWakeTimeEnd(goodWakeTimeEnd)
        }
    }
    override fun getShoppingList(): GameEntity.ShoppingEntity?{
        return exceptionHandler.runWithUnexpectedExceptionHandler("Error retrieving shopping list from database"){
            gameOperations.getShoppingList()
        }
    }
    override fun updateShoppingList(shoppingEntity: GameEntity.ShoppingEntity){
        exceptionHandler.runWithUnexpectedExceptionHandler("Error updating shopping list."){
            gameOperations.updateShoppingList(shoppingEntity)
        }
    }

    /******************** LOGGING ********************/
    override fun showToast(message: String) {
        log(Logger.Level.INFO, "showToast called with message: $message")
        try {
            ContextCompat.getMainExecutor(context).execute( { ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            })
        }
        catch (e: Exception){
            log(Logger.Level.SEVERE, e.message.toString())
            log(Logger.Level.SEVERE, e.stackTraceToString())
        }
    }

    override fun getLoggedNextAlarm() : String{
        return logger.getNextAlarm()
    }

    override fun logNextAlarm(date: LocalDateTime, type: String){
        logger.updateNextAlarmFile(date, type)
    }

    override fun log(
        level: Logger.Level,
        text: String
    ) {
        // Always log to Android logcat for debug as well
        android.util.Log.d("CoreLog", "[$level] $text")
        try {
            logger.log(level, text)
        }
        catch (e: PersistenceException){
            val logger = Logger(null)
            logger.log(Logger.Level.SEVERE, "Can't write to log " + e.message)
        }
        catch (e: Exception){
            showToast("Error with Log writing " + e.message)
        }
    }

    override fun getLog(): String {
        log(Logger.Level.INFO, "getLog called")
        return try {
            logger.getLogs()
        } catch (e: PersistenceException){
            e.message.toString()
        }
    }
}
