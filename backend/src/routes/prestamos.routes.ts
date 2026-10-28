import { Router } from 'express';
import { PrestamosController } from '../controllers/prestamos.controller';
import { authMiddleware } from '../middlewares/auth.middleware';
import { roleMiddleware } from '../middlewares/role.middleware';
import { RolUsuario } from '@prisma/client';

const router = Router();
const prestamosController = new PrestamosController();

// Todas las rutas requieren estar autenticado
router.use(authMiddleware);

// ---------------------------------------------------------------------
// RUTAS DEL ESTUDIANTE
// ---------------------------------------------------------------------

// GET /api/v1/prestamos/mis-prestamos -> Préstamos activos e históricos del estudiante
router.get(
  '/mis-prestamos',
  roleMiddleware(RolUsuario.ESTUDIANTE),
  prestamosController.misPrestamos
);

// ---------------------------------------------------------------------
// RUTAS DEL BIBLIOTECARIO Y ADMINISTRADOR
// ---------------------------------------------------------------------

// GET /api/v1/prestamos -> Listado general de préstamos (Activos, Vencidos o Historial)
router.get(
  '/',
  roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN),
  prestamosController.listar
);

// POST /api/v1/prestamos -> Registro presencial de préstamo físico en ventanilla
router.post(
  '/',
  roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN),
  prestamosController.crearDirecto
);

// PUT /api/v1/prestamos/:id/devolver -> Recepción de libro, libera stock y genera multa automática si hay mora
router.put(
  '/:id/devolver',
  roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN),
  prestamosController.devolver
);

export default router;
