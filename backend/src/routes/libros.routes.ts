import { Router } from 'express';
import { LibrosController } from '../controllers/libros.controller';
import { authMiddleware } from '../middlewares/auth.middleware';
import { roleMiddleware } from '../middlewares/role.middleware';
import { uploadPdf } from '../middlewares/upload.middleware';
import { RolUsuario } from '@prisma/client';

const router = Router();
const librosController = new LibrosController();

// ---------------------------------------------------------------------
// RUTAS PÚBLICAS (Catálogo y lectura)
// ---------------------------------------------------------------------

// GET /api/v1/libros -> Catálogo con búsqueda, filtros y paginación
router.get('/', librosController.listar);

// GET /api/v1/libros/categorias -> Lista de categorías académicas disponibles
router.get('/categorias', librosController.categorias);

// GET /api/v1/libros/:id -> Detalle completo de un libro y disponibilidad
router.get('/:id', librosController.obtenerPorId);

// ---------------------------------------------------------------------
// RUTAS ADMINISTRATIVAS (Solo Bibliotecario y Admin)
// ---------------------------------------------------------------------

// POST /api/v1/libros/upload -> Registrar libro digital con subida de archivo PDF
router.post(
  '/upload',
  authMiddleware,
  roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN),
  uploadPdf.single('archivo'),
  librosController.crearConArchivo
);

// POST /api/v1/libros/manual -> Registrar libro físico en estantería
router.post(
  '/manual',
  authMiddleware,
  roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN),
  librosController.crearManual
);

// PUT /api/v1/libros/:id -> Actualizar datos o stock de un libro
router.put(
  '/:id',
  authMiddleware,
  roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN),
  librosController.actualizar
);

// DELETE /api/v1/libros/:id -> Eliminar libro del catálogo
router.delete(
  '/:id',
  authMiddleware,
  roleMiddleware(RolUsuario.BIBLIOTECARIO, RolUsuario.ADMIN),
  librosController.eliminar
);

export default router;
