# MathQuiz – İki Kişilik Matematik Bilgi Yarışması (Android)

MathQuiz, **Mobil Programlama (Android / Java)** dersi kapsamında geliştirilmiş, iki oyunculu bir matematik bilgi yarışması uygulamasıdır.  
Uygulama, temel matematik işlemleri üzerinden rekabetçi ve sırayla oynanan bir oyun deneyimi sunar.

---

## 📌 Proje Genel Bilgileri

- Platform: **Android**
- Programlama Dili: **Java**
- IDE: **Android Studio**
- Veri Saklama: **SQLite**
- Mimari: **Activity tabanlı yapı**
- Tema Desteği: **Açık / Karanlık Mod**
- Oyuncu Sayısı: **2 (Aynı cihaz üzerinden)**

---

## 🎮 Oyun Özellikleri

### 🔹 Oyun Yapısı
- İki oyuncu aynı cihaz üzerinde sırayla oynar
- Her turda yalnızca bir oyuncu cevap verir
- Doğru cevap **10 puan** kazandırır
- Süre dolduğunda sıra otomatik olarak diğer oyuncuya geçer

### 🔹 Zorluk Seviyeleri
- **Kolay**
  - Tek basamaklı toplama, çıkarma, çarpma
- **Orta**
  - Tek basamak × iki basamak çarpma  
  - İki basamaklı toplama / çıkarma
- **Zor**
  - İki basamak × iki basamak çarpma  
  - Üç basamaklı toplama

### 🔹 Ayarlanabilir Seçenekler
- Zorluk seviyesi (Kolay / Orta / Zor)
- Soru sayısı (5, 7, 10)
- Soru süresi (15, 20, 30, 45, 60 saniye)
- Ses efektlerini açma / kapatma
- Karanlık mod açma / kapatma

---

## 🧠 Oyun Mekanikleri

- Oyuncular isimlerini girip avatar seçer
- Sorular, seçilen zorluk seviyesine göre dinamik olarak üretilir
- Her tur için geri sayım sayacı bulunur
- Görsel geri bildirimler:
  - Doğru cevap → Yeşil uyarı
  - Yanlış cevap → Kırmızı uyarı
  - Süre doldu → Turuncu uyarı
- Oyun sonunda sonuç ekranında:
  - Oyuncu skorları
  - Zorluk seviyesi
  - Kazanan oyuncu (yeşil renkle) gösterilir

---

## 🔊 Ses ve Arayüz Özellikleri

- Ses efektleri:
  - Doğru cevap
  - Yanlış cevap
  - Süre bitimine 3 saniye kala uyarı
- Skor artışı animasyonu
- Aktif oyuncuyu gösteren yanıp sönen sıra göstergesi
- Uygulama açılışında splash ekran
- Açık ve karanlık modlara uyumlu kullanıcı arayüzü

---

## 🗃️ Veri Saklama (SQLite)

- Oynanan tüm oyunlar SQLite veritabanında saklanır
- Saklanan bilgiler:
  - Oyuncu adları
  - Skorlar
  - Zorluk seviyesi
  - Kazanan oyuncu
- Geçmiş oyunlar ekranında tablo halinde listelenir

---

## 🌓 Karanlık Mod Desteği

- Karanlık mod ayarlardan açılıp kapatılabilir
- Splash ekranı her zaman açık tema ile gösterilir
- Diğer tüm ekranlar tema değişikliğine otomatik uyum sağlar
- Tema tercihi SharedPreferences ile saklanır

---

