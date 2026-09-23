package com.example

import com.example.data.network.GitHubNetworkModule
import org.junit.Assert.*
import org.junit.Test

class GitHubSyncUnitTest {

    @Test
    fun testGitHubDefaultConfig() {
        assertEquals("zxiu86", GitHubNetworkModule.DEFAULT_OWNER)
        assertEquals("Data", GitHubNetworkModule.DEFAULT_DATA_REPO)
        assertEquals("main", GitHubNetworkModule.DEFAULT_BRANCH)
        assertEquals("nexusap", GitHubNetworkModule.getAppRepo())
        assertEquals("Data", GitHubNetworkModule.getDataRepo())
    }

    @Test
    fun testVersionMatchesCurrentVersion() {
        assertEquals("2.0.7", com.example.util.AppVersionConfig.VERSION_NAME)
        assertEquals(47, com.example.util.AppVersionConfig.VERSION_CODE)
    }
}
