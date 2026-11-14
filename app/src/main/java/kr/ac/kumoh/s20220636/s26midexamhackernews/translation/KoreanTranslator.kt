package kr.ac.kumoh.s20220636.s26midexamhackernews.translation

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 영어 → 한국어 번역기
 *
 * Google ML Kit 온디바이스 번역을 사용하므로 API 키가 필요 없고,
 * 최초 1회 번역 모델(약 30MB)을 내려받은 뒤에는 오프라인에서도 동작한다.
 */
interface TextTranslator {
    suspend fun translate(text: String): String
}

object KoreanTranslator : TextTranslator {

    private val translator by lazy {
        Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.KOREAN)
                .build()
        )
    }

    // 같은 문장을 반복 번역하지 않도록 메모리 캐시
    private val cache = ConcurrentHashMap<String, String>()

    private val modelMutex = Mutex()
    private var modelReady = false

    private suspend fun ensureModel() = modelMutex.withLock {
        if (!modelReady) {
            translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
            modelReady = true
        }
    }

    override suspend fun translate(text: String): String {
        if (text.isBlank()) return text
        cache[text]?.let { return it }

        ensureModel()
        val result = translator.translate(text).await()
        cache[text] = result
        return result
    }
}

// Google Play Services Task → 코루틴 suspend 함수 변환
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}
