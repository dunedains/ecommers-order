# Orders Service

Microservicio de gestión de órdenes de compra. Crea órdenes a partir de un producto, calcula el total automáticamente y gestiona el ciclo de vida del pedido.

## Información general

| Campo | Valor |
|-------|-------|
| Puerto | `8087` |
| Base de datos | `db_orders` (PostgreSQL) |
| Contexto | `/api/orders` |

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/orders/{id}` | Obtener orden por ID |
| `GET` | `/api/orders/user/{userId}` | Listar órdenes de un usuario |
| `POST` | `/api/orders` | Crear nueva orden |
| `PATCH` | `/api/orders/{id}/status` | Actualizar estado de la orden |
| `DELETE` | `/api/orders/{id}` | Cancelar orden |

## Estados de una orden

```
PENDING → CONFIRMED → (fin)
PENDING → CANCELLED
CONFIRMED → CANCELLED  (vía reembolso desde pagos)
```

## Ejemplo de uso

**Crear orden:**
```bash
curl -X POST http://localhost:8087/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "productId": 1,
    "quantity": 2
  }'
```

**Respuesta:**
```json
{
  "id": 1,
  "userId": 1,
  "productId": 1,
  "quantity": 2,
  "totalAmount": 2999.98,
  "status": "PENDING"
}
```

**Actualizar estado:**
```bash
curl -X PATCH "http://localhost:8087/api/orders/1/status?status=CONFIRMED"
```

## Modelo de datos

```sql
CREATE TABLE orders (
    id           BIGINT           GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT           NOT NULL,
    product_id   BIGINT           NOT NULL,
    quantity     INTEGER          NOT NULL,
    total_amount DOUBLE PRECISION NOT NULL,
    status       VARCHAR(20)      NOT NULL DEFAULT 'PENDING'
);
```

## Dependencias externas

| Servicio | Uso | Puerto |
|---------|-----|--------|
| **productos** | Obtiene nombre y precio del producto para calcular total | `8081` |

## Configuración (variables de entorno Docker)

| Variable | Descripción |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | URL de conexión a PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos |
| `FEIGN_CLIENT_PRODUCT_URL` | URL del servicio de productos |

## Tecnologías

- Java 25 · Spring Boot 4.0.6
- Spring Data JPA · Hibernate 7
- Spring Cloud OpenFeign
- Flyway (migraciones)
- PostgreSQL 16
- Lombok · Bean Validation
