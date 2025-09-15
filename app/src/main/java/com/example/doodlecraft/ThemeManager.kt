package com.example.doodlecraft

import android.content.Context
import android.content.SharedPreferences

enum class ThemeMode {
    LIGHT, DARK, NIGHT
}

object ThemeManager {
    private const val PREFS_NAME = "ThemePrefs"
    private const val KEY_THEME = "theme_mode"

    fun getTheme(context: Context): ThemeMode {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeName = prefs.getString(KEY_THEME, ThemeMode.LIGHT.name)
        // Use try-catch for safety in case of a stored string not matching the enum
        return try {
            ThemeMode.valueOf(themeName!!)
        } catch (e: IllegalArgumentException) {
            ThemeMode.LIGHT
        }
    }

    fun setTheme(context: Context, theme: ThemeMode) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }
}
