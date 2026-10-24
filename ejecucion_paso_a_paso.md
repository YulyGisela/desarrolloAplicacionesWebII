# BodegaPro — Guía de Ejecución Paso a Paso

> Guía técnica exhaustiva para levantar el proyecto completo desde cero en cualquier máquina local.  
> Tiempo estimado total: **≈ 10–15 minutos**.

---

## Tabla de Contenidos

1. [Requisitos Previos](#1-requisitos-previos)
2. [Fase 1 — Infraestructura Base con Docker](#2-fase-1--infraestructura-base-con-docker)
3. [Fase 2 — Arranque de Microservicios (Backend)](#3-fase-2--arranque-de-microservicios-backend)
4. [Fase 3 — Frontend Angular](#4-fase-3--frontend-angular)
5. [Fase 4 — Despliegue en Kubernetes (Opcional)](#5-fase-4--despliegue-en-kubernetes-opcional)
6. [Accesos, URLs y Credenciales](#6-accesos-urls-y-credenciales)
7. [Prueba Rápida End-to-End](#7-prueba-rápida-end-to-end)
8. [Solución de Problemas Comunes](#8-solución-de-problemas-comunes)

---

## 1. Requisitos Previos

Asegúrese de tener instaladas las siguientes herramientas **antes de iniciar**:

| Herramienta | Versión mínima | Propósito | Enlace de descarga |
|---|---|---|---|
| **JDK (Temurin / Eclipse Adoptium)** | 17 LTS | Compilar y ejecutar los microservicios Spring Boot | https://adoptium.net/ |
| **Docker Desktop** | 4.x | Levantar toda la infraestructura (BD, broker, observabilidad) | https://www.docker.com/products/docker-desktop |
| **Node.js** | 20 LTS o superior | Ejecutar el frontend Angular | https://nodejs.org/ |
| **npm** | 10.x (incluido con Node) | Gestor de paquetes del frontend | — |
| **IntelliJ IDEA / VS Code** | Cualquier versión actual | IDE para editar y ejecutar el backend | https://www.jetbrains.com/idea/ |
| **Postman** *(opcional)* | Cualquier versión | Probar los endpoints REST manualmente | https://www.postman.com/ |
| **kubectl** *(solo Fase 4)* | 1.28+ | Aplicar manifiestos de Kubernetes | https://kubernetes.io/docs/tasks/tools/ |

### Verificación del entorno

Ejecute los siguientes comandos en su terminal para confirmar que todo está instalado:

```bash
java -version
# Debe mostrar: openjdk version "17.x.x" ...

docker -v
# Debe mostrar: Docker version 4x.x.x ...

node -v
# Debe mostrar: v20.x.x

npm -v
# Debe mostrar: 10.x.x
```

> [!IMPORTANT]
> **JAVA_HOME** debe apuntar al JDK 17. Si el proyecto usa el Gradle Wrapper (`gradlew`), no es necesario tener Gradle instalado globalmente.

---

## 2. Fase 1 — Infraestructura Base con Docker

Esta fase levanta **todos los servicios de infraestructura** necesarios: base de datos, broker de mensajería y el stack de observabilidad completo.

### 2.1 Posicionarse en la raíz del proyecto

```bash
cd C:\CIBERTEC\SEXTO-CICLO\BODEGAPRO
```

### 2.2 Levantar todos los contenedores (primera vez)

```bash
docker compose up -d --build
```

**¿Qué hace este comando?**
- `--build` recompila las imágenes Docker de los microservicios si han cambiado.
- `-d` ejecuta los contenedores en segundo plano (modo *detached*).

### 2.3 Levantar sin reconstruir imágenes (ejecuciones posteriores)

```bash
docker compose up -d
```

### 2.4 Verificar que todos los contenedores estén corriendo

```bash
docker compose ps
```

Debe ver **12 contenedores** en estado `running` o `healthy`:

| Contenedor | Puerto(s) expuesto(s) | Estado esperado |
|---|---|---|
| `bodegapro-mysql` | `5510:3306` | `healthy` |
| `bodegapro-rabbitmq` | `5672`, `15672` | `healthy` |
| `bodegapro-elasticsearch` | `9200` | `running` |
| `bodegapro-logstash` | `5044` | `running` |
| `bodegapro-kibana` | `5601` | `running` |
| `bodegapro-prometheus` | `9090` | `running` |
| `bodegapro-grafana` | `3000` | `running` |
| `bodegapro-config-server` | `8888` | `running` |
| `bodegapro-auth-service` | `8081` | `running` |
| `bodegapro-catalog-service` | `8082` | `running` |
| `bodegapro-inventory-service` | `8083` | `running` |
| `bodegapro-api-gateway` | `8080` | `running` |

### 2.5 Ver logs en tiempo real de un servicio específico

```bash
# Logs del config-server
docker logs -f bodegapro-config-server

# Logs del api-gateway
docker logs -f bodegapro-api-gateway

# Logs de todos los servicios a la vez
docker compose logs -f
```

### 2.6 Apagar toda la infraestructura

```bash
# Solo detener contenedores (preserva datos en volúmenes)
docker compose down

# Detener Y eliminar todos los volúmenes (RESET completo de datos)
docker compose down -v
```

> [!WARNING]
> El flag `-v` eliminará **todos los datos** de MySQL, RabbitMQ y Elasticsearch. Úselo únicamente cuando desee un entorno limpio desde cero.

---

## 3. Fase 2 — Arranque de Microservicios (Backend)

> [!NOTE]
> Si usó `docker compose up -d --build`, esta fase **ya está incluida** automáticamente. Los microservicios Spring Boot se ensamblan dentro de sus respectivos contenedores Docker.  
> Solo siga los pasos de esta sección si desea ejecutar los microservicios **directamente desde el IDE o consola**, fuera de Docker (modo de desarrollo local).

### 3.1 Compilar todos los módulos del backend

Desde la carpeta `backend/`:

```bash
cd backend

# Windows
.\gradlew.bat bootJar -x test

# Linux / macOS
./gradlew bootJar -x test
```

Esto compilará los 5 módulos en orden (`common-dto` → `config-server` → `auth-service` → `catalog-service` → `inventory-service` → `api-gateway`) y generará los `.jar` en las carpetas `build/libs/` de cada submódulo.

### 3.2 Orden estricto de inicialización

> [!IMPORTANT]
> El orden de arranque debe respetarse estrictamente. Cada servicio depende de que el anterior esté completamente listo.

---

#### Paso 1 — Config Server (Puerto 8888)

**Propósito:** Centralizar y distribuir la configuración a todos los demás microservicios.  
Debe estar **totalmente operativo** antes de arrancar cualquier otro servicio.

```bash
# Opción A: Ejecutar el JAR directamente
java -jar backend/config-server/build/libs/config-server-0.0.1-SNAPSHOT.jar

# Opción B: Desde Gradle
cd backend
.\gradlew.bat :config-server:bootRun
```

**Señal de éxito en los logs:**
```
Started ConfigServerApplication in X.XXX seconds
Tomcat started on port 8888
```

**Verificación:**
```bash
curl http://localhost:8888/inventory-service/default
# Debe retornar un JSON con las propiedades de configuración del inventory-service
```

---

#### Paso 2 — Microservicios de Dominio (en paralelo)

Una vez que el Config Server esté operativo, los tres microservicios de dominio pueden levantarse **simultáneamente** (abra 3 terminales independientes):

**auth-service (Puerto 8081):**
```bash
java -jar backend/auth-service/build/libs/auth-service-0.0.1-SNAPSHOT.jar
# Señal: "Started AuthServiceApplication in X.XXX seconds"
```

**catalog-service (Puerto 8082):**
```bash
java -jar backend/catalog-service/build/libs/catalog-service-0.0.1-SNAPSHOT.jar
# Señal: "Started CatalogServiceApplication in X.XXX seconds"
```

**inventory-service (Puerto 8083):**
```bash
java -jar backend/inventory-service/build/libs/inventory-service-0.0.1-SNAPSHOT.jar
# Señal: "Started InventoryServiceApplication in X.XXX seconds"
```

**Señal de éxito común para los tres:**
- Conexión exitosa a MySQL: `HikariPool-1 - Start completed`
- Conexión exitosa a RabbitMQ: `Created new connection: rabbitConnectionFactory`

---

#### Paso 3 — API Gateway (Puerto 8080)

Solo se levanta **después** de que los tres microservicios de dominio estén listos.

```bash
java -jar backend/api-gateway/build/libs/api-gateway-0.0.1-SNAPSHOT.jar
# Señal: "Netty started on port 8080"
```

**Verificación:**
```bash
curl http://localhost:8080/actuator/health
# Debe retornar: {"status":"UP",...}
```

### 3.3 Desde IntelliJ IDEA

1. Importar el proyecto como **Gradle Project** apuntando a `backend/`.
2. En el panel **Gradle**, ejecutar `bootJar` en el módulo raíz.
3. Usar las configuraciones de ejecución para arrancar cada `Application.java` en el orden indicado arriba.
4. Asegurarse de que la variable de entorno `JAVA_HOME` apunte al JDK 17.

---

## 4. Fase 3 — Frontend Angular

### 4.1 Posicionarse en la carpeta del frontend

```bash
cd frontend/inventario-web
```

### 4.2 Instalar dependencias (primera vez)

```bash
npm install
```

Esto descarga todos los paquetes de Angular 21 y sus dependencias. Puede tardar 1–3 minutos la primera vez.

### 4.3 Iniciar el servidor de desarrollo

```bash
npm start
```

Internamente ejecuta `ng serve`, que compila la aplicación y la sirve en:

```
http://localhost:4200
```

**Señal de éxito:**
```
Application bundle generation complete. [X.XXX seconds]
Watch mode enabled. Watching for file changes...
  ➜  Local:   http://localhost:4200/
```

### 4.4 Compilar para producción (opcional)

```bash
npm run build
# Los artefactos se generan en: frontend/inventario-web/dist/inventario-web/
```

> [!TIP]
> El frontend está preconfigurado para consumir la API en `http://localhost:8080`. Si necesita cambiar el backend URL, modifique el `environment.ts` en `src/environments/`.

---

## 5. Fase 4 — Despliegue en Kubernetes (Opcional)

Esta fase es **independiente** de Docker Compose. Usa los manifiestos YAML generados en `k8s/` para orquestar la aplicación en un clúster local.

### 5.1 Requisitos

- **Docker Desktop** con Kubernetes habilitado:  
  *Settings → Kubernetes → Enable Kubernetes → Apply & Restart*
- O bien **Minikube** iniciado:
  ```bash
  minikube start --driver=docker
  ```

### 5.2 Cargar imágenes locales en Minikube (solo si usa Minikube)

```bash
minikube image load bodegapro-config-server:latest
minikube image load bodegapro-auth-service:latest
minikube image load bodegapro-catalog-service:latest
minikube image load bodegapro-inventory-service:latest
minikube image load bodegapro-api-gateway:latest
```

### 5.3 Aplicar todos los manifiestos

```bash
# Opción A: Un solo comando (recomendado)
kubectl apply -f k8s/k8s-all-in-one.yaml

# Opción B: Por componentes, en orden
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-secrets.yaml
kubectl apply -f k8s/02-configmaps.yaml
kubectl apply -f k8s/03-mysql.yaml
kubectl apply -f k8s/04-rabbitmq.yaml
kubectl apply -f k8s/05-config-server.yaml
kubectl apply -f k8s/06-auth-service.yaml
kubectl apply -f k8s/07-catalog-service.yaml
kubectl apply -f k8s/08-inventory-service.yaml
kubectl apply -f k8s/09-api-gateway.yaml
```

### 5.4 Verificar el estado del clúster

```bash
# Estado de todos los pods
kubectl get pods -n bodegapro

# Estado de los servicios y puertos
kubectl get svc -n bodegapro

# Logs en tiempo real de un pod
kubectl logs -n bodegapro deployment/api-gateway -f
```

### 5.5 Acceso en Docker Desktop

El API Gateway queda disponible automáticamente en:
```
http://localhost:8080
```

### 5.6 Acceso en Minikube

```bash
# Opción A: Túnel para LoadBalancer
minikube tunnel

# Opción B: Port-forward directo
kubectl port-forward -n bodegapro svc/api-gateway 8080:8080
```

### 5.7 Eliminar todos los recursos del clúster

```bash
kubectl delete namespace bodegapro
```

---

## 6. Accesos, URLs y Credenciales

### 6.1 URLs de la Aplicación

| Servicio | URL Local | Descripción |
|---|---|---|
| **Frontend Angular** | http://localhost:4200 | Interfaz de usuario principal |
| **API Gateway** | http://localhost:8080 | Punto de entrada único de la API REST |
| **Config Server** | http://localhost:8888 | Configuración centralizada (interno) |
| **Auth Service** | http://localhost:8081 | Autenticación directa (interno) |
| **Catalog Service** | http://localhost:8082 | Catálogo de productos (interno) |
| **Inventory Service** | http://localhost:8083 | Movimientos de stock (interno) |
| **RabbitMQ Management** | http://localhost:15672 | Consola web del broker de mensajes |
| **Prometheus** | http://localhost:9090 | Métricas en tiempo real |
| **Grafana** | http://localhost:3000 | Dashboards de monitoreo |
| **Kibana** | http://localhost:5601 | Visualización de logs centralizados |
| **Elasticsearch** | http://localhost:9200 | Motor de búsqueda de logs (API REST) |
| **MySQL** | `localhost:5510` | Base de datos relacional (cliente externo) |

### 6.2 Credenciales de Acceso

| Servicio | Usuario | Contraseña | Notas |
|---|---|---|---|
| **App — Administrador** | `admin` | `admin123` | Acceso completo al sistema |
| **App — Vendedor** | `vendedor` | `vendedor123` | Acceso de solo lectura a ciertas funciones |
| **MySQL (usuario app)** | `app` | `password` | Usuario de la aplicación |
| **MySQL (root)** | `root` | `password` | Solo para administración directa |
| **RabbitMQ** | `guest` | `guest` | Consola de administración del broker |
| **Grafana** | `admin` | `admin` | Dashboard de monitoreo |

### 6.3 Endpoints Principales del API Gateway

| Método | Endpoint | Autenticación | Descripción |
|---|---|---|---|
| `POST` | `/auth/login` | No requerida | Obtener token JWT |
| `POST` | `/auth/register` | No requerida | Registrar nuevo usuario |
| `GET` | `/productos` | JWT requerido | Listar todos los productos |
| `POST` | `/productos` | JWT requerido | Crear producto |
| `PUT` | `/productos/{id}` | JWT requerido | Actualizar producto |
| `DELETE` | `/productos/{id}` | JWT requerido | Eliminar producto |
| `GET` | `/categorias` | JWT requerido | Listar categorías |
| `GET` | `/proveedores` | JWT requerido | Listar proveedores |
| `GET` | `/movimientos` | JWT requerido | Historial de movimientos |
| `POST` | `/movimientos` | JWT requerido | Registrar entrada/salida de stock |
| `GET` | `/dashboard` | JWT requerido | Resumen ejecutivo del inventario |
| `GET` | `/reportes/movimientos/excel` | JWT requerido | Exportar reporte en Excel |
| `GET` | `/reportes/movimientos/pdf` | JWT requerido | Exportar reporte en PDF |
| `GET` | `/actuator/health` | No requerida | Estado de salud del gateway |
| `GET` | `/actuator/prometheus` | No requerida | Métricas en formato Prometheus |

---

## 7. Prueba Rápida End-to-End

Una vez que toda la infraestructura esté levantada, ejecute la siguiente secuencia para validar el flujo completo:

### Paso 1 — Obtener Token JWT

```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "tipo": "Bearer",
  "expiracionMinutos": 120,
  "username": "admin",
  "nombre": "Administrador Principal",
  "role": "ADMIN"
}
```

### Paso 2 — Consultar Catálogo (con JWT)

```bash
# Reemplace <TOKEN> con el valor obtenido en el paso anterior
curl -s http://localhost:8080/productos \
  -H "Authorization: Bearer <TOKEN>"
```

### Paso 3 — Registrar Movimiento de Stock

```bash
curl -s -X POST http://localhost:8080/movimientos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"productoId":1,"tipo":"SALIDA","cantidad":5,"motivo":"Venta mostrador"}'
```

### Paso 4 — Verificar Métricas de Observabilidad

```bash
# Métricas Prometheus del Gateway
curl http://localhost:8080/actuator/prometheus | grep jvm_memory

# Targets de Prometheus (todos deben estar "up")
curl http://localhost:9090/api/v1/targets
```

---

## 8. Solución de Problemas Comunes

| Síntoma | Causa probable | Solución |
|---|---|---|
| Puerto `5510` ya en uso | Contenedor MySQL externo (de otro proyecto) corriendo | Ejecutar `docker ps` para identificarlo y `docker stop <nombre>` |
| `catalog-service` no inicia (NoSuchMethodException) | Dos constructores en `ProductoService` sin `@Autowired` explícito | Verificar que el constructor principal tenga `@Autowired` |
| `Connection refused` al RabbitMQ en los logs | El healthcheck de RabbitMQ no completó antes del microservicio | Esperar ~30 segundos y verificar con `docker compose ps` |
| Token JWT inválido (401) | El secret en el gateway no coincide con el del auth-service | Confirmar que `app.jwt.secret` sea idéntico en ambos `application.yml` |
| `npm install` falla | Versión de Node incompatible | Verificar con `node -v` que sea Node 20 LTS o superior |
| Frontend no conecta al backend | CORS no configurado o API Gateway no arrancó | Verificar que el gateway esté corriendo en `localhost:8080` |
| Kibana sin datos en el índice | Logstash tardó en procesar | Esperar 2-3 minutos y refrescar el índice en Kibana |
