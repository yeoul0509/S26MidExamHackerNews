package kr.ac.kumoh.s20220636.s26midexamhackernews.model

data class NewsItem(
    val id: Long,
    val type: String,            // "story" (글) 또는 "comment" (댓글) 구별용
    val by: String,              // 작성자
    val time: Long,              // 작성 시간
    val kids: List<Long> = emptyList(), // 하위 댓글 ID 리스트

    val title: String = "",      // 글 제목 (댓글일 때는 빈 값)
    val url: String = "",        // 글 외부 링크 URL
    val score: Int = 0,          // 글 추천수
    val descendants : Int = 0,   // 글에 달린 총 댓글 수

    val text: String = "",       // 댓글 본문 (글일 때는 빈 값)
)