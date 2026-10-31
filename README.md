# plazoleta-usuarios

Microservicio de **usuarios** de la plazoleta de comidas (Spring Boot 3 + arquitectura hexagonal).
Responsable de usuarios, roles y autenticación (emisión de JWT).

## Historias de usuario implementadas

| HU | Descripción | Endpoint | Rol |
|----|-------------|----------|-----|
| HU-1 | Crear propietario | `POST /users/owner` | ADMIN |
| HU-2 | Consultar usuario por id (usado por plazoleta para validar el propietario) | `GET /users/{id}` | Autenticado |
| HU-5 | Autenticación de usuarios (login y permisos por rol) | `POST /auth/login` | Público |
| HU-6 | Crear empleado | `POST /users/employee` | OWNER (dueño del restaurante) |

### Reglas de la HU-6

- Solo el **propietario del restaurante** puede crear empleados para ese restaurante: el token debe tener rol
  `OWNER` y su id debe ser el `ownerId` del restaurante, que se consulta en `GET /restaurants/{id}` de
  plazoleta (403 si no; 404 si el restaurante no existe; 503 si plazoleta no responde).
- Campos obligatorios: nombre, apellido, documento, celular, correo, **idRol**, contraseña e id del restaurante.
- `idRol` debe ser **3** (empleado); cualquier otro valor responde 400. El usuario queda con rol `EMPLOYEE`.
- Mismas validaciones que el propietario: correo válido, celular de máximo 13 caracteres con `+` opcional,
  documento numérico, correo y documento únicos (409) y contraseña con BCrypt. No se pide fecha de nacimiento.
- El empleado queda asociado a su restaurante (`restaurantId`). Al iniciar sesión, el login y el JWT incluyen
  `restaurantId`, para que plazoleta sepa de qué restaurante es el empleado.

Ids de rol: 1 ADMIN, 2 OWNER, 3 EMPLOYEE, 4 CLIENT.

> **Bases de datos creadas antes de la HU-6:** la columna `birth_date` se creó como obligatoria y Hibernate
> no quita esa restricción. Ejecutar una vez:
> `docker exec postgres-usuarios psql -U postgres -d plazoleta_usuarios -c "ALTER TABLE users ALTER COLUMN birth_date DROP NOT NULL;"`

### Reglas de la HU-5

- El inicio de sesión usa **correo y contraseña** (correo con formato válido; si no, 400).
- Si el correo no existe o la contraseña no coincide: 401 con el mismo mensaje (no revela qué correos existen).
- Los intentos son **ilimitados**: los intentos fallidos no bloquean la cuenta.
- La respuesta incluye `token`, `userId` y `role`, para que la app muestre las opciones del rol.
- Cada endpoint valida el rol: `POST /users/owner` solo ADMIN; `GET /users/{id}` cualquier usuario autenticado.
- No hay recuperación de contraseña en esta versión.

Matriz de permisos de los dos microservicios:

| Endpoint | Microservicio | Rol permitido |
|----------|---------------|---------------|
| `POST /auth/login` | usuarios | Público |
| `POST /users/owner` | usuarios | ADMIN |
| `POST /users/employee` | usuarios | OWNER, dueño del restaurante |
| `GET /users/{id}` | usuarios | Cualquier usuario autenticado |
| `POST /restaurants` | plazoleta | ADMIN |
| `GET /restaurants/{id}` | plazoleta | Cualquier usuario autenticado |
| `POST /dishes` | plazoleta | OWNER, dueño del restaurante |
| `PATCH /dishes/{id}` | plazoleta | OWNER, dueño del restaurante |

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
| `PLAZOLETA_URL` | `http://localhost:8082` (para validar el dueño del restaurante al crear empleados) |

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

- `plazoleta-usuarios.postman_collection.json`: colección con 43 peticiones (más 3 intentos fallidos automáticos) y 108 aserciones.
- `plazoleta-local.postman_environment.json`: entorno con `baseUrl`, `plazoletaUrl`, `adminEmail` y `adminPassword`.

1. En Postman: **Import** → seleccionar ambos archivos.
2. Seleccionar el entorno **Plazoleta - Local** (arriba a la derecha).
3. Clic derecho sobre la colección → **Run collection** → **Run**.

El orden importa: `01 - Autenticacion` guarda el token del ADMIN, `02 - HU-1` crea el propietario y
`03 - Seguridad` inicia sesión con ese propietario para probar el 403, y `04 - HU-2` lo consulta por id.
Las pruebas de login de la HU-5 (formato de correo, intentos ilimitados, rol en la respuesta) están en `01` y `03`.
`05 - HU-6` crea un restaurante para ese propietario en plazoleta (necesita plazoleta levantado) y luego sus empleados.
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
