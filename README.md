# plazoleta-usuarios

Microservicio de **usuarios** de la plazoleta de comidas (Spring Boot 3 + arquitectura hexagonal).
Responsable de usuarios, roles y autenticación (emisión de JWT).

## Historias de usuario implementadas

| HU | Descripción | Endpoint | Rol |
|----|-------------|----------|-----|
| HU-1 | Crear propietario | `POST /users/owner` | ADMIN |
| HU-2 | Consultar usuario por id (usado por plazoleta para validar el propietario) | `GET /users/{id}` | Autenticado |
| — | Login (soporte para JWT) | `POST /auth/login` | Público |

## Requisitos

- Java 21+
- Maven 3.9+
- Docker (para PostgreSQL)

## Cómo levantarlo

```bash
docker compose up -d          # PostgreSQL en localhost:5432
mvn spring-boot:run           # Servicio en http://localhost:8081
```

Swagger UI: http://localhost:8081/swagger-ui.html
OpenAPI JSON: http://localhost:8081/v3/api-docs

### Variables de entorno

| Variable | Default |
|----------|---------|
| `SERVER_PORT` | `8081` |
| `DB_URL` | `jdbc:postgresql://localhost:5432/plazoleta_usuarios` |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `postgres` |
| `JWT_SECRET` | clave Base64 de desarrollo (cambiar en producción) |
| `JWT_EXPIRATION_MS` | `3600000` |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | `admin@plazoleta.com` / `Admin123*` |

Al iniciar se crea automáticamente un usuario **ADMIN** (si no existe) para poder consumir el endpoint de crear propietario.

## Uso rápido

```bash
# 1. Login como administrador
curl -X POST http://localhost:8081/auth/login -H "Content-Type: application/json" \
  -d '{"email":"admin@plazoleta.com","password":"Admin123*"}'

# 2. Crear propietario con el token obtenido
curl -X POST http://localhost:8081/users/owner \
  -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"firstName":"Juan","lastName":"Perez","documentNumber":"1234567890","phone":"+573005698325","birthDate":"1990-05-20","email":"juan.perez@correo.com","password":"Clave123*"}'
```

## Pruebas con Postman

En la carpeta `postman/`:

- `plazoleta-usuarios.postman_collection.json`: colección con 24 peticiones y 64 aserciones.
- `plazoleta-local.postman_environment.json`: entorno con `baseUrl`, `adminEmail` y `adminPassword`.

1. En Postman: **Import** → seleccionar ambos archivos.
2. Seleccionar el entorno **Plazoleta - Local** (arriba a la derecha).
3. Clic derecho sobre la colección → **Run collection** → **Run**.

El orden importa: `01 - Autenticacion` guarda el token del ADMIN, `02 - HU-1` crea el propietario y
`03 - Seguridad` inicia sesión con ese propietario para probar el 403, y `04 - HU-2` lo consulta por id.
Correos y documentos se generan
únicos en cada ejecución, así que la colección se puede repetir sin limpiar la base de datos.

Desde consola: `npx newman run postman/plazoleta-usuarios.postman_collection.json -e postman/plazoleta-local.postman_environment.json`

## Arquitectura

```
domain/            Java puro: modelos, puertos (api / spi), casos de uso, excepciones
application/       DTOs, mappers (MapStruct) y handlers
infrastructure/    Controllers REST, adaptadores JPA, seguridad JWT/BCrypt, manejo de errores, configuración
```

Los casos de uso no tienen anotaciones de Spring; se registran como beans en `BeanConfiguration`.
BCrypt y la generación de JWT se acceden desde el dominio mediante puertos (`IPasswordEncoderPort`, `ITokenGeneratorPort`).

## Pruebas

```bash
mvn test                      # Reporte de cobertura en target/site/jacoco/index.html
```
