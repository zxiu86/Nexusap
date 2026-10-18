package com.example

import com.example.data.model.GitHubReleaseAsset
import com.example.data.network.GitHubNetworkModule
import com.example.util.ResourceSetupManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubReleaseDiscoveryTest {

    @Test
    fun testParseAssetsFromReleaseBody() {
        val sampleReleaseBody = """
            **Full Changelog**: https://github.com/zxiu86/Nexusap/compare/v2.1.7...0.0.1
            [libmtx_v8a.zip](https://github.com/user-attachments/files/33609140/libmtx_v8a.zip)

            [libmtx_v7a.zip](https://github.com/user-attachments/files/33609160/libmtx_v7a.zip)
        """.trimIndent()

        val parsed = ResourceSetupManager.parseAssetsFromReleaseBody(sampleReleaseBody)
        assertEquals(2, parsed.size)

        val v8a = parsed.firstOrNull { it.name == "libmtx_v8a.zip" }
        assertNotNull("libmtx_v8a.zip must be discovered", v8a)
        assertEquals("https://github.com/user-attachments/files/33609140/libmtx_v8a.zip", v8a?.browserDownloadUrl)

        val v7a = parsed.firstOrNull { it.name == "libmtx_v7a.zip" }
        assertNotNull("libmtx_v7a.zip must be discovered", v7a)
        assertEquals("https://github.com/user-attachments/files/33609160/libmtx_v7a.zip", v7a?.browserDownloadUrl)
    }

    @Test
    fun testSanitizeUrlForLogging() {
        val sensitiveUrl = "https://github.com/zxiu86/Nexusap/releases/download/0.0.1/libmtx_v8a.zip?token=ghp_SECRET12345&user=admin"
        val cleanUrl = ResourceSetupManager.sanitizeUrlForLogging(sensitiveUrl)
        assertEquals("https://github.com/zxiu86/Nexusap/releases/download/0.0.1/libmtx_v8a.zip", cleanUrl)
        assertTrue("Tokens must not be present in sanitized URL", !cleanUrl.contains("ghp_"))
    }

    @Test
    fun testLiveGitHubReleaseDiscoveryAndAssetResolution() = runBlocking {
        val owner = GitHubNetworkModule.getConfiguredOwner()
        val repo = GitHubNetworkModule.getAppRepo()
        val api = GitHubNetworkModule.apiService

        assertEquals("zxiu86", owner)

        val response = api.getReleaseByTag(owner, repo, ResourceSetupManager.TARGET_ENGINE_TAG)
        assertTrue("GitHub API release 0.0.1 call should succeed (HTTP ${response.code()})", response.isSuccessful)

        val release = response.body()
        assertNotNull("Release object must not be null", release)
        assertEquals("0.0.1", release?.tagName)
        assertEquals("Mtxv8a", release?.name)

        // Verify resolution of both required assets either from assets array or markdown attachments
        val combinedAssets = mutableListOf<GitHubReleaseAsset>()
        release?.assets?.let { combinedAssets.addAll(it) }
        combinedAssets.addAll(ResourceSetupManager.parseAssetsFromReleaseBody(release?.body))

        val v8aAsset = combinedAssets.firstOrNull { it.name.equals(ResourceSetupManager.ASSET_NAME_ARM64, ignoreCase = true) }
        assertNotNull("libmtx_v8a.zip must be present in release 0.0.1", v8aAsset)
        assertTrue("Download URL for v8a must be HTTPS", v8aAsset?.browserDownloadUrl?.startsWith("https://") == true)

        val v7aAsset = combinedAssets.firstOrNull { it.name.equals(ResourceSetupManager.ASSET_NAME_ARMEABI_V7A, ignoreCase = true) }
        assertNotNull("libmtx_v7a.zip must be present in release 0.0.1", v7aAsset)
        assertTrue("Download URL for v7a must be HTTPS", v7aAsset?.browserDownloadUrl?.startsWith("https://") == true)

        // Verify dynamic size retrieval via fetchContentLength on the direct download URL
        val sizeV8a = ResourceSetupManager.fetchContentLength(v8aAsset!!.browserDownloadUrl!!)
        assertTrue("v8a asset size must be real and > 10MB ($sizeV8a bytes)", sizeV8a > 10L * 1024 * 1024)

        val sizeV7a = ResourceSetupManager.fetchContentLength(v7aAsset!!.browserDownloadUrl!!)
        assertTrue("v7a asset size must be real and > 10MB ($sizeV7a bytes)", sizeV7a > 10L * 1024 * 1024)
    }
}
