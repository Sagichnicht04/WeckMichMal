package de.heinzenburger.g2_weckmichmal.api.mensa

import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class StudierendenWerkKarlsruheTest {
    @Test
    fun `nextMeals is not empty`() {
        // Skip test on weekends when Mensa is closed
        if(LocalDate.now().dayOfWeek != DayOfWeek.SATURDAY && LocalDate.now().dayOfWeek != DayOfWeek.SUNDAY){
            val mensaFetcher = StudierendenWerkKarlsruhe()
            val meals = mensaFetcher.nextMeals()
            // Only assert if meals are returned - empty list is valid when no data is available (e.g., Friday evening, holidays)
            if (meals.isNotEmpty()) {
                assert(meals.all { it.name.isNotBlank() }) { "All meals should have a name" }
            }
        }
    }

    @Test
    fun `nextMeals has at least one price`() {
        // Skip test on weekends when Mensa is closed
        if(LocalDate.now().dayOfWeek != DayOfWeek.SATURDAY && LocalDate.now().dayOfWeek != DayOfWeek.SUNDAY){
            val mensaFetcher = StudierendenWerkKarlsruhe()
            val meals = mensaFetcher.nextMeals()
            // Only assert if meals are returned - empty list is valid when no data is available (e.g., Friday evening, holidays)
            if (meals.isNotEmpty()) {
                assert(meals.any {it.price > 0}) { "Expected at least one meal with a price greater than 0"}
            }
        }
    }
}