import { Request, Response, NextFunction } from 'express';
import { SolicitudesService } from '../services/solicitudes.service';
import { EstadoSolicitud } from '@prisma/client';
import { AppError } from '../middlewares/error.middleware';

const solicitudesService = new SolicitudesService();

export class SolicitudesController {
  async crear(req: Request, res: Response, next: NextFunction) {
    try {
      const usuarioId = req.user!.id;
      const result = await solicitudesService.crearSolicitud(usuarioId, req.body);
      res.status(201).json({
        success: true,
        message: 'Solicitud de préstamo registrada. Será evaluada por el bibliotecario.',
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async misSolicitudes(req: Request, res: Response, next: NextFunction) {
    try {
      const usuarioId = req.user!.id;
      const result = await solicitudesService.listarMisSolicitudes(usuarioId);
      res.status(200).json({
        success: true,
        total: result.length,
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async listar(req: Request, res: Response, next: NextFunction) {
    try {
      const estado = req.query.estado as EstadoSolicitud | undefined;
      const result = await solicitudesService.listarSolicitudes(estado);
      res.status(200).json({
        success: true,
        total: result.length,
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async aprobar(req: Request, res: Response, next: NextFunction) {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new AppError('El ID de la solicitud debe ser un número entero', 400);
      }
      const result = await solicitudesService.aprobarSolicitud(id);
      res.status(200).json({
        success: true,
        message: 'Solicitud aprobada con éxito. Préstamo generado e inventario actualizado.',
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async rechazar(req: Request, res: Response, next: NextFunction) {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new AppError('El ID de la solicitud debe ser un número entero', 400);
      }
      const { motivoRechazo } = req.body;
      const result = await solicitudesService.rechazarSolicitud(id, motivoRechazo);
      res.status(200).json({
        success: true,
        message: 'Solicitud rechazada con motivo registrado.',
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async cancelar(req: Request, res: Response, next: NextFunction) {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new AppError('El ID de la solicitud debe ser un número entero', 400);
      }
      const usuarioId = req.user!.id;
      const result = await solicitudesService.cancelarSolicitud(id, usuarioId);
      res.status(200).json({
        success: true,
        message: 'Solicitud cancelada por el estudiante.',
        data: result
      });
    } catch (error) {
      next(error);
    }
  }
}
