# SweetColors — Arquitectura del Proyecto

## 1. Que es esto

Aplicacion web de e-commerce para **desayunos sorpresa y decoraciones**, con catalogo de productos y lista de deseos (wishlist). Es la adaptacion de una prueba tecnica original de Carvajal (modelo B2C), llevada a este dominio.

**Funcionalidades principales:**
- Catalogo publico de productos (desayunos, decoraciones) con stock visible.
- Lista de deseos por usuario: agregar, listar, actualizar, eliminar productos.
- Notificacion cuando un producto de la wishlist ya no tiene stock.
- Historico de todo lo que ha pasado por la wishlist de cada usuario (auditoria).
- Autenticacion de usuarios con JWT.

## 2. Stack tecnologico (decision de equipo)

| Capa | Tecnologia |
|---|---|
| Backend | Java 17 + Spring Boot 3.3.4 |
| Build | Maven |
| Base de datos | MySQL 8 (una base por servicio) |
| ORM | Spring Data JPA + Hibernate |
| Autenticacion | JWT (access token en cookie httpOnly + refresh token hasheado en BD) |
| Frontend | React (Vite) |
| Contenedores | Docker + Docker Compose (plus del enunciado) |
| Control de versiones | Git + GitHub, flujo tipo GitFlow |

**Por que todo en Java/Spring Boot y no mezclar tecnologias:** el enunciado original de la prueba dice explicitamente que es obligatorio usar Java y Spring Boot. Mantener los 4 microservicios en el mismo stack evita duplicar curva de aprendizaje, configuracion de despliegue y herramientas, y deja mas tiempo para pulir los plus (JWT, Docker, arquitectura).

## 3. Los microservicios

Cada microservicio es un proyecto Maven independiente, con su propio `pom.xml`, su propia base de datos, y corre en su propio puerto.

| Servicio | Carpeta | Puerto | Base de datos | Responsable |
|---|---|---|---|---|
| Auth Service | `backend/auth-service` | 8081 | `auth_db` | Alejandro |
| Catalog Service | `backend/catalog-service` | 8082 | `catalog_db` | (por asignar) |
| Wishlist Service | `backend/wishlist-service` | 8083 | `wishlist_db` | (por asignar) |
| History Service | `backend/history-service` | 8084 | `history_db` | (por asignar) |
| API Gateway | `backend/api-gateway` | 8080 | - | (por asignar) |
| Frontend (React) | `frontend` | 5500 (Docker) / 5173 (dev local) | - | Alejandro |

Cada servicio tiene su propio `.env` (nunca se sube a Git, cada quien lo crea localmente con `.env.example` como guia) y su propio `README.md` con instrucciones especificas de esa parte.

### Que hace cada servicio

- **auth-service**: registro y login de usuarios, genera y valida JWT, guarda refresh tokens (hasheados) para poder revocarlos.
- **catalog-service**: CRUD de productos (nombre, precio, stock, categoria: desayuno o decoracion). Expone un endpoint para que wishlist-service pueda consultar si un producto sigue en stock.
- **wishlist-service**: CRUD de la lista de deseos de cada usuario. Al listar, consulta a catalog-service el stock actual y marca los productos que ya no estan disponibles.
- **history-service**: guarda el log de todo evento que ocurre en la wishlist (agregado, eliminado, actualizado, notificacion de sin-stock). Es el "historico" que pide el enunciado.

## 4. Base de datos: una por servicio

Cada servicio es dueno exclusivo de sus datos — ningun servicio consulta la base de datos de otro directamente, solo a traves de su API REST. Esto evita acoplamiento fuerte entre servicios.

Las bases son schemas distintos de un mismo servidor MySQL: `auth_db`, `catalog_db`, `wishlist_db`, `history_db`. Por defecto corren en el contenedor `mysql` de `docker-compose.yml`, que las crea con los scripts de `database/init`. El equipo tambien puede apuntar `DB_HOST` a un MySQL compartido por Tailscale.

## 5. Estrategia de autenticacion (JWT)

- El usuario hace login en `auth-service` (`POST /api/auth/login`).
- `auth-service` genera un **access token** (JWT, dura 15 min) y un **refresh token** (dura 7 dias).
- El access token se entrega en una **cookie httpOnly + secure + sameSite=strict** (el frontend no puede leerla con JS, protege contra XSS).
- El refresh token se guarda **hasheado** (nunca en texto plano) en la tabla `refresh_tokens` de `auth_db`, y tambien se entrega como cookie httpOnly aparte.
- Cada peticion a un endpoint protegido de cualquier microservicio debe incluir el access token; ese servicio valida la firma del JWT usando el mismo secreto compartido (`JWT_SECRET`, definido igual en el `.env` de cada servicio).
- Cuando el access token vence, el frontend usa el refresh token para pedir uno nuevo sin que el usuario tenga que loguearse otra vez.

## 6. Estructura del repositorio (monorepo)

```
SweetColors/
├── backend/
│   ├── auth-service/
│   ├── catalog-service/
│   ├── wishlist-service/
│   ├── history-service/
│   └── api-gateway/
├── frontend/
├── docs/
│   ├── ARQUITECTURA.md          <- este archivo
│   ├── modelo-entidad-relacion.md
│   └── (un README por servicio, se agrega cuando cada uno se termina)
├── database/init/               <- scripts SQL (esquema y datos de ejemplo)
├── docker-compose.yml
├── .env.example
└── README.md
```

## 7. Flujo de trabajo en Git (GitFlow simplificado)

- `main`: solo versiones estables, nadie hace commit directo ahi.
- `develop`: rama de integracion, donde se juntan todos los servicios terminados.
- `feature/nombre-del-servicio`: cada persona trabaja su microservicio en su propia rama, creada desde `develop`.

```bash
# Cada quien, al empezar su parte:
git checkout develop
git pull
git checkout -b feature/catalog-service   # o wishlist-service, history-service, frontend

# Trabajan normal, hacen commits frecuentes con mensajes claros:
git add .
git commit -m "feat(catalog): CRUD de productos"
git push -u origin feature/catalog-service
```

Cuando un servicio esta funcional, se abre un **Pull Request** de `feature/su-servicio` hacia `develop` en GitHub, para que otro del equipo revise antes de integrar. Esto evita que un error de una persona rompa el trabajo de las demas directamente en `develop`.

**Importante:** como cada quien trabaja en su propia carpeta (`backend/catalog-service`, etc.), es muy dificil que haya conflictos de Git entre servicios distintos — los conflictos solo pasarian si dos personas tocan el mismo archivo, lo cual no deberia ocurrir si cada quien se queda en su carpeta.

## 8. Como arranca cada servicio localmente

Cada servicio sigue el mismo patron:

```bash
cd backend/nombre-del-servicio
# Crear el .env local (ver .env.example de esa carpeta para las variables necesarias)
mvn spring-boot:run
```

Requisitos previos en la maquina de cada quien: Java 17 (recomendado instalar con SDKMAN para evitar problemas de compatibilidad con versiones mas nuevas de Java) y Maven.

**Con Docker (recomendado):** desde la raiz, `cp .env.example .env` y `docker compose up --build -d` levanta MySQL, los 4 microservicios, el gateway y el frontend. Detalle en el `README.md`.

## 9. Plus del enunciado y como los estamos cubriendo

| Plus | Como se cubre |
|---|---|
| Arquitectura de microservicios | 4 servicios independientes + frontend, cada uno con su propia BD |
| Autenticacion JWT | access token en cookie httpOnly + refresh token hasheado en BD |
| Contenedorizacion (Docker) | Dockerfile por servicio + docker-compose.yml con MySQL, gateway y frontend |
| Ramificacion (GitFlow) | descrito en la seccion 7 de este documento |

## 10. Estado actual del proyecto

- [x] Repositorio creado, estructura de carpetas, GitFlow inicial
- [x] `auth-service`: register, login, refresh, logout, filtro JWT protegiendo rutas privadas
- [x] `catalog-service`: CRUD de productos con consulta de stock
- [x] `wishlist-service`: CRUD de lista de deseos, consulta stock a catalog-service
- [x] `history-service`: historico de eventos, sincronizado con wishlist-service
- [x] Los 4 microservicios fusionados a `develop` y probados corriendo simultaneamente sin conflictos de puerto ni de base de datos
- [x] Filtro JWT integrado y probado en los 4 microservicios: catalog-service (lectura publica, escritura protegida), wishlist-service (todo protegido excepto `/events`), history-service (todo protegido)
- [x] Docker: los 4 microservicios dockerizados (Dockerfile multi-stage) + docker-compose.yml en la raiz, levanta los 4 con `docker compose up` sin necesidad de correr comandos por separado
- [x] Frontend: catalogo, login y registro, wishlist con cantidades editables y aviso de sin stock, pantalla de bienvenida y modo dia/noche
- [x] Modelo entidad-relacion documentado (`docs/modelo-entidad-relacion.md`) y scripts SQL en `database/init`
- [x] API Gateway (puerto 8080), unico punto de entrada del frontend
- [x] README con manual de despliegue, credenciales y pruebas
- [ ] Rama `main` creada y etiqueta de version
- [ ] Pendiente de seguridad: `GET /api/wishlist/events` esta publico temporalmente para que history-service pueda consumirlo sin JWT de usuario. Evaluar autenticacion servicio-a-servicio (API key compartida, por ejemplo) antes de la entrega final.