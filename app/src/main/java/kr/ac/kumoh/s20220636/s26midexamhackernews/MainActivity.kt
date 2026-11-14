package kr.ac.kumoh.s20220636.s26midexamhackernews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kr.ac.kumoh.s20220636.s26midexamhackernews.navigation.NewsRoute
import kr.ac.kumoh.s20220636.s26midexamhackernews.repository.HackerNewsRepository
import kr.ac.kumoh.s20220636.s26midexamhackernews.ui.screen.CommentListScreen
import kr.ac.kumoh.s20220636.s26midexamhackernews.ui.screen.NewsListScreen
import kr.ac.kumoh.s20220636.s26midexamhackernews.ui.theme.S26MidExamHackerNewsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            S26MidExamHackerNewsTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val repository = remember { HackerNewsRepository() }
    val backStack = rememberNavBackStack(NewsRoute.TopNews)

    // 한국어 번역 보기 여부 (모든 화면에서 공유, 화면 회전에도 유지)
    var showKorean by rememberSaveable { mutableStateOf(false) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<NewsRoute.TopNews> {
                NewsListScreen(
                    repository = repository,
                    showKorean = showKorean,
                    onShowKoreanChange = { showKorean = it },
                    onNavigateToComment = { title, kids ->
                        backStack.add(NewsRoute.NewsComment(title, kids))
                    }
                )
            }
            entry<NewsRoute.NewsComment> { route ->
                CommentListScreen(
                    text = route.text,
                    kids = route.kids,
                    repository = repository,
                    showKorean = showKorean,
                    onShowKoreanChange = { showKorean = it },
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToComment = { text, kids ->
                        backStack.add(NewsRoute.NewsComment(text, kids))
                    }
                )
            }
        }
    )
}
