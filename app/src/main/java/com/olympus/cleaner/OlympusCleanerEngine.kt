package com.olympus.cleaner

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import java.io.Closeable
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * High-performance Android JNI interface for Olympus Text Cleaner Native Engine.
 *
 * Implements the official JNI bindings defined in the native library:
 * - Sub-pixel Signed Distance Field (SDF) contour extraction
 * - Adaptive Hybrid inpainting (Structure Tensor + PatchMatch + Poisson + TV)
 * - Thread-safe cancellation and RAII native memory lifecycle
 */
class OlympusCleanerEngine private constructor(private val nativeHandle: Long) : Closeable {

    private val isClosed = AtomicBoolean(false)

    companion object {
        private const val TAG = "OlympusCleanerEngine"

        @Volatile
        private var isLibraryLoaded = false

        @Volatile
        private var loadErrorMessage: String? = null

        init {
            loadNativeLibrary()
        }

        private fun loadNativeLibrary() {
            val supportedAbis = Build.SUPPORTED_ABIS ?: emptyArray()
            val is64Bit = supportedAbis.any { it.contains("64") }

            // Candidate library names based on architecture and user specification
            val candidates = if (is64Bit) {
                listOf("mtxv8a", "olympus_cleaner", "mtxv7a")
            } else {
                listOf("mtxv7a", "olympus_cleaner", "mtxv8a")
            }

            var loaded = false
            for (libName in candidates) {
                try {
                    System.loadLibrary(libName)
                    Log.i(TAG, "Successfully loaded native library: lib$libName.so")
                    loaded = true
                    break
                } catch (e: UnsatisfiedLinkError) {
                    Log.d(TAG, "Could not load lib$libName.so (${e.message})")
                } catch (t: Throwable) {
                    Log.d(TAG, "Error loading lib$libName.so: ${t.message}")
                }
            }

            isLibraryLoaded = loaded
            if (!loaded) {
                loadErrorMessage = "Native engine library not loaded. Ensure libmtxv8a.so (arm64-v8a) or libmtxv7a.so (armeabi-v7a) is installed."
                Log.w(TAG, loadErrorMessage!!)
            }
        }

        /**
         * Checks if the native library was successfully loaded on the current device.
         */
        fun isNativeAvailable(): Boolean = isLibraryLoaded

        /**
         * Returns an explanation of why the library could not be loaded, if any.
         */
        fun getLoadError(): String? = loadErrorMessage

        /**
         * Returns the semantic version of the native engine if loaded.
         */
        fun getVersion(): String {
            return if (isLibraryLoaded) {
                try {
                    nativeGetVersion()
                } catch (e: Throwable) {
                    "1.2.0 (fallback)"
                }
            } else {
                "Unavailable"
            }
        }

        /**
         * Instantiates an OlympusCleanerEngine with default parameters.
         *
         * @param targetConfidence Minimum fuzzy match score (0.0 to 1.0, default: 0.70f)
         * @param inpaintAlgorithm 0=TELEA, 1=NAVIER_STOKES, 2=EXEMPLAR_PATCH, 3=EXEMPLAR_ADVANCED, 4=HYBRID_ADAPTIVE (default: 4)
         * @param maskExpansionPx Pixels to expand mask beyond detected stroke boundary (default: 3)
         * @param featherRadiusPx Gaussian blur radius for smooth alpha transition (default: 2)
         * @param enableTiling Slices large images into overlapping tiles to prevent OOM (default: true)
         */
        @JvmStatic
        fun create(
            targetConfidence: Float = 0.70f,
            inpaintAlgorithm: Int = 4,
            maskExpansionPx: Int = 3,
            featherRadiusPx: Int = 2,
            enableTiling: Boolean = true
        ): OlympusCleanerEngine {
            if (!isLibraryLoaded) {
                return OlympusCleanerEngine(0L)
            }
            return try {
                val handle = nativeCreate(
                    targetConfidence,
                    inpaintAlgorithm,
                    maskExpansionPx,
                    featherRadiusPx,
                    enableTiling
                )
                OlympusCleanerEngine(handle)
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to create native engine handle: ${e.message}")
                OlympusCleanerEngine(0L)
            }
        }

        /**
         * Instantiates an OlympusCleanerEngine with advanced hybrid configuration.
         */
        @JvmStatic
        fun createAdvanced(
            targetConfidence: Float = 0.70f,
            inpaintAlgorithm: Int = 4,
            maskExpansionPx: Int = 3,
            featherRadiusPx: Int = 2,
            enableTiling: Boolean = true,
            qualityProfile: Int = 1, // 0=FAST, 1=BALANCED, 2=HIGH_QUALITY, 3=ULTRA_DETAIL
            enableStructureTensor: Boolean = true,
            enablePoissonBlend: Boolean = true,
            enableFourierAnalysis: Boolean = true
        ): OlympusCleanerEngine {
            if (!isLibraryLoaded) {
                return OlympusCleanerEngine(0L)
            }
            return try {
                val handle = nativeCreateAdvanced(
                    targetConfidence,
                    inpaintAlgorithm,
                    maskExpansionPx,
                    featherRadiusPx,
                    enableTiling,
                    qualityProfile,
                    enableStructureTensor,
                    enablePoissonBlend,
                    enableFourierAnalysis
                )
                OlympusCleanerEngine(handle)
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to create advanced native engine: ${e.message}")
                create(targetConfidence, inpaintAlgorithm, maskExpansionPx, featherRadiusPx, enableTiling)
            }
        }

        // Native declarations
        @JvmStatic
        private external fun nativeCreate(
            targetConfidence: Float,
            inpaintAlgorithm: Int,
            maskExpansionPx: Int,
            featherRadiusPx: Int,
            enableTiling: Boolean
        ): Long

        @JvmStatic
        private external fun nativeCreateAdvanced(
            targetConfidence: Float,
            inpaintAlgorithm: Int,
            maskExpansionPx: Int,
            featherRadiusPx: Int,
            enableTiling: Boolean,
            qualityProfile: Int,
            enableStructureTensor: Boolean,
            enablePoissonBlend: Boolean,
            enableFourierAnalysis: Boolean
        ): Long

        @JvmStatic
        private external fun nativeProcessBitmap(
            handle: Long,
            inputBitmap: Bitmap,
            outputBitmap: Bitmap,
            generateMask: Boolean
        ): Int

        @JvmStatic
        private external fun nativeProcessByteBuffer(
            handle: Long,
            inputBuffer: ByteBuffer,
            width: Int,
            height: Int,
            channels: Int,
            stride: Int,
            outputBuffer: ByteBuffer
        ): Int

        @JvmStatic
        private external fun nativeCancel(handle: Long)

        @JvmStatic
        private external fun nativeGetVersion(): String

        @JvmStatic
        private external fun nativeDestroy(handle: Long)
    }

    /**
     * Cleans watermarks from the provided Bitmap directly in memory.
     * Guarantees ARGB_8888 software format for zero-copy JNI pixel locking.
     *
     * @param original Input bitmap to clean
     * @return Cleaned bitmap, or the original bitmap if native engine is unavailable or inpainting is skipped
     */
    fun cleanImage(original: Bitmap): Bitmap {
        if (!isLibraryLoaded || nativeHandle == 0L || isClosed.get()) {
            return original
        }

        return try {
            // Ensure bitmap is in ARGB_8888 software format for AndroidBitmap_lockPixels
            val inputSoftwareBitmap = if (original.config != Bitmap.Config.ARGB_8888) {
                original.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                original
            }

            // Create matching mutable output bitmap
            val outputBitmap = Bitmap.createBitmap(
                inputSoftwareBitmap.width,
                inputSoftwareBitmap.height,
                Bitmap.Config.ARGB_8888
            )

            val status = nativeProcessBitmap(nativeHandle, inputSoftwareBitmap, outputBitmap, false)

            if (status == 0) { // StatusCode::OK
                outputBitmap
            } else {
                Log.w(TAG, "Native process returned status code: $status")
                original
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Exception during native image cleaning: ${e.message}")
            original
        }
    }

    /**
     * Signals the active native worker threads to halt processing immediately.
     */
    fun cancel() {
        if (isLibraryLoaded && nativeHandle != 0L && !isClosed.get()) {
            try {
                nativeCancel(nativeHandle)
            } catch (e: Throwable) {
                Log.e(TAG, "Error cancelling native task: ${e.message}")
            }
        }
    }

    override fun close() {
        if (isClosed.compareAndSet(false, true)) {
            if (isLibraryLoaded && nativeHandle != 0L) {
                try {
                    nativeDestroy(nativeHandle)
                } catch (e: Throwable) {
                    Log.e(TAG, "Error releasing native engine handle: ${e.message}")
                }
            }
        }
    }

    protected fun finalize() {
        close()
    }
}
