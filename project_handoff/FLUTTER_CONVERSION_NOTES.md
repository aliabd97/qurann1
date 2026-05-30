# ملاحظات وإرشادات التحويل إلى Flutter (Flutter Conversion Notes)

يقدم هذا الملف وثيقة توجيهية متكاملة للبدء في نقل شاشات ومكونات تطبيق الـ Kotlin/Compose الحالي إلى بيئة **Flutter / Dart** بكفاءة وتأمين سرعة التسليم مع شرح للودجت المقابل ونماذج إدارة الحالة والاستعلامات الشارحة.

---

## 1. التوصيات العامة لمكتبات البناء البديلة بالـ Flutter

قبل البدء في تصميم وهندسة شاشات الدارت الكريمة، يوصى باعتماد التوليفات المكتبية التالية لمحاكاة نفس وظائف وأداء النسخة الحالية:

* **إدارة حالات التطبيق (State Management)**: يفضل استخدام **`flutter_bloc` (Cubit)** أو **`Riverpod`** كبديلين ممتازين وأكفاء لحالات الـ `MutableStateFlow` وجلب البيانات بشكل تفاعلي سريع.
* **قواعد البيانات المحلية دون اتصال (Local Database)**: يوصى بتبني حزمة **`sqflite`** للاستعلامات المباشرة القياسية بالـ SQL، أو حزمة **`drift`** (المعروفة سابقاً بـ Moor) كمكافئ صريح ومكتمل لخصائص Room لربط ودمج الكيانات والجداول.
* **إدارة الصوت والبث السريع (Audio Playback)**: استخدام حزمة **`just_audio`** التي تدعم تلاوات الآية بالروابط الحية وكاش البيانات دون تجميد التطبيق.
* **التعامل التفاعلي مع التاريخ الهجري**: تبني حزمة **`hijri`** الموثوقة لتوليد الشهور والتواريخ الهجرية المناسبة في الحاضنات العربية بمرونة.

---

## 2. مصفوفة ترحيل شاشات ومكونات التطبيق (Screens Migration Matrix)

### 1ـ الشاشة الرئيسية (`HomeScreen`)
* **الـ Widget المقرر بناءه بالـ Flutter**: `StatelessWidget` يتغذى على `BlocBuilder` لضمان تحديثات البيانات وتجنب البناء العشوائي لمربعات الحلفاء.
* **الـ State المقابل والمطلوب**:
  * `surahsList` (قائمة السور كاملة)
  * `hijriDateText` (تاريخ هجري مولد)
  * `bookmarkedAyahs` (الآيات المفضلة الحالية)
  * `dailyAyah` (كيان آية التدبر العشوائية المحدثة باليوم)
  * `playbackState` (هل توجد تلاوة نشطة؟ وما هو رقم الآية الجارية؟)
* **الـ Models المقابلة**: `SurahModel`, `AyahModel`
* **الـ Queries المطلوبة**:
  * `SELECT * FROM surahs ORDER BY id ASC`
  * `SELECT * FROM ayahs WHERE id IN (SELECT ayahId FROM bookmarks)`
* **تعقيدات متوقعة ببيئة Flutter**:
  * تصميم المؤشرات والمنحنيات التنافسية لشبكة الإنجاز بطريقة معيارية. يفضل استخدام `CircularProgressIndicator` مخصص ناعم ذو ألوان مذهبة مريحة للعين.

---

### 2ـ شاشة التفسير والتدبر المتقدم (`StudyReaderScreen`)
* **الـ Widget المقرر بناءه بالـ Flutter**: `StatefulWidget` مع إلحاق `PageController` مستجيب لتمرير السور الـ 114 بالـ `PageView.builder`.
* **الـ State المقابل والمطلوب**:
  * `activeSurahId` (رقم السورة النشطة)
  * `ayahsList` (قائمة الآيات المقابلة للسورة النشطة)
  * `wordsMap` (خريطة الكلمات كلمة تلو كلمة لكل آية)
  * `isFilterSheetOpen` (تفعيل تخصيص الشاشة)
  * `displayFilters` (الخيارات المفعلة: التفسير، الترجمة، الإعراب، سبب النزول)
* **الـ Models المقابلة**: `SurahModel`, `AyahModel`, `WordModel`
* **الـ Queries المطلوبة**:
  * `SELECT * FROM ayahs WHERE surahId = :surahId ORDER BY verseNumber ASC`
  * `SELECT * FROM ayah_words WHERE ayahId IN (...) ORDER BY position ASC`
* **تعقيدات متوقعة ببيئة Flutter**:
  * صف الكلمات التفاعلي كلمة كلمة: في Compose تتيح الـ `FlowRow` رص الكلمات في سطور بشكل مريح. في Flutter، يماثلها استخدام الودجت الذكي **`Wrap`** مع معامل اتجاه `textDirection: TextDirection.rtl` لترتيب كلمات الآية عثمانياً ومثاليا وبلا تعطل بالصفوف المكسورة.

---

### 3ـ شاشة مصحف التلاوة العثماني (`MushafReaderScreen`)
* **الـ Widget المقرر بناءه بالـ Flutter**: `StatefulWidget` مدعم بحاوية تمرير `PageController` وبنطاق تمرير ثابت من 0 إلى 603 صفحات في كلاس `PageView.builder`.
* **الـ State المقابل والمطلوب**:
  * `activePageNumber` (رقم الصفحة الفعالة حالياً)
  * `ayahsOnCurrentPage` (قائمة الآيات الموجودة بحدود الصفحة النشطة)
  * `playingAyahId` (تظليل وتجهيز تلوين الآية المستمع إليها بصوت دافئ)
* **الـ Models المقابلة**: `AyahModel`
* **الـ Queries المطلوبة**:
  * `SELECT * FROM ayahs WHERE pageNumber = :pageNumber ORDER BY surahId ASC, verseNumber ASC`
* **تعقيدات متوقعة ببيئة Flutter**:
  * التلوين اللحظي للآية الممنوعة للتشغيل الصوتي. يجب بناء الـ `Text.rich` ليتضمن مقاطع الآيات مع تلوين مقطع الآية الفعال بلون ذهبي بيجي دافئ عند تلاوتها ومزامنته بسلام ويسر مع مشغل الصوت.

---

### 4ـ شاشة التفاصيل والبيان للآية الكريمة (`AyahDetailsScreen`)
* **الـ Widget المقرر بناءه بالـ Flutter**: `StatefulWidget` مع دمج `TabController` وقفة انزلاقه للتبويبات الخمسة.
* **الـ State المقابل والمطلوب**:
  * `activeTabIndex` (مؤشر التبويب الجاري)
  * `noteEditingText` (النص المكتوب بداخل محرر التأمل للآية)
  * `bookmarksSet` (هل الآية المحددة الحالية مفضلة؟)
* **الـ Models المقابلة**: `AyahModel`, `WordModel`, `NoteModel`
* **الـ Queries المطلوبة**:
  * `SELECT * FROM notes WHERE ayahId = :ayahId`
  * `INSERT OR REPLACE INTO notes (ayahId, noteText, updatedAt) VALUES (...)`
* **تعقيدات متوقعة ببيئة Flutter**:
  * معالجة نص كتابة الملاحظات ومزامنته بـ Auto-save دون فرط بالطاقة ودون تأخر بالاستجابة الكتابية. يوصى باستخدام تكنيك الـ **`Debouncer`** البسيط بالمنطقة الكتابية لحفظ المتغير بعد توقف يد الصبي عن التحرير بنصف ثانية تلقائياً.

---

### 5ـ ورقة التبيان اللفظي للكلمات والتحليل الصرفي (`WordDetailsSheet`)
* **الـ Widget المقرر بناءه بالـ Flutter**: ورقة منبثقة تستدعى وتصاغ عبر `showModalBottomSheet`. وبداخلها منزلق أفقي مريح بـ `PageView.builder` بخصائص التفصيل الدقيقة.
* **الـ State المقابل والمطلوب**:
  * `currentWordPageIndex` (تموضع الكلمة النشطة للآية)
* **الـ Models المقابلة**: `WordModel`
* **الـ Queries المطلوبة**:
  * تسحب البيانات بالعموم بمصفوفة بارزة من شاشة الدراسة، فلا يتم استعلامها تكراراً لتوفير عمليات المعالجة البينية.
* **تعقيدات متوقعة ببيئة Flutter**:
  * تحديد ارتفاع ثابت ومتحكم به لمنطقة الشيت في الشاشات الأصغر حجماً (مثل أجهزة الاقتصاد الضعيفة) لتجاوز مشاكل الطفح العمودي للمكونات (Bottom Sheet Overflow). استخدام الودجت المكمل `SingleChildScrollView` مع المكون المحاذي للأطراف.

---

### 6ـ ورقة خيارات الآية المنبثقة (`AyahActionSheet`)
* **الـ Widget المقرر بناءه بالـ Flutter**: ورقة منبثقة تفاعلية تصاغ عبر دمج مكونات `GridView.count` لتمثيل الشبكة خيارات كروت الإجراءات.
* **الـ State المقابل والمطلوب**:
  * `isBookmarked` (هل الآية الجارية موسومة بعلامة مرجعية حالياً؟)
* **الـ Models المقابلة**: `AyahModel`
* **الـ Queries المطلوبة**:
  * `DELETE FROM bookmarks WHERE ayahId = :ayahId`
  * `INSERT INTO bookmarks (ayahId, addedAt) VALUES (...)`
* **تعقيدات متوقعة ببيئة Flutter**:
  * نسخ نص الآية بسلاسة إلى حافظة نظام التشغيل (Clipboard) واستعراض بوب اب مبهج (SnackBar / Toast). في Flutter تتم العملية فورياً عبر الكود:
    ```dart
    Clipboard.setData(ClipboardData(text: "$textAr (${nameAr}:${verseNumber})"));
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('تم نسخ الآية الكريمة بنجاح')));
    ```

---

### 7ـ المكتبة وإدارة حزم الكتب والتراجم (`PackageManagerScreen`)
* **الـ Widget المقرر بناءه بالـ Flutter**: شاشة كاملة مبسطة ومريحة بـ `ListView.builder` لتبيان الكتب والحزم والتراجم.
* **الـ State المقابل والمطلوب**:
  * `packagesList` (قائمة الحزم المسجلة والجاهزة بقاعدة البيانات)
* **الـ Models المقابلة**: `PackageModel`
* **الـ Queries المطلوبة**:
  * `SELECT * FROM packages ORDER BY displayOrder ASC`
  * `UPDATE packages SET isInstalled = :isInstalled WHERE packageId = :packageId`
* **تعقيدات متوقعة ببيئة Flutter**:
  * رسم حالات وأزرار تبيان تفعيل وإيقاف الخدمات وعرقلة العمل بنظام التغذية اللحظية. التأكد من إعادة تدوير وبث كتل شاشات القراءة وبتحديث فوري وموفق.

---

### 8ـ فهرس السور السريع والشامل (`SurahIndexScreen`)
* **الـ Widget المقرر بناءه بالـ Flutter**: نموذج حوار حواري عائم `Dialog` أو واجهة مستقلة مدعمة بنظام بحث طولي سريع.
* **الـ State المقابل والمطلوب**:
  * `indexSearchQuery` (نص البحث الداخلي)
  * `filteredSurahsList` (قائمة السور المفلترة)
* **الـ Models المقابلة**: `SurahModel`
* **الـ Queries المطلوبة**:
  * يستقبل مستند القائمة بالبناء الأول المولد بقائمة الـ ViewModel ويقوم بفرزه وتحويره محلياً لعدم استنزاف موارد الجهاز بقراءة الاستعلام مراراً وتكراراً.
* **تعقيدات متوقعة ببيئة Flutter**:
  * مواءمة حركة الفهرس والارتقاء بالتلاوات، استخدام نظام الملاحة الصريح `Navigator.pop(context, selectedSurahId)` لتمرير السورة الموجهة بسلام وأمان.
