import { Request, Response, NextFunction } from 'express';
import { AdminService } from '../services/admin.service';

const adminService = new AdminService();

export class AdminController {
  async crearPersonal(req: Request, res: Response, next: NextFunction) {
    try {
      const result = await adminService.crearPersonal(req.body);
      res.status(201).json({
        success: true,
        message: `Usuario creado exitosamente con rol ${result.rol}`,
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async listarPersonal(_req: Request, res: Response, next: NextFunction) {
    try {
      const result = await adminService.listarPersonal();
      res.status(200).json({
        success: true,
        total: result.length,
        data: result
      });
    } catch (error) {
      next(error);
    }
  }
}
