# Contrato de la API REST — Admitienda

Contrato único entre el backend (Spring Boot, arquitectura hexagonal) y el
frontend (React). Toda la comunicación va en JSON con campos en camelCase y
montos en pesos colombianos como número (máximo 2 decimales).

## Convenciones

- Autenticación por token de sesión: cabecera `Authorization: Bearer <token>`.
  El token se obtiene en `POST /api/sesiones` y dura 24 horas.
- Rutas públicas (sin token): `POST /api/sesiones`, `POST /api/usuarios` y las
  lecturas de productos (`GET /api/productos**`).
- Resto de rutas: requieren token. Los permisos finos (ADMIN / REGISTRADO)
  los decide el dominio en cada caso de uso.
- Errores: cualquier fallo responde `4xx` con `{"error": "mensaje legible"}`.
  Las validaciones pueden responder `400 {"error": "línea 1\nlínea 2"}`.
- Fechas: enteros en milisegundos epoch (UTC). El frontend las formatea
  con zona `America/Bogota`.

## Sesiones

### `POST /api/sesiones` — iniciar sesión (público)

```json
// petición
{ "nombreUsuario": "admin", "contrasena": "admin123" }
// respuesta 200
{ "token": "uuid", "usuario": { "id": 1, "nombreUsuario": "admin", "rol": "ADMIN" } }
// error 401
{ "error": "Usuario o contraseña incorrectos." }
```

### `DELETE /api/sesiones` — cerrar sesión (token)

Responde `204` sin cuerpo.

### `GET /api/sesiones/actual` — validar token / restaurar sesión (token)

Responde 200 con `{ "usuario": { ... } }` o 401.

## Usuarios

### `POST /api/usuarios` — registro (público)

```json
{ "nombreUsuario": "ana", "contrasena": "ana12345", "indicio": "mi mascota" }
```
Responde 201 con el usuario creado (rol `REGISTRADO`). Errores: nombre
ocupado (409), datos inválidos (400).

### `GET /api/usuarios` — listar (ADMIN)

Responde 200 con `[{ "id": 1, "nombreUsuario": "admin", "rol": "ADMIN", "fechaRegistro": 1789000000000 }]`.

### `PUT /api/usuarios/{id}/rol` — cambiar rol (ADMIN)

```json
{ "rol": "ADMIN" }   // INVITADO | REGISTRADO | ADMIN
```
Regla: no puede quedar la tienda sin administradores. Responde 200 con el usuario.

### `DELETE /api/usuarios/{id}` — eliminar (ADMIN)

Elimina también sus solicitudes. Regla del último administrador. Responde 204.

## Solicitudes de administrador

### `POST /api/solicitudes-admin` — solicitar permiso (REGISTRADO)

```json
{ "motivo": "Necesito administrar el inventario" }
```
Solo una solicitud PENDIENTE por usuario; los ADMIN no solicitan.
Responde 201 con `{ "id": 3, "usuarioId": 2, "nombreUsuario": "ana", "estado": "PENDIENTE", "fechaSolicitud": 1789000000000, "motivo": "..." }`.

### `GET /api/solicitudes-admin` — listar (ADMIN)

Responde 200 con la lista (PENDIENTE primero).

### `POST /api/solicitudes-admin/{id}/aprobar` — aprobar (ADMIN)

Promueve al solicitante a ADMIN. Responde 200 con la solicitud APROBADA.

### `POST /api/solicitudes-admin/{id}/rechazar` — rechazar (ADMIN)

Responde 200 con la solicitud RECHAZADA (el rol no cambia).

## Productos

### `GET /api/productos?nombre=` — listar (público)

```json
[{
  "id": 1, "tipoProducto": "Abarrotes", "nombreProducto": "Arroz 500g",
  "precioNeto": 1800, "ganancia": 500, "precioVenta": 2300,
  "cantidadStock": 40, "stockMinimo": 5, "enAlerta": false
}]
```
`nombre` es un filtro opcional por texto contenido. `precioVenta` siempre
es `precioNeto + ganancia` (calculado, no se envía al crear/editar).

### `GET /api/productos/{id}` — consultar (público)

Responde 200 con el producto o 404.

### `POST /api/productos` — crear (ADMIN)

```json
{ "tipoProducto": "Abarrotes", "nombreProducto": "Arroz 500g",
  "precioNeto": 1800, "ganancia": 500, "cantidadStock": 40, "stockMinimo": 5 }
```

### `PUT /api/productos/{id}` — editar (ADMIN)

Mismo cuerpo de creación. Responde 200 con el producto actualizado.

### `DELETE /api/productos/{id}` — eliminar (ADMIN)

Responde 204.

## Movimientos (caja)

### `GET /api/movimientos?desde=&hasta=` — listar (REGISTRADO)

`desde`/`hasta` opcionales en milisegundos epoch. Responde 200:

```json
{
  "saldoCaja": 150000,
  "movimientos": [{
    "id": 1, "tipo": "INGRESO", "monto": 50000,
    "descripcion": "Venta del día", "fecha": 1789000000000
  }]
}
```
`saldoCaja` = suma de INGRESOS − suma de EGRESOS (histórico completo).

### `POST /api/movimientos` — registrar (ADMIN)

```json
{ "tipo": "EGRESO", "monto": 12000.5, "descripcion": "Compra de bolsas" }
```
`monto` > 0 con máximo 2 decimales. La fecha la fija el servidor.
Responde 201 con el movimiento creado.

### `DELETE /api/movimientos/{id}` — eliminar (ADMIN)

Responde 204. El saldo de caja se recalcula.

## Deudas (fiado)

### `GET /api/deudores` — listar (REGISTRADO)

```json
[{
  "id": 1, "nombreDeudor": "María Pérez", "deudasPendientes": 2,
  "totalPendiente": 30000, "fechaUltimaDeuda": 1789000000000
}]
```

### `GET /api/deudores/{id}` — historial (REGISTRADO)

```json
{
  "id": 1, "nombreDeudor": "María Pérez",
  "deudas": [{
    "id": 7, "monto": 20000, "saldo": 15000, "descripcion": "Mercado",
    "fecha": 1789000000000, "pagos": [{ "id": 1, "monto": 5000, "fecha": 1789000100000 }]
  }]
}
```

### `POST /api/deudas` — registrar venta fiada (ADMIN)

```json
{ "nombreDeudor": "María Pérez", "monto": 20000, "descripcion": "Mercado" }
```
El deudor se resuelve por nombre normalizado (sin mayúsculas ni espacios
repetidos): si existe se reutiliza. Responde 201 con la deuda creada.

### `PUT /api/deudas/{id}` — editar valor/descripción (ADMIN)

```json
{ "monto": 22000, "descripcion": "Mercado completo" }
```
Regla: el monto no puede ser menor que lo ya abonado (`monto − saldo`).
El saldo se recalcula conservando lo abonado. Responde 200 con la deuda.

### `POST /api/deudas/{id}/abonos` — abonar (ADMIN)

```json
{ "monto": 5000, "descripcion": "Abono parcial" }
```
Descuenta el saldo y registra un INGRESO en caja por el mismo valor
("Pago de deuda de María Pérez"). No se puede abonar más que el saldo ni a
una deuda saldada. Responde 200 con la deuda actualizada.

### `DELETE /api/deudas/{id}` — eliminar (ADMIN)

Elimina la deuda y sus pagos. Responde 204.

## Resumen y alertas

### `GET /api/resumenes` — resumen general (REGISTRADO)

```json
{
  "saldoCaja": 150000,
  "totalPorCobrar": 45000,
  "porDia": [{ "fecha": "2026-11-03", "ingresos": 50000, "egresos": 12000, "neto": 38000 }],
  "porMes": [{ "mes": "2026-11", "ingresos": 900000, "egresos": 400000, "neto": 500000 }]
}
```
`porDia`: últimos 7 días (hora Colombia). `porMes`: últimos 12 meses.

### `GET /api/alertas` — productos con stock bajo (REGISTRADO)

```json
[{
  "id": 3, "nombreProducto": "Huevos 30", "tipoProducto": "Abarrotes",
  "cantidadStock": 2, "stockMinimo": 5, "faltante": 3
}]
```
Un producto está en alerta cuando `cantidadStock <= stockMinimo`.
