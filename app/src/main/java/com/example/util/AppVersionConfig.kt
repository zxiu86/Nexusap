package com.example.util

/**
 * Central Single Source of Truth for Application Versioning & Metadata.
 * Whenever a new release is prepared, updating values in this file updates all UI
 * components across the entire app automatically (Settings, Splash/Preload Screen,
 * Update Cards, Badges, Diagnostics, Footer).
 */
object AppVersionConfig {
    const val VERSION_NAME = "2.0.8"
    const val VERSION_CODE = 48
    const val BUILD_CODENAME = "Nexus Titan SUPER"
    const val RELEASE_CHANNEL = "النسخة الرسمية الخارقة (SUPER)"
    const val RELEASE_DATE = "سبتمبر 2026"
    const val AUTHOR_CREDITS = "فريق Nexus"

    /**
     * Short version string: "v2.0.8 SUPER"
     */
    fun getFullVersionString(): String = "v$VERSION_NAME SUPER"

    /**
     * Build identifier: "Build 48"
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
     * "الإصدار الرسمي v2.0.8 SUPER (Build 48) • فريق Nexus"
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
        "تحديث مسار قراءة وتحميل بيانات المستخدمين حصراً من مستودع البيانات zxiu86/Data/user/user.json وإلغاء إنشاء ومزامنة ملف users/users.json نهائياً.",
        "تحديث قراءة وتحميل البلاغات من مستودع zxiu86/Data/report.json مع دعم الترحيل السلس والتلقائي للبلاغات.",
        "تطبيق إعدادات تخصيص مظهر شعاع وتموج الفوتر فورياً في شريط التنقل ولوحة المعاينة دون الحاجة لإغلاق أو إعادة تشغيل التطبيق.",
        "ترقية إصدار التطبيق رسمياً إلى 2.0.8 داخلياً وخارجياً لضمان أعلى مستويات الكفاءة والاستقرار."
    )
}
