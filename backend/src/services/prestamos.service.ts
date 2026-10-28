import { prisma } from '../config/prisma';
import { EstadoPrestamo, FormatoLibro, EstadoMulta } from '@prisma/client';
import { AppError } from '../middlewares/error.middleware';
import { env } from '../config/env';
import { z } from 'zod';

export const crearPrestamoDirectoSchema = z.object({
  libroId: z.number().int().positive('El ID del libro es obligatorio'),
  codigoLector: z.string().min(3, 'El código del lector es obligatorio'),
  nombreLector: z.string().min(2, 'El nombre del lector es obligatorio'),
  carreraOArea: z.string().min(2, 'La carrera o área es obligatoria'),
  telefonoLector: z.string().optional(),
  diasPrestamo: z.number().int().min(1).max(30).default(7),
  notas: z.string().optional()
});

export type CrearPrestamoDirectoDTO = z.infer<typeof crearPrestamoDirectoSchema>;

export class PrestamosService {
  /**
   * Listar préstamos (Vista administrativa para bibliotecarios)
   */
  async listarPrestamos(estado?: EstadoPrestamo) {
    const where: any = {};
    const now = new Date();

    if (estado) {
      if (estado === EstadoPrestamo.VENCIDO) {
        where.estado = EstadoPrestamo.ACTIVO;
        where.fechaDevolucionEstimada = { lt: now };
      } else {
        where.estado = estado;
      }
    }

    const prestamos = await prisma.prestamo.findMany({
      where,
      include: {
        libro: {
          select: {
            id: true,
            titulo: true,
            autor: true,
            categoria: true,
            ubicacion: true,
            portadaUrl: true
          }
        }
      },
      orderBy: { fechaPrestamo: 'desc' }
    });

    // Calcular dinámicamente si alguno en ACTIVO ya venció
    return prestamos.map((p) => {
      let estadoCalculado: EstadoPrestamo = p.estado;
      if (p.estado === EstadoPrestamo.ACTIVO && now > p.fechaDevolucionEstimada) {
        estadoCalculado = EstadoPrestamo.VENCIDO;
      }
      return {
        ...p,
        estado: estadoCalculado,
        esVencido: estadoCalculado === EstadoPrestamo.VENCIDO
      };
    });
  }

  /**
   * Estudiante consulta sus préstamos personales
   */
  async listarMisPrestamos(usuarioId: number) {
    const now = new Date();
    const prestamos = await prisma.prestamo.findMany({
      where: { usuarioId },
      include: {
        libro: {
          select: {
            id: true,
            titulo: true,
            autor: true,
            categoria: true,
            ubicacion: true,
            portadaUrl: true
          }
        }
      },
      orderBy: { fechaPrestamo: 'desc' }
    });

    return prestamos.map((p) => {
      let estadoCalculado: EstadoPrestamo = p.estado;
      if (p.estado === EstadoPrestamo.ACTIVO && now > p.fechaDevolucionEstimada) {
        estadoCalculado = EstadoPrestamo.VENCIDO;
      }
      return {
        ...p,
        estado: estadoCalculado,
        esVencido: estadoCalculado === EstadoPrestamo.VENCIDO
      };
    });
  }

  /**
   * Registro directo de préstamo en mostrador por el bibliotecario
   */
  async crearPrestamoDirecto(dto: CrearPrestamoDirectoDTO) {
    const validated = crearPrestamoDirectoSchema.parse(dto);

    const libro = await prisma.libro.findUnique({
      where: { id: validated.libroId }
    });

    if (!libro) {
      throw new AppError('Libro no encontrado', 404);
    }

    if (libro.formato !== FormatoLibro.DIGITAL && libro.ejemplaresDisponibles <= 0) {
      throw new AppError('No hay ejemplares físicos disponibles en estante', 400);
    }

    // Buscar si el usuario existe para asociar su ID
    const usuario = await prisma.usuario.findUnique({
      where: { codigo: validated.codigoLector.trim() }
    });

    const now = new Date();
    const dayMillis = 24 * 60 * 60 * 1000;
    const fechaEstimada = new Date(now.getTime() + validated.diasPrestamo * dayMillis);

    return prisma.$transaction(async (tx) => {
      // Decrementar stock
      if (libro.formato !== FormatoLibro.DIGITAL) {
        await tx.libro.update({
          where: { id: libro.id },
          data: { ejemplaresDisponibles: { decrement: 1 } }
        });
      }

      return tx.prestamo.create({
        data: {
          usuarioId: usuario ? usuario.id : 1, // Fallback si fue un lector externo
          libroId: libro.id,
          nombreLector: validated.nombreLector.trim(),
          codigoLector: validated.codigoLector.trim(),
          carreraOArea: validated.carreraOArea.trim(),
          telefonoLector: validated.telefonoLector?.trim() || 'Sin teléfono',
          fechaPrestamo: now,
          fechaDevolucionEstimada: fechaEstimada,
          estado: EstadoPrestamo.ACTIVO,
          notas: validated.notas?.trim() || 'Préstamo manual en ventanilla de biblioteca.'
        }
      });
    });
  }

  /**
   * Registrar devolución física y evaluar mora:
   * 1. Marca préstamo como DEVUELTO
   * 2. Incrementa stock físico disponible del libro
   * 3. Si la fecha actual supera a fechaDevolucionEstimada:
   *    Calcula días de retraso * S/. 1.50 y genera automáticamente la multa
   */
  async registrarDevolucion(prestamoId: number) {
    const prestamo = await prisma.prestamo.findUnique({
      where: { id: prestamoId },
      include: { libro: true, usuario: true }
    });

    if (!prestamo) {
      throw new AppError('Préstamo no encontrado', 404);
    }

    if (prestamo.estado === EstadoPrestamo.DEVUELTO) {
      throw new AppError('Este préstamo ya fue devuelto anteriormente', 400);
    }

    const now = new Date();
    const isOverdue = now > prestamo.fechaDevolucionEstimada;

    return prisma.$transaction(async (tx) => {
      // 1. Marcar préstamo como DEVUELTO
      const prestamoActualizado = await tx.prestamo.update({
        where: { id: prestamoId },
        data: {
          estado: EstadoPrestamo.DEVUELTO,
          fechaDevolucionReal: now
        }
      });

      // 2. Incrementar stock disponible del libro
      await tx.libro.update({
        where: { id: prestamo.libroId },
        data: {
          ejemplaresDisponibles: { increment: 1 }
        }
      });

      let multaGenerada = null;
      let mensaje = 'Devolución registrada con éxito. Ejemplar reincorporado a estantería.';

      // 3. Generar multa automática si hubo retraso en la entrega
      if (isOverdue) {
        const diffMillis = now.getTime() - prestamo.fechaDevolucionEstimada.getTime();
        const dayMillis = 24 * 60 * 60 * 1000;
        const diasRetraso = Math.max(1, Math.floor(diffMillis / dayMillis));
        const montoTotal = parseFloat((diasRetraso * env.TARIFA_MORA_POR_DIA).toFixed(2));

        multaGenerada = await tx.multa.create({
          data: {
            prestamoId: prestamo.id,
            usuarioId: prestamo.usuarioId,
            libroId: prestamo.libroId,
            codigoEstudiante: prestamo.codigoLector,
            nombreEstudiante: prestamo.nombreLector,
            tituloLibro: prestamo.libro.titulo,
            diasRetraso,
            montoTotal,
            estado: EstadoMulta.PENDIENTE,
            fechaGeneracion: now,
            observaciones: `Generada automáticamente por ${diasRetraso} día(s) de retraso en devolución física.`
          }
        });

        mensaje = `Devolución con ${diasRetraso} día(s) de mora. Se generó multa por S/. ${montoTotal.toFixed(2)}.`;
      }

      return {
        prestamo: prestamoActualizado,
        multa: multaGenerada,
        mensaje
      };
    });
  }
}
