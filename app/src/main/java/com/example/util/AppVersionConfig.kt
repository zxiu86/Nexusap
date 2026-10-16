package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Central Single Source of Truth for Application Versioning & Metadata.
 * Whenever a new release is prepared, updating values in this file updates all UI
 * components across the entire app automatically (Settings, Splash/Preload Screen,
 * Update Cards, Badges, Diagnostics, Footer).
 */
object AppVersionConfig {
    const val VERSION_NAME = "2.1.7"
    const val VERSION_CODE = 57
    const val BUILD_CODENAME = "Nexus Stellar Cosmic PRIME"
    const val RELEASE_CHANNEL = "النسخة الرسمية الفائقة (PRIME)"
    const val RELEASE_DATE = "أكتوبر 2026"
    const val AUTHOR_CREDITS = "فريق Nexus"
    const val CODER_NAME = "CODER"
    const val CODER_SNAPCHAT_URL = "https://www.snapchat.com/add/zaid09_90?share_id=SVpvc9niKBU&locale=ar-AE"
    const val CODER_SNAPCHAT_URI = "snapchat://add/zaid09_90"

    /**
     * Safely open the CODER Snapchat profile, launching the Snapchat app if available
     * or falling back gracefully to the web browser profile link.
     */
    fun openSnapchatProfile(context: Context) {
        try {
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(CODER_SNAPCHAT_URI)).apply {
                setPackage("com.snapchat.android")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(appIntent)
        } catch (_: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(CODER_SNAPCHAT_URL)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            } catch (_: Exception) {
                // Graceful fallback ignore
            }
        }
    }

    /**
     * Short version string: "v2.1.7 PRIME"
     */
    fun getFullVersionString(): String = "v$VERSION_NAME PRIME"

    /**
     * Build identifier: "Build 57"
     */
    fun getFormattedVersionCode(): String = "Build $VERSION_CODE"

    /**
     * Label displayed on the splash / preload screen before the main page opens
     */
    fun getSplashVersionLabel(): String = "الإصدار $VERSION_NAME PRIME • تهيئة المحتوى والمستودع"

    /**
     * Badge text shown in settings card
     */
    fun getSettingsVersionLabel(): String = "v$VERSION_NAME PRIME"

    /**
     * Full footer/copyright string in settings:
     * "الإصدار الرسمي v2.1.7 PRIME (Build 57) • فريق Nexus"
     */
    fun getSettingsFullDetails(): String =
        "الإصدار الرسمي v$VERSION_NAME PRIME (Build $VERSION_CODE) • $AUTHOR_CREDITS"

    /**
     * Current version label in the update status section
     */
    fun getCurrentVersionBadge(): String = "الإصدار الحالي: v$VERSION_NAME PRIME"

    /**
     * Changelog header text
     */
    fun getChangelogHeader(): String = "جديد الإصدار v$VERSION_NAME PRIME:"

    /**
     * Highlights of this release
     */
    val CURRENT_CHANGELOG_FEATURES = listOf(
        "تأثير فضاء عميق واقعي ومبهر جداً لبطاقة الغلاف الدوارة في صفحة التفاصيل مع شهب ثلاثية الأبعاد ونجوم متلألئة.",
        "إضافة توقيع تفاعلي Created by CODER في صفحة التحديثات وبنهاية الإعدادات ينقلك مباشرة لحساب سناب شات.",
        "ترقية شاملة لإصدار التطبيق رسمياً إلى v2.1.7 PRIME داخلياً وخارجياً مع أداء سلس وخارق.",
        "قائمة منزلقة بتصميم زجاج سائل (Liquid Glass) على غرار آيفون للتبديل بين تنزيل طوابير وتجهيز الكل.",
        "تقوية وإصلاح محرك التنظيف التلقائي للشعارات والعلامات المائية ليعمل بأقصى دقة وسرعة."
    )
}
