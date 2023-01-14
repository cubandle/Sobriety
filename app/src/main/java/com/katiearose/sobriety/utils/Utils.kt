package com.katiearose.sobriety.utils

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.katiearose.sobriety.R
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

private const val MINUTE = 60
private const val HOUR = MINUTE * 60
private const val DAY = HOUR * 24
private const val WEEK = DAY * 7
private const val MONTH = DAY * 31
private const val YEAR = MONTH * 12

fun Context.convertSecondsToString(given: Long): String {
    if (given == -1L) return ""
    var time = given
    val s = time % MINUTE
    time -= s
    val m = (time % HOUR) / MINUTE
    time -= m * MINUTE
    val h = (time % DAY) / HOUR
    time -= h * HOUR
    val d = (time % WEEK) / DAY
    time -= d * DAY
    val w = (time % MONTH) / WEEK
    time -= w * WEEK
    val mo = (time % YEAR) / MONTH
    time -= mo * MONTH
    val y = time / YEAR
    val stringBuilder = StringBuilder()
    if (y != 0L) stringBuilder.append(getString(R.string.years, y)).append(" ")
    if (mo != 0L) stringBuilder.append(getString(R.string.months, mo)).append(" ")
    if (w != 0L) stringBuilder.append(getString(R.string.weeks, w)).append(" ")
    if (d != 0L) stringBuilder.append(getString(R.string.days, d)).append(" ")
    if (h != 0L) stringBuilder.append(getString(R.string.hours, h)).append(" ")
    if (m != 0L) stringBuilder.append(getString(R.string.minutes, m)).append(" ")
    if (!(y == 0L && mo == 0L && w == 0L && d == 0L && h == 0L && m == 0L)) stringBuilder.append(
        getString(R.string.and)
    ).append(" ")
    stringBuilder.append(getString(R.string.seconds, s))
    return stringBuilder.toString()
}

fun Context.showConfirmDialog(title: String, message: String, action: () -> Unit) {
    MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok) { _, _ -> action() }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}

fun Activity.applyThemes() {
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    when (preferences.getString("theme", "system")) {
        "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
    if (preferences.getBoolean("material_you", false)) {
        setTheme(R.style.Theme_Sobriety_Material3)
    } else setTheme(R.style.Theme_Sobriety)
}

fun AppCompatEditText.isInputEmpty(): Boolean {
    return text == null || text.toString().isBlank()
}

fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

fun Instant.secondsFromNow(): Long = Instant.now().epochSecond - this.epochSecond

/**
 * Puts the specified value to the last key in this map.
 */
fun <K, V> LinkedHashMap<K, V>.putLast(value: V) {
    val lastKey = keys.map { it }.last()
    put(lastKey, value)
}

/**
 * Returns JSONObject with keys and values keys as JSONArray
 * This is because JSONObjects do not have guaranteed order
 * process_value is an optional functions to convert val to a JSONObject
 */
fun <K, V> Map<K, V>.toJSONObject(
    process_value: ((V) -> JSONObject)? = null
): JSONObject {
    val json = JSONObject()
    for ((k, v) in this) {
        json.put(k.toString(),
            if (process_value != null) {
                process_value(v)
            } else v)
    }
    return json
}

