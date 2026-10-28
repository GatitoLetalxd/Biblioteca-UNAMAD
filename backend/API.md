# Documentación de la API REST — Biblioteca Central UNAMAD
**Universidad Nacional Amazónica de Madre de Dios**  
**Versión de la API:** 1.0.0 (v1)  
**Base URL:** `http://localhost:3000/api/v1` (o tu dominio en VPS: `https://tu-dominio.com/api/v1`)  
**Autenticación:** Encabezado HTTP `Authorization: Bearer <JWT_TOKEN>`  
**Formato de Intercambio:** `application/json` (y `multipart/form-data` para subida de archivos PDF)  

---

## 1. Convenciones y Códigos de Respuesta HTTP

| Código | Significado | Descripción |
|---|---|---|
| **200 OK** | Éxito | Petición procesada correctamente. Retorna datos solicitados. |
| **201 Created** | Recurso Creado | Registro insertado exitosamente (usuario, solicitud, libro). |
| **400 Bad Request** | Error de Validación | Faltan campos obligatorios o el formato de datos es inválido. |
| **401 Unauthorized** | No Autenticado | Token JWT ausente, expirado o firma inválida. |
| **403 Forbidden** | Acceso Denegado | Rol insuficiente o cuenta bloqueada por falta de solvencia/deudas. |
| **404 Not Found** | No Encontrado | El recurso solicitado (libro, solicitud, préstamo) no existe. |
| **409 Conflict** | Conflicto | Código de estudiante, personal o ISBN ya registrado. |
| **500 Server Error** | Error Interno | Fallo no controlado en el servidor. |

---

## 2. Autenticación y Registro (`/api/v1/auth`)

### 2.1. Registro Público de Estudiantes
Permite a cualquier alumno autoregistrarse. **Asigna automáticamente el rol `ESTUDIANTE`** y su correo institucional `{codigo}@unamad.edu.pe`.

- **Método y Ruta:** `POST /api/v1/auth/register`
- **Acceso:** Público (Sin token)
- **Cuerpo de la Petición (`application/json`):**
```json
{
  "codigo": "20230451",
  "password": "miPasswordSegura123",
  "nombre": "Jhordy",
  "apellido": "Pérez",
  "carrera": "Ingeniería de Sistemas e Informática",
  "telefono": "982123456"
}
```
> **Reglas de validación:**
> - `codigo`: Exactamente 8 dígitos numéricos (expresión regular `/^\d{8}$/`). No se solicita DNI.
> - `carrera`: Debe pertenecer a las carreras oficiales de la UNAMAD (ej: *Ingeniería de Sistemas e Informática*, *Ingeniería Forestal y Medio Ambiente*, *Enfermería*, *Ecoturismo*, etc.).
> - `nombre` y `apellido`: Solo primer nombre y primer apellido.

- **Respuesta Exitosa (201 Created):**
```json
{
  "success": true,
  "message": "Estudiante registrado exitosamente con rol ESTUDIANTE",
  "data": {
    "usuario": {
      "id": 3,
      "codigo": "20230451",
      "nombre": "Jhordy",
      "apellido": "Pérez",
      "carrera": "Ingeniería de Sistemas e Informática",
      "telefono": "982123456",
      "email": "20230451@unamad.edu.pe",
      "rol": "ESTUDIANTE",
      "activo": true,
      "createdAt": "2026-10-28T10:00:00.000Z"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

### 2.2. Inicio de Sesión Institucional
Permite el ingreso tanto a **Estudiantes**, **Bibliotecarios** como al **Administrador**.

- **Método y Ruta:** `POST /api/v1/auth/login`
- **Acceso:** Público
- **Cuerpo de la Petición:**
```json
{
  "codigo": "20230451",
  "password": "miPasswordSegura123"
}
```
- **Respuesta Exitosa (200 OK):**
```json
{
  "success": true,
  "message": "Autenticación exitosa",
  "data": {
    "usuario": {
      "id": 3,
      "codigo": "20230451",
      "nombre": "Jhordy",
      "apellido": "Pérez",
      "carrera": "Ingeniería de Sistemas e Informática",
      "telefono": "982123456",
      "email": "20230451@unamad.edu.pe",
      "rol": "ESTUDIANTE",
      "activo": true
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

### 2.3. Consultar Perfil Actual
Obtiene los datos del usuario portador del token.

- **Método y Ruta:** `GET /api/v1/auth/me`
- **Acceso:** Requiere token Bearer (Cualquier rol)
- **Encabezados:** `Authorization: Bearer <TOKEN>`
- **Respuesta Exitosa (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 3,
    "codigo": "20230451",
    "nombre": "Jhordy",
    "apellido": "Pérez",
    "carrera": "Ingeniería de Sistemas e Informática",
    "telefono": "982123456",
    "email": "20230451@unamad.edu.pe",
    "rol": "ESTUDIANTE",
    "activo": true,
    "createdAt": "2026-10-28T10:00:00.000Z"
  }
}
```

---

## 3. Administración de Personal (`/api/v1/admin`)

### 3.1. Crear Personal Administrativo
**Exclusivo para el Administrador.** Permite dar de alta usuarios **únicamente con rol `BIBLIOTECARIO` o `ADMIN`**. No permite crear estudiantes.

- **Método y Ruta:** `POST /api/v1/admin/usuarios`
- **Acceso:** Protegido (Solo rol `ADMIN`)
- **Cuerpo de la Petición:**
```json
{
  "codigo": "BIBLIO-02",
  "password": "passwordSegura2026",
  "nombre": "María",
  "apellido": "Sánchez",
  "carreraOArea": "Atención al Usuario en Hemeroteca",
  "telefono": "982554433",
  "email": "msanchez@unamad.edu.pe",
  "rol": "BIBLIOTECARIO"
}
```
> **Restricción:** El campo `rol` solo admite `"BIBLIOTECARIO"` o `"ADMIN"`.

- **Respuesta Exitosa (201 Created):**
```json
{
  "success": true,
  "message": "Usuario creado exitosamente con rol BIBLIOTECARIO",
  "data": {
    "id": 5,
    "codigo": "BIBLIO-02",
    "nombre": "María",
    "apellido": "Sánchez",
    "carrera": "Atención al Usuario en Hemeroteca",
    "telefono": "982554433",
    "email": "msanchez@unamad.edu.pe",
    "rol": "BIBLIOTECARIO",
    "activo": true,
    "createdAt": "2026-10-28T10:30:00.000Z"
  }
}
```

---

### 3.2. Listar Personal Administrativo
- **Método y Ruta:** `GET /api/v1/admin/usuarios`
- **Acceso:** Protegido (Solo rol `ADMIN`)
- **Respuesta Exitosa (200 OK):** Lista de usuarios con rol `ADMIN` o `BIBLIOTECARIO`.

---

## 4. Catálogo de Libros (`/api/v1/libros`)

### 4.1. Listar Catálogo con Búsqueda y Filtros
- **Método y Ruta:** `GET /api/v1/libros`
- **Acceso:** Público
- **Parámetros de Consulta (Query Params Opcionales):**
  - `search`: Coincidencia textual en título, autor, categoría o ISBN (ej: `?search=algoritmos`).
  - `categoria`: Filtrar por categoría académica (ej: `?categoria=Ingeniería de Sistemas`).
  - `formato`: `FISICO`, `DIGITAL` o `HIBRIDO`.
  - `soloDisponibles`: `true` o `false`.
  - `page`: Número de página (default: 1).
  - `limit`: Cantidad por página (default: 20).
- **Ejemplo de Consulta:** `GET /api/v1/libros?categoria=Inteligencia Artificial&page=1&limit=10`
- **Respuesta Exitosa (200 OK):**
```json
{
  "success": true,
  "data": {
    "total": 20,
    "pagina": 1,
    "limite": 10,
    "totalPaginas": 2,
    "libros": [
      {
        "id": 81,
        "titulo": "Feature Selection And Feature Extraction In Pattern Analysis",
        "autor": "Investigadores AI",
        "categoria": "Inteligencia Artificial",
        "isbn": "978-UNA-0081",
        "formato": "DIGITAL",
        "ejemplaresTotales": 1,
        "ejemplaresDisponibles": 1,
        "ubicacion": "Repositorio Digital UNAMAD",
        "archivoUrl": "/storage/libros/81_ia_Feature_Selection_and_Feature_Extraction_in.pdf",
        "archivoNombre": "81_ia_Feature_Selection_and_Feature_Extraction_in.pdf",
        "sha256": "4b9cb90728373b987b7a15998a632ffec8cfd3f94793f1d24c0d0246a482088d",
        "tamanoMb": "1.30",
        "portadaUrl": null,
        "resumen": "Recurso bibliográfico digital en formato PDF de alta calidad...",
        "year": "2024"
      }
    ]
  }
}
```

---

### 4.2. Obtener Detalle de Libro
- **Método y Ruta:** `GET /api/v1/libros/:id`
- **Acceso:** Público
- **Respuesta Exitosa (200 OK):** Incluye datos del libro y la lista de préstamos activos (si es físico).

---

### 4.3. Registrar Libro Digital con Archivo PDF
Permite subir un PDF físico. El backend **no guarda el archivo en la base de datos**, sino que lo almacena en `storage/libros/`, calcula automáticamente su **SHA-256** y **tamaño en MB**, y guarda únicamente la referencia en PostgreSQL.

- **Método y Ruta:** `POST /api/v1/libros/upload`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Tipo de Contenido:** `multipart/form-data`
- **Campos del Formulario:**
  - `archivo`: Archivo binario en formato `.pdf` (obligatorio, máx 50 MB).
  - `titulo`: Título oficial del libro (texto).
  - `autor`: Autor o autores (texto).
  - `categoria`: Área o carrera académica (texto).
  - `isbn`: ISBN o código bibliográfico único (texto).
  - `ejemplaresTotales`: Cantidad (número, default: 1).
  - `ubicacion`: Ubicación o servidor (texto, opcional).
  - `resumen`: Descripción del contenido (texto, opcional).
  - `year`: Año de publicación (texto, opcional).
- **Respuesta Exitosa (201 Created):**
```json
{
  "success": true,
  "message": "Libro digital y metadatos registrados exitosamente",
  "data": {
    "id": 101,
    "titulo": "Arquitectura de Software Distribuida",
    "autor": "Martin Fowler",
    "categoria": "Ingeniería de Sistemas",
    "isbn": "978-UNA-0101",
    "formato": "DIGITAL",
    "archivoUrl": "/storage/libros/1729864200000_arquitectura_distribuida.pdf",
    "archivoNombre": "1729864200000_arquitectura_distribuida.pdf",
    "sha256": "a3f5b7218d89e4726e...",
    "tamanoMb": "4.25",
    "ubicacion": "Repositorio Digital UNAMAD"
  }
}
```

---

### 4.4. Registrar Libro Físico (Manual)
- **Método y Ruta:** `POST /api/v1/libros/manual`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Cuerpo (`application/json`):**
```json
{
  "titulo": "Cálculo con Geometría Analítica",
  "autor": "Earl W. Swokowski",
  "categoria": "Ciencias Básicas",
  "isbn": "978-968-7277-83-7",
  "formato": "FISICO",
  "ejemplaresTotales": 6,
  "ejemplaresDisponibles": 6,
  "ubicacion": "Estante C-04",
  "resumen": "Manual de cálculo diferencial e integral."
}
```

---

### 4.5. Actualizar Libro
- **Método y Ruta:** `PUT /api/v1/libros/:id`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Cuerpo:** Campos a modificar (`titulo`, `autor`, `categoria`, `ejemplaresDisponibles`, `ubicacion`, etc.).

---

### 4.6. Eliminar Libro
- **Método y Ruta:** `DELETE /api/v1/libros/:id`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Validación:** Si el libro tiene ejemplares físicos prestados a lectores en el momento, el backend **rechaza la eliminación con error 400** para proteger el inventario.

---

### 4.7. Listar Categorías
- **Método y Ruta:** `GET /api/v1/libros/categorias`
- **Acceso:** Público
- **Respuesta:** Array con los nombres únicos de categorías registradas en la universidad.

---

## 5. Solicitudes de Préstamo Físico (`/api/v1/solicitudes`)

### 5.1. Estudiante Solicita Préstamo
- **Método y Ruta:** `POST /api/v1/solicitudes`
- **Acceso:** Protegido (Solo rol `ESTUDIANTE`)
- **Cuerpo de la Petición:**
```json
{
  "libroId": 1,
  "diasSolicitados": 7,
  "notas": "Requiero el libro para el proyecto de tesis final."
}
```
> **Candados de Solvencia Integrados:**
> 1. Si el estudiante tiene **multas pendientes de pago**, la API responde `403 Forbidden` bloqueando la solicitud.
> 2. Si tiene **préstamos vencidos no entregados**, responde `403 Forbidden`.
> 3. Si el libro no tiene stock disponible (`ejemplaresDisponibles <= 0`), responde `400 Bad Request`.

- **Respuesta Exitosa (201 Created):**
```json
{
  "success": true,
  "message": "Solicitud de préstamo registrada. Será evaluada por el bibliotecario.",
  "data": {
    "id": 12,
    "usuarioId": 3,
    "libroId": 1,
    "codigoEstudiante": "20230451",
    "nombreEstudiante": "Jhordy Pérez",
    "carreraEstudiante": "Ingeniería de Sistemas e Informática",
    "telefonoEstudiante": "982123456",
    "estado": "PENDIENTE",
    "diasSolicitados": 7,
    "fechaSolicitud": "2026-10-28T11:00:00.000Z"
  }
}
```

---

### 5.2. Mis Solicitudes (Estudiante)
- **Método y Ruta:** `GET /api/v1/solicitudes/mis-solicitudes`
- **Acceso:** Protegido (Solo rol `ESTUDIANTE`)
- **Respuesta:** Lista de solicitudes del alumno con insignias de estado (`PENDIENTE`, `APROBADA`, `RECHAZADA`).

---

### 5.3. Cancelar Solicitud Pendiente
- **Método y Ruta:** `DELETE /api/v1/solicitudes/:id/cancelar`
- **Acceso:** Protegido (Solo rol `ESTUDIANTE`)

---

### 5.4. Listar Solicitudes Generales (Bibliotecario)
- **Método y Ruta:** `GET /api/v1/solicitudes?estado=PENDIENTE`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)

---

### 5.5. Aprobar Solicitud
Al aprobar, el backend realiza una **transacción atómica en PostgreSQL**:
1. Cambia la solicitud a `APROBADA`.
2. Resta 1 ejemplar al inventario disponible del libro (`ejemplaresDisponibles = ejemplaresDisponibles - 1`).
3. Crea el registro formal en la tabla `prestamos` con estado `ACTIVO` y fecha límite calculada.

- **Método y Ruta:** `PUT /api/v1/solicitudes/:id/aprobar`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Respuesta Exitosa (200 OK):**
```json
{
  "success": true,
  "message": "Solicitud aprobada con éxito. Préstamo generado e inventario actualizado.",
  "data": {
    "solicitud": { "id": 12, "estado": "APROBADA" },
    "prestamo": {
      "id": 8,
      "nombreLector": "Jhordy Pérez",
      "codigoLector": "20230451",
      "fechaDevolucionEstimada": "2026-11-04T11:00:00.000Z",
      "estado": "ACTIVO"
    }
  }
}
```

---

### 5.6. Rechazar Solicitud
- **Método y Ruta:** `PUT /api/v1/solicitudes/:id/rechazar`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Cuerpo:**
```json
{
  "motivoRechazo": "Ejemplar apartado para consulta exclusiva en sala de lectura."
}
```

---

## 6. Préstamos y Circulación (`/api/v1/prestamos`)

### 6.1. Listar Préstamos
- **Método y Ruta:** `GET /api/v1/prestamos?estado=ACTIVO`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Valores posibles de `estado`:** `ACTIVO`, `DEVUELTO`, `VENCIDO`.

---

### 6.2. Mis Préstamos (Estudiante)
- **Método y Ruta:** `GET /api/v1/prestamos/mis-prestamos`
- **Acceso:** Protegido (Solo rol `ESTUDIANTE`)

---

### 6.3. Registro Directo de Préstamo en Mostrador
- **Método y Ruta:** `POST /api/v1/prestamos`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Cuerpo:**
```json
{
  "libroId": 3,
  "codigoLector": "20220120",
  "nombreLector": "Lucía Mendoza",
  "carreraOArea": "Ingeniería Forestal y Medio Ambiente",
  "telefonoLector": "974556677",
  "diasPrestamo": 7,
  "notas": "Préstamo en mostrador de ventanilla."
}
```

---

### 6.4. Registrar Devolución Física y Detección de Mora
Procesa la devolución del ejemplar:
1. Marca el préstamo como `DEVUELTO`.
2. Restituye el stock físico disponible en estantería (+1).
3. **Cálculo de mora institucional:** Si la fecha actual superó la fecha límite, calcula los días de retraso a razón de **S/. 1.50 por día** y crea la multa automáticamente en estado `PENDIENTE`.

- **Método y Ruta:** `PUT /api/v1/prestamos/:id/devolver`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Respuesta (Caso con retraso):**
```json
{
  "success": true,
  "message": "Devolución con 2 día(s) de mora. Se generó multa por S/. 3.00.",
  "data": {
    "prestamo": {
      "id": 2,
      "estado": "DEVUELTO",
      "fechaDevolucionReal": "2026-10-28T12:00:00.000Z"
    },
    "multa": {
      "id": 4,
      "codigoEstudiante": "20220120",
      "diasRetraso": 2,
      "montoTotal": 3.00,
      "estado": "PENDIENTE"
    }
  }
}
```

---

## 7. Multas y Solvencia (`/api/v1/multas`)

### 7.1. Consulta de Solvencia del Estudiante (Carnet Digital)
- **Método y Ruta:** `GET /api/v1/multas/mi-solvencia`
- **Acceso:** Protegido (Solo rol `ESTUDIANTE`)
- **Respuesta Exitosa (200 OK):**
```json
{
  "success": true,
  "data": {
    "esSolvente": false,
    "totalMultasPendientes": 1,
    "deudaTotal": 3.00,
    "multas": [
      {
        "id": 4,
        "tituloLibro": "Estructuras de Datos y Algoritmos en Java",
        "diasRetraso": 2,
        "montoTotal": "3.00",
        "estado": "PENDIENTE",
        "fechaGeneracion": "2026-10-28T12:00:00.000Z"
      }
    ],
    "mensaje": "Tienes S/. 3.00 en multas pendientes por retraso en devolución. Cancela en ventanilla para reactivar tu cuenta."
  }
}
```

---

### 7.2. Listar Multas Generales
- **Método y Ruta:** `GET /api/v1/multas?estado=PENDIENTE`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)

---

### 7.3. Cobrar Multa en Ventanilla
- **Método y Ruta:** `PUT /api/v1/multas/:id/cobrar`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Efecto:** Pasa la multa a estado `PAGADA`, registra la fecha de pago y libera la solvencia del alumno.

---

### 7.4. Condonar Multa Administrativamente
- **Método y Ruta:** `PUT /api/v1/multas/:id/condonar`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Cuerpo:**
```json
{
  "motivo": "Condonación autorizada por Resolución Directoral N° 045-2026-UNAMAD."
}
```

---

## 8. Reportes y Base de Datos de Circulación (`/api/v1/reportes`)

### 8.1. Métricas de Dashboard (KPIs en tiempo real)
- **Método y Ruta:** `GET /api/v1/reportes/dashboard`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Respuesta:**
```json
{
  "success": true,
  "data": {
    "totalTitulos": 103,
    "ejemplaresFisicos": {
      "totales": 15,
      "enEstante": 12,
      "enManosDeLectores": 3
    },
    "librosDigitales": 101,
    "prestamos": {
      "activos": 3,
      "vencidos": 1,
      "alDia": 2,
      "historicoDevueltos": 14
    },
    "solicitudesPendientes": 2,
    "multasPendientes": {
      "cantidad": 1,
      "montoTotal": 3.00
    }
  }
}
```

---

### 8.2. Base de Datos de Inventario con Lectores en Posesión
Proporciona la vista de base de datos que permite al bibliotecario auditar título por título qué alumnos tienen ejemplares físicos prestados en tiempo real.

- **Método y Ruta:** `GET /api/v1/reportes/inventario-lectores`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Respuesta:**
```json
{
  "success": true,
  "data": {
    "metricas": {
      "totalTitulos": 103,
      "totalEjemplaresFisicos": 15,
      "totalEnEstante": 12,
      "totalEnLectores": 3
    },
    "libros": [
      {
        "id": 1,
        "titulo": "Ingeniería de Software: Un Enfoque Práctico",
        "ejemplaresTotales": 5,
        "ejemplaresDisponibles": 4,
        "ejemplaresPrestados": 1,
        "enPosesionDeLectores": true,
        "lectoresActivos": [
          {
            "prestamoId": 1,
            "nombreLector": "Jhordy Pérez",
            "codigoLector": "20230451",
            "carrera": "Ingeniería de Sistemas e Informática",
            "telefono": "982123456",
            "fechaDevolucionEstimada": "2026-11-04T10:00:00.000Z",
            "esVencido": false
          }
        ]
      }
    ],
    "lectoresEnPosesion": [
      {
        "prestamoId": 1,
        "nombreLector": "Jhordy Pérez",
        "codigoLector": "20230451",
        "carrera": "Ingeniería de Sistemas e Informática",
        "libroId": 1,
        "tituloLibro": "Ingeniería de Software: Un Enfoque Práctico",
        "esVencido": false
      }
    ]
  }
}
```

---

### 8.3. Reporte Institucional UNAMAD
- **Método y Ruta:** `GET /api/v1/reportes/institucional`
- **Acceso:** Protegido (`BIBLIOTECARIO` o `ADMIN`)
- **Respuesta:** Tasa porcentual de devolución a tiempo, total histórico de préstamos, multas recaudadas vs condonadas, ranking Top 5 de libros más leídos y porcentaje de lectura por escuela profesional.

---

## 9. Servicio de Archivos Estáticos (PDFs)

### 9.1. Descarga o Streaming del PDF
- **Método y Ruta:** `GET /storage/libros/:filename`
- **Acceso:** Público (o protegido por proxy inverso en Nginx)
- **Ejemplo:** `http://localhost:3000/storage/libros/01_lit_Prohibido_Enamorarse.pdf`
- **Cabeceras Retornadas:**
  - `Content-Type: application/pdf`
  - `Content-Disposition: inline` (permite visualización directa en el lector móvil in-app)
  - `Cache-Control: public, max-age=604800, immutable` (optimización de caché en red móvil)
