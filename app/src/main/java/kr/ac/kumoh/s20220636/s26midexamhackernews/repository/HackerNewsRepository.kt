package kr.ac.kumoh.s20220636.s26midexamhackernews.repository

import android.text.Html
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.json.Json
import kr.ac.kumoh.s20220636.s26midexamhackernews.model.NewsItem
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class HackerNewsRepository (
    private val api: NewsApi = newsService
) : NewsRepository {

    companion object {
        private val jsonConfig = Json {
            // 서버가 내가 안 만든 데이터(Key)를 보내더라도 에러 내지 말고 무시
            ignoreUnknownKeys = true
        }

        private val newsService: NewsApi by lazy {
            Retrofit.Builder()
                .baseUrl("https://hacker-news.firebaseio.com/v0/")
                .addConverterFactory(
                    jsonConfig.asConverterFactory("application/json".toMediaType())
                )
                .build()
                .create(NewsApi::class.java)
        }
    }

    // 일부 async에서 Exception이 발생해도
    // 다른 요청들을 강제 취소시키지 않고 끝까지 정상 처리하기 위해 사용
    override suspend fun getTopStories(limit: Int): List<NewsItem> = supervisorScope             {
        runCatching { api.getTopStoryIds() }
            .getOrDefault(emptyList())
            .take(limit)
            .map { id ->
                async {
                    // story (글)인데 삭제되었거나 에러가 나면 리스트에서 빠지도록 null 처리
                    runCatching { fetchNewsItem(id) }.getOrNull()
                }
            }
            .awaitAll()
            .filterNotNull()
    }

    override suspend fun getComments(kids: List<Long>): List<NewsItem>?  = supervisorScope {
        if (kids.isEmpty()) return@supervisorScope   null

        kids.map { id ->
            async {
                // comment (댓글) 로딩에 실패하면 리스트에서 빠지도록 null 처리
                runCatching { fetchNewsItem(id) }.getOrNull()
            }
        }
            .awaitAll()
            .filterNotNull()
    }

    private suspend fun fetchNewsItem(id: Long): NewsItem {
        val dto = api.getItemDetail(id)

        // story 글이 삭제되었거나 블라인드된 경우
        // 상위 mapNotNull/filterNotNull에서 걸러지도록 예외 발생
        if (dto.type == "story" && (dto.deleted || dto.dead)) {
            throw IllegalStateException("Deleted or dead story")
        }

        // comment 댓글이 삭제된 경우 작성자: [deleted], text: [삭제된 댓글입니다]
        val author = if (dto.deleted) "[deleted]" else (dto.by ?: "unknown")
        val contentText = if (dto.deleted) "[삭제된 댓글입니다]" else (dto.text ?: "")

        // HTML 텍스트 변환
        val title = dto.title?.let { Html.fromHtml(
            dto.title,
            Html.FROM_HTML_MODE_LEGACY)
            .toString()
        } ?: ""
        val text = Html.fromHtml(
            contentText,
            Html.FROM_HTML_MODE_LEGACY)
            .toString()
            .trim()

        return NewsItem(
            id = dto.id,
            type = dto.type,
            by = author,
            time = dto.time,
            kids = dto.kids,
            title = title,
            url = dto.url ?: "",
            score = dto.score,
            descendants = dto.descendants,
            text = text
        )
    }
}