package de.heinzenburger.g2_weckmichmal.game

import de.heinzenburger.g2_weckmichmal.specifications.CoreSpecification
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class CoinManager(
    val core: CoreSpecification
) {
    fun getRewardForWindow(startTime: LocalTime, endTime: LocalTime): Int{
        if(startTime >= endTime) return 0
        val duration = Duration.between(endTime, startTime).toMinutes()
        if(duration>180) return 1
        if(duration>90) return 2
        return 3
    }
    fun getRewardForNow(): Int{
        val start = core.getGoodWakeTimeStart()
        val end = core.getGoodWakeTimeEnd()
        if(start == null || end == null || LocalTime.now().isBefore(start) || LocalTime.now().isAfter(end)) return 0
        return getRewardForWindow(start, end)
    }

    fun ringRingRing(){
        if(core.getLastTimeCoinsReceived() != LocalDate.now()){
            core.updateCoins(getRewardForNow())
            core.updateLastTimeCoinsReceived(LocalDate.now())
        }
    }
}