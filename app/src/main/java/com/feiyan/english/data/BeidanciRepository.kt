package com.feiyan.english.data

import com.feiyan.english.core.model.BdCourse
import com.feiyan.english.core.model.BdUnit
import com.feiyan.english.core.model.BdWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object BeidanciRepository {

    const val WEB_BASE = "https://beidanci365.com"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Volatile
    private var coursesCache: List<BdCourse>? = null

    private val unitCache = ConcurrentHashMap<Int, List<BdUnit>>()
    private val wordCache = ConcurrentHashMap<String, List<BdWord>>()

    suspend fun courses(force: Boolean = false): List<BdCourse> {
        coursesCache?.takeUnless { force }?.let { return it }
        return withContext(Dispatchers.IO) {
            val text = get("/api/courses")
            val courses = json.decodeFromString(
                ListSerializer(BdCourse.serializer()),
                text,
            )
            coursesCache = courses
            courses
        }
    }

    suspend fun course(key: String): BdCourse? = courses().firstOrNull { it.key == key }

    suspend fun units(courseId: Int, force: Boolean = false): List<BdUnit> {
        if (!force) unitCache[courseId]?.let { return it }
        return withContext(Dispatchers.IO) {
            val text = get("/api/courses/$courseId/units")
            val units = json.decodeFromString(ListSerializer(BdUnit.serializer()), text)
            unitCache[courseId] = units
            units
        }
    }

    suspend fun words(
        courseId: Int,
        unitId: Int,
        limit: Int = 200,
        force: Boolean = false,
    ): List<BdWord> {
        val key = "$courseId:$unitId:$limit"
        if (!force) wordCache[key]?.let { return it }
        return withContext(Dispatchers.IO) {
            val text = get("/api/courses/$courseId/units/$unitId/words?limit=$limit")
            val words = json.decodeFromString(ListSerializer(BdWord.serializer()), text)
            wordCache[key] = words
            words
        }
    }

    private fun get(path: String): String {
        val request = Request.Builder()
            .url(WEB_BASE + path)
            .header("Accept", "application/json")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) EnglishTime/1.0")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("背单词365返回 HTTP ${response.code}")
            }
            return response.body.string()
        }
    }
}
