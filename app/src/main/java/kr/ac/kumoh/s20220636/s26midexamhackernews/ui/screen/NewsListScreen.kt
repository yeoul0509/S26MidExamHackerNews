package kr.ac.kumoh.s20220636.s26midexamhackernews.ui.screen

import android.content.Intent
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
import kr.ac.kumoh.s20220636.s26midexamhackernews.ui.component.domainOf
import kr.ac.kumoh.s20220636.s26midexamhackernews.ui.component.relativeTime
import kr.ac.kumoh.s20220636.s26midexamhackernews.viewmodel.TopNewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListScreen(
    repository: NewsRepository,
    showKorean: Boolean,
    onShowKoreanChange: (Boolean) -> Unit,
    viewModel: TopNewsViewModel = viewModel {
        TopNewsViewModel(repository)
    },
    onNavigateToComment: (String, List<Long>) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // 한국어 모드가 켜져 있으면 새로 불러온 글까지 번역
    LaunchedEffect(showKorean, state.items) {
        if (showKorean) viewModel.translateMissing()
    }
    // 번역 실패 시 안내 후 원문 모드로 되돌림
    LaunchedEffect(state.translateError) {
        state.translateError?.let {
            onShowKoreanChange(false)
            snackbarHostState.showSnackbar(it)
            viewModel.clearTranslateError()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            NewsTopBar(
                title = "Hacker News 인기글",
                showKorean = showKorean,
                isTranslating = state.isTranslating,
                onToggleKorean = { onShowKoreanChange(!showKorean) },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            when {
                state.isLoading -> LoadingView()

                state.errorMessage != null && state.items.isEmpty() -> MessageView(
                    message = state.errorMessage!!,
                    actionLabel = "다시 시도",
                    onAction = { viewModel.load() }
                )

                else -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.load(refresh = true) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(state.items, key = { _, item -> item.id }) { index, item ->
                            val translated = state.translations[item.id]
                            StoryItemRow(
                                rank = index + 1,
                                story = item,
                                translatedTitle = if (showKorean) translated else null,
                            ) {
                                onNavigateToComment(item.title, item.kids)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoryItemRow(
    rank: Int,
    story: NewsItem,
    translatedTitle: String?,
    navigateToComment: () -> Unit,
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = navigateToComment,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // 순위
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .padding(top = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.width(8.dp))

                Column(Modifier.weight(1f)) {
                    // 글 제목 (번역문이 있으면 번역문을 크게, 원문은 작게)
                    Text(
                        text = translatedTitle ?: story.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (translatedTitle != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = story.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // 외부 링크 (도메인만 깔끔하게 표시)
                    if (story.url.isNotBlank()) {
                        AssistChip(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, story.url.toUri()))
                            },
                            label = {
                                Text(
                                    text = domainOf(story.url),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_open_in_new),
                                    contentDescription = "브라우저에서 열기",
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                            },
                            shape = CircleShape,
                        )
                    } else {
                        Spacer(Modifier.height(8.dp))
                    }

                    // 하단 메타 정보: 작성자 · 시간 / 추천수 · 댓글수
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(story.by, size = 20.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = story.by,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(Modifier.width(8.dp))
                        MetaInfo(R.drawable.ic_schedule, relativeTime(story.time))
                        Spacer(Modifier.weight(1f))
                        MetaInfo(R.drawable.ic_arrow_upward, "${story.score}")
                        Spacer(Modifier.width(12.dp))
                        MetaInfo(R.drawable.ic_comment, "${story.descendants}")
                    }
                }
            }
        }
    }
}
