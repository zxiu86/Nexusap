package com.example.util

/**
 * Central Single Source of Truth for Application Versioning & Metadata.
 * Whenever a new release is prepared, updating values in this file updates all UI
 * components across the entire app automatically (Settings, Splash/Preload Screen,
 * Update Cards, Badges, Diagnostics, Footer).
 */
object AppVersionConfig {
    const val VERSION_NAME = "2.0.3"
    const val VERSION_CODE = 43
    const val BUILD_CODENAME = "Nexus Titan SUPER"
    const val RELEASE_CHANNEL = "النسخة الرسمية الخارقة (SUPER)"
    const val RELEASE_DATE = "سبتمبر 2026"
    const val AUTHOR_CREDITS = "فريق Nexus"

    /**
     * Short version string: "v2.0.3 SUPER"
     */
    fun getFullVersionString(): String = "v$VERSION_NAME SUPER"

    /**
     * Build identifier: "Build 43"
     */
    fun getFormattedVersionCode(): String = "Build $VERSION_CODE"

    /**
     * Label displayed on the splash / preload screen before the main page opens
     */
    fun getSplashVersionLabel(): String = "الإصدار $VERSION_NAME SUPER • تهيئة المحتوى والمستودع"

    /**
     * Badge text shown in settings card
     */
    fun getSettingsVersionLabel(): String = "v$VERSION_NAME SUPER"

    /**
     * Full footer/copyright string in settings:
     * "الإصدار الرسمي v2.0.3 SUPER (Build 43) • فريق Nexus"
     */
    fun getSettingsFullDetails(): String =
        "الإصدار الرسمي v$VERSION_NAME SUPER (Build $VERSION_CODE) • $AUTHOR_CREDITS"

    /**
     * Current version label in the update status section
     */
    fun getCurrentVersionBadge(): String = "الإصدار الحالي: v$VERSION_NAME SUPER"

    /**
     * Changelog header text
     */
    fun getChangelogHeader(): String = "جديد الإصدار v$VERSION_NAME SUPER:"

    /**
     * Highlights of this release
     */
    val CURRENT_CHANGELOG_FEATURES = listOf(
        "تقسيم صفحة الإعدادات بالكامل إلى صفحات فرعية وتصنيفات داخلية عصرية وفاخرة عالمياً لتجربة تحكم سلسة وفائقة السرعة.",
        "نقل تصميم الفضاء الأسطوري المتوهج إلى خلف شريط الفوتر السفلي ليتنفس ويصعد للأعلى دون التأثير على وضوح وتفاعل الفوتر.",
        "إطلاق الإصدار الخارق 2.0.3 SUPER مع تحسينات أداء شاملة وسرعة استجابة فائقة في التصفح والقراءة.",
        "نظام البلاغات وطلبات الأعمال والميزات الشامل مع إمكانية التخزين المؤقت الذكي وخيار الإرسال الفوري والمزامنة السحابية.",
        "لوحة إدارة وتحكم المشرفين لمراجعة البلاغات والطلبات مع أزرار الموافقة والرفض المباشرة والإشعارات الفورية.",
        "باقة من تدرجات الألوان التجميلية الساحرة (الشفق، الغروب، سايبربانك، أمواج المحيط، لهب التنين، سديم الفضاء) وشريط تقدم 500 فصل."
    )
}
