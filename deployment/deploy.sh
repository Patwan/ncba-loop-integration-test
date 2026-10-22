#!/usr/bin/env bash
set -euo pipefail

CLUSTER=microservices-cluster
NS=microservices

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=== 1. Create Kind cluster (if missing) ==="
if ! kind get clusters | grep -q "^${CLUSTER}$"; then
  kind create cluster --name "${CLUSTER}"
else
  echo "Cluster '${CLUSTER}' already exists."
fi


echo "=== 2. Build Docker images (skip if exists) ==="
build_if_missing() {
  local IMAGE_NAME="$1"
  local CONTEXT_PATH="$2"

  if docker image inspect "$IMAGE_NAME" >/dev/null 2>&1; then
    echo "Image '$IMAGE_NAME' already exists locally, skipping build."
  else
    echo "Building '$IMAGE_NAME'..."
    docker build -t "$IMAGE_NAME" "$CONTEXT_PATH"
  fi
}

build_if_missing "ncba-loop-test-eureka-server:latest" "$ROOT_DIR/eureka-server"
build_if_missing "ncba-loop-test-crud-service:latest"  "$ROOT_DIR/crud-service"
build_if_missing "ncba-loop-test-api-gateway:latest"   "$ROOT_DIR/api-gateway"

echo "=== 3. Load images into Kind ==="
kind load docker-image ncba-loop-test-eureka-server:latest --name "${CLUSTER}"
kind load docker-image ncba-loop-test-crud-service:latest  --name "${CLUSTER}"
kind load docker-image ncba-loop-test-api-gateway:latest   --name "${CLUSTER}"

echo "=== 4. Apply manifests (order matters) ==="
kubectl apply -f k8s/01-namespace.yaml
kubectl apply -f k8s/02-configmap.yaml
kubectl apply -f k8s/03-secret.yaml
kubectl apply -f k8s/04-storage.yaml
kubectl apply -f k8s/05-mysql.yaml
kubectl apply -f k8s/06-redis.yaml
kubectl apply -f k8s/07-eureka-server.yaml
kubectl apply -f k8s/08-crud-service.yaml
kubectl apply -f k8s/09-api-gateway.yaml

echo "=== 5. Wait for pods ==="
kubectl wait --for=condition=ready pod -l app=mysql         -n "${NS}" --timeout=180s
kubectl wait --for=condition=ready pod -l app=redis         -n "${NS}" --timeout=60s
kubectl wait --for=condition=ready pod -l app=eureka-server -n "${NS}" --timeout=180s
kubectl wait --for=condition=ready pod -l app=crud-service  -n "${NS}" --timeout=240s

echo "=== Done ==="
kubectl get pods -n "${NS}"