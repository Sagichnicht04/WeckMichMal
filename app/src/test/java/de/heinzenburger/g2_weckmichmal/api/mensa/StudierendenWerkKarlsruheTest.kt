package de.heinzenburger.g2_weckmichmal.api.mensa

import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class StudierendenWerkKarlsruheTest {
    val today = LocalDate.now().dayOfWeek
    val blacklistedDaysOfWeek = listOf(DayOfWeek.THURSDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

    @Test()
    fun `nextMeals is not empty`() {
        if (blacklistedDaysOfWeek.contains(today)) return

        val mensaFetcher = StudierendenWerkKarlsruhe()
        val meals = mensaFetcher.nextMeals()
        assert(meals.isNotEmpty()) { "Expected non-empty HTML response from Mensa page" }
    }

    @Test()
    fun `nextMeals has at least one price`() {
        if (blacklistedDaysOfWeek.contains(today)) return

        val mensaFetcher = StudierendenWerkKarlsruhe()
        val meals = mensaFetcher.nextMeals()
        assert(meals.any { it.price > 0 }) { "Expected at least one meal with a price greater than 0" }
    }
}