# Backend API REST — Biblioteca Central UNAMAD
**Universidad Nacional Amazónica de Madre de Dios (UNAMAD)**  
**Tecnología:** Node.js (v20+ LTS) • TypeScript • Express • Prisma ORM • PostgreSQL 16  
**Entorno de Despliegue:** VPS Linux (Ubuntu 22.04/24.04 o Debian 12) sin Docker  
**Gestor de Procesos:** PM2  
**Servidor Web Inverso & SSL:** Nginx + Certbot Let's Encrypt  

---

## 1. Características Principales

1. **Gestión de Roles Segregada (3 Roles):**
   - **`ESTUDIANTE`:** Autoregistro público obligatorio con código universitario de **8 dígitos numéricos** (sin DNI), primer nombre, primer apellido, carrera UNAMAD y teléfono. Auto-asignación de correo `{codigo}@unamad.edu.pe`.
   - **`BIBLIOTECARIO`:** Gestión de ventanilla, préstamos, devoluciones físicas, cobranza de mora y catálogo.
   - **`ADMIN`:** Superusuario institucional. Único con permisos para crear y gestionar cuentas de `BIBLIOTECARIO` y nuevos `ADMIN`.
2. **Almacenamiento Referenciado (Cero Blobs en BD):**
   - Los archivos binarios en formato PDF residen en el sistema de archivos (`storage/libros/`).
   - PostgreSQL únicamente almacena metadatos, tamaño en MB, hash **SHA-256** y la URL relativa.
   - Endpoint con `multer` para subir nuevos libros que calcula automáticamente el hash SHA-256 y tamaño en MB.
3. **Acervo Inicial de 100 Libros Digitales:**
   - Incluye catálogo inicial con **100 libros completos en PDF** distribuidos en 5 áreas: Literatura, Cuentos, Ingeniería de Sistemas, Inteligencia Artificial y Ciencias Ambientales/Amazonía.
4. **Candado de Solvencia:**
   - Bloqueo automático de solicitudes físicas si el estudiante tiene multas impagas o libros vencidos no devueltos.
5. **Cálculo Automático de Mora:**
   - Aplicación automática de **S/. 1.50 por día de retraso** al registrar devoluciones fuera de plazo.

---

## 2. Requisitos Mínimos del VPS

- **Sistema Operativo:** Ubuntu 22.04 LTS, Ubuntu 24.04 LTS o Debian 12.
- **Memoria RAM:** Mínimo 1 GB (Recomendado: 2 GB).
- **CPU:** 1 vCPU.
- **Almacenamiento:** 25 GB SSD.
- **Dominio o Subdominio:** Apuntando a la IP pública de tu VPS (ej. `api.biblioteca-unamad.edu.pe`).

---

## 3. Guía Paso a Paso para Desplegar en tu VPS (Sin Docker)

Conéctate a tu servidor VPS mediante SSH:
```bash
ssh root@tu-vps-ip
```

### Paso 1: Actualizar el Sistema Operativo
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y curl git build-essential ufw
```

---

### Paso 2: Instalar Node.js 20 LTS y PM2
```bash
# Instalar Node.js 20 vía NodeSource
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# Verificar versiones
node -v   # v20.x.x
npm -v

# Instalar PM2 globalmente
sudo npm install -g pm2
```

---

### Paso 3: Instalar y Configurar PostgreSQL
```bash
# Instalar PostgreSQL y utilidades
sudo apt install -y postgresql postgresql-contrib

# Iniciar y habilitar PostgreSQL en el arranque
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Crear el usuario y la base de datos de producción
sudo -u postgres psql
```

Dentro de la consola interactiva de PostgreSQL (`psql`), ejecuta:
```sql
-- Crear usuario con contraseña segura (cambia 'tu_password_segura')
CREATE USER biblioteca_user WITH PASSWORD 'tu_password_segura';

-- Crear base de datos asignando propietario
CREATE DATABASE biblioteca_db OWNER biblioteca_user;

-- Otorgar todos los privilegios
GRANT ALL PRIVILEGES ON DATABASE biblioteca_db TO biblioteca_user;

-- Salir de psql
\q
```

---

### Paso 4: Clonar el Repositorio de GitHub
```bash
# Crear directorio de aplicaciones web
sudo mkdir -p /var/www
sudo chown -R $USER:$USER /var/www
cd /var/www

# Clonar tu repositorio
git clone https://github.com/tu-usuario/biblioteca-unamad-backend.git
cd biblioteca-unamad-backend

# Crear carpeta de PDFs si no existe
mkdir -p storage/libros
```

---

### Paso 5: Configurar Variables de Entorno (`.env`)
Copia la plantilla y edita con tus credenciales:
```bash
cp .env.example .env
nano .env
```

Configura tu archivo `.env` en el VPS:
```ini
PORT=3000
NODE_ENV=production

# Cadena de conexión con el usuario creado en el Paso 3
DATABASE_URL="postgresql://biblioteca_user:tu_password_segura@localhost:5432/biblioteca_db?schema=public"

# Clave secreta segura para firmar tokens JWT
JWT_SECRET="clave_secreta_super_segura_unamad_vps_produccion_2026"
JWT_EXPIRES_IN="7d"

TARIFA_MORA_POR_DIA=1.50
STORAGE_DIR="./storage/libros"
CORS_ORIGIN="*"
```
Guarda y sal con `Ctrl + O`, `Enter` y `Ctrl + X`.

---

### Paso 6: Instalar Dependencias, Migrar y Sembrar Datos
```bash
# 1. Instalar dependencias
npm install

# 2. Generar cliente de Prisma
npx prisma generate

# 3. Crear las tablas en PostgreSQL
npx prisma migrate deploy

# 4. Sembrar los usuarios iniciales y los 100 libros
npx prisma db seed

# 5. Compilar TypeScript a producción (crea la carpeta dist/)
npm run build
```

---

### Paso 7: Iniciar y Persistir con PM2
PM2 mantiene la API en ejecución continua y la reinicia automáticamente ante caídas o reinicios del VPS:
```bash
# Iniciar la aplicación compilada
pm2 start dist/server.js --name biblioteca-api --max-memory-restart 300M

# Guardar la lista de procesos activos
pm2 save

# Configurar el servicio para que inicie al encender el VPS
pm2 startup
# (Ejecuta el comando 'sudo env PATH=...' que te sugiera la terminal)
```

---

### Paso 8: Instalar y Configurar Nginx (Proxy Inverso y PDFs)
```bash
sudo apt install -y nginx
sudo nano /etc/nginx/sites-available/biblioteca-api
```

Pega la siguiente configuración:
```nginx
server {
    listen 80;
    server_name api.biblioteca-unamad.edu.pe tu-vps-ip; # Cambia por tu dominio o IP

    # 1. Entrega ultra-rápida de los 100 archivos PDF
    location /storage/libros/ {
        alias /var/www/biblioteca-unamad-backend/storage/libros/;
        add_header Content-Type application/pdf;
        add_header Content-Disposition "inline";
        add_header Cache-Control "public, max-age=604800, immutable";
        autoindex off;
    }

    # 2. Proxy inverso hacia la API de Node.js (puerto 3000)
    location /api/ {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 50M; # Permite subir PDFs de hasta 50MB
    }

    # Redirección de raíz al health check
    location = / {
        proxy_pass http://127.0.0.1:3000/api/v1/health;
    }
}
```

Habilita el sitio y reinicia Nginx:
```bash
sudo ln -s /etc/nginx/sites-available/biblioteca-api /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

---

### Paso 9: Activar Certificado SSL HTTPS Gratuito (Certbot)
```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d api.biblioteca-unamad.edu.pe
```
Certbot configurará automáticamente la renovación automática y el cifrado HTTPS.

---

## 4. Cómo Subir el Backend a GitHub desde tu PC Local

En tu máquina local, dentro de `D:\Programacion y Proyectos\biblioteca-unamad-backend`:

```bash
cd "D:\Programacion y Proyectos\biblioteca-unamad-backend"

# Inicializar Git
git init

# Agregar archivos (los PDFs pesados se pueden subir o sincronizar por rsync)
git add .
git commit -m "feat: Backend API REST institucional Biblioteca UNAMAD con Prisma y PostgreSQL"

# Vincular con tu repositorio de GitHub
git branch -M main
git remote add origin https://github.com/tu-usuario/biblioteca-unamad-backend.git
git push -u origin main
```

---

## 5. Sincronizar los 100 Archivos PDF al VPS

Para transferir los 100 libros descargados hacia tu servidor de manera eficiente:
```bash
# Desde PowerShell en tu máquina local:
scp -r "D:\Programacion y Proyectos\biblioteca-unamad-backend\storage\libros\*.pdf" root@tu-vps-ip:/var/www/biblioteca-unamad-backend/storage/libros/
```

---

## 6. Actualizaciones Futuras en Producción (`deploy.sh`)

Cada vez que subas cambios a GitHub, solo debes conectarte al VPS y ejecutar:
```bash
cd /var/www/biblioteca-unamad-backend
chmod +x deploy.sh
./deploy.sh
```
El script descargará los cambios, migrará la base de datos, compilará y recargará PM2 sin interrumpir a los usuarios.

---

## 7. Credenciales Iniciales de Prueba (Seed)

| Rol | Código | Contraseña | Nombre / Cargo | Estado de Prueba |
|---|:---:|:---:|---|---|
| **ADMIN** | `ADMIN-01` | `admin123` | Ronald Vásquez (Dirección) | Superusuario, crea bibliotecarios |
| **BIBLIOTECARIO** | `BIBLIO-01` | `biblio123` | Carlos Gutiérrez (Ventanilla) | Atiende préstamos y multas |
| **ESTUDIANTE 1** | `20230451` | `alumno123` | Jhordy Pérez (Sistemas) | Cuenta al día y solvente |
| **ESTUDIANTE 2** | `20220120` | `alumno123` | Lucía Mendoza (Forestal) | Multa pendiente S/. 3.00 (Insolvente) |

Consulta la documentación completa de endpoints en [`API.md`](./API.md).
