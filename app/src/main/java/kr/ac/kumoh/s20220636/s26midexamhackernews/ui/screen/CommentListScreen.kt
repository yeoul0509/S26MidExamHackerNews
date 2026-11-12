package kr.ac.kumoh.s20220636.s26midexamhackernews.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kr.ac.kumoh.s20220636.s26midexamhackernews.model.NewsItem
import kr.ac.kumoh.s20220636.s26midexamhackernews.repository.NewsRepository
import kr.ac.kumoh.s20220636.s26midexamhackernews.viewmodel.CommentViewModel


@Composable
fun CommentListScreen(
    text: String,
    kids: List<Long>,
    repository: NewsRepository,
    viewModel: CommentViewModel = viewModel {
        CommentViewModel(
            text = text,
            kids = kids,
            repository = repository
        )
    },
    onNavigateToComment: (String, List<Long>) -> Unit,
) {
    val comments by viewModel.comments.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text)
        }

        items(comments) { item ->
            CommentItemRow(item) {
                onNavigateToComment(item.text, item.kids)
            }
        }
    }
}

@Composable
fun CommentItemRow(
    comment: NewsItem,
    navigateToComment: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = navigateToComment
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(comment.text)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        model = "https://i.pravatar.cc/150?u=${comment.by}",
                        contentDescription = "${comment.by} 이미지",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = comment.by,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "댓글 ${comment.kids.size}개",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}