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
| Framework | Spring Boot 3.5 + Spring WebFlux |
| Base de datos | MySQL 8.4 (local) / RDS MySQL (AWS) |
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
│       │   │   ├── AwsConfig.java
│       │   │   └── SecurityConfig.java
│       │   ├── controller/
│       │   │   └── FranquiciaController.java
│       │   ├── dto/
│       │   │   ├── TopProductDTO.java
│       │   │   └── ErrorResponse.java
│       │   ├── exception/
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── model/
│       │   │   ├── Franquicia.java
│       │   │   ├── Sucursal.java
│       │   │   └── Producto.java
│       │   ├── repository/
│       │   │   ├── FranquiciaRepository.java
│       │   │   ├── SucursalRepository.java
│       │   │   └── ProductoRepository.java
│       │   └── service/
│       │       └── FranquiciaService.java
│       └── resources/
│           ├── application.yaml
│           └── schema.sql
├── infrastructure/
│   └── cloudformation/
│       ├── 01-vpc.yaml
│       ├── 02-rds.yaml
│       ├── 03-secrets.yaml
│       ├── 04-ecr.yaml
│       └── 05-ecs.yaml
├── .github/
│   └── workflows/
│       └── deploy.yml
├── localstack/
│   └── init-aws.sh
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

---

## Requisitos previos

- Docker Desktop instalado y corriendo
- Java 21 (Amazon Corretto)
- Maven 3.9+

---

## Levantar en local

**1. Clonar el repositorio:**
```bash
git clone https://github.com/ManuelCris29/Accenture-Franquicia.git
cd Accenture-Franquicia
```

**2. Configurar variables de entorno:**
```bash
cp .env.example .env
```

Edita el `.env` con tus valores:
```env
MYSQL_ROOT_PASSWORD=root
MYSQL_USER=admin
MYSQL_PASSWORD=password
DB_NAME=franquicia_db
AWS_REGION=us-east-1
AWS_ENDPOINT=http://localstack:4566
AWS_SECRET_NAME=/accenture/dev/db-credentials
```

**3. Levantar el entorno:**
```bash
docker-compose up --build
```

**4. Verificar que está corriendo:**
```bash
curl http://localhost:8080/actuator/health
```

**5. Bajar el entorno:**
```bash
docker-compose down        # conserva datos
docker-compose down -v     # elimina datos
```

---

## Endpoints

### Franquicias

| Método | URL | Descripción | Criterio |
|--------|-----|-------------|----------|
| `POST` | `/api/franquicias` | Agregar franquicia | Obligatorio |
| `GET` | `/api/franquicias` | Listar franquicias | - |
| `GET` | `/api/franquicias/{id}` | Obtener por id | - |
| `PATCH` | `/api/franquicias/{id}/nombre` | Actualizar nombre | Plus |
| `DELETE` | `/api/franquicias/{id}` | Eliminar franquicia | - |
| `GET` | `/api/franquicias/{id}/top-productos` | Producto con más stock por sucursal | Obligatorio |

### Sucursales

| Método | URL | Descripción | Criterio |
|--------|-----|-------------|----------|
| `POST` | `/api/franquicias/{id}/sucursales` | Agregar sucursal | Obligatorio |
| `GET` | `/api/franquicias/{id}/sucursales` | Listar por franquicia | - |
| `GET` | `/api/franquicias/sucursales/{id}` | Obtener por id | - |
| `PATCH` | `/api/franquicias/sucursales/{id}/nombre` | Actualizar nombre | Plus |
| `DELETE` | `/api/franquicias/sucursales/{id}` | Eliminar sucursal | - |

### Productos

| Método | URL | Descripción | Criterio |
|--------|-----|-------------|----------|
| `POST` | `/api/franquicias/sucursales/{id}/productos` | Agregar producto | Obligatorio |
| `GET` | `/api/franquicias/sucursales/{id}/productos` | Listar por sucursal | - |
| `GET` | `/api/franquicias/sucursales/productos/{id}` | Obtener por id | - |
| `DELETE` | `/api/franquicias/sucursales/{id}/productos/{id}` | Eliminar producto | Obligatorio |
| `PATCH` | `/api/franquicias/sucursales/productos/{id}/stock` | Modificar stock | Obligatorio |
| `PATCH` | `/api/franquicias/sucursales/productos/{id}/nombre` | Actualizar nombre | Plus |

---

## Ejemplos de uso

### Flujo completo

```bash
# 1. Crear franquicia
curl -X POST http://localhost:8080/api/franquicias \
  -H "Content-Type: application/json" \
  -d '{"name": "McDonalds Colombia"}'

# 2. Agregar sucursal
curl -X POST http://localhost:8080/api/franquicias/1/sucursales \
  -H "Content-Type: application/json" \
  -d '{"name": "Sucursal Norte"}'

# 3. Agregar producto
curl -X POST http://localhost:8080/api/franquicias/sucursales/1/productos \
  -H "Content-Type: application/json" \
  -d '{"name": "Big Mac", "stock": 100}'

# 4. Modificar stock
curl -X PATCH "http://localhost:8080/api/franquicias/sucursales/productos/1/stock?stock=150"

# 5. Eliminar producto
curl -X DELETE http://localhost:8080/api/franquicias/sucursales/1/productos/1

# 6. Producto con más stock por sucursal
curl http://localhost:8080/api/franquicias/1/top-productos

# 7. Actualizar nombre de franquicia
curl -X PATCH "http://localhost:8080/api/franquicias/1/nombre?nombre=Burger King"

# 8. Actualizar nombre de sucursal
curl -X PATCH "http://localhost:8080/api/franquicias/sucursales/1/nombre?nombre=Sucursal Centro"

# 9. Actualizar nombre de producto
curl -X PATCH "http://localhost:8080/api/franquicias/sucursales/productos/1/nombre?nombre=Whopper"
```

### Respuesta del endpoint top-productos

```json
[
  {
    "branchId": 1,
    "branchName": "Sucursal Norte",
    "productId": 2,
    "productName": "Big Mac",
    "stock": 200
  },
  {
    "branchId": 2,
    "branchName": "Sucursal Sur",
    "productId": 5,
    "productName": "McNuggets",
    "stock": 350
  }
]
```

---

## Manejo de errores

```json
{
  "error": "NOT_FOUND",
  "message": "Franquicia no encontrada: 99",
  "timestamp": "2025-01-01T10:00:00"
}
```

| Código | Descripción |
|---|---|
| `201` | Creado exitosamente |
| `204` | Eliminado exitosamente |
| `400` | Datos inválidos |
| `404` | Recurso no encontrado |
| `500` | Error interno |

---

## Manejo de ambientes

| Variable `AWS_ENDPOINT` | Ambiente | Credenciales |
|---|---|---|
| `http://localstack:4566` | Local | Credenciales falsas (test/test) |
| Vacía o no definida | AWS | IAM Role del contenedor ECS |

---

## Despliegue en AWS

```bash
# 1. Infraestructura con CloudFormation
aws cloudformation deploy --template-file infrastructure/cloudformation/01-vpc.yaml --stack-name franquicia-vpc-dev
aws cloudformation deploy --template-file infrastructure/cloudformation/02-rds.yaml --stack-name franquicia-rds-dev --parameter-overrides DBPassword=TuPassword
aws cloudformation deploy --template-file infrastructure/cloudformation/03-secrets.yaml --stack-name franquicia-secrets-dev --parameter-overrides DBPassword=TuPassword
aws cloudformation deploy --template-file infrastructure/cloudformation/04-ecr.yaml --stack-name franquicia-ecr-dev
aws cloudformation deploy --template-file infrastructure/cloudformation/05-ecs.yaml --stack-name franquicia-ecs-dev --capabilities CAPABILITY_IAM

# 2. Build y push imagen a ECR
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
aws ecr get-login-password | docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com
docker build -t franquicia-api .
docker tag franquicia-api:latest $ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/franquicia-api:latest
docker push $ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/franquicia-api:latest

# 3. Obtener URL pública
aws cloudformation describe-stacks --stack-name franquicia-ecs-dev \
  --query "Stacks[0].Outputs[?OutputKey=='LoadBalancerURL'].OutputValue" --output text

# 4. Eliminar infraestructura al terminar
aws cloudformation delete-stack --stack-name franquicia-ecs-dev
aws cloudformation delete-stack --stack-name franquicia-ecr-dev
aws cloudformation delete-stack --stack-name franquicia-secrets-dev
aws cloudformation delete-stack --stack-name franquicia-rds-dev
aws cloudformation delete-stack --stack-name franquicia-vpc-dev
```

---

## CI/CD

Push a `main` → build automático → push a ECR → deploy en ECS Fargate.

Secrets requeridos en GitHub → Settings → Secrets:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

---

## Decisiones técnicas

- **Spring WebFlux sobre Spring MVC** — modelo no bloqueante con event loop, mayor throughput con menos recursos
- **R2DBC sobre JPA** — mantiene el modelo reactivo end-to-end sin bloquear hilos en la DB
- **ECS Fargate sobre EKS** — serverless, sin gestionar nodos, correcto para un único microservicio
- **CloudFormation sobre Terraform** — nativo de AWS, sin dependencias externas
- **MySQL sobre DynamoDB** — datos con relaciones claras (Franquicia → Sucursal → Producto)
- **AwsConfig con detección de ambiente** — misma imagen Docker funciona en local y en AWS sin cambios de código
- **Un solo FranquiciaService** — patrón Aggregate DDD, Franquicia es la raíz del agregado

---

## Autor

Manuel Moreno Lizcano
- GitHub: [@ManuelCris29](https://github.com/ManuelCris29)
- LinkedIn: [manuelcmoreno](https://www.linkedin.com/in/manuelcmoreno/)