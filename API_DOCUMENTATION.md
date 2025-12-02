# API Documentation - GastuApp

Documentación completa de todos los endpoints disponibles en el sistema GastuApp.

**Base URL**: `http://localhost:8080`

---

## 🔐 Autenticación

### Registrar Usuario

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "juanperez",
  "email": "juan@example.com",
  "telefono": "1234567890",
  "password": "miPassword123"
}
```

**Respuesta exitosa (200)**:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "juanperez",
  "userId": 1,
  "email": "juan@example.com",
  "message": "Usuario registrado exitosamente"
}
```

### Iniciar Sesión

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "juanperez",
  "password": "miPassword123"
}
```

**Respuesta exitosa (200)**:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "juanperez",
  "userId": 1,
  "email": "juan@example.com"
}
```

> **Nota**: El token debe incluirse en todas las peticiones subsiguientes en el header `Authorization: Bearer {token}`

---

## 👤 Perfil de Usuario

Endpoints para gestionar la información del usuario autenticado.

### Obtener Perfil

```http
GET /api/usuario
Authorization: Bearer {token}
```

**Respuesta (200)**:

```json
{
  "id": 1,
  "username": "juanperez",
  "email": "juan@example.com",
  "telefono": "1234567890"
}
```

### Actualizar Perfil

Permite actualizar el nombre de usuario. El correo no es modificable.

```http
PUT /api/usuario
Authorization: Bearer {token}
Content-Type: application/json

{
  "username": "juanperez_nuevo"
}
```

**Respuesta (200)**:

```json
{
  "id": 1,
  "username": "juanperez_nuevo",
  "email": "juan@example.com",
  "telefono": "1234567890"
}
```

### Cambiar Contraseña

```http
POST /api/usuario/cambiar-password
Authorization: Bearer {token}
Content-Type: application/json

{
  "passwordActual": "password123",
  "passwordNueva": "nuevaPassword123"
}
```

**Respuesta (200)**:

```json
{
  "mensaje": "Contraseña actualizada exitosamente"
}
```

---

## 📊 Conceptos

Los conceptos clasifican los movimientos financieros (INGRESO, EGRESO, AHORRO).

### Listar Todos los Conceptos

```http
GET /api/conceptos
Authorization: Bearer {token}
```

**Respuesta (200)**:

```json
[
  {
    "id": 1,
    "tipo": "INGRESO",
    "nombre": "Salario",
    "descripcion": "Salario mensual"
  },
  {
    "id": 8,
    "tipo": "EGRESO",
    "nombre": "Alimentación",
    "descripcion": "Compras de comida y restaurantes"
  }
]
```

### Obtener Concepto por ID

```http
GET /api/conceptos/{id}
Authorization: Bearer {token}
```

**Ejemplo**: `GET /api/conceptos/8`

**Respuesta (200)**:

```json
{
  "id": 8,
  "tipo": "EGRESO",
  "nombre": "Alimentación",
  "descripcion": "Compras de comida y restaurantes"
}
```

### Filtrar Conceptos por Tipo

```http
GET /api/conceptos/tipo/{tipo}
Authorization: Bearer {token}
```

**Valores válidos para {tipo}**: `INGRESO`, `EGRESO`, `AHORRO`

**Ejemplo**: `GET /api/conceptos/tipo/EGRESO`

**Respuesta (200)**:

```json
[
  {
    "id": 8,
    "tipo": "EGRESO",
    "nombre": "Alimentación",
    "descripcion": "Compras de comida y restaurantes"
  },
  {
    "id": 9,
    "tipo": "EGRESO",
    "nombre": "Transporte",
    "descripcion": "Gasolina, transporte público, Uber"
  }
]
```

### Crear Concepto (Solo Admin)

```http
POST /api/conceptos
Authorization: Bearer {token}
Content-Type: application/json

{
  "tipo": "EGRESO",
  "nombre": "Educación",
  "descripcion": "Cursos, libros, materiales educativos"
}
```

**Respuesta (201)**:

```json
{
  "id": 13,
  "tipo": "EGRESO",
  "nombre": "Educación",
  "descripcion": "Cursos, libros, materiales educativos"
}
```

### Actualizar Concepto (Solo Admin)

```http
PUT /api/conceptos/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "tipo": "EGRESO",
  "nombre": "Educación y Formación",
  "descripcion": "Cursos, libros, certificaciones"
}
```

**Ejemplo**: `PUT /api/conceptos/13`

### Eliminar Concepto (Solo Admin)

```http
DELETE /api/conceptos/{id}
Authorization: Bearer {token}
```

**Ejemplo**: `DELETE /api/conceptos/13`

**Respuesta (204)**: Sin contenido

---

## 💰 Ingresos

### Listar Ingresos del Usuario

```http
GET /api/movimientos/ingresos
Authorization: Bearer {token}
```

**Respuesta (200)**:

```json
[
  {
    "id": 1,
    "monto": 5000.0,
    "descripcion": "Salario de noviembre",
    "conceptoId": 1,
    "fechaRegistro": "2025-11-28T10:30:00"
  },
  {
    "id": 2,
    "monto": 500.0,
    "descripcion": "Freelance proyecto web",
    "conceptoId": 4,
    "fechaRegistro": "2025-11-25T15:20:00"
  }
]
```

### Obtener Ingreso Específico

```http
GET /api/movimientos/ingresos/{id}
Authorization: Bearer {token}
```

**Ejemplo**: `GET /api/movimientos/ingresos/1`

### Crear Ingreso

```http
POST /api/movimientos/ingresos
Authorization: Bearer {token}
Content-Type: application/json

{
  "monto": 5000.00,
  "descripcion": "Salario de diciembre",
  "conceptoId": 1
}
```

**Validaciones**:

- `monto`: Obligatorio, mayor a 0, máximo 10 dígitos enteros y 2 decimales
- `descripcion`: Opcional, máximo 100 caracteres
- `conceptoId`: Obligatorio, debe existir y ser de tipo INGRESO

**Respuesta (201)**:

```json
{
  "id": 3,
  "monto": 5000.0,
  "descripcion": "Salario de diciembre",
  "conceptoId": 1,
  "fechaRegistro": "2025-11-28T17:25:00"
}
```

### Actualizar Ingreso

```http
PUT /api/movimientos/ingresos/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "monto": 5200.00,
  "descripcion": "Salario de diciembre + bono",
  "conceptoId": 1
}
```

**Ejemplo**: `PUT /api/movimientos/ingresos/3`

### Eliminar Ingreso

```http
DELETE /api/movimientos/ingresos/{id}
Authorization: Bearer {token}
```

**Ejemplo**: `DELETE /api/movimientos/ingresos/3`

**Respuesta (204)**: Sin contenido

---

## 💸 Egresos

Los egresos incluyen validaciones automáticas de salud financiera y generan notificaciones cuando se detectan problemas.

### Listar Egresos del Usuario

```http
GET /api/movimientos/egresos
Authorization: Bearer {token}
```

**Respuesta (200)**:

```json
[
  {
    "id": 1,
    "monto": 1200.0,
    "descripcion": "Renta del mes",
    "conceptoId": 10,
    "fechaRegistro": "2025-11-28T09:00:00"
  },
  {
    "id": 2,
    "monto": 300.0,
    "descripcion": "Supermercado",
    "conceptoId": 8,
    "fechaRegistro": "2025-11-27T18:30:00"
  }
]
```

### Obtener Egreso Específico

```http
GET /api/movimientos/egresos/{id}
Authorization: Bearer {token}
```

**Ejemplo**: `GET /api/movimientos/egresos/1`

### Crear Egreso

```http
POST /api/movimientos/egresos
Authorization: Bearer {token}
Content-Type: application/json

{
  "monto": 500.00,
  "descripcion": "Compras del mes",
  "conceptoId": 8
}
```

**Validaciones**:

- `monto`: Obligatorio, mayor a 0, máximo 10 dígitos enteros y 2 decimales
- `descripcion`: Opcional, máximo 100 caracteres
- `conceptoId`: Obligatorio, debe existir y ser de tipo EGRESO

**Validaciones de Salud Financiera** (automáticas):

1. **Egresos > Ingresos**: Notifica si los egresos totales superan los ingresos totales
2. **Umbral de advertencia**: Notifica si se alcanza el porcentaje configurado (default: 80%)
3. **Egreso individual grande**: Notifica si un egreso representa más del porcentaje configurado (default: 30%)

**Respuesta (201)**:

```json
{
  "id": 3,
  "monto": 500.0,
  "descripcion": "Compras del mes",
  "conceptoId": 8,
  "fechaRegistro": "2025-11-28T17:30:00"
}
```

> **Nota**: Si se generan notificaciones, se crean automáticamente en la tabla de notificaciones.

### Actualizar Egreso

```http
PUT /api/movimientos/egresos/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "monto": 550.00,
  "descripcion": "Compras del mes (actualizado)",
  "conceptoId": 8
}
```

**Ejemplo**: `PUT /api/movimientos/egresos/3`

### Eliminar Egreso

```http
DELETE /api/movimientos/egresos/{id}
Authorization: Bearer {token}
```

**Ejemplo**: `DELETE /api/movimientos/egresos/3`

**Respuesta (204)**: Sin contenido

---

## 🔔 Notificaciones

Las notificaciones se generan automáticamente cuando se detectan eventos importantes (egresos que superan ingresos, umbrales alcanzados, etc.).

### Listar Todas las Notificaciones

```http
GET /api/notificaciones
Authorization: Bearer {token}
```

**Respuesta (200)**:

```json
[
  {
    "id": 1,
    "tipo": "MOVIMIENTO",
    "referenciaId": 3,
    "titulo": "Egresos superan ingresos",
    "descripcion": "Tus egresos totales ($5200.00) han superado tus ingresos ($5000.00) por $200.00. Considera revisar tus gastos para mantener un balance financiero saludable.",
    "leida": false,
    "fechaCreacion": "2025-11-28T17:30:00"
  },
  {
    "id": 2,
    "tipo": "MOVIMIENTO",
    "referenciaId": 2,
    "titulo": "Umbral de gastos alcanzado",
    "descripcion": "Has utilizado el 85.00% de tus ingresos ($4250.00 de $5000.00). Tu umbral de advertencia está configurado en 80%. Considera moderar tus gastos.",
    "leida": true,
    "fechaCreacion": "2025-11-27T18:30:00"
  }
]
```

### Listar Notificaciones No Leídas

```http
GET /api/notificaciones/no-leidas
Authorization: Bearer {token}
```

**Respuesta (200)**:

```json
[
  {
    "id": 1,
    "tipo": "MOVIMIENTO",
    "referenciaId": 3,
    "titulo": "Egresos superan ingresos",
    "descripcion": "Tus egresos totales ($5200.00) han superado tus ingresos ($5000.00) por $200.00...",
    "leida": false,
    "fechaCreacion": "2025-11-28T17:30:00"
  }
]
```

### Contar Notificaciones No Leídas

```http
GET /api/notificaciones/no-leidas/count
Authorization: Bearer {token}
```

**Respuesta (200)**:

```json
{
  "count": 3
}
```

### Marcar Notificación como Leída

```http
PUT /api/notificaciones/{id}/marcar-leida
Authorization: Bearer {token}
```

**Ejemplo**: `PUT /api/notificaciones/1/marcar-leida`

**Respuesta (200)**:

```json
{
  "id": 1,
  "tipo": "MOVIMIENTO",
  "referenciaId": 3,
  "titulo": "Egresos superan ingresos",
  "descripcion": "Tus egresos totales ($5200.00) han superado tus ingresos ($5000.00) por $200.00...",
  "leida": true,
  "fechaCreacion": "2025-11-28T17:30:00"
}
```

---

## ⚙️ Preferencias Financieras

Permite configurar los umbrales de validación de salud financiera sin modificar la base de datos.

### Obtener Preferencias Actuales

```http
GET /api/movimientos/preferencias
Authorization: Bearer {token}
```

**Respuesta (200)**:

```json
{
  "umbralAdvertenciaPorcentaje": 80,
  "egresoGrandePorcentaje": 30,
  "alertaEgresoGrandeActiva": true
}
```

**Valores por defecto**:

- `umbralAdvertenciaPorcentaje`: 80 (notifica cuando egresos >= 80% de ingresos)
- `egresoGrandePorcentaje`: 30 (notifica cuando un egreso >= 30% de ingresos totales)
- `alertaEgresoGrandeActiva`: true (activa la validación de egreso grande)

### Actualizar Preferencias

```http
PUT /api/movimientos/preferencias
Authorization: Bearer {token}
Content-Type: application/json

{
  "umbralAdvertenciaPorcentaje": 70,
  "egresoGrandePorcentaje": 25,
  "alertaEgresoGrandeActiva": true
}
```

**Validaciones**:

- `umbralAdvertenciaPorcentaje`: Obligatorio, entre 1 y 100
- `egresoGrandePorcentaje`: Obligatorio, entre 1 y 100
- `alertaEgresoGrandeActiva`: Obligatorio, true o false

**Respuesta (200)**:

```json
{
  "umbralAdvertenciaPorcentaje": 70,
  "egresoGrandePorcentaje": 25,
  "alertaEgresoGrandeActiva": true
}
```

### Resetear Preferencias a Valores por Defecto

```http
POST /api/movimientos/preferencias/reset
Authorization: Bearer {token}
```

**Respuesta (200)**:

```json
{
  "umbralAdvertenciaPorcentaje": 80,
  "egresoGrandePorcentaje": 30,
  "alertaEgresoGrandeActiva": true
}
```

---

## 📝 Notas Importantes

### Autenticación

- Todos los endpoints (excepto `/api/auth/*`) requieren el header `Authorization: Bearer {token}`
- El token se obtiene al hacer login
- Si el token expira, debes hacer login nuevamente

### Validaciones de Conceptos

- Al crear/actualizar **ingresos**, el `conceptoId` debe ser de tipo `INGRESO`
- Al crear/actualizar **egresos**, el `conceptoId` debe ser de tipo `EGRESO`
- Si el concepto no existe o es del tipo incorrecto, recibirás un error 400

### Validaciones de Salud Financiera

- Se ejecutan automáticamente al crear o actualizar egresos
- Generan notificaciones de tipo `MOVIMIENTO`
- El egreso se registra incluso si excede el presupuesto (solo notifica)
- Las preferencias se almacenan en memoria (se pierden al reiniciar el servidor)

### Códigos de Respuesta HTTP

- `200 OK`: Operación exitosa
- `201 Created`: Recurso creado exitosamente
- `204 No Content`: Operación exitosa sin contenido de respuesta
- `400 Bad Request`: Error de validación o datos inválidos
- `401 Unauthorized`: Token inválido o ausente
- `403 Forbidden`: Sin permisos para acceder al recurso
- `404 Not Found`: Recurso no encontrado

---

## 🧪 Ejemplo de Flujo Completo

### 1. Registrar usuario

```http
POST /api/auth/register
{
  "nombre": "María García",
  "correo": "maria@example.com",
  "telefono": "9876543210",
  "password": "password123"
}
```

### 2. Iniciar sesión

```http
POST /api/auth/login
{
  "correo": "maria@example.com",
  "password": "password123"
}
```

Guardar el `token` de la respuesta.

### 3. Ver conceptos disponibles

```http
GET /api/conceptos/tipo/INGRESO
Authorization: Bearer {token}
```

### 4. Registrar un ingreso

```http
POST /api/movimientos/ingresos
Authorization: Bearer {token}
{
  "monto": 3000.00,
  "descripcion": "Salario",
  "conceptoId": 1
}
```

### 5. Ver conceptos de egresos

```http
GET /api/conceptos/tipo/EGRESO
Authorization: Bearer {token}
```

### 6. Registrar egresos

```http
POST /api/movimientos/egresos
Authorization: Bearer {token}
{
  "monto": 1000.00,
  "descripcion": "Renta",
  "conceptoId": 10
}
```

### 7. Verificar notificaciones

```http
GET /api/notificaciones/no-leidas
Authorization: Bearer {token}
```

### 8. Ajustar preferencias

```http
PUT /api/movimientos/preferencias
Authorization: Bearer {token}
{
  "umbralAdvertenciaPorcentaje": 90,
  "egresoGrandePorcentaje": 40,
  "alertaEgresoGrandeActiva": true
}
```
