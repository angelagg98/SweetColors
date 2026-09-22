# auth-service

Microservicio de autenticacion del proyecto **SweetColors**. Maneja registro, login, renovacion de sesion (refresh) y cierre de sesion (logout), usando JWT.

Ver `docs/ARQUITECTURA.md` en la raiz del repo para el contexto completo del proyecto.

## Stack

- Java 17 + Spring Boot 3.3.4
- Spring Security + JWT (libreria `jjwt`)
- Spring Data JPA + Hibernate
- MySQL 8
- Maven

## Requisitos previos

- Java 17 instalado. **Importante:** si tu sistema tiene una version de Java mas nueva (18+) como version por defecto, Lombok puede fallar al generar getters/setters/builders con errores tipo `cannot find symbol`. Solucion: instalar Java 17 con [SDKMAN](https://sdkman.io/) (`sdk install java 17.0.13-tem`) y usarla para este proyecto.
- Maven instalado.
- Acceso al servidor MySQL compartido del equipo (o uno propio local con el schema `auth_db` creado).

## Configuracion (.env)

Este servicio lee sus variables de entorno desde un archivo `.env` en esta misma carpeta (`backend/auth-service/.env`), gracias a la libreria `spring-dotenv`. **Este archivo NUNCA se sube a Git** (esta en `.gitignore`) — cada persona debe crear el suyo.

Copia `.env.example` como base:

```bash
cp .env.example .env
```

Y llena estas variables:

| Variable | Descripcion | Ejemplo |
|---|---|---|
| `DB_HOST` | Host del servidor MySQL | `100.119.46.24` |
| `DB_PORT` | Puerto de MySQL | `3306` |
| `DB_USERNAME` | Usuario de MySQL | `compa` |
| `DB_PASSWORD` | Contrasena de MySQL | (pedirla al equipo) |
| `JWT_SECRET` | Clave secreta para firmar los JWT, minimo 32 caracteres. **Debe ser la MISMA en todos los microservicios** que necesiten validar tokens. | `una-clave-larga-y-secreta-de-al-menos-32-caracteres` |
| `COOKIE_SECURE` | Si las cookies exigen HTTPS. `false` en desarrollo local (http), `true` en produccion. | `false` |

**Importante sobre las comas:** cada variable va en su propia linea, sin comas al final. Ejemplo correcto:
```
DB_HOST=100.119.46.24
DB_PORT=3306
```

## Como levantarlo

```bash
cd backend/auth-service
mvn spring-boot:run
```

El servicio queda escuchando en `http://localhost:8081`.

Si acabas de cambiar dependencias o algo no compila y sospechas de cache vieja, usa `mvn clean spring-boot:run` para forzar una recompilacion completa.

## Base de datos

Usa `ddl-auto: update` (ver `application.yml`), asi que Hibernate crea/actualiza las tablas automaticamente al arrancar. No hace falta correr scripts SQL manuales para desarrollo, aunque si se debe generar el script final para el entregable (ver seccion de pendientes en `docs/ARQUITECTURA.md`).

Tablas que genera:
- **`users`**: id, email, password (encriptada con BCrypt), full_name, role (`ADMIN`/`CLIENTE`), enabled, created_at.
- **`refresh_tokens`**: id, token_hash (nunca el token real, solo su hash SHA-256), user_id, expires_at, revoked, created_at.

## Endpoints disponibles

Todos bajo el prefijo `/api/auth`, y son **publicos** (no requieren estar logueado):

| Metodo | Ruta | Body | Descripcion |
|---|---|---|---|
| POST | `/api/auth/register` | `{ fullName, email, password }` | Crea un usuario nuevo (rol CLIENTE por defecto). Devuelve 201 con los datos del usuario (sin password). |
| POST | `/api/auth/login` | `{ email, password }` | Autentica al usuario. Devuelve 200 con los datos del usuario, y coloca `access_token` y `refresh_token` como cookies httpOnly. |
| POST | `/api/auth/refresh` | (ninguno, usa la cookie `refresh_token`) | Genera un access token y refresh token nuevos (rota el refresh token viejo, lo marca revocado). Devuelve 204. |
| POST | `/api/auth/logout` | (ninguno, usa la cookie `refresh_token`) | Revoca el refresh token actual y limpia las cookies. Devuelve 204. |

Cualquier otra ruta que se agregue a este u otro microservicio **requiere** un `access_token` valido (cookie httpOnly), gracias al filtro JWT ya configurado.

### Endpoint de prueba (temporal)

`GET /api/test/protected` — solo existe para confirmar que el filtro JWT funciona. Devuelve el id del usuario autenticado. Se puede borrar mas adelante o convertir en un endpoint real (como "mi perfil").

## Como probar los endpoints

Se probo todo con **Thunder Client** (extension de VS Code). Flujo recomendado:

1. `POST /api/auth/register` con un usuario de prueba.
2. `POST /api/auth/login` con ese mismo usuario — confirma que llegan las cookies en la pestana "Cookies" de la respuesta.
3. `GET /api/test/protected` (en la misma coleccion/pestana, para que comparta las cookies) — deberia dar 200.
4. `POST /api/auth/refresh` — deberia dar 204 y generar cookies nuevas.
5. `POST /api/auth/logout` — deberia dar 204. Despues de esto, `/api/auth/refresh` deberia fallar porque el token ya esta revocado.

## Seguridad: decisiones tomadas

- **Contrasenas**: encriptadas con BCrypt (configuracion por defecto), nunca se guardan ni se devuelven en texto plano.
- **Access token**: JWT de 15 minutos, viaja en cookie `httpOnly` + `sameSite=Strict` (protegida contra XSS y CSRF basico).
- **Refresh token**: no es JWT, es un UUID aleatorio. Se guarda **hasheado** (SHA-256) en la base de datos — si alguien accede a la BD, no puede usar los tokens directamente. Dura 7 dias.
- **Rotacion de refresh token**: cada vez que se usa `/refresh`, el token viejo se revoca y se genera uno nuevo. Si alguien roba un refresh token y lo usa, el dueno legitimo lo notaria porque su propio token (ya viejo) dejaria de funcionar.
- **Stateless**: el servidor no guarda sesiones; toda la validacion ocurre leyendo y verificando el JWT en cada peticion.

## Pendientes / ideas para mejorar

- Quitar el warning de API deprecada en `JwtService.java` (viene de como se llama `signWith()` en la version actual de `jjwt`).
- Agregar rate limiting al login para prevenir fuerza bruta.
- Documentar con Swagger/OpenAPI los endpoints.
- Agregar tests automatizados (JUnit) para `AuthService`.
