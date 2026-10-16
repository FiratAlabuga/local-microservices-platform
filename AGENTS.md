# AGENTS.md — Local Microservices Platform

## 1. Rolün ve çalışma biçimin

Sen kıdemli bir Java Platform Engineer, DevOps Engineer ve Kubernetes/GitOps uzmanı olarak çalışıyorsun.

Görevin, sıfırdan çalışan ve GitHub üzerinde versiyonlanabilen bir local microservices platformu oluşturmaktır.

Yalnızca örnek kod veya dokümantasyon üretme. Çalışma ortamında dosyaları oluştur, komutları çalıştır, testleri gerçekleştir, hataları gider ve sonuçları doğrula.

### Zorunlu çalışma kuralları

1. İlk olarak çalışma dizinini, mevcut dosyaları, işletim sistemini ve kurulu araçları incele.
2. Mevcut dosyaları kullanıcı onayı olmadan silme veya üzerine yazma.
3. Bir aşamayı tamamlanmış saymadan önce doğrulama komutlarını çalıştır.
4. Bir komut başarısız olursa hata nedenini analiz et, düzelt ve yeniden dene.
5. Gerçekleştirmediğin bir testi başarılı olarak raporlama.
6. Kullanıcıdan habersiz teknoloji veya major/minor sürüm değiştirme.
7. Erişim bilgilerini, tokenları ve parolaları kaynak koduna yazma.
8. Gerekli bilgi veya yetki eksikse bunu açıkça belirt. Güvenli şekilde devam edilebilen işleri durdurma.
9. Her aşamada oluşturulan dosyaları, kullanılan komutları ve test sonuçlarını açıkla.
10. Bir sonraki aşamaya geçmeden önce mevcut aşamanın kabul kriterlerini kontrol et.
11. Gereksiz bağımlılık, karmaşık soyutlama veya sırf mimari gösteriş için bileşen ekleme.
12. Gerçek ortamda uygulanabilir, tekrar çalıştırılabilir ve bakımı kolay çözümler üret.

Açıklamalar Türkçe, kodlar ve teknik isimlendirmeler İngilizce olsun.

---

## 2. Projenin hedefi

Tek GitHub deposunda iki bağımsız Spring Boot mikroservisi, yerel altyapı, Kubernetes deployment'ları, GitOps, CI/CD ve release yönetimi oluştur.

### Sabit teknoloji gereksinimleri

| Bileşen | Sürüm / Detay |
|---|---|
| Java | Amazon Corretto 25 (25.0.3+) |
| Spring Boot | 4.1.1 |
| Build sistemi | Gradle Wrapper (gradlew) — Gradle 9.5.1 |
| Veritabanı | PostgreSQL 16 Alpine |
| Cache | Redis 7.2 Alpine |
| Kafka | confluentinc/cp-kafka:8.3.0, KRaft modu |
| Kafka UI | provectuslabs/kafka-ui — sabitlenmiş ve doğrulanmış etiket |
| Container runtime | Docker |
| Local Kubernetes | kind |
| Package management | Helm |
| GitOps | Argo CD |
| Secret management | HashiCorp Vault |
| CI/CD | GitHub Actions |
| Container registry | GitHub Container Registry (GHCR) |
| Release versioning | Semantic Versioning (SemVer) |
| Repository | GitHub monorepo |

Tüm sürümler merkezi bir `versions.properties` veya kök `pom.xml` properties bloğunda ve ilgili Docker/Helm dosyalarında tek yerden yönetilmelidir.

### Sürüm doğrulama politikası

- Java 25 ve Spring Boot 4.1.1 ile gerçek Maven build çalıştır.
- Docker image etiketlerinin registry'de bulunup bulunmadığını `docker pull --dry-run` veya manifest sorgusuyla doğrula.
- Kafka için önce tam olarak `confluentinc/cp-kafka:8.3.0` etiketini kontrol et.
- İstenen image bulunamazsa otomatik olarak `8.3.2`, `latest` veya başka bir etikete geçme.
- Bulunamayan etiketleri ve etkilerini raporla; alternatif sürüm için kullanıcı onayı iste.
- Üretim amaçlı yapılandırmalarda `latest` kullanma.
- CI/CD'de mümkün olduğunda image digest sabitlemesini destekle.
- Bağımlılık ve container güvenlik taramalarını ekle.

---

## 3. Mikroservis tasarımı

İki bağımsız, test edilebilir ve container içinde çalıştırılabilir mikroservis oluştur.

### Service A: Order Service

**Sorumluluklar:**

- Sipariş oluşturma ve sorgulama REST API'leri
- PostgreSQL üzerinden kalıcı veri saklama
- Redis üzerinden uygun cache kullanımı
- Kafka'ya sipariş oluşturma olayı gönderme
- Health ve readiness endpoint'leri
- Structured logging ve correlation ID

**Endpoint'ler:**

```
GET  /api/v1/orders/{id}
GET  /api/v1/orders
POST /api/v1/orders
GET  /actuator/health
GET  /actuator/health/readiness
GET  /actuator/health/liveness
```

Sipariş oluşturma olayını `order.created` Kafka topic'ine gönder.

### Service B: Notification Service

**Sorumluluklar:**

- Kafka üzerinden sipariş olaylarını tüketme
- Olayı doğrulama ve işleme
- PostgreSQL'de işlenmiş olayları ve bildirim kayıtlarını saklama
- Aynı olayın tekrar işlenmesine karşı idempotency sağlama
- Health ve readiness endpoint'leri
- Structured logging ve correlation ID

Notification Service, `order.created` topic'ini tüketmeli ve deterministik bir bildirim kaydı oluşturmalıdır. Gerçek e-posta veya SMS zorunlu değildir.

### Servisler arası iletişim

- Order Service senkron REST API sunar.
- Order Service, sipariş oluşturulduktan sonra Kafka olayı üretir.
- Notification Service olayı asenkron tüketir.
- Servisler birbirlerinin veritabanı tablolarına doğrudan erişmez.
- Servisler bağımsız build ve container image üretebilmelidir.
- Event schema açıkça tanımlanmalıdır (Java record veya DTO).
- Kafka producer/consumer hata yönetimi, retry ve idempotency ele alınmalıdır.
- İlk aşamada Schema Registry zorunlu değildir.

### Veri modeli

**Order:**

| Alan | Tip | Açıklama |
|---|---|---|
| id | UUID | Birincil anahtar |
| customerId | UUID | Müşteri referansı |
| status | ENUM | PENDING, CONFIRMED, CANCELLED |
| totalAmount | BigDecimal | Sipariş tutarı |
| createdAt | Instant | Oluşturma zamanı |
| updatedAt | Instant | Güncelleme zamanı |

**Notification:**

| Alan | Tip | Açıklama |
|---|---|---|
| id | UUID | Birincil anahtar |
| eventId | UUID | Kafka mesaj idempotency anahtarı |
| orderId | UUID | İlişkili sipariş |
| status | ENUM | PENDING, PROCESSED, FAILED |
| createdAt | Instant | Oluşturma zamanı |

UUID tabanlı kimlikler, uygun indeksler ve zaman damgaları kullan.

Veritabanı şema yönetimi için **Flyway** kullan. Spring Boot 4.1.1 ile uyumluluğu doğrula.

---

## 4. Repository yapısı

```
local-microservices-platform/
├── AGENTS.md
├── README.md
├── pom.xml                          ← Maven multi-module root
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── mvnw
├── mvnw.cmd
├── services/
│   ├── service-a/                   ← Order Service
│   │   ├── pom.xml
│   │   ├── Dockerfile
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/
│   │       │   └── resources/
│   │       └── test/
│   └── service-b/                   ← Notification Service
│       ├── pom.xml
│       ├── Dockerfile
│       └── src/
│           ├── main/
│           │   ├── java/
│           │   └── resources/
│           └── test/
├── infra/
│   ├── docker/
│   │   ├── compose.yaml
│   │   └── .env.example
│   ├── kind/
│   │   └── cluster-config.yaml
│   ├── helm/
│   │   ├── service-a/
│   │   │   ├── Chart.yaml
│   │   │   ├── values.yaml
│   │   │   ├── values-local.yaml
│   │   │   ├── values-ci.yaml
│   │   │   └── templates/
│   │   ├── service-b/
│   │   │   ├── Chart.yaml
│   │   │   ├── values.yaml
│   │   │   ├── values-local.yaml
│   │   │   ├── values-ci.yaml
│   │   │   └── templates/
│   │   └── platform/
│   │       ├── Chart.yaml
│   │       └── values.yaml
│   ├── argocd/
│   │   ├── service-a-app.yaml
│   │   └── service-b-app.yaml
│   └── vault/
│       ├── policy-service-a.hcl
│       ├── policy-service-b.hcl
│       └── init-vault.sh
├── .github/
│   └── workflows/
│       ├── ci.yaml
│       ├── release.yaml
│       └── cd-update.yaml
├── scripts/
│   ├── check-prerequisites.sh
│   ├── start-local.sh
│   ├── create-cluster.sh
│   ├── deploy-local.sh
│   ├── smoke-test.sh
│   └── stop-local.sh
├── docs/
│   ├── architecture.md
│   ├── local-setup.md
│   ├── ci-cd.md
│   ├── gitops.md
│   ├── vault.md
│   └── release-process.md
├── .gitignore
├── .editorconfig
└── .releaserc.json
```

Maven multi-module yapı kullan. İki servis bağımsız çalıştırılabilmeli ve kendi image'larını oluşturabilmelidir.

**Maven Wrapper dosyalarını gerçekten oluştur.** `mvn wrapper:wrapper` komutuyla yada doğru binary içeriğiyle oluştur; elle sahte içerikle üretme.

---

## 5. Aşamalı uygulama planı

### Aşama 0 — Ortam keşfi

Önce şunları kontrol et:

```bash
uname -a
git --version
java -version
docker version
docker compose version
kubectl version --client
kind version
helm version
gh --version
```

Eksik araçları listele. Kurulum için işletim sistemine uygun talimatlar hazırla.

**Kabul kriteri:** Ortam raporu hazırlanmış, eksikler belirlenmiş ve proje dizini güvenli biçimde seçilmiş olmalı.

---

### Aşama 1 — Git repository ve iskelet

1. Repository dizinini oluştur: `~/local-microservices-platform`
2. `git init` ile Git deposu başlat.
3. Maven multi-module root `pom.xml` oluştur (parent POM).
4. İki servis için minimal Spring Boot 4.1.1 uygulaması scaffold et.
5. `mvn wrapper:wrapper` ile gerçek Maven Wrapper oluştur.
6. `.gitignore` ve `.editorconfig` ekle.
7. İlk `README.md` ve `docs/architecture.md` oluştur.
8. `./mvnw clean verify` ile build'i doğrula.

**Java compiler target:** `pom.xml` içinde `<maven.compiler.release>25</maven.compiler.release>` olarak ayarla.

**Spring Boot 4.1.1 BOM:** Parent veya import olarak `spring-boot-starter-parent:4.1.1` kullan. Bağımlılık uyumsuzluğu varsa raporla.

**Kabul kriteri:** İki servis derleniyor (`BUILD SUCCESS`), testler çalışıyor ve temiz bir Git working tree elde edilebiliyor.

---

### Aşama 2 — Servislerin geliştirilmesi

Order Service ve Notification Service'i tamamla.

Her serviste:

- REST API veya Kafka consumer implementasyonu
- Flyway migration dosyaları
- Redis cache entegrasyonu (Order Service)
- Kafka producer (Order Service) / consumer (Notification Service)
- Unit testler
- Integration testler (Testcontainers kullanabilirsin; Docker yoksa skip et ve raporla)
- Validation ve hata yönetimi (`@Valid`, `ControllerAdvice`)
- Structured logging (JSON format, `correlation-id` MDC)
- Spring Boot Actuator health endpoint'leri
- Environment variable üzerinden konfigürasyon (`application.yml` + `@ConfigurationProperties`)

**Kabul kriteri:** Sipariş oluşturulabiliyor, Kafka olayı yayınlanabiliyor, Notification Service olayı tüketebiliyor ve testler beklenen sonuçları doğruluyor.

---

### Aşama 3 — Docker Compose ile local altyapı

`infra/docker/compose.yaml` oluştur. Şunları dahil et:

- PostgreSQL 16 Alpine
- Redis 7.2 Alpine
- `confluentinc/cp-kafka:8.3.0` — KRaft modunda
- Kafka UI (doğrulanmış sabitlenmiş etiket)
- HashiCorp Vault (dev profili — local only)

**Kafka KRaft yapılandırması için zorunlular:**
- `KAFKA_NODE_ID`, `KAFKA_PROCESS_ROLES=broker,controller`
- `KAFKA_CONTROLLER_QUORUM_VOTERS`
- `KAFKA_LISTENERS` ve `KAFKA_ADVERTISED_LISTENERS` ayrımı
- `KAFKA_LISTENER_SECURITY_PROTOCOL_MAP`
- Kalıcı volume
- Healthcheck

**PostgreSQL ve Redis için:** healthcheck ekle; servisler bağımlılıkların hazır olmasına (`depends_on: condition: service_healthy`) göre başlasın.

Parolaları `.env.example` içinde yalnızca örnek (`changeme`, `dev-only`) değerlerle göster. Gerçek `.env` dosyası `.gitignore`'a eklensin.

```bash
docker compose -f infra/docker/compose.yaml config   # YAML parse kontrolü
docker compose -f infra/docker/compose.yaml pull     # Image doğrulama
docker compose -f infra/docker/compose.yaml up -d
docker compose -f infra/docker/compose.yaml ps
```

**`8.3.0` etiketi bulunamazsa:** Bunu açıkça raporla, `8.3.2` veya başka sürüme otomatik geçme, kullanıcı onayı iste.

**Kabul kriteri:** PostgreSQL bağlantısı, Redis ping ve Kafka topic üretme/tüketme doğrulanmış.

---

### Aşama 4 — kind Kubernetes cluster

```bash
kind create cluster --config infra/kind/cluster-config.yaml --name microservices-local
kubectl cluster-info --context kind-microservices-local
```

- `microservices` ve `monitoring` namespace'leri oluştur.
- Ingress controller veya port-forward seçeneğini belgele.
- Resource request/limit değerlerini tanımla.
- non-root container çalıştırma politikası uygula.

**Kabul kriteri:** Cluster hazır, namespace'ler mevcut, basit test uygulaması erişilebilir.

---

### Aşama 5 — Helm chart'ları

Her servis için bağımsız Helm chart:

```
infra/helm/service-a/
  Chart.yaml
  values.yaml
  values-local.yaml
  values-ci.yaml
  templates/
    deployment.yaml
    service.yaml
    configmap.yaml
    serviceaccount.yaml
    hpa.yaml (isteğe bağlı)
    NOTES.txt

infra/helm/service-b/  ← aynı yapı
```

- Secret değerlerini `values.yaml` içine koyma.
- Resource limit, liveness/readiness/startup probe, replica ve securityContext ayarları zorunlu.
- Image repository ve tag açıkça tanımlanmalı.

```bash
helm lint infra/helm/service-a
helm lint infra/helm/service-b
helm template service-a infra/helm/service-a -f infra/helm/service-a/values.yaml
helm template service-b infra/helm/service-b -f infra/helm/service-b/values.yaml
```

**Kabul kriteri:** Chart'lar render ediliyor, Kubernetes kaynakları geçerli, Helm ile install/upgrade/rollback çalışıyor.

---

### Aşama 6 — Vault secret yönetimi

- Local için Vault dev modu — **yalnızca local/test** — dokümanlarda açıkça belirt.
- Secret path tasarımı: `secret/service-a/config`, `secret/service-b/config`
- Policy: her servis yalnızca kendi path'ine erişebilir (least privilege).
- Kubernetes auth method ile servis kimliği doğrulama.
- Secret değerlerini Git'e commit etme.
- Vault erişilemezse uygulama davranışını belirle (fail-fast veya graceful degradation — belgelenmiş).

Local Kubernetes'te servislerin Vault'tan secret okuyabildiğini gerçek testle doğrula.

**Kabul kriteri:** Yetkili servis secret okuyor; yetkisiz erişim engelleniyor.

---

### Aşama 7 — Argo CD ve GitOps

```bash
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/install.yaml
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

- `infra/argocd/service-a-app.yaml` ve `service-b-app.yaml` oluştur.
- Automated sync, self-heal, prune ayarlarını belirli bir politikaya göre seç ve belgelendir.
- GitHub Actions doğrudan `kubectl apply` YAPMAZ — sadece image üretir.
- Argo CD Git'teki Helm chart'ları izler ve cluster'ı bu state'e eşitler.

Local Git repo henüz GitHub'a bağlı değilse, local GitOps testini ayrı yöntemle gerçekleştir ve bunu açıkça belirt.

**Kabul kriteri:** Argo CD Application synced ve healthy, Git değişikliği deployment'a yansıyor.

---

### Aşama 8 — GitHub Actions CI

`.github/workflows/ci.yaml` içinde en az:

1. PR build ve unit test
2. Integration test
3. Docker image build (push olmadan)
4. Trivy ile dependency ve container vulnerability taraması
5. `helm lint` ve template doğrulaması
6. Kubernetes manifest doğrulaması (kubeval veya kubeconform)

**Güvenlik kuralları:**
- Minimum `permissions` tanımla (workflow ve job düzeyinde)
- Üçüncü taraf Actions sürümlerini `@sha256:...` veya `@vX.Y.Z` ile sabitle
- Fork PR'larında ayrıcalıklı secret erişimini engelle (`pull_request_target` kullanma)
- Test başarısızsa workflow başarısız olsun
- Secret ve token değerlerini loglama

**Kabul kriteri:** PR açıldığında workflow çalışıyor; hatalı kod build'de kırılıyor.

---

### Aşama 9 — GHCR ve CD

```
ghcr.io/<owner>/<repo>/service-a:<tag>
ghcr.io/<owner>/<repo>/service-b:<tag>
```

**Gerçek owner ve repo adını GitHub'dan doğruladıktan sonra kullan. Placeholder koyma.**

**Tag stratejisi:**
- PR: `pr-<number>-<sha7>` (geçici, test amaçlı)
- Main: `sha-<sha7>` (immutable)
- Release: `v1.2.3` (SemVer)
- `latest` yalnızca açık politikayla ve main branch'te

GitOps güncelleme: yeni image başarıyla build edildiğinde Helm chart `values.yaml`'ındaki image tag'i güncelleyen ayrı bir job veya PR akışı kullan.

**Kabul kriteri:** Image GHCR'ye yükleniyor, GitOps manifestinde yeni sürüm referansı var, Argo CD deployment tamamlıyor.

---

### Aşama 10 — Release ve versiyonlama

**Conventional Commits:**

```
feat:     yeni özellik
fix:      hata düzeltmesi
docs:     yalnızca dokümantasyon
refactor: yeniden yapılandırma
test:     test ekleme/güncelleme
build:    build sistemi değişikliği
ci:       CI konfigürasyonu
chore:    rutin görev
```

Breaking change: `feat!:` veya commit body'de `BREAKING CHANGE:`.

**Release otomasyonu:** `release-please` veya `semantic-release` — ikisinden birini seç; gerekçeyi yaz.

**Release akışı:**
1. Commit convention kontrolü
2. Version hesaplama
3. Changelog oluşturma
4. Release PR veya tag mekanizması
5. Git tag ve GitHub Release
6. Image build ve GHCR push
7. GitOps manifest güncelleme
8. Deployment ve smoke test
9. Hata durumunda rollback prosedürü

**Kabul kriteri:** Deneme release'iyle sürüm etiketi, changelog, image ve GitOps güncelleme adımları doğrulanmış.

---

## 6. GitHub bağlantısı

```bash
gh auth status
git status
git remote -v
```

Kimlik doğrulaması yoksa:

```bash
gh auth login
```

Kullanıcının hesabı, organization ve repo adı konusunda tahmin yürütme.

İlk push öncesi kontroller:
1. `.env` ve secret dosyalarının `.gitignore`'da olduğunu doğrula
2. Token, parola, private key, Vault root token sızıntısı için `git diff --cached` incele
3. Remote URL'yi doğrula

```bash
git add .
git diff --cached
git commit -m "chore: initialize local microservices platform"
git push -u origin main
```

GitHub Actions workflow'larının gerçekten tetiklendiğini kontrol et.

---

## 7. Güvenlik kuralları

- Secret'lar hiçbir zaman kaynak koduna veya image katmanlarına yazılmamalı.
- `.env.example` yalnızca sahte/placeholder değerler içermeli.
- Gerçek `.env`, Vault token, kubeconfig ve private key'ler `.gitignore`'da olmalı.
- CI izinleri least-privilege prensibine uygun.
- Container'lar non-root çalışmalı (`runAsNonRoot: true`, `runAsUser: 1000`).
- Base image'lar ve GitHub Actions versiyonları sabitlenmeli.
- Trivy veya Grype ile kritik güvenlik bulguları CI'da bloke olarak işaretlenmeli.
- Local geliştirme parolaları ile production secret'ları birbirinden ayrılmalı.
- Vault dev mode production çözümü olarak sunulmamalı; README'de açıkça uyarı bulunmalı.
- GitHub Actions secret'ları manifest veya build log'a yazılmamalı.

---

## 8. Dokümantasyon zorunlulukları

`README.md` şunları içermeli:

- Proje amacı ve mimari diyagramı (ASCII veya Mermaid)
- Gereksinimler ve araç sürümleri
- İlk kurulum adımları
- Docker Compose ile başlatma
- kind cluster oluşturma
- Helm ile deploy
- Argo CD erişimi
- Vault kurulumu
- Kafka UI erişimi
- API örnekleri (curl)
- Test komutları
- GitHub bağlantısı
- CI/CD akışı
- Release oluşturma
- Rollback
- Temizleme
- Sık karşılaşılan sorunlar (Troubleshooting)

**Scripts (idempotent olmalı):**

```
scripts/check-prerequisites.sh
scripts/start-local.sh
scripts/create-cluster.sh
scripts/deploy-local.sh
scripts/smoke-test.sh
scripts/stop-local.sh
```

---

## 9. Son doğrulama senaryosu

Tüm aşamalar tamamlandıktan sonra uçtan uca testi çalıştır:

1. Local altyapıyı başlat (`./scripts/start-local.sh`)
2. PostgreSQL, Redis ve Kafka hazır olduğunu doğrula
3. Vault secret erişimini doğrula
4. Mikroservisleri derle ve testleri çalıştır
5. Container image'larını oluştur
6. kind cluster üzerinde Helm ile deployment yap
7. Argo CD senkronizasyonunu kontrol et
8. Order Service'e sipariş oluşturma isteği gönder:
   ```bash
   curl -X POST http://localhost:8080/api/v1/orders \
     -H "Content-Type: application/json" \
     -d '{"customerId":"00000000-0000-0000-0000-000000000001","totalAmount":99.99}'
   ```
9. PostgreSQL'de siparişin kaydedildiğini doğrula
10. Redis cache davranışını test et
11. Kafka UI veya CLI ile `order.created` olayını doğrula
12. Notification Service'in olayı tükettiğini doğrula
13. Notification kaydının PostgreSQL'de oluştuğunu kontrol et
14. Aynı olayı tekrar göndererek idempotency'yi test et
15. GitHub Actions CI sonuçlarını kontrol et
16. Test release'i ve GitOps güncelleme akışını doğrula

Bir bileşen çalışmıyorsa test sonucunu **başarısız veya engellenmiş** olarak işaretle.

---

## 10. Çalışma sonunda teslim edilecekler

- [ ] Çalışan iki mikroservis (Order + Notification)
- [ ] Maven Wrapper ve build yapılandırması
- [ ] Dockerfile'lar (multi-stage, non-root)
- [ ] Docker Compose altyapısı
- [ ] kind cluster yapılandırması
- [ ] Helm chart'ları (lint ve install doğrulanmış)
- [ ] Vault yapılandırması ve policy örnekleri
- [ ] Argo CD Application manifestleri
- [ ] GitHub Actions workflow'ları
- [ ] GHCR image yayınlama akışı
- [ ] SemVer ve changelog otomasyonu
- [ ] Testler ve smoke test scriptleri
- [ ] Kurulum ve sorun giderme dokümantasyonu
- [ ] GitHub repository bağlantısı, remote ve branch bilgisi
- [ ] Gerçek test sonuçları ve kalan sorunlar

### Son rapor formatı

Her aşama için:

```
## Aşama N — <İsim>
Durum: Başarılı / Başarısız / Engellenmiş
Oluşturulan dosyalar:
  - path/to/file
Çalıştırılan komutlar:
  $ komut
Test sonuçları: ...
Karşılaşılan sorunlar: ...
Çözülen sorunlar: ...
Kullanıcıdan beklenen işlemler: ...
```

---

**Önce Aşama 0'ı tamamla, sonra Aşama 1'e geç. Her aşamanın kabul kriterlerini doğrulamadan bir sonraki aşamaya geçme.**
