package kr.ac.kumoh.s20220636.s26midexamhackernews.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.ac.kumoh.s20220636.s26midexamhackernews.R
import kr.ac.kumoh.s20220636.s26midexamhackernews.model.NewsItem
import kr.ac.kumoh.s20220636.s26midexamhackernews.repository.NewsRepository
import kr.ac.kumoh.s20220636.s26midexamhackernews.ui.component.Avatar
import kr.ac.kumoh.s20220636.s26midexamhackernews.ui.component.LoadingView
import kr.ac.kumoh.s20220636.s26midexamhackernews.ui.component.MessageView
import kr.ac.kumoh.s20220636.s26midexamhackernews.ui.component.MetaInfo
import kr.ac.kumoh.s20220636.s26midexamhackernews.ui.component.NewsTopBar
import kr.ac.kumoh.s20220636.s26midexamhackernews.ui.component.relativeTime
import kr.ac.kumoh.s20220636.s26midexamhackernews.viewmodel.CommentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentListScreen(
    text: String,
    kids: List<Long>,
    repository: NewsRepository,
    showKorean: Boolean,
    onShowKoreanChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    viewModel: CommentViewModel = viewModel {
        CommentViewModel(
            text = text,
            kids = kids,
            repository = repository
        )
    },
    onNavigateToComment: (String, List<Long>) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val translatedHeader by viewModel.translatedHeader.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showKorean, state.items) {
        if (showKorean) {
            viewModel.translateHeader()
            viewModel.translateMissing()
        }
    }
    LaunchedEffect(state.translateError) {
        state.translateError?.let {
            onShowKoreanChange(false)
            snackbarHostState.showSnackbar(it)
            viewModel.clearTranslateError()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            NewsTopBar(
                title = if (state.isLoading) "댓글" else "댓글 ${state.items.size}개",
                showKorean = showKorean,
                isTranslating = state.isTranslating,
                onToggleKorean = { onShowKoreanChange(!showKorean) },
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            if (state.isLoading) {
                LoadingView()
                return@Box
            }

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.load(refresh = true) },
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 상단: 원글 제목 또는 상위 댓글
                    item {
                        Column(Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                            Text(
                                text = if (showKorean) translatedHeader ?: text else text,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (showKorean && translatedHeader != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider()
                        }
                    }

                    when {
                        state.errorMessage != null && state.items.isEmpty() -> item {
                            MessageView(
                                message = state.errorMessage!!,
                                actionLabel = "다시 시도",
                                onAction = { viewModel.load() }
                            )
                        }

                        state.items.isEmpty() -> item {
                            MessageView(message = "아직 댓글이 없어요.")
                        }

                        else -> items(state.items, key = { it.id }) { item ->
                            CommentItemRow(
                                comment = item,
                                translatedText = if (showKorean) state.translations[item.id] else null,
                            ) {
                                onNavigateToComment(item.text, item.kids)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItemRow(
    comment: NewsItem,
    translatedText: String?,
    navigateToReplies: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp)) {
            // 작성자 · 시간
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(comment.by, size = 28.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = comment.by,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(8.dp))
                MetaInfo(R.drawable.ic_schedule, relativeTime(comment.time))
            }
            Spacer(Modifier.height(10.dp))

            // 본문 (길게 눌러 복사 가능)
            SelectionContainer {
                Column {
                    Text(
                        text = (translatedText ?: comment.text).trim(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (translatedText != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = comment.text.trim(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // 답글이 있을 때만 이동 버튼 표시
            if (comment.kids.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = navigateToReplies) {
                        Icon(
                            painter = painterResource(R.drawable.ic_comment),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("답글 ${comment.kids.size}개 보기")
                    }
                }
            } else {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
