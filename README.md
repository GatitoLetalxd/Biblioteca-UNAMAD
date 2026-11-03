# Sistema Integral de Biblioteca Central — UNAMAD
### Universidad Nacional Amazónica de Madre de Dios

![Android](https://img.shields.io/badge/Android-Kotlin%202.2-green.svg?logo=android&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-blue.svg?logo=jetpackcompose&logoColor=white)
![Theme](https://img.shields.io/badge/Theme-Light%20%7C%20Obsidian%20Dark-amber.svg)
![Backend](https://img.shields.io/badge/Backend-Node.js%2020%20%7C%20TypeScript-informational.svg?logo=nodedotjs&logoColor=white)
![Database](https://img.shields.io/badge/Database-PostgreSQL%2016%20%7C%20Room%20v4-blue.svg?logo=postgresql&logoColor=white)
![ORM](https://img.shields.io/badge/ORM-Prisma%205-2D3748.svg?logo=prisma&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)

Monorepo institucional de alto rendimiento que integra la **aplicación móvil nativa Android**, el **backend API REST en Node.js/TypeScript**, persistencia relacional en **PostgreSQL**, base de datos local **Room (Offline-First)** y un **acervo digital de 100 libros completos en PDF** clasificados por facultades y disciplinas académicas.

---

## 1. Novedades y Sistema Visual Rediseñado

### Tema Claro y Modo Oscuro Obsidian Slate
- **Conmutador de Tema Persistente:** Permite alternar entre **Modo Claro**, **Modo Oscuro** y **Seguir al Sistema**, manteniendo la preferencia del usuario en `SharedPreferences` (`SessionManager`) incluso tras cerrar sesión.
- **Paleta Obsidian Minimalista (Cero Verde en Modo Oscuro):**
  - Fondo: Negro Obsidiana Profundo (`#0B0F17`).
  - Superficies y Tarjetas: Slate Carbón (`#121722` y `#1A2130`).
  - Acento Primario: Oro / Ámbar Cálido (`#FBBF24`), garantizando contraste superior a 11:1.
  - Tipografía: Blanco Puro (`#F8FAFC`, ratio > 16:1) y Slate 400 (`#94A3B8`, ratio > 7:1) en estricto cumplimiento con las directrices **WCAG AAA**.
- **Tema Claro Institucional:** Conserva la identidad universitaria con Verde Selva UNAMAD (`#164E3F`) y Ámbar Clásico (`#D97706`).

### Iconografía y Nuevo Isotipo Minimalista
- **Cero Emojis:** Se erradicaron los emojis de Unicode en toda la interfaz de usuario, reemplazándolos por iconografía vectorial uniforme de **Material 3** (`Icons.Default.*` y `Icons.AutoMirrored.*`).
- **Nuevo Logo Adaptativo:** Isotipo geométrico monolineal de libro abierto sobre fondo obsidiana mate (`#0E1726`), integrado como drawable WebP e icono adaptativo en todas las densidades de pantalla (`mdpi` a `xxxhdpi`).
- **Cuadro de Búsqueda Compacto:** Se optimizaron los insets de ventana (`WindowInsets(0.dp)`) eliminando el espaciado redundante superior, con botón dinámico de limpieza de texto.
- **Cierre de Sesión Seguro:** Centralizado exclusivamente en la sección de **Perfil / Carnet** con modal de confirmación emergente.

---

## 2. Estructura del Monorepo

```text
Biblioteca-UNAMAD/
├── app/                        # Aplicación Móvil Android (Jetpack Compose + Clean Architecture + Room)
│   ├── src/main/java/com/example/biblioteca/
│   │   ├── MainActivity.kt     # Actividad principal y observador de tema visual
│   │   ├── model/              # Modelos de dominio inmutables y Enums
│   │   ├── data/
│   │   │   ├── local/          # Base de datos SQLite Room, DAOs, Entidades y SessionManager
│   │   │   ├── remote/         # Retrofit API REST Client y DTOs de sincronización
│   │   │   └── repository/     # Repositorio centralizado (Single Source of Truth)
│   │   └── ui/
│   │       ├── theme/          # Sistema de diseño, tokens M3 y paleta Obsidian
│   │       ├── navigation/     # Scaffold principal, barra superior y NavigationBar animada
│   │       ├── viewmodel/      # Gestión reactiva de estados con StateFlow y Corrutinas
│   │       ├── screens/        # Pantallas: Catálogo, Préstamos, Solicitudes, Dashboard, Perfil, Login
│   │       ├── dialogs/        # Modales: Visor Digital, Multas, Reportes, Solicitudes
│   │       ├── components/     # Componentes modulares reutilizables y banners de conectividad
│   │       └── util/           # Utilidades (Resolutor de URLs y proxies de PDF)
│   └── src/main/res/           # Recursos visuales, WebP optimizados y vectores adaptativos
├── backend/                    # Servidor Backend API REST (Node.js + TypeScript + Express + Prisma + PostgreSQL)
│   ├── src/                    # Controladores, Servicios, Middlewares y Rutas modulares
│   ├── prisma/                 # Esquema relacional y script de siembra (seed.ts)
│   ├── storage/libros/         # Almacén de los 100 libros completos en PDF
│   ├── API.md                  # Especificación técnica exhaustiva de endpoints REST
│   └── README.md               # Guía de despliegue en servidor VPS con PM2, Nginx y SSL
├── libros_descargados/         # Acervo bibliográfico descargado y scripts DDL
├── GUIA_EXPOSICION_PROYECTO.md # Guía estructurada para la defensa y exposición técnica
├── requerimientos.md           # Especificación de Requerimientos de Software (ERS)
├── estructura.md               # Mapeo de archivos y recursos
└── arquitectura.md             # Arquitectura de software, modelo de datos y diagramas
```

---

## 3. Capacidades y Roles del Sistema (RBAC)

El sistema implementa **Control de Acceso Basado en Roles** con 3 perfiles diferenciados:

1. **ESTUDIANTE:**
   - Autoregistro con código universitario oficial de 8 dígitos y carrera UNAMAD.
   - Búsqueda en catálogo con filtros avanzados (categoría, formato, disponibilidad y ordenamiento).
   - Solicitud de préstamos de libros físicos (3, 7 o 14 días) con seguimiento en tiempo real.
   - Carnet Digital de Lector con semáforo de solvencia y deuda acumulada.
   - Lector Digital integrado con selector de fuente (Serif, Sans, Mono), tamaño ajustable (12sp a 24sp) y temas de papel (Sepia, Cálido, Noche).
   - Selector de apariencia visual Claro / Oscuro con persistencia.
   - **Candado de Solvencia:** Bloqueo preventivo de nuevas solicitudes en caso de multas pendientes o préstamos vencidos.

2. **BIBLIOTECARIO:**
   - Bandeja de evaluación de solicitudes: Aprobación (descuento automático de inventario) o Rechazo justificado.
   - Control de circulación y devoluciones físicas con confirmación háptica (`SwipeToDismissBox`).
   - **Cálculo Automático de Mora:** Generación de multas automáticas a razón de S/. 1.00 por día de retraso.
   - Ventanilla de finanzas para cobro de multas o condonación documentada.
   - Auditoría de inventario físico y listado de lectores con ejemplares en custodia.

3. **ADMINISTRADOR:**
   - Superusuario directivo con todas las facultades de sala.
   - Panel de Control institucional con métricas analíticas en tiempo real.
   - Generador y exportador de Reportes de Circulación y Rendimiento Bibliotecario.
   - Gestión y alta de personal bibliotecario.

---

## 4. Puesta en Marcha Rápida

### 4.1. Servidor Backend API (Desarrollo Local)
```bash
cd backend

# 1. Instalar dependencias
npm install

# 2. Configurar variables de entorno
cp .env.example .env

# 3. Generar cliente Prisma, migrar y sembrar datos de prueba
npx prisma generate
npx prisma migrate dev --name init
npx prisma db seed

# 4. Iniciar servidor en modo desarrollo
npm run dev
# -> API operativa en http://localhost:3000/api/v1
```

### 4.2. Aplicación Móvil Android
1. Abrir el directorio raíz en **Android Studio Ladybug (o versión superior)**.
2. Sincronizar dependencias mediante Gradle (`Sync Project with Gradle Files`).
3. Compilar y ejecutar en emulador o dispositivo físico (Android 8.0+ / API 26+):
```powershell
# Verificación de compilación por terminal
.\gradlew.bat assembleDebug
```

---

## 5. Credenciales Iniciales de Prueba (Seed)

| Rol | Código | Contraseña | Usuario | Estado Inicial |
|---|:---:|:---:|---|---|
| **ADMIN** | `ADMIN-01` | `admin123` | Ronald Vásquez (Dirección) | Superusuario institucional |
| **BIBLIOTECARIO** | `BIBLIO-01` | `biblio123` | Carlos Gutiérrez (Ventanilla) | Operador de sala y circulación |
| **ESTUDIANTE 1** | `20230451` | `alumno123` | Rogeero Daniel Montufar (Sistemas) | Cuenta solvente (al día) |
| **ESTUDIANTE 2** | `20220120` | `alumno123` | Lucía Mendoza (Forestal) | Multa pendiente (Insolvente) |

---

## 6. Recursos para Exposición y Documentación Técnica

- **[Guía para Exposición y Defensa Técnica](./GUIA_EXPOSICION_PROYECTO.md)** *(Arquitectura, diagrama de flujo, respuestas a preguntas del jurado y guion de presentación).*
- [Especificación de Requerimientos de Software (ERS)](./requerimientos.md)
- [Mapeo de Estructura de Paquetes y Recursos](./estructura.md)
- [Arquitectura de Software y Modelo de Datos](./arquitectura.md)
- [Documentación Exhaustiva de la API REST (`API.md`)](./backend/API.md)
- [Guía de Despliegue en Servidor VPS Linux](./backend/README.md)

---
**Universidad Nacional Amazónica de Madre de Dios (UNAMAD)**  
*Vicerrectorado Académico — Dirección de Biblioteca Central*  
Puerto Maldonado, Madre de Dios, Perú.
