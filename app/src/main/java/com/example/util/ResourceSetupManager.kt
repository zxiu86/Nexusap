package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.example.data.model.GitHubReleaseAsset
import com.example.data.model.GitHubReleaseDto
import com.example.data.network.GitHubNetworkModule
import com.olympus.cleaner.OlympusCleanerEngine
import com.olympus.cleaner.OlympusCleanerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * State definitions for the Mandatory First-Launch Resource Setup System.
 */
sealed class ResourceSetupState {
    object Idle : ResourceSetupState()

    object Discovering : ResourceSetupState()

    data class MetadataReady(
        val assetName: String,
        val targetAbi: String,
        val version: String,
        val totalBytes: Long,
        val formattedSize: String
    ) : ResourceSetupState()

    data class Downloading(
        val assetName: String,
        val targetAbi: String,
        val version: String,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val progressPercent: Int,
        val speedText: String,
        val formattedProgress: String,
        val formattedSize: String
    ) : ResourceSetupState()

    data class Verifying(
        val assetName: String,
        val message: String
    ) : ResourceSetupState()

    data class Extracting(
        val message: String
    ) : ResourceSetupState()

    data class InitializingEngine(
        val message: String
    ) : ResourceSetupState()

    data class Success(
        val version: String,
        val assetName: String,
        val engineStatus: String
    ) : ResourceSetupState()

    data class Error(
        val message: String,
        val canContinueWithoutCleaning: Boolean = true,
        val isOffline: Boolean = false
    ) : ResourceSetupState()
}

/**
 * Coordinates discovery, verification, downloading, and JNI initialization of the
 * Olympus MTX cleaning engine assets from GitHub repository (zxiu86/nexusap).
 */
object ResourceSetupManager {

    private const val TAG = "ResourceSetupManager"
    private const val PREFS_NAME = "nexus_resource_setup_prefs"
    private const val KEY_SETUP_COMPLETED = "is_setup_completed_v219"
    private const val KEY_INSTALLED_VERSION = "installed_engine_version"
    private const val KEY_INSTALLED_ASSET = "installed_asset_name"
    private const val KEY_INSTALLED_TIMESTAMP = "installed_timestamp"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var downloadJob: Job? = null

    private val _setupState = MutableStateFlow<ResourceSetupState>(ResourceSetupState.Idle)
    val setupState: StateFlow<ResourceSetupState> = _setupState.asStateFlow()

    private val _isDialogVisible = MutableStateFlow(false)
    val isDialogVisible: StateFlow<Boolean> = _isDialogVisible.asStateFlow()

    private var networkMonitor: NetworkMonitor? = null

    /**
     * Initializes the manager, attaches network listener, and checks if first-launch setup is required.
     */
    fun initialize(context: Context) {
        val appContext = context.applicationContext

        // Initialize NetworkMonitor to auto-reopen dialog when internet restores
        if (networkMonitor == null) {
            networkMonitor = NetworkMonitor(appContext)
            scope.launch {
                networkMonitor?.isOnline?.collect { isOnline ->
                    if (isOnline && !isSetupCompleted(appContext) && !_isDialogVisible.value) {
                        Log.i(TAG, "Network restored: Re-triggering mandatory resource setup dialog.")
                        withContext(Dispatchers.Main) {
                            showSetupDialog(appContext)
                        }
                    }
                }
            }
        }

        // Check if already completed and verified
        if (!isSetupCompleted(appContext)) {
            Log.i(TAG, "First-launch resource setup required.")
            showSetupDialog(appContext)
        } else {
            Log.i(TAG, "Resource setup already completed. Checking native engine...")
            scope.launch {
                OlympusCleanerEngine.loadNativeLibrary(appContext)
                OlympusCleanerManager.getOrCreateEngine()
            }
        }
    }

    /**
     * Returns true only if the native engine resources have been successfully set up and verified.
     */
    fun isSetupCompleted(context: Context): Boolean {
        val prefs = getPrefs(context)
        val completed = prefs.getBoolean(KEY_SETUP_COMPLETED, false)
        if (!completed) return false

        // Fast runtime health verification
        val nativeDir = context.getDir("nexus_native_libs", Context.MODE_PRIVATE)
        val hasExtractedLib = File(nativeDir, "libolympus_cleaner.so").exists() ||
                File(nativeDir, "libmtx_v8a.so").exists() ||
                File(nativeDir, "libmtx_v7a.so").exists()

        val hasAppLib = File(context.applicationInfo.nativeLibraryDir, "libolympus_cleaner.so").exists() ||
                File(context.applicationInfo.nativeLibraryDir, "libmtx_v8a.so").exists()

        return hasExtractedLib || hasAppLib || OlympusCleanerEngine.isNativeAvailable()
    }

    fun getInstalledVersion(context: Context): String {
        return getPrefs(context).getString(KEY_INSTALLED_VERSION, "v2.1.9") ?: "v2.1.9"
    }

    fun showSetupDialog(context: Context) {
        _isDialogVisible.value = true
        startSetup(context)
    }

    fun dismissDialogTemporarily() {
        _isDialogVisible.value = false
    }

    /**
     * Starts the full discovery, download, and verification workflow.
     */
    fun startSetup(context: Context) {
        downloadJob?.cancel()
        val appContext = context.applicationContext

        downloadJob = scope.launch {
            try {
                _setupState.value = ResourceSetupState.Discovering

                // 1. Detect device architecture
                val supportedAbis = Build.SUPPORTED_ABIS ?: emptyArray()
                val is64Bit = supportedAbis.any { it.contains("64") }
                val targetAbi = if (is64Bit) "arm64-v8a" else "armeabi-v7a"
                val expectedAssetName = if (is64Bit) "libmtx_v8a.zip" else "libmtx_v7a.zip"

                Log.d(TAG, "Detecting resources for ABI: $targetAbi (Preferred asset: $expectedAssetName)")

                // 2. Query GitHub Releases for zxiu86/nexusap
                val owner = GitHubNetworkModule.DEFAULT_OWNER
                val repo = GitHubNetworkModule.DEFAULT_APP_REPO

                var matchedAsset: GitHubReleaseAsset? = null
                var releaseVersion = "v2.1.9"
                var releaseBody = ""

                try {
                    val api = GitHubNetworkModule.apiService
                    val latestResp = api.getLatestRelease(owner, repo)
                    if (latestResp.isSuccessful && latestResp.body() != null) {
                        val release = latestResp.body()!!
                        releaseVersion = release.tagName ?: "v2.1.9"
                        releaseBody = release.body ?: ""
                        matchedAsset = findMatchingAsset(release.assets, is64Bit)
                    }

                    if (matchedAsset == null) {
                        val allResp = api.getAllReleases(owner, repo)
                        if (allResp.isSuccessful && !allResp.body().isNullOrEmpty()) {
                            for (rel in allResp.body()!!) {
                                matchedAsset = findMatchingAsset(rel.assets, is64Bit)
                                if (matchedAsset != null) {
                                    releaseVersion = rel.tagName ?: releaseVersion
                                    releaseBody = rel.body ?: ""
                                    break
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "GitHub API discovery error: ${e.message}")
                }

                // Fallback direct download URL if release asset was not in API response
                val downloadUrl = matchedAsset?.browserDownloadUrl
                    ?: "https://github.com/$owner/$repo/releases/download/$releaseVersion/$expectedAssetName"

                var assetSize = matchedAsset?.size ?: 0L

                // If size is missing from metadata, query HTTP Content-Length
                if (assetSize <= 0L) {
                    assetSize = fetchContentLength(downloadUrl)
                }

                val formattedSize = if (assetSize > 0L) formatBytes(assetSize) else "جارٍ تحديد حجم الملف"

                _setupState.value = ResourceSetupState.MetadataReady(
                    assetName = matchedAsset?.name ?: expectedAssetName,
                    targetAbi = targetAbi,
                    version = releaseVersion,
                    totalBytes = assetSize,
                    formattedSize = formattedSize
                )

                delay(350L)

                // 3. Download the asset into app's private cache
                val tempZip = File(appContext.cacheDir, "mtx_engine_${System.currentTimeMillis()}.zip")
                val sha256Expected = extractExpectedSha256(releaseBody, matchedAsset?.name ?: expectedAssetName)

                downloadFileWithProgress(
                    url = downloadUrl,
                    destination = tempZip,
                    totalBytes = assetSize,
                    assetName = matchedAsset?.name ?: expectedAssetName,
                    targetAbi = targetAbi,
                    version = releaseVersion,
                    formattedSize = formattedSize
                )

                // 4. Verify integrity (SHA-256 and ZIP header)
                _setupState.value = ResourceSetupState.Verifying(
                    assetName = matchedAsset?.name ?: expectedAssetName,
                    message = "جارٍ التحقق من بصمة SHA-256 وسلامة الأرشيف..."
                )

                val calculatedSha256 = calculateSha256(tempZip)
                Log.d(TAG, "Calculated SHA-256: $calculatedSha256")

                if (!sha256Expected.isNullOrBlank()) {
                    if (!calculatedSha256.equals(sha256Expected, ignoreCase = true)) {
                        tempZip.delete()
                        throw IllegalStateException("فشل فحص بصمة الأمان SHA-256 (الملف قد يكون تالفاً أو تم التعديل عليه).")
                    }
                }

                // Verify ZIP structure
                verifyZipIntegrity(tempZip)

                // 5. Safely extract libraries
                _setupState.value = ResourceSetupState.Extracting("جارٍ فك ضغط المكتبات الأصلية بأمان...")

                val targetLibsDir = appContext.getDir("nexus_native_libs", Context.MODE_PRIVATE)
                targetLibsDir.mkdirs()

                safeExtractZip(tempZip, targetLibsDir)
                tempZip.delete()

                // 6. Initialize native engine & self-test
                _setupState.value = ResourceSetupState.InitializingEngine("جارٍ تهيئة واختبار المحرك الأصلي في الذاكرة...")

                OlympusCleanerEngine.loadNativeLibrary(appContext, forceReload = true)
                val testResult = OlympusCleanerManager.runDiagnosticTest(appContext)

                val engineVer = if (testResult.engineVersion.isNotBlank()) testResult.engineVersion else releaseVersion

                // Mark setup completed permanently
                markSetupCompleted(
                    context = appContext,
                    version = engineVer,
                    assetName = matchedAsset?.name ?: expectedAssetName
                )

                _setupState.value = ResourceSetupState.Success(
                    version = engineVer,
                    assetName = matchedAsset?.name ?: expectedAssetName,
                    engineStatus = if (testResult.isSuccess) "المحرك نشط وجاهز للعمل" else "تم تجهيز الموارد بنجاح"
                )

                Log.i(TAG, "Resource setup completed successfully!")

                // Auto-dismiss dialog after successful setup
                delay(1200L)
                _isDialogVisible.value = false

            } catch (e: Exception) {
                Log.e(TAG, "Setup failed: ${e.message}", e)
                val isOffline = networkMonitor?.isOnline?.value == false
                val errorMsg = when {
                    isOffline -> "لا يوجد اتصال بالإنترنت. يرجى التحقق من الشبكة وإعادة المحاولة."
                    e.message != null -> e.message!!
                    else -> "حدث خطأ غير متوقع أثناء تحميل أو تثبيت موارد المحرك."
                }
                _setupState.value = ResourceSetupState.Error(
                    message = errorMsg,
                    canContinueWithoutCleaning = true,
                    isOffline = isOffline
                )
            }
        }
    }

    private fun findMatchingAsset(assets: List<GitHubReleaseAsset>?, is64Bit: Boolean): GitHubReleaseAsset? {
        if (assets.isNullOrEmpty()) return null
        val searchKeyword = if (is64Bit) "v8a" else "v7a"

        return assets.firstOrNull {
            val name = it.name?.lowercase() ?: ""
            name.contains(searchKeyword) && name.endsWith(".zip")
        } ?: assets.firstOrNull {
            val name = it.name?.lowercase() ?: ""
            name.contains("mtx") && name.endsWith(".zip")
        }
    }

    private suspend fun fetchContentLength(url: String): Long = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).head().build()
            val response = GitHubNetworkModule.okHttpClient.newCall(request).execute()
            val length = response.header("Content-Length")?.toLongOrNull() ?: 0L
            response.close()
            length
        } catch (_: Exception) {
            0L
        }
    }

    private suspend fun downloadFileWithProgress(
        url: String,
        destination: File,
        totalBytes: Long,
        assetName: String,
        targetAbi: String,
        version: String,
        formattedSize: String
    ) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Nexus-Resource-Setup/${AppVersionConfig.VERSION_NAME}")
            .build()

        val response = GitHubNetworkModule.okHttpClient.newCall(request).execute()
        if (!response.isSuccessful || response.body == null) {
            throw IllegalStateException("تعذر تنزيل حزمة المحرك من الخادم (رمز الخطأ: ${response.code})")
        }

        val body = response.body!!
        val contentLength = if (totalBytes > 0L) totalBytes else body.contentLength()
        val finalTotal = if (contentLength > 0L) contentLength else 15L * 1024 * 1024 // Estimated 15MB if unknown

        destination.parentFile?.mkdirs()
        var downloaded = 0L
        val startTime = System.currentTimeMillis()
        var lastUpdate = 0L

        body.byteStream().use { input ->
            FileOutputStream(destination).use { output ->
                val buffer = ByteArray(32 * 1024)
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    if (!isActive) {
                        destination.delete()
                        throw IllegalStateException("تم إلغاء عملية التنزيل")
                    }

                    output.write(buffer, 0, bytesRead)
                    downloaded += bytesRead

                    val now = System.currentTimeMillis()
                    if (now - lastUpdate >= 120L || downloaded >= finalTotal) {
                        lastUpdate = now
                        val percent = ((downloaded.toDouble() / finalTotal) * 100).toInt().coerceIn(0, 100)
                        val elapsedSec = ((now - startTime) / 1000.0).coerceAtLeast(0.1)
                        val speedKb = (downloaded / 1024.0) / elapsedSec

                        val speedText = if (speedKb >= 1024.0) {
                            String.format("%.1f MB/s", speedKb / 1024.0)
                        } else {
                            String.format("%.0f KB/s", speedKb)
                        }

                        _setupState.value = ResourceSetupState.Downloading(
                            assetName = assetName,
                            targetAbi = targetAbi,
                            version = version,
                            downloadedBytes = downloaded,
                            totalBytes = finalTotal,
                            progressPercent = percent,
                            speedText = speedText,
                            formattedProgress = "${formatBytes(downloaded)} / ${formatBytes(finalTotal)}",
                            formattedSize = formattedSize
                        )
                    }
                }
                output.flush()
            }
        }
    }

    private fun extractExpectedSha256(releaseBody: String, assetName: String): String? {
        if (releaseBody.isBlank()) return null
        val lines = releaseBody.lines()
        for (line in lines) {
            if (line.contains(assetName, ignoreCase = true) || line.contains("sha256", ignoreCase = true)) {
                val hexMatch = Regex("[a-fA-F0-9]{64}").find(line)
                if (hexMatch != null) return hexMatch.value
            }
        }
        return null
    }

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(16 * 1024)
            var count: Int
            while (fis.read(buffer).also { count = it } != -1) {
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun verifyZipIntegrity(zipFile: File) {
        try {
            val zip = ZipFile(zipFile)
            val entries = zip.entries()
            var count = 0
            while (entries.hasMoreElements()) {
                entries.nextElement()
                count++
            }
            zip.close()
            if (count == 0) throw IllegalStateException("ملف الأرشيف فارغ.")
        } catch (e: Exception) {
            throw IllegalStateException("ملف الأرشيف تالف وغير صالح: ${e.message}")
        }
    }

    private fun safeExtractZip(zipFile: File, targetDir: File) {
        val zip = ZipFile(zipFile)
        val entries = zip.entries()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val destFile = File(targetDir, entry.name)

            // Prevent Zip Slip vulnerability
            val canonicalDestPath = destFile.canonicalPath
            val canonicalTargetPath = targetDir.canonicalPath
            if (!canonicalDestPath.startsWith(canonicalTargetPath)) {
                throw SecurityException("محاولة غير آمنة لكتابة ملف خارج المسار المسموح: ${entry.name}")
            }

            if (entry.isDirectory) {
                destFile.mkdirs()
            } else {
                destFile.parentFile?.mkdirs()
                zip.getInputStream(entry).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                destFile.setReadable(true, false)
                destFile.setExecutable(true, false)
                Log.d(TAG, "Extracted library: ${destFile.name} (${destFile.length()} bytes)")
            }
        }
        zip.close()
    }

    private fun markSetupCompleted(context: Context, version: String, assetName: String) {
        getPrefs(context).edit()
            .putBoolean(KEY_SETUP_COMPLETED, true)
            .putString(KEY_INSTALLED_VERSION, version)
            .putString(KEY_INSTALLED_ASSET, assetName)
            .putLong(KEY_INSTALLED_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun formatBytes(bytes: Long): String {
        val mb = bytes.toDouble() / (1024.0 * 1024.0)
        return String.format("%.1f MB", mb)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
