import { prisma } from '../config/prisma';
import { EstadoPrestamo, FormatoLibro, EstadoMulta, EstadoSolicitud } from '@prisma/client';

export class ReportesService {
  /**
   * Métricas y KPIs globales para el Dashboard del Bibliotecario
   */
  async getDashboard() {
    const now = new Date();

    const [
      totalTitulos,
      librosFisicos,
      librosDigitales,
      prestamosActivos,
      prestamosVencidos,
      totalDevueltos,
      solicitudesPendientes,
      multasPendientes
    ] = await Promise.all([
      prisma.libro.count(),
      prisma.libro.findMany({
        where: { formato: { not: FormatoLibro.DIGITAL } },
        select: { ejemplaresTotales: true, ejemplaresDisponibles: true }
      }),
      prisma.libro.count({
        where: { formato: { in: [FormatoLibro.DIGITAL, FormatoLibro.HIBRIDO] } }
      }),
      prisma.prestamo.count({
        where: { estado: EstadoPrestamo.ACTIVO }
      }),
      prisma.prestamo.count({
        where: {
          estado: EstadoPrestamo.ACTIVO,
          fechaDevolucionEstimada: { lt: now }
        }
      }),
      prisma.prestamo.count({
        where: { estado: EstadoPrestamo.DEVUELTO }
      }),
      prisma.solicitudPrestamo.count({
        where: { estado: EstadoSolicitud.PENDIENTE }
      }),
      prisma.multa.findMany({
        where: { estado: EstadoMulta.PENDIENTE },
        select: { montoTotal: true }
      })
    ]);

    const totalEjemplaresFisicos = librosFisicos.reduce((acc, l) => acc + l.ejemplaresTotales, 0);
    const totalDisponiblesFisicos = librosFisicos.reduce((acc, l) => acc + l.ejemplaresDisponibles, 0);
    const totalMontoMultasPendientes = multasPendientes.reduce((acc, m) => acc + Number(m.montoTotal), 0);

    return {
      totalTitulos,
      ejemplaresFisicos: {
        totales: totalEjemplaresFisicos,
        enEstante: totalDisponiblesFisicos,
        enManosDeLectores: totalEjemplaresFisicos - totalDisponiblesFisicos
      },
      librosDigitales,
      prestamos: {
        activos: prestamosActivos,
        vencidos: prestamosVencidos,
        alDia: prestamosActivos - prestamosVencidos,
        historicoDevueltos: totalDevueltos
      },
      solicitudesPendientes,
      multasPendientes: {
        cantidad: multasPendientes.length,
        montoTotal: parseFloat(totalMontoMultasPendientes.toFixed(2))
      }
    };
  }

  /**
   * Panel de Auditoría de Base de Datos e Inventario
   * Lista todos los libros con los lectores específicos que tienen ejemplares físicos en su poder
   */
  async getInventarioConLectores() {
    const now = new Date();

    const [libros, prestamosActivos] = await Promise.all([
      prisma.libro.findMany({
        orderBy: { id: 'asc' },
        select: {
          id: true,
          titulo: true,
          autor: true,
          categoria: true,
          isbn: true,
          formato: true,
          ejemplaresTotales: true,
          ejemplaresDisponibles: true,
          ubicacion: true,
          portadaUrl: true
        }
      }),
      prisma.prestamo.findMany({
        where: { estado: EstadoPrestamo.ACTIVO },
        select: {
          id: true,
          libroId: true,
          nombreLector: true,
          codigoLector: true,
          carreraOArea: true,
          telefonoLector: true,
          fechaPrestamo: true,
          fechaDevolucionEstimada: true
        }
      })
    ]);

    const librosConLectores = libros.map((libro) => {
      const lectores = prestamosActivos
        .filter((p) => p.libroId === libro.id)
        .map((p) => ({
          prestamoId: p.id,
          nombreLector: p.nombreLector,
          codigoLector: p.codigoLector,
          carrera: p.carreraOArea,
          telefono: p.telefonoLector,
          fechaPrestamo: p.fechaPrestamo,
          fechaDevolucionEstimada: p.fechaDevolucionEstimada,
          esVencido: now > p.fechaDevolucionEstimada
        }));

      return {
        ...libro,
        ejemplaresPrestados: lectores.length,
        enPosesionDeLectores: lectores.length > 0,
        lectoresActivos: lectores
      };
    });

    const todosLectoresEnPosesion = prestamosActivos.map((p) => {
      const libroAsoc = libros.find((l) => l.id === p.libroId);
      return {
        prestamoId: p.id,
        nombreLector: p.nombreLector,
        codigoLector: p.codigoLector,
        carrera: p.carreraOArea,
        telefono: p.telefonoLector,
        libroId: p.libroId,
        tituloLibro: libroAsoc?.titulo || 'Desconocido',
        autorLibro: libroAsoc?.autor || 'Desconocido',
        fechaPrestamo: p.fechaPrestamo,
        fechaDevolucionEstimada: p.fechaDevolucionEstimada,
        esVencido: now > p.fechaDevolucionEstimada
      };
    });

    return {
      metricas: {
        totalTitulos: libros.length,
        totalEjemplaresFisicos: libros.reduce((acc, l) => acc + (l.formato !== 'DIGITAL' ? l.ejemplaresTotales : 0), 0),
        totalEnEstante: libros.reduce((acc, l) => acc + (l.formato !== 'DIGITAL' ? l.ejemplaresDisponibles : 0), 0),
        totalEnLectores: prestamosActivos.length
      },
      libros: librosConLectores,
      lectoresEnPosesion: todosLectoresEnPosesion
    };
  }

  /**
   * Reportes Estadísticos Institucionales UNAMAD
   */
  async getReporteInstitucional() {
    const [
      totalPrestamosHistoricos,
      prestamosDevueltos,
      todasLasMultas,
      prestamosAgrupadosLibro,
      prestamosAgrupadosCarrera
    ] = await Promise.all([
      prisma.prestamo.count(),
      prisma.prestamo.findMany({
        where: { estado: EstadoPrestamo.DEVUELTO },
        select: { fechaDevolucionEstimada: true, fechaDevolucionReal: true }
      }),
      prisma.multa.findMany({
        select: { montoTotal: true, estado: true }
      }),
      prisma.prestamo.groupBy({
        by: ['libroId'],
        _count: { libroId: true },
        orderBy: { _count: { libroId: 'desc' } },
        take: 5
      }),
      prisma.prestamo.groupBy({
        by: ['carreraOArea'],
        _count: { carreraOArea: true },
        orderBy: { _count: { carreraOArea: 'desc' } }
      })
    ]);

    // Tasa de devolución a tiempo
    let tasaDevolucion = 100;
    if (prestamosDevueltos.length > 0) {
      const aTiempo = prestamosDevueltos.filter(
        (p) => p.fechaDevolucionReal && p.fechaDevolucionReal <= p.fechaDevolucionEstimada
      ).length;
      tasaDevolucion = parseFloat(((aTiempo / prestamosDevueltos.length) * 100).toFixed(1));
    }

    // Multas recaudadas y condonadas
    const totalRecaudadoMultas = todasLasMultas
      .filter((m) => m.estado === EstadoMulta.PAGADA)
      .reduce((acc, m) => acc + Number(m.montoTotal), 0);

    const totalMultasCondonadas = todasLasMultas
      .filter((m) => m.estado === EstadoMulta.CONDONADA)
      .reduce((acc, m) => acc + Number(m.montoTotal), 0);

    // Ranking de libros
    const topLibrosIds = prestamosAgrupadosLibro.map((g) => g.libroId);
    const librosInfo = await prisma.libro.findMany({
      where: { id: { in: topLibrosIds } },
      select: { id: true, titulo: true, autor: true, categoria: true, portadaUrl: true }
    });

    const topLibros = prestamosAgrupadosLibro.map((g) => {
      const libro = librosInfo.find((l) => l.id === g.libroId);
      return {
        libroId: g.libroId,
        titulo: libro?.titulo || 'Libro',
        autor: libro?.autor || 'Autor',
        categoria: libro?.categoria || 'General',
        portadaUrl: libro?.portadaUrl,
        totalPrestamos: g._count.libroId
      };
    });

    // Desglose por carrera
    const totalCirculacion = totalPrestamosHistoricos > 0 ? totalPrestamosHistoricos : 1;
    const topCarreras = prestamosAgrupadosCarrera.map((c) => ({
      carrera: c.carreraOArea,
      totalPrestamos: c._count.carreraOArea,
      porcentaje: parseFloat((c._count.carreraOArea / totalCirculacion).toFixed(2))
    }));

    return {
      tasaDevolucionATiempo: tasaDevolucion,
      totalPrestamosHistoricos,
      recaudacionMultas: {
        totalRecaudado: parseFloat(totalRecaudadoMultas.toFixed(2)),
        totalCondonado: parseFloat(totalMultasCondonadas.toFixed(2))
      },
      topLibros,
      topCarreras
    };
  }
}
