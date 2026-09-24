# SweetColors

Aplicación web de e-commerce para **desayunos sorpresa y decoraciones**, con catálogo de productos y **lista de deseos**. Es una adaptación de la prueba técnica de Carvajal (modelo B2C) a este dominio.

**Qué puede hacer el cliente:**
- Ver el catálogo con las cantidades disponibles de cada producto.
- Registrarse e iniciar sesión (JWT).
- Listar, agregar, actualizar (cantidad) y eliminar productos de su lista de deseos.
- Recibir un aviso cuando un producto de su lista se queda sin stock.
- Guardar un histórico de todo lo que pasa en su lista de deseos.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Java 17, Spring Boot 3.3.4, Maven |
| Arquitectura | Microservicios + API Gateway (Spring Cloud Gateway) |
| Base de datos | MySQL 8 (una base por servicio) |
| ORM | Spring Data JPA + Hibernate |
| Autenticación | JWT (cookie httpOnly + refresh token hasheado en BD) |
| Frontend | React (Vite) + Tailwind CSS |
| Contenedores | Docker y Docker Compose |
| Control de versiones | Git y GitHub, flujo GitFlow |

## Servicios y puertos

| Servicio | Puerto | Base de datos | Función |
|---|---|---|---|
| frontend | 5500 | - | Aplicación web |
| api-gateway | 8080 | - | Punto de entrada único; enruta hacia los servicios |
| auth-service | 8081 | `auth_db` | Registro, login, refresh y logout con JWT |
| catalog-service | 8082 | `catalog_db` | Productos y consulta de stock |
| wishlist-service | 8083 | `wishlist_db` | Lista de deseos y eventos |
| history-service | 8084 | `history_db` | Histórico de eventos de la lista de deseos |
| mysql | 3307 (en tu equipo) | - | MySQL en contenedor; los servicios lo usan por el 3306 interno de Docker |

Más detalle en [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md) y el modelo de datos en [`docs/modelo-entidad-relacion.md`](docs/modelo-entidad-relacion.md).

## Manual de despliegue

### Requisitos

- Git
- Docker y Docker Compose (v2)

No hace falta instalar Java, Maven, Node ni MySQL: todo corre en contenedores.

### Pasos

```bash
# 1. Clonar el repositorio
git clone <URL-DEL-REPOSITORIO>
cd SweetColors

# 2. Crear el archivo de variables de entorno
cp .env.example .env

# 3. Construir y levantar todo
docker compose up --build -d

# 4. Ver que todos los servicios estén arriba
docker compose ps
```

La primera vez tarda unos minutos porque construye las imágenes de los 4 microservicios y del gateway.

Cuando termine, abre **http://localhost:5500**.

### Base de datos

El contenedor `mysql` crea las 4 bases de datos, las tablas y los productos de ejemplo automáticamente al iniciar por primera vez, con los scripts de la carpeta [`database/init`](database/init):

| Script | Contenido |
|---|---|
| `01-schema.sql` | Crea `auth_db`, `catalog_db`, `wishlist_db`, `history_db` y sus tablas |
| `02-datos-ejemplo.sql` | Carga 10 productos de ejemplo (desayunos y decoraciones); algunos con stock 0 |

> Los scripts solo se ejecutan cuando el volumen de MySQL está vacío. Para empezar de cero: `docker compose down -v` y luego `docker compose up --build -d`.

### Credenciales

| Qué | Valor |
|---|---|
| Servidor MySQL (desde tu equipo, p. ej. Workbench) | `localhost`, puerto `3307` |
| Usuario de base de datos | `root` |
| Contraseña de base de datos | `sweetcolors123` |
| Usuarios de la aplicación | No hay usuarios precargados: créalos desde **Registro** en http://localhost:5500 |

Son credenciales de desarrollo, definidas en `.env.example`. En un entorno real deben cambiarse (incluido `JWT_SECRET`).

## Cómo probar la aplicación

1. Entra a http://localhost:5500. Sin sesión verás la pantalla de bienvenida.
2. Crea una cuenta en **Registro** e inicia sesión en **Entrar**.
3. En el **Catálogo** verás los productos con su stock. Los que tienen stock 0 no se pueden añadir.
4. Pulsa **Añadir a Wishlist** en uno o varios productos.
5. Abre **Mi Wishlist**: puedes subir o bajar la cantidad con **+ / −** y eliminar productos.
6. **Probar el aviso de sin stock:** agotar un producto que ya esté en tu lista.

   ```bash
   docker exec -it sweetcolors-mysql mysql -uroot -p catalog_db \
     -e "UPDATE products SET stock = 0 WHERE id = 2;"
   ```

   (te pedirá la contraseña de la base de datos). Al recargar **Mi Wishlist** aparece el aviso amarillo y la etiqueta "Sin stock", y se registra el evento `OUT_OF_STOCK` en el histórico.
7. Para ver los eventos guardados en la base de datos:

   ```bash
   docker exec -it sweetcolors-mysql mysql -uroot -p \
     -e "SELECT * FROM wishlist_db.wishlist_events ORDER BY created_at DESC;"
   ```

## Endpoints principales

Todos se consumen a través del gateway (`http://localhost:8080`).

| Servicio | Método y ruta | Descripción |
|---|---|---|
| Auth | `POST /api/auth/register` | Registrar usuario |
| Auth | `POST /api/auth/login` | Iniciar sesión (entrega el JWT en cookie) |
| Auth | `POST /api/auth/refresh`, `POST /api/auth/logout` | Renovar y cerrar sesión |
| Catálogo | `GET /api/catalog/products?category=` | Listar productos (público) |
| Catálogo | `GET /api/catalog/products/{id}`, `GET /api/catalog/products/{id}/stock` | Detalle y stock |
| Catálogo | `POST /api/catalog/products` | Crear producto (requiere sesión) |
| Wishlist | `GET /api/wishlist/items?userId=` | Listar deseos (incluye `inStock`) |
| Wishlist | `POST /api/wishlist/items` | Agregar deseo |
| Wishlist | `PUT /api/wishlist/items/{id}?userId=` | Actualizar cantidad |
| Wishlist | `DELETE /api/wishlist/items/{id}?userId=` | Eliminar deseo |
| Wishlist | `GET /api/wishlist/events` | Eventos (lo consume history-service) |
| Historial | `/api/history/**` | Consulta del histórico |

## Comandos útiles

```bash
docker compose logs -f wishlist-service   # ver logs de un servicio
docker compose restart frontend           # reiniciar un servicio
docker compose down                       # detener (conserva los datos)
docker compose down -v                    # detener y borrar la base de datos
```

## Solución de problemas

| Problema | Qué revisar |
|---|---|
| Puerto 3307 ocupado | Cambia `MYSQL_PORT` en `.env` (por ejemplo `3308`) |
| Un servicio sale con error 503 desde el gateway | `docker compose ps` y `docker compose logs <servicio>`: puede seguir arrancando o no conectar con la base de datos |
| Cambié un script SQL y no se aplica | Los scripts solo corren con el volumen vacío: `docker compose down -v` |
| Los cambios de variables de entorno no se ven | `docker compose up -d` vuelve a crear los contenedores afectados |

## Dos formas de trabajar (entornos)

El proyecto se puede usar de dos maneras. Lo único que cambia es el archivo `.env`: el código y el `docker-compose.yml` son los mismos.

| | Entorno local (Docker) | Entorno de equipo (MySQL compartido) |
|---|---|---|
| Para quién | Quien clona el repositorio por primera vez o evalúa el proyecto | El equipo de desarrollo |
| Base de datos | Contenedor `mysql`, con las bases y productos de ejemplo ya cargados | MySQL de un equipo del grupo, alcanzado por Tailscale |
| `DB_HOST` | `mysql` | IP de Tailscale del equipo que aloja la base |
| `DB_PORT` | `3306` | `3306` |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `sweetcolors123` | Las credenciales personales de esa base |
| `.env` | Copia de `.env.example` | El `.env` propio de cada persona (no se sube a Git) |

### Entorno local (Docker)

```bash
cp .env.example .env
docker compose up --build -d
```

Los datos de esta base viven en el volumen `mysql_data` y no se comparten con nadie. Para empezar de cero: `docker compose down -v`.

### Entorno de equipo (base compartida por Tailscale)

1. Tener Tailscale activo y con acceso al equipo que aloja el MySQL compartido.
2. En tu `.env`, poner la IP de Tailscale en `DB_HOST`, el puerto `3306` en `DB_PORT` y tus credenciales en `DB_USERNAME` y `DB_PASSWORD`.
3. Las cuatro bases (`auth_db`, `catalog_db`, `wishlist_db`, `history_db`) deben existir en ese servidor. Quien lo administra las crea una sola vez ejecutando `database/init/01-schema.sql` desde MySQL Workbench.
4. Levantar los servicios:

```bash
docker compose up -d
```

Los servicios se conectan a la base compartida. El contenedor `mysql` local también arranca, pero no se usa; publica el puerto `3307` para no chocar con un MySQL propio que ya use el `3306`. Para explorar la base compartida se usa MySQL Workbench con la IP de Tailscale y el puerto `3306`.

### Cambiar de un entorno a otro

Edita el `.env` y ejecuta `docker compose up -d`, que recrea los contenedores con la nueva configuración. Los datos **no** se comparten entre entornos: los usuarios y listas de deseos creados en uno no aparecen en el otro.

## Flujo de ramas (GitFlow)

- `main`: versiones estables.
- `develop`: integración de todos los servicios.
- `feature/<nombre>`: una rama por servicio o funcionalidad (`feature/catalog-service`, `feature/wishlist-service`, `feature/history-service`, `feature/frontend`...), que se integra a `develop` mediante Pull Request.

## Notas

- `GET /api/wishlist/events` es público de forma temporal para que history-service pueda consumirlo sin JWT de usuario. Queda como mejora la autenticación entre servicios.
- Cada servicio crea o actualiza sus tablas con Hibernate (`ddl-auto: update`); los scripts de `database/init` dejan la base lista desde el inicio.