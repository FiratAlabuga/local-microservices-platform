# CI/CD ve Pipeline Tasarımı

## CI (Continuous Integration)
- Workflow: `.github/workflows/ci.yaml`
- Tetiklenme: `main` branch'ine push ve pull request açılışları
- Aşamalar:
  1. **Java 25 Derleme ve Test:** `./gradlew test bootJar`
  2. **Helm Lint ve Şablon Doğrulama:** `helm lint` & `helm template`
  3. **Docker Build ve Güvenlik Taraması:** Multi-stage image build, Trivy ile vulnerability scanning

## CD (Continuous Delivery & GitOps)
- Release Workflow: `.github/workflows/release.yaml`
- Model: **Pull-based GitOps (Argo CD)**
- Akış:
  1. GitHub Actions yeni imajı derler ve GitHub Container Registry'ye (GHCR) push eder:
     `ghcr.io/<owner>/local-microservices-platform/service-a:v1.0.0`
  2. GitHub Actions, `infra/helm/*/values.yaml` dosyasındaki `image.tag` alanını günceller.
  3. Argo CD, Git deposundaki değişikliği algılar ve Kubernetes cluster'ındaki deployment'ı otomatik günceller (Self-heal & Auto-sync).
