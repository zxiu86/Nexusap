package com.example.util

/**
 * Central Single Source of Truth for Application Versioning & Metadata.
 * Whenever a new release is prepared, updating values in this file updates all UI
 * components across the entire app automatically (Settings, Splash/Preload Screen,
 * Update Cards, Badges, Diagnostics, Footer).
 */
object AppVersionConfig {
    const val VERSION_NAME = "2.0.5"
    const val VERSION_CODE = 45
    const val BUILD_CODENAME = "Nexus Titan SUPER"
    const val RELEASE_CHANNEL = "النسخة الرسمية الخارقة (SUPER)"
    const val RELEASE_DATE = "سبتمبر 2026"
    const val AUTHOR_CREDITS = "فريق Nexus"

    /**
     * Short version string: "v2.0.5 SUPER"
     */
    fun getFullVersionString(): String = "v$VERSION_NAME SUPER"

    /**
     * Build identifier: "Build 45"
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
     * "الإصدار الرسمي v2.0.5 SUPER (Build 45) • فريق Nexus"
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
        "إمكانية تخصيص سرعة مرور التموج اللوني وسرعة ظهوره في كل دورة لشريط الفوتر السفلي بحرية تامة.",
        "إمكانية اختيار لون التموج اللوني لشريط الفوتر مع تشكيلة ألوان نيون وكريستالية متوهجة.",
        "إضافة سمة اللون الأبيض الأنيق والمريح للتطبيق لتجربة قراءة وتصفح نقية ومريحة للعين.",
        "إعادة تصميم شاملة وأكثر ديناميكية لصفحة تخصيص المظهر في الإعدادات مع لوحة معاينة حية وتوزيع أرحب للبطاقات.",
        "ترقية إصدار التطبيق إلى 2.0.5 SUPER مع تحسينات في استقرار الأداء وسلاسة الحركات والتنقل."
    )
}
