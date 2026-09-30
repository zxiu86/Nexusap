package com.example.util

/**
 * Central Single Source of Truth for Application Versioning & Metadata.
 * Whenever a new release is prepared, updating values in this file updates all UI
 * components across the entire app automatically (Settings, Splash/Preload Screen,
 * Update Cards, Badges, Diagnostics, Footer).
 */
object AppVersionConfig {
    const val VERSION_NAME = "2.1.3"
    const val VERSION_CODE = 53
    const val BUILD_CODENAME = "Nexus Glassmorphism PRIME"
    const val RELEASE_CHANNEL = "النسخة الرسمية الفائقة (PRIME)"
    const val RELEASE_DATE = "سبتمبر 2026"
    const val AUTHOR_CREDITS = "فريق Nexus"

    /**
     * Short version string: "v2.1.3 PRIME"
     */
    fun getFullVersionString(): String = "v$VERSION_NAME PRIME"

    /**
     * Build identifier: "Build 53"
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
     * "الإصدار الرسمي v2.1.3 PRIME (Build 53) • فريق Nexus"
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
        "إزالة زر المفضلة والنافذة المنبثقة من الهيدر لتصميم انسيابي مريح وبسيط بدون تشتيت.",
        "إضافة زر إيقاف وتشغيل التموج اللوني لشريط الفوتر (Footer Wave Toggle) مع تحكم كامل فوري.",
        "إضافة مفتاح تفعيل المظهر الزجاجي الفاخر الشفاف (Glassmorphism) على الهيدر والفوتر في الرئيسية وشاشة القراءة.",
        "استبدال حرف N في شاشة التحميل التمهيدي الأولى بشعار الأسد الكوني الرسمي الفخم للتطبيق.",
        "ترقية إصدار التطبيق رسمياً إلى 2.1.3 (Build 53) من داخل وخارج التطبيق مع تحسينات شاملة."
    )
}
