package com.olympus.cleaner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.Log
import java.io.Closeable
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Diagnostic result model capturing the operational status of the Olympus MTX Native Engine.
 */
data class CleanerDiagnosticResult(
    val isSuccess: Boolean,
    val engineVersion: String,
    val loadedLibraryName: String?,
    val latencyMs: Long,
    val deviceAbis: List<String>,
    val isArmArchitecture: Boolean,
    val isHostX86: Boolean,
    val warningMessage: String?,
    val errorMessage: String?,
    val logs: List<String>
)

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
        private var loadedLibName: String? = null

        @Volatile
        private var loadErrorMessage: String? = null

        private val diagnosticLogs = CopyOnWriteArrayList<String>()

        init {
            loadNativeLibrary()
        }

        fun logDiagnostic(msg: String) {
            val time = try {
                SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
            } catch (_: Exception) {
                "${System.currentTimeMillis()}"
            }
            val entry = "[$time] $msg"
            diagnosticLogs.add(entry)
            if (diagnosticLogs.size > 150) {
                diagnosticLogs.removeAt(0)
            }
            Log.d(TAG, msg)
        }

        /**
         * Attempts to load the native MTX engine library into memory.
         */
        @Synchronized
        fun loadNativeLibrary(forceReload: Boolean = false): Boolean {
            if (isLibraryLoaded && !forceReload) return true

            val supportedAbis = try {
                Build.SUPPORTED_ABIS ?: emptyArray()
            } catch (_: Throwable) {
                emptyArray()
            }
            val is64Bit = supportedAbis.any { it.contains("64") }
            val isArm = supportedAbis.any { it.contains("arm", ignoreCase = true) }
            val isX86 = supportedAbis.any { it.contains("x86", ignoreCase = true) }

            logDiagnostic("بدء محاولة تحميل مكتبة التنظيف الأصلية (ABIs: ${supportedAbis.joinToString()})")

            // Candidate library names: matches actual ELF SONAME (olympus_cleaner) or archive aliases (mtx_v8a, mtxv8a, mtx_v7a)
            val candidates = if (is64Bit) {
                listOf("olympus_cleaner", "mtx_v8a", "mtxv8a", "mtx_v7a", "mtxv7a")
            } else {
                listOf("olympus_cleaner", "mtx_v7a", "mtxv7a", "mtx_v8a", "mtxv8a")
            }

            var loaded = false
            val failureDetails = StringBuilder()

            for (libName in candidates) {
                try {
                    logDiagnostic("محاولة تحميل: System.loadLibrary(\"$libName\")...")
                    System.loadLibrary(libName)
                    logDiagnostic("✅ نجح تحميل مكتبة المحرك الأصلي: lib$libName.so")
                    loadedLibName = "lib$libName.so"
                    loaded = true
                    loadErrorMessage = null
                    break
                } catch (e: UnsatisfiedLinkError) {
                    val msg = "فشل تحميل $libName: ${e.message}"
                    logDiagnostic("⚠️ $msg")
                    failureDetails.append(msg).append("\n")
                } catch (t: Throwable) {
                    val msg = "خطأ غير متوقع أثناء تحميل $libName: ${t.message}"
                    logDiagnostic("❌ $msg")
                    failureDetails.append(msg).append("\n")
                }
            }

            isLibraryLoaded = loaded
            if (!loaded) {
                val archHint = if (isX86 && !isArm) {
                    "الجهاز/المحاكي الحالي يعمل بمعمارية x86/x86_64، بينما مكتبات MTX المدمجة مخصصة لأجهزة الهواتف الذكية ARM (arm64-v8a و armeabi-v7a). التطبيق سيعمل فوراً وتلقائياً على هواتف الأندرويد الحقيقية."
                } else {
                    "تأكد من وجود وتوافق مكتبة libolympus_cleaner.so داخل حزمة التطبيق لأحد المعماريات المدعومة."
                }
                loadErrorMessage = "تعذر تحميل مكتبة التنظيف الأصلية.\n$archHint\nالتفاصيل البرمجية:\n${failureDetails.toString().trim()}"
                logDiagnostic("❌ $loadErrorMessage")
            }
            return loaded
        }

        /**
         * Checks if the native library was successfully loaded on the current device.
         */
        fun isNativeAvailable(): Boolean = isLibraryLoaded

        /**
         * Returns the filename of the loaded library, if any.
         */
        fun getLoadedLibraryName(): String? = loadedLibName

        /**
         * Returns the diagnostic logs recorded during initialization and execution.
         */
        fun getDiagnosticLogs(): List<String> = diagnosticLogs.toList()

        /**
         * Clears diagnostic logs.
         */
        fun clearDiagnosticLogs() {
            diagnosticLogs.clear()
        }

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
                "غير متوفر (المكتبة غير محملة)"
            }
        }

        /**
         * Executes an end-to-end live diagnostic self-test:
         * 1. Checks or forces library load.
         * 2. Tests version retrieval.
         * 3. Creates a test native handle.
         * 4. Synthesizes a test bitmap and passes it through JNI pixel locking.
         * 5. Measures processing latency.
         * 6. Cleans up native handle and memory safely.
         */
        fun runDiagnosticTest(context: Context): CleanerDiagnosticResult {
            val startTime = System.currentTimeMillis()
            logDiagnostic("--- بدء الفحص التشخيصي المباشر لمحرك التنظيف ---")

            val supportedAbis = try {
                (Build.SUPPORTED_ABIS ?: emptyArray()).toList()
            } catch (_: Throwable) {
                emptyList<String>()
            }
            val isArm = supportedAbis.any { it.contains("arm", ignoreCase = true) }
            val isX86 = supportedAbis.any { it.contains("x86", ignoreCase = true) }

            // 1. Check or reload library
            if (!isLibraryLoaded) {
                loadNativeLibrary(forceReload = true)
            }

            if (!isLibraryLoaded) {
                val warning = if (isX86 && !isArm) {
                    "المحاكي الحالي يعمل بمعمارية x86_64، بينما مكتبات MTX مبنية لأجهزة الأندرويد الحقيقية ARM (arm64-v8a و armeabi-v7a). على هواتف المستخدمين الحقيقية ستعمل الآلية فوراً وبأقصى سرعة."
                } else null

                logDiagnostic("❌ انتهى الفحص: المكتبة غير محملة.")
                return CleanerDiagnosticResult(
                    isSuccess = false,
                    engineVersion = "غير متوفر",
                    loadedLibraryName = null,
                    latencyMs = System.currentTimeMillis() - startTime,
                    deviceAbis = supportedAbis,
                    isArmArchitecture = isArm,
                    isHostX86 = isX86,
                    warningMessage = warning,
                    errorMessage = loadErrorMessage ?: "المكتبة الأصلية لم يتم تحميلها",
                    logs = getDiagnosticLogs()
                )
            }

            // 2. Query Version
            val version = try {
                val ver = nativeGetVersion()
                logDiagnostic("نسخة المحرك من JNI: $ver")
                ver
            } catch (t: Throwable) {
                logDiagnostic("⚠️ تعذر جلب النسخة: ${t.message}")
                "1.2.0 (fallback)"
            }

            // 3. Test instantiation & image processing
            try {
                logDiagnostic("إنشاء مقبض محرك تجريبي (nativeCreate)...")
                val handle = nativeCreate(0.70f, 4, 3, 2, true)
                if (handle == 0L) {
                    logDiagnostic("❌ فشل إنشاء مقبض المحرك الأصلي (nativeCreate returned 0).")
                    return CleanerDiagnosticResult(
                        isSuccess = false,
                        engineVersion = version,
                        loadedLibraryName = loadedLibName,
                        latencyMs = System.currentTimeMillis() - startTime,
                        deviceAbis = supportedAbis,
                        isArmArchitecture = isArm,
                        isHostX86 = isX86,
                        warningMessage = null,
                        errorMessage = "فشل إنشاء المقبض الأصلي لمحرك التنظيف (nativeCreate returned 0)",
                        logs = getDiagnosticLogs()
                    )
                }

                // Create test bitmap (160x160)
                val testBitmap = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(testBitmap)
                canvas.drawColor(Color.WHITE)
                val paint = Paint().apply {
                    color = Color.BLACK
                    textSize = 18f
                }
                canvas.drawText("NEXUS TEST", 20f, 80f, paint)

                val outputBitmap = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)

                logDiagnostic("تشغيل معالجة صورة تجريبية (nativeProcessBitmap)...")
                val procStart = System.currentTimeMillis()
                val status = nativeProcessBitmap(handle, testBitmap, outputBitmap, false)
                val latency = System.currentTimeMillis() - procStart
                logDiagnostic("اكتملت المعالجة في $latency ms، رمز الحالة: $status")

                // Destroy temporary handle
                try {
                    nativeDestroy(handle)
                } catch (_: Throwable) {}

                testBitmap.recycle()
                outputBitmap.recycle()

                if (status == 0) {
                    logDiagnostic("✅ نجح الفحص التشخيصي: محرك MTX يعمل بكفاءة تامة!")
                    return CleanerDiagnosticResult(
                        isSuccess = true,
                        engineVersion = version,
                        loadedLibraryName = loadedLibName,
                        latencyMs = latency,
                        deviceAbis = supportedAbis,
                        isArmArchitecture = isArm,
                        isHostX86 = isX86,
                        warningMessage = null,
                        errorMessage = null,
                        logs = getDiagnosticLogs()
                    )
                } else {
                    val err = "المحرك أرجع كود حالة غير ناجح: $status"
                    logDiagnostic("⚠️ $err")
                    return CleanerDiagnosticResult(
                        isSuccess = false,
                        engineVersion = version,
                        loadedLibraryName = loadedLibName,
                        latencyMs = latency,
                        deviceAbis = supportedAbis,
                        isArmArchitecture = isArm,
                        isHostX86 = isX86,
                        warningMessage = null,
                        errorMessage = err,
                        logs = getDiagnosticLogs()
                    )
                }
            } catch (t: Throwable) {
                val err = "استثناء أثناء اختبار المعالجة: ${t.message}"
                logDiagnostic("❌ $err")
                return CleanerDiagnosticResult(
                    isSuccess = false,
                    engineVersion = version,
                    loadedLibraryName = loadedLibName,
                    latencyMs = System.currentTimeMillis() - startTime,
                    deviceAbis = supportedAbis,
                    isArmArchitecture = isArm,
                    isHostX86 = isX86,
                    warningMessage = null,
                    errorMessage = err,
                    logs = getDiagnosticLogs()
                )
            }
        }

        /**
         * Instantiates an OlympusCleanerEngine with default parameters.
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
            qualityProfile: Int = 1,
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
     */
    fun cleanImage(original: Bitmap): Bitmap {
        if (!isLibraryLoaded || nativeHandle == 0L || isClosed.get()) {
            return original
        }

        return try {
            val inputSoftwareBitmap = if (original.config != Bitmap.Config.ARGB_8888) {
                original.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                original
            }

            val outputBitmap = Bitmap.createBitmap(
                inputSoftwareBitmap.width,
                inputSoftwareBitmap.height,
                Bitmap.Config.ARGB_8888
            )

            val status = nativeProcessBitmap(nativeHandle, inputSoftwareBitmap, outputBitmap, false)

            if (status == 0) {
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
