import { Router } from 'express';
import { MultasController } from '../controllers/multas.controller';
import { authMiddleware } from '../middlewares/auth.middleware';
import { roleMiddleware } from '../middlewares/role.middleware';
import { RolUsuario } from '@prisma/client';

const router = Router();
const multasController = new MultasController();

// Todas las rutas requieren estar autenticado
router.use(authMiddleware);

// ---------------------------------------------------------------------
// RUTAS DEL ESTUDIANTE (Carnet Digital y Solvencia)
// ---------------------------------------------------------------------

// GET /api/v1/multas/mi-solvencia -> Consulta estado de solvencia y deuda acumulada
router.get(
  '/mi-solvencia',
  roleMiddleware(RolUsuario.ESTUDIANTE),
  multasController.miSolvencia
);

// ---------------------------------------------------------------------
// RUTAS DEL BIBLIOTECARIO Y ADMINISTRADOR
// ---------------------------------------------------------------------

// GET /api/v1/multas -> Listado general de multas (Pendientes, Pagadas o Condonadas)
router.get(
  '/',
  roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN),
  multasController.listar
);

// PUT /api/v1/multas/:id/cobrar -> Registrar cobro en efectivo en ventanilla
router.put(
  '/:id/cobrar',
  roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN),
  multasController.cobrar
);

// PUT /api/v1/multas/:id/condonar -> Condonación administrativa con motivo obligatorio
router.put(
  '/:id/condonar',
  roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN),
  multasController.condonar
);

export default router;
