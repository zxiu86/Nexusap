package com.example.util

/**
 * Central Single Source of Truth for Application Versioning & Metadata.
 * Whenever a new release is prepared, updating values in this file updates all UI
 * components across the entire app automatically (Settings, Splash/Preload Screen,
 * Update Cards, Badges, Diagnostics, Footer).
 */
object AppVersionConfig {
    const val VERSION_NAME = "2.1.1"
    const val VERSION_CODE = 51
    const val BUILD_CODENAME = "Nexus Cosmic PRIME"
    const val RELEASE_CHANNEL = "النسخة الرسمية الفائقة (PRIME)"
    const val RELEASE_DATE = "سبتمبر 2026"
    const val AUTHOR_CREDITS = "فريق Nexus"

    /**
     * Short version string: "v2.1.1 PRIME"
     */
    fun getFullVersionString(): String = "v$VERSION_NAME PRIME"

    /**
     * Build identifier: "Build 51"
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
     * "الإصدار الرسمي v2.1.1 PRIME (Build 51) • فريق Nexus"
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
        "شعار ولوجو كوني جديد فائق الأناقة بمواصفات أندرويد القياسية: ثعلب هندسي كوني مفعم بنجوم وسدم الفضاء مع خلفية موحدة نظيفة وخالية من الحواف والإطارات.",
        "إعادة برمجة وهندسة شريط السمات السريعة الجاهزة بنماذج مثالية ومطابقة 100% للألوان الحقيقية (ذهب إمبراطوري، أزرق ملكي، زمرد ملكي، بنفسجي كوني، أبيض لؤلؤي، وأزرق بحري).",
        "نافذة منبثقة تفاعلية لمتابعة تنزيل التحديث المباشر من داخل التطبيق لحظة بلحظة بنسبة الإنجاز وسرعة النقل وحجم الملف.",
        "استوديو المعاينة الحية المطور 2.0 مع تحسينات شاملة لترتيب وتصميم صفحة المظهر والتخصيص.",
        "ترقية إصدار التطبيق رسمياً إلى 2.1.1 (Build 51) من داخل وخارج التطبيق مع أداء فائق وسرعة استجابة عالية."
    )
}
