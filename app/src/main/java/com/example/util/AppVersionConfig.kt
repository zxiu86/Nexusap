package com.example.util

/**
 * Central Single Source of Truth for Application Versioning & Metadata.
 * Whenever a new release is prepared, updating values in this file updates all UI
 * components across the entire app automatically (Settings, Splash/Preload Screen,
 * Update Cards, Badges, Diagnostics, Footer).
 */
object AppVersionConfig {
    const val VERSION_NAME = "2.0.4"
    const val VERSION_CODE = 44
    const val BUILD_CODENAME = "Nexus Titan SUPER"
    const val RELEASE_CHANNEL = "النسخة الرسمية الخارقة (SUPER)"
    const val RELEASE_DATE = "سبتمبر 2026"
    const val AUTHOR_CREDITS = "فريق Nexus"

    /**
     * Short version string: "v2.0.4 SUPER"
     */
    fun getFullVersionString(): String = "v$VERSION_NAME SUPER"

    /**
     * Build identifier: "Build 44"
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
     * "الإصدار الرسمي v2.0.4 SUPER (Build 44) • فريق Nexus"
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
        "تكيّف ديناميكي كامل وتلقائي لنافذة تسجيل الدخول/إنشاء الحساب مع الثيم والألوان التجميلية المختارة في الإعدادات.",
        "أنيميشن تموج سريع وانسيابي على شريط الفوتر السفلي يظهر ويختفي دورياً كل 3 ثوانٍ متناغماً مع الألوان التجميلية للمستخدم.",
        "تبسيط وتجميل الهيدر العلوي بإزالة زر إعادة التحميل مع الإبقاء التام على ميزة السحب للأسفل للتحديث (Pull-to-Refresh).",
        "ترقية إصدار التطبيق إلى 2.0.4 SUPER مع تحسينات في استقرار الواجهة والأداء وسرعة الاستجابة.",
        "تقسيم صفحة الإعدادات بالكامل إلى صفحات فرعية وتصنيفات داخلية عصرية وفاخرة عالمياً لتجربة تحكم سلسة.",
        "لوحة إدارة وتحكم المشرفين لمراجعة البلاغات والطلبات مع المزامنة السحابية وباقة الألوان التجميلية المتنوعة."
    )
}
