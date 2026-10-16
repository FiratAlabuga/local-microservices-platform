# HashiCorp Vault Secret Yönetimi

## Genel Bakış
Local geliştirmede mikroservislerin hassas konfigürasyonları (veritabanı şifreleri, Redis auth token'ları) HashiCorp Vault üzerinde güvenli ve yalıtılmış (least-privilege) biçimde yönetilir.

> [!WARNING]
> Local ortamda Docker Compose içindeki Vault `dev` profilinde çalıştırılmaktadır.
> `dev` profili yalnızca yerel testler içindir; üretim ortamında asla dev mod kullanılmamalı, raft/consul backend, TLS ve unseal anahtarlarıyla kurulmalıdır.

## Path ve Politika Tasarımı

| Servis | Secret Yolu | Politika Dosyası | İzin Verilen Eylemler |
|---|---|---|---|
| Service A | `secret/data/service-a/*` | `infra/vault/policy-service-a.hcl` | Sadece kendi yolunu okuma |
| Service B | `secret/data/service-b/*` | `infra/vault/policy-service-b.hcl` | Sadece kendi yolunu okuma |

### İzolasyon Doğrulaması
- Service A'ya ait token `secret/data/service-b/config` yoluna erişmeyi denediğinde Vault HTTP 403 Permission Denied yanıtı döner.
- Service B'ye ait token `secret/data/service-a/config` yoluna erişmeyi denediğinde aynı şekilde engellenir.

## Başlatma ve Doğrulama
```bash
./infra/vault/init-vault.sh
```
