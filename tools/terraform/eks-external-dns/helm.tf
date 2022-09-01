resource "helm_release" "external-dns" {
  count = var.create ? 1 : 0
  name      = "external-dns"
  namespace = local.k8s_service_namespace
  chart     = "https://github.com/kubernetes-sigs/external-dns/releases/download/external-dns-helm-chart-1.11.0/external-dns-1.11.0.tgz"

  lifecycle {
    create_before_destroy = true
  }

  set {
    name = "serviceAccount.create"
    value = false
  }

  set {
    name  = "serviceAccount.name"
    value = local.k8s_service_account_name
  }

  set {
    name = "provider"
    value = "aws"
  }

  set {
    name = "policy"
    value = "sync"
  }

  set {
    name = "aws.zoneType"
    value = "public"
  }

  set {
    name = "domainFilters[0]"
    value = var.zone_name
  }

  set {
    name = "txtOwnerId"
    value = var.zone_id
    type = "string"
  }

  depends_on = [
    module.iam_assumable_role_admin
  ]
}