# خطة دمج محرك التنظيف الأصلي Olympus Cleaner في تطبيق Nexus

خطة شاملة لاستبدال نظام التنظيف القديم (المبني على كشف النصوص ورسم مربعات بيضاء) بمحرك **Olympus Cleaner** الأصلي المكتوب بلغة C++، مع دعم التنظيف المباشر في الذاكرة لصفحات المانجا أثناء القراءة.

---

## مراجعة المستخدم والقرارات المؤكدة (Confirmed Decisions)

> [!IMPORTANT]
> تم تأكيد الخيارات التالية بناءً على إجابات المستخدم:
> - **تسمية ومواقع ملفات المكتبات:** وضع ملفات نائبة (placeholders) لملفات `libmtxv8a.so` في مجلد `arm64-v8a` و `libmtxv7a.so` في مجلد `armeabi-v7a` ليقوم المستخدم باستبدالها بالملفات المجمعة الفعلية.
> - **تسمية المكتبات في jniLibs:** الإبقاء على الأسماء الأصلية `libmtxv8a.so` و `libmtxv7a.so` مع تنفيذ كود تحميل ديناميكي ذكي يفحص المعمارية الحالية للجهاز (`Build.SUPPORTED_ABIS`).
> - **آلية التنظيف:** تنفيذ التنظيف الفوري المباشر في الذاكرة (Real-time inpainting) أثناء تحميل صور الفصول في القارئ عبر مسار معالجة Coil المخصص دون تأخير واجهة المستخدم.

---

## 1. النظرة العامة والهدف الرئيسي (Overview & Concept)

- **الهدف:** الانتقال من أسلوب تغطية النصوص بمربعات بيضاء (`White Layer Overlays`) إلى ترميم الصورة الكامل بالذكاء الاصطناعي وخوارزميات الرؤية الحاسوبية (Sub-Pixel SDF Mask + Hybrid Inpainting: Structure Tensor + PatchMatch + Poisson Blend).
- **التجربة:** عند فتح القارئ وتفعيل وضع التنظيف، يتم تمرير الـ Bitmap المحملة إلى محرك Olympus لتنظيف الشعارات (مثل `olympustaff.com` و `Team X` و `تيم أكس`) وإرجاع الصورة مرممة ونقية بالكامل دون تشويه الرسوم التحتية.

---

## 2. البنية الهيكلية وتدفق المعالجة (Architecture & Flow)

```
┌─────────────────────────────────────────────────────────────┐
│                    Reader Screen (Compose)                  │
└──────────────────────────────┬──────────────────────────────┘
                               │ طلب تحميل صورة الصفحة
                               ▼
┌─────────────────────────────────────────────────────────────┐
│             Coil Image Pipeline / Transformation            │
│  - يفحص حالة تفعيل التنظيف التلقائي                        │
│  - يمرر الـ Bitmap إلى مسار Olympus Inpainter               │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│       Kotlin JNI Binding (com.olympus.cleaner)              │
│       OlympusCleanerEngine & OlympusInpaintTransformation   │
│  - فحص المعمارية وتحميل إما libmtxv8a أو libmtxv7a          │
│  - إدارة دورة الحياة الآمنة ومؤشرات الذاكرة الأصلية         │
└──────────────────────────────┬──────────────────────────────┘
                               │ قفل الـ Bitmap (AndroidBitmap_lockPixels)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│             Native Olympus C++ Engine (.so)                 │
│  1. Text Detection (DBNet / Stroke Gradient)                │
│  2. Multilingual Arabic/English Normalization               │
│  3. Sub-Pixel SDF Mask Generator                            │
│  4. Adaptive Hybrid Inpainting (PatchMatch + Tensor + TV)   │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. خطة التنفيذ خطوة بخطوة (Implementation Steps)

### المرحلة 1: بنية مجلدات المكتبات الأصلية (jniLibs)
- إنشاء المجلدات الرسمية:
  - `app/src/main/jniLibs/arm64-v8a/libmtxv8a.so`
  - `app/src/main/jniLibs/armeabi-v7a/libmtxv7a.so`
- إضافة توثيق وملف إرشادي داخل المجلد ليوضح للمستخدم كيفية استبدال الملفات المؤقتة.
- تحديث `app/build.gradle.kts` لضبط فلترة المعماريات `ndk.abiFilters` والتأكد من تضمين `jniLibs`.

### المرحلة 2: طبقة JNI والـ Wrapper في Kotlin
- إنشاء الحزمة الرسمية المتطابقة مع الرموز المصدرة: `com.olympus.cleaner.OlympusCleanerEngine`.
- دعم التوابع الأصلية:
  - `nativeCreate(targetConfidence, inpaintAlgorithm, maskExpansionPx, featherRadiusPx, enableTiling): Long`
  - `nativeProcessBitmap(handle, inputBitmap, outputBitmap, generateMask): Int`
  - `nativeCancel(handle)`
  - `nativeGetVersion(): String`
  - `nativeDestroy(handle)`
- توفير آلية تحميل ذكية:
  - محاولة تحميل `mtxv8a` على أنظمة 64-bit، ومحاولة تحميل `mtxv7a` على أنظمة 32-bit، مع خيار fallback لـ `olympus_cleaner`.
  - معالجة حالات عدم توفر المكتبة أو وجود خطأ `UnsatisfiedLinkError` بمرونة وأمان دون أي انهيار (Crash-Proof).

### المرحلة 3: دمج مسار المعالجة مع مكتبة Coil والقارئ
- إنشاء `OlympusInpaintTransformation`:
  - تحويل صورة Coil تلقائياً عبر المحرك الأصلي عند تفعيل الميزة في الإعدادات.
  - الحفاظ على كاش الذاكرة الخاص بالصور المنظفة لمنع إعادة المعالجة غير الضرورية.
- استبدال المعالجة القديمة في `WatermarkCleanerBot.kt`:
  - إيقاف رسم الطبقات البيضاء العشوائية `watermarkData.boxes` القديمة في `ReaderScreen.kt`.
  - توجيه طلبات التنظيف المباشرة عبر محرك Olympus C++.
  - الحفاظ على العداد الكوزميك السري وإعدادات واجهة المستخدم كما هي لضمان عدم كسر أي ميزة تابعة.

### المرحلة 4: التحقق والاختبار (Verification)
- فحص بناء المشروع بالكامل عبر `compile_applet`.
- التأكد من عدم وجود أي خطأ استيراد أو تضارب في الموارد والاعتماديات.
