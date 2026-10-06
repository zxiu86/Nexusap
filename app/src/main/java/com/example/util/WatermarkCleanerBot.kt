package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Log
import com.example.data.model.Chapter
import com.example.data.model.ChapterCoordinatesDto
import com.example.data.model.NormalizedBoundingBox
import com.example.data.model.PageWatermarkData
import com.example.data.network.GitHubNetworkModule
import com.example.data.repository.CoordinatesRepository
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
import java.util.concurrent.ConcurrentHashMap

/**
 * High-Speed Precision Watermark Cleaner Engine (Native C++-Style Optimization)
 *
 * Characteristics:
 * 1. Directly tied to the active user: Cleans only the opened chapter silently in the background.
 * 2. Smart Skip: If chapter is already cleaned (locally or on GitHub Coordinates repo), cleaner is NOT triggered.
 * 3. High Precision White Box Overlay: Tightly covers ONLY target keywords without clipping manga artwork.
 * 4. Target words: "موقع تيمكس", "تيم اكس", "تيمكس", "olympustaff.com", "https//:olympustaff.com", etc.
 * 5. GitHub Batch Protection: Coordinates are enqueued and uploaded in batches of 30 chapters per single commit.
 */
object WatermarkCleanerBot {

    private const val TAG = "PrecisionCleanerEngine"
    private val botScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val processingMutex = Mutex()
    private val activeChapterCleaning = ConcurrentHashMap<String, Boolean>()

    val TARGET_KEYWORDS = listOf(
        "موقع تيمكس",
        "تيم اكس",
        "تيمكس",
        "olympustaff.com",
        "https//:olympustaff.com",
        "https://olympustaff.com",
        "olympustaff",
        "olympus",
        "olympus-scans",
        "olympusscan",
        "olympus staff",
        "olympustaf",
        "teamx",
        "team-x",
        "timx",
        "tim-x"
    )

    /**
     * Cleans the active chapter currently opened by the user.
     * Runs completely in the background without disturbing the user or hitching the UI.
     */
    fun cleanChapterSilently(
        context: Context,
        seriesSlug: String,
        chapter: Chapter,
        onCleaned: (ChapterCoordinatesDto) -> Unit = {}
    ) {
        val chapterKey = "${seriesSlug}_${chapter.number}"
        if (activeChapterCleaning.putIfAbsent(chapterKey, true) == true) {
            // Already actively cleaning this chapter
            return
        }

        botScope.launch {
            try {
                val coordsRepo = CoordinatesRepository.getInstance(context)

                // 1. Check if chapter is already cleaned
                val existing = coordsRepo.getCoordinates(seriesSlug, chapter.number)
                if (existing != null) {
                    Log.d(TAG, "Chapter $chapterKey is already cleaned. Skipping cleaning engine.")
                    withContext(Dispatchers.Main) { onCleaned(existing) }
                    return@launch
                }

                // 2. Process chapter with precision high-speed engine
                val cleanedDto = processChapterPrecision(context, coordsRepo, seriesSlug, chapter)
                if (cleanedDto != null) {
                    withContext(Dispatchers.Main) { onCleaned(cleanedDto) }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Silent cleaning error for $chapterKey: ${e.message}")
            } finally {
                activeChapterCleaning.remove(chapterKey)
            }
        }
    }

    /**
     * Precision chapter cleaner with fast zero-alloc memory management.
     */
    private suspend fun processChapterPrecision(
        context: Context,
        coordsRepo: CoordinatesRepository,
        seriesSlug: String,
        chapter: Chapter
    ): ChapterCoordinatesDto? = processingMutex.withLock {
        val pages = chapter.pages
        if (pages.isEmpty()) return null

        Log.d(TAG, "Starting precision watermark cleaning for $seriesSlug chapter ${chapter.number} (${pages.size} pages)...")

        val pageResults = mutableListOf<PageWatermarkData>()
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        try {
            for ((index, page) in pages.withIndex()) {
                val pNum = page.pageNumber.takeIf { it > 0 } ?: (index + 1)
                val imageUrl = page.imageUrl
                if (imageUrl.isNullOrBlank()) continue

                val boxes = analyzeImageForWatermarks(imageUrl, recognizer)
                if (boxes.isNotEmpty()) {
                    pageResults.add(
                        PageWatermarkData(
                            pageNumber = pNum,
                            boxes = boxes,
                            originalText = "olympustaff.com / تيمكس",
                            replacementText = "" // Pure clean white overlay
                        )
                    )
                }
                // Gentle cooperative yield to guarantee smooth 120Hz reader scrolling
                delay(30L)
            }
        } finally {
            try {
                recognizer.close()
            } catch (_: Exception) {}
        }

        val dto = ChapterCoordinatesDto(
            seriesSlug = seriesSlug,
            chapterNumber = chapter.number,
            totalPages = pages.size,
            pages = pageResults,
            version = 1,
            updatedAt = System.currentTimeMillis()
        )

        // 1. Immediately cache locally so this user and local readers see the white layer right away
        coordsRepo.saveToCache(dto)

        // 2. Enqueue for batch commit to GitHub (zxiu86/Coordinates) every 30 chapters in 1 commit
        if (dto.hasWatermarks()) {
            coordsRepo.enqueueCleanedChapter(dto)
        }

        Log.d(TAG, "Finished cleaning chapter ${chapter.number} of $seriesSlug. Found watermarks on ${pageResults.size} pages.")
        return dto
    }

    /**
     * Native-speed image analysis:
     * - Decodes image directly into compact RGB_565 Bitmap with optimal sample size.
     * - Runs ML Kit text recognition.
     * - Tightly identifies watermark coordinates with word-level precision.
     * - Immediately recycles Bitmap memory.
     */
    private suspend fun analyzeImageForWatermarks(
        imageUrl: String,
        recognizer: com.google.mlkit.vision.text.TextRecognizer
    ): List<NormalizedBoundingBox> = withContext(Dispatchers.IO) {
        var bitmap: Bitmap? = null
        try {
            bitmap = downloadScaledBitmap(imageUrl, maxDimension = 1400)
            if (bitmap == null) return@withContext emptyList()

            val imgWidth = bitmap.width.toFloat()
            val imgHeight = bitmap.height.toFloat()
            if (imgWidth <= 0 || imgHeight <= 0) return@withContext emptyList()

            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val visionText = recognizer.process(inputImage).await()

            val matchedBoxes = mutableListOf<NormalizedBoundingBox>()

            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    val lineText = line.text
                    val lineNormalized = normalizeSearchString(lineText)

                    val isLineMatch = TARGET_KEYWORDS.any { kw ->
                        val cleanKw = normalizeSearchString(kw)
                        lineNormalized.contains(cleanKw) || (cleanKw.contains(lineNormalized) && lineNormalized.length >= 5)
                    }

                    if (isLineMatch) {
                        // Word-level precision: check if specific element(s) contain the watermark
                        val matchingElements = line.elements.filter { element ->
                            val elemNorm = normalizeSearchString(element.text)
                            TARGET_KEYWORDS.any { kw ->
                                val cleanKw = normalizeSearchString(kw)
                                elemNorm.contains(cleanKw) || cleanKw.contains(elemNorm)
                            }
                        }

                        if (matchingElements.isNotEmpty()) {
                            // Compute ultra-tight bounding box strictly enclosing the matching words
                            var minLeft = Float.MAX_VALUE
                            var minTop = Float.MAX_VALUE
                            var maxRight = Float.MIN_VALUE
                            var maxBottom = Float.MIN_VALUE

                            for (el in matchingElements) {
                                val r = el.boundingBox ?: continue
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

                        // Fallback to line box with tight padding
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

    private fun normalizeSearchString(input: String): String {
        return input.lowercase()
            .replace(" ", "")
            .replace("-", "")
            .replace("_", "")
            .replace("/", "")
            .replace(":", "")
            .replace(".", "")
            .replace("https", "")
            .replace("http", "")
    }

    /**
     * Ultra-precise normalized bounding box:
     * Tightly pads by only 2-3% (2-4px max) to prevent deleting any manga drawing or dialogue.
     */
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
     * Efficient bitmap decode with zero memory overhead using RGB_565
     */
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
            Log.w(TAG, "Bitmap download decode error: ${e.message}")
            null
        }
    }
}
