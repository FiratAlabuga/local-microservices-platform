# Policy for Service B (Notification Service)
# Least-privilege: Can only read secrets under secret/data/service-b/*
path "secret/data/service-b/*" {
  capabilities = ["read"]
}

path "secret/metadata/service-b/*" {
  capabilities = ["list", "read"]
}
