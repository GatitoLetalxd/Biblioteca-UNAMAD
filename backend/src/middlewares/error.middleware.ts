import { Request, Response, NextFunction } from 'express';
import { ZodError } from 'zod';

export class AppError extends Error {
  public statusCode: number;

  constructor(message: string, statusCode = 400) {
    super(message);
    this.statusCode = statusCode;
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

export function errorMiddleware(
  err: Error,
  _req: Request,
  res: Response,
  _next: NextFunction
): void {
  if (err instanceof AppError) {
    res.status(err.statusCode).json({
      success: false,
      message: err.message
    });
    return;
  }

  if (err instanceof ZodError) {
    const issues = err.issues.map((i) => ({
      campo: i.path.join('.'),
      mensaje: i.message
    }));

    res.status(400).json({
      success: false,
      message: 'Error de validación en los datos enviados',
      errores: issues
    });
    return;
  }

  console.error('Unhandled Error:', err);

  res.status(500).json({
    success: false,
    message: 'Error interno en el servidor de Biblioteca UNAMAD'
  });
}
