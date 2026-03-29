# KediBiloTV — Project Instructions

## Stack
Android · Kotlin · Jetpack Compose + Compose TV · Media3 · Room · Ktor · Hilt · Coil

## Rules
- Türkçe iletişim
- Her task grubundan sonra README güncelle
- Single module, Clean Architecture (UseCase → Repository → ViewModel → Screen)
- TV ve mobil UI ayrı composable ama aynı ViewModel

## Design Context

### Brand Personality
**Playful · Çılgın · Cesur** — Kedi gibi: meraklı, beklenmedik, ama zarif iniş yapan.

### Aesthetic: "Neon Gatos Cinema"
Film noir poster estetiği + neon arcade enerjisi

| Token | Değer | Kullanım |
|-------|-------|---------|
| Background | `#040E0E` | Ana arka plan |
| Surface | `#0A1A1A` / `#112020` | Kartlar, yüzeyler |
| Primary | `#FF4500` | Butonlar, vurgular |
| Accent | `#00FFD1` | Elektrik camgöbeği — beklenmedik pop |
| Secondary | `#FF0080` | Favoriler, kalpler |
| Text Primary | `#F0F8F8` | Hafif teal tonu beyaz |
| Text Secondary | `#7AA8A8` | İkincil metin |

### Design Principles
1. **Cesur kontrast** — her ekranda en az bir "wow" anı
2. **Kedi enerjisi** — beklenmedik küçük detaylar, ama layout temiz
3. **Sinematik hiyerarşi** — içerik öne çıkar, UI ikinci planda
4. **Hız hissi** — geçişler ve yüklemeler anında hissettirilmeli
5. **Tutarlı ama sürprizli** — sistem tutarlı, her ekranda küçük keşif var
