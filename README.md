# Franquicia API

API REST reactiva para gestión de franquicias, sucursales y productos. Construida con Spring WebFlux, MySQL y desplegada en AWS con arquitectura de producción.

---

## Arquitectura

```
Internet
    │
    ▼
┌─────────────────────────────────────────┐
│         Application Load Balancer        │
│         (subnet pública, puerto 80)      │
└──────────────────┬──────────────────────┘
                   │
    ┌──────────────▼──────────────┐
    │         ECS Fargate          │
    │   (franquicia-api container) │
    │       subnet privada         │
    └──────┬──────────────┬────────┘
           │              │
    ┌──────▼──────┐  ┌────▼────────────┐
    │  RDS MySQL  │  │ Secrets Manager  │
    │  (privada)  │  │  (credenciales)  │
    └─────────────┘  └─────────────────┘

Todo desplegado con CloudFormation (IaC)
Imágenes Docker almacenadas en ECR
CI/CD automatizado con GitHub Actions
```

---

## Tecnologías

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 21 (Amazon Corretto) |
| Framework | Spring Boot 4.0.5 + Spring WebFlux |
| Base de datos | MySQL 8.4 (local) / RDS MySQL 8.0 (AWS) |
| Acceso a DB | Spring Data R2DBC (reactivo) |
| Contenedorización | Docker multistage |
| Nube | AWS (ECS Fargate, RDS, ECR, ALB, Secrets Manager) |
| IaC | CloudFormation (5 stacks) |
| CI/CD | GitHub Actions |
| Simulación local | LocalStack 3.0 |

---

## Estructura del proyecto

```
franquicia-api/
├── src/
│   └── main/
│       ├── java/com/accenture/franquicia/
│       │   ├── config/
│       │   │   ├── AwsConfig.java          ← cliente Secrets Manager + lectura credenciales
│       │   │   ├── R2dbcConfig.java        ← configuración dinámica R2DBC
│       │   │   └── SecurityConfig.java     ← configuración Spring Security
│       │   ├── controller/
│       │   │   └── FranquiciaController.java ← endpoints REST
│       │   ├── dto/
│       │   │   ├── TopProductDTO.java      ← respuesta top productos
│       │   │   └── ErrorResponse.java      ← respuesta de errores
│       │   ├── exception/
│       │   │   └── GlobalExceptionHandler.java ← manejo global de errores
│       │   ├── model/
│       │   │   ├── Franquicia.java
│       │   │   ├── Sucursal.java
│       │   │   └── Producto.java
│       │   ├── repository/
│       │   │   ├── FranquiciaRepository.java
│       │   │   ├── SucursalRepository.java
│       │   │   └── ProductoRepository.java
│       │   └── services/
│       │       └── FranquiciaService.java  ← lógica de negocio completa
│       └── resources/
│           ├── application.yaml
│           └── schema.sql
├── infrastructure/
│   └── cloudformation/
│       ├── 01-vpc.yaml        ← VPC, subnets públicas/privadas, internet gateway
│       ├── 02-rds.yaml        ← RDS MySQL en subnet privada
│       ├── 03-secrets.yaml    ← Secrets Manager con credenciales DB
│       ├── 04-ecr.yaml        ← registro de imágenes Docker
│       └── 05-ecs.yaml        ← ECS Fargate + ALB + IAM Roles
├── .github/
│   └── workflows/
│       └── deploy.yml         ← pipeline CI/CD
├── localstack/
│   └── init-aws.sh            ← inicialización AWS local
├── Dockerfile                 ← build multistage
├── docker-compose.yml         ← entorno local completo
├── .env.example               ← plantilla de variables de entorno
├── .gitignore
└── README.md
```

---

## Manejo de ambientes

El proyecto detecta automáticamente en qué ambiente está corriendo:

| Variable `AWS_ENDPOINT` | Ambiente | Credenciales DB |
|---|---|---|
| `http://localstack:4566` | Local | Leídas desde LocalStack |
| Vacía o no definida | AWS | Leídas desde Secrets Manager real |

No se requiere ningún cambio de código entre ambientes — solo variables de entorno.

---

## Requisitos previos

### Para correr en local
- Docker Desktop instalado y corriendo
- Java 21 (Amazon Corretto)
- Maven 3.9+

### Para desplegar en AWS
- Cuenta AWS activa
- AWS CLI v2 configurado (`aws configure`)
- Permisos: CloudFormation, ECS, ECR, RDS, Secrets Manager, IAM, VPC, ALB

---

## Levantar en local

### 1. Clonar el repositorio

```bash
git clone https://github.com/ManuelCris29/Accenture-Franquicia.git
cd Accenture-Franquicia
```

### 2. Configurar variables de entorno

```bash
cp .env.example .env
```

Edita el `.env` con estos valores:

```env
MYSQL_ROOT_PASSWORD=root
MYSQL_USER=admin
MYSQL_PASSWORD=password
DB_NAME=franquicia_db
AWS_REGION=us-east-1
AWS_ENDPOINT=http://localstack:4566
AWS_SECRET_NAME=/accenture/dev/db-credentials
```

### 3. Levantar el entorno

```bash
docker-compose up --build
```

Este comando levanta automáticamente:
- MySQL 8.4 en el puerto 3307
- LocalStack (simula AWS Secrets Manager) en el puerto 4566
- La API en el puerto 8080

La primera vez tarda varios minutos porque descarga las imágenes y compila el código.

### 4. Verificar que está corriendo

Cuando veas este mensaje en la terminal la API está lista:

```
franquicia-api | Started FranquiciaApplication
```

Verifica con:

```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{"status": "UP"}
```

### 5. Bajar el entorno

```bash
docker-compose down        # conserva datos
docker-compose down -v     # elimina datos también
```

---

## Endpoints

### Base URL local
```
http://localhost:8080
```

### Base URL AWS (cuando está desplegado)
```
http://<ALB-DNS-NAME>
```

---

### Franquicias

| Método | URL | Descripción | Criterio |
|--------|-----|-------------|----------|
| `POST` | `/api/franquicias` | Agregar franquicia | Obligatorio |
| `GET` | `/api/franquicias` | Listar todas | - |
| `GET` | `/api/franquicias/{id}` | Obtener por id | - |
| `PATCH` | `/api/franquicias/{id}/nombre?nombre=NuevoNombre` | Actualizar nombre | Plus |
| `DELETE` | `/api/franquicias/{id}` | Eliminar franquicia | - |
| `GET` | `/api/franquicias/{id}/top-productos` | Producto con más stock por sucursal | Obligatorio |

### Sucursales

| Método | URL | Descripción | Criterio |
|--------|-----|-------------|----------|
| `POST` | `/api/franquicias/{franquiciaId}/sucursales` | Agregar sucursal | Obligatorio |
| `GET` | `/api/franquicias/{franquiciaId}/sucursales` | Listar por franquicia | - |
| `GET` | `/api/franquicias/sucursales/{id}` | Obtener por id | - |
| `PATCH` | `/api/franquicias/sucursales/{id}/nombre?nombre=NuevoNombre` | Actualizar nombre | Plus |
| `DELETE` | `/api/franquicias/sucursales/{id}` | Eliminar sucursal | - |

### Productos

| Método | URL | Descripción | Criterio |
|--------|-----|-------------|----------|
| `POST` | `/api/franquicias/sucursales/{sucursalId}/productos` | Agregar producto | Obligatorio |
| `GET` | `/api/franquicias/sucursales/{sucursalId}/productos` | Listar por sucursal | - |
| `GET` | `/api/franquicias/sucursales/productos/{id}` | Obtener por id | - |
| `DELETE` | `/api/franquicias/sucursales/{sucursalId}/productos/{productoId}` | Eliminar producto | Obligatorio |
| `PATCH` | `/api/franquicias/sucursales/productos/{id}/stock?stock=150` | Modificar stock | Obligatorio |
| `PATCH` | `/api/franquicias/sucursales/productos/{id}/nombre?nombre=NuevoNombre` | Actualizar nombre | Plus |

---

## Ejemplos de uso

### Flujo completo de prueba

```bash
# 1. Crear franquicia
curl -X POST http://localhost:8080/api/franquicias \
  -H "Content-Type: application/json" \
  -d '{"name": "McDonalds Colombia"}'

# 2. Agregar sucursal
curl -X POST http://localhost:8080/api/franquicias/1/sucursales \
  -H "Content-Type: application/json" \
  -d '{"name": "Sucursal Norte"}'

# 3. Agregar segunda sucursal
curl -X POST http://localhost:8080/api/franquicias/1/sucursales \
  -H "Content-Type: application/json" \
  -d '{"name": "Sucursal Sur"}'

# 4. Agregar producto a sucursal 1
curl -X POST http://localhost:8080/api/franquicias/sucursales/1/productos \
  -H "Content-Type: application/json" \
  -d '{"name": "Big Mac", "stock": 100}'

# 5. Agregar producto a sucursal 2
curl -X POST http://localhost:8080/api/franquicias/sucursales/2/productos \
  -H "Content-Type: application/json" \
  -d '{"name": "McNuggets", "stock": 200}'

# 6. Modificar stock
curl -X PATCH "http://localhost:8080/api/franquicias/sucursales/productos/1/stock?stock=150"

# 7. Eliminar producto
curl -X DELETE http://localhost:8080/api/franquicias/sucursales/1/productos/1

# 8. Producto con más stock por sucursal
curl http://localhost:8080/api/franquicias/1/top-productos

# 9. Actualizar nombre de franquicia
curl -X PATCH "http://localhost:8080/api/franquicias/1/nombre?nombre=Burger King"

# 10. Actualizar nombre de sucursal
curl -X PATCH "http://localhost:8080/api/franquicias/sucursales/1/nombre?nombre=Sucursal Centro"

# 11. Actualizar nombre de producto
curl -X PATCH "http://localhost:8080/api/franquicias/sucursales/productos/1/nombre?nombre=Whopper"
```

### Respuesta del endpoint top-productos

```json
[
  {
    "sucursalId": 1,
    "sucursalName": "Sucursal Norte",
    "productId": 1,
    "productName": "Big Mac",
    "stock": 150
  },
  {
    "sucursalId": 2,
    "sucursalName": "Sucursal Sur",
    "productId": 2,
    "productName": "McNuggets",
    "stock": 200
  }
]
```

---

## Manejo de errores

Todos los errores se devuelven en formato estándar:

```json
{
  "error": "NOT_FOUND",
  "message": "Franquicia no encontrada: 99",
  "timestamp": "2025-01-01T10:00:00"
}
```

| Código HTTP | Descripción |
|---|---|
| `201` | Creado exitosamente |
| `204` | Eliminado exitosamente |
| `400` | Datos inválidos (stock negativo, nombre vacío) |
| `404` | Recurso no encontrado |
| `500` | Error interno del servidor |

---

## Despliegue en AWS

### Prerequisitos

```bash
# Configurar credenciales AWS
aws configure

# Verificar identidad
aws sts get-caller-identity
```

### 1. Desplegar infraestructura con CloudFormation

Los stacks deben desplegarse en orden porque dependen entre sí:

```bash
# Stack 1 — VPC y red (~1 min)
aws cloudformation deploy \
  --template-file infrastructure/cloudformation/01-vpc.yaml \
  --stack-name franquicia-vpc-dev \
  --parameter-overrides Environment=dev

# Stack 2 — RDS MySQL (~5 min)
aws cloudformation deploy \
  --template-file infrastructure/cloudformation/02-rds.yaml \
  --stack-name franquicia-rds-dev \
  --parameter-overrides Environment=dev DBPassword=TuPasswordSeguro123!

# Stack 3 — Obtener endpoint RDS para el secreto
RDS_ENDPOINT=$(aws cloudformation describe-stacks \
  --stack-name franquicia-rds-dev \
  --query "Stacks[0].Outputs[?OutputKey=='RDSEndpoint'].OutputValue" \
  --output text)

# Stack 3 — Secrets Manager
aws cloudformation deploy \
  --template-file infrastructure/cloudformation/03-secrets.yaml \
  --stack-name franquicia-secrets-dev \
  --parameter-overrides Environment=dev DBPassword=TuPasswordSeguro123! DBHost=$RDS_ENDPOINT

# Stack 4 — ECR
aws cloudformation deploy \
  --template-file infrastructure/cloudformation/04-ecr.yaml \
  --stack-name franquicia-ecr-dev \
  --parameter-overrides Environment=dev
```

### 2. Build y push de imagen a ECR

```bash
# Obtener ID de cuenta AWS
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
REGION=us-east-1

# Login a ECR
aws ecr get-login-password --region $REGION | \
  docker login --username AWS \
  --password-stdin $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com

# Build
docker build -t franquicia-api .

# Tag
docker tag franquicia-api:latest \
  $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/franquicia-api:latest

# Push
docker push \
  $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/franquicia-api:latest
```

### 3. Desplegar ECS Fargate + ALB

```bash
# Stack 5 — ECS + ALB (~3-5 min)
aws cloudformation deploy \
  --template-file infrastructure/cloudformation/05-ecs.yaml \
  --stack-name franquicia-ecs-dev \
  --parameter-overrides \
    Environment=dev \
    ImageUri=$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/franquicia-api:latest \
  --capabilities CAPABILITY_NAMED_IAM
```

### 4. Obtener URL pública

```bash
aws elbv2 describe-load-balancers \
  --names franquicia-alb-dev \
  --query "LoadBalancers[0].DNSName" \
  --output text
```

La API estará disponible en:
```
http://<ALB-DNS-NAME>/api/franquicias
```

### 5. Eliminar toda la infraestructura

Para evitar costos, elimina los stacks cuando termines:

```bash
aws cloudformation delete-stack --stack-name franquicia-ecs-dev
aws cloudformation delete-stack --stack-name franquicia-ecr-dev
aws cloudformation delete-stack --stack-name franquicia-secrets-dev
aws cloudformation delete-stack --stack-name franquicia-rds-dev
aws cloudformation delete-stack --stack-name franquicia-vpc-dev

# Eliminar imagen de ECR
aws ecr batch-delete-image \
  --repository-name franquicia-api \
  --image-ids imageTag=latest
```

---

## CI/CD con GitHub Actions

El pipeline se activa automáticamente con cada push a `main`:

```
Push a main
    ↓ Build con Maven
    ↓ Build imagen Docker
    ↓ Push a ECR
    ↓ Deploy en ECS Fargate (zero downtime)
    ↓ Imprime URL pública
```

### Configurar secrets en GitHub

Ve a tu repositorio → Settings → Secrets and variables → Actions:

| Secret | Descripción |
|---|---|
| `AWS_ACCESS_KEY_ID` | Access key de AWS |
| `AWS_SECRET_ACCESS_KEY` | Secret key de AWS |

---

## Decisiones técnicas

- **Spring WebFlux sobre Spring MVC** — modelo no bloqueante con event loop, mayor throughput con menos recursos
- **R2DBC sobre JPA** — mantiene el modelo reactivo end-to-end sin bloquear hilos en la DB
- **ECS Fargate sobre EKS** — serverless, sin gestionar nodos, correcto para un único microservicio. EKS añadiría complejidad innecesaria para este caso
- **CloudFormation sobre Terraform** — nativo de AWS, sin dependencias externas
- **MySQL sobre DynamoDB** — los datos tienen relaciones claras (Franquicia → Sucursal → Producto), un modelo relacional es la elección correcta
- **Secrets Manager para credenciales** — las credenciales de la DB nunca están hardcodeadas en el código ni en variables de entorno del contenedor
- **Un solo FranquiciaService** — patrón Aggregate DDD, Franquicia es la raíz del agregado. Sucursal y Producto no existen fuera de ese contexto
- **AwsConfig con detección de ambiente** — misma imagen Docker funciona en local (LocalStack) y en AWS (Secrets Manager real) sin cambios de código

---

## Autor

Manuel Moreno Lizcano
- GitHub: [@ManuelCris29](https://github.com/ManuelCris29)
- LinkedIn: [manuelcmoreno](https://www.linkedin.com/in/manuelcmoreno/)