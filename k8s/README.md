# Guía de Despliegue en Kubernetes (K8s) - BodegaPro

Este directorio contiene los manifiestos declarativos de Kubernetes para orquestar la arquitectura completa de microservicios de **BodegaPro** en un clúster local (**Docker Desktop con Kubernetes habilitado**, **Minikube**, **Kind**, o **K3s**).

---

## 1. Arquitectura de Despliegue en Kubernetes

Los manifiestos se organizan bajo el espacio de nombres (**Namespace**) `bodegapro`:

| Recurso | Tipo | Imagen Docker / Detalle | Puerto(s) | Exposición |
|---|---|---|---|---|
| **mysql** | Deployment / PVC / ConfigMap | `mysql:8.0` | 3306 (TCP) | ClusterIP |
| **rabbitmq** | Deployment | `rabbitmq:3-management-alpine` | 5672 (AMQP), 15672 (Web) | ClusterIP |
| **config-server** | Deployment | `bodegapro-config-server:latest` | 8888 (HTTP) | ClusterIP |
| **auth-service** | Deployment | `bodegapro-auth-service:latest` | 8081 (HTTP) | ClusterIP |
| **catalog-service** | Deployment | `bodegapro-catalog-service:latest` | 8082 (HTTP) | ClusterIP |
| **inventory-service** | Deployment | `bodegapro-inventory-service:latest` | 8083 (HTTP) | ClusterIP |
| **api-gateway** | Deployment | `bodegapro-api-gateway:latest` | 8080 (HTTP) | **LoadBalancer / NodePort (30080)** |

---

## 2. Requisitos Previos

1. **Docker y Kubernetes habilitados**:
   - En **Docker Desktop**: *Settings -> Kubernetes -> Enable Kubernetes -> Apply & Restart*.
   - O usando **Minikube**:
     ```bash
     minikube start --driver=docker
     ```
2. **Imágenes Docker Locales Construidas**:
   Las imágenes deben estar disponibles localmente (ya construidas en el Paso 7 mediante `docker compose up -d --build`). Los manifiestos usan la política `imagePullPolicy: IfNotPresent` para utilizar directamente las imágenes del daemon local.

   *Si utilizas Minikube*, carga las imágenes locales al entorno de Minikube con:
   ```bash
   minikube image load bodegapro-config-server:latest
   minikube image load bodegapro-auth-service:latest
   minikube image load bodegapro-catalog-service:latest
   minikube image load bodegapro-inventory-service:latest
   minikube image load bodegapro-api-gateway:latest
   ```

---

## 3. Despliegue en el Clúster

### Opción A: Despliegue Consolidado (Recomendado)
Aplica todos los manifiestos en una sola instrucción:
```bash
kubectl apply -f k8s/k8s-all-in-one.yaml
```

### Opción B: Despliegue Secuencial por Componentes
Aplica los manifiestos paso a paso:
```bash
# 1. Namespace, Secretos y ConfigMaps
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-secrets.yaml
kubectl apply -f k8s/02-configmaps.yaml

# 2. Infraestructura de Datos y Mensajería
kubectl apply -f k8s/03-mysql.yaml
kubectl apply -f k8s/04-rabbitmq.yaml

# 3. Config Server Centralizado
kubectl apply -f k8s/05-config-server.yaml

# 4. Microservicios de Dominio
kubectl apply -f k8s/06-auth-service.yaml
kubectl apply -f k8s/07-catalog-service.yaml
kubectl apply -f k8s/08-inventory-service.yaml

# 5. API Gateway
kubectl apply -f k8s/09-api-gateway.yaml
```

---

## 4. Verificación del Estado del Clúster

Para inspeccionar todos los recursos creados en el namespace `bodegapro`:

```bash
# Listar todos los pods y su estado
kubectl get pods -n bodegapro

# Listar los servicios y puertos asignados
kubectl get svc -n bodegapro

# Ver estado de los deployments
kubectl get deployments -n bodegapro

# Ver eventos del clúster en tiempo real
kubectl get events -n bodegapro --sort-by='.metadata.creationTimestamp'
```

---

## 5. Acceso a la Aplicación (Frontend y Pruebas)

### En Docker Desktop:
El servicio `api-gateway` de tipo `LoadBalancer` expone automáticamente el puerto `8080` en `localhost:8080`.
El frontend Angular puede consumir la API directamente en `http://localhost:8080`.

### En Minikube:
Si utilizas Minikube, puedes acceder mediante el comando tunnel o NodePort:
```bash
# Opción 1: Túnel para LoadBalancer
minikube tunnel

# Opción 2: Port-forward directo al Gateway
kubectl port-forward -n bodegapro svc/api-gateway 8080:8080
```

---

## 6. Monitoreo y Diagnóstico de Logs

```bash
# Logs del API Gateway en tiempo real
kubectl logs -n bodegapro deployment/api-gateway -f

# Logs del Catalog Service
kubectl logs -n bodegapro deployment/catalog-service -f

# Logs del Inventory Service (para ver eventos de RabbitMQ)
kubectl logs -n bodegapro deployment/inventory-service -f

# Logs del Auth Service
kubectl logs -n bodegapro deployment/auth-service -f

# Logs del Config Server
kubectl logs -n bodegapro deployment/config-server -f
```

---

## 7. Eliminación y Limpieza del Clúster

Para dar de baja todos los recursos creados:
```bash
kubectl delete namespace bodegapro
```
O eliminando los archivos individualmente:
```bash
kubectl delete -f k8s/k8s-all-in-one.yaml
```
