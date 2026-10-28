import { Request, Response, NextFunction } from 'express';
import { ReportesService } from '../services/reportes.service';

const reportesService = new ReportesService();

export class ReportesController {
  async dashboard(_req: Request, res: Response, next: NextFunction) {
    try {
      const result = await reportesService.getDashboard();
      res.status(200).json({
        success: true,
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async inventarioLectores(_req: Request, res: Response, next: NextFunction) {
    try {
      const result = await reportesService.getInventarioConLectores();
      res.status(200).json({
        success: true,
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async institucional(_req: Request, res: Response, next: NextFunction) {
    try {
      const result = await reportesService.getReporteInstitucional();
      res.status(200).json({
        success: true,
        data: result
      });
    } catch (error) {
      next(error);
    }
  }
}
