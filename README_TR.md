# NEXA Browser

NEXA, Android için hazırlanmış hafif bir WebView tarayıcı başlangıç projesidir.

## Özellikler
- URL açma ve Google araması
- Sekme oluşturma, sekmeler arasında geçiş ve sekme kapatma
- Geri / ileri / ana sayfa / yenile
- Yer imleri ve son ziyaret edilen sayfalar
- Android İndirme Yöneticisi ile indirme

## Derleme
Projeyi Android Studio ile açın. Android SDK Platform 35 ve JDK 17 kurulu olmalı.
Build > Build Bundle(s) / APK(s) > Build APK(s)

APK: app/build/outputs/apk/debug/app-debug.apk

## Notlar
Android System WebView kullanır; ayrı bir Chromium motoru içermez.
Reklam engelleyici, gizli mod ve senkronizasyon henüz eklenmedi.