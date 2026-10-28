import { prisma } from '../config/prisma';
import { EstadoSolicitud, EstadoPrestamo, FormatoLibro } from '@prisma/client';
import { AppError } from '../middlewares/error.middleware';
import { z } from 'zod';

export const crearSolicitudSchema = z.object({
  libroId: z.number().int().positive('El ID del libro es obligatorio'),
  diasSolicitados: z.number().int().refine((val) => [3, 7, 14].includes(val), {
    message: 'El plazo de préstamo debe ser de 3, 7 o 14 días'
  }),
  notas: z.string().optional()
});

export type CrearSolicitudDTO = z.infer<typeof crearSolicitudSchema>;

export class SolicitudesService {
  /**
   * Estudiante emite solicitud de préstamo de libro físico
   * Incluye CANDADO DE SOLVENCIA (Bloqueo por multas pendientes o libros vencidos)
   */
  async crearSolicitud(usuarioId: number, dto: CrearSolicitudDTO) {
    const validated = crearSolicitudSchema.parse(dto);

    // 1. Candado de Solvencia: Verificar si el alumno tiene multas impagas
    const multasPendientes = await prisma.multa.count({
      where: {
        usuarioId,
        estado: 'PENDIENTE'
      }
    });

    if (multasPendientes > 0) {
      throw new AppError(
        'Cuenta no solvente: Tienes multas pendientes de pago por mora. Acércate a la ventanilla de biblioteca para cancelar y reactivar tus servicios.',
        403
      );
    }

    // 2. Candado de Solvencia: Verificar si tiene libros físicos vencidos en su poder
    const now = new Date();
    const prestamosVencidos = await prisma.prestamo.count({
      where: {
        usuarioId,
        estado: EstadoPrestamo.ACTIVO,
        fechaDevolucionEstimada: { lt: now }
      }
    });

    if (prestamosVencidos > 0) {
      throw new AppError(
        'Cuenta no solvente: Tienes libros físicos vencidos pendientes de devolución. Entrega los ejemplares antes de solicitar nuevos préstamos.',
        403
      );
    }

    // 3. Validar libro y disponibilidad
    const libro = await prisma.libro.findUnique({
      where: { id: validated.libroId }
    });

    if (!libro) {
      throw new AppError('El libro solicitado no existe', 404);
    }

    if (libro.formato === FormatoLibro.DIGITAL) {
      throw new AppError('Este libro es 100% digital y no requiere solicitud física. Puedes leerlo y descargarlo directamente.', 400);
    }

    if (libro.ejemplaresDisponibles <= 0) {
      throw new AppError('No hay ejemplares físicos disponibles en estante actualmente', 400);
    }

    // 4. Obtener datos del estudiante
    const estudiante = await prisma.usuario.findUnique({
      where: { id: usuarioId }
    });

    if (!estudiante) {
      throw new AppError('Estudiante no encontrado', 404);
    }

    // 5. Crear la solicitud en estado PENDIENTE
    return prisma.solicitudPrestamo.create({
      data: {
        usuarioId,
        libroId: validated.libroId,
        codigoEstudiante: estudiante.codigo,
        nombreEstudiante: `${estudiante.nombre} ${estudiante.apellido}`,
        carreraEstudiante: estudiante.carrera || 'Pregrado UNAMAD',
        telefonoEstudiante: estudiante.telefono || 'Sin teléfono',
        diasSolicitados: validated.diasSolicitados,
        notas: validated.notas?.trim() || null,
        estado: EstadoSolicitud.PENDIENTE
      },
      include: {
        libro: {
          select: {
            id: true,
            titulo: true,
            autor: true,
            categoria: true,
            portadaUrl: true,
            ubicacion: true
          }
        }
      }
    });
  }

  /**
   * Estudiante consulta sus propias solicitudes
   */
  async listarMisSolicitudes(usuarioId: number) {
    return prisma.solicitudPrestamo.findMany({
      where: { usuarioId },
      include: {
        libro: {
          select: {
            id: true,
            titulo: true,
            autor: true,
            categoria: true,
            portadaUrl: true,
            ubicacion: true
          }
        }
      },
      orderBy: { fechaSolicitud: 'desc' }
    });
  }

  /**
   * Bibliotecario lista solicitudes generales (Pendientes o Historial)
   */
  async listarSolicitudes(estado?: EstadoSolicitud) {
    const where: any = {};
    if (estado) {
      where.estado = estado;
    }

    return prisma.solicitudPrestamo.findMany({
      where,
      include: {
        libro: {
          select: {
            id: true,
            titulo: true,
            autor: true,
            categoria: true,
            portadaUrl: true,
            ubicacion: true,
            ejemplaresDisponibles: true,
            ejemplaresTotales: true
          }
        },
        usuario: {
          select: {
            id: true,
            codigo: true,
            nombre: true,
            apellido: true,
            carrera: true,
            telefono: true,
            email: true
          }
        }
      },
      orderBy: { fechaSolicitud: 'desc' }
    });
  }

  /**
   * Bibliotecario aprueba la solicitud:
   * 1. Cambia estado a APROBADA
   * 2. Descuenta stock físico disponible del libro
   * 3. Crea automáticamente el préstamo activo en la tabla `prestamos`
   */
  async aprobarSolicitud(solicitudId: number) {
    const solicitud = await prisma.solicitudPrestamo.findUnique({
      where: { id: solicitudId },
      include: { libro: true, usuario: true }
    });

    if (!solicitud) {
      throw new AppError('Solicitud de préstamo no encontrada', 404);
    }

    if (solicitud.estado !== EstadoSolicitud.PENDIENTE) {
      throw new AppError(`La solicitud ya fue procesada anteriormente (Estado: ${solicitud.estado})`, 400);
    }

    if (solicitud.libro.ejemplaresDisponibles <= 0) {
      throw new AppError('No hay ejemplares disponibles en estante para aprobar este préstamo', 400);
    }

    const now = new Date();
    const dayMillis = 24 * 60 * 60 * 1000;
    const fechaEstimada = new Date(now.getTime() + solicitud.diasSolicitados * dayMillis);

    // Transacción atómica
    return prisma.$transaction(async (tx) => {
      // 1. Actualizar solicitud
      const solicitudActualizada = await tx.solicitudPrestamo.update({
        where: { id: solicitudId },
        data: {
          estado: EstadoSolicitud.APROBADA,
          fechaRespuesta: now
        }
      });

      // 2. Decrementar stock físico disponible
      await tx.libro.update({
        where: { id: solicitud.libroId },
        data: {
          ejemplaresDisponibles: { decrement: 1 }
        }
      });

      // 3. Crear el préstamo formal
      const nuevoPrestamo = await tx.prestamo.create({
        data: {
          solicitudId: solicitud.id,
          usuarioId: solicitud.usuarioId,
          libroId: solicitud.libroId,
          nombreLector: `${solicitud.usuario.nombre} ${solicitud.usuario.apellido}`,
          codigoLector: solicitud.usuario.codigo,
          carreraOArea: solicitud.usuario.carrera || 'Pregrado',
          telefonoLector: solicitud.usuario.telefono || 'Sin teléfono',
          fechaPrestamo: now,
          fechaDevolucionEstimada: fechaEstimada,
          estado: EstadoPrestamo.ACTIVO,
          notas: solicitud.notas || 'Préstamo aprobado vía solicitud web/móvil.'
        }
      });

      return {
        solicitud: solicitudActualizada,
        prestamo: nuevoPrestamo
      };
    });
  }

  /**
   * Bibliotecario rechaza la solicitud registrando motivo obligatorio
   */
  async rechazarSolicitud(solicitudId: number, motivoRechazo: string) {
    if (!motivoRechazo || motivoRechazo.trim().length < 3) {
      throw new AppError('El motivo de rechazo es obligatorio para conocimiento del estudiante', 400);
    }

    const solicitud = await prisma.solicitudPrestamo.findUnique({
      where: { id: solicitudId }
    });

    if (!solicitud) {
      throw new AppError('Solicitud de préstamo no encontrada', 404);
    }

    if (solicitud.estado !== EstadoSolicitud.PENDIENTE) {
      throw new AppError(`La solicitud ya fue procesada anteriormente (Estado: ${solicitud.estado})`, 400);
    }

    return prisma.solicitudPrestamo.update({
      where: { id: solicitudId },
      data: {
        estado: EstadoSolicitud.RECHAZADA,
        fechaRespuesta: new Date(),
        motivoRechazo: motivoRechazo.trim()
      }
    });
  }

  /**
   * Estudiante cancela su propia solicitud si aún está pendiente
   */
  async cancelarSolicitud(solicitudId: number, usuarioId: number) {
    const solicitud = await prisma.solicitudPrestamo.findUnique({
      where: { id: solicitudId }
    });

    if (!solicitud) {
      throw new AppError('Solicitud no encontrada', 404);
    }

    if (solicitud.usuarioId !== usuarioId) {
      throw new AppError('No tienes permisos para cancelar esta solicitud', 403);
    }

    if (solicitud.estado !== EstadoSolicitud.PENDIENTE) {
      throw new AppError('Solo se pueden cancelar solicitudes en estado PENDIENTE', 400);
    }

    return prisma.solicitudPrestamo.update({
      where: { id: solicitudId },
      data: {
        estado: EstadoSolicitud.CANCELADA,
        fechaRespuesta: new Date()
      }
    });
  }
}
