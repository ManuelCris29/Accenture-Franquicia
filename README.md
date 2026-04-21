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
│       │   │   ├── AwsConfig.java
│       │   │   ├── R2dbcConfig.java
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
│       │   └── services/
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

## Manejo de ambientes

| Variable `AWS_ENDPOINT` | Ambiente | Credenciales DB |
|---|---|---|
| `http://localstack:4566` | Local | Leídas desde LocalStack |
| Vacía o no definida | AWS | Leídas desde Secrets Manager real |

No se requiere ningún cambio de código entre ambientes.

---

## Requisitos previos

### Para correr en local
- Docker Desktop instalado y corriendo
- Java 21 (Amazon Corretto)
- Maven 3.9+

### Para desplegar en AWS
- Cuenta AWS activa
- AWS CLI v2 instalado
- Usuario IAM con política `AdministratorAccess`

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

```
GET http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{"status": "UP"}
```

### 5. Bajar el entorno

```bash
docker-compose down
```

---

## Despliegue en AWS

### Prerequisitos AWS

**Paso 1 — Crear usuario IAM**

1. Ve a https://console.aws.amazon.com
2. Busca IAM → Users → Create user
3. Nombre: `franquicia-deploy`
4. Attach policies → AdministratorAccess
5. Create user → Security credentials → Create access key → CLI
6. Copia el Access Key ID y Secret Access Key

**Paso 2 — Configurar AWS CLI**

```bash
aws configure
```

Ingresa:
```
AWS Access Key ID: TU-ACCESS-KEY
AWS Secret Access Key: TU-SECRET-KEY
Default region name: us-east-1
Default output format: json
```

**Paso 3 — Verificar**

```bash
aws sts get-caller-identity
```

Debe mostrar tu Account ID de 12 dígitos.

---

### Despliegue paso a paso

**IMPORTANTE:** Ejecuta cada comando y espera que termine antes de continuar con el siguiente. Los comandos están en una sola línea para compatibilidad con CMD y PowerShell de Windows.

---

**Stack 1 — VPC (~1 min)**

```bash
aws cloudformation deploy --template-file infrastructure/cloudformation/01-vpc.yaml --stack-name franquicia-vpc-dev --parameter-overrides Environment=dev
```

---

**Stack 2 — RDS MySQL (~5 min)**

Reemplaza `TuPasswordSeguro123!` por una contraseña segura y recuérdala para los siguientes pasos.

```bash
aws cloudformation deploy --template-file infrastructure/cloudformation/02-rds.yaml --stack-name franquicia-rds-dev --parameter-overrides Environment=dev DBPassword=TuPasswordSeguro123!
```

---

**Obtener endpoint de RDS**

```bash
aws cloudformation describe-stacks --stack-name franquicia-rds-dev --query "Stacks[0].Outputs[?OutputKey=='RDSEndpoint'].OutputValue" --output text
```

Copia el valor que aparece (ejemplo: `franquicia-mysql-dev.xxxxxx.us-east-1.rds.amazonaws.com`). Lo necesitas en el siguiente paso.

---

**Stack 3 — Secrets Manager**

Reemplaza `TuPasswordSeguro123!` por la misma contraseña del Stack 2 y `TU-ENDPOINT-RDS` por el valor que copiaste.

```bash
aws cloudformation deploy --template-file infrastructure/cloudformation/03-secrets.yaml --stack-name franquicia-secrets-dev --parameter-overrides Environment=dev DBPassword=TuPasswordSeguro123! DBHost=TU-ENDPOINT-RDS
```

---

**Stack 4 — ECR**

```bash
aws cloudformation deploy --template-file infrastructure/cloudformation/04-ecr.yaml --stack-name franquicia-ecr-dev --parameter-overrides Environment=dev
```

---

**Obtener Account ID**

```bash
aws sts get-caller-identity --query Account --output text
```

Copia el número de 12 dígitos. Lo necesitas en los siguientes pasos.

---

**Login a ECR**

Reemplaza `TU-ACCOUNT-ID` por el número de 12 dígitos.

```bash
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin TU-ACCOUNT-ID.dkr.ecr.us-east-1.amazonaws.com
```

Debe responder: `Login Succeeded`

---

**Build de la imagen Docker**

```bash
docker build -t franquicia-api .
```

---

**Tag de la imagen**

Reemplaza `TU-ACCOUNT-ID` por tu Account ID.

```bash
docker tag franquicia-api:latest TU-ACCOUNT-ID.dkr.ecr.us-east-1.amazonaws.com/franquicia-api:latest
```

---

**Push de la imagen a ECR**

Reemplaza `TU-ACCOUNT-ID` por tu Account ID.

```bash
docker push TU-ACCOUNT-ID.dkr.ecr.us-east-1.amazonaws.com/franquicia-api:latest
```

---

**Stack 5 — ECS Fargate + ALB (~5 min)**

Reemplaza `TU-ACCOUNT-ID` por tu Account ID.

```bash
aws cloudformation deploy --template-file infrastructure/cloudformation/05-ecs.yaml --stack-name franquicia-ecs-dev --parameter-overrides Environment=dev ImageUri=TU-ACCOUNT-ID.dkr.ecr.us-east-1.amazonaws.com/franquicia-api:latest --capabilities CAPABILITY_NAMED_IAM
```

---

**Obtener URL pública**

```bash
aws elbv2 describe-load-balancers --names franquicia-alb-dev --query "LoadBalancers[0].DNSName" --output text
```

La API estará disponible en:
```
http://TU-ALB-DNS/api/franquicias
```

**Verificar que está funcionando:**
```
GET http://TU-ALB-DNS/actuator/health
```

Respuesta esperada: `{"status": "UP"}`

---

### Eliminar infraestructura

Ejecuta en orden — espera que cada uno termine antes del siguiente:

```bash
aws cloudformation delete-stack --stack-name franquicia-ecs-dev
```

Espera ~3 min, luego:

```bash
aws cloudformation delete-stack --stack-name franquicia-ecr-dev
aws cloudformation delete-stack --stack-name franquicia-secrets-dev
aws cloudformation delete-stack --stack-name franquicia-rds-dev
aws cloudformation delete-stack --stack-name franquicia-vpc-dev
```

Eliminar imagen y repositorio ECR:

```bash
aws ecr delete-repository --repository-name franquicia-api --force
```

Verificar que todo fue eliminado:

```bash
aws cloudformation list-stacks --stack-status-filter CREATE_COMPLETE UPDATE_COMPLETE --query "StackSummaries[?contains(StackName,'franquicia')].{Name:StackName,Status:StackStatus}" --output table
```

Si no aparece nada — todo eliminado y sin costos.

---

## Endpoints

### Base URL local
```
http://localhost:8080
```

### Base URL AWS
```
http://TU-ALB-DNS
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

```
--- CREAR DATOS ---

1.  POST /api/franquicias
    Body: {"name": "McDonalds Colombia"}
    → id: 1

2.  POST /api/franquicias/1/sucursales
    Body: {"name": "Sucursal Norte"}
    → id: 1

3.  POST /api/franquicias/1/sucursales
    Body: {"name": "Sucursal Sur"}
    → id: 2

4.  POST /api/franquicias/sucursales/1/productos
    Body: {"name": "Big Mac", "stock": 100}
    → id: 1

5.  POST /api/franquicias/sucursales/1/productos
    Body: {"name": "McFlurry", "stock": 50}
    → id: 2

6.  POST /api/franquicias/sucursales/2/productos
    Body: {"name": "McNuggets", "stock": 200}
    → id: 3

--- CONSULTAR ---

7.  GET /api/franquicias
    → lista todas las franquicias

8.  GET /api/franquicias/1
    → obtiene franquicia con id 1

9.  GET /api/franquicias/1/sucursales
    → lista Sucursal Norte y Sucursal Sur

10. GET /api/franquicias/sucursales/1
    → obtiene Sucursal Norte

11. GET /api/franquicias/sucursales/1/productos
    → lista Big Mac y McFlurry

12. GET /api/franquicias/sucursales/productos/1
    → obtiene Big Mac

--- ACTUALIZAR ---

13. PATCH /api/franquicias/sucursales/productos/1/stock?stock=150
    → Big Mac stock: 150

14. PATCH /api/franquicias/1/nombre?nombre=Burger King Colombia
    → nombre de franquicia actualizado

15. PATCH /api/franquicias/sucursales/1/nombre?nombre=Sucursal Centro
    → nombre de sucursal actualizado

16. PATCH /api/franquicias/sucursales/productos/1/nombre?nombre=Whopper
    → nombre de producto actualizado

--- CONSULTA ESPECIAL ---

17. GET /api/franquicias/1/top-productos
    → Whopper (Sucursal Centro, stock 150) y McNuggets (Sucursal Sur, stock 200)

--- ELIMINAR ---

18. DELETE /api/franquicias/sucursales/1/productos/2
    → 204 No Content (elimina McFlurry)

19. DELETE /api/franquicias/sucursales/2
    → 204 No Content (elimina Sucursal Sur y sus productos)

20. DELETE /api/franquicias/1
    → 204 No Content (elimina franquicia con todas sus sucursales y productos)

--- CASOS BORDE ---

21. GET /api/franquicias/999
    → 404 {"error": "NOT_FOUND", "message": "Franquicia no encontrada con ID: 999"}

22. PATCH /api/franquicias/sucursales/productos/1/stock?stock=-10
    → 400 {"error": "BAD_REQUEST", "message": "El stock no puede ser negativo"}

23. PATCH /api/franquicias/1/nombre?nombre=
    → 400 {"error": "BAD_REQUEST", "message": "El nombre no puede estar vacío"}
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
    "productId": 3,
    "productName": "McNuggets",
    "stock": 200
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

| Código HTTP | Descripción |
|---|---|
| `201` | Creado exitosamente |
| `204` | Eliminado exitosamente |
| `400` | Datos inválidos (stock negativo, nombre vacío) |
| `404` | Recurso no encontrado |
| `500` | Error interno del servidor |

---

## CI/CD con GitHub Actions

El pipeline se activa automáticamente con cada push a `main`:

```
Push a main → Build Maven → Build Docker → Push ECR → Deploy ECS
```

Secrets requeridos en GitHub → Settings → Secrets:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

---

## Decisiones técnicas

- **Spring WebFlux sobre Spring MVC** — modelo no bloqueante, mayor throughput con menos recursos
- **R2DBC sobre JPA** — mantiene el modelo reactivo end-to-end sin bloquear hilos
- **ECS Fargate sobre EKS** — serverless, correcto para un único microservicio
- **CloudFormation sobre Terraform** — nativo de AWS, sin dependencias externas
- **MySQL sobre DynamoDB** — datos con relaciones claras (Franquicia → Sucursal → Producto)
- **Secrets Manager para credenciales** — credenciales nunca hardcodeadas en el código
- **Un solo FranquiciaService** — patrón Aggregate DDD, Franquicia es la raíz del agregado
- **AwsConfig con detección de ambiente** — misma imagen Docker funciona en local y AWS sin cambios

---

## Autor

Manuel Moreno Lizcano
- GitHub: [@ManuelCris29](https://github.com/ManuelCris29)
- LinkedIn: [manuelcmoreno](https://www.linkedin.com/in/manuelcmoreno/)