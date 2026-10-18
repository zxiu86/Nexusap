package com.olympus.cleaner

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.data.settings.AppSettingsManager

/**
 * Singleton manager coordinating Olympus Text Cleaner lifecycle, configuration,
 * and concurrent cleaning requests.
 */
object OlympusCleanerManager {

    private const val TAG = "OlympusCleanerManager"

    @Volatile
    private var sharedEngine: OlympusCleanerEngine? = null
    private val initLock = Any()

    /**
     * Initializes or returns the shared engine instance.
     */
    fun getOrCreateEngine(): OlympusCleanerEngine? {
        if (!OlympusCleanerEngine.isNativeAvailable()) {
            return null
        }
        if (sharedEngine == null) {
            synchronized(initLock) {
                if (sharedEngine == null) {
                    sharedEngine = OlympusCleanerEngine.create(
                        targetConfidence = 0.70f,
                        inpaintAlgorithm = 4, // HYBRID_ADAPTIVE
                        maskExpansionPx = 3,
                        featherRadiusPx = 2,
                        enableTiling = true
                    )
                    Log.i(TAG, "OlympusCleaner shared engine initialized (Version: ${OlympusCleanerEngine.getVersion()})")
                }
            }
        }
        return sharedEngine
    }

    /**
     * Cleans watermarks from a Bitmap in real-time.
     * Thread-safe and returns the cleaned bitmap directly.
     */
    fun cleanBitmap(context: Context, original: Bitmap): Bitmap {
        val engine = getOrCreateEngine() ?: return original

        return try {
            val cleaned = engine.cleanImage(original)
            if (cleaned !== original) {
                // Increment app watermark counter
                AppSettingsManager.getInstance(context).incrementCleanedWatermarksCount(1)
            }
            cleaned
        } catch (e: Throwable) {
            Log.e(TAG, "Failed cleaning bitmap with Olympus Cleaner: ${e.message}")
            original
        }
    }

    /**
     * Halts active inpainting worker threads.
     */
    fun cancelActive() {
        sharedEngine?.cancel()
    }

    /**
     * Executes the comprehensive self-diagnostic test on the native engine.
     */
    fun runDiagnosticTest(context: Context): CleanerDiagnosticResult {
        return OlympusCleanerEngine.runDiagnosticTest(context)
    }

    /**
     * Resets the active engine and attempts to reload the native library.
     */
    fun reloadEngine(context: Context? = null): OlympusCleanerEngine? {
        release()
        OlympusCleanerEngine.loadNativeLibrary(context = context, forceReload = true)
        return getOrCreateEngine()
    }

    /**
     * Closes the engine and frees all native memory.
     */
    fun release() {
        synchronized(initLock) {
            sharedEngine?.close()
            sharedEngine = null
        }
    }
}
