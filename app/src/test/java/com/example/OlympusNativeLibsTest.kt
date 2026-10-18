package com.example

import com.olympus.cleaner.OlympusCleanerEngine
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OlympusNativeLibsTest {

    @Test
    fun testRealNativeLibrariesPackagingAndElfHeaders() {
        val projectRoot = File(".").canonicalFile
        val jniLibsDir = File(projectRoot, "src/main/jniLibs").takeIf { it.exists() }
            ?: File(projectRoot, "app/src/main/jniLibs")

        assertTrue("jniLibs directory must exist at ${jniLibsDir.absolutePath}", jniLibsDir.exists())

        val arm64Dir = File(jniLibsDir, "arm64-v8a")
        if (arm64Dir.exists()) {
            val arm64So = File(arm64Dir, "libolympus_cleaner.so")
            if (arm64So.exists()) {
                assertTrue("arm64-v8a binary must be a real compiled library (> 10MB)", arm64So.length() > 10 * 1024 * 1024)
                val arm64Header = ByteArray(4)
                FileInputStream(arm64So).use { it.read(arm64Header) }
                assertArrayEquals(byteArrayOf(0x7F.toByte(), 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte()), arm64Header)
            }
        }

        val v7aDir = File(jniLibsDir, "armeabi-v7a")
        if (v7aDir.exists()) {
            val v7aSo = File(v7aDir, "libolympus_cleaner.so")
            if (v7aSo.exists()) {
                assertTrue("armeabi-v7a binary must be a real compiled library (> 10MB)", v7aSo.length() > 10 * 1024 * 1024)
                val v7aHeader = ByteArray(4)
                FileInputStream(v7aSo).use { it.read(v7aHeader) }
                assertArrayEquals(byteArrayOf(0x7F.toByte(), 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte()), v7aHeader)
            }
        }


        // Verify obsolete placeholder files are removed
        val obsoleteArm64 = File(jniLibsDir, "arm64-v8a/libmtxv8aiiii.so")
        assertFalse("Obsolete placeholder file libmtxv8aiiii.so must be removed", obsoleteArm64.exists())

        val obsoleteV7a = File(jniLibsDir, "armeabi-v7a/libmtxv7iiiiia.so")
        assertFalse("Obsolete placeholder file libmtxv7iiiiia.so must be removed", obsoleteV7a.exists())
    }

    @Test
    fun testOlympusCleanerEngineSafeInitialization() {
        // Safe check on Robolectric - doesn't crash when native lib is compiled for arm on x86_64 runner
        assertNotNull(OlympusCleanerEngine.getVersion())
        assertFalse(OlympusCleanerEngine.isNativeAvailable()) // JVM host without arm translation
        assertNotNull(OlympusCleanerEngine.getLoadError())
    }

    @Test
    fun testResourceSetupManagerInitialState() {
        assertNotNull(com.example.util.ResourceSetupManager.setupState.value)
    }
}
