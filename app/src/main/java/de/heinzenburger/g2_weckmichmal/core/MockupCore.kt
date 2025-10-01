package de.heinzenburger.g2_weckmichmal.core

import de.heinzenburger.g2_weckmichmal.persistence.Logger
import de.heinzenburger.g2_weckmichmal.specifications.ConfigurationWithEvent
import de.heinzenburger.g2_weckmichmal.specifications.Configuration
import de.heinzenburger.g2_weckmichmal.specifications.Course
import de.heinzenburger.g2_weckmichmal.specifications.Event
import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import de.heinzenburger.g2_weckmichmal.specifications.MealType
import de.heinzenburger.g2_weckmichmal.specifications.MensaMeal
import de.heinzenburger.g2_weckmichmal.specifications.Route
import de.heinzenburger.g2_weckmichmal.specifications.RouteSection
import de.heinzenburger.g2_weckmichmal.specifications.SettingsEntity.DefaultAlarmValues
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

//Is a mockup version of real core, in order to keep UI preview functionality
//Most methods do nothing, only those that cause something in UI are relevant
//For description of each method, see I_Core in specifications
class MockupCore : CoreSpecification {
    override fun deriveStationName(input: String): List<String> {
        return listOf("Europaplatz","Europaplatz U", "Europaplatz Berlin","Europaplatz München", "Europaplatz Dortmund")
    }

    override fun nextMensaMeals(): List<MensaMeal> {
        return listOf(MensaMeal(
                name = "Lecker lecker",
                price = 2.5,
                type = MealType.VEGETARIAN
            ),
            MensaMeal(
                name = "Nich so lecker",
                price = 12.5,
                type = MealType.MEAT
            ),
            MensaMeal(
                name = "Bombastisch",
                price = 0.5,
                type = MealType.VEGAN
            )
        )
    }

    companion object{
        val mockupConfigurations = listOf(
            Configuration(
                uid = 123,
                name = "Wecker 1",
                days = setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
                fixedArrivalTime = null,
                fixedTravelBuffer = null,
                startBuffer = 30,
                endBuffer = 0,
                startStation = "Wiesloch",
                endStation = "Duale Folter",
                isActive = true,
                enforceStartBuffer = true
            )
        )
        val mockupEvents = listOf(
            Event(
                configID = 123,
                wakeUpTime = LocalTime.of(8,30),
                days = setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
                date = LocalDate.of(2024,10,1),
                courses = listOf(
                    Course(
                        name = "Rechnerarchitekturen",
                        lecturer = "Kollege Röthig",
                        room = "4692",
                        startDate = LocalDateTime.of(2024,10,1,10,0,0),
                        endDate = LocalDateTime.of(2024,10,1,13,0,0)
                    ),
                    Course(
                        name = "Statistik",
                        lecturer = "Kollege Markus",
                        room = "4692",
                        startDate = LocalDateTime.of(2024,10,1,14,0,0),
                        endDate = LocalDateTime.of(2024,10,1,18,0,0)
                    )
                ),
                routes = listOf(
                    Route(
                        startStation = "Wiesloch",
                        endStation = "Hauptbahnhof",
                        startTime = LocalDateTime.of(2024,10,1,9,0,0),
                        endTime = LocalDateTime.of(2024,10,1,9,50,0),
                        sections = listOf(
                            RouteSection(
                                vehicleName = "S3",
                                startTime = LocalDateTime.of(2024,10,1,9,0,0),
                                startStation = "Wiesloch",
                                endTime = LocalDateTime.of(2024,10,1,9,30,0),
                                endStation = "Funkloch",
                            ),
                            RouteSection(
                                vehicleName = "STR 1",
                                startTime = LocalDateTime.of(2024,10,1,9,30,0),
                                startStation = "Funkloch",
                                endTime = LocalDateTime.of(2024,10,1,9,50,0),
                                endStation = "Duale Folter",
                            ),
                            RouteSection(
                                vehicleName = "STR 1",
                                startTime = LocalDateTime.of(2024,10,1,9,55,0),
                                startStation = "Duale Folter",
                                endTime = LocalDateTime.of(2024,10,1,10,0,0),
                                endStation = "Europaplatz",
                            ),
                            RouteSection(
                                vehicleName = "STR 3",
                                startTime = LocalDateTime.of(2024,10,1,10,5,0),
                                startStation = "Europaplatz",
                                endTime = LocalDateTime.of(2024,10,1,10,30,0),
                                endStation = "Hauptbahnhof",
                            )
                        )
                    ),
                    Route(
                        startStation = "Wiesloch",
                        endStation = "Duale Folter",
                        startTime = LocalDateTime.of(2024,10,1,9,30,0),
                        endTime = LocalDateTime.of(2024,10,1,10,20,0),
                        sections = listOf(
                            RouteSection(
                                vehicleName = "S3",
                                startTime = LocalDateTime.of(2024,10,1,9,30,0),
                                startStation = "Wiesloch",
                                endTime = LocalDateTime.of(2024,10,1,10,0,0),
                                endStation = "Funkloch",
                            ),
                            RouteSection(
                                vehicleName = "STR 1",
                                startTime = LocalDateTime.of(2024,10,1,10,0,0),
                                startStation = "Funkloch",
                                endTime = LocalDateTime.of(2024,10,1,10,20,0),
                                endStation = "Duale Folter",
                            )
                        )
                    )
                )
            )
        )
    }

    override fun runUpdateLogic() {}
    override fun saveRaplaURL(url : String){}
    override fun saveRaplaURL(director: String, course: String) {

    }

    override fun isApplicationOpenedFirstTime(): Boolean? {return null}
    override fun generateOrUpdateAlarmConfiguration(configuration: Configuration) {}

    override fun getRaplaURL(): String? {
        return ""
    }

    override fun isInternetAvailable(): Boolean {
        return true
    }

    override fun log(
        level: Logger.Level,
        text: String
    ) {
    }

    override fun getLog(): String {
        return "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.  \n" +
                "\n" +
                "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.  \n" +
                "\n" +
                "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.  \n" +
                "\n" +
                "Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem"
    }

    override fun updateConfigurationActive(
        isActive: Boolean,
        configuration: Configuration
    ) {

    }

    override fun updateConfigurationIchHabGeringt(
        date: LocalDate,
        uid: Long
    ) {

    }

    override fun getAllConfigurationAndEvent(): List<ConfigurationWithEvent>? {
        val result = mutableListOf<ConfigurationWithEvent>()
        mockupConfigurations.forEach {
            val configurationEntity = it
            var event : Event? = null
            mockupEvents.forEach { if(it.configID == configurationEntity.uid){ event = it } }
            result.add(ConfigurationWithEvent(configurationEntity,event))
        }
        return result
    }

    override fun isValidCourseURL(urlString : String) : Boolean {
        return true
    }

    override fun isValidCourseURL(director: String, course: String): Boolean {
        return true
    }

    override fun getListOfNameOfCourses(): List<String> {
        return listOf("Numerik","Compilerbau")
    }

    override fun getListOfExcludedCourses(): List<String>? {
        return listOf("Compilerbau")
    }

    override fun updateListOfExcludedCourses(excludedCoursesList: List<String>) {

    }

    override fun getDefaultAlarmValues(): DefaultAlarmValues? {
        return DefaultAlarmValues()
    }

    override fun updateDefaultAlarmValues(defaultAlarmValues: DefaultAlarmValues) {
    }


    override fun validateConfiguration(configuration: Configuration): Boolean {
        return true
    }

    override fun getGrantedPermissions(): List<String>? {
        return listOf(
            "Alarm",
            "Battery",
            //"Notifications",
        )
    }

    override fun deleteAlarmConfiguration(uid: Long) {
    }

    override fun showToast(message: String) {
    }

    override fun getLoggedNextAlarm(): String {
        return "No alarm scheduled"
    }

    override fun logNextAlarm(date: LocalDateTime, type: String) {
    }
}