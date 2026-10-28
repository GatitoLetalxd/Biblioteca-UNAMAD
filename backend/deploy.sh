#!/usr/bin/env bash
# =====================================================================
# SCRIPT DE DESPLIEGUE Y ACTUALIZACIÓN CONTINUA EN VPS (SIN DOCKER)
# Sistema de Biblioteca Central — Universidad Nacional Amazónica de Madre de Dios
# =====================================================================

set -e

echo "======================================================================"
echo " 🚀 INICIANDO DESPLIEGUE / ACTUALIZACIÓN: BIBLIOTECA UNAMAD BACKEND"
echo "======================================================================"

# 1. Obtener última versión desde GitHub
echo "[1/6] Obteniendo cambios de Git..."
git pull origin main

# 2. Instalar dependencias
echo "[2/6] Instalando dependencias de Node.js..."
npm install

# 3. Generar cliente de Prisma
echo "[3/6] Generando cliente de Prisma ORM..."
npx prisma generate

# 4. Aplicar migraciones pendientes en PostgreSQL
echo "[4/6] Aplicando migraciones de base de datos..."
npx prisma migrate deploy

# 5. Compilar TypeScript a JavaScript de producción
echo "[5/6] Compilando TypeScript (dist/)..."
npm run build

# 6. Reiniciar o iniciar proceso en PM2 sin caída de servicio (Zero-downtime)
echo "[6/6] Reiniciando servicio en PM2..."
if pm2 describe biblioteca-api > /dev/null 2>&1; then
    pm2 reload biblioteca-api --update-env
    echo "✓ Proceso 'biblioteca-api' recargado exitosamente."
else
    pm2 start dist/server.js --name biblioteca-api --max-memory-restart 300M
    pm2 save
    echo "✓ Proceso 'biblioteca-api' iniciado y registrado en PM2."
fi

echo "======================================================================"
echo " ✅ DESPLIEGUE COMPLETADO EXITOSAMENTE"
echo " Estado del servicio:"
pm2 status biblioteca-api
echo "======================================================================"
