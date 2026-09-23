package com.example.util

/**
 * Central Single Source of Truth for Application Versioning & Metadata.
 * Whenever a new release is prepared, updating values in this file updates all UI
 * components across the entire app automatically (Settings, Splash/Preload Screen,
 * Update Cards, Badges, Diagnostics, Footer).
 */
object AppVersionConfig {
    const val VERSION_NAME = "2.0.6"
    const val VERSION_CODE = 46
    const val BUILD_CODENAME = "Nexus Titan SUPER"
    const val RELEASE_CHANNEL = "النسخة الرسمية الخارقة (SUPER)"
    const val RELEASE_DATE = "سبتمبر 2026"
    const val AUTHOR_CREDITS = "فريق Nexus"

    /**
     * Short version string: "v2.0.6 SUPER"
     */
    fun getFullVersionString(): String = "v$VERSION_NAME SUPER"

    /**
     * Build identifier: "Build 46"
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
     * "الإصدار الرسمي v2.0.6 SUPER (Build 46) • فريق Nexus"
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
        "معالجة جذرية وفورية للكراش عند فتح صفحة تخصيص المظهر واستقرار فائق لكافة خيارات الألوان.",
        "تحديث مستودع التحديث الديناميكي التلقائي من داخل التطبيق وربطه بمستودع zxiu86/nexusap الجديد.",
        "إضافة توهج وهالة لونية سينمائية ساحرة (Aura Glow) في صفحة تفاصيل العمل تشمل خلفية البانر وبطاقة كافر العمل.",
        "تخصيص كامل لسرعة مرور التموج اللوني ودورة ظهوره ولونه لشريط الفوتر السفلي.",
        "سمة اللون الأبيض الأنيق والمريح للعين لقراءة هادئة ومتزنة بدون إجهاد بصري.",
        "ترقية إصدار التطبيق إلى 2.0.6 SUPER داخلياً وخارجياً لضمان أعلى مستويات الاستقرار والأداء."
    )
}
