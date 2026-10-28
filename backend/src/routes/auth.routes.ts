import { Router } from 'express';
import { AuthController } from '../controllers/auth.controller';
import { authMiddleware } from '../middlewares/auth.middleware';

const router = Router();
const authController = new AuthController();

// POST /api/v1/auth/register -> Registro público de estudiantes (código 8 dígitos, rol automático ESTUDIANTE)
router.post('/register', authController.register);

// POST /api/v1/auth/login -> Inicio de sesión institucional para cualquier rol
router.post('/login', authController.login);

// GET /api/v1/auth/me -> Datos del usuario autenticado actual
router.get('/me', authMiddleware, authController.me);

export default router;
