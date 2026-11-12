package kr.ac.kumoh.s20220636.s26midexamhackernews.repository
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

interface NewsApi {
    /**
     * 현재 가장 인기 있는 스토리(Top Stories)의 ID 리스트
     * 반환 값 예시: [50242870, 50215997, ...]
     */
    @GET("topstories.json")
    suspend fun getTopStoryIds(): List<Long>

    /**
     * 특정 ID를 가진 아이템(Story 또는 Comment)의 상세 정보
     * @param id 글 또는 댓글의 고유 ID
     */
    @GET("item/{id}.json")
    suspend fun getItemDetail(
        @Path("id") id: Long
    ): HackerNewsDto
}

@Serializable
data class HackerNewsDto(
    val id: Long,
    val type: String,         // "story" 글 또는 "comment" 댓글
    val by: String? = null,  // deleted=true일 때 누락되므로 Nullable
    val time: Long,
    val kids: List<Long> = emptyList(),
    val title: String? = null,          // story 전용
    val url: String? = null,            // story 전용
    val score: Int = 0,                 // story 전용
    val descendants: Int = 0,           // story 전용 (총 댓글 수)
    val text: String? = null, // comment 전용 (deleted=true일 때 누락되므로 Nullable)
    val deleted: Boolean = false,       // 삭제된 댓글
    val dead: Boolean = false           // 블라인드된 글
)