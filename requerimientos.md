# Especificación de Requerimientos de Software (ERS)
## Sistema Móvil de Gestión Bibliotecaria — Biblioteca Central UNAMAD

**Institución:** Universidad Nacional Amazónica de Madre de Dios (UNAMAD)  
**Proyecto:** Aplicación Móvil de Biblioteca Universitaria y Repositorio Digital  
**Versión del Sistema:** 2.0.0 (Producción / Room Database v4)  
**Fecha de Publicación:** Octubre 2026  
**Estado:** Implementado, compilado y verificado en entorno Android  

---

## 1. Introducción y Objetivos del Sistema

### 1.1. Propósito
El propósito del presente documento es definir, formalizar y detallar la totalidad de los requerimientos funcionales, no funcionales y reglas de negocio implementadas en la aplicación móvil de la **Biblioteca Central de la UNAMAD**. 

### 1.2. Alcance
La aplicación móvil opera como una plataforma integral de doble propósito:
1. **Para la Comunidad Estudiantil e Investigadores:** Proporciona un canal digital moderno y accesible para consultar el catálogo bibliográfico físico y digital, solicitar préstamos de ejemplares en estante, consultar la solvencia académica mediante un carnet digital universitario y leer/descargar documentos en formato PDF de manera segura.
2. **Para el Personal Bibliotecario y Administrativo:** Funciona como un sistema centralizado de control de circulación física, autorización de solicitudes en ventanilla, gestión de cobranza o condonación de multas por mora, visualización de inventario tipo base de datos en tiempo real (identificando nominalmente qué alumnos tienen libros en su poder) y emisión de reportes estadísticos de uso por carrera profesional.

### 1.3. Definiciones y Acrónimos
- **UNAMAD:** Universidad Nacional Amazónica de Madre de Dios.
- **RBAC:** Role-Based Access Control (Control de Acceso Basado en Roles).
- **Mora:** Penalización económica aplicada por cada día calendario de retraso respecto a la fecha límite fijada para la devolución de un ejemplar físico.
- **Solvencia Bibliotecaria:** Estado administrativo de un lector que certifica la ausencia de multas económicas impagas y préstamos vencidos, habilitándolo para nuevos trámites académicos.
- **Acervo Bibliográfico:** Totalidad de recursos bibliográficos (libros físicos, digitales e híbridos) custodiados o administrados por la universidad.
- **SSOT:** Single Source of Truth (Fuente Única de Verdad).

---

## 2. Actores y Perfiles de Usuario

| Actor | Descripción | Responsabilidad Principal |
|---|---|---|
| **Estudiante / Lector** | Alumno de pregrado, posgrado o egresado con código universitario institucional vigente. | Consultar catálogo, solicitar préstamos físicos, leer libros digitales, monitorear sus solicitudes y revisar su carnet de lector. |
| **Bibliotecario / Administrador** | Personal encargado de la custodia y circulación en sala y domicilio de la Biblioteca Central. | Revisar y dictaminar solicitudes, registrar devoluciones físicas, gestionar cobranza/condonación de multas, administrar el catálogo y supervisar métricas. |
| **Sistema Local (App Engine)** | Motor reactivo local basado en Room y Kotlin Coroutines. | Calcular días de mora, auditar inventario, persistir estados y emitir alertas reactivas. |

---

## 3. Matriz de Control de Acceso (RBAC)

| Capacidad / Caso de Uso | Estudiante | Bibliotecario / Admin |
|---|:---:|:---:|
| Inicio de sesión institucional por código y contraseña | ✅ | ✅ |
| Conmutación rápida mediante selector demo institucional | ✅ | ✅ |
| Búsqueda de libros por título, autor, categoría o ISBN | ✅ | ✅ |
| Filtrado por formato (Todos, Físicos, Digitales, Disponibles) | ✅ | ✅ |
| Lectura de documentos digitales in-app (Visor Multi-capítulo) | ✅ | ✅ |
| Descarga offline sanitizada de archivos PDF | ✅ | ✅ |
| Emisión de Solicitud de Préstamo de libro físico | ✅ | ❌ *(No aplica)* |
| Visualización de "Mis Solicitudes" con insignias de estado | ✅ *(Solo propias)* | ❌ *(Ver todas)* |
| Cancelación de solicitudes pendientes | ✅ *(Solo propias)* | ❌ |
| Bandeja de Gestión de Solicitudes (Aprobar / Rechazar) | ❌ | ✅ |
| Registro de justificación oficial ante rechazo de solicitud | ❌ | ✅ |
| Registro manual directo de préstamos en ventanilla | ❌ | ✅ |
| Registro de devolución física y restitución de stock | ❌ | ✅ |
| Cálculo automático de multas por mora (S/. 1.50/día) | Automático | Automático |
| Consulta de solvencia y deuda acumulada | ✅ *(En Carnet)* | ✅ *(En Dashboard)* |
| Cobro de multas en ventanilla de biblioteca | ❌ | ✅ |
| Condonación justificada de multas | ❌ | ✅ |
| Alta, edición y baja de libros en el catálogo (CRUD) | ❌ | ✅ |
| Visualización del Panel "Base de Datos de Inventario" | ❌ | ✅ |
| Despliegue de lectores nominales en posesión de libros | ❌ | ✅ |
| Consulta del Reporte Institucional con KPIs y Gráficas | ❌ | ✅ |
| Recarga de datos semilla de demostración | ❌ | ✅ |

---

## 4. Requerimientos Funcionales (RF)

### 4.1. Módulo de Autenticación y Sesiones
- **RF-01.1 — Inicio de Sesión Institucional:** El sistema debe autenticar al usuario mediante su código universitario/ID y su contraseña frente a la tabla local `usuarios`.
- **RF-01.2 — Selector Rápido de Prueba (Demo):** La pantalla de login debe incluir un acceso táctil directo para las cuentas institucionales precargadas:
  - Estudiante 1: Jhordy Pérez Ramos (`2023-04510`, Sistemas).
  - Estudiante 2: Lucía Mendoza Ríos (`2022-01205`, Forestal).
  - Bibliotecario: Prof. Ronald Vásquez Arce (`BIBLIO-01`, Administración).
- **RF-01.3 — Persistencia de Sesión:** El sistema debe registrar el identificador y rol del usuario logueado en `SessionManager` (utilizando `SharedPreferences`), manteniendo la sesión activa al reiniciar la aplicación hasta que el usuario decida salir explícitamente.
- **RF-01.4 — Cierre de Sesión:** La barra superior debe disponer de una acción de salida protegida mediante un diálogo de confirmación `AlertDialog`.

### 4.2. Módulo de Catálogo Bibliográfico
- **RF-02.1 — Visualización de Acervo:** El sistema debe presentar los 7 títulos semilla oficiales de la UNAMAD, indicando título, autor, categoría académica, ubicación física (estante) o repositorio digital, formato e inventario disponible.
- **RF-02.2 — Carga Asíncrona de Portadas:** Cada tarjeta de libro debe cargar su portada oficial en formato WebP local (`portada_*.webp`), contando con respaldo de carga remota mediante Coil (`io.coil-kt:coil-compose`).
- **RF-02.3 — Motor de Búsqueda Reactivo:** Debe permitir la búsqueda en tiempo real por coincidencia parcial de texto en los campos título, autor, categoría académica e ISBN, insensible a mayúsculas, minúsculas y tildes (`unaccent()`).
- **RF-02.4 — Filtros Dinámicos:** El catálogo debe contar con filtros inmediatos mediante `FilterChip`: *Todos los formatos*, *Físicos*, *Digitales* y *Solo disponibles*.
- **RF-02.5 — Gestión CRUD (Solo Bibliotecario):** El bibliotecario debe poder registrar nuevos libros, modificar ejemplares o ubicaciones, y eliminar títulos mediante el diálogo `RegistroLibroDialog`.

### 4.3. Módulo de Solicitudes de Préstamo
- **RF-03.1 — Emisión de Solicitud:** Un estudiante autenticado debe poder pulsar "Solicitar Préstamo" en libros de formato físico o híbrido que cuenten con ejemplares disponibles.
- **RF-03.2 — Parámetros de Solicitud:** El diálogo `SolicitarPrestamoDialog` debe precargar los datos del estudiante (nombre, código, carrera) y permitir seleccionar el plazo deseado: 3 días, 7 días o 14 días calendario, además de una nota opcional.
- **RF-03.3 — Estados de Solicitud:** La solicitud debe crearse con estado `PENDIENTE`. Sus estados posteriores podrán ser: `APROBADA`, `RECHAZADA` o `CANCELADA`.
- **RF-03.4 — Seguimiento para el Estudiante:** La sección *Mis Préstamos* del estudiante debe contar con la pestaña *Solicitudes*, mostrando insignias visuales de estado:
  - `⏳ En Revisión` (con opción de cancelación por parte del alumno).
  - `✅ Aprobada` (con indicación de retiro en mostrador central).
  - `❌ Rechazada` (mostrando el motivo redactado por el bibliotecario).

### 4.4. Módulo de Dictamen y Control de Circulación
- **RF-04.1 — Bandeja de Solicitudes (Admin):** La pantalla `GestionSolicitudesScreen` debe listar las solicitudes pendientes ordenadas cronológicamente, con insignias de alerta numérica en la barra de navegación inferior.
- **RF-04.2 — Aprobación de Solicitud:** Al pulsar "Aprobar", el sistema debe:
  - Cambiar el estado de la solicitud a `APROBADA`.
  - Crear de forma atómica un registro en la tabla `prestamos` con estado `ACTIVO` y fecha límite calculada según los días aprobados.
  - Decrementar en 1 el contador `ejemplaresDisponibles` del libro en la tabla `libros`.
- **RF-04.3 — Rechazo de Solicitud:** Al pulsar "Rechazar", debe desplegarse el diálogo `RechazoSolicitudDialog` obligando a ingresar el motivo oficial del rechazo para conocimiento del alumno.
- **RF-04.4 — Registro de Préstamo Directo:** El bibliotecario debe poder registrar préstamos físicos presenciales en mostrador mediante `NuevoPrestamoDialog`.
- **RF-04.5 — Registro de Devolución:** Al devolver un ejemplar físico, el sistema debe:
  - Cambiar el estado del préstamo a `DEVUELTO` y guardar la fecha y hora real de entrega.
  - Incrementar en 1 el stock `ejemplaresDisponibles` del libro.
  - Evaluar si la devolución superó la fecha límite estipulada.

### 4.5. Módulo de Multas y Mora
- **RF-05.1 — Detección Automática de Retraso:** Si al devolver un libro la fecha actual supera a `fechaDevolucionEstimada`, el sistema debe calcular la diferencia en días naturales transcurridos.
- **RF-05.2 — Generación de Multa:** Se debe crear automáticamente una entidad en la tabla `multas` con:
  - `diasRetraso`: Número de días naturales de atraso.
  - `montoTotal`: Días de retraso multiplicados por la tarifa de **S/. 1.50**.
  - `estado`: `PENDIENTE`.
  - `observaciones`: Detalle del atraso y ejemplar comprometido.
- **RF-05.3 — Cobranza en Ventanilla:** En el diálogo `GestionMultasDialog`, el bibliotecario puede marcar una multa como `PAGADA`, registrando la fecha de pago y liberando la cuenta del estudiante.
- **RF-05.4 — Condonación Administrativa:** El bibliotecario puede condonar una multa pendiente cambiando su estado a `CONDONADA`, registrando el fundamento o resolución que autoriza la dispensa.

### 4.6. Módulo de Carnet Digital y Solvencia
- **RF-06.1 — Carnet Digital UNAMAD:** La pantalla `PerfilEstudianteScreen` debe renderizar una tarjeta virtual con diseño institucional (verde amazónico, escudo oficial de la UNAMAD, avatar circular con iniciales, nombre, código y carrera profesional).
- **RF-06.2 — Semáforo de Solvencia:**
  - Si el estudiante no registra multas pendientes ni libros vencidos: Se muestra una tarjeta verde esmeralda con el mensaje `Sin Penalizaciones ni Deudas — Lector Activo y Solvente`.
  - Si el estudiante registra multas impagas: Se muestra una alerta roja destacando el monto adeudado en soles (S/.) y la recomendación de pago en ventanilla.
  - Si tiene préstamos vencidos en curso: Se muestra una advertencia ámbar solicitando la devolución inmediata antes de generar nuevas sanciones acumulativas.
- **RF-06.3 — Estadísticas Personales:** Conteo reactivo de libros en posesión activa y total histórico devuelto con éxito.

### 4.7. Módulo de Lectura Digital y Descargas
- **RF-07.1 — Visor Digital In-App (`DigitalViewerDialog`):** El visor debe permitir la lectura multi-capítulo continua mediante scroll, evitando pantallas en blanco y ofreciendo:
  - Selector de ambientación visual: *Papel*, *Sepia* y *Noche*.
  - Modificador de tamaño de tipografía: *A-* (13sp), *A* (15sp), *A+* (18sp).
  - Encabezado con miniatura oficial del libro, autor y carrera.
- **RF-07.2 — Descarga de PDF Sanitizada:** El botón "Descargar PDF" debe:
  - Sanitizar el nombre del archivo eliminando caracteres conflictivos del sistema de archivos (`:`, `/`, `\`, `?`, `*`, `"`).
  - Intentar la descarga mediante el `DownloadManager` de Android para URLs HTTP/HTTPS externas.
  - Ejecutar un generador local de respaldo mediante `android.graphics.pdf.PdfDocument` ante URLs locales o de demostración, almacenando el archivo íntegro directamente en la carpeta estándar `/sdcard/Download/` de Android.
  - Notificar el progreso y la finalización mediante `Toast` y `Snackbar`.

### 4.8. Módulo de Base de Datos e Inventario en Circulación
- **RF-08.1 — Panel Administrativo de Base de Datos (`PanelInventarioDatabaseScreen`):** Panel en pantalla completa que presenta el acervo desde la perspectiva de auditoría de circulación:
  - Cabecera con 4 métricas consolidadas: *Títulos*, *Total Físicos*, *En Estante* y *En Lectores*.
  - Buscador global por título, autor, ISBN, nombre del estudiante o código.
- **RF-08.2 — Pestaña "Registro Libros":** Listado detallado de títulos con tarjetas expandibles. Al pulsar una tarjeta, se despliegan nominalmente los estudiantes que tienen ejemplares físicos de dicho título en su poder, su código, carrera, fecha de vencimiento y botón para registrar la devolución directa.
- **RF-08.3 — Pestaña "Lectores con Libros en Posesión":** Listado de todos los préstamos activos de la universidad agrupados por alumno, con badge de estado (*Al Día* o *Vencido*), datos de contacto telefónico y botón directo *Registrar Devolución y Liberar Stock*.

### 4.9. Módulo de Métricas y Reportes Institucionales
- **RF-09.1 — Diálogo de Reportes (`ReportesDialog`):** Dashboard analítico con los siguientes indicadores institucionales:
  - *Tasa de Devolución a Tiempo* (porcentaje sobre préstamos históricos).
  - *Total Circulación* (acumulado histórico de préstamos concedidos).
  - *Total Recaudado por Multas* (S/.).
  - *Total Multas Condonadas* (S/.).
- **RF-09.2 — Ranking de Libros Más Prestados:** Top 5 con barras de progreso relativas (`LinearProgressIndicator`).
- **RF-09.3 — Distribución por Carrera:** Desglose porcentual y numérico de préstamos por escuela académica profesional (ej. Ingeniería de Sistemas e Informática, Ingeniería Forestal y Medio Ambiente, etc.).

---

## 5. Requerimientos No Funcionales (RNF)

### 5.1. Rendimiento y Eficiencia
- **RNF-01.1 — Tiempo de Respuesta:** Las transacciones de aprobación, devolución y cobro en base de datos local deben ejecutarse en un tiempo inferior a 150 milisegundos.
- **RNF-01.2 — Fluidez Gráfica:** La interfaz desarrollada en Jetpack Compose debe mantener una tasa de refresco constante de 60 cuadros por segundo (FPS) en listas con desplazamiento.
- **RNF-01.3 — Tamaño y Optimización de Gráficos:** Las portadas deben estar codificadas en WebP con compresión optimizada, manteniendo un peso menor a 150 KB por imagen sin pérdida perceptible de nitidez.

### 5.2. Disponibilidad y Operación Offline
- **RNF-02.1 — Independencia de Red:** La aplicación debe funcionar al 100% en modo local offline sin requerir conexión a internet activa para consultas de catálogo, registro de préstamos, cobranza de multas y lectura digital.
- **RNF-02.2 — Persistencia ACID:** La base de datos local Room debe asegurar la integridad transaccional frente a cierres inesperados de la aplicación o apagados del dispositivo.

### 5.3. Usabilidad y Ergonomía Visual
- **RNF-03.1 — Soporte de Modo Oscuro:** La paleta de colores institucional (`DarkColorScheme`) debe asegurar un ratio de contraste mínimo de 4.5:1 (conforme a WCAG AA/AAA), eliminando textos negros u oscuros sobre fondos oscuros o tarjetas `surfaceVariant`.
- **RNF-03.2 — Identidad Institucional:** El diseño debe reflejar la paleta corporativa de la UNAMAD (Verde Amazónico `#1B5E20`, Verde Primario `#2E7D32`, Acento Ámbar `#F59E0B` y fondo de bosque profundo `#0D1B12`).
- **RNF-03.3 — Diseño Adaptativo:** Los componentes deben adaptarse fluidamente a pantallas de teléfonos estándar (desde 5.0 hasta 6.8 pulgadas).

### 5.4. Seguridad
- **RNF-04.1 — Segregación de Privilegios:** Ningún componente UI de administración debe ser instanciado o accesible cuando la sesión activa corresponda al rol `ESTUDIANTE`.
- **RNF-04.2 — Sanitización de Entradas:** Todas las rutas del sistema de archivos deben ser sanitizadas antes de llamar a las APIs de almacenamiento de Android.

---

## 6. Reglas de Negocio Institucionales (RN)

- **RN-01 — Límite de Disponibilidad Física:** No es posible aprobar una solicitud ni registrar un préstamo físico de un libro cuyo stock `ejemplaresDisponibles` sea igual a 0.
- **RN-02 — Acceso Ilimitado a Contenido Digital:** Los libros catalogados como `DIGITAL` no consumen stock físico y se encuentran disponibles para lectura y descarga sin restricción de ejemplares.
- **RN-03 — Tarifa de Mora Institucional:** Todo préstamo físico no entregado en su fecha límite devengará una penalización fija e improrrogable de **S/. 1.50 (Un sol con cincuenta céntimos) por día natural de atraso**, generada automáticamente en el momento de la recepción física.
- **RN-04 — Justificación de Condonación:** La condonación de una multa requiere obligatoriamente registrar un motivo o número de documento administrativo en el campo de observaciones.
- **RN-05 — Unicidad de Código Universitario:** Todo usuario registrado en el sistema debe poseer un código universitario único irrepetible.
