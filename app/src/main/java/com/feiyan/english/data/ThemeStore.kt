package com.feiyan.english.data

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 日间 / 夜间模式。首次启动跟随系统，用户切换后持久化选择。 */
object ThemeStore {

    private const val PREFS_NAME = "feiyan_theme"
    private const val KEY_DARK = "dark_mode"

    private var prefs: SharedPreferences? = null
    private val _isDark = MutableStateFlow(false)

    val isDark: StateFlow<Boolean> = _isDark.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val systemDark =
            (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        _isDark.value = prefs?.getBoolean(KEY_DARK, systemDark) ?: systemDark
    }

    fun toggle() {
        setDark(!_isDark.value)
    }

    fun setDark(dark: Boolean) {
        _isDark.value = dark
        prefs?.edit()?.putBoolean(KEY_DARK, dark)?.apply()
    }
}
