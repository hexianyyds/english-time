package com.feiyan.english.core.net

/**
 * BookYell 登录态（JWT）。接口用 `Authorization: Bearer <token>`，
 * 由 FeiyanApi 的拦截器统一注入，朗读音频走同一个 OkHttp 客户端。
 */
object ByAuth {

    @Volatile
    private var token: String = ""

    val isLoggedIn: Boolean get() = token.isNotEmpty()

    fun current(): String = token

    fun update(value: String?) {
        token = value.orEmpty().trim()
    }
}
