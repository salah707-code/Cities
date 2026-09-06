# Area Directory (دليل المناطق)

محرك بحث عربي سريع وحديث للأماكن والخدمات المسجلة في Google Maps عبر مختلف الدول والمحافظات والمديريات، بالاعتماد على **Google Places API (New)**.

---

## 🌟 مميزات المشروع

- **واجهة مستخدم عربية بالكامل** تدعم اتجاه الكتابة من اليمين إلى اليسار (RTL) مبنية بأحدث معايير **Jetpack Compose** و **Material 3**.
- **دعم الوضعين الليلي والنهاري** (Light & Dark Theme) وألوان ديناميكية متناسقة.
- **تصفح دقيق للمناطق الجغرافية**: الدولة → المحافظة → المديرية → الفئة (مع تغطية كاملة لمحافظة عدن وكافة مديرياتها الثمان).
- **أمان عالي**: مفتاح Google Places API محفوظ حصريًا داخل الخادم الوسيط (Backend) ولا يتم تضمينه إطلاقًا داخل كود أو حزمة تطبيق Android.
- **تصفية ذكية وإلغاء التكرار**: استبعاد التكرارات استنادًا إلى المعرف الفريد `Place ID`.
- **فتح فوري في Google Maps**: زر مباشر للانتقال لموقع المكان عبر تطبيق الخرائط الرسمي أو المتصفح.
- **معالجة متقدمة لحالات الاتصال والأخطاء**: جاري البحث، عدم وجود نتائج، انقطاع الإنترنت، وتعطل الخدمة دون كشف تفاصيل داخلية حساسة.

---

## 🏗️ هيكلية المشروع (Architecture)

```
Android App (Jetpack Compose)
       │
       ▼
SearchViewModel (StateFlow)
       │
       ▼
PlacesRepositoryImpl
       │
       ▼
Retrofit + OkHttp
       │
       ▼  (POST /search)
Area Directory Backend (Node.js / Express)
       │
       ▼  (POST places:searchText)
Google Places API (New)
```

---

## 📋 المتطلبات (Requirements)

1. **لتشغيل تطبيق Android**:
   - Android Studio (Ladybug أو أحدث) أو Android SDK Build Tools.
   - Java Development Kit (JDK 17 أو JDK 21).
   - هاتف Android أو محاكي يعمل بنظام Android 7.0 (API 24) فما فوق.

2. **لتشغيل خادم Backend**:
   - Node.js (الإصدار 18 فما فوق).
   - npm أو yarn.
   - مفتاح API صالح لخدمة **Google Places API (New)**.

---

## 🔑 إعداد Google Places API (New)

1. توجه إلى [Google Cloud Console](https://console.cloud.google.com/).
2. أنشئ مشروعًا جديدًا أو اختر مشروعًا قائمًا.
3. فعّل خدمة **Places API (New)**:
   - من القائمة الجانبية: **APIs & Services** > **Library**.
   - ابحث عن `Places API (New)` واضغط **Enable**.
4. استخرج مفتاح API:
   - من القائمة الجانبية: **APIs & Services** > **Credentials**.
   - اضغط **Create Credentials** > **API key**.
   - يُوصى بوضع قيود أمان (API restrictions) للمفتاح لحصره على `Places API (New)`.

---

## 🚀 تشغيل الخادم (Backend Setup)

1. انتقل إلى مجلد `backend`:
   ```bash
   cd backend
   ```

2. ثبّت الحزم المطلوبة:
   ```bash
   npm install
   ```

3. أنشئ ملف المتغيرات السرية `.env`:
   ```bash
   cp .env.example .env
   ```

4. افتح ملف `.env` وضع مفتاح Google Places API:
   ```env
   GOOGLE_PLACES_API_KEY=AIzaSy...YourActualApiKeyHere
   PORT=3000
   ```

5. شغّل الخادم:
   ```bash
   npm start
   ```
   سيعمل الخادم على: `http://localhost:3000`

### اختبار نقطة النهاية (Endpoint Verification)

```bash
curl -X POST http://localhost:3000/search \
  -H "Content-Type: application/json" \
  -d '{
    "country": "اليمن",
    "governorate": "عدن",
    "district": "المنصورة",
    "category": "hospital"
  }'
```

---

## 📱 إعداد وتشغيل تطبيق Android

### 1. ربط التطبيق بالخادم المحلي
- إذا كنت تستخدم **محاكي Android الرسمي (Android Emulator)**:
  الرابط الافتراضي المُعد في التطبيق هو `http://10.0.2.2:3000/`، حيث يمثل `10.0.2.2` جهاز الكمبيوتر المضيف تلقائيًا.
- إذا كنت تستخدم **هاتفًا حقيقيًا عبر Wi-Fi** أو خادمًا سحابيًا:
  يمكنك تغيير رابط الـ Backend مباشرة من داخل التطبيق عبر النقر على أيقونة **الإعدادات (⚙️)** في أعلى الشاشة الرئيسية وإدخال الرابط مثل `http://192.168.1.50:3000/` وحفظه فورًا.

### 2. تشغيل الاختبارات الآلية
```bash
gradle testDebugUnitTest
```

### 3. بناء وتثبيت التطبيق (Debug APK)
```bash
gradle assembleDebug
```
ملف الـ APK الناتج ستجده في:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🗂️ بنية ملفات الكود في Android

```
app/src/main/
├── assets/data/
│   └── yemen.json                   # بيانات الدول والمحافظات والمديريات والفئات
├── java/com/example/
│   ├── MainActivity.kt              # نقطة الدخول وتهيئة Edge-to-Edge
│   └── areadirectory/
│       ├── data/
│       │   ├── model/               # Place, Country, Governorate, District, Category, DTOs
│       │   ├── api/                 # Retrofit Interfaces & OkHttpClient setup
│       │   ├── loader/              # قارئ ملف البيانات المحلية JSON
│       │   └── repository/          # PlacesRepository وإلغاء التكرار
│       ├── ui/
│       │   ├── HomeScreen.kt        # الشاشة الرئيسية واختيار القوائم
│       │   ├── ResultsScreen.kt     # شاشة النتائج والحالات المختلفة
│       │   ├── components/          # بطاقة المكان PlaceCard وأزرار الخرائط
│       │   ├── theme/               # ألوان Material 3 والخطوط ودعم RTL
│       │   └── navigation/          # التنقل بين الشاشات
│       └── viewmodel/
│           └── SearchViewModel.kt   # إدارة حالات البحث ونموذج الإدخال
```
