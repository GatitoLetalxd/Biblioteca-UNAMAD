import express, { Application, Request, Response } from 'express';
import cors from 'cors';
import * as path from 'path';
import { env } from './config/env';
import apiRoutes from './routes';
import { errorMiddleware } from './middlewares/error.middleware';

const app: Application = express();

// 1. Configuración de CORS
app.use(
  cors({
    origin: env.CORS_ORIGIN,
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization']
  })
);

// 2. Parsers de petición
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// 3. Servir archivos estáticos de libros en PDF directamente
// Permite que la app móvil y navegadores descarguen y lean los PDFs
const storagePath = path.resolve(env.STORAGE_DIR);
app.use(
  '/storage/libros',
  express.static(storagePath, {
    setHeaders: (res) => {
      res.setHeader('Content-Type', 'application/pdf');
      res.setHeader('Content-Disposition', 'inline');
      res.setHeader('Cache-Control', 'public, max-age=604800, immutable');
    }
  })
);

// 4. Montar API REST v1
app.use('/api/v1', apiRoutes);

// 5. Manejo de Rutas no encontradas (404)
app.use((req: Request, res: Response) => {
  res.status(404).json({
    success: false,
    message: `Ruta no encontrada: ${req.method} ${req.originalUrl}`
  });
});

// 6. Middleware Global de Errores (Debe ser el último)
app.use(errorMiddleware);

export default app;
