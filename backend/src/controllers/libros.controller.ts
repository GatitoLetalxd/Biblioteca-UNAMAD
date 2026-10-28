import { Request, Response, NextFunction } from 'express';
import { LibrosService } from '../services/libros.service';
import { calcularSha256YTamano } from '../middlewares/upload.middleware';
import { AppError } from '../middlewares/error.middleware';

const librosService = new LibrosService();

export class LibrosController {
  async listar(req: Request, res: Response, next: NextFunction) {
    try {
      const result = await librosService.listarLibros(req.query as any);
      res.status(200).json({
        success: true,
        data: result
      });
    } catch (error) {
      next(error);
    }
  }

  async obtenerPorId(req: Request, res: Response, next: NextFunction) {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new AppError('El ID del libro debe ser un número entero', 400);
      }
      const libro = await librosService.obtenerPorId(id);
      res.status(200).json({
        success: true,
        data: libro
      });
    } catch (error) {
      next(error);
    }
  }

  async crearConArchivo(req: Request, res: Response, next: NextFunction) {
    try {
      if (!req.file) {
        throw new AppError('Debe adjuntar un archivo en formato PDF para el libro digital', 400);
      }

      const { sha256, tamanoMb } = calcularSha256YTamano(req.file.path);

      const nuevoLibro = await librosService.registrarLibroConArchivo(
        req.body,
        {
          filename: req.file.filename,
          sha256,
          tamanoMb
        }
      );

      res.status(201).json({
        success: true,
        message: 'Libro digital y metadatos registrados exitosamente',
        data: nuevoLibro
      });
    } catch (error) {
      next(error);
    }
  }

  async crearManual(req: Request, res: Response, next: NextFunction) {
    try {
      const nuevoLibro = await librosService.crearLibroManual(req.body);
      res.status(201).json({
        success: true,
        message: 'Libro físico registrado exitosamente en estantería',
        data: nuevoLibro
      });
    } catch (error) {
      next(error);
    }
  }

  async actualizar(req: Request, res: Response, next: NextFunction) {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new AppError('El ID del libro debe ser un número entero', 400);
      }
      const libroActualizado = await librosService.actualizarLibro(id, req.body);
      res.status(200).json({
        success: true,
        message: 'Libro actualizado exitosamente',
        data: libroActualizado
      });
    } catch (error) {
      next(error);
    }
  }

  async eliminar(req: Request, res: Response, next: NextFunction) {
    try {
      const id = parseInt(req.params.id, 10);
      if (isNaN(id)) {
        throw new AppError('El ID del libro debe ser un número entero', 400);
      }
      await librosService.eliminarLibro(id);
      res.status(200).json({
        success: true,
        message: 'Libro eliminado exitosamente del catálogo'
      });
    } catch (error) {
      next(error);
    }
  }

  async categorias(_req: Request, res: Response, next: NextFunction) {
    try {
      const categorias = await librosService.obtenerCategorias();
      res.status(200).json({
        success: true,
        total: categorias.length,
        data: categorias
      });
    } catch (error) {
      next(error);
    }
  }
}
