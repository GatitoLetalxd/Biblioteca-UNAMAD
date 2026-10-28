import { Request, Response, NextFunction } from 'express';
import { RolUsuario } from '@prisma/client';

export function roleMiddleware(...rolesPermitidos: RolUsuario[]) {
  return (req: Request, res: Response, next: NextFunction): void => {
    if (!req.user) {
      res.status(401).json({
        success: false,
        message: 'Acceso no autorizado: Sesión no iniciada'
      });
      return;
    }

    if (!rolesPermitidos.includes(req.user.rol)) {
      res.status(403).json({
        success: false,
        message: `Acceso restringido: Se requiere rol ${rolesPermitidos.join(' o ')}`
      });
      return;
    }

    next();
  };
}
