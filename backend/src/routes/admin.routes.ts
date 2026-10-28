import { Router } from 'express';
import { AdminController } from '../controllers/admin.controller';
import { authMiddleware } from '../middlewares/auth.middleware';
import { roleMiddleware } from '../middlewares/role.middleware';
import { RolUsuario } from '@prisma/client';

const router = Router();
const adminController = new AdminController();

// Todas las rutas de administración requieren estar autenticado y tener rol ADMIN
router.use(authMiddleware, roleMiddleware(RolUsuario.ADMIN));

// POST /api/v1/admin/usuarios -> Crear usuario con rol BIBLIOTECARIO o ADMIN únicamente
router.post('/usuarios', adminController.crearPersonal);

// GET /api/v1/admin/usuarios -> Listar usuarios de administración y biblioteca
router.get('/usuarios', adminController.listarPersonal);

export default router;
