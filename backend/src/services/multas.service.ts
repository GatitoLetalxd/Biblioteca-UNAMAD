import { prisma } from '../config/prisma';
import { EstadoMulta } from '@prisma/client';
import { AppError } from '../middlewares/error.middleware';

export class MultasService {
  /**
   * Listar multas generales para el bibliotecario
   */
  async listarMultas(estado?: EstadoMulta) {
    const where: any = {};
    if (estado) {
      where.estado = estado;
    }

    const multas = await prisma.multa.findMany({
      where,
      include: {
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
        },
        libro: {
          select: {
            id: true,
            titulo: true,
            autor: true,
            portadaUrl: true
          }
        }
      },
      orderBy: { fechaGeneracion: 'desc' }
    });

    const totalMonto = multas.reduce((acc, m) => acc + Number(m.montoTotal), 0);

    return {
      totalRegistros: multas.length,
      totalMonto: parseFloat(totalMonto.toFixed(2)),
      multas
    };
  }

  /**
   * Consulta de solvencia y deuda acumulada para el estudiante (Carnet Digital)
   */
  async consultarMiSolvencia(usuarioId: number) {
    const multasPendientes = await prisma.multa.findMany({
      where: {
        usuarioId,
        estado: EstadoMulta.PENDIENTE
      },
      include: {
        libro: {
          select: {
            id: true,
            titulo: true,
            autor: true
          }
        }
      },
      orderBy: { fechaGeneracion: 'desc' }
    });

    const deudaTotal = multasPendientes.reduce((acc, m) => acc + Number(m.montoTotal), 0);
    const esSolvente = multasPendientes.length === 0;

    return {
      esSolvente,
      totalMultasPendientes: multasPendientes.length,
      deudaTotal: parseFloat(deudaTotal.toFixed(2)),
      multas: multasPendientes,
      mensaje: esSolvente
        ? 'Cuenta al día. Tienes derecho a solicitar préstamos físicos y digitales.'
        : `Tienes S/. ${deudaTotal.toFixed(2)} en multas pendientes por retraso en devolución. Cancela en ventanilla para reactivar tu cuenta.`
    };
  }

  /**
   * Registrar cobro en efectivo de multa en ventanilla de biblioteca
   */
  async cobrarMulta(multaId: number) {
    const multa = await prisma.multa.findUnique({
      where: { id: multaId }
    });

    if (!multa) {
      throw new AppError('Multa no encontrada', 404);
    }

    if (multa.estado === EstadoMulta.PAGADA) {
      throw new AppError('Esta multa ya fue cobrada y cancelada previamente', 400);
    }

    if (multa.estado === EstadoMulta.CONDONADA) {
      throw new AppError('Esta multa fue condonada anteriormente y no puede cobrarse', 400);
    }

    return prisma.multa.update({
      where: { id: multaId },
      data: {
        estado: EstadoMulta.PAGADA,
        fechaPago: new Date(),
        observaciones: multa.observaciones
          ? `${multa.observaciones} | Cobrado en ventanilla de biblioteca.`
          : 'Cobrado en ventanilla de biblioteca.'
      }
    });
  }

  /**
   * Condonar multa administrativamente con motivo obligatorio
   */
  async condonarMulta(multaId: number, motivo: string) {
    if (!motivo || motivo.trim().length < 4) {
      throw new AppError('Debe ingresar un motivo o número de resolución que justifique la condonación', 400);
    }

    const multa = await prisma.multa.findUnique({
      where: { id: multaId }
    });

    if (!multa) {
      throw new AppError('Multa no encontrada', 404);
    }

    if (multa.estado === EstadoMulta.PAGADA) {
      throw new AppError('No se puede condonar una multa que ya fue cobrada y registrada como pagada', 400);
    }

    return prisma.multa.update({
      where: { id: multaId },
      data: {
        estado: EstadoMulta.CONDONADA,
        fechaPago: new Date(),
        observaciones: `Condonación autorizada por administración. Motivo: ${motivo.trim()}`
      }
    });
  }
}
