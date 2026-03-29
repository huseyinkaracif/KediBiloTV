# KediBiloTV - Tasarim Dokumani

## Ozet

KediBiloTV, Android TV, telefon ve tablet icin gelistirilen bir IPTV uygulamasidir. Xtream Codes API uzerinden canli TV, film ve dizi iceriklerini kullaniciya sunar. Netflix benzeri ama kedi temali, neseli bir arayuze sahiptir. Dusuk RAM/CPU cihazlarda optimize calisma onceliktir.

## Kararlar

| Karar | Secim | Sebep |
|-------|-------|-------|
| IPTV Kaynagi | Xtream Codes API | Zengin metadata, kategori/poster/bolum bilgisi |
| Video Oynatici | Media3 / ExoPlayer | Android native, dusuk RAM, genis format destegi |
| Veri Saklama | Room DB (lokal) | Basit, internet gerektirmez, performansli |
| UI Yaklasimi | Compose TV + Compose Material | Ortak paradigma, ayrı UI katmanlari |
| Modul Yapisi | Single Module | Tek gelistirici, basit build, dusuk overhead |
| Dil | Sadece Turkce (strings.xml) | Hizli gelistirme, i18n altyapisi hazir |
| Navigasyon | Compose Navigation | Tek NavHost, platform dallanmasi composable'da |
| Build Stratejisi | Tek APK, runtime cihaz algilama | Basit dagitim, TV/mobil ayni APK |
| Tema Seviyesi | Orta (kedi dokunuslari) | Karakter + performans dengesi |

## Teknoloji Stack

- Kotlin, minSdk 24, targetSdk 34
- Jetpack Compose (mobil) + Compose TV (tv)
- Compose Navigation (tek NavHost, platform dallanmasi composable seviyesinde)
- Media3 / ExoPlayer
- Room (lokal DB)
- Ktor Client (ag istekleri)
- Hilt (dependency injection)
- Coil (resim yukleme)
- Kotlin Coroutines + Flow

## Paket Yapisi

```
com.kedibilotv
├── data/
│   ├── api/          # Xtream Codes API servisi
│   ├── db/           # Room entities, DAO'lar
│   ├── repository/   # Repository implementasyonlari
│   └── model/        # API ve DB modelleri
├── domain/
│   ├── model/        # Domain modelleri
│   ├── repository/   # Repository interface'leri
│   └── usecase/      # Is mantigi
├── ui/
│   ├── mobile/       # Telefon/tablet composable'lari
│   ├── tv/           # TV composable'lari (Compose TV)
│   ├── common/       # Paylasilan UI bilesenleri
│   └── theme/        # KediBilo temasi, renkler, tipografi
├── player/           # Media3 player wrapper
├── di/               # Hilt modulleri
└── util/             # Extension fonksiyonlari
```

## Xtream Codes API & Veri Katmani

### API Endpoint'leri

- `player_api.php?username=X&password=Y` — Kimlik dogrulama + sunucu bilgisi
- `player_api.php?...&action=get_live_categories` — Canli TV kategorileri
- `player_api.php?...&action=get_live_streams` — Canli TV kanallari
- `player_api.php?...&action=get_vod_categories` — Film kategorileri
- `player_api.php?...&action=get_vod_streams` — Filmler
- `player_api.php?...&action=get_series_categories` — Dizi kategorileri
- `player_api.php?...&action=get_series` — Diziler
- `player_api.php?...&action=get_series_info&series_id=X` — Dizi bolumleri

### Stream URL Formatlari

- Canli: `http://server:port/live/user/pass/streamId.ts`
- Film: `http://server:port/movie/user/pass/vodId.mp4`
- Dizi: `http://server:port/series/user/pass/episodeId.mp4`

### Room DB Tablolari

- **server_config** — Sunucu URL, kullanici adi, sifre, son giris. Tek sunucu destegi (V1). Sunucu degistirmek icin cikis yap → yeni giris.
- **favorites** — Favori icerikler (type: live/vod/series, stream_id, name, poster_url, category)
- **watch_history** — Izleme gecmisi (stream_id, type, position_ms, duration_ms, last_watched, episode_id nullable)

Not: V1'de offline cache tablolari yok. Baglanti yoksa "Baglanti yok" mesaji gosterilir. Veriler in-memory cache ile tutulur (repository seviyesinde).

### Veri Akisi

API cagrisi → Repository in-memory cache'e yazar → UI, StateFlow ile dinler. Baglanti yoksa hata mesaji gosterilir.

## Navigasyon Mimarisi

Compose Navigation ile tek NavHost kullanilir. Route'lar:
- `login` — Giris ekrani
- `home` — Ana ekran
- `category/{type}` — Kategori listesi (type: live/vod/series)
- `content/{type}/{categoryId}` — Icerik listesi
- `detail/{type}/{streamId}` — Detay ekrani
- `player/{type}/{streamId}?episodeId={episodeId}` — Oynatici
- `settings` — Ayarlar

Platform dallanmasi: Her route'un composable'i icinde `isTV()` kontrolu ile TV veya mobil UI secilir. Ortak ViewModel paylasılır.

Build stratejisi: Tek APK, tek launcher Activity. `UiModeManager` ile runtime'da TV/mobil algilanir. TV'de Leanback launcher intent, mobilde normal launcher intent — manifest'te her ikisi de tanimlanir.

## Hesap Durumu

Xtream API auth yaniti `auth` (0/1) ve `exp_date` icerır. Login'de kontrol edilir:
- `auth=0` → "Hesap aktif degil" hatasi
- `exp_date` gecmisse → "Hesabinizin suresi dolmus" hatasi
- Uygulama acikken session expired olursa (API 403) → Login ekranina yonlendir

## Ekranlar

### Giris Ekrani (Login)
- Sunucu URL, kullanici adi, sifre alanlari
- "Baglan" butonu → API auth kontrolu → basariliysa ana ekrana
- Son girisi hatirla (Room'dan)

### Ana Ekran (Home)
- Ustte one cikan icerik banner'i (rastgele secim — VOD listesinden random 5 icerik rotate)
- "Devam Et" satiri (izleme gecmisinden, position > 0 olanlar)
- "Favoriler" satiri
- "Canli TV", "Filmler", "Diziler" kategori kartlari
- TV'de: yatay kaydirmali satirlar (Netflix tarzi row)
- Mobilde: dikey scroll, yatay kaydirmali LazyRow'lar

### Kategori Listesi
- Secilen ana kategorideki alt kategoriler grid olarak
- TV'de: D-pad ile gezinme
- Mobilde: grid veya liste gorunumu

### Icerik Listesi
- Bir kategorideki tum kanallar/filmler/diziler
- Poster + isim kartlari
- Arama: ust bar'da text field ile lokal filtreleme (in-memory, API'den gelen listeyi filtreler). Ayri bir arama ekrani yok, her icerik listesinde filtreleme mevcut.

### Detay Ekrani (Film/Dizi)
- Poster, baslik, aciklama
- Film: "Oynat" / "Devam Et" butonu
- Dizi: Sezon secici → bolum listesi → bolum sec → oynat
- Favorilere ekle/cikar toggle

### Oynatici Ekrani
- Tam ekran Media3 player
- Kontroller: oynat/duraklat, ileri/geri sarma (10s/30s), ses, altyazi secimi, ses kanali secimi
- TV'de: D-pad ile kontrol (center=play/pause, left/right=seek)
- Mobilde: dokunma gestleri
- Canli TV'de: kanal degistirme (yukari/asagi)
- Cikista pozisyon Room'a kaydedilir

### Ayarlar
- Hesap bilgileri (cikis yap → login ekranina don, sunucu bilgileri silinir)
- Player ayarlari (buffer boyutu: dusuk/orta/yuksek)

## Player Detaylari & Performans

### Media3 Konfigurasyon
- `DefaultLoadControl`: initialBuffer 2s, minBuffer 10s, maxBuffer 30s (RAM tasarrufu)
- `DefaultTrackSelector`: max video resolution siniri (cihaz kapasitesine gore otomatik)
- Format destegi: HLS, MPEGTS, MP4 (RTMP destegi V1'de yok — Media3'te deprecated. Gerekirse ileride eklenir)
- Subtitle: SRT, VTT (SSA/ASS Media3'te sinirli destek — plain text olarak render edilir)

### Performans Stratejisi
- Coil ile poster cache (memory + disk cache, max 100MB disk)
- LazyColumn/LazyRow ile sadece gorunen ogeler render edilir
- In-memory cache ile gereksiz API cagrisi onlenir (uygulama acikken gecerli, yeni veri cekmek icin pull-to-refresh)
- Ktor ile connection pooling
- Background'da calisan servis yok — sadece aktif oynatma
- ProGuard/R8 ile APK kucultme
- Compose'da `remember` ve `derivedStateOf` ile gereksiz recomposition onlenir

### Dusuk Cihaz Hedefleri
- 2GB RAM cihazda sorunsuz calisma hedefi (Compose + Media3 icin realistik alt sinir)
- Poster yukleme sirasinda placeholder (kedi silueti vector drawable)
- Buyuk listelerde LazyColumn/LazyRow ile client-side chunked rendering (Xtream API pagination desteklemiyor, tum liste gelir, UI lazy render eder)

## Tema & Gorsel Tasarim

### Renk Paleti
- Primary: Turuncu-amber (#FF8C00) — sicak, neseli
- Background: Koyu lacivert/siyah (#0D1117) — sinema hissi
- Surface: Koyu gri (#1A1A2E) — kart arka planlari
- Accent: Acik turuncu/sari (#FFB347) — vurgular
- Text: Beyaz (#FFFFFF) ana, gri (#B0B0B0) ikincil

### Kedi Dokunuslari (Orta Seviye)
V1'de vector drawable ve emoji ile saglanir, custom illustrasyonlar polish fazinda eklenir:
- App ikonu: Kedi silueti + TV ekrani (vector drawable)
- Splash screen: App ikonu + uygulama adi (basit)
- Bos durumlar: Kedi emoji + aciklama metni
- Loading: Kedi pati seklinde CircularProgressIndicator
- Kategori ikonlari: Material ikonlari (kedi temali ikon seti polish fazinda)
- Bottom bar / sidebar ikonlari: Material ikonlari (V1)

### Tipografi
- Basliklar: Bold, rounded font (Nunito veya Quicksand)
- Govde: Regular, okunabilir (varsayilan system font)

## Hata Yonetimi

- **Baglanti hatasi:** "Sunucuya baglanamadi" + yeniden dene butonu
- **Gecersiz kimlik:** Login'de hata mesaji, tekrar giris
- **Stream oynatilamiyor:** Player'da hata overlay + "Kanal calismiyor" mesaji, listeye don
- **Bos kategori:** Kedi illustrasyonlu bos durum ekrani
- **Internet kesilmesi:** "Baglanti yok" mesaji + yeniden dene butonu
- **Hesap suresi dolmus:** Login ekranina yonlendir + "Hesabinizin suresi dolmus" mesaji

## V1 Kapsam Disi (Ileride Eklenebilir)

- EPG (Elektronik Program Rehberi) canli TV icin
- Coklu sunucu / profil yonetimi
- Offline cache (Room tablolari ile)
- RTMP stream destegi
- Custom kedi illustrasyonlari (vector placeholder'lar yerine)
- Coklu dil destegi (EN vb.)
- Varsayilan kalite ayari (cogu Xtream stream tek bitrate)
