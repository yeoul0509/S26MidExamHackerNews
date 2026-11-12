package kr.ac.kumoh.s20220636.s26midexamhackernews.repository

import kr.ac.kumoh.s20220636.s26midexamhackernews.model.NewsItem

interface NewsRepository {
    /**
     * 인기 글 리스트
     * @param limit 가져올 글의 최대 개수
     * @return 뉴스 아이템 리스트
     */
    suspend fun getTopStories(limit: Int): List<NewsItem>

    /**
     * 지정된 자식 ID 목록(kids)에 해당하는 댓글 리스트
     * 대댓글은 수집하지 않음
     * @param kids 조회할 하위 댓글들의 고유 ID 리스트
     * @return 존재하지 않거나 삭제된 경우 null 반환
     */
    suspend fun getComments(kids: List<Long>): List<NewsItem>?
}