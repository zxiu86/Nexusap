package com.example.util

/**
 * Central Single Source of Truth for Application Versioning & Metadata.
 * Whenever a new release is prepared, updating values in this file updates all UI
 * components across the entire app automatically (Settings, Splash/Preload Screen,
 * Update Cards, Badges, Diagnostics, Footer).
 */
object AppVersionConfig {
    const val VERSION_NAME = "2.1.2"
    const val VERSION_CODE = 52
    const val BUILD_CODENAME = "Nexus Lion PRIME"
    const val RELEASE_CHANNEL = "النسخة الرسمية الفائقة (PRIME)"
    const val RELEASE_DATE = "سبتمبر 2026"
    const val AUTHOR_CREDITS = "فريق Nexus"

    /**
     * Short version string: "v2.1.2 PRIME"
     */
    fun getFullVersionString(): String = "v$VERSION_NAME PRIME"

    /**
     * Build identifier: "Build 52"
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
     * "الإصدار الرسمي v2.1.2 PRIME (Build 52) • فريق Nexus"
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
        "حل مشكلة سجل حفظ القراءة ومتابعة الفصول بدقة تامة: الآن عند القراءة المتتالية لفصول متعددة والخروج، يفتح التطبيق مباشرة في آخر فصل تمت قراءته دون العودة للفصل الأول.",
        "شعار ولوجو كوني جديد فائق الفخامة والأناقة: أسد أوريغامي هندسي كوني مفعم بنجوم وسدم الفضاء مع خلفية سوداء نقية كاملة #000000 بلا حواف أو إطارات.",
        "مزامنة فورية ودقيقة لمسار التنقل بين الفصول (Navigation Backstack Sync) مع حفظ جلسة القراءة النشطة محلياً وسحابياً.",
        "إعادة برمجة وهندسة شريط السمات السريعة الجاهزة بنماذج متناسقة ومطابقة 100% للألوان الحقيقية.",
        "ترقية إصدار التطبيق رسمياً إلى 2.1.2 (Build 52) من داخل وخارج التطبيق مع أعلى معايير الاستقرار والسلاسة."
    )
}
