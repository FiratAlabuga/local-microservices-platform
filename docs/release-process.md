# Sürüm Yönetimi ve Release Süreci

## Versiyonlama Standardı
Proje **Semantic Versioning (SemVer 2.0.0)** ve **Conventional Commits** kurallarına tabidir.

### Commit Türleri
- `feat:` Yeni bir özellik eklenmesi (MINOR sürümü artırır)
- `fix:` Hata düzeltmesi (PATCH sürümü artırır)
- `feat!:`, `fix!:` veya `BREAKING CHANGE:` Geriye dönük uyumsuzluk (MAJOR sürümü artırır)
- `docs:`, `chore:`, `ci:`, `test:`, `build:` Versiyon artışı tetiklemez (veya isteğe bağlı PATCH)

## Release Otomasyonu
1. Geliştirici pull request açar ve CI kontrolleri yeşile döner.
2. PR `main` branch'ine merge edilir.
3. Otomatik sürüm etiketi (`vX.Y.Z`) oluşturulur veya manuel olarak workflow dispatch edilir.
4. GHCR'ye immutable tag ve latest etiketiyle imajlar basılır.
5. GitOps manifestleri güncellenir ve Argo CD cluster'ı eşitler.
