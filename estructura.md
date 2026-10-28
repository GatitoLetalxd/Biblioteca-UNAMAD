# Estructura del Proyecto, Paquetes y Recursos
## Sistema Móvil Biblioteca UNAMAD

**Institución:** Universidad Nacional Amazónica de Madre de Dios (UNAMAD)  
**Proyecto:** Aplicación Móvil de Biblioteca Universitaria y Repositorio Digital  
**Versión de Plataforma:** Android SDK (Compile: 36, Min: 26, Target: 36)  
**Lenguaje:** Kotlin 2.2.10 + Jetpack Compose con Material 3  

---

## 1. Árbol de Directorios del Proyecto

```text
Biblioteca UNAMAD/
├── .gradle/                                    # Caché interno de Gradle
├── .idea/                                      # Metadatos del entorno de desarrollo Android Studio
├── app/                                        # Módulo principal de la aplicación Android (Kotlin + Jetpack Compose)
│   ├── build.gradle.kts                        # Configuración de compilación, plugins y dependencias del módulo
│   ├── proguard-rules.pro                      # Reglas de ofuscación y optimización ProGuard/R8
│   └── src/
│       ├── test/                               # Suite de pruebas unitarias locales (JVM)
│       │   └── java/com/example/biblioteca/
│       │       └── ExampleUnitTest.kt          # Tests unitarios estándar de verificación
│       └── main/
│           ├── AndroidManifest.xml             # Manifiesto de Android (permisos de almacenamiento, Application, MainActivity)
│           ├── java/com/example/biblioteca/    # Código fuente Kotlin (Arquitectura MVVM + Clean Architecture)
│           │   ├── BibliotecaApplication.kt    # Clase Application: contenedor de inyección manual de dependencias
│           │   ├── MainActivity.kt             # Entrada principal con configuración Edge-to-Edge y llamada a BibliotecaApp
│           │   ├── data/                       # Capa de Acceso a Datos
│           │   │   ├── local/                  # Persistencia en base de datos local SQLite con Room
│           │   │   │   ├── BibliotecaDatabase.kt # Definición de RoomDatabase v4, migraciones y precarga inicial (seed data)
│           │   │   │   ├── dao/
│           │   │   │   │   └── Daos.kt         # Interfaces DAO (LibroDao, PrestamoDao, UsuarioDao, SolicitudDao, MultaDao)
│           │   │   │   └── entity/
│           │   │   │       └── Entities.kt     # Entidades Room con llaves primarias, foráneas e índices relacionales
│           │   │   └── repository/
│           │   │       ├── BibliotecaRepository.kt # Repositorio central: mediador entre DAOs, lógica de negocio y sesión
│           │   │       └── SessionManager.kt   # Gestor de persistencia de sesión local (SharedPreferences)
│           │   ├── model/
│           │   │   └── Models.kt               # Modelos de dominio puros, Enums institucionales y DTOs de agregación
│           │   └── ui/                         # Capa de Presentación (Jetpack Compose)
│           │       ├── components/
│           │       │   └── CommonComponents.kt # Componentes gráficos reutilizables (BookCoverImage, BookCard, LoanCard, Badges)
│           │       ├── dialogs/
│           │       │   └── Dialogs.kt          # Diálogos modales y visores (Visor PDF, Cobro de Multas, Solicitud, Registro)
│           │       ├── navigation/
│           │       │   └── BibliotecaApp.kt    # Orquestador maestro de navegación dual, TopAppBar y BottomBar por rol
│           │       ├── screens/                # Vistas y pantallas completas de la aplicación
│           │       │   ├── CatalogoScreen.kt   # Catálogo bibliográfico con filtros, búsqueda y acciones por rol
│           │       │   ├── DashboardScreen.kt  # Panel de administración, KPIs institucionales y accesos rápidos
│           │       │   ├── GestionSolicitudesScreen.kt # Bandeja de revisión, aprobación y rechazo de solicitudes
│           │       │   ├── LoginScreen.kt      # Pantalla de acceso institucional con accesos rápidos demo
│           │       │   ├── PanelInventarioDatabaseScreen.kt # Base de datos e inventario con lectores nominales en posesión
│           │       │   ├── PerfilEstudianteScreen.kt # Carnet digital del estudiante, semáforo de solvencia y reglas
│           │       │   └── PrestamosScreen.kt  # Gestión y consulta de préstamos activos e histórico
│           │       ├── theme/                  # Sistema de diseño y tokens visuales institucionales
│           │       │   ├── Color.kt            # Paleta de colores oficial UNAMAD (Verde Amazónico, Ámbar, Fondos)
│           │       │   ├── Theme.kt            # Configuración de MaterialTheme (LightColorScheme y DarkColorScheme adaptado)
│           │       │   └── Type.kt             # Tipografías y estilos de texto Material 3
│           │       └── viewmodel/
│           │           └── BibliotecaViewModel.kt # ViewModel unificado: StateFlows reactivos, validaciones y lógica de interfaz
│           └── res/                            # Recursos de la aplicación Android
│               ├── drawable/                   # Portadas oficiales WebP, escudo institucional y vectores XML
│               │   ├── logo_unamad.webp        # Escudo oficial de la UNAMAD optimizado en formato WebP
│               │   ├── portada_pressman.webp   # Portada: Ingeniería de Software (Pressman)
│               │   ├── portada_ecosistemas.webp # Portada: Ecosistemas Amazónicos (Valdivia UNAMAD)
│               │   ├── portada_lafore.webp     # Portada: Estructuras de Datos en Java (Lafore)
│               │   ├── portada_korth.webp      # Portada: Bases de Datos Relacional (Silberschatz & Korth)
│               │   ├── portada_tanenbaum.webp  # Portada: Redes de Computadoras (Tanenbaum)
│               │   ├── portada_biodiversidad.webp # Portada: Biodiversidad y Fauna Silvestre (UNAMAD)
│               │   ├── portada_sampieri.webp   # Portada: Metodología de la Investigación (Sampieri)
│               │   ├── ic_launcher_background.xml # Fondo del icono de la aplicación
│               │   └── ic_launcher_foreground.xml # Primer plano del icono de la aplicación
│               ├── values/                     # Recursos XML de valores
│               │   ├── colors.xml              # Colores primitivos del sistema Android
│               │   ├── strings.xml             # Cadenas de texto institucional
│               │   └── themes.xml              # Tema base de la actividad Android
│               └── xml/
│                   ├── backup_rules.xml        # Reglas de respaldo de Android
│                   └── data_extraction_rules.xml # Reglas de extracción de datos de Android 12+
├── backend/                                    # Módulo Backend API REST (Node.js + TypeScript + Prisma + PostgreSQL)
│   ├── package.json                            # Dependencias del servidor (Express, Prisma, bcryptjs, jsonwebtoken, zod)
│   ├── tsconfig.json                           # Configuración del compilador TypeScript
│   ├── .env.example                            # Plantilla de variables de entorno para producción y desarrollo
│   ├── API.md                                  # Documentación exhaustiva y limpia de todos los endpoints de la API
│   ├── README.md                               # Guía completa de despliegue nativo en VPS (PM2 + Nginx + PostgreSQL)
│   ├── deploy.sh                               # Script bash automatizado de actualización para el VPS
│   ├── prisma/                                 # Capa de datos relacional
│   │   ├── schema.prisma                       # Modelos Prisma: Usuario, Libro, SolicitudPrestamo, Prestamo, Multa
│   │   ├── catalogo_100.json                   # Metadatos base de los 100 libros digitales
│   │   └── seed.ts                             # Script de siembra: usuarios demo y los 100 libros digitales
│   ├── storage/libros/                         # Almacén local de los 100 libros en PDF referenciados
│   └── src/                                    # Código fuente TypeScript
│       ├── app.ts                              # Servidor Express, CORS, estáticos y manejo global de errores
│       ├── server.ts                           # Entrada principal con apagado ordenado (graceful shutdown)
│       ├── config/                             # Validación de entorno (env.ts) y cliente singleton de Prisma
│       ├── controllers/                        # Controladores HTTP (Auth, Admin, Libros, Solicitudes, Prestamos, Multas, Reportes)
│       ├── middlewares/                        # Middlewares: JWT, RBAC por rol, subida con Multer y errores
│       ├── routes/                             # Enrutador modular (/api/v1/...)
│       ├── services/                           # Lógica de negocio (candado de solvencia, mora S/. 1.50/día, inventario)
│       └── utils/                              # Validadores de carreras UNAMAD, hashing y tokens JWT
├── libros_descargados/                         # Acervo de los 100 libros en PDF + SQL seed para PostgreSQL
├── scripts/                                    # Scripts Python de automatización y recolección bibliográfica
├── gradle/                                     # Wrapper de Gradle y catálogo de dependencias TOML
│   ├── libs.versions.toml                      # Catálogo centralizado de versiones y dependencias de Gradle
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties           # Gradle 9.0 distribution
├── build.gradle.kts                            # Configuración de Gradle a nivel de raíz del proyecto
├── settings.gradle.kts                         # Configuración de módulos y repositorios de dependencias
├── requerimientos.md                           # Especificación formal de requerimientos de software (ERS)
├── estructura.md                               # Este documento: desglose de archivos, paquetes y recursos
└── arquitectura.md                             # Arquitectura de software, modelo de datos y diagramas de flujo
```

---

## 2. Desglose Detallado de Paquetes y Responsabilidades

### 2.1. Paquete Raíz (`com.example.biblioteca`)
- **`BibliotecaApplication.kt`**: Subclase de `android.app.Application`. Instancia la base de datos Room (`BibliotecaDatabase.getDatabase(this)`) y el repositorio singleton (`BibliotecaRepository`), asegurando un ciclo de vida compartido sin fugas de memoria.
- **`MainActivity.kt`**: `ComponentActivity` única de la app. Habilita el modo `enableEdgeToEdge()` y delega el renderizado visual completo al composable `BibliotecaApp(viewModel)`.

---

### 2.2. Paquete de Datos (`com.example.biblioteca.data`)

#### `data.local.entity.Entities.kt`
Define las 5 tablas SQLite mapeadas mediante Room:
1. **`LibroEntity`**: Almacena el acervo bibliográfico (id, título, autor, categoría, isbn, descripción, formato `FISICO|DIGITAL|HIBRIDO`, stock total, stock disponible, ubicación física, portada local o remota, tamaño en MB, url digital).
2. **`PrestamoEntity`**: Registra los préstamos formales (id, relación con libro, código y nombre del lector, carrera o área de procedencia, teléfono de contacto, fecha de préstamo, fecha estimada de devolución, fecha real de entrega, estado `ACTIVO|DEVUELTO|VENCIDO`, notas).
3. **`UsuarioEntity`**: Usuarios autorizados del sistema (id, código único institucional, contraseña, nombre, apellidos, carrera/área, teléfono, correo institucional, rol `ESTUDIANTE|BIBLIOTECARIO`, estado activo).
4. **`SolicitudPrestamoEntity`**: Solicitudes de préstamo de libros físicos emitidas por estudiantes (id, usuarioId, libroId, código y nombre del alumno, carrera, teléfono, fecha de solicitud, estado `PENDIENTE|APROBADA|RECHAZADA|CANCELADA`, días solicitados, notas del alumno, fecha de respuesta, motivo de rechazo dictaminado).
5. **`MultaEntity`**: Sanciones económicas devengadas por mora (id, prestamoId, usuarioId, código del alumno, nombre, título del ejemplar comprometido, días de retraso calculados, monto total en soles, estado `PENDIENTE|PAGADA|CONDONADA`, fecha de generación, fecha de cobro/dispensa, observaciones).

#### `data.local.dao.Daos.kt`
Contiene los objetos de acceso a datos con operaciones asíncronas y reactivas (`Flow<T>`):
- **`LibroDao`**: Búsqueda reactiva por texto (`searchLibros`), filtros por formato, ordenamiento por popularidad, decremento e incremento atómico de stock físico en sala.
- **`PrestamoDao`**: Consultas relacionales `PrestamoWithLibroInfo` uniendo préstamos con títulos y portadas de libros, conteo de préstamos activos/vencidos/devueltos, y marcación de devoluciones físicas.
- **`UsuarioDao`**: Autenticación por código y contraseña, consulta de perfiles por código único, listado por rol.
- **`SolicitudPrestamoDao`**: Consultas relacionales `SolicitudWithLibroInfo`, conteo de solicitudes pendientes, actualización atómica de estado con fecha y motivo de rechazo.
- **`MultaDao`**: Listado de multas pendientes, cálculo reactivo de la sumatoria total adeudada (`sumMontoMultasPendientes`), actualización a `PAGADA` o `CONDONADA`.

#### `data.local.BibliotecaDatabase.kt`
- Hereda de `RoomDatabase`. Contiene la versión de esquema **4**.
- Declara las migraciones históricas `MIGRATION_1_2`, `MIGRATION_2_3` y `MIGRATION_3_4`.
- Incorpora el método estático `populateInitialData()` con los datos semilla oficiales: 7 libros oficiales de la UNAMAD, usuarios demo, préstamos activos y multas precalculadas.

#### `data.repository.SessionManager.kt`
- Encapsula `SharedPreferences` para almacenar la clave de sesión del usuario logueado (`KEY_USER_ID`, `KEY_USER_CODIGO`, `KEY_USER_ROL`).
- Provee métodos `saveSession()`, `getUserRol()`, `isLoggedIn()` y `clearSession()`.

#### `data.repository.BibliotecaRepository.kt`
- **Patrón Repositorio (Single Source of Truth)**.
- Expone flujos reactivos consolidados: `todosLosLibros`, `todosLosPrestamos`, `prestamosActivos`, `todasLasSolicitudes`, `multasPendientes`, `estadisticas`, `reporteInstitucional` e **`inventarioConLectores`** (que combina libros con préstamos activos para el panel de base de datos).
- Coordina transacciones críticas: `solicitarPrestamo()`, `aprobarSolicitud()`, `rechazarSolicitud()`, `registrarDevolucion()` (con generación automática de multas por mora), `cobrarMulta()` y `condonarMulta()`.

---

### 2.3. Paquete de Modelos (`com.example.biblioteca.model.Models.kt`)
Define las clases de datos independientes del framework de base de datos:
- **`Libro`**: Modelo de dominio de un título bibliográfico.
- **`Prestamo`**: Modelo de dominio de un préstamo de libro con estado calculado dinámicamente según la fecha actual.
- **`Usuario`**: Modelo de dominio del usuario con su rol.
- **`SolicitudPrestamo`**: Modelo de dominio de una solicitud física.
- **`Multa`**: Modelo de dominio de una penalización económica.
- **`LibroConCirculacion`**: DTO que empareja un `Libro` con su lista de `Prestamo` físicos en poder de alumnos para el panel de inventario tipo base de datos.
- **`BibliotecaStats`**: Indicadores globales para el Dashboard.
- **`ReporteInstitucional`**: DTO consolidado con tasa de devolución, recaudación, ranking de libros y desglose por carreras.
- **Enums**: `FormatoLibro`, `EstadoPrestamo`, `RolUsuario`, `EstadoSolicitud`, `EstadoMulta`.

---

### 2.4. Paquete de Componentes UI (`com.example.biblioteca.ui.components.CommonComponents.kt`)
- **`BookCoverImage`**: Resuelve imágenes WebP locales desde `@drawable/portada_*` por nombre de recurso, o las carga asíncronamente mediante `AsyncImage` de Coil para URLs web remotas, con fallback visual a icono temático.
- **`BookCard`**: Tarjeta de libro interactiva para el catálogo con badge de categoría, autores, indicador de disponibilidad física/digital, ubicación en estante y botones de acción según el rol del usuario (*Solicitar Préstamo* o *Prestar* / *Editar* / *Eliminar*).
- **`LoanCard`**: Tarjeta de préstamo activo o devuelto con badge de estado, datos del estudiante, fechas límite y acción de devolución.
- **Badges de Estado**: `StatusAvailableBadge`, `StatusLoanedBadge`, `StatusReservedBadge`, `OverdueBadge`.

---

### 2.5. Paquete de Diálogos (`com.example.biblioteca.ui.dialogs.Dialogs.kt`)
- **`SolicitarPrestamoDialog`**: Diálogo para estudiantes donde se define el plazo de préstamo (3, 7, 14 días) y notas adicionales.
- **`RechazoSolicitudDialog`**: Modal de administración para capturar obligatoriamente el motivo del rechazo de una solicitud.
- **`GestionMultasDialog`**: Panel modal de control de multas con pestañas *Pendientes* e *Historial*, saldo total por cobrar, botón *Cobrar* y modal para *Condonar* con registro de justificación.
- **`ReportesDialog`**: Presentación de métricas institucionales UNAMAD, tasa de retorno puntual, total de circulación, ranking Top 5 de libros más leídos y distribución porcentual de lectura por escuelas profesionales.
- **`DigitalViewerDialog`**: Visor de lectura digital multi-capítulo enriquecido, con cambio de ambientación visual (Papel, Sepia, Noche), selector de fuente (A-, A, A+) y botón para descarga de PDF a `/sdcard/Download/`.
- **`RegistroLibroDialog`**: Modal CRUD para registrar o modificar títulos del acervo.
- **`NuevoPrestamoDialog`**: Modal para registro manual presencial de préstamos en mostrador.

---

### 2.6. Paquete de Pantallas (`com.example.biblioteca.ui.screens`)
- **`LoginScreen.kt`**: Pantalla inicial de bienvenida con escudo oficial de la UNAMAD, formulario de acceso por código universitario y acceso rápido para cuentas demo.
- **`CatalogoScreen.kt`**: Vista del catálogo bibliográfico con barra de búsqueda reactiva, chips de filtrado rápido y listado de títulos.
- **`GestionSolicitudesScreen.kt`**: Bandeja para el bibliotecario con pestañas *Pendientes* e *Historial*, con acciones de aprobación directa y rechazo motivado.
- **`PrestamosScreen.kt`**: Para administradores muestra todos los préstamos en sala y domicilio con advertencias de vencimiento; para estudiantes muestra la vista dividida en *Solicitudes*, *En Préstamo* e *Historial*.
- **`PerfilEstudianteScreen.kt`**: Credencial virtual de lector universitario con escudo UNAMAD, número de libros activos y devueltos, semáforo de solvencia (*Sin Deudas* vs. *Sancionado*) y normativa de lectura.
- **`DashboardScreen.kt`**: Panel principal para el bibliotecario con métricas de acervo, ejemplares físicos disponibles, circulación activa, accesos al panel de base de datos, reporte institucional y recarga de datos demo.
- **`PanelInventarioDatabaseScreen.kt`**: Panel especializado tipo base de datos de libros físicos en circulación:
  - Fila superior de métricas: *Títulos*, *Total Físicos*, *En Estante* y *En Lectores*.
  - Pestaña *Registro Libros*: Tarjetas expandibles que revelan los alumnos en posesión de cada título con acción para devolver y liberar ejemplar.
  - Pestaña *Lectores con Libros*: Fichas de estudiantes con préstamos activos, estado (*Al Día* o *Vencido*), teléfono y botón *Registrar Devolución y Liberar Stock*.

---

### 2.7. Paquete de Navegación (`com.example.biblioteca.ui.navigation.BibliotecaApp.kt`)
- Enrutador reactivo que escucha el `currentUser` del `BibliotecaViewModel`.
- Si no hay usuario autenticado: Despliega `LoginScreen`.
- Si el usuario es `ESTUDIANTE`: Construye la interfaz con barra inferior de 3 pestañas (*Catálogo*, *Préstamos*, *Mi Perfil*) y TopAppBar con badge de alumno y logout.
- Si el usuario es `BIBLIOTECARIO`: Construye la interfaz administrativa con 4 pestañas (*Catálogo*, *Solicitudes*, *Préstamos*, *Dashboard*), FloatingActionButton para nuevo libro y TopAppBar con badge administrativo.

---

### 2.8. Paquete de Tema (`com.example.biblioteca.ui.theme`)
- **`Color.kt`**: Define los colores institucionales: `UnamadGreenPrimary` (`#1B5E20`), `UnamadGreenMedium` (`#2E7D32`), `UnamadGreenDark` (`#0D1B12`), `AmberAccent` (`#F59E0B`), y paletas de estado (`StatusAvailableBg`, `StatusAvailableText`, `StatusOutBg`, `StatusOutText`).
- **`Theme.kt`**: Define `LightColorScheme` y `DarkColorScheme`. En Modo Oscuro, garantiza `onPrimary = Color.White` y contrastes de texto para evitar texto ilegible sobre fondos oscuros.
- **`Type.kt`**: Configuración de `Typography` de Material 3.

---

### 2.9. Paquete de ViewModel (`com.example.biblioteca.ui.viewmodel.BibliotecaViewModel.kt`)
- Administra el estado completo de la UI mediante `StateFlow` reactivos:
  - `currentUser`: Usuario actualmente autenticado.
  - `catalogoUiState`: Libros filtrados, búsqueda activa, categorías.
  - `solicitudesUiState`: Solicitudes pendientes e históricas, conteo no leído.
  - `prestamosUiState`: Préstamos activos, vencidos e históricos.
  - `inventarioConLectores`: Flujo reactivo con libros y detalle de lectores en posesión.
  - `estadisticas` y `reporteInstitucional`: Datos analíticos del Dashboard.
  - Estados de visibilidad de modales y diálogos.
- Métodos de negocio ejecutados en `viewModelScope` con `Dispatchers.IO`.

---

## 3. Catálogo de Recursos Gráficos Locales (`res/drawable/`)

| Recurso | Formato | Resolución / Tamaño | Descripción |
|---|:---:|:---:|---|
| **`logo_unamad.webp`** | WebP | 512 × 512 px (~45 KB) | Escudo oficial de la Universidad Nacional Amazónica de Madre de Dios. |
| **`portada_pressman.webp`** | WebP | 320 × 480 px (~38 KB) | Portada: *Ingeniería de Software: Un Enfoque Práctico* (Roger S. Pressman). |
| **`portada_ecosistemas.webp`** | WebP | 320 × 480 px (~35 KB) | Portada: *Ecosistemas Amazónicos y Manejo Forestal* (Dr. Carlos Valdivia UNAMAD). |
| **`portada_lafore.webp`** | WebP | 320 × 480 px (~42 KB) | Portada: *Estructuras de Datos y Algoritmos en Java* (Robert Lafore). |
| **`portada_korth.webp`** | WebP | 320 × 480 px (~36 KB) | Portada: *Bases de Datos: Diseño e Implementación Relacional* (Silberschatz & Korth). |
| **`portada_tanenbaum.webp`** | WebP | 320 × 480 px (~40 KB) | Portada: *Redes de Computadoras y Telecomunicaciones* (Andrew S. Tanenbaum). |
| **`portada_biodiversidad.webp`** | WebP | 320 × 480 px (~39 KB) | Portada: *Biodiversidad y Fauna de Madre de Dios* (Fondo Editorial UNAMAD). |
| **`portada_sampieri.webp`** | WebP | 320 × 480 px (~37 KB) | Portada: *Metodología de la Investigación Científica* (Roberto Hernández Sampieri). |

---

## 4. Dependencias y Configuración de Compilación

### 4.1. Catálogo de Versiones (`gradle/libs.versions.toml`)

```toml
[versions]
agp = "9.4.1"
kotlin = "2.2.10"
ksp = "2.2.10-2.0.2"
composeBom = "2026.02.01"
room = "2.7.2"
navigationCompose = "2.8.8"
coil = "2.7.0"
lifecycleRuntimeKtx = "2.11.0"
activityCompose = "1.13.0"
coreKtx = "1.19.1"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"

[libraries]
# Jetpack Compose
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Persistencia Local (Room)
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Carga de Imágenes Asíncrona (Coil)
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }

# Lifecycle y Arquitectura
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }

# Pruebas Unitarias
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
```

### 4.2. Especificaciones de Plataforma (`app/build.gradle.kts`)
- **Plugin de Aplicación:** `com.android.application` (AGP 9.4.1).
- **Plugin de Kotlin:** `org.jetbrains.kotlin.plugin.compose` (Kotlin 2.2.10).
- **Procesador de Anotaciones:** `com.google.devtools.ksp` (KSP 2.2.10-2.0.2).
- **Compile SDK:** 36.
- **Min SDK:** 26 (Android 8.0 Oreo — asegura compatibilidad con APIs de fecha modernas y Room v4).
- **Target SDK:** 36.
- **Java Compatibility:** `JavaVersion.VERSION_21` con fallback a JVM target 24.
