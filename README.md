# Admitienda

Aplicación para la caja, el inventario y el fiado de una tienda.
Backend en Spring Boot (arquitectura hexagonal) con SQLite, y frontend en React + Vite.
El contrato de la API está en [`docs/CONTRATO_API.md`](docs/CONTRATO_API.md).

## Requisitos

- Java 17
- Maven 3.9+ (o el wrapper de Maven ya descargado en `~/.m2/wrapper`)
- Node.js 20+ con npm

## Cómo ejecutar

Backend (puerto 8080):

```bash
cd backend
mvn spring-boot:run
```

Frontend (puerto 5173, con proxy de `/api` al backend):

```bash
cd frontend
npm install
npm run dev
```

Abre http://localhost:5173.

Pruebas:

```bash
cd backend && mvn test
cd frontend && npm run lint && npm run build && npx vitest run
```

## Usuarios de prueba

Hay un usuario por rol para probar los permisos. Las contraseñas son solo para desarrollo.

| Rol        | Usuario         | Contraseña         | Qué puede hacer                                                        |
|------------|-----------------|--------------------|------------------------------------------------------------------------|
| INVITADO   | `invitado`      | `invitado123`      | Sin acceso a resumen, usuarios ni solicitudes (solo catálogo público). |
| REGISTRADO | `registrado`    | `registrado123`    | Consultar resumen, caja, deudas y alertas. Puede solicitar ser admin.  |
| ADMIN      | `administrador` | `administrador123` | Acceso completo: gestionar usuarios, productos, caja y fiado.          |

Administrador inicial de la configuración: `admin` / `admin123`
(`backend/src/main/resources/application.yml`). Cámbialo antes de cualquier despliegue.

Otros usuarios que ya existen en la base de datos: `ana` (REGISTRADO), `PEDRO` (REGISTRADO), `juan` (REGISTRADO).

## Datos de prueba

La base `backend/admitienda.db` contiene datos inventados para explorar la tienda:

- **Productos:** 16 productos de varias categorías (bebidas, snacks, lácteos, aseo, frutas, abarrotes), algunos con stock bajo para probar las alertas.
- **Caja:** movimientos de ingresos y egresos de los últimos meses.
- **Fiado:** deudas de clientes como María Pérez, Carlos Rojas y Lucía Gómez, con abonos registrados.
- **Solicitudes de admin:** solicitudes pendientes de `PEDRO` y `juan`, y una ya aprobada de `ana`.

Si borras `admitienda.db`, el arranque la vuelve a crear con la semilla de `IniciadorDeDatos`.
Esa semilla solo crea el administrador inicial, `ana` y datos de demostración; los usuarios de la tabla de arriba se agregan aparte.
