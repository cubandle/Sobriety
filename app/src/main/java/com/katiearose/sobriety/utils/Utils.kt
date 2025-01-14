package com.katiearose.sobriety.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import androidx.preference.PreferenceManager
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.katiearose.sobriety.R
import com.katiearose.sobriety.activities.Main
import com.katiearose.sobriety.shared.CacheHandler
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.Period
import java.util.*

private const val MINUTE = 60
private const val HOUR = MINUTE * 60
private const val DAY = HOUR * 24
private const val WEEK = DAY * 7
private const val YEAR = (DAY * 365.25).toInt()
private const val MONTH = YEAR / 12


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
    if (y != 0L) stringBuilder.append(resources.getQuantityString(R.plurals.years, y.toInt(), y)).append(" ")
    if (mo != 0L) stringBuilder.append(resources.getQuantityString(R.plurals.months, mo.toInt(), mo)).append(" ")
    if (w != 0L) stringBuilder.append(resources.getQuantityString(R.plurals.weeks, w.toInt(), w)).append(" ")
    if (d != 0L) stringBuilder.append(resources.getQuantityString(R.plurals.days, d.toInt(), d)).append(" ")
    if (h != 0L) stringBuilder.append(resources.getQuantityString(R.plurals.hours, h.toInt(), h)).append(" ")
    if (m != 0L) stringBuilder.append(resources.getQuantityString(R.plurals.minutes, m.toInt(), m)).append(" ")
    if (!(y == 0L && mo == 0L && w == 0L && d == 0L && h == 0L && m == 0L)) stringBuilder.append(
        getString(R.string.and)
    ).append(" ")
    stringBuilder.append(resources.getQuantityString(R.plurals.seconds, s.toInt(), s))
    return stringBuilder.toString()
}

// Expects start and end timestamps in epoch milliseconds
fun Context.convertRangeToString(start: Long, end: Long = Instant.now().toEpochMilli()): String {
    if (start == -1L) return ""
    val startDate: LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(start),
        TimeZone.getDefault().toZoneId())
    val endDate: LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(end),
        TimeZone.getDefault().toZoneId())
    // Period for years, months, weeks, days
    val period: Period = Period.between(startDate.toLocalDate(), endDate.toLocalDate())
    // Duration for hours, minutes, seconds
    val duration: Duration = Duration.between(startDate, endDate)

    val y = period.years
    val mo = period.months
    val d = period.days % 7
    val w = period.days / 7
    val h = duration.toHoursPart()
    val m = duration.toMinutesPart()
    val s = duration.toSecondsPart()

    val stringBuilder = StringBuilder()
    if (y != 0) stringBuilder.append(resources.getQuantityString(R.plurals.years, y, y)).append(" ")
    if (mo != 0) stringBuilder.append(resources.getQuantityString(R.plurals.months, mo, mo)).append(" ")
    if (w != 0) stringBuilder.append(resources.getQuantityString(R.plurals.weeks, w, w)).append(" ")
    if (d != 0) stringBuilder.append(resources.getQuantityString(R.plurals.days, d, d)).append(" ")
    if (h != 0) stringBuilder.append(resources.getQuantityString(R.plurals.hours, h, h)).append(" ")
    if (m != 0) stringBuilder.append(resources.getQuantityString(R.plurals.minutes, m, m)).append(" ")
    if (!(y == 0 && mo == 0 && w == 0 && d == 0 && h == 0 && m == 0)) stringBuilder.append(
        getString(R.string.and)
    ).append(" ")
    stringBuilder.append(resources.getQuantityString(R.plurals.seconds, s, s))
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

fun Activity.checkValidIntentData() : Int {
    val index = intent.getIntExtra(Main.EXTRA_ADDICTION_POSITION, -1)
    require(index != -1) { "Invalid intent data received" }
    return index
}

fun AppCompatEditText.isInputEmpty(): Boolean = text == null || text.toString().isBlank()

fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

fun Context.toast(@StringRes textRes: Int) = Toast.makeText(this, textRes, Toast.LENGTH_SHORT).show()

inline var TextView.textResource: Int
    set(@StringRes value) = setText(value)
    @Deprecated("This property is set-only, don't bother.", level = DeprecationLevel.HIDDEN)
    get() = error("don't")

fun CacheHandler.write() = writeCache(Main.addictions)