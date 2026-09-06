const express = require('express');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json({ limit: '10kb' }));

// Simple in-memory rate limiting map
const requestCounts = new Map();
const RATE_LIMIT_WINDOW_MS = 60 * 1000;
const MAX_REQUESTS_PER_WINDOW = 60;

function rateLimitMiddleware(req, res, next) {
  const ip = req.ip || req.connection.remoteAddress || 'unknown';
  const now = Date.now();
  const entry = requestCounts.get(ip) || { count: 0, resetAt: now + RATE_LIMIT_WINDOW_MS };

  if (now > entry.resetAt) {
    entry.count = 1;
    entry.resetAt = now + RATE_LIMIT_WINDOW_MS;
  } else {
    entry.count += 1;
  }
  requestCounts.set(ip, entry);

  if (entry.count > MAX_REQUESTS_PER_WINDOW) {
    return res.status(429).json({
      error: 'Too Many Requests',
      message: 'تم تجاوز الحد المسموح به من الطلبات مؤقتًا. يرجى الانتظار والمحاولة لاحقًا.'
    });
  }
  next();
}

app.use(rateLimitMiddleware);

// Category mapping helper
const CATEGORY_MAP = {
  hospital: { ar: 'المستشفيات', en: 'Hospitals', query: 'Hospitals' },
  health_center: { ar: 'المراكز الصحية', en: 'Health Centers', query: 'Health Centers' },
  pharmacy: { ar: 'الصيدليات', en: 'Pharmacies', query: 'Pharmacies' },
  clinic: { ar: 'العيادات', en: 'Clinics', query: 'Clinics' },
  medical_lab: { ar: 'المختبرات', en: 'Medical Laboratories', query: 'Medical Laboratories' },
  school: { ar: 'المدارس', en: 'Schools', query: 'Schools' },
  university: { ar: 'الجامعات', en: 'Universities', query: 'Universities' },
  institute: { ar: 'المعاهد', en: 'Institutes', query: 'Institutes' },
  mosque: { ar: 'المساجد', en: 'Mosques', query: 'Mosques' },
  restaurant: { ar: 'المطاعم', en: 'Restaurants', query: 'Restaurants' },
  cafe: { ar: 'المقاهي', en: 'Cafes', query: 'Cafes' },
  hotel: { ar: 'الفنادق', en: 'Hotels', query: 'Hotels' },
  bank: { ar: 'البنوك', en: 'Banks', query: 'Banks' },
  gas_station: { ar: 'محطات الوقود', en: 'Gas Stations', query: 'Gas Stations' },
  market: { ar: 'الأسواق', en: 'Markets', query: 'Markets' },
  shopping_mall: { ar: 'المولات', en: 'Shopping Malls', query: 'Shopping Malls' },
  park: { ar: 'الحدائق', en: 'Parks', query: 'Parks' },
  museum: { ar: 'المتاحف', en: 'Museums', query: 'Museums' },
  government_office: { ar: 'الدوائر الحكومية', en: 'Government Offices', query: 'Government Offices' },
  police_station: { ar: 'مراكز الشرطة', en: 'Police Stations', query: 'Police Stations' },
  post_office: { ar: 'مكاتب البريد', en: 'Post Offices', query: 'Post Offices' }
};

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', time: new Date().toISOString() });
});

/**
 * POST /search
 * Request body:
 * {
 *   "country": "اليمن",
 *   "governorate": "عدن",
 *   "district": "المنصورة",
 *   "category": "hospital"
 * }
 */
app.post('/search', async (req, res) => {
  try {
    const { country, governorate, district, category } = req.body || {};

    // 1. Input Validation
    if (!country || typeof country !== 'string' || !country.trim()) {
      return res.status(400).json({ error: 'حقل الدولة (country) مطلوب' });
    }
    if (!governorate || typeof governorate !== 'string' || !governorate.trim()) {
      return res.status(400).json({ error: 'حقل المحافظة (governorate) مطلوب' });
    }
    if (!district || typeof district !== 'string' || !district.trim()) {
      return res.status(400).json({ error: 'حقل المديرية (district) مطلوب' });
    }
    if (!category || typeof category !== 'string' || !category.trim()) {
      return res.status(400).json({ error: 'حقل الفئة (category) مطلوب' });
    }

    const cleanCountry = country.trim();
    const cleanGov = governorate.trim();
    const cleanDistrict = district.trim();
    const cleanCategory = category.trim();

    // Resolve category names
    const catInfo = CATEGORY_MAP[cleanCategory.toLowerCase()] || {
      ar: cleanCategory,
      en: cleanCategory,
      query: cleanCategory
    };

    const displayCategory = catInfo.ar || cleanCategory;
    const categoryQueryPart = catInfo.query || cleanCategory;

    // 2. Build Places API query string
    // e.g. "Hospitals in Al Mansurah, Aden, Yemen"
    const textQuery = `${categoryQueryPart} in ${cleanDistrict}, ${cleanGov}, ${cleanCountry}`;

    const apiKey = process.env.GOOGLE_PLACES_API_KEY;
    if (!apiKey) {
      return res.status(503).json({
        error: 'Service Unavailable',
        message: 'Google Places API key is not configured on the server. Please set GOOGLE_PLACES_API_KEY in backend/.env.'
      });
    }

    // 3. Connect to Google Places API (New) – Text Search
    // POST https://places.googleapis.com/v1/places:searchText
    // Field Mask: places.id,places.displayName,places.formattedAddress,places.googleMapsLinks.placeUri
    const placesEndpoint = 'https://places.googleapis.com/v1/places:searchText';
    const fieldMask = 'places.id,places.displayName,places.formattedAddress,places.googleMapsLinks.placeUri';

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 12000); // 12 second timeout

    let placesResponse;
    try {
      placesResponse = await fetch(placesEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Goog-Api-Key': apiKey,
          'X-Goog-FieldMask': fieldMask
        },
        body: JSON.stringify({
          textQuery: textQuery,
          languageCode: 'ar'
        }),
        signal: controller.signal
      });
    } catch (fetchErr) {
      clearTimeout(timeoutId);
      if (fetchErr.name === 'AbortError') {
        return res.status(504).json({ error: 'Gateway Timeout', message: 'انتهت مهلة الاتصال بالخادم الخارجي' });
      }
      throw fetchErr;
    } finally {
      clearTimeout(timeoutId);
    }

    if (!placesResponse.ok) {
      const errText = await placesResponse.text().catch(() => '');
      console.error(`Google Places API error (${placesResponse.status}):`, errText);
      return res.status(502).json({
        error: 'Bad Gateway',
        message: 'فشل الاتصال بخدمة Google Places API'
      });
    }

    const placesData = await placesResponse.json();
    const rawPlaces = placesData.places || [];

    // 4. Extract required fields and deduplicate by Place ID
    const seenIds = new Set();
    const results = [];

    for (const place of rawPlaces) {
      const placeId = place.id;
      if (!placeId || seenIds.has(placeId)) {
        continue;
      }
      seenIds.add(placeId);

      const name = place.displayName?.text || 'مكان غير مسمى';
      const address = place.formattedAddress || `${cleanDistrict}، ${cleanGov}`;
      
      // Use official placeUri or fallback to official Google Maps URL with place_id
      const mapsUrl = place.googleMapsLinks?.placeUri ||
        `https://www.google.com/maps/place/?q=place_id:${encodeURIComponent(placeId)}`;

      results.push({
        placeId: placeId,
        name: name,
        address: address,
        mapsUrl: mapsUrl
      });
    }

    // 5. Return clean JSON response
    return res.json({
      district: cleanDistrict,
      category: displayCategory,
      count: results.length,
      results: results
    });

  } catch (error) {
    console.error('Unhandled server error:', error);
    return res.status(500).json({
      error: 'Internal Server Error',
      message: 'حدث خطأ غير متوقع في الخادم'
    });
  }
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Area Directory Backend is running on port ${PORT}`);
});
