package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.data.model.Chapter
import com.example.data.model.ChapterCoordinatesDto
import com.example.data.model.NormalizedBoundingBox
import com.example.data.model.PageWatermarkData
import com.example.data.network.GitHubNetworkModule
import com.example.data.repository.CoordinatesRepository
import com.example.data.settings.AppSettingsManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Ultra-Fast Precision Watermark Cleaning Engine
 *
 * Capabilities:
 * 1. Directly tied to the active user opening a chapter (silent background execution).
 * 2. Instant memory & Coil cache reuse: Obtains software bitmaps directly without double-downloading.
 * 3. Progressive Real-Time Application: Coordinates update page-by-page as they are scanned.
 * 4. High-Precision White Layer: Targets strictly keywords and isolates matching element words.
 * 5. Secret Background Counter: Tracks total cleaned watermarks for cosmic aura unlocking.
 * 6. GitHub Batch Protection: Gathers 30 chapters before pushing 1 single commit.
 */
object WatermarkCleanerBot {

    private const val TAG = "PrecisionCleanerBot"
    private val botScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val processingMutex = Mutex()
    private val activeChapterCleaning = ConcurrentHashMap<String, Boolean>()

    val TARGET_KEYWORDS = listOf(
        "olympustaff.com",
        "https//:olympustaff.com",
        "https://olympustaff.com",
        "http://olympustaff.com",
        "olympustaff",
        "olympus",
        "olympus-scans",
        "olympusscan",
        "olympus staff",
        "olympustaf",
        "موقع تيمكس",
        "تيم اكس",
        "تيمكس",
        "موقع",
        "تيم",
        "اكس",
        "teamx",
        "team-x",
        "timx",
        "tim-x",
        "team x",
        "teamxnovel",
        "teamx.top",
        "teamx.org",
        "teamx.net"
    )

    /**
     * Cleans the active chapter currently opened by the user in the background.
     * Progressively calls onPageCleaned as each page finishes so the reader displays white overlays right away!
     */
    fun cleanChapterSilently(
        context: Context,
        seriesSlug: String,
        chapter: Chapter,
        onPageCleaned: (PageWatermarkData) -> Unit = {},
        onAllFinished: (ChapterCoordinatesDto) -> Unit = {}
    ) {
        val chapterKey = "${seriesSlug}_${chapter.number}"
        if (activeChapterCleaning.putIfAbsent(chapterKey, true) == true) {
            return // Already actively running for this chapter
        }

        botScope.launch {
            try {
                val coordsRepo = CoordinatesRepository.getInstance(context)

                // 1. Check if chapter is already cleaned (Local cache or GitHub)
                val existing = coordsRepo.getCoordinates(seriesSlug, chapter.number)
                if (existing != null) {
                    Log.d(TAG, "Chapter $chapterKey is already cleaned. Skipping cleaner.")
                    withContext(Dispatchers.Main) {
                        onAllFinished(existing)
                    }
                    return@launch
                }

                // 2. Run progressive precision cleaner
                val cleanedDto = processChapterProgressive(
                    context = context,
                    coordsRepo = coordsRepo,
                    seriesSlug = seriesSlug,
                    chapter = chapter,
                    onPageCleaned = onPageCleaned
                )

                if (cleanedDto != null) {
                    withContext(Dispatchers.Main) {
                        onAllFinished(cleanedDto)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Silent cleaning error for $chapterKey: ${e.message}")
            } finally {
                activeChapterCleaning.remove(chapterKey)
            }
        }
    }

    private suspend fun processChapterProgressive(
        context: Context,
        coordsRepo: CoordinatesRepository,
        seriesSlug: String,
        chapter: Chapter,
        onPageCleaned: (PageWatermarkData) -> Unit
    ): ChapterCoordinatesDto? = processingMutex.withLock {
        val pages = chapter.pages
        if (pages.isEmpty()) return null

        Log.d(TAG, "Starting precision watermark cleaner on $seriesSlug chapter ${chapter.number} (${pages.size} pages)...")

        val pageResults = mutableListOf<PageWatermarkData>()
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        var totalDetectedBoxes = 0

        try {
            for ((index, page) in pages.withIndex()) {
                val pNum = page.pageNumber.takeIf { it > 0 } ?: (index + 1)
                val imageUrl = page.imageUrl
                if (imageUrl.isNullOrBlank()) continue

                val boxes = analyzeImageForWatermarks(context, imageUrl, recognizer)
                if (boxes.isNotEmpty()) {
                    totalDetectedBoxes += boxes.size
                    val pageData = PageWatermarkData(
                        pageNumber = pNum,
                        boxes = boxes,
                        originalText = "olympustaff / teamx",
                        replacementText = "" // Pure white layer
                    )
                    pageResults.add(pageData)

                    // 🎯 Immediately merge and notify progressive update
                    coordsRepo.mergePageCoordinates(seriesSlug, chapter.number, pages.size, pageData)
                    withContext(Dispatchers.Main) {
                        onPageCleaned(pageData)
                    }
                }
                // Gentle yield to keep 120Hz scrolling butter-smooth
                delay(15L)
            }
        } finally {
            try {
                recognizer.close()
            } catch (_: Exception) {}
        }

        // Secretly increment total cleaned counter for the cosmic aura threshold
        if (totalDetectedBoxes > 0) {
            AppSettingsManager.getInstance(context).incrementCleanedWatermarksCount(totalDetectedBoxes)
        }

        val dto = ChapterCoordinatesDto(
            seriesSlug = seriesSlug,
            chapterNumber = chapter.number,
            totalPages = pages.size,
            pages = pageResults,
            version = 1,
            updatedAt = System.currentTimeMillis()
        )

        // Save locally to cache immediately
        coordsRepo.saveToCache(dto)

        // Enqueue to batch commit queue (commits every 30 chapters in 1 commit)
        if (dto.hasWatermarks()) {
            coordsRepo.enqueueCleanedChapter(dto)
        }

        Log.d(TAG, "Finished cleaning chapter ${chapter.number} of $seriesSlug. Found watermarks on ${pageResults.size} pages ($totalDetectedBoxes total boxes).")
        return dto
    }

    /**
     * Analyzes image with high accuracy:
     * - Reuses Coil's downloaded bitmap or decodes locally with zero hardware lock.
     * - Performs word-level element precision bounding box extraction.
     * - Disposes and cleans memory immediately.
     */
    private suspend fun analyzeImageForWatermarks(
        context: Context,
        imageUrl: String,
        recognizer: com.google.mlkit.vision.text.TextRecognizer
    ): List<NormalizedBoundingBox> = withContext(Dispatchers.IO) {
        var bitmap: Bitmap? = null
        try {
            bitmap = obtainPageBitmap(context, imageUrl)
            if (bitmap == null) return@withContext emptyList()

            val imgWidth = bitmap.width.toFloat()
            val imgHeight = bitmap.height.toFloat()
            if (imgWidth <= 0 || imgHeight <= 0) return@withContext emptyList()

            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val visionText = recognizer.process(inputImage).await()

            val matchedBoxes = mutableListOf<NormalizedBoundingBox>()

            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    val lineRaw = line.text
                    val lineClean = normalizeSearchString(lineRaw)

                    val isLineMatch = isTextMatchingWatermark(lineRaw, lineClean)

                    if (isLineMatch) {
                        // 1. If line is concise and dedicated to the watermark banner/URL/badge (<= 60 chars)
                        val lineRect = line.boundingBox
                        if (lineRaw.length <= 60 && lineRect != null) {
                            val tightBox = normalizeAndPadRect(
                                left = lineRect.left.toFloat(),
                                top = lineRect.top.toFloat(),
                                right = lineRect.right.toFloat(),
                                bottom = lineRect.bottom.toFloat(),
                                imgWidth = imgWidth,
                                imgHeight = imgHeight
                            )
                            matchedBoxes.add(tightBox)
                            continue
                        }

                        // 2. Otherwise for longer mixed lines: find span from first matching element to last matching element
                        val elements = line.elements
                        val matchingIndices = elements.mapIndexedNotNull { idx, el ->
                            val elemRaw = el.text
                            val elemClean = normalizeSearchString(elemRaw)
                            if (isTextMatchingWatermark(elemRaw, elemClean)) idx else null
                        }

                        if (matchingIndices.isNotEmpty()) {
                            val firstIdx = matchingIndices.first()
                            val lastIdx = matchingIndices.last()

                            var minLeft = Float.MAX_VALUE
                            var minTop = Float.MAX_VALUE
                            var maxRight = Float.MIN_VALUE
                            var maxBottom = Float.MIN_VALUE

                            for (i in firstIdx..lastIdx) {
                                val r = elements[i].boundingBox ?: continue
                                if (r.left < minLeft) minLeft = r.left.toFloat()
                                if (r.top < minTop) minTop = r.top.toFloat()
                                if (r.right > maxRight) maxRight = r.right.toFloat()
                                if (r.bottom > maxBottom) maxBottom = r.bottom.toFloat()
                            }

                            if (minLeft < maxRight && minTop < maxBottom) {
                                val tightBox = normalizeAndPadRect(
                                    left = minLeft,
                                    top = minTop,
                                    right = maxRight,
                                    bottom = maxBottom,
                                    imgWidth = imgWidth,
                                    imgHeight = imgHeight
                                )
                                matchedBoxes.add(tightBox)
                                continue
                            }
                        }

                        // Fallback to line bounding box
                        val rect = line.boundingBox ?: block.boundingBox
                        if (rect != null) {
                            val tightBox = normalizeAndPadRect(
                                left = rect.left.toFloat(),
                                top = rect.top.toFloat(),
                                right = rect.right.toFloat(),
                                bottom = rect.bottom.toFloat(),
                                imgWidth = imgWidth,
                                imgHeight = imgHeight
                            )
                            matchedBoxes.add(tightBox)
                        }
                    }
                }
            }

            matchedBoxes
        } catch (e: Exception) {
            Log.w(TAG, "Detection error for $imageUrl: ${e.message}")
            emptyList()
        } finally {
            bitmap?.recycle()
        }
    }

    private fun isTextMatchingWatermark(rawText: String, cleanText: String): Boolean {
        val normAr = normalizeArabic(rawText).lowercase()

        // 1. Direct Arabic checks with normalization
        if (normAr.contains("تيمكس") || normAr.contains("تيم اكس") || normAr.contains("موقع تيمكس") || normAr.contains("موقع تيم اكس")) {
            return true
        }
        if (normAr.contains("تيم") && normAr.contains("اكس")) {
            return true
        }

        // 2. Keyword normalization checks
        for (kw in TARGET_KEYWORDS) {
            val kwClean = normalizeSearchString(kw)
            if (cleanText.contains(kwClean)) return true
            if (kwClean.contains(cleanText) && cleanText.length >= 4) return true
        }

        // 3. Brand specifics & fuzzy handles for stylized fonts
        if (cleanText.contains("olympustaff") || cleanText.contains("olympus") || cleanText.contains("olympusta") ||
            cleanText.contains("olympust") || cleanText.contains("lympustaff") || cleanText.contains("staffcom")
        ) {
            return true
        }
        if (cleanText.contains("teamx") || cleanText.contains("timx") || cleanText.contains("teanx") ||
            cleanText.contains("team-x") || cleanText.contains("teamxnovel")
        ) {
            return true
        }

        return false
    }

    private fun normalizeArabic(input: String): String {
        return input
            .replace(Regex("[\\u064B-\\u065F\\u0670]"), "") // remove tashkeel
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ة', 'ه')
            .replace('ى', 'ي')
    }

    private fun normalizeSearchString(input: String): String {
        return normalizeArabic(input).lowercase()
            .replace(" ", "")
            .replace("-", "")
            .replace("_", "")
            .replace("/", "")
            .replace("\\", "")
            .replace(":", "")
            .replace(".", "")
            .replace("https", "")
            .replace("http", "")
    }

    private fun normalizeAndPadRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        imgWidth: Float,
        imgHeight: Float
    ): NormalizedBoundingBox {
        val width = right - left
        val height = bottom - top

        val padX = (width * 0.03f).coerceIn(2f, 5f)
        val padY = (height * 0.04f).coerceIn(2f, 4f)

        val cleanLeft = (left - padX).coerceAtLeast(0f)
        val cleanTop = (top - padY).coerceAtLeast(0f)
        val cleanRight = (right + padX).coerceAtMost(imgWidth)
        val cleanBottom = (bottom + padY).coerceAtMost(imgHeight)

        val normX = cleanLeft / imgWidth
        val normY = cleanTop / imgHeight
        val normW = (cleanRight - cleanLeft) / imgWidth
        val normH = (cleanBottom - cleanTop) / imgHeight

        return NormalizedBoundingBox(
            x = normX.coerceIn(0f, 1f),
            y = normY.coerceIn(0f, 1f),
            width = normW.coerceIn(0.005f, 1f),
            height = normH.coerceIn(0.005f, 1f)
        )
    }

    /**
     * Obtains a software Bitmap with zero double-downloading:
     * 1. Reuses Coil memory / disk cache if already loaded or loading.
     * 2. Checks local filesystem if offline file.
     * 3. Fallbacks to direct scaled HTTP decode if needed.
     */
    private suspend fun obtainPageBitmap(context: Context, imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        if (imageUrl.isBlank()) return@withContext null

        // Method 1: Local file check
        if (imageUrl.startsWith("/") || imageUrl.startsWith("file:")) {
            try {
                val path = imageUrl.removePrefix("file://")
                val file = File(path)
                if (file.exists() && file.length() > 0) {
                    val options = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.RGB_565
                        inSampleSize = 2
                    }
                    val bmp = BitmapFactory.decodeFile(file.absolutePath, options)
                    if (bmp != null) return@withContext bmp
                }
            } catch (_: Exception) {}
        }

        // Method 2: Coil Cache / Loader
        try {
            val loader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Must be Software Bitmap for ML Kit
                .size(1200, 1800)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val drawable = result.drawable
                if (drawable is BitmapDrawable && drawable.bitmap != null) {
                    return@withContext drawable.bitmap.copy(Bitmap.Config.RGB_565, false)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Coil decode fallback for $imageUrl: ${e.message}")
        }

        // Method 3: Fallback direct network decode
        downloadScaledBitmap(imageUrl, maxDimension = 1200)
    }

    private fun downloadScaledBitmap(imageUrl: String, maxDimension: Int): Bitmap? {
        return try {
            val request = Request.Builder()
                .url(imageUrl)
                .header("User-Agent", "Nexus-Manga-App-Android/${AppVersionConfig.VERSION_NAME}")
                .build()

            val response = GitHubNetworkModule.okHttpClient.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) return null

            val bytes = response.body!!.bytes()
            if (bytes.isEmpty()) return null

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

            val origWidth = options.outWidth
            val origHeight = options.outHeight
            if (origWidth <= 0 || origHeight <= 0) return null

            var sampleSize = 1
            while ((origWidth / sampleSize) > maxDimension || (origHeight / sampleSize) > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
                inMutable = false
            }

            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
        } catch (e: Exception) {
            Log.w(TAG, "Direct download decode error: ${e.message}")
            null
        }
    }
}
