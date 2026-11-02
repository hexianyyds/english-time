package com.feiyan.english.ui.update

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.feiyan.english.data.UpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UpdateUiState(
    val checking: Boolean = false,
    val downloading: Boolean = false,
    val progress: Float = 0f,
    val info: UpdateManager.ReleaseInfo? = null,
    val status: String? = null,
    val dialogVisible: Boolean = false,
    val autoUpdate: Boolean = false,
) {
    val currentVersion: String get() = UpdateManager.currentVersion
}

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(
        UpdateUiState(autoUpdate = UpdateManager.autoUpdateEnabled(application)),
    )
    val state: StateFlow<UpdateUiState> = _state.asStateFlow()

    /**
     * @param silent 静默检查（启动时）：已是最新就不弹窗
     * @param autoInstall 开启自动更新时，发现新版直接下载安装
     */
    fun check(silent: Boolean = false, autoInstall: Boolean = false) {
        if (_state.value.checking || _state.value.downloading) return
        val context = getApplication<Application>()
        if (!UpdateManager.configured) {
            _state.update { it.copy(status = "未配置更新仓库（UPDATE_REPO）") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(checking = true, status = null) }
            runCatching { UpdateManager.checkLatest() }
                .onSuccess { info ->
                    if (info == null) {
                        _state.update {
                            it.copy(
                                checking = false,
                                info = null,
                                dialogVisible = !silent,
                                status = "已经是最新版本 v${UpdateManager.currentVersion}",
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                checking = false,
                                info = info,
                                dialogVisible = true,
                                status = "检测到新版本 v${info.version}",
                            )
                        }
                        if (autoInstall && UpdateManager.autoUpdateEnabled(context)) startDownload()
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            checking = false,
                            dialogVisible = !silent,
                            status = error.message ?: "检查更新失败",
                        )
                    }
                }
        }
    }

    fun startDownload() {
        val info = _state.value.info ?: return
        if (_state.value.downloading) return
        val context = getApplication<Application>()
        viewModelScope.launch {
            _state.update { it.copy(downloading = true, progress = 0f, status = "正在下载…") }
            runCatching {
                UpdateManager.download(context, info) { progress, _, _ ->
                    _state.update { it.copy(progress = progress) }
                }
            }.onSuccess { file ->
                _state.update {
                    it.copy(downloading = false, dialogVisible = false, status = "下载完成，正在安装…")
                }
                UpdateManager.install(context, file)
            }.onFailure { error ->
                _state.update {
                    it.copy(downloading = false, status = error.message ?: "下载失败")
                }
            }
        }
    }

    fun dismiss() {
        _state.update { it.copy(dialogVisible = false) }
    }

    fun toggleAutoUpdate(enabled: Boolean) {
        UpdateManager.setAutoUpdate(getApplication(), enabled)
        _state.update { it.copy(autoUpdate = enabled) }
    }
}
