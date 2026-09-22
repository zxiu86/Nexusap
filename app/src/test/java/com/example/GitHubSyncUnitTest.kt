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
    }

    @Test
    fun testVersionMatches204() {
        assertEquals("2.0.4", com.example.util.AppVersionConfig.VERSION_NAME)
        assertEquals(44, com.example.util.AppVersionConfig.VERSION_CODE)
    }
}
