package com.example.util

/**
 * Central Single Source of Truth for Application Versioning & Metadata.
 * Whenever a new release is prepared, updating values in this file updates all UI
 * components across the entire app automatically (Settings, Splash/Preload Screen,
 * Update Cards, Badges, Diagnostics, Footer).
 */
object AppVersionConfig {
    const val VERSION_NAME = "2.0.9"
    const val VERSION_CODE = 49
    const val BUILD_CODENAME = "Nexus Titan SUPER"
    const val RELEASE_CHANNEL = "النسخة الرسمية الخارقة (SUPER)"
    const val RELEASE_DATE = "سبتمبر 2026"
    const val AUTHOR_CREDITS = "فريق Nexus"

    /**
     * Short version string: "v2.0.9 SUPER"
     */
    fun getFullVersionString(): String = "v$VERSION_NAME SUPER"

    /**
     * Build identifier: "Build 49"
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
     * "الإصدار الرسمي v2.0.9 SUPER (Build 49) • فريق Nexus"
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
        "تطوير مظهر الفضاء الكوني السفلي للفوتر وجعله ثابتاً ومستقراً 100% دون أخذ مساحة إضافية أو تحريك الفوتر، مع استغلال المساحة السفلية الفارغة تحته بأناقة.",
        "ترقية نجوم الفضاء الفلكية لتكون واقعية فائقة الجمال بدقة فيزيائية تشمل إشعاعات ضوئية رباعية (Diffraction Flares)، هالات غازية، شهب متلألئة، وميضاً عضوياً، ولمعان غبار النجوم.",
        "تطوير شامل لسجل القراءة: إضافة شريط بحث فوري، تصنيفات وفلاتر (الكل، قيد القراءة، مكتمل، اليوم)، مؤشر تقدم القراءة لكل فصل بالنسبة المئوية، وبطاقة إحصائيات علوية واستئناف سريع.",
        "إضافة تأكيد أمان عند مسح كامل سجل القراءة أو حذف عنصر، مع زيادة سعة تخزين السجل إلى 150 عملاً.",
        "ترقية إصدار التطبيق رسمياً إلى 2.0.9 (Build 49) داخلياً وخارجياً لضمان أعلى مستويات الأداء والاستقرار."
    )
}
