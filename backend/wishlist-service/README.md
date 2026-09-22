# wishlist-service

Microservicio de lista de deseos de SweetColors. Permite a un usuario agregar, listar,
actualizar y eliminar productos de su wishlist. Al listar, verifica el stock real contra
el `catalog-service` y registra eventos (agregado, eliminado, actualizado, sin-stock) que
quedan disponibles para el `history-service`.

| Dato | Valor |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Build | Maven |
| Puerto | 8083 |
| Base de datos | MySQL, schema `wishlist_db` |
| Depende de | `catalog-service` (puerto 8082) para validar stock y nombre de producto |

## Requisitos

- Java 17
- Maven 3.9+
- Acceso al servidor MySQL compartido del equipo (Tailscale)
- El schema `wishlist_db` debe existir en MySQL
- El `catalog-service` corriendo en `http://localhost:8082` (ajustable con `CATALOG_BASE_URL`)

## Configuracion

1. Copia el archivo `.env.example` como `.env` y rellena los datos:

```bash
cp .env.example .env
```

2. Crea la base de datos (si no existe):

```sql
CREATE DATABASE wishlist_db;
```

`ddl-auto: update` crea las tablas (`wishlist_items`, `wishlist_events`) al levantar.

## Como correrlo

1. Levanta primero `catalog-service` (puerto 8082).
2. Luego este servicio:

```bash
mvn spring-boot:run
```

## Endpoints

Base URL: `http://localhost:8083/api/wishlist`

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/items?userId=1` | Lista la wishlist del usuario, con stock real consultado al catalog |
| POST | `/items` | Agrega un producto a la wishlist |
| PUT | `/items/{id}?userId=1` | Actualiza la cantidad del item |
| DELETE | `/items/{id}?userId=1` | Elimina el item de la wishlist |
| GET | `/events?userId=1` | Lista eventos registrados (para history-service; `userId` opcional) |

### Ejemplo de POST

```json
{
  "userId": 1,
  "productId": 2,
  "quantity": 1
}
```

Al hacer `GET /items` se incluye por cada item el campo `inStock`:
`true` (disponible), `false` (agotado, se registra evento `OUT_OF_STOCK`) o `null`
(catalog-service no respondio, stock no verificable).

## Estructura

```
src/main/java/com/sweetcolors/wishlistservice/
├── client/      CatalogClient.java (RestClient al catalog-service)
├── config/      CorsConfig.java (permite el frontend en :5173)
├── controller/  WishlistController.java
├── dto/         WishlistItemRequest, WishlistItemResponse, UpdateQuantityRequest, WishlistEventResponse
├── entity/      WishlistItem, WishlistEvent (con enum EventType)
├── exception/   GlobalExceptionHandler, WishlistItemNotFoundException, CatalogUnavailableException
├── repository/  WishlistItemRepository, WishlistEventRepository
└── service/     WishlistService.java
```