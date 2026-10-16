# Policy for Service A (Order Service)
# Least-privilege: Can only read secrets under secret/data/service-a/*
path "secret/data/service-a/*" {
  capabilities = ["read"]
}

path "secret/metadata/service-a/*" {
  capabilities = ["list", "read"]
}
