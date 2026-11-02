package com.feiyan.english.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feiyan.english.core.net.ByApi
import com.feiyan.english.data.PodcastRepository
import com.feiyan.english.data.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 单个站点的登录态编辑状态。 */
data class CredentialUiState(
    val input: String = "",
    val saved: String = "",
    val busy: Boolean = false,
    val summary: String? = null,
    val error: String? = null,
    val rows: List<Pair<String, String>> = emptyList(),
)

data class SettingsUiState(
    val feiyan: CredentialUiState = CredentialUiState(),
    val bookyell: CredentialUiState = CredentialUiState(),
)

class SettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        val feiyan = TokenStore.get()
        val bookyell = TokenStore.bookyellToken()
        _state.value = SettingsUiState(
            feiyan = CredentialUiState(input = feiyan, saved = feiyan),
            bookyell = CredentialUiState(input = bookyell, saved = bookyell),
        )
        if (feiyan.isNotBlank()) verifyFeiyan()
        if (bookyell.isNotBlank()) verifyBookyell()
    }

    fun onFeiyanChange(value: String) {
        _state.update { it.copy(feiyan = it.feiyan.copy(input = value, error = null)) }
    }

    fun onBookyellChange(value: String) {
        _state.update { it.copy(bookyell = it.bookyell.copy(input = value, error = null)) }
    }

    fun saveFeiyan() {
        val token = _state.value.feiyan.input.trim()
        TokenStore.save(token)
        _state.update {
            it.copy(feiyan = it.feiyan.copy(saved = token, summary = null, rows = emptyList(), error = null))
        }
        if (token.isNotBlank()) verifyFeiyan()
    }

    fun clearFeiyan() {
        TokenStore.clear()
        _state.update { it.copy(feiyan = CredentialUiState()) }
    }

    fun saveBookyell() {
        val token = _state.value.bookyell.input.trim()
        TokenStore.saveBookyell(token)
        _state.update {
            it.copy(bookyell = it.bookyell.copy(saved = token, summary = null, rows = emptyList(), error = null))
        }
        if (token.isNotBlank()) verifyBookyell()
    }

    fun clearBookyell() {
        TokenStore.clearBookyell()
        _state.update { it.copy(bookyell = CredentialUiState()) }
    }

    fun verifyFeiyan() {
        if (TokenStore.get().isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(feiyan = it.feiyan.copy(busy = true, error = null)) }
            runCatching { PodcastRepository.me() }
                .onSuccess { user ->
                    _state.update {
                        it.copy(
                            feiyan = it.feiyan.copy(
                                busy = false,
                                error = null,
                                summary = (user.phone ?: "已登录") + " · " +
                                    if (user.member) "会员" else "普通账号",
                                rows = listOfNotNull(
                                    user.phone?.let { phone -> "手机号" to phone },
                                    "账号类型" to if (user.member) "会员账号" else "普通账号",
                                    "已解锁" to if (user.member) "完整字幕 · 会员节目" else "完整字幕",
                                ),
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            feiyan = it.feiyan.copy(
                                busy = false,
                                summary = null,
                                rows = emptyList(),
                                error = error.message ?: "校验失败",
                            ),
                        )
                    }
                }
        }
    }

    fun verifyBookyell() {
        if (TokenStore.bookyellToken().isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(bookyell = it.bookyell.copy(busy = true, error = null)) }
            runCatching { ByApi.me() }
                .onSuccess { me ->
                    if (me == null || !me.isLoggedIn) {
                        _state.update {
                            it.copy(
                                bookyell = it.bookyell.copy(
                                    busy = false,
                                    summary = null,
                                    rows = emptyList(),
                                    error = "登录态已失效",
                                ),
                            )
                        }
                        return@onSuccess
                    }
                    val member = me.memberExpiredAt > System.currentTimeMillis() / 1000
                    _state.update {
                        it.copy(
                            bookyell = it.bookyell.copy(
                                busy = false,
                                error = null,
                                summary = (me.nickname ?: "已登录") + " · " +
                                    (me.memberTypeString ?: if (member) "会员" else "普通用户"),
                                rows = listOfNotNull(
                                    me.nickname?.let { name -> "昵称" to name },
                                    (me.email?.takeIf { email -> email.isNotBlank() })?.let { email -> "邮箱" to email },
                                    "账号类型" to (me.memberTypeString ?: "普通用户"),
                                    "朗读权限" to if (member) "会员朗读" else "每日 1 次免费试听",
                                ),
                            ),
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            bookyell = it.bookyell.copy(
                                busy = false,
                                summary = null,
                                rows = emptyList(),
                                error = error.message ?: "校验失败",
                            ),
                        )
                    }
                }
        }
    }
}
