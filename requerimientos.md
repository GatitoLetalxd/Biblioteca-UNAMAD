# Especificación de Requerimientos de Software (ERS)
## Sistema Integral de Biblioteca Central — UNAMAD (Monorepo)

**Institución:** Universidad Nacional Amazónica de Madre de Dios (UNAMAD)  
**Proyecto:** Sistema Bibliotecario Universitario (App Móvil Android + Backend API REST + Acervo Digital)  
**Versión del Sistema:** 2.5.0 (Monorepo / PostgreSQL 16 / Room Database v4)  
**Backend en Producción:** `https://biblioteca.moondev.online/api/v1/`  
**Almacén de PDFs:** `https://biblioteca.moondev.online/storage/libros/`  
**Fecha de Actualización:** Octubre 2026  
**Estado:** Implementado, desplegado en VPS, probado de extremo a extremo y versionado en GitHub  

---

## 1. Introducción y Objetivos del Sistema

### 1.1. Propósito
El propósito del presente documento es definir, formalizar y detallar la totalidad de los requerimientos funcionales, no funcionales y reglas de negocio del sistema de la **Biblioteca Central de la UNAMAD**, abarcando tanto la aplicación móvil nativa (Android) como el servidor backend API REST desplegado en el VPS institucional.

### 1.2. Alcance del Monorepo
El sistema opera bajo una arquitectura de monorepo integrada por:
1. **Aplicación Móvil Android (`app/`):** Canal digital para estudiantes y personal bibliotecario con interfaz moderna en Jetpack Compose, soporte 100% offline (Room v4), lector digital multi-capítulo y descarga de PDFs.
2. **Servidor Backend API REST (`backend/`):** API de alto rendimiento en Node.js, TypeScript y Prisma ORM conectada a PostgreSQL nativo, encargada del control de acceso, auditoría de circulación, cálculo de penalizaciones y entrega estática de PDFs.
3. **Acervo Digital de 100 Libros (`libros_descargados/` y `backend/storage/libros/`):** Colección de libros completos en PDF clasificados en Literatura, Cuentos, Ingeniería, Inteligencia Artificial y Medio Ambiente.

---

## 2. Actores y Perfiles de Usuario (3 Roles Estrictos)

| Actor | Rol en Sistema | Descripción y Atribuciones |
|---|:---:|---|
| **Estudiante** | `ESTUDIANTE` | Alumno universitario de pregrado o posgrado. Se autoregistra con su **código universitario de 8 dígitos** (sin DNI). Consulta catálogo, solicita préstamos físicos, revisa su solvencia y lee/descarga libros digitales. |
| **Bibliotecario** | `BIBLIOTECARIO` | Personal operativo de ventanilla creado por el Administrador. Dictamina solicitudes (aprueba/rechaza con motivo), registra préstamos y devoluciones físicas, cobra/condona multas y gestiona el acervo. |
| **Administrador** | `ADMIN` | Superusuario de la Dirección de Biblioteca. Posee todas las facultades del bibliotecario y la **atribución exclusiva de crear y gestionar cuentas de personal (`BIBLIOTECARIO` o `ADMIN`)**. |

---

## 3. Matriz de Control de Acceso Basado en Roles (RBAC)

| Capacidad / Caso de Uso | Estudiante | Bibliotecario | Administrador |
|---|:---:|:---:|:---:|
| Autoregistro público con código de 8 dígitos (sin DNI) | ✅ | ❌ | ❌ |
| Inicio de sesión institucional por código y contraseña | ✅ | ✅ | ✅ |
| Creación de personal administrativo (`BIBLIOTECARIO` / `ADMIN`) | ❌ | ❌ | ✅ |
| Búsqueda de libros por título, autor, categoría o ISBN | ✅ | ✅ | ✅ |
| Filtrado por formato (Todos, Físicos, Digitales, Disponibles) | ✅ | ✅ | ✅ |
| Lectura de documentos digitales in-app (Visor Multi-capítulo) | ✅ | ✅ | ✅ |
| Descarga directa de archivos PDF con verificación SHA-256 | ✅ | ✅ | ✅ |
| Emisión de solicitud de préstamo de libro físico | ✅ | ❌ | ❌ |
| Seguimiento de "Mis Solicitudes" (`⏳ En Revisión`, `✅ Aprobada`, `❌ Rechazada`) | ✅ *(Solo propias)* | ❌ | ❌ |
| Cancelación de solicitudes pendientes | ✅ *(Solo propias)* | ❌ | ❌ |
| Bandeja general de solicitudes (Aprobar / Rechazar con motivo) | ❌ | ✅ | ✅ |
| Registro de préstamo directo en mostrador | ❌ | ✅ | ✅ |
| Registro de devolución física y restitución de stock | ❌ | ✅ | ✅ |
| Cálculo automático de mora institucional (**S/. 1.50/día**) | Automático | Automático | Automático |
| Consulta de solvencia y deuda acumulada en Carnet Digital | ✅ *(En Carnet)* | ✅ *(En Dashboard)* | ✅ *(En Dashboard)* |
| Cobro de multas en ventanilla y emisión de recibo | ❌ | ✅ | ✅ |
| Condonación administrativa de multas con justificación | ❌ | ✅ | ✅ |
| Alta de libros digitales con subida de PDF (`multipart/form-data`) | ❌ | ✅ | ✅ |
| Alta de libros físicos, edición y baja de catálogo (CRUD) | ❌ | ✅ | ✅ |
| Panel de Base de Datos de Inventario con lectores en posesión | ❌ | ✅ | ✅ |
| Consulta del Dashboard y Reportes Institucionales UNAMAD | ❌ | ✅ | ✅ |
| Sincronización del catálogo móvil con el VPS en producción | ✅ | ✅ | ✅ |

---

## 4. Requerimientos Funcionales Detallados

### 4.1. Módulo de Autenticación, Registro y Usuarios
- **RF-01.1 — Autoregistro de Alumnos:** El sistema debe permitir el registro público de estudiantes mediante `POST /api/v1/auth/register` con los siguientes campos:
  - `codigo`: Exactamente 8 dígitos numéricos (validación regex `/^\d{8}$/`). **No se solicita DNI**.
  - `password`: Mínimo 6 caracteres (encriptado con `bcryptjs`).
  - `nombre`: Primer nombre.
  - `apellido`: Primer apellido.
  - `carrera`: Selección obligatoria dentro de las 12 carreras oficiales de la UNAMAD.
  - `telefono`: Teléfono móvil de contacto.
- **RF-01.2 — Asignación Automática de Rol y Correo:** Al registrarse un estudiante, el sistema asigna forzosamente `rol = "ESTUDIANTE"`, `activo = true` y correo institucional `{codigo}@unamad.edu.pe`.
- **RF-01.3 — Creación Exclusiva de Personal:** Solo los usuarios con rol `ADMIN` pueden acceder a `POST /api/v1/admin/usuarios` para crear cuentas con rol `BIBLIOTECARIO` o `ADMIN`. El sistema rechaza cualquier intento de crear estudiantes por este medio.
- **RF-01.4 — Inicio de Sesión y Token JWT:** El endpoint `POST /api/v1/auth/login` valida credenciales y retorna un token JWT firmado (vigencia de 7 días) junto a los datos del perfil.
- **RF-01.5 — Persistencia en App Móvil:** La app Android almacena el token JWT en `SessionManager` (`SharedPreferences`) y lo inyecta automáticamente en cada petición mediante `AuthInterceptor`.

### 4.2. Módulo de Catálogo Bibliográfico y Almacenamiento Referenciado
- **RF-02.1 — Cero Blobs en Base de Datos:** Los archivos PDF **no se almacenan en PostgreSQL**. Se guardan físicamente en `storage/libros/` del servidor y la base de datos almacena exclusivamente sus metadatos (`archivo_url`, `archivo_nombre`, `sha256`, `tamano_mb`).
- **RF-02.2 — Acervo Digital de 100 Libros:** El sistema cuenta con 100 libros completos en PDF pre-cargados y catalogados, organizados en:
  1. *Literatura y Filosofía* (20 libros).
  2. *Cuentos y Narrativa* (20 libros).
  3. *Ingeniería de Sistemas* (20 libros).
  4. *Inteligencia Artificial y Computación* (20 libros).
  5. *Ciencias Ambientales, Amazonía e Investigación* (20 libros).
  6. *Libros físicos oficiales de estante* (3 libros). Total: **103 libros**.
- **RF-02.3 — Subida de Libros Digitales:** El endpoint `POST /api/v1/libros/upload` recibe el archivo PDF vía `multer`, lo guarda en disco, calcula su hash SHA-256 y tamaño en MB, y genera el registro referenciado en PostgreSQL.
- **RF-02.4 — Entrega Estática de PDFs:** El servidor Nginx y Express exponen `/storage/libros/:filename` con cabeceras `Content-Type: application/pdf`, `Content-Disposition: inline` y caché HTTP de 7 días.
- **RF-02.5 — Resolución Híbrida de Portadas:** La app móvil resuelve las portadas evaluando:
  1. Recurso WebP local en `res/drawable/*.webp` (carga instantánea sin consumo de datos).
  2. URL remota en el servidor mediante Coil (`AsyncImage`) con caché en memoria y disco.
  3. Fallback visual con el escudo oficial de la UNAMAD.

### 4.3. Módulo de Solicitudes y Candado de Solvencia
- **RF-03.1 — Emisión de Solicitud:** Un estudiante puede solicitar un libro físico o híbrido disponible seleccionando un plazo de 3, 7 o 14 días calendario.
- **RF-03.2 — Candado de Solvencia Institucional:** El backend evalúa la cuenta del alumno antes de admitir la solicitud:
  - Si registra **multas pendientes de pago**, bloquea la petición con `403 Forbidden`.
  - Si registra **préstamos vencidos no entregados**, bloquea la petición con `403 Forbidden`.
- **RF-03.3 — Dictamen en Ventanilla:**
  - **Aprobación:** Cambia estado a `APROBADA`, crea el préstamo activo con fecha límite y descuenta 1 unidad al stock físico disponible.
  - **Rechazo:** Exige registrar obligatoriamente el motivo del rechazo para notificación del estudiante.

### 4.4. Módulo de Préstamos, Devoluciones y Mora
- **RF-04.1 — Préstamo Directo:** El bibliotecario puede registrar préstamos físicos presenciales en mostrador mediante `POST /api/v1/prestamos`.
- **RF-04.2 — Registro de Devolución:** El endpoint `PUT /api/v1/prestamos/:id/devolver` marca el préstamo como `DEVUELTO` y restituye el stock físico disponible (+1).
- **RF-04.3 — Cálculo Automático de Mora:** Si la fecha actual supera a `fechaDevolucionEstimada`:
  $$\text{Días de Retraso} = \max\left(1, \left\lfloor\frac{\text{Fecha Actual} - \text{Fecha Límite}}{86400000}\right\rfloor\right)$$
  $$\text{Monto Multa} = \text{Días de Retraso} \times \text{S/. } 1.50$$
  Se genera automáticamente una entidad en la tabla `multas` con estado `PENDIENTE`.

### 4.5. Módulo de Multas y Cobranzas
- **RF-05.1 — Consulta de Solvencia:** `GET /api/v1/multas/mi-solvencia` permite al estudiante verificar en su carnet digital si está solvente (`esSolvente: true`) o si tiene multas pendientes detallando el monto.
- **RF-05.2 — Cobro en Ventanilla:** `PUT /api/v1/multas/:id/cobrar` marca la multa como `PAGADA`, registra la fecha y hora de cobro y rehabilita la solvencia del alumno.
- **RF-05.3 — Condonación con Justificación:** `PUT /api/v1/multas/:id/condonar` marca la multa como `CONDONADA`, exigiendo registrar el fundamento o número de resolución administrativa.

### 4.6. Módulo de Base de Datos de Inventario y Reportes
- **RF-06.1 — Panel de Base de Datos (`GET /api/v1/reportes/inventario-lectores`):** Expone en tiempo real qué estudiantes tienen libros físicos en su poder, su código, carrera, teléfono, fecha límite y estado de vencimiento.
- **RF-06.2 — Dashboard de KPIs (`GET /api/v1/reportes/dashboard`):** Total de títulos, ejemplares en estante vs. en manos de alumnos, préstamos activos/vencidos y multas por cobrar.
- **RF-06.3 — Reporte Institucional (`GET /api/v1/reportes/institucional`):** Tasa porcentual de retorno a tiempo, histórico de préstamos, ranking Top 5 de libros más leídos y desglose por carrera.

---

## 5. Requerimientos No Funcionales (RNF)

- **RNF-01 — Arquitectura Offline-First:** La app Android debe mantener en funcionamiento la consulta de catálogo, lectura digital y carnet aunque se pierda la conexión a internet, utilizando Room v4 como fuente única de verdad sincronizada con el VPS.
- **RNF-02 — Eficiencia de Recursos en Servidor:** El backend en Node.js/TypeScript debe consumir menos de 150 MB de memoria RAM en el VPS y utilizar PM2 para autorestart y persistencia tras reinicios del sistema operativo.
- **RNF-03 — Seguridad en Comunicaciones:** Todas las transacciones deben viajar sobre HTTPS con certificados SSL emitidos por Let's Encrypt (Certbot) y contraseñas cifradas con `bcrypt` (factor 10).
- **RNF-04 — Accesibilidad y Modo Oscuro:** La app móvil debe cumplir el ratio de contraste WCAG AAA en tema oscuro y claro, garantizando textos legibles sobre cualquier superficie de tarjeta.
- **RNF-05 — Control de Versiones:** El proyecto completo debe mantenerse organizado como un Monorepo Git sincronizado con la rama `main` en GitHub.
