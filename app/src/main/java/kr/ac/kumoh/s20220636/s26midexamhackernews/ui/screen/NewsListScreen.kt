package kr.ac.kumoh.s20220636.s26midexamhackernews.ui.screen

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kr.ac.kumoh.s20220636.s26midexamhackernews.model.NewsItem
import kr.ac.kumoh.s20220636.s26midexamhackernews.repository.NewsRepository
import kr.ac.kumoh.s20220636.s26midexamhackernews.viewmodel.TopNewsViewModel

@Composable
fun NewsListScreen(
    repository: NewsRepository,
    viewModel: TopNewsViewModel = viewModel {
        TopNewsViewModel(repository)
    },
    onNavigateToComment : (String, List<Long>) -> Unit,
) {
    val news by viewModel.news.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(news) { item ->
            StoryItemRow(item) {
                Log.d("NewsListScreen", item.kids.toString())
                onNavigateToComment(item.title, item.kids)
            }
        }
    }
}

@Composable
fun StoryItemRow(
    story: NewsItem,
    navigateToComment: () -> Unit,
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = navigateToComment
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 글 제목
            Text(
                text = story.title,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            // 하단 정보 메타데이터 (추천수, 작성자, 댓글 수)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Parent는 최소 영역, Child는 최대 영역 차지
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // 아바타 이미지 로딩
                AsyncImage(
                    model = "https://i.pravatar.cc/150?u=${story.by}",
                    contentDescription = "${story.by} 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
                Spacer(
                    modifier = Modifier.width(16.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = story.by,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "${story.score}점 댓글 ${story.descendants}개",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            // URL이 있을 때 버튼
            if (story.url.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        // Intent 사용
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            story.url.toUri()
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = story.url,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}