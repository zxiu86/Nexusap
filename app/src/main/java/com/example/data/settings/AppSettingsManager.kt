package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences
import coil.Coil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.DecimalFormat

data class AppSettings(
    val readerMode: Int = 0, // 0: Webtoon Vertical, 1: Horizontal RTL, 2: Horizontal LTR
    val imageQuality: Int = 0, // 0: HD, 1: Balanced, 2: Data Saver
    val keepScreenOn: Boolean = true,
    val wifiOnlyDownloads: Boolean = false,
    val autoSyncUpdates: Boolean = true,
    val themeMode: Int = 0, // 0: System, 1: Dark, 2: Light
    val backgroundStyle: Int = 0, // 0: Default, 1: AMOLED Pure Black, 2: Pure White
    val accentColor: Int = 0, // 0..7: Solid, 10..15: Multi-Color Gradients
    val preventChapterCache: Boolean = true, // Don't persist chapter images in disk cache, auto-clear on exit
    val cardAnimationEnabled: Boolean = true, // Enable animated gradient aura for the top 2 newest works in multi-color themes
    val cosmicSpaceFooterEnabled: Boolean = true, // Legendary Cosmic Space Footer Aura (unlocked at 500 chapters or admin)
    val footerWaveSpeed: Int = 1, // 0: هادئ (2200ms), 1: متوازن (1200ms), 2: سريع (800ms), 3: فائق (500ms)
    val footerWaveInterval: Int = 2, // 0: مستمر (600ms), 1: متكرر (1500ms), 2: متوازن (3000ms), 3: متباعد (5000ms)
    val footerWaveColor: Int = 0 // 0: حسب سمة التطبيق, 1: أبيض كريستالي أنيق, 2: أورورا نيون, 3: شفق ذهبي ملكي, 4: بنفسجي كوني, 5: زمردي نيون, 6: وردي أزهار الكرز, 7: طيف نيون متعدد
)

class AppSettingsManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("nexus_app_settings", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            readerMode = prefs.getInt(KEY_READER_MODE, 0),
            imageQuality = prefs.getInt(KEY_IMAGE_QUALITY, 0),
            keepScreenOn = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true),
            wifiOnlyDownloads = prefs.getBoolean(KEY_WIFI_ONLY, false),
            autoSyncUpdates = prefs.getBoolean(KEY_AUTO_SYNC, true),
            themeMode = prefs.getInt(KEY_THEME_MODE, 0),
            backgroundStyle = prefs.getInt(KEY_BACKGROUND_STYLE, 0),
            accentColor = prefs.getInt(KEY_ACCENT_COLOR, 0),
            preventChapterCache = prefs.getBoolean(KEY_PREVENT_CHAPTER_CACHE, true),
            cardAnimationEnabled = prefs.getBoolean(KEY_CARD_ANIMATION_ENABLED, true),
            cosmicSpaceFooterEnabled = prefs.getBoolean(KEY_COSMIC_SPACE_FOOTER_ENABLED, prefs.getBoolean("pref_cosmic_space_header_enabled", true)),
            footerWaveSpeed = prefs.getInt(KEY_FOOTER_WAVE_SPEED, 1),
            footerWaveInterval = prefs.getInt(KEY_FOOTER_WAVE_INTERVAL, 2),
            footerWaveColor = prefs.getInt(KEY_FOOTER_WAVE_COLOR, 0)
        )
    }

    fun updateReaderMode(mode: Int) {
        prefs.edit().putInt(KEY_READER_MODE, mode).apply()
        _settingsFlow.value = _settingsFlow.value.copy(readerMode = mode)
    }

    fun updateImageQuality(quality: Int) {
        prefs.edit().putInt(KEY_IMAGE_QUALITY, quality).apply()
        _settingsFlow.value = _settingsFlow.value.copy(imageQuality = quality)
    }

    fun updateKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(keepScreenOn = enabled)
    }

    fun updateWifiOnlyDownloads(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIFI_ONLY, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(wifiOnlyDownloads = enabled)
    }

    fun updateAutoSyncUpdates(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(autoSyncUpdates = enabled)
    }

    fun updateThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
        _settingsFlow.value = _settingsFlow.value.copy(themeMode = mode)
    }

    fun updateBackgroundStyle(style: Int) {
        prefs.edit().putInt(KEY_BACKGROUND_STYLE, style).apply()
        _settingsFlow.value = _settingsFlow.value.copy(backgroundStyle = style)
    }

    fun updateAccentColor(color: Int) {
        prefs.edit().putInt(KEY_ACCENT_COLOR, color).apply()
        _settingsFlow.value = _settingsFlow.value.copy(accentColor = color)
    }

    fun updatePreventChapterCache(prevent: Boolean) {
        prefs.edit().putBoolean(KEY_PREVENT_CHAPTER_CACHE, prevent).apply()
        _settingsFlow.value = _settingsFlow.value.copy(preventChapterCache = prevent)
    }

    fun updateCardAnimationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CARD_ANIMATION_ENABLED, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(cardAnimationEnabled = enabled)
    }

    fun updateCosmicSpaceFooterEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_COSMIC_SPACE_FOOTER_ENABLED, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(cosmicSpaceFooterEnabled = enabled)
    }

    fun updateFooterWaveSpeed(speed: Int) {
        prefs.edit().putInt(KEY_FOOTER_WAVE_SPEED, speed).apply()
        _settingsFlow.value = _settingsFlow.value.copy(footerWaveSpeed = speed)
    }

    fun updateFooterWaveInterval(interval: Int) {
        prefs.edit().putInt(KEY_FOOTER_WAVE_INTERVAL, interval).apply()
        _settingsFlow.value = _settingsFlow.value.copy(footerWaveInterval = interval)
    }

    fun updateFooterWaveColor(color: Int) {
        prefs.edit().putInt(KEY_FOOTER_WAVE_COLOR, color).apply()
        _settingsFlow.value = _settingsFlow.value.copy(footerWaveColor = color)
    }

    fun getCalculatedCacheSize(context: Context): String {
        val bytes = calculateDirSize(context.cacheDir) + calculateDirSize(context.externalCacheDir)
        val mb = bytes.toDouble() / (1024 * 1024)
        return if (mb < 0.1) "0.5 MB" else "${DecimalFormat("#.#").format(mb)} MB"
    }

    fun clearAllCache(context: Context): String {
        val beforeBytes = calculateDirSize(context.cacheDir) + calculateDirSize(context.externalCacheDir)
        try {
            context.cacheDir.deleteRecursively()
            context.cacheDir.mkdirs()
            context.externalCacheDir?.deleteRecursively()
            context.externalCacheDir?.mkdirs()
            Coil.imageLoader(context).memoryCache?.clear()
            Coil.imageLoader(context).diskCache?.clear()
        } catch (_: Exception) {}
        val mb = beforeBytes.toDouble() / (1024 * 1024)
        return if (mb < 0.1) "تم تنظيف الذاكرة المؤقتة بنجاح" else "تم تحرير ${DecimalFormat("#.#").format(mb)} MB بنجاح"
    }

    fun clearChapterImages(context: Context, urls: List<String>) {
        try {
            val loader = Coil.imageLoader(context)
            urls.forEach { url ->
                loader.memoryCache?.remove(coil.memory.MemoryCache.Key(url))
                loader.diskCache?.remove(url)
            }
        } catch (_: Exception) {}
    }

    private fun calculateDirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (f in files) {
            size += if (f.isDirectory) calculateDirSize(f) else f.length()
        }
        return size
    }

    companion object {
        private const val KEY_READER_MODE = "pref_reader_mode"
        private const val KEY_IMAGE_QUALITY = "pref_image_quality"
        private const val KEY_KEEP_SCREEN_ON = "pref_keep_screen_on"
        private const val KEY_WIFI_ONLY = "pref_wifi_only"
        private const val KEY_AUTO_SYNC = "pref_auto_sync"
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_BACKGROUND_STYLE = "pref_background_style"
        private const val KEY_ACCENT_COLOR = "pref_accent_color"
        private const val KEY_PREVENT_CHAPTER_CACHE = "pref_prevent_chapter_cache"
        private const val KEY_CARD_ANIMATION_ENABLED = "pref_card_animation_enabled"
        private const val KEY_COSMIC_SPACE_FOOTER_ENABLED = "pref_cosmic_space_footer_enabled"
        private const val KEY_FOOTER_WAVE_SPEED = "pref_footer_wave_speed"
        private const val KEY_FOOTER_WAVE_INTERVAL = "pref_footer_wave_interval"
        private const val KEY_FOOTER_WAVE_COLOR = "pref_footer_wave_color"

        @Volatile
        private var INSTANCE: AppSettingsManager? = null

        fun getInstance(context: Context): AppSettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppSettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
