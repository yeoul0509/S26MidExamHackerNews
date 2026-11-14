package kr.ac.kumoh.s20220636.s26midexamhackernews.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.ac.kumoh.s20220636.s26midexamhackernews.model.NewsItem
import kr.ac.kumoh.s20220636.s26midexamhackernews.translation.TextTranslator

/**
 * 목록 화면 공통 UI 상태
 */
data class NewsListUiState(
    val items: List<NewsItem> = emptyList(),
    val isLoading: Boolean = true,          // 최초 로딩
    val isRefreshing: Boolean = false,      // 당겨서 새로고침
    val errorMessage: String? = null,

    val isTranslating: Boolean = false,
    val translations: Map<Long, String> = emptyMap(),  // 아이템 id → 번역문
    val translateError: String? = null,
)

/**
 * 뉴스/댓글 목록 로딩 + 한국어 번역을 공통으로 처리하는 ViewModel
 */
abstract class NewsListViewModel(
    private val translator: TextTranslator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsListUiState())
    val uiState: StateFlow<NewsListUiState> = _uiState.asStateFlow()

    private var translateJob: Job? = null

    /** 실제 데이터를 가져오는 함수 */
    protected abstract suspend fun fetch(): List<NewsItem>

    /** 번역 대상 텍스트 (글은 제목, 댓글은 본문) */
    protected abstract fun textOf(item: NewsItem): String

    protected abstract val logTag: String

    fun load(refresh: Boolean = false) {
        _uiState.update {
            it.copy(
                isLoading = !refresh && it.items.isEmpty(),
                isRefreshing = refresh,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            try {
                val items = fetch()
                _uiState.update {
                    it.copy(items = items, isLoading = false, isRefreshing = false)
                }
            } catch (e: Exception) {
                Log.e(logTag, "로딩 실패", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "불러오지 못했어요. 네트워크 연결을 확인해 주세요."
                    )
                }
            }
        }
    }

    fun clearTranslateError() {
        _uiState.update { it.copy(translateError = null) }
    }

    /** 아직 번역되지 않은 아이템만 번역 (한국어 모드가 켜졌을 때 화면에서 호출) */
    fun translateMissing() {
        if (translateJob?.isActive == true) return

        translateJob = viewModelScope.launch {
            val state = _uiState.value
            val targets = state.items.filter {
                it.id !in state.translations && textOf(it).isNotBlank()
            }
            if (targets.isEmpty()) return@launch

            _uiState.update { it.copy(isTranslating = true) }
            try {
                val results = targets.map { item ->
                    async { item.id to translator.translate(textOf(item)) }
                }.awaitAll()

                _uiState.update {
                    it.copy(translations = it.translations + results, isTranslating = false)
                }
            } catch (e: Exception) {
                Log.e(logTag, "번역 실패", e)
                _uiState.update {
                    it.copy(
                        isTranslating = false,
                        translateError = "번역에 실패했어요. 처음 사용 시 번역 모델을 받기 위해 인터넷 연결이 필요합니다."
                    )
                }
            }
        }
    }
}
