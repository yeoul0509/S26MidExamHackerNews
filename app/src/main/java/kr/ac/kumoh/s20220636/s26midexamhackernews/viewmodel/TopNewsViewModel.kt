package kr.ac.kumoh.s20220636.s26midexamhackernews.viewmodel

import kr.ac.kumoh.s20220636.s26midexamhackernews.model.NewsItem
import kr.ac.kumoh.s20220636.s26midexamhackernews.repository.NewsRepository
import kr.ac.kumoh.s20220636.s26midexamhackernews.translation.KoreanTranslator
import kr.ac.kumoh.s20220636.s26midexamhackernews.translation.TextTranslator

class TopNewsViewModel(
    private val repository: NewsRepository,
    translator: TextTranslator = KoreanTranslator,
) : NewsListViewModel(translator) {

    override val logTag = "TopNews"

    override suspend fun fetch(): List<NewsItem> {
        val stories = repository.getTopStories(30)
        // 전체 요청이 실패한 경우 에러 화면을 보여주기 위해 예외 발생
        if (stories.isEmpty()) throw IllegalStateException("No stories")
        return stories
    }

    override fun textOf(item: NewsItem) = item.title

    init {
        load()
    }
}
