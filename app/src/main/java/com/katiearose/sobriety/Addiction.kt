package com.katiearose.sobriety

import com.katiearose.sobriety.internal.CircularBuffer
import com.katiearose.sobriety.utils.putLast
import com.katiearose.sobriety.utils.secondsFromNow
import com.katiearose.sobriety.utils.toJSONObject
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class Addiction(
    val name: String,
    var lastRelapse: Instant,
    var isStopped: Boolean,
    var timeStopped: Long, //in milliseconds
    val history: LinkedHashMap<Long, Long>, //in milliseconds
    var priority: Priority,
    val dailyNotes: LinkedHashMap<LocalDate, String>,
    var timeSaving: LocalTime,
    val savings: LinkedHashMap<String, Pair<Double, String>>,
    val milestones: LinkedHashSet<Pair<Int, ChronoUnit>>,
    private val relapses: CircularBuffer<Long> = CircularBuffer(3) //Default is a new one, but you can provide your own (from a cache)
) : Serializable {
    var averageRelapseDuration = if (relapses.get(0) == null) -1 else calculateAverageRelapseDuration()
        private set

    enum class Priority {
        HIGH, MEDIUM, LOW
    }

    fun isFuture(): Boolean {
        return lastRelapse > Instant.now()
    }

    fun stopAbstaining() {
        isStopped = true
        timeStopped = System.currentTimeMillis()
        relapses.update(Instant.ofEpochMilli(timeStopped).epochSecond - lastRelapse.epochSecond)
        averageRelapseDuration = calculateAverageRelapseDuration()
        history.putLast(System.currentTimeMillis())
    }

    fun relapse() {
        if (!isStopped && !isFuture()) {
            relapses.update(lastRelapse.secondsFromNow())
            history.putLast(System.currentTimeMillis())
        }
        history[System.currentTimeMillis()] = 0
        isStopped = false
        averageRelapseDuration = calculateAverageRelapseDuration()
        lastRelapse = Instant.now()
    }

    private fun calculateAverageRelapseDuration(): Long {
        return relapses.getAll().filterNotNull().sumOf { it } / 3L
    }

    fun toJSON(): JSONObject {
        val addictionJSON: JSONObject = JSONObject();
        addictionJSON.put("name", name)

        val lastRelapseJSON = JSONObject()
        lastRelapseJSON.put("epochSeconds", lastRelapse.epochSecond)
        lastRelapseJSON.put("nanosecondsOfSecond", lastRelapse.nano)
        addictionJSON.put("last_relapse", lastRelapseJSON)


        addictionJSON.put("is_stopped", isStopped)
        addictionJSON.put("time_stopped", timeStopped)
        addictionJSON.put("history", history.toJSONObject())
        addictionJSON.put("priority", priority.ordinal)
        addictionJSON.put("daily_notes", dailyNotes.toJSONObject())
        addictionJSON.put("time_saving", timeSaving.toString())
        addictionJSON.put("savings", savings.toJSONObject(process_value = { pair ->
            val json = JSONObject()
            json.put("first", pair.first)
            json.put("second", pair.second)
            json
        }))

        val milestonesJSON = JSONArray()
        for (pair in milestones) {
            val json = JSONObject()
            json.put("first", pair.first)
            milestonesJSON.put(milestonesJSON.length(), json)
            json.put("second", when (pair.second) {
                ChronoUnit.HOURS -> JSONObject()
                    .put("type", "TimeBased")
                    .put("nanoseconds", 3600000000000)
                ChronoUnit.DAYS -> JSONObject()
                    .put("type", "DayBased")
                    .put("days", 1)
                ChronoUnit.WEEKS -> JSONObject()
                    .put("type", "DayBased")
                    .put("days", 7)
                ChronoUnit.MONTHS -> JSONObject()
                    .put("type", "MonthBased")
                    .put("months", 1)
                ChronoUnit.YEARS -> JSONObject()
                    .put("type", "MonthBased")
                    .put("months", 12)
                else -> JSONObject()
            })
        }
        addictionJSON.put("milestones", milestonesJSON)

        val relapsesJSON = JSONObject()
        val relapsesList = relapses.getAll()
        relapsesJSON.put("size", relapsesList.size)
        relapsesJSON.put("buffer", JSONArray(relapsesList))
        addictionJSON.put("relapses", relapsesJSON)
        return addictionJSON
    }

    fun toCacheable(): HashMap<Int, Any> {
        val map = HashMap<Int, Any>()
        map[0] = name
        map[1] = lastRelapse
        map[2] = isStopped
        map[3] = timeStopped
        map[4] = history
        map[5] = priority
        map[6] = dailyNotes
        map[7] = timeSaving
        map[8] = savings
        map[9] = milestones
        map[10] = relapses
        return map
    }

    companion object {
        fun fromCacheable(map: HashMap<Int, Any>): Addiction {
            //Migration strategies
            if (map.size == 7) { //is it version 6.1.1?
                return Addiction(
                    map[0] as String,
                    map[1] as Instant,
                    map[2] as Boolean,
                    map[3] as Long,
                    map[4] as LinkedHashMap<Long, Long>,
                    map[5] as Priority,
                    //default values...
                    LinkedHashMap(),
                    LocalTime.of(0, 0),
                    LinkedHashMap(),
                    LinkedHashSet(),
                    map[6] as CircularBuffer<Long>
                )
            } else return Addiction(
                map[0] as String,
                map[1] as Instant,
                map[2] as Boolean,
                map[3] as Long,
                map[4] as LinkedHashMap<Long, Long>,
                map[5] as Priority,
                map[6] as LinkedHashMap<LocalDate, String>,
                map[7] as LocalTime,
                map[8] as LinkedHashMap<String, Pair<Double, String>>,
                map[9] as LinkedHashSet<Pair<Int, ChronoUnit>>,
                map[10] as CircularBuffer<Long>
            )
        }
    }
}