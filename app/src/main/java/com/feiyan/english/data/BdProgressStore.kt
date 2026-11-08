package com.feiyan.english.data

import android.content.Context
import android.content.SharedPreferences
import com.feiyan.english.core.model.BdResume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 无需登录的本地续学位置。 */
object BdProgressStore {

    private const val PREFS_NAME = "beidanci365_progress"
    private const val KEY_COURSE = "course_key"
    private const val KEY_COURSE_TITLE = "course_title"
    private const val KEY_UNIT_ID = "unit_id"
    private const val KEY_UNIT_TITLE = "unit_title"

    private var prefs: SharedPreferences? = null
    private val _resume = MutableStateFlow<BdResume?>(null)

    val resume: StateFlow<BdResume?> = _resume.asStateFlow()

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val courseKey = prefs?.getString(KEY_COURSE, "").orEmpty()
        if (courseKey.isNotBlank()) {
            _resume.value = BdResume(
                courseKey = courseKey,
                courseTitle = prefs?.getString(KEY_COURSE_TITLE, "").orEmpty(),
                unitId = prefs?.getInt(KEY_UNIT_ID, 0) ?: 0,
                unitTitle = prefs?.getString(KEY_UNIT_TITLE, "").orEmpty(),
            )
        }
    }

    fun save(context: Context, resume: BdResume) {
        init(context)
        _resume.value = resume
        prefs?.edit()
            ?.putString(KEY_COURSE, resume.courseKey)
            ?.putString(KEY_COURSE_TITLE, resume.courseTitle)
            ?.putInt(KEY_UNIT_ID, resume.unitId)
            ?.putString(KEY_UNIT_TITLE, resume.unitTitle)
            ?.apply()
    }
}
