package kr.ac.kumoh.s20220636.s26midexamhackernews.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kr.ac.kumoh.s20220636.s26midexamhackernews.R

/**
 * 공통 상단 바: 제목 + (뒤로가기) + 한국어 번역 토글
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsTopBar(
    title: String,
    showKorean: Boolean,
    isTranslating: Boolean,
    onToggleKorean: () -> Unit,
    onBack: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = {
            Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = "뒤로 가기"
                    )
                }
            }
        },
        actions = {
            FilterChip(
                selected = showKorean,
                onClick = onToggleKorean,
                label = { Text(if (showKorean) "한국어" else "번역") },
                leadingIcon = {
                    if (isTranslating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_translate),
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                },
                modifier = Modifier.padding(end = 8.dp)
            )
        },
        scrollBehavior = scrollBehavior,
    )
}

/** 로딩 / 에러 / 빈 화면 */
@Composable
fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun MessageView(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.size(16.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/** 아이콘 + 텍스트 형태의 메타 정보 (추천수, 댓글수, 시간 등) */
@Composable
fun MetaInfo(
    @DrawableRes icon: Int,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun Avatar(name: String, size: Dp = 28.dp) {
    AsyncImage(
        model = "https://i.pravatar.cc/150?u=$name",
        contentDescription = "$name 프로필 이미지",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
    )
}

/** Unix 시간(초) → "3시간 전" 형태 */
fun relativeTime(epochSeconds: Long): String {
    val diff = System.currentTimeMillis() / 1000 - epochSeconds
    return when {
        diff < 60 -> "방금 전"
        diff < 3600 -> "${diff / 60}분 전"
        diff < 86400 -> "${diff / 3600}시간 전"
        diff < 86400 * 30 -> "${diff / 86400}일 전"
        diff < 86400 * 365 -> "${diff / (86400 * 30)}개월 전"
        else -> "${diff / (86400 * 365)}년 전"
    }
}

/** "https://www.example.com/abc" → "example.com" */
fun domainOf(url: String): String =
    url.substringAfter("://")
        .substringBefore("/")
        .removePrefix("www.")
