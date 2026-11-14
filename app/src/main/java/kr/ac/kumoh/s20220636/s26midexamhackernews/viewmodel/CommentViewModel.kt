package kr.ac.kumoh.s20220636.s26midexamhackernews.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.kumoh.s20220636.s26midexamhackernews.model.NewsItem
import kr.ac.kumoh.s20220636.s26midexamhackernews.repository.NewsRepository
import kr.ac.kumoh.s20220636.s26midexamhackernews.translation.KoreanTranslator
import kr.ac.kumoh.s20220636.s26midexamhackernews.translation.TextTranslator

class CommentViewModel(
    private val text: String,
    private val kids: List<Long>,
    private val repository: NewsRepository,
    private val translator: TextTranslator = KoreanTranslator,
) : NewsListViewModel(translator) {

    override val logTag = "Comments"

    // 상단에 보여줄 원글 제목 / 상위 댓글의 번역문
    private val _translatedHeader = MutableStateFlow<String?>(null)
    val translatedHeader = _translatedHeader.asStateFlow()

    override suspend fun fetch(): List<NewsItem> =
        repository.getComments(kids) ?: emptyList()

    override fun textOf(item: NewsItem) = item.text

    fun translateHeader() {
        if (_translatedHeader.value != null || text.isBlank()) return
        viewModelScope.launch {
            runCatching { translator.translate(text) }
                .onSuccess { _translatedHeader.value = it }
        }
    }

    init {
        load()
    }
}
