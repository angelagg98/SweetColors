# catalog-service

Microservicio de catalogo de productos de SweetColors. Permite el CRUD de productos
(nombre, precio, stock y categoria: `DESAYUNO` o `DECORACION`) y expone un endpoint de
consulta de stock para el wishlist-service.

| Dato | Valor |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Build | Maven |
| Puerto | 8082 |
| Base de datos | MySQL, schema `catalog_db` |

## Requisitos

- Java 17
- Maven 3.9+
- Acceso al servidor MySQL compartido del equipo (Tailscale)
- El schema `catalog_db` debe existir en MySQL

## Configuracion

1. Copia el archivo `.env.example` como `.env` y rellena los datos:

```bash
cp .env.example .env
```

Variables necesarias: `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`.
El `.env` no se sube a Git.

2. Crea la base de datos (si no existe):

```sql
CREATE DATABASE catalog_db;
```

`ddl-auto: update` crea la tabla `products` automaticamente al levantar el servicio.

## Como correrlo

```bash
mvn spring-boot:run
```

## Endpoints

Base URL: `http://localhost:8082/api/catalog/products`

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/api/catalog/products` | Lista todos los productos. Opcional `?category=DESAYUNO` o `?category=DECORACION` para filtrar |
| GET | `/api/catalog/products/{id}` | Obtiene un producto por id |
| GET | `/api/catalog/products/{id}/stock` | Consulta stock actual e `inStock` (usado por wishlist-service) |
| POST | `/api/catalog/products` | Crea un producto |
| PUT | `/api/catalog/products/{id}` | Actualiza un producto |
| DELETE | `/api/catalog/products/{id}` | Elimina un producto |

### Ejemplo de POST

```json
{
  "name": "Desayuno clasico",
  "price": 45000.00,
  "stock": 10,
  "category": "DESAYUNO"
}
```

## Estructura

```
src/main/java/com/sweetcolors/catalogservice/
├── controller/   ProductController.java
├── dto/          ProductRequest, ProductResponse, StockResponse
├── entity/       Product (con enum Category)
├── exception/    GlobalExceptionHandler, ProductNotFoundException
├── repository/   ProductRepository.java
└── service/      ProductService.java
```