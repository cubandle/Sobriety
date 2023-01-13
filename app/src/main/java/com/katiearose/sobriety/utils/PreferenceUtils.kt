package com.katiearose.sobriety.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.preference.PreferenceManager
import com.katiearose.sobriety.activities.Main
import org.json.JSONArray

fun Context.getSharedPref(): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(this)
}

fun SharedPreferences.getSortNotesPref(): String {
    return getString("sort_notes", "asc")!!
}

fun SharedPreferences.getSortMilestonesPref(): String {
    return getString("sort_milestones", "asc")!!
}

fun SharedPreferences.getHideCompletedMilestonesPref(): Boolean {
    return getBoolean("hide_completed_milestones", false)
}

fun Context.exportData(uri: Uri) {
    // Construct the JSON Object
    val json: JSONArray = JSONArray();
    for (i in 0 until Main.addictions.size) {
        json.put(i, Main.addictions[i].toJSON())
    }

    // Write to file
    json.toString(4).byteInputStream().use { input ->
        contentResolver.openOutputStream(uri)?.use { output ->
            input.copyTo(output)
        }
    }
}