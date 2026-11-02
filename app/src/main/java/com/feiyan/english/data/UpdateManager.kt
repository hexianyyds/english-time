package com.feiyan.english.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.feiyan.english.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 基于 GitHub Release 的自更新：
 *  - 检查 https://api.github.com/repos/{owner}/{repo}/releases/latest
 *  - 下载 apk 资产（带进度回调）
 *  - 通过 FileProvider 拉起系统安装器
 */
object UpdateManager {

    data class ReleaseInfo(
        val version: String,
        val notes: String,
        val apkUrl: String,
        val size: Long,
    )

    private const val PREFS = "feiyan_update"
    private const val KEY_AUTO = "auto_update"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val repo: String get() = BuildConfig.UPDATE_REPO.ifBlank { "" }

    val configured: Boolean get() = repo.isNotBlank()

    val currentVersion: String get() = BuildConfig.VERSION_NAME

    fun autoUpdateEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_AUTO, false)

    fun setAutoUpdate(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_AUTO, enabled).apply()
    }

    /** 返回最新版本；已是最新或仓库未配置时返回 null。 */
    suspend fun checkLatest(): ReleaseInfo? = withContext(Dispatchers.IO) {
        if (!configured) return@withContext null
        val request = Request.Builder()
            .url("https://api.github.com/repos/$repo/releases/latest")
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "FeiyanEnglish/${BuildConfig.VERSION_NAME}")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                if (response.code == 404) return@withContext null
                throw IllegalStateException("检查更新失败：HTTP ${response.code}")
            }
            val body = json.parseToJsonElement(response.body.string()).jsonObject
            val tag = body["tag_name"]?.jsonPrimitive?.content.orEmpty().removePrefix("v")
            val notes = body["body"]?.jsonPrimitive?.content.orEmpty()
            if (tag.isBlank() || !isNewer(tag, BuildConfig.VERSION_NAME)) return@withContext null
            val assets = body["assets"] as? JsonArray ?: JsonArray(emptyList())
            val apk = assets.mapNotNull { it as? JsonObject }
                .firstOrNull { asset ->
                    asset["name"]?.jsonPrimitive?.content?.endsWith(".apk", ignoreCase = true) == true
                } ?: return@withContext null
            ReleaseInfo(
                version = tag,
                notes = notes.take(400),
                apkUrl = apk["browser_download_url"]?.jsonPrimitive?.content.orEmpty(),
                size = apk["size"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
            )
        }
    }

    fun isNewer(remote: String, current: String): Boolean {
        val a = remote.split(".", "-").mapNotNull { it.toIntOrNull() }
        val b = current.split(".", "-").mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    suspend fun download(
        context: Context,
        info: ReleaseInfo,
        onProgress: (Float, Long, Long) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(info.apkUrl)
            .header("User-Agent", "FeiyanEnglish/${BuildConfig.VERSION_NAME}")
            .get()
            .build()
        val target = File(context.cacheDir, "feiyan-${info.version}.apk")
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("下载失败：HTTP ${response.code}")
            val body = response.body
            val total = if (body.contentLength() > 0) body.contentLength() else info.size
            var written = 0L
            body.byteStream().use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        written += read
                        val progress = if (total > 0) written.toFloat() / total else 0f
                        onProgress(progress.coerceIn(0f, 1f), written, total)
                    }
                }
            }
        }
        target
    }

    /** 拉起系统安装器；未授予「安装未知应用」权限时跳到授权页。 */
    fun install(context: Context, file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val settings = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(settings)
            return
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
