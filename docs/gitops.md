# Argo CD & GitOps Mimarisi

## Prensip
Bu platformda **GitOps** modeli uygulanır:
- **CI (GitHub Actions):** Kod derleme, test çalıştırma, container image build etme ve GHCR'ye push işlemlerinden sorumludur. GitHub Actions asla doğrudan Kubernetes cluster'ına `kubectl apply` ile deployment yapmaz.
- **CD (Argo CD):** Git deposundaki Helm yapılandırmasını (`infra/helm/`) ve arzulanan durumu (desired state) izler. Image tag değiştiğinde veya Helm değerleri güncellendiğinde cluster'ı bu durumla otomatik olarak eşitler (Sync).

## Bileşenler

| Bileşen | Namespace | Sorumluluk |
|---|---|---|
| Argo CD Server | `argocd` | Web UI & API |
| Application Controller | `argocd` | Git ve K8s state karşılaştırma & senkronizasyon |
| Repo Server | `argocd` | Git klonlama ve Helm manifest oluşturma |
| Service A App | `argocd` | `infra/helm/service-a` chart senkronizasyonu |
| Service B App | `argocd` | `infra/helm/service-b` chart senkronizasyonu |

## Yerel Erişim
```bash
# Argo CD UI port-forward (varsayılan kullanıcı: admin)
kubectl port-forward svc/argocd-server -n argocd 8080:443

# İlk admin şifresini alma:
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d; echo
```
