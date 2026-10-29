# 🏛️ Sistema Integral de Biblioteca Central — UNAMAD
### Universidad Nacional Amazónica de Madre de Dios

![Android](https://img.shields.io/badge/Android-Kotlin%202.2-green.svg?logo=android&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-blue.svg?logo=jetpackcompose&logoColor=white)
![Backend](https://img.shields.io/badge/Backend-Node.js%2020%20%7C%20TypeScript-informational.svg?logo=nodedotjs&logoColor=white)
![Database](https://img.shields.io/badge/Database-PostgreSQL%2016%20%7C%20Room%20v4-blue.svg?logo=postgresql&logoColor=white)
![ORM](https://img.shields.io/badge/ORM-Prisma%205-2D3748.svg?logo=prisma&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)

Monorepo institucional que integra la **aplicación móvil Android**, el **backend API REST en Node.js/TypeScript**, un catálogo relacional en **PostgreSQL**, y un **acervo digital de 100 libros completos en PDF** clasificados por facultades y disciplinas académicas.

---

## 📂 Estructura del Monorepo

```text
Biblioteca-UNAMAD/
├── app/                  # 📱 Aplicación Móvil Android (Jetpack Compose + Clean Architecture + Room v4)
├── backend/              # 💻 Servidor Backend API REST (Node.js + TypeScript + Express + Prisma + PostgreSQL)
│   ├── src/              # Código fuente modular (Controllers, Services, Middlewares, Routes)
│   ├── prisma/           # Modelado relacional y script de siembra (seed.ts)
│   ├── storage/libros/   # Almacén de los 100 libros completos en PDF referenciados
│   ├── API.md            # Documentación técnica completa de los endpoints REST
│   ├── README.md         # Guía de despliegue nativo en VPS (PM2 + Nginx + PostgreSQL)
│   └── deploy.sh         # Script automatizado de actualización para el VPS
├── libros_descargados/   # Acervo bibliográfico de 100 PDFs y script SQL DDL masivo
├── scripts/              # Herramientas de extracción y catalogación automatizada
├── requerimientos.md     # Especificación de Requerimientos de Software (ERS)
├── estructura.md         # Mapeo exhaustivo de archivos, módulos y recursos
└── arquitectura.md       # Arquitectura de software, modelo de datos y diagramas de flujo
```

---

## 🎯 Capacidades y Roles del Sistema

El sistema implementa **Control de Acceso Basado en Roles (RBAC)** con 3 roles estrictos:

1. **`ESTUDIANTE`:**
   - Autoregistro público obligatorio con **código universitario de 8 dígitos** (sin solicitud de DNI), primer nombre, primer apellido, carrera UNAMAD y teléfono.
   - Solicitud de préstamos de libros físicos (3, 7 o 14 días) con seguimiento en tiempo real (`⏳ En Revisión`, `✅ Aprobada`, `❌ Rechazada`).
   - Carnet Digital de Lector con semáforo de solvencia y deuda acumulada.
   - Lector Digital Multi-Capítulo in-app con temas visuales (Papel, Sepia, Noche) y descarga de PDFs con verificación SHA-256.
   - **Candado de Solvencia:** Bloqueo automático si el alumno tiene multas impagas o libros vencidos no devueltos.

2. **`BIBLIOTECARIO`:**
   - Bandeja de solicitudes de préstamo: Aprobación (descuenta stock físico) y rechazo con registro obligatorio de motivo.
   - Control de circulación y devoluciones físicas.
   - **Cálculo Automático de Mora:** Aplicación automática de **S/. 1.50 por día de retraso** al recibir libros vencidos.
   - Cobranza de multas en ventanilla y condonación administrativa con justificación documental.
   - Panel de Auditoría tipo Base de Datos con visualización de alumnos en posesión de ejemplares físicos.

3. **`ADMIN`:**
   - Superusuario institucional. Posee todas las facultades del bibliotecario y la exclusividad del endpoint `POST /api/v1/admin/usuarios` para crear y gestionar cuentas de `BIBLIOTECARIO` y nuevos `ADMIN`.

---

## 🚀 Puesta en Marcha Rápida

### 1. Backend API (Desarrollo Local)
```bash
cd backend

# Instalar dependencias
npm install

# Configurar variables de entorno
cp .env.example .env

# Generar cliente Prisma y sembrar la base de datos
npx prisma generate
npx prisma migrate dev --name init
npx prisma db seed

# Iniciar servidor en modo desarrollo (recarga en caliente)
npm run dev
# -> API corriendo en http://localhost:3000/api/v1
```

### 2. Aplicación Móvil Android
1. Abrir la carpeta raíz `Biblioteca UNAMAD` en **Android Studio Ladybug (o superior)**.
2. Sincronizar Gradle (`Sync Project with Gradle Files`).
3. Ejecutar en emulador o dispositivo físico (Android 8.0+ / API 26+).

---

## 🌐 Despliegue en VPS (Linux sin Docker)

Para desplegar el backend en un servidor VPS con **Ubuntu 22.04 / 24.04** utilizando **PM2, Nginx y PostgreSQL nativo**:

```bash
# En el VPS:
cd /var/www
git clone https://github.com/GatitoLetalxd/Biblioteca-UNAMAD.git
cd Biblioteca-UNAMAD/backend

# Configurar y compilar
cp .env.example .env # Configurar DATABASE_URL y JWT_SECRET
npm install
npx prisma migrate deploy
npx prisma db seed
npm run build

# Iniciar con PM2 (Autorestart y persistencia tras reinicios)
pm2 start dist/server.js --name biblioteca-api --max-memory-restart 300M
pm2 startup
pm2 save
```
> Consulta la guía detallada de configuración de Nginx con SSL y caché en [`backend/README.md`](./backend/README.md).

---

## 📚 Acervo de 100 Libros Digitales (PDFs)

El catálogo incluye **100 libros completos en PDF** organizados temáticamente:
- **Libros 01 – 20:** *Literatura y Filosofía* (Camus, Martín Gaite, Spinoza, Schopenhauer, etc.).
- **Libros 21 – 40:** *Cuentos y Narrativa* (Ribeyro, Hermanos Grimm, Gallegos Lara, Wilde, etc.).
- **Libros 41 – 60:** *Ingeniería de Sistemas* (Algoritmos, Python, Redes, Electrónica, TIC).
- **Libros 61 – 80:** *Inteligencia Artificial* (Deep Learning, Pattern Analysis, Feature Extraction, Vision).
- **Libros 81 – 100:** *Ciencias Ambientales e Investigación* (Amazonía, Ecología, Botánica, Metodología IIAP).

Todos los libros están referenciados mediante rutas relativas y cuentan con hashes **SHA-256 precalculados** para validación de integridad en [`backend/prisma/catalogo_100.json`](./backend/prisma/catalogo_100.json).

---

## 🔑 Credenciales Iniciales de Prueba (Seed)

| Rol | Código | Contraseña | Usuario | Estado Inicial |
|---|:---:|:---:|---|---|
| **ADMIN** | `ADMIN-01` | `admin123` | Ronald Vásquez (Dirección) | Superusuario institucional |
| **BIBLIOTECARIO** | `BIBLIO-01` | `biblio123` | Carlos Gutiérrez (Ventanilla) | Operador de sala y circulación |
| **ESTUDIANTE 1** | `20230451` | `alumno123` | Jhordy Pérez (Sistemas) | Cuenta solvente (al día) |
| **ESTUDIANTE 2** | `20220120` | `alumno123` | Lucía Mendoza (Forestal) | Multa pendiente S/. 3.00 (Insolvente) |

---

## 📖 Documentación Técnica de Referencia

- 📋 [**Especificación de Requerimientos de Software (ERS)**](./requerimientos.md)
- 🗺️ [**Estructura Completa de Paquetes y Recursos**](./estructura.md)
- 🏗️ [**Arquitectura de Software y Modelo Relacional (ERD)**](./arquitectura.md)
- ⚡ [**Documentación Exhaustiva de APIs REST (`API.md`)**](./backend/API.md)
- 🖥️ [**Guía de Despliegue en Servidor VPS (`README.md`)**](./backend/README.md)

---
**Universidad Nacional Amazónica de Madre de Dios (UNAMAD)**  
*Vicerrectorado Académico — Dirección de Biblioteca Central*  
Puerto Maldonado, Madre de Dios, Perú.
