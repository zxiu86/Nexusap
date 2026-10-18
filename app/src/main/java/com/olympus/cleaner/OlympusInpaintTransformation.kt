package com.olympus.cleaner

import android.content.Context
import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation

/**
 * Coil Transformation that routes image bitmaps through the native Olympus Cleaner engine
 * for real-time sub-pixel inpainting before rendering in the reader.
 */
class OlympusInpaintTransformation(
    private val context: Context,
    private val enabled: Boolean = true
) : Transformation {

    override val cacheKey: String = if (enabled && com.example.util.ResourceSetupManager.isSetupCompleted(context) && OlympusCleanerEngine.isNativeAvailable()) {
        "olympus_inpaint_v1_active"
    } else {
        "olympus_inpaint_passthrough"
    }

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        if (!enabled || !com.example.util.ResourceSetupManager.isSetupCompleted(context) || !OlympusCleanerEngine.isNativeAvailable()) {
            return input
        }

        return OlympusCleanerManager.cleanBitmap(context, input)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OlympusInpaintTransformation) return false
        return enabled == other.enabled
    }

    override fun hashCode(): Int {
        return enabled.hashCode()
    }
}
