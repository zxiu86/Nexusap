package com.example.util

/**
 * Central Single Source of Truth for Application Versioning & Metadata.
 * Whenever a new release is prepared, updating values in this file updates all UI
 * components across the entire app automatically (Settings, Splash/Preload Screen,
 * Update Cards, Badges, Diagnostics, Footer).
 */
object AppVersionConfig {
    const val VERSION_NAME = "2.1.6"
    const val VERSION_CODE = 56
    const val BUILD_CODENAME = "Nexus Glassmorphism PRIME"
    const val RELEASE_CHANNEL = "النسخة الرسمية الفائقة (PRIME)"
    const val RELEASE_DATE = "أكتوبر 2026"
    const val AUTHOR_CREDITS = "فريق Nexus"

    /**
     * Short version string: "v1.2.6 PRIME"
     */
    fun getFullVersionString(): String = "v$VERSION_NAME PRIME"

    /**
     * Build identifier: "Build 56"
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
     * "الإصدار الرسمي v1.2.6 PRIME (Build 56) • فريق Nexus"
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
        "تقوية وإصلاح محرك التنظيف التلقائي الذكي ليعمل لحظياً عبر ذاكرة Coil والمسارات المحلية فوراً.",
        "قائمة منزلقة بتصميم زجاج سائل (Liquid Glass) على غرار آيفون للتبديل بين تنزيل طوابير وتجهيز الكل.",
        "تحسين دقة وسرعة وضع الطبقة البيضاء على كلمات تيمكس، تيم اكس، olympustaff.com، وhttps//:olympustaff.com.",
        "تحديث العداد الخلفي السري وتفعيل نظام المكافأة عند الوصول لـ 500 في الخلفية.",
        "ترقية إصدار التطبيق رسمياً إلى 1.2.6 (Build 56) من داخل وخارج التطبيق."
    )
}
