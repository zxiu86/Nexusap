package com.example.util

import android.app.DownloadManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

enum class UpdateDownloadStatus {
    IDLE,
    CONNECTING,
    DOWNLOADING,
    VERIFYING,
    COMPLETED,
    ERROR,
    CANCELLED
}

data class UpdateDownloadProgressState(
    val status: UpdateDownloadStatus = UpdateDownloadStatus.IDLE,
    val progress: Float = 0f, // 0.0 to 1.0
    val progressPercent: Int = 0, // 0 to 100
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val speedKbps: Double = 0.0,
    val estimatedRemainingSeconds: Long = 0L,
    val downloadedApkFile: File? = null,
    val downloadUrl: String = "",
    val versionName: String = "",
    val errorMessage: String? = null,
    val isDialogVisible: Boolean = false
) {
    val formattedDownloadedSize: String
        get() = formatBytes(bytesDownloaded)

    val formattedTotalSize: String
        get() = if (totalBytes > 0) formatBytes(totalBytes) else "-- MB"

    val formattedSpeed: String
        get() = when {
            speedKbps >= 1024 -> String.format("%.2f MB/s", speedKbps / 1024.0)
            speedKbps > 0 -> String.format("%.0f KB/s", speedKbps)
            else -> "جاري الحساب..."
        }

    private fun formatBytes(bytes: Long): String {
        val mb = bytes.toDouble() / (1024.0 * 1024.0)
        return String.format("%.1f MB", mb)
    }
}

object InAppUpdateManager {

    private const val TAG = "InAppUpdateManager"
    private const val PREFS_NAME = "nexus_update_prefs"
    private const val KEY_PENDING_APK_PATH = "pending_apk_path"

    private val _downloadState = MutableStateFlow(UpdateDownloadProgressState())
    val downloadState: StateFlow<UpdateDownloadProgressState> = _downloadState.asStateFlow()

    private var currentDownloadJob: Job? = null

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    private fun showToast(context: Context, message: String, isLong: Boolean = false) {
        mainHandler.post {
            Toast.makeText(
                context.applicationContext,
                message,
                if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Checks if the app has permission to install packages from unknown sources (Android 8.0+)
     */
    fun canInstallPackages(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Opens system settings to allow installing apps from this source
     */
    fun requestInstallPermission(context: Context, pendingApkFile: File? = null) {
        if (pendingApkFile != null && pendingApkFile.exists()) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_PENDING_APK_PATH, pendingApkFile.absolutePath).apply()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open install permissions settings", e)
            }
        }
    }

    /**
     * Checks if there is a pending APK waiting to be installed after permission is granted
     */
    fun checkPendingInstall(context: Context) {
        if (!canInstallPackages(context)) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val pendingPath = prefs.getString(KEY_PENDING_APK_PATH, null) ?: return
        val apkFile = File(pendingPath)
        if (apkFile.exists() && isValidApkZip(apkFile)) {
            prefs.edit().remove(KEY_PENDING_APK_PATH).apply()
            installApk(context, apkFile)
        }
    }

    /**
     * Checks if the file is a valid Zip/APK file by checking its magic header (PK\x03\x04)
     */
    fun isValidApkZip(file: File): Boolean {
        if (!file.exists() || file.length() < 1024 * 1024) return false
        return try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(4)
                val read = fis.read(header)
                read == 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() &&
                        (header[2] == 0x03.toByte() || header[2] == 0x05.toByte() || header[2] == 0x07.toByte()) &&
                        (header[3] == 0x04.toByte() || header[3] == 0x06.toByte() || header[3] == 0x08.toByte())
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Resolves the best directory to store the APK for installation.
     */
    private fun getUpdateStorageDir(context: Context): File {
        val extDownloads = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (extDownloads != null && (extDownloads.exists() || extDownloads.mkdirs())) {
            return extDownloads
        }
        val extCache = context.externalCacheDir
        if (extCache != null && (extCache.exists() || extCache.mkdirs())) {
            return extCache
        }
        val internalUpdates = File(context.filesDir, "app_updates")
        if (internalUpdates.exists() || internalUpdates.mkdirs()) {
            return internalUpdates
        }
        return context.cacheDir
    }

    /**
     * Starts downloading the update APK with real-time reactive progress updates.
     */
    fun startApkDownload(context: Context, downloadUrl: String, versionName: String) {
        if (downloadUrl.isBlank()) {
            showToast(context, "رابط التحديث غير متوفر حالياً")
            return
        }

        // Cancel previous job if running
        currentDownloadJob?.cancel()

        val appContext = context.applicationContext

        _downloadState.value = UpdateDownloadProgressState(
            status = UpdateDownloadStatus.CONNECTING,
            progress = 0f,
            progressPercent = 0,
            bytesDownloaded = 0L,
            totalBytes = 0L,
            speedKbps = 0.0,
            downloadUrl = downloadUrl,
            versionName = versionName,
            isDialogVisible = true
        )

        currentDownloadJob = CoroutineScope(Dispatchers.IO).launch {
            var tempApk: File? = null
            var targetApk: File? = null

            try {
                val updateDir = getUpdateStorageDir(appContext)
                targetApk = File(updateDir, "nexus_v$versionName.apk")
                tempApk = File(updateDir, "nexus_v$versionName.apk.tmp")

                if (tempApk.exists()) tempApk.delete()
                if (targetApk.exists()) targetApk.delete()

                val request = Request.Builder()
                    .url(downloadUrl)
                    .header("User-Agent", "Mozilla/5.0 (Android; Nexus-Updater)")
                    .header("Accept", "*/*")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful || response.body == null) {
                    throw IllegalStateException("فشل الاتصال بالخادم (HTTP ${response.code})")
                }

                val body = response.body!!
                val contentLength = body.contentLength().coerceAtLeast(0L)
                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(tempApk)

                _downloadState.value = _downloadState.value.copy(
                    status = UpdateDownloadStatus.DOWNLOADING,
                    totalBytes = contentLength
                )

                val buffer = ByteArray(64 * 1024)
                var bytesRead: Int
                var totalBytesRead = 0L

                var lastSpeedCalcTime = System.currentTimeMillis()
                var bytesSinceLastSpeedCalc = 0L
                var currentSpeedKbps = 0.0

                try {
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        if (!isActive) {
                            throw kotlinx.coroutines.CancellationException("تم إلغاء التنزيل من قبل المستخدم")
                        }

                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        bytesSinceLastSpeedCalc += bytesRead

                        val now = System.currentTimeMillis()
                        val timeDiff = now - lastSpeedCalcTime
                        if (timeDiff >= 300) {
                            val seconds = timeDiff / 1000.0
                            currentSpeedKbps = (bytesSinceLastSpeedCalc / 1024.0) / seconds
                            lastSpeedCalcTime = now
                            bytesSinceLastSpeedCalc = 0L

                            val progressFraction = if (contentLength > 0) {
                                (totalBytesRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                            } else 0f
                            val percent = (progressFraction * 100).toInt()

                            val remainingBytes = (contentLength - totalBytesRead).coerceAtLeast(0L)
                            val remainingSec = if (currentSpeedKbps > 0) {
                                ((remainingBytes / 1024.0) / currentSpeedKbps).toLong()
                            } else 0L

                            _downloadState.value = _downloadState.value.copy(
                                progress = progressFraction,
                                progressPercent = percent,
                                bytesDownloaded = totalBytesRead,
                                speedKbps = currentSpeedKbps,
                                estimatedRemainingSeconds = remainingSec
                            )
                        }
                    }
                    outputStream.flush()
                } finally {
                    try { outputStream.close() } catch (_: Exception) {}
                    try { inputStream.close() } catch (_: Exception) {}
                    try { body.close() } catch (_: Exception) {}
                }

                // Verify file integrity
                _downloadState.value = _downloadState.value.copy(
                    status = UpdateDownloadStatus.VERIFYING,
                    progress = 1f,
                    progressPercent = 100,
                    bytesDownloaded = totalBytesRead
                )

                if (!isValidApkZip(tempApk)) {
                    tempApk.delete()
                    throw IllegalStateException("الملف المنزل غير مكتمل أو تالف (${totalBytesRead / 1024} KB)")
                }

                // Rename to target APK
                if (!tempApk.renameTo(targetApk)) {
                    tempApk.copyTo(targetApk, overwrite = true)
                    tempApk.delete()
                }

                targetApk.setReadable(true, false)

                // Verify with PackageManager
                val packageInfo = appContext.packageManager.getPackageArchiveInfo(targetApk.absolutePath, PackageManager.GET_ACTIVITIES)
                    ?: appContext.packageManager.getPackageArchiveInfo(targetApk.absolutePath, 0)

                if (packageInfo == null) {
                    targetApk.delete()
                    throw IllegalStateException("حزمة التحديث غير متوافقة مع هذا الجهاز")
                }

                Log.d(TAG, "APK successfully downloaded & verified: ${targetApk.absolutePath}")

                _downloadState.value = _downloadState.value.copy(
                    status = UpdateDownloadStatus.COMPLETED,
                    progress = 1f,
                    progressPercent = 100,
                    downloadedApkFile = targetApk,
                    bytesDownloaded = targetApk.length(),
                    totalBytes = targetApk.length()
                )

                withContext(Dispatchers.Main) {
                    showToast(appContext, "اكتمل تنزيل التحديث بنجاح! جاري التثبيت...")
                    installApk(appContext, targetApk)
                }

            } catch (e: kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Download cancelled by user")
                tempApk?.delete()
                _downloadState.value = _downloadState.value.copy(
                    status = UpdateDownloadStatus.CANCELLED,
                    errorMessage = "تم إلغاء التنزيل"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Direct download failed", e)
                tempApk?.delete()
                _downloadState.value = _downloadState.value.copy(
                    status = UpdateDownloadStatus.ERROR,
                    errorMessage = e.localizedMessage ?: "حدث خطأ غير متوقع أثناء التنزيل"
                )
            }
        }
    }

    /**
     * Cancels the active download job
     */
    fun cancelDownload() {
        currentDownloadJob?.cancel()
        _downloadState.value = _downloadState.value.copy(
            status = UpdateDownloadStatus.CANCELLED,
            errorMessage = "تم إلغاء التنزيل"
        )
    }

    /**
     * Hides the progress dialog while keeping the background download alive
     */
    fun hideDownloadDialog() {
        _downloadState.value = _downloadState.value.copy(isDialogVisible = false)
    }

    /**
     * Shows the download progress dialog again
     */
    fun showDownloadDialog() {
        _downloadState.value = _downloadState.value.copy(isDialogVisible = true)
    }

    /**
     * Resets the download state to IDLE and closes dialog
     */
    fun dismissDownload() {
        currentDownloadJob?.cancel()
        _downloadState.value = UpdateDownloadProgressState(isDialogVisible = false)
    }

    /**
     * Launches Android Package Installer to install the verified APK
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                showToast(context, "ملف التحديث غير موجود")
                return
            }

            if (!isValidApkZip(apkFile)) {
                showToast(context, "ملف التحديث تالف أو غير مكتمل، يرجى إعادة التحميل", true)
                return
            }

            if (!canInstallPackages(context)) {
                showToast(context, "يرجى تفعيل خيار تثبيت التطبيقات غير المعروفة للمتابعة", true)
                requestInstallPermission(context, apkFile)
                return
            }

            apkFile.setReadable(true, false)

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                clipData = ClipData.newRawUri("nexus_apk", apkUri)
            }

            val knownInstallers = listOf(
                "com.google.android.packageinstaller",
                "com.android.packageinstaller",
                "com.google.android.apps.packageinstaller",
                "com.samsung.android.packageinstaller"
            )
            for (pkg in knownInstallers) {
                try {
                    context.grantUriPermission(pkg, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {}
            }

            val resolvedActivities = context.packageManager.queryIntentActivities(
                installIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            for (resolveInfo in resolvedActivities) {
                val pkg = resolveInfo.activityInfo.packageName
                try {
                    context.grantUriPermission(pkg, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (_: Exception) {}
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            showToast(context, "تعذر فتح مثبت الحزم: ${e.message}", true)
        }
    }

    /**
     * Direct download fallback via external web browser
     */
    fun openDownloadInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            showToast(context, "تعذر فتح المتصفح: ${e.message}")
        }
    }
}
