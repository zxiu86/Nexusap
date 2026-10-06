package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.ChapterCoordinatesDto
import com.example.data.network.GitHubNetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap

data class PendingChapterUpload(
    val seriesSlug: String,
    val chapterNumber: Int,
    val jsonContent: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Manages coordinates storage, checking, and batch-uploading to:
 * https://github.com/zxiu86/Coordinates
 *
 * Folder organization:
 * {seriesSlug}/{chapterNumber}/coordinates.json
 *
 * Performance and Rate-Limit Protection:
 * - Immediate local caching so the active reader sees white layers instantly.
 * - Queueing up to 30 chapters before pushing a single atomic commit via GitHub Git Tree API.
 */
class CoordinatesRepository(private val context: Context) {

    private val TAG = "CoordinatesRepo"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val queueMutex = Mutex()

    private val memoryCache = ConcurrentHashMap<String, ChapterCoordinatesDto>()
    private val diskCacheDir = File(context.cacheDir, "nexus_coords_cache").apply {
        if (!exists()) mkdirs()
    }
    private val queueFile = File(context.filesDir, "pending_coords_batch_queue.json")

    private val _pendingQueueCount = MutableStateFlow(0)
    val pendingQueueCount: StateFlow<Int> = _pendingQueueCount.asStateFlow()

    companion object {
        const val COORDINATES_REPO = "Coordinates"
        const val COORDINATES_OWNER = "zxiu86"
        const val COORDINATES_BRANCH = "main"
        const val BATCH_COMMIT_SIZE = 30

        @Volatile
        private var instance: CoordinatesRepository? = null

        fun getInstance(context: Context): CoordinatesRepository {
            return instance ?: synchronized(this) {
                instance ?: CoordinatesRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        // Initialize pending count from disk
        updatePendingCount()
    }

    private fun getCacheKey(seriesSlug: String, chapterNumber: Int) = "${seriesSlug}_$chapterNumber"

    /**
     * Checks if a chapter has already been cleaned.
     * True if coordinates are present locally or on GitHub.
     */
    suspend fun isChapterCleaned(seriesSlug: String, chapterNumber: Int): Boolean = withContext(Dispatchers.IO) {
        val coords = getCoordinates(seriesSlug, chapterNumber)
        coords != null
    }

    /**
     * Retrieves coordinates for a specific chapter:
     * 1. Memory cache
     * 2. Disk cache
     * 3. Remote GitHub repository (https://github.com/zxiu86/Coordinates)
     */
    suspend fun getCoordinates(seriesSlug: String, chapterNumber: Int): ChapterCoordinatesDto? = withContext(Dispatchers.IO) {
        val cacheKey = getCacheKey(seriesSlug, chapterNumber)
        memoryCache[cacheKey]?.let { return@withContext it }

        // 1. Check Disk Cache
        val diskFile = File(diskCacheDir, "coords_${seriesSlug}_${chapterNumber}.json")
        if (diskFile.exists() && diskFile.length() > 0) {
            try {
                val json = diskFile.readText()
                val parsed = ChapterCoordinatesDto.fromJsonString(json)
                if (parsed != null) {
                    memoryCache[cacheKey] = parsed
                    return@withContext parsed
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading disk coords: ${e.message}")
            }
        }

        // 2. Check Remote GitHub Repository (zxiu86/Coordinates)
        val remote = fetchFromGitHub(seriesSlug, chapterNumber)
        if (remote != null) {
            saveToCache(remote)
            return@withContext remote
        }

        null
    }

    /**
     * Fetches from https://github.com/zxiu86/Coordinates
     * Checking organized folder structure:
     * - {seriesSlug}/{chapterNumber}/coordinates.json
     * - {seriesSlug}/{chapterNumber}.json
     * - coordinates/{seriesSlug}/{chapterNumber}.json
     */
    private fun fetchFromGitHub(seriesSlug: String, chapterNumber: Int): ChapterCoordinatesDto? {
        val owner = COORDINATES_OWNER
        val repo = COORDINATES_REPO
        val branch = COORDINATES_BRANCH

        val pathsToTry = listOf(
            "$seriesSlug/$chapterNumber/coordinates.json",
            "$seriesSlug/$chapterNumber.json",
            "coordinates/$seriesSlug/$chapterNumber.json"
        )

        for (path in pathsToTry) {
            // Mirror 1: Direct GitHub raw fetch
            val rawUrl = "https://raw.githubusercontent.com/$owner/$repo/$branch/$path"
            val rawContent = GitHubNetworkModule.fetchDirectRaw(rawUrl, forceFresh = false)
            if (!rawContent.isNullOrBlank()) {
                val dto = ChapterCoordinatesDto.fromJsonString(rawContent)
                if (dto != null) return dto
            }

            // Mirror 2: GitHub API Content
            try {
                val response = kotlinx.coroutines.runBlocking {
                    GitHubNetworkModule.apiService.getContentRaw(
                        owner = owner,
                        repo = repo,
                        path = path,
                        branch = branch
                    )
                }
                if (response.isSuccessful && response.body() != null) {
                    val bodyStr = response.body()!!.string()
                    val dto = ChapterCoordinatesDto.fromJsonString(bodyStr)
                    if (dto != null) return dto
                }
            } catch (_: Exception) {}
        }

        return null
    }

    /**
     * Saves coordinates into local memory and disk caches immediately
     */
    fun saveToCache(dto: ChapterCoordinatesDto) {
        val cacheKey = getCacheKey(dto.seriesSlug, dto.chapterNumber)
        memoryCache[cacheKey] = dto
        try {
            val diskFile = File(diskCacheDir, "coords_${dto.seriesSlug}_${dto.chapterNumber}.json")
            diskFile.writeText(dto.toJsonString())
        } catch (e: Exception) {
            Log.w(TAG, "Failed saving coordinates to disk: ${e.message}")
        }
    }

    /**
     * Enqueues a cleaned chapter into the batch queue.
     * When the queue reaches 30 chapters, automatically commits all 30 in a single GitHub commit!
     */
    fun enqueueCleanedChapter(dto: ChapterCoordinatesDto) {
        saveToCache(dto)
        scope.launch {
            queueMutex.withLock {
                val queue = readPendingQueueFromDisk()

                // Avoid duplicate entries in the pending queue
                val exists = queue.any { it.seriesSlug == dto.seriesSlug && it.chapterNumber == dto.chapterNumber }
                if (!exists) {
                    queue.add(
                        PendingChapterUpload(
                            seriesSlug = dto.seriesSlug,
                            chapterNumber = dto.chapterNumber,
                            jsonContent = dto.toJsonString(2)
                        )
                    )
                    writePendingQueueToDisk(queue)
                    _pendingQueueCount.value = queue.size
                    Log.d(TAG, "Enqueued chapter ${dto.chapterNumber} of ${dto.seriesSlug}. Pending queue: ${queue.size}/$BATCH_COMMIT_SIZE")
                }

                // Check if queue has reached batch threshold (30 chapters)
                if (queue.size >= BATCH_COMMIT_SIZE) {
                    Log.d(TAG, "Pending queue reached $BATCH_COMMIT_SIZE chapters. Triggering atomic batch commit...")
                    val batchToUpload = queue.take(BATCH_COMMIT_SIZE)
                    val result = executeSingleCommitBatch(batchToUpload)
                    if (result.isSuccess) {
                        queue.removeAll(batchToUpload)
                        writePendingQueueToDisk(queue)
                        _pendingQueueCount.value = queue.size
                        Log.d(TAG, "✅ Successfully committed batch of ${batchToUpload.size} chapters in ONE commit! Remaining: ${queue.size}")
                    } else {
                        Log.w(TAG, "Batch commit failed: ${result.exceptionOrNull()?.message}. Will retry with next batch.")
                    }
                }
            }
        }
    }

    /**
     * Manually triggers a batch commit of all pending chapters (even if < 30).
     */
    suspend fun flushPendingBatchNow(): Result<Int> = withContext(Dispatchers.IO) {
        queueMutex.withLock {
            val queue = readPendingQueueFromDisk()
            if (queue.isEmpty()) {
                return@withContext Result.success(0)
            }

            val batchToUpload = queue.take(BATCH_COMMIT_SIZE)
            val result = executeSingleCommitBatch(batchToUpload)
            if (result.isSuccess) {
                queue.removeAll(batchToUpload)
                writePendingQueueToDisk(queue)
                _pendingQueueCount.value = queue.size
                Result.success(batchToUpload.size)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error committing batch"))
            }
        }
    }

    /**
     * Executes atomic multi-file commit using GitHub Git Database API:
     * 1. GET /repos/{owner}/{repo}/git/ref/heads/{branch} -> get HEAD commit SHA
     * 2. GET /repos/{owner}/{repo}/git/commits/{commit_sha} -> get Base Tree SHA
     * 3. POST /repos/{owner}/{repo}/git/trees -> create new Tree with all files
     * 4. POST /repos/{owner}/{repo}/git/commits -> create new Commit
     * 5. PATCH /repos/{owner}/{repo}/git/refs/heads/{branch} -> point branch to new Commit
     */
    private fun executeSingleCommitBatch(batch: List<PendingChapterUpload>): Result<String> {
        val token = GitHubNetworkModule.getActiveToken()
        if (token.isBlank()) {
            return Result.failure(IllegalStateException("No GitHub token configured. Pending chapters saved locally."))
        }

        val owner = COORDINATES_OWNER
        val repo = COORDINATES_REPO
        val branch = COORDINATES_BRANCH

        val authHeader = when {
            token.startsWith("Bearer ") || token.startsWith("token ") -> token
            else -> "Bearer $token"
        }

        val jsonMediaType = "application/json; charset=utf-8".toMediaType()

        try {
            // 1. Get HEAD commit SHA
            val refUrl = "https://api.github.com/repos/$owner/$repo/git/ref/heads/$branch"
            val refReq = Request.Builder()
                .url(refUrl)
                .header("Authorization", authHeader)
                .header("User-Agent", "Nexus-Manga-App-Android/2.1.5")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .build()

            val refResp = GitHubNetworkModule.okHttpClient.newCall(refReq).execute()
            if (!refResp.isSuccessful || refResp.body == null) {
                return Result.failure(Exception("Failed getting branch ref: HTTP ${refResp.code}"))
            }

            val refJson = JSONObject(refResp.body!!.string())
            val headCommitSha = refJson.getJSONObject("object").getString("sha")

            // 2. Get Base Tree SHA from HEAD commit
            val commitUrl = "https://api.github.com/repos/$owner/$repo/git/commits/$headCommitSha"
            val commitReq = Request.Builder()
                .url(commitUrl)
                .header("Authorization", authHeader)
                .header("User-Agent", "Nexus-Manga-App-Android/2.1.5")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .build()

            val commitResp = GitHubNetworkModule.okHttpClient.newCall(commitReq).execute()
            if (!commitResp.isSuccessful || commitResp.body == null) {
                return Result.failure(Exception("Failed getting base tree: HTTP ${commitResp.code}"))
            }

            val commitJson = JSONObject(commitResp.body!!.string())
            val baseTreeSha = commitJson.getJSONObject("tree").getString("sha")

            // 3. Build Git Tree with all batch files
            // Format: {seriesSlug}/{chapterNumber}/coordinates.json
            val treeArray = JSONArray()
            for (item in batch) {
                val filePath = "${item.seriesSlug}/${item.chapterNumber}/coordinates.json"
                val fileNode = JSONObject().apply {
                    put("path", filePath)
                    put("mode", "100644")
                    put("type", "blob")
                    put("content", item.jsonContent)
                }
                treeArray.put(fileNode)
            }

            val treeBodyJson = JSONObject().apply {
                put("base_tree", baseTreeSha)
                put("tree", treeArray)
            }

            val treeUrl = "https://api.github.com/repos/$owner/$repo/git/trees"
            val treeReq = Request.Builder()
                .url(treeUrl)
                .header("Authorization", authHeader)
                .header("User-Agent", "Nexus-Manga-App-Android/2.1.5")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .post(treeBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val treeResp = GitHubNetworkModule.okHttpClient.newCall(treeReq).execute()
            if (!treeResp.isSuccessful || treeResp.body == null) {
                return Result.failure(Exception("Failed creating Git tree: HTTP ${treeResp.code}"))
            }

            val treeJson = JSONObject(treeResp.body!!.string())
            val newTreeSha = treeJson.getString("sha")

            // 4. Create new Commit referencing new Tree
            val commitMsg = "Batch clean watermark coordinates for ${batch.size} chapters [Nexus Bot 2.1.5]"
            val newCommitBodyJson = JSONObject().apply {
                put("message", commitMsg)
                put("tree", newTreeSha)
                put("parents", JSONArray().put(headCommitSha))
            }

            val createCommitUrl = "https://api.github.com/repos/$owner/$repo/git/commits"
            val createCommitReq = Request.Builder()
                .url(createCommitUrl)
                .header("Authorization", authHeader)
                .header("User-Agent", "Nexus-Manga-App-Android/2.1.5")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .post(newCommitBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val newCommitResp = GitHubNetworkModule.okHttpClient.newCall(createCommitReq).execute()
            if (!newCommitResp.isSuccessful || newCommitResp.body == null) {
                return Result.failure(Exception("Failed creating Git commit: HTTP ${newCommitResp.code}"))
            }

            val newCommitJson = JSONObject(newCommitResp.body!!.string())
            val newCommitSha = newCommitJson.getString("sha")

            // 5. Update Branch Ref to new Commit SHA
            val updateRefBodyJson = JSONObject().apply {
                put("sha", newCommitSha)
                put("force", false)
            }

            val updateRefUrl = "https://api.github.com/repos/$owner/$repo/git/refs/heads/$branch"
            val updateRefReq = Request.Builder()
                .url(updateRefUrl)
                .header("Authorization", authHeader)
                .header("User-Agent", "Nexus-Manga-App-Android/2.1.5")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .patch(updateRefBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val updateRefResp = GitHubNetworkModule.okHttpClient.newCall(updateRefReq).execute()
            if (!updateRefResp.isSuccessful) {
                return Result.failure(Exception("Failed updating branch reference: HTTP ${updateRefResp.code}"))
            }

            Log.d(TAG, "Batch commit completed successfully: $newCommitSha")
            return Result.success(newCommitSha)
        } catch (e: Exception) {
            Log.e(TAG, "Error in executeSingleCommitBatch: ${e.message}", e)
            return Result.failure(e)
        }
    }

    private fun readPendingQueueFromDisk(): MutableList<PendingChapterUpload> {
        val list = mutableListOf<PendingChapterUpload>()
        if (!queueFile.exists() || queueFile.length() == 0L) return list

        try {
            val text = queueFile.readText()
            val arr = JSONArray(text)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    PendingChapterUpload(
                        seriesSlug = obj.getString("slug"),
                        chapterNumber = obj.getInt("chapter"),
                        jsonContent = obj.getString("content"),
                        timestamp = obj.optLong("time", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reading pending queue: ${e.message}")
        }
        return list
    }

    private fun writePendingQueueToDisk(queue: List<PendingChapterUpload>) {
        try {
            val arr = JSONArray()
            for (item in queue) {
                val obj = JSONObject().apply {
                    put("slug", item.seriesSlug)
                    put("chapter", item.chapterNumber)
                    put("content", item.jsonContent)
                    put("time", item.timestamp)
                }
                arr.put(obj)
            }
            queueFile.writeText(arr.toString())
        } catch (e: Exception) {
            Log.w(TAG, "Error writing pending queue: ${e.message}")
        }
    }

    private fun updatePendingCount() {
        scope.launch {
            queueMutex.withLock {
                _pendingQueueCount.value = readPendingQueueFromDisk().size
            }
        }
    }

    fun clearCache() {
        memoryCache.clear()
        try {
            diskCacheDir.deleteRecursively()
            diskCacheDir.mkdirs()
        } catch (e: Exception) {
            Log.w(TAG, "Failed clearing coords cache: ${e.message}")
        }
    }
}
