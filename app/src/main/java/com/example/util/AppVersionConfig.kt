package com.example.util

/**
 * Central Single Source of Truth for Application Versioning & Metadata.
 * Whenever a new release is prepared, updating values in this file updates all UI
 * components across the entire app automatically (Settings, Splash/Preload Screen,
 * Update Cards, Badges, Diagnostics, Footer).
 */
object AppVersionConfig {
    const val VERSION_NAME = "2.1.0"
    const val VERSION_CODE = 50
    const val BUILD_CODENAME = "Nexus Nova PRIME"
    const val RELEASE_CHANNEL = "النسخة الرسمية الفائقة (PRIME)"
    const val RELEASE_DATE = "سبتمبر 2026"
    const val AUTHOR_CREDITS = "فريق Nexus"

    /**
     * Short version string: "v2.1.0 PRIME"
     */
    fun getFullVersionString(): String = "v$VERSION_NAME PRIME"

    /**
     * Build identifier: "Build 50"
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
     * "الإصدار الرسمي v2.1.0 PRIME (Build 50) • فريق Nexus"
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
        "إضافة نافذة منبثقة تفاعلية مخصصة لمتابعة تنزيل التحديث المباشر من داخل التطبيق لحظة بلحظة بنسبة التقدم والسرعة (MB/s) وحجم الملف المنزّل (MB) مع إمكانية المتابعة بالخلفية أو الإلغاء والتثبيت الفوري.",
        "تحديث وتطوير شامل لصفحة تخصيص المظهر: إعادة ترتيب أنيقة، بطاقات السمات السريعة الجاهزة، استوديو المعاينة الحية المطور 2.0، واستعادة الإعدادات الافتراضية بنقرة واحدة.",
        "شريط عائم ذكي لمتابعة تقدم التنزيل في الخلفية مع إمكانية إعادة فتح لوحة التنزيل في أي وقت.",
        "ترقية ثبات فضاء الفوتر الكوني وسلاسة التموج اللوني وإمكانات تخصيص الأنماط البيضاء والداكنة بدقة فائقة.",
        "ترقية إصدار التطبيق رسمياً إلى 2.1.0 (Build 50) من داخل التطبيق وخارجه مع أعلى معايير الاستقرار والأداء."
    )
}
