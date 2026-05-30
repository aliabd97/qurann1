# رموز ومحددات التصميم (Design Tokens) - مشروع القرآن الكريم الكافي

يتأسس المظهر البصري لـ **القرآن الكريم الكافي ومصحف التبيان** على سمة إسلامية مذهبة راقية، تجمع بين ألوان المساجد والكتب الفقهية العتيقة والخطوط العربية الكلاسيكية. يستخرج هذا المستند كافة محددات التصميم لتمكين مطور Flutter من محاكاة نفس التجربة البصرية بدقة متناهية.

---

## 1. لوحة الألوان الهندسية (Color Palette)

تتميز الألوان بأنها محددة سلفاً في ملف `Color.kt` ولا يتم استخدام الألوان الديناميكية للنظام للمحافظة على قدسية وروحانية سمة التطبيق الموحدة:

### أ. الوضع المضيء الكلاسيكي (Light Theme Colors - Scholarly Scholarly Green Theme)
* **اللون الأساسي (`primary`)**: `0xFF0F5A3E` (أخضر إسلامي داكن - IslamicGreen)
* **اللون الثانوي (`secondary`)**: `0xFF2E8F67` (أخضر عشبي مبهر - LightGreenAccent)
* **اللون الثالث (`tertiary`)**: `0xFF4A7D63` (أخضر زيتي وقور - GreenTertiary)
* **خلفية التطبيق (`background`)**: `0xFFFAF8F5` (بيج ناعم مائل للكريمة الورقية - SoftCreamBackground)
* **الأسطح والبطاقات (`surface`)**: `0xFFFFFFFF` (أبيض ناصع)
* **الأسطح البديلة وبطاقات الآية النشطة (`surfaceVariant`)**: `0xFFF3EFE9` (ذهبي بردي دافئ - WarmSand)
* **النصوص فوق الأساسيات (`onPrimary` / `onSecondary`)**: `0xFFFFFFFF` (أبيض)
* **النصوص الرئيسية والكتابة العريضة (`onBackground` / `onSurface`)**: `0xFF191D1A` (أسود مخضر فحمي)
* **النصوص الفرعية الهامشية (`onSurfaceVariant`)**: `0xFF404943` (رمادي معتم دافئ)
* **لون التفضيل والنجوم والمشغلات**: `0xFFD4AF37` (أصفر ذهبي إسلامي - GoldColor)

### ب. الوضع المظلم الوقور (Dark Theme Colors - Mystical Moss Green Theme)
* **اللون الأساسي (`primary`)**: `0xFF8CD8A7` (أخضر نعناعي مشرق - DarkPrimary)
* **اللون الثانوي (`secondary`)**: `0xFFB0D2C0` (أخضر رمادي باهت - DarkSecondary)
* **اللون الثالث (`tertiary`)**: `0xFF9EBFAD` (أخضر باهت دافئ - DarkTertiary)
* **خلفية التطبيق (`background`)**: `0xFF121A16` (أسود مخضر ليلي غامق - DarkBackground)
* **الأسطح والبطاقات (`surface`)**: `0xFF1B2420` (أخضر معتم عميق - DarkSurface)
* **الأسطح البديلة وبطاقات الآية النشطة (`surfaceVariant`)**: `0xFF23312A` (أخضر غاب غامق - DarkSurfaceVariant)
* **النصوص فوق كتل الأساسيات (`onPrimary`)**: `0xFF00391F` (أخضر غابات داكن جداً)
* **النصوص فوق الثانويات (`onSecondary`)**: `0xFF1B3528` (أخضر معتم هادئ)
* **النصوص والآيات فوق السطوع المظلم (`onBackground` / `onSurface`)**: `0xFFE1E3DF` (رمادي طباشيري مريح للعين)
* **النصوص الفرعية الهامشية في المظلم (`onSurfaceVariant`)**: `0xFFBFC9C2` (رمادي أخضر ناعم)

---

## 2. الطوبوغرافيا وأحجام الخطوط (Typography)

يستخدم التطبيق عائلة الخطوط التأسيسية Serif لتعكس وقار وجمال الرسم العربي الكلاسيكي ومحاكاة المطبوعات القديمة:

```dart
// تعريف الـ TextStyle الافتراضي في Flutter بمحاكاة الـ Compose Typography
final TextTheme quranTextTheme = TextTheme(
  displayLarge: TextStyle(fontFamily: 'serif', fontWeight: FontWeight.bold, fontSize: 57, height: 1.12),
  headlineLarge: TextStyle(fontFamily: 'serif', fontWeight: FontWeight.bold, fontSize: 32, height: 1.5),
  titleLarge: TextStyle(fontFamily: 'serif', fontWeight: FontWeight.bold, fontSize: 22, height: 1.25),
  titleMedium: TextStyle(fontFamily: 'serif', fontWeight: FontWeight.medium, fontSize: 18, height: 1.44),
  bodyLarge: TextStyle(fontFamily: 'serif', fontWeight: FontWeight.normal, fontSize: 16, height: 1.75), // حجم مثالي للآيات وسطور التفسير لمنع التداخل
  bodyMedium: TextStyle(fontFamily: 'serif', fontWeight: FontWeight.normal, fontSize: 14, height: 1.57),
  labelSmall: TextStyle(fontFamily: 'serif', fontWeight: FontWeight.medium, fontSize: 11, height: 1.45),
);
```

* **ملاحظة لغوية أساسية**: تم تمديد مساحة الارتفاع السطري `lineHeight` للآيات وتفصيل الإعراب بالخاصية `lineHeight = 28.sp` أو `lineHeight = 36.sp` وذلك لمنع تداخل الحركات النحوية التحتية والفوقية (الضم والكسر والفتح الشديد) لسهولة بالغة بالدراسة.

---

## 3. التباعد الهيكلي وهوامش التصميم (Spacing Grid)

يسير التطبيق بدقة متناهية على شبكة تباعدات Material Design بمعدل خطوة 4dp و 8dp:
* **الهوامش الخارجية للشاشات**: `12.dp` أو `16.dp` لمنع ملامسة البطاقات لحواف الهاتف.
* **البطانة الداخلية داخل كتل الآيات**: `16.dp` لتوفير مساحة ملائمة (Negative Space) تريح القارئ.
* **التباعد بين كتل السطور المتتالية**: `8.dp` أو `12.dp`.
* **الحد الأدنى لمساحة النقر التفاعلية لكافة الأزرار**: `48.dp` لتلبية معايير سهولة الاستخدام لكبار السن (Accessibility standards).

---

## 4. مكونات البنية التشكيلية وتصميم المكونات الرسومية

### أ. كروت وبطاقات الآية والسور (`Cards Design`)
* **شكل الحواف (Shape Corner)**: زوايا دائرية بوزن `12.dp` باستخدام `RoundedCornerShape(12.dp)`.
* **الهوامش الخلوية والمظهر البصري**: يتم تعبئة البطاقات بلون مريح ومتباين بقوة مع خلفية الشاشة (مثل استخدام لون السطح مع كاش ناعم من اللون الأساسي بنسبة توفر 25% من الشفافية لبطاقات الآية النشطة: `MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)`).

### ب. شريط الواجهة العلوي (`TopAppBar Design`)
* **الارتفاع والتمديد**: شريط علوي بارز ذو خلفية ثابتة بمظهر أخضر غامق إسلامي بالوضع العادي (`IslamicGreen`) وأخضر نعناعي هادئ بالوضع المظلم (`surface`).
* **العنوان ومحاذاة RTL**: عناوين عريضة مكتوبة بـ `FontWeight.Bold` محاذية إلى اليمين بمرونة وراحة، مع مواءمة أيقونة الرجوع لتتطابق مع التحرك المعاكس للغة العربية وصورة السهم متجهة لليسار بذكاء (`Icons.AutoMirrored.Filled.ArrowBack`).

### ج. شريط الواجهة السفلي للتصفح (`NavigationBar Design`)
* **الإكساء والتصميم**: يرتفع الشريط بتموضع مريح وثابت ويحتوي على تباين لوني دقيق. في الوضع المظلم يستخدم لون السطح الحاد، وفي الوضع المضيء يعتمد لون الأساس الأخضر الصريح.
* **قوة التفاعل (Indicators)**: الأزرار المفعلة تضاء بلون ذهبي بيجي دافئ ودمج رقيق بمؤشر بيضاوي مفرغ بالخلفية ذو شفافية لطيفة مكملة.

### د. أوراق الخيارات الممتدة والمنبثقة ومفردات الصرف (`Bottom Sheets Design`)
* **شكل الحواف البارزة**: حواف دائرية علوية فقط بدقة تزيد عن `16.dp` لتمثيل تدفق تصاعدي جذاب ومستحب للمستخدم.
* **إكساء الرأس البصري**: يعرض في المنتصف خطاً صغيراً أفقياً ناعماً بالمنتصف دلالة على إمكانية السحب لأسفل والإغلاق التلقائي.

---

## 5. مصفوفة الأيقونات والرموز المعتمدة لجميع الأزرار

يوضح الجدول التالي خريطة الأيقونات الموحدة والمستخدمة في كافة شاشات وأزرار التطبيق لتوفير تماسك تجربة المستخدم البصرية:

| مسمى الزر والعملية | الأيقونة المحددة في Compose | المكافئ البصري في Flutter (Material Icons) | دلالة الاستخدام البصري وتوضيحه |
| :--- | :--- | :--- | :--- |
| **الرئيسية بأسفل التبويبات** | `Icons.Default.Home` | `Icons.home` | بوابة توجيه المستخدم والتقدم اليومي. |
| **القرآن وبوابة التلاوة** | `Icons.Default.MenuBook` | `Icons.menu_book` | مصحف التلاوة وغرفة الدراسة. |
| **المكتبة والحزم والتنزيل** | `Icons.Default.CloudDownload` | `Icons.cloud_download` | تبيان حالة التنزيل للكتب وتشجيع التحميل. |
| **تصفية المحتوى المتقدم** | `Icons.Default.Tune` | `Icons.tune` | فلاتر تخصيص بطاقة الآية النشطة. |
| **قائمة وفهرس السور** | `Icons.AutoMirrored.Filled.List` | `Icons.list_alt` | فهرس سور القرآن للتحرك السريع والموضوعي. |
| **الرجوع والارتداد للخارج** | `Icons.AutoMirrored.Filled.ArrowBack` | `Icons.arrow_back` | حركة الرجوع المعاكسة للغة العربية (RTL). |
| **تفضيل ووسم الآية بالقرآن** | `Icons.Default.Star` / `StarBorder` | `Icons.star` / `Icons.star_outline` | علامة الإشراق والتفضيل للآية القرآنية. |
| **أسباب النزول المأثورة** | `Icons.Default.HistoryEdu` | `Icons.history_edu` | الروايات والأحداث التاريخية لنزول الآية. |
| **تدوين الملاحظات والتدبر الشخصي**| `Icons.Default.Edit` | `Icons.edit` | فتح حقل التحرير والكتابة للخواطر. |
| **نسخ الآية الكريمة** | `Icons.Default.ContentCopy` | `Icons.content_copy` | وضع نص الآية في حافظة الذاكرة مفرغاً. |
| **إعراب الآيات والجمل** | `Icons.AutoMirrored.Filled.MenuBook` | `Icons.menu_book` | النحو وأحكام الإعراب وبناء الكلمة. |
| **التفسير ومأثوره** | `Icons.AutoMirrored.Filled.Comment` | `Icons.comment` | كتب تفاسير السلف ومعاني الآية لغةً. |
| **موضوعات وتصنيفات الآيات** | `Icons.Default.AutoAwesome` | `Icons.auto_awesome` | تصانيف القرآن الدينية والعلمية الفرعية. |
| **إلغاء والحذف والتطهير** | `Icons.Default.Delete` | `Icons.delete` | مسح الملفات الفائضة لتقليل الاستهلاك. |

---

هذه الرموز والتوليفات البصرية تمكن مطور Flutter من إدراج عناصر الثيم الإسلامي المعمد والمذهب بدقة وتقديم تطبيق رائع يسر قلوب وأعين المستخدمين بالكامل.
