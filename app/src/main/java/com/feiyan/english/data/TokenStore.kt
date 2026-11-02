package com.feiyan.english.data

import android.content.Context
import android.content.SharedPreferences
import com.feiyan.english.core.net.FeiyanApi

/**
 * 登录 Cookie 的本地存储。首次启动用编译期注入的 token 兜底，
 * 用户在「设置」里改写后以本地值为准。
 */
object TokenStore {

    private const val PREFS_NAME = "feiyan_prefs"
    private const val KEY_TOKEN = "feiyan_token"
    private const val KEY_BOOKYELL = "bookyell_token"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs?.getString(KEY_TOKEN, null)?.let { stored ->
            // 用户显式保存过（含保存为空 = 退出登录），以本地为准
            FeiyanApi.setToken(stored)
        }
        val storedBookyell = prefs?.getString(KEY_BOOKYELL, null)
        com.feiyan.english.core.net.ByAuth.update(
            storedBookyell ?: com.feiyan.english.BuildConfig.BOOKYELL_TOKEN,
        )
    }

    /** BookYell 登录 JWT。 */
    fun bookyellToken(): String = com.feiyan.english.core.net.ByAuth.current()

    fun saveBookyell(token: String) {
        val value = token.trim()
        prefs?.edit()?.putString(KEY_BOOKYELL, value)?.apply()
        com.feiyan.english.core.net.ByAuth.update(value)
    }

    fun clearBookyell() {
        prefs?.edit()?.putString(KEY_BOOKYELL, "")?.apply()
        com.feiyan.english.core.net.ByAuth.update("")
    }

    fun get(): String = FeiyanApi.currentToken()

    fun save(token: String) {
        val value = token.trim()
        prefs?.edit()?.putString(KEY_TOKEN, value)?.apply()
        FeiyanApi.setToken(value)
    }

    fun clear() {
        prefs?.edit()?.putString(KEY_TOKEN, "")?.apply()
        FeiyanApi.setToken("")
    }
}
