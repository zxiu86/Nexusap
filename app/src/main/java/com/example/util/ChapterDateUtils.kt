package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap

/**
 * High-Performance Utility to determine if a chapter is considered "New".
 * Optimized with fast-path string checks and cached date formatters to avoid UI thread lag.
 */
object ChapterDateUtils {

    private const val THREE_DAYS_MILLIS = 3 * 24 * 60 * 60 * 1000L

    private val DATE_PATTERNS = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
        "yyyy/MM/dd",
        "dd-MM-yyyy",
        "dd/MM/yyyy",
        "d MMMM yyyy",
        "MMMM d, yyyy"
    )

    // Thread-safe formatters cache using ThreadLocal
    private val threadLocalFormatters = ThreadLocal.withInitial {
        DATE_PATTERNS.map { pattern ->
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = true
            }
        }
    }

    private val resultCache = ConcurrentHashMap<String, Boolean>(256)

    /**
     * Checks whether a chapter is within the 3-day "New" window.
     */
    fun isChapterNew(releaseDate: String?, isNewFlag: Boolean): Boolean {
        if (isNewFlag) return true
        val rawDate = releaseDate?.trim() ?: ""
        if (rawDate.isEmpty()) {
            return false
        }

        val cacheKey = "$rawDate#$isNewFlag"
        resultCache[cacheKey]?.let { return it }

        val res = evaluateIsNew(rawDate, isNewFlag)
        if (resultCache.size > 500) {
            resultCache.clear()
        }
        resultCache[cacheKey] = res
        return res
    }

    private fun evaluateIsNew(rawDate: String, isNewFlag: Boolean): Boolean {
        val normalized = rawDate.lowercase(Locale.ROOT)

        // If closed or maintenance, not new
        if (normalized.contains("صيانة") || normalized.contains("maintenance") || normalized.contains("مغلق")) {
            return false
        }

        // Fast-path Arabic/English relative time checks (Zero allocation)
        if (normalized.contains("اليوم") || normalized.contains("today") ||
            normalized.contains("ساعة") || normalized.contains("ساعات") ||
            normalized.contains("دقيقة") || normalized.contains("دقائق") ||
            normalized.contains("hour") || normalized.contains("minute") ||
            normalized.contains("now") || normalized.contains("الان") || normalized.contains("الآن")
        ) {
            return true
        }

        if (normalized.contains("أمس") || normalized.contains("امس") || normalized.contains("yesterday") ||
            normalized.contains("يوم") || normalized.contains("يومين") || normalized.contains("يومان")
        ) {
            return true
        }

        if (normalized.contains("3 أيام") || normalized.contains("3 ايام") || normalized.contains("3 days") || normalized.contains("3d")) {
            return true
        }

        // Check if explicitly 4+ days, weeks, months, years -> definitely NOT new
        if (normalized.contains("أسبوع") || normalized.contains("اسبوع") || normalized.contains("week") ||
            normalized.contains("شهر") || normalized.contains("month") ||
            normalized.contains("سنة") || normalized.contains("عام") || normalized.contains("year") ||
            normalized.contains("4 أيام") || normalized.contains("5 أيام") || normalized.contains("6 أيام") ||
            normalized.contains("4 ايام") || normalized.contains("5 ايام") || normalized.contains("6 ايام")
        ) {
            return false
        }

        val dayMatch = Regex("""(?:منذ\s*)?(\d+)\s*(?:أيام|ايام|يوم|days?|d)""").find(normalized)
        if (dayMatch != null) {
            val days = dayMatch.groupValues[1].toIntOrNull()
            if (days != null) {
                return days <= 3
            }
        }

        // Standard date parsing using cached thread-local formatters
        val now = System.currentTimeMillis()
        val formatters = threadLocalFormatters.get()
        if (formatters != null) {
            for (sdf in formatters) {
                try {
                    val parsedDate = sdf.parse(rawDate)
                    if (parsedDate != null) {
                        val diff = now - parsedDate.time
                        return diff in 0..THREE_DAYS_MILLIS
                    }
                } catch (_: Exception) {}
            }
        }

        return isNewFlag
    }
}

