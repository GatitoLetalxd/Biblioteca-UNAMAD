import app from './app';
import { env } from './config/env';
import { prisma } from './config/prisma';

const PORT = env.PORT;

async function startServer() {
  try {
    // Verificar conexión con la base de datos PostgreSQL
    await prisma.$connect();
    console.log('✅ Conexión establecida exitosamente con PostgreSQL');

    const server = app.listen(PORT, () => {
      console.log('======================================================================');
      console.log(' 🚀 SERVIDOR BACKEND — BIBLIOTECA CENTRAL UNAMAD ONLINE');
      console.log(` 📡 Escuchando en el puerto: ${PORT}`);
      console.log(` 🌍 Entorno: ${env.NODE_ENV}`);
      console.log(` 📚 Almacén de Libros: ${env.STORAGE_DIR}`);
      console.log(` 🩺 Health Check: http://localhost:${PORT}/api/v1/health`);
      console.log('======================================================================');
    });

    // Cierre ordenado (Graceful Shutdown)
    const shutdown = async (signal: string) => {
      console.log(`\n🛑 Recibida señal ${signal}. Cerrando servidor de forma segura...`);
      server.close(async () => {
        await prisma.$disconnect();
        console.log('🔌 Conexión con PostgreSQL cerrada. Servidor detenido.');
        process.exit(0);
      });
    };

    process.on('SIGINT', () => shutdown('SIGINT'));
    process.on('SIGTERM', () => shutdown('SIGTERM'));

  } catch (error) {
    console.error('❌ Error al iniciar el servidor backend:', error);
    await prisma.$disconnect();
    process.exit(1);
  }
}

startServer();
