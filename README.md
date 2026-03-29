# 🐱 KediBiloTV

Netflix benzeri, kedi temalı Android IPTV uygulaması. Android TV, telefon ve tablet destekler.

## Özellikler

- **Xtream Codes API** desteği ile Canlı TV, Film ve Dizi içerikleri
- **Devam et** — Kapattığın yerden kaldığın yerden izlemeye devam
- **Favoriler** — İstediğin içerikleri kaydet
- **Kedi teması** — Neşeli, renkli ve sinema tadında arayüz
- **Düşük kaynak kullanımı** — 2GB RAM cihazlarda sorunsuz çalışır
- **TV + Mobil** — Tek APK, runtime'da platform algılanır

## Kurulum

### Gereksinimler

- Android Studio Hedgehog veya üstü
- JDK 17
- Android SDK 34
- minSdk 24

### Projeyi Klonla

```bash
git clone https://github.com/KULLANICI_ADI/KediBiloTV.git
cd KediBiloTV
```

### Build

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

### Test

```bash
# Unit testler
./gradlew test

# Instrumented testler (bağlı cihaz gerekli)
./gradlew connectedAndroidTest
```

## Kullanım

1. Uygulamayı aç
2. Xtream Codes sunucu bilgilerini gir:
   - **Sunucu URL** — Örnek: `http://sunucu.com:8080`
   - **Kullanıcı Adı**
   - **Şifre**
3. "Bağlan" butonuna bas
4. Canlı TV, Filmler veya Diziler kategorilerini seç

## Teknoloji Stack

| Bileşen | Teknoloji |
|---------|-----------|
| Dil | Kotlin |
| UI (Mobil) | Jetpack Compose + Material3 |
| UI (TV) | Compose TV |
| Navigasyon | Compose Navigation |
| Video Oynatıcı | Media3 / ExoPlayer |
| Veritabanı | Room |
| Ağ | Ktor Client |
| DI | Hilt |
| Resim Yükleme | Coil |
| Async | Coroutines + Flow |

## Proje Yapısı

```
app/src/main/java/com/kedibilotv/
├── data/
│   ├── api/          # Xtream Codes API servisi ve DTO'lar
│   ├── db/           # Room veritabanı, entity'ler, DAO'lar
│   └── repository/   # Repository implementasyonları
├── domain/
│   ├── model/        # Domain modelleri
│   ├── repository/   # Repository arayüzleri
│   └── usecase/      # İş mantığı use case'leri
├── ui/
│   ├── common/       # Paylaşılan UI bileşenleri
│   ├── theme/        # KediBilo teması, renkler, tipografi
│   ├── navigation/   # NavHost ve route tanımları
│   ├── login/        # Giriş ekranı
│   ├── home/         # Ana ekran (TV + Mobil)
│   ├── category/     # Kategori listesi
│   ├── content/      # İçerik listesi + arama
│   ├── detail/       # Film/Dizi detay ekranı
│   ├── player/       # Video oynatıcı
│   └── settings/     # Ayarlar
├── player/           # Media3 player wrapper
└── di/               # Hilt DI modülleri
```

## Uygulama İçi Ekranlar

| Ekran | Açıklama |
|-------|----------|
| Login | Sunucu bağlantı bilgileri girişi |
| Home | Banner, Devam Et, Favoriler, Kategoriler |
| Category | Alt kategori grid görünümü |
| Content List | Poster kartları + anlık arama filtresi |
| Detail | Film/Dizi bilgisi, sezon/bölüm seçici, favori toggle |
| Player | Tam ekran Media3 oynatıcı, D-pad + gesture kontrolü |
| Settings | Buffer boyutu, çıkış |

## Geliştirme Durumu

| Task | Durum |
|------|-------|
| Proje altyapısı (Gradle, Manifest) | ✅ Tamamlandı |
| Domain modelleri ve repository arayüzleri | ✅ Tamamlandı |
| Room veritabanı (Favoriler, Geçmiş, Sunucu) | ✅ Tamamlandı |
| Xtream Codes API servisi | ✅ Tamamlandı |
| Repository implementasyonları ve DI | ✅ Tamamlandı |
| Tema, ortak UI bileşenleri, navigasyon | ✅ Tamamlandı |
| Login ekranı | ✅ Tamamlandı |
| Ana ekran (Home) | ✅ Tamamlandı |
| Kategori ve içerik listesi | ✅ Tamamlandı |
| Detay ekranı | ✅ Tamamlandı |
| Oynatıcı ekranı | ✅ Tamamlandı |
| Ayarlar ekranı | ✅ Tamamlandı |

## Kapsam Dışı (V1)

Şu an geliştirilmeyecek, ileride eklenebilir:
- EPG (Elektronik Program Rehberi)
- Çoklu sunucu / profil yönetimi
- Offline cache
- RTMP stream desteği
- Özel kedi illüstrasyonları
- Çok dilli destek

## Lisans

MIT
