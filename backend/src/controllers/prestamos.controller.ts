import { Request, Response, NextFunction } from 'express';
import { PrestamosService } from '../services/prestamos.service';
import { EstadoPrestamo } from '@prisma/client';
import { AppError } from '../middlewares/error.middleware';

const prestamosService = new PrestamosService();

export class PrestamosController {
  async listar(req: Request, res: Response, next: NextFunction) {
    try {
      const estado = req.query.estado as EstadoPrestamo | undefined;
      const result = await prestamosService.listarPrestamos(estado);
      res.status(200).json({
        success: true,
        total: result.length,
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async misPrestamos(req: Request, res: Response, next: NextFunction) {
    try {
      const usuarioId = req.user!.id;
      const result = await prestamosService.listarMisPrestamos(usuarioId);
      res.status(200).json({
        success: true,
        total: result.length,
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async crearDirecto(req: Request, res: Response, next: NextFunction) {
    try {
      const result = await prestamosService.crearPrestamoDirecto(req.body);
      res.status(201).json({
        success: true,
        message: 'Préstamo físico en sala/domicilio registrado exitosamente',
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async devolver(req: Request, res: Response, next: NextFunction) {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new AppError('El ID del préstamo debe ser un número entero', 400);
      }
      const result = await prestamosService.registrarDevolucion(id);
      res.status(200).json({
        success: true,
        message: result.mensaje,
        data: result
      });
    } catch (error) {
      next(error);
    }
  }
}
