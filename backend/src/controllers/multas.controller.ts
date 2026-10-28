import { Request, Response, NextFunction } from 'express';
import { MultasService } from '../services/multas.service';
import { EstadoMulta } from '@prisma/client';
import { AppError } from '../middlewares/error.middleware';

const multasService = new MultasService();

export class MultasController {
  async listar(req: Request, res: Response, next: NextFunction) {
    try {
      const estado = req.query.estado as EstadoMulta | undefined;
      const result = await multasService.listarMultas(estado);
      res.status(200).json({
        success: true,
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async miSolvencia(req: Request, res: Response, next: NextFunction) {
    try {
      const usuarioId = req.user!.id;
      const result = await multasService.consultarMiSolvencia(usuarioId);
      res.status(200).json({
        success: true,
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async cobrar(req: Request, res: Response, next: NextFunction) {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new AppError('El ID de la multa debe ser un número entero', 400);
      }
      const result = await multasService.cobrarMulta(id);
      res.status(200).json({
        success: true,
        message: 'Pago de multa registrado con éxito en ventanilla.',
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async condonar(req: Request, res: Response, next: NextFunction) {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new AppError('El ID de la multa debe ser un número entero', 400);
      }
      const { motivo } = req.body;
      const result = await multasService.condonarMulta(id, motivo);
      res.status(200).json({
        success: true,
        message: 'Condonación de multa autorizada y registrada con justificación.',
        data: result
      });
    } catch (error) {
      next(error);
    }
  }
}
