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

    const val TARGET_ENGINE_TAG = "0.0.1"
    const val ASSET_NAME_ARM64 = "libmtx_v8a.zip"
    const val ASSET_NAME_ARMEABI_V7A = "libmtx_v7a.zip"

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

                // 1. Detect device architecture & target ABI
                val supportedAbis = Build.SUPPORTED_ABIS ?: emptyArray()
                val targetAbi: String
                val expectedAssetName: String

                when {
                    supportedAbis.any { it.equals("arm64-v8a", ignoreCase = true) } -> {
                        targetAbi = "arm64-v8a"
                        expectedAssetName = ASSET_NAME_ARM64
                    }
                    supportedAbis.any { it.equals("armeabi-v7a", ignoreCase = true) || it.equals("armeabi", ignoreCase = true) } -> {
                        targetAbi = "armeabi-v7a"
                        expectedAssetName = ASSET_NAME_ARMEABI_V7A
                    }
                    supportedAbis.any { it.contains("64") } -> {
                        targetAbi = "arm64-v8a"
                        expectedAssetName = ASSET_NAME_ARM64
                    }
                    else -> {
                        targetAbi = "armeabi-v7a"
                        expectedAssetName = ASSET_NAME_ARMEABI_V7A
                    }
                }

                Log.i(TAG, "[DIAGNOSTIC] Selected ABI: $targetAbi (Supported ABIs: ${supportedAbis.joinToString()})")
                Log.i(TAG, "[DIAGNOSTIC] Selected asset name: $expectedAssetName")

                // 2. Query GitHub Releases API for the verified repository and specific tag 0.0.1
                val owner = GitHubNetworkModule.getConfiguredOwner()
                val repo = GitHubNetworkModule.getAppRepo()
                val api = GitHubNetworkModule.apiService

                Log.i(TAG, "[DIAGNOSTIC] Querying GitHub Releases API for repo: $owner/$repo, target tag: $TARGET_ENGINE_TAG")

                var targetRelease: GitHubReleaseDto? = null
                var apiResponseCode = 0

                // Attempt 1: Locate intended release directly by its actual tag "0.0.1"
                try {
                    val tagResp = api.getReleaseByTag(owner, repo, TARGET_ENGINE_TAG)
                    apiResponseCode = tagResp.code()
                    Log.i(TAG, "[DIAGNOSTIC] API response status for getReleaseByTag($TARGET_ENGINE_TAG): $apiResponseCode")

                    if (tagResp.isSuccessful && tagResp.body() != null) {
                        targetRelease = tagResp.body()
                    } else if (apiResponseCode == 401 || apiResponseCode == 403) {
                        throw IllegalStateException("خطأ في صلاحيات الوصول أو تم تجاوز حد استعلامات GitHub API (رمز الخطأ: $apiResponseCode).")
                    }
                } catch (e: Exception) {
                    if (e is IllegalStateException) throw e
                    Log.w(TAG, "[DIAGNOSTIC] Query by tag threw exception: ${e.message}")
                }

                // Attempt 2: If querying by tag endpoint failed or returned 404, query all releases
                if (targetRelease == null) {
                    try {
                        Log.i(TAG, "[DIAGNOSTIC] Fallback: Querying all releases for $owner/$repo...")
                        val allResp = api.getAllReleases(owner, repo)
                        apiResponseCode = allResp.code()
                        Log.i(TAG, "[DIAGNOSTIC] API response status for getAllReleases: $apiResponseCode")

                        if (allResp.isSuccessful && !allResp.body().isNullOrEmpty()) {
                            val releases = allResp.body()!!
                            targetRelease = releases.firstOrNull { it.tagName?.trim() == TARGET_ENGINE_TAG }
                                ?: releases.firstOrNull { it.name?.contains("Mtx", ignoreCase = true) == true }
                        } else if (apiResponseCode == 401 || apiResponseCode == 403) {
                            throw IllegalStateException("خطأ في صلاحيات الوصول أو تم تجاوز حد استعلامات GitHub API (رمز الخطأ: $apiResponseCode).")
                        } else if (apiResponseCode == 404) {
                            throw IllegalStateException("المستودع $owner/$repo غير موجود أو لا يمكن الوصول إليه عبر GitHub API (رمز الخطأ: 404).")
                        }
                    } catch (e: Exception) {
                        if (e is IllegalStateException) throw e
                        Log.w(TAG, "[DIAGNOSTIC] Fallback getAllReleases threw exception: ${e.message}")
                    }
                }

                if (targetRelease == null) {
                    val notFoundMsg = if (apiResponseCode == 404) {
                        "لم يتم العثور على الإصدار الوسم $TARGET_ENGINE_TAG في المستودع $owner/$repo (رمز الخطأ 404 Not Found)."
                    } else {
                        "تعذر تحديد موقع إصدار محرك التنظيف ($TARGET_ENGINE_TAG) في المستودع $owner/$repo (كود الاستجابة: $apiResponseCode)."
                    }
                    throw IllegalStateException(notFoundMsg)
                }

                val resolvedTag = targetRelease.tagName ?: TARGET_ENGINE_TAG
                val resolvedTitle = targetRelease.name ?: ""
                Log.i(TAG, "[DIAGNOSTIC] Resolved release tag: $resolvedTag (Title: $resolvedTitle)")

                // 3. Inspect assets array and match by exact name
                val apiAssets = targetRelease.assets ?: emptyList()
                val availableAssetNames = apiAssets.mapNotNull { it.name }
                Log.i(TAG, "[DIAGNOSTIC] Available asset names in assets[]: $availableAssetNames")

                var matchedAsset: GitHubReleaseAsset? = apiAssets.firstOrNull {
                    it.name?.equals(expectedAssetName, ignoreCase = true) == true
                }

                // If not in assets array (e.g. assets[] is empty because files are attached to markdown body), parse release.body
                if (matchedAsset == null || matchedAsset.browserDownloadUrl.isNullOrBlank()) {
                    Log.i(TAG, "[DIAGNOSTIC] Asset $expectedAssetName not found in assets[]. Inspecting release.body...")
                    val parsedAttachments = parseAssetsFromReleaseBody(targetRelease.body)
                    Log.i(TAG, "[DIAGNOSTIC] Parsed attachments from release body: ${parsedAttachments.map { it.name }}")
                    matchedAsset = parsedAttachments.firstOrNull {
                        it.name?.equals(expectedAssetName, ignoreCase = true) == true
                    }
                }

                if (matchedAsset == null || matchedAsset.browserDownloadUrl.isNullOrBlank()) {
                    throw IllegalStateException("الملف المطلوب ($expectedAssetName) غير موجود ضمن أصول الإصدار $resolvedTag في المستودع $owner/$repo.")
                }

                // 4. Extract direct download URL (never construct manually)
                val downloadUrl = matchedAsset.browserDownloadUrl!!.trim()
                val cleanLogUrl = sanitizeUrlForLogging(downloadUrl)
                Log.i(TAG, "[DIAGNOSTIC] Resolved direct download URL for $expectedAssetName: $cleanLogUrl")

                // 5. Read actual asset size dynamically
                var assetSize = matchedAsset.size ?: 0L
                if (assetSize <= 0L) {
                    assetSize = fetchContentLength(downloadUrl)
                    Log.i(TAG, "[DIAGNOSTIC] Dynamic content length fetched: $assetSize bytes")
                }

                val formattedSize = if (assetSize > 0L) formatBytes(assetSize) else "جارٍ تحديد حجم الملف..."

                _setupState.value = ResourceSetupState.MetadataReady(
                    assetName = matchedAsset.name ?: expectedAssetName,
                    targetAbi = targetAbi,
                    version = resolvedTag,
                    totalBytes = assetSize,
                    formattedSize = formattedSize
                )

                delay(350L)

                // 6. Download the asset into app's private cache
                val tempZip = File(appContext.cacheDir, "mtx_engine_${System.currentTimeMillis()}.zip")
                val sha256Expected = extractExpectedSha256(targetRelease.body ?: "", matchedAsset.name ?: expectedAssetName)

                downloadFileWithProgress(
                    url = downloadUrl,
                    destination = tempZip,
                    totalBytes = assetSize,
                    assetName = matchedAsset.name ?: expectedAssetName,
                    targetAbi = targetAbi,
                    version = resolvedTag,
                    formattedSize = formattedSize
                )

                // 7. Verify integrity (SHA-256 and ZIP header)
                _setupState.value = ResourceSetupState.Verifying(
                    assetName = matchedAsset.name ?: expectedAssetName,
                    message = "جارٍ التحقق من بصمة SHA-256 وسلامة الأرشيف..."
                )

                val calculatedSha256 = calculateSha256(tempZip)
                Log.d(TAG, "[DIAGNOSTIC] Calculated SHA-256: $calculatedSha256")

                if (!sha256Expected.isNullOrBlank()) {
                    if (!calculatedSha256.equals(sha256Expected, ignoreCase = true)) {
                        tempZip.delete()
                        throw IllegalStateException("فشل فحص بصمة الأمان SHA-256 (الملف قد يكون تالفاً أو تم التعديل عليه).")
                    }
                }

                // Verify ZIP structure
                verifyZipIntegrity(tempZip)

                // 8. Safely extract libraries and create alias mirrors
                _setupState.value = ResourceSetupState.Extracting("جارٍ فك ضغط المكتبات الأصلية بأمان...")

                val targetLibsDir = appContext.getDir("nexus_native_libs", Context.MODE_PRIVATE)
                targetLibsDir.mkdirs()

                safeExtractZip(tempZip, targetLibsDir, targetAbi)
                tempZip.delete()

                // 9. Initialize native engine & self-test
                _setupState.value = ResourceSetupState.InitializingEngine("جارٍ تهيئة واختبار المحرك الأصلي في الذاكرة...")

                OlympusCleanerEngine.loadNativeLibrary(appContext, forceReload = true)
                val testResult = OlympusCleanerManager.runDiagnosticTest(appContext)

                val engineVer = if (testResult.engineVersion.isNotBlank()) testResult.engineVersion else resolvedTag

                // Mark setup completed permanently
                markSetupCompleted(
                    context = appContext,
                    version = engineVer,
                    assetName = matchedAsset.name ?: expectedAssetName
                )

                _setupState.value = ResourceSetupState.Success(
                    version = engineVer,
                    assetName = matchedAsset.name ?: expectedAssetName,
                    engineStatus = if (testResult.isSuccess) "المحرك نشط وجاهز للعمل" else "تم تجهيز الموارد بنجاح"
                )

                Log.i(TAG, "[DIAGNOSTIC] Resource setup completed successfully! (Engine: $engineVer)")

                // Auto-dismiss dialog after successful setup
                delay(1200L)
                _isDialogVisible.value = false

            } catch (e: Exception) {
                Log.e(TAG, "[DIAGNOSTIC] Setup failed: ${e.message}", e)
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

    /**
     * Parses markdown attachments and direct links from GitHub release description body
     */
    fun parseAssetsFromReleaseBody(body: String?): List<GitHubReleaseAsset> {
        if (body.isNullOrBlank()) return emptyList()
        val list = mutableListOf<GitHubReleaseAsset>()

        // 1. Matches markdown link syntax: [libmtx_v8a.zip](https://github.com/user-attachments/files/33609140/libmtx_v8a.zip)
        val mdLinkRegex = Regex("""\[([^\]]+\.zip)\]\((https?://[^\s)]+)\)""", RegexOption.IGNORE_CASE)
        mdLinkRegex.findAll(body).forEach { match ->
            val name = match.groupValues[1].trim()
            val url = match.groupValues[2].trim()
            list.add(GitHubReleaseAsset(name = name, browserDownloadUrl = url, size = 0L))
        }

        // 2. Matches raw URLs ending in .zip if not already matched
        val rawUrlRegex = Regex("""(https?://[^\s)"]+/(libmtx_[a-zA-Z0-9_]+\.zip))""", RegexOption.IGNORE_CASE)
        rawUrlRegex.findAll(body).forEach { match ->
            val url = match.groupValues[1].trim()
            val name = match.groupValues[2].trim()
            if (list.none { it.name.equals(name, ignoreCase = true) }) {
                list.add(GitHubReleaseAsset(name = name, browserDownloadUrl = url, size = 0L))
            }
        }
        return list
    }

    /**
     * Sanitizes URLs for diagnostic logs by removing query params and sensitive credentials
     */
    fun sanitizeUrlForLogging(url: String): String {
        return try {
            val uri = java.net.URI(url)
            java.net.URI(uri.scheme, uri.authority, uri.path, null, null).toString()
        } catch (_: Exception) {
            url.substringBefore("?")
        }
    }

    suspend fun fetchContentLength(url: String): Long = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .head()
                .header("User-Agent", "Nexus-Resource-Setup/${AppVersionConfig.VERSION_NAME}")
                .build()
            val response = GitHubNetworkModule.okHttpClient.newCall(request).execute()
            val length = response.header("Content-Length")?.toLongOrNull() ?: 0L
            response.close()
            if (length > 0L) return@withContext length

            // Fallback for CDNs that do not supply Content-Length on HEAD: range 0-0
            val rangeRequest = Request.Builder()
                .url(url)
                .header("Range", "bytes=0-0")
                .header("User-Agent", "Nexus-Resource-Setup/${AppVersionConfig.VERSION_NAME}")
                .build()
            val rangeResp = GitHubNetworkModule.okHttpClient.newCall(rangeRequest).execute()
            val contentRange = rangeResp.header("Content-Range")
            val total = contentRange?.substringAfterLast("/")?.toLongOrNull() ?: 0L
            rangeResp.close()
            total
        } catch (e: Exception) {
            Log.w(TAG, "[DIAGNOSTIC] fetchContentLength error: ${e.message}")
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
        val cleanLogUrl = sanitizeUrlForLogging(url)
        Log.i(TAG, "[DIAGNOSTIC] Starting download of $assetName from: $cleanLogUrl")

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Nexus-Resource-Setup/${AppVersionConfig.VERSION_NAME}")
            .build()

        val response = GitHubNetworkModule.okHttpClient.newCall(request).execute()
        Log.i(TAG, "[DIAGNOSTIC] Download response status: ${response.code}")

        if (!response.isSuccessful || response.body == null) {
            Log.e(TAG, "[DIAGNOSTIC] Download request failed: URL=$cleanLogUrl, HTTP status=${response.code}")
            val errorMsg = when (response.code) {
                404 -> "تعذر العثور على ملف الأصل $assetName على الخادم (رمز 404 Not Found)."
                401, 403 -> "تم رفض الوصول أثناء تنزيل الأصل $assetName من GitHub (رمز الخطأ: ${response.code})."
                else -> "تعذر تنزيل حزمة المحرك $assetName من الخادم (رمز الخطأ: ${response.code})."
            }
            throw IllegalStateException(errorMsg)
        }

        val body = response.body!!
        val contentLength = if (totalBytes > 0L) totalBytes else body.contentLength()
        val finalTotal = if (contentLength > 0L) contentLength else 14L * 1024 * 1024
        Log.i(TAG, "[DIAGNOSTIC] Download response status: ${response.code} (Content-Length: $finalTotal)")

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
                        throw IllegalStateException("تم إلغاء عملية التنزيل.")
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

    private fun safeExtractZip(zipFile: File, targetDir: File, targetAbi: String) {
        val zip = ZipFile(zipFile)
        val entries = zip.entries()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val simpleFileName = File(entry.name).name
            if (entry.isDirectory || simpleFileName.isBlank()) continue

            val destFile = File(targetDir, simpleFileName)

            // Prevent Zip Slip vulnerability
            val canonicalDestPath = destFile.canonicalPath
            val canonicalTargetPath = targetDir.canonicalPath
            if (!canonicalDestPath.startsWith(canonicalTargetPath)) {
                throw SecurityException("محاولة غير آمنة لكتابة ملف خارج المسار المسموح: ${entry.name}")
            }

            destFile.parentFile?.mkdirs()
            zip.getInputStream(entry).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.setReadable(true, false)
            destFile.setExecutable(true, false)
            Log.i(TAG, "[DIAGNOSTIC] Extracted library: ${destFile.name} (${destFile.length()} bytes)")

            // Alias mirroring for expected names
            val expectedLibName = if (targetAbi == "arm64-v8a") "libmtx_v8a.so" else "libmtx_v7a.so"
            if (simpleFileName.equals("libolympus_cleaner.so", ignoreCase = true)) {
                val mirrorFile = File(targetDir, expectedLibName)
                if (!mirrorFile.exists()) {
                    runCatching {
                        destFile.copyTo(mirrorFile, overwrite = true)
                        mirrorFile.setReadable(true, false)
                        mirrorFile.setExecutable(true, false)
                        Log.i(TAG, "[DIAGNOSTIC] Created alias mirror: ${mirrorFile.name}")
                    }
                }
            } else if (simpleFileName.equals(expectedLibName, ignoreCase = true)) {
                val olympusFile = File(targetDir, "libolympus_cleaner.so")
                if (!olympusFile.exists()) {
                    runCatching {
                        destFile.copyTo(olympusFile, overwrite = true)
                        olympusFile.setReadable(true, false)
                        olympusFile.setExecutable(true, false)
                        Log.i(TAG, "[DIAGNOSTIC] Created alias mirror: libolympus_cleaner.so")
                    }
                }
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
