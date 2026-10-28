import { Router } from 'express';
import authRoutes from './auth.routes';
import adminRoutes from './admin.routes';
import librosRoutes from './libros.routes';
import solicitudesRoutes from './solicitudes.routes';
import prestamosRoutes from './prestamos.routes';
import multasRoutes from './multas.routes';
import reportesRoutes from './reportes.routes';

const router = Router();

// Montar subrutas de la API v1
router.use('/auth', authRoutes);
router.use('/admin', adminRoutes);
router.use('/libros', librosRoutes);
router.use('/solicitudes', solicitudesRoutes);
router.use('/prestamos', prestamosRoutes);
router.use('/multas', multasRoutes);
router.use('/reportes', reportesRoutes);

// Endpoint de verificación de salud (Health Check)
router.get('/health', (_req, res) => {
  res.status(200).json({
    status: 'OK',
    institucion: 'Universidad Nacional Amazónica de Madre de Dios',
    servicio: 'API Backend Biblioteca Central UNAMAD',
    timestamp: new Date().toISOString()
  });
});

export default router;
