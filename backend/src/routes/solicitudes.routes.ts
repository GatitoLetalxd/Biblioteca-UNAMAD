import { Router } from 'express';
import { SolicitudesController } from '../controllers/solicitudes.controller';
import { authMiddleware } from '../middlewares/auth.middleware';
import { roleMiddleware } from '../middlewares/role.middleware';
import { RolUsuario } from '@prisma/client';

const router = Router();
const solicitudesController = new SolicitudesController();

// Todas las rutas requieren estar autenticado
router.use(authMiddleware);

// ---------------------------------------------------------------------
// RUTAS DEL ESTUDIANTE
// ---------------------------------------------------------------------

// POST /api/v1/solicitudes -> Estudiante solicita préstamo físico (valida solvencia y disponibilidad)
router.post(
  '/',
  roleMiddleware(RolUsuario.ESTUDIANTE),
  solicitudesController.crear
);

// GET /api/v1/solicitudes/mis-solicitudes -> Estudiante consulta el estado de sus solicitudes
router.get(
  '/mis-solicitudes',
  roleMiddleware(RolUsuario.ESTUDIANTE),
  solicitudesController.misSolicitudes
);

// DELETE /api/v1/solicitudes/:id/cancelar -> Estudiante cancela solicitud pendiente
router.delete(
  '/:id/cancelar',
  roleMiddleware(RolUsuario.ESTUDIANTE),
  solicitudesController.cancelar
);

// ---------------------------------------------------------------------
// RUTAS DEL BIBLIOTECARIO Y ADMINISTRADOR
// ---------------------------------------------------------------------

// GET /api/v1/solicitudes -> Listar todas las solicitudes (Pendientes o Historial)
router.get(
  '/',
  roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN),
  solicitudesController.listar
);

// PUT /api/v1/solicitudes/:id/aprobar -> Aprueba solicitud, genera préstamo y descuenta stock
router.put(
  '/:id/aprobar',
  roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN),
  solicitudesController.aprobar
);

// PUT /api/v1/solicitudes/:id/rechazar -> Rechaza solicitud con motivo obligatorio
router.put(
  '/:id/rechazar',
  roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN),
  solicitudesController.rechazar
);

export default router;
