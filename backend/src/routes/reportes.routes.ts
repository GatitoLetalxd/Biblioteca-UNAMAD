import { Router } from 'express';
import { ReportesController } from '../controllers/reportes.controller';
import { authMiddleware } from '../middlewares/auth.middleware';
import { roleMiddleware } from '../middlewares/role.middleware';
import { RolUsuario } from '@prisma/client';

const router = Router();
const reportesController = new ReportesController();

// Todas las rutas de reportes son exclusivas de Bibliotecarios y Administradores
router.use(authMiddleware, roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN));

// GET /api/v1/reportes/dashboard -> KPIs en tiempo real (libros, préstamos, solicitudes, multas)
router.get('/dashboard', reportesController.dashboard);

// GET /api/v1/reportes/inventario-lectores -> Base de datos e inventario con lectores nominales en posesión
router.get('/inventario-lectores', reportesController.inventarioLectores);

// GET /api/v1/reportes/institucional -> Métricas de circulación histórica, ranking de libros y desglose por carrera
router.get('/institucional', reportesController.institucional);

export default router;
