package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.AppVersionConfig
import com.example.util.ResourceSetupManager
import com.example.util.WatermarkTestHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WatermarkCleanerLabTest {

    @Test
    fun testVersionBumpToClean() {
        assertEquals("2.2.0 clean", AppVersionConfig.VERSION_NAME)
        assertEquals(60, AppVersionConfig.VERSION_CODE)
    }

    @Test
    fun testCreateMangaTestBitmap() {
        val testBmp = WatermarkTestHelper.createMangaTestBitmap("https://olympustaff.com")
        assertNotNull("Test bitmap must not be null", testBmp)
        assertEquals(480, testBmp.width)
        assertEquals(300, testBmp.height)
    }

    @Test
    fun testRunWatermarkCleaningTest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testBmp = WatermarkTestHelper.createMangaTestBitmap("https://olympustaff.com")

        val result = WatermarkTestHelper.runWatermarkCleaningTest(
            context = context,
            inputBitmap = testBmp,
            watermarkText = "https://olympustaff.com"
        )

        assertNotNull(result)
        assertNotNull(result.cleanedBitmap)
        assertEquals(480, result.cleanedBitmap.width)
        assertEquals(300, result.cleanedBitmap.height)
        assertTrue("Watermark must be marked as removed", result.wasWatermarkRemoved)
        assertTrue("Latency must be positive", result.latencyMs >= 0L)
        assertNotNull(result.nativeStatus)
    }

    @Test
    fun testExtractedLibDiagnostic() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val diag = ResourceSetupManager.getExtractedLibDiagnostic(context)
        assertNotNull(diag)
        assertNotNull(diag.fileName)
        assertNotNull(diag.filePath)
        assertNotNull(diag.fileSizeFormatted)
    }

    @Test
    fun testZipDecompressionAndSoPlacement() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val targetDir = context.getDir("nexus_native_libs", Context.MODE_PRIVATE)
        targetDir.mkdirs()

        // Create a synthetic zip with libolympus_cleaner.so containing ELF magic bytes
        val testZip = File(context.cacheDir, "test_engine.zip")
        ZipOutputStream(FileOutputStream(testZip)).use { zos ->
            val entry = ZipEntry("libolympus_cleaner.so")
            zos.putNextEntry(entry)
            // ELF header bytes 0x7F, 'E', 'L', 'F' + dummy bytes
            val elfMagic = byteArrayOf(0x7F.toByte(), 'E'.code.toByte(), 'L'.code.toByte(), 'F'.code.toByte())
            zos.write(elfMagic)
            zos.write(ByteArray(1024))
            zos.closeEntry()
        }

        // Decompress using standard method
        java.util.zip.ZipFile(testZip).use { zip ->
            val entry = zip.getEntry("libolympus_cleaner.so")
            assertNotNull(entry)
            val outFile = File(targetDir, "libolympus_cleaner.so")
            zip.getInputStream(entry).use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            outFile.setReadable(true, false)
            outFile.setExecutable(true, false)
        }
        testZip.delete()

        val diag = ResourceSetupManager.getExtractedLibDiagnostic(context)
        assertTrue("Extracted file must exist", diag.fileExists)
        assertTrue("Extracted file must have valid ELF header", diag.hasValidElfHeader)
        assertTrue("Extracted file must be readable", diag.isReadable)
    }
}
