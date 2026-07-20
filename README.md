# E-commerce REST API - EPN (Grupo 1)

Backend modular e institucional desarrollado en Java 21 utilizando el ecosistema Quarkus
para gestionar un flujo transaccional de ventas seguro: usuarios, inventario de productos
y notas de venta (receipts), con autenticación mediante JWT (llaves RSA asimétricas).

---

## Stack Tecnológico

- **Lenguaje:** Java 21 (OpenJDK)
- **Framework:** Quarkus 3.15.1 (RESTEasy Reactive + Jackson)
- **Persistencia:** Hibernate ORM con Panache (patrón Repository) + PostgreSQL 15
- **Validación:** Hibernate Validator (Bean Validation)
- **Seguridad:** MicroProfile JWT (SmallRye JWT) con llaves RSA + BCrypt (jbcrypt) para contraseñas
- **Documentación:** SmallRye OpenAPI + Swagger UI
- **Pruebas:** JUnit 5 + REST-Assured (`@QuarkusTest`)
- **Contenedores:** Docker / Docker Compose

---

## Estructura del proyecto

```
src/main/java/ec/epn/ecommerce/
├── config/        # Configuración de OpenAPI/Swagger
├── controller/     # Endpoints REST (JAX-RS)
├── dto/            # Objetos de entrada/salida (Request/Response)
├── entity/         # Entidades JPA (tablas de la base de datos)
├── exception/       # Excepciones de negocio + ExceptionMapper (manejo centralizado)
├── mapper/         # Conversión DTO <-> Entity
├── repository/     # Acceso a datos (PanacheRepository)
├── security/       # Generación de JWT y cifrado de contraseñas
└── service/        # Lógica de negocio y reglas transaccionales

src/main/docker/     # Dockerfiles (JVM, legacy-jar, native, native-micro)
src/main/resources/  # application.properties + llaves RSA (privateKey.pem / publicKey.pem)
src/test/java/       # Pruebas de endpoints (JUnit 5 + REST-Assured)
```

---

## Requisitos previos

- Docker y Docker Compose instalados (para el despliegue rápido).
- Maven 3.9+ y JDK 21 (solo si se desea ejecutar en modo desarrollo, sin contenedores).

---

## Ejecución con Docker Compose (recomendado)

Levanta la base de datos PostgreSQL y la API en un solo comando:

```bash
# 1. Compilar el proyecto (genera target/quarkus-app/)
./mvnw package

# 2. Construir la imagen y levantar ambos contenedores
docker compose up -d --build
```

Esto levanta:
- `ecommerce-postgres`: PostgreSQL 15 en el puerto `5432`, con volumen persistente `postgres_data`.
- `ecommerce-api`: la API Quarkus en el puerto `8080`, conectada internamente a `postgres-db`.

Para detener y liberar los contenedores:
```bash
docker compose down
```

Para ver los logs de la API en vivo:
```bash
docker compose logs -f quarkus-api
```

---

## Ejecución en modo desarrollo (sin Docker)

Requiere tener PostgreSQL corriendo localmente en `localhost:5432` con la base
`ecommerce_db` (usuario `ecommerce_user` / contraseña `ecommerce_password`, ver
`application.properties`).

```bash
./mvnw quarkus:dev
```

El modo dev habilita *live reload* (los cambios en el código se recargan sin
reiniciar) y la consola de desarrollador en `http://localhost:8080/q/dev`.

---

## Documentación de la API (Swagger)

Con la aplicación corriendo (Docker o modo dev), la documentación interactiva está en:

- **Swagger UI:** http://localhost:8080/q/swagger-ui
- **Especificación OpenAPI (JSON/YAML):** http://localhost:8080/openapi

---

## Endpoints principales

| Método | Ruta                         | Acceso        | Descripción                                  |
|--------|------------------------------|---------------|-----------------------------------------------|
| POST   | `/api/users/register`        | Público       | Registra un nuevo usuario                     |
| POST   | `/api/auth/login`            | Público       | Autentica y devuelve un JWT                   |
| GET    | `/api/users`                 | Autenticado   | Lista todos los usuarios                      |
| GET    | `/api/users/{id}`            | Autenticado   | Obtiene un usuario por ID                     |
| PUT    | `/api/users/{id}`            | Autenticado   | Actualiza nombre/correo de un usuario         |
| DELETE | `/api/users/{id}`            | Autenticado   | Elimina un usuario                            |
| GET    | `/api/products`              | Público       | Lista productos (soporta `search`, `page`, `size`) |
| GET    | `/api/products/{id}`         | Público       | Obtiene un producto por ID                    |
| POST   | `/api/products`              | Autenticado   | Crea un producto                              |
| PUT    | `/api/products/{id}`         | Autenticado   | Actualiza un producto                         |
| DELETE | `/api/products/{id}`         | Autenticado   | Elimina un producto                           |
| POST   | `/api/receipts`               | Autenticado   | Crea una nota de venta (valida stock, calcula totales) |
| GET    | `/api/receipts`               | Autenticado   | Lista todas las notas de venta                |
| GET    | `/api/receipts/{id}`          | Autenticado   | Obtiene una nota de venta por ID              |
| GET    | `/api/receipts/user/{userId}` | Autenticado   | Lista las notas de venta de un usuario        |
| DELETE | `/api/receipts/{id}`          | Autenticado   | Elimina una nota de venta                     |

Los endpoints "Autenticado" requieren el encabezado:
```
Authorization: Bearer <token>
```
obtenido desde `POST /api/auth/login`.

---

## Ejemplo de flujo completo (curl)

```bash
# 1. Registrar un usuario
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Juan Perez","email":"juan@epn.edu.ec","password":"password123"}'

# 2. Iniciar sesión y obtener el token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"juan@epn.edu.ec","password":"password123"}'
# -> {"token": "eyJhbGciOiJSUzI1NiIs..."}

# 3. Crear un producto (requiere token)
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"name":"Mouse Inalambrico","price":25.50,"stock":10}'

# 4. Crear una nota de venta
curl -X POST http://localhost:8080/api/receipts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"userId":1,"items":[{"productId":1,"quantity":2}]}'
```

---

## Pruebas

Las pruebas de endpoints usan `@QuarkusTest` + REST-Assured y ejercitan la API real
(registro, login, CRUD de productos, creación de notas de venta, validaciones y
manejo de errores):

```bash
./mvnw test
```

Cobertura actual:
- **Autenticación:** login exitoso, contraseña incorrecta, correo no registrado, datos inválidos.
- **Productos:** acceso público a lectura, rechazo sin token en escritura, creación y consulta,
  validaciones de datos, búsqueda y paginación.
- **Notas de venta:** creación exitosa con cálculo correcto del total y descuento de stock,
  validación de stock insuficiente (con reversión transaccional), usuario inexistente,
  eliminación de un recurso inexistente.
- **Documentación:** disponibilidad de `/openapi` y `/q/swagger-ui`.

---

## Seguridad

- Contraseñas cifradas con **BCrypt** (costo 12), nunca se almacenan ni se devuelven en texto plano.
- Autenticación con **JWT firmado con RSA** (llave privada para firmar, llave pública para verificar),
  con expiración de 2 horas.
- Las llaves (`privateKey.pem` / `publicKey.pem`) se inyectan al contenedor como **volúmenes de solo
  lectura**, no se incluyen dentro de la imagen Docker.
- Los endpoints protegidos usan `@RolesAllowed("USER")`, y los de lectura pública usan `@PermitAll`
  explícitamente.

> **Nota:** actualmente todos los usuarios registrados comparten el rol `USER`. El control de
> roles diferenciados (por ejemplo `ADMIN` para gestión de inventario) está identificado como
> mejora futura del proyecto.

---

---

## Integrantes del Grupo 1

José Castro 
Estefano Santacruz
Anna Nevenchenaia
