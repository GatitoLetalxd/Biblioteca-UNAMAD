import { prisma } from '../config/prisma';
import { FormatoLibro } from '@prisma/client';
import { AppError } from '../middlewares/error.middleware';
import { z } from 'zod';

export const libroFiltrosSchema = z.object({
  search: z.string().optional(),
  categoria: z.string().optional(),
  formato: z.enum([FormatoLibro.FISICO, FormatoLibro.DIGITAL, FormatoLibro.HIBRIDO]).optional(),
  soloDisponibles: z.enum(['true', 'false']).optional(),
  page: z.string().optional().transform((val) => (val ? parseInt(val, 10) : 1)),
  limit: z.string().optional().transform((val) => (val ? parseInt(val, 10) : 20))
});

export type LibroFiltrosDTO = z.infer<typeof libroFiltrosSchema>;

export const crearLibroManualSchema = z.object({
  titulo: z.string().min(2, 'El título es obligatorio'),
  autor: z.string().min(2, 'El autor es obligatorio'),
  categoria: z.string().min(2, 'La categoría académica es obligatoria'),
  isbn: z.string().min(3, 'El ISBN o código bibliográfico es obligatorio'),
  formato: z.enum([FormatoLibro.FISICO, FormatoLibro.DIGITAL, FormatoLibro.HIBRIDO]).default(FormatoLibro.FISICO),
  ejemplaresTotales: z.number().int().min(1).default(1),
  ejemplaresDisponibles: z.number().int().min(0).default(1),
  ubicacion: z.string().default('Estante General UNAMAD'),
  archivoUrl: z.string().optional(),
  archivoNombre: z.string().optional(),
  sha256: z.string().optional(),
  tamanoMb: z.number().optional(),
  portadaUrl: z.string().optional(),
  resumen: z.string().optional(),
  year: z.string().optional()
});

export type CrearLibroManualDTO = z.infer<typeof crearLibroManualSchema>;

export class LibrosService {
  /**
   * Catálogo con búsqueda textual, filtros por categoría/formato y paginación
   */
  async listarLibros(filtros: LibroFiltrosDTO) {
    const { search, categoria, formato, soloDisponibles, page = 1, limit = 20 } = filtros;
    const skip = (page - 1) * limit;

    const where: any = {};

    if (search && search.trim().length > 0) {
      const q = search.trim();
      where.OR = [
        { titulo: { contains: q, mode: 'insensitive' } },
        { autor: { contains: q, mode: 'insensitive' } },
        { categoria: { contains: q, mode: 'insensitive' } },
        { isbn: { contains: q, mode: 'insensitive' } }
      ];
    }

    if (categoria && categoria.trim().length > 0) {
      where.categoria = { equals: categoria.trim(), mode: 'insensitive' };
    }

    if (formato) {
      where.formato = formato;
    }

    if (soloDisponibles === 'true') {
      where.OR = [
        { formato: FormatoLibro.DIGITAL },
        { ejemplaresDisponibles: { gt: 0 } }
      ];
    }

    const [total, libros] = await Promise.all([
      prisma.libro.count({ where }),
      prisma.libro.findMany({
        where,
        skip,
        take: limit,
        orderBy: { id: 'asc' }
      })
    ]);

    return {
      total,
      pagina: page,
      limite: limit,
      totalPaginas: Math.ceil(total / limit),
      libros
    };
  }

  /**
   * Obtener detalle completo de un libro por su ID
   */
  async obtenerPorId(id: number) {
    const libro = await prisma.libro.findUnique({
      where: { id },
      include: {
        prestamos: {
          where: { estado: { in: ['ACTIVO', 'VENCIDO'] } },
          select: {
            id: true,
            nombreLector: true,
            codigoLector: true,
            carreraOArea: true,
            fechaDevolucionEstimada: true,
            estado: true
          }
        }
      }
    });

    if (!libro) {
      throw new AppError('Libro no encontrado en el acervo bibliográfico', 404);
    }

    return libro;
  }

  /**
   * Registrar libro con archivo PDF referenciado y metadatos
   */
  async registrarLibroConArchivo(
    datos: {
      titulo: string;
      autor: string;
      categoria: string;
      isbn: string;
      formato?: FormatoLibro;
      ejemplaresTotales?: number;
      ubicacion?: string;
      resumen?: string;
      year?: string;
    },
    archivo: {
      filename: string;
      sha256: string;
      tamanoMb: number;
    }
  ) {
    const isbnExiste = await prisma.libro.findUnique({
      where: { isbn: datos.isbn.trim() }
    });
    if (isbnExiste) {
      throw new AppError('El ISBN o código de libro ya existe en el catálogo', 409);
    }

    const formato = datos.formato || FormatoLibro.DIGITAL;
    const ejemplaresTotales = datos.ejemplaresTotales ? Number(datos.ejemplaresTotales) : 1;

    return prisma.libro.create({
      data: {
        titulo: datos.titulo.trim(),
        autor: datos.autor.trim(),
        categoria: datos.categoria.trim(),
        isbn: datos.isbn.trim(),
        formato,
        ejemplaresTotales,
        ejemplaresDisponibles: ejemplaresTotales,
        ubicacion: datos.ubicacion?.trim() || 'Repositorio Digital UNAMAD',
        archivoUrl: `/storage/libros/${archivo.filename}`,
        archivoNombre: archivo.filename,
        sha256: archivo.sha256,
        tamanoMb: archivo.tamanoMb,
        resumen: datos.resumen?.trim() || 'Recurso bibliográfico digital.',
        year: datos.year?.trim() || '2024'
      }
    });
  }

  /**
   * Crear libro manual (para libros físicos)
   */
  async crearLibroManual(dto: CrearLibroManualDTO) {
    const validated = crearLibroManualSchema.parse(dto);

    const isbnExiste = await prisma.libro.findUnique({
      where: { isbn: validated.isbn.trim() }
    });
    if (isbnExiste) {
      throw new AppError('El ISBN ya existe en el catálogo', 409);
    }

    return prisma.libro.create({
      data: {
        titulo: validated.titulo.trim(),
        autor: validated.autor.trim(),
        categoria: validated.categoria.trim(),
        isbn: validated.isbn.trim(),
        formato: validated.formato,
        ejemplaresTotales: validated.ejemplaresTotales,
        ejemplaresDisponibles: validated.ejemplaresDisponibles,
        ubicacion: validated.ubicacion.trim(),
        archivoUrl: validated.archivoUrl || '/storage/libros/referencia_fisica.pdf',
        archivoNombre: validated.archivoNombre || 'referencia_fisica.pdf',
        sha256: validated.sha256 || '0000000000000000000000000000000000000000000000000000000000000000',
        tamanoMb: validated.tamanoMb || 0,
        portadaUrl: validated.portadaUrl,
        resumen: validated.resumen,
        year: validated.year || '2024'
      }
    });
  }

  /**
   * Modificar datos o inventario de un libro existente
   */
  async actualizarLibro(id: number, datos: Partial<CrearLibroManualDTO>) {
    const libro = await prisma.libro.findUnique({ where: { id } });
    if (!libro) {
      throw new AppError('Libro no encontrado', 404);
    }

    return prisma.libro.update({
      where: { id },
      data: {
        titulo: datos.titulo?.trim(),
        autor: datos.autor?.trim(),
        categoria: datos.categoria?.trim(),
        formato: datos.formato,
        ejemplaresTotales: datos.ejemplaresTotales,
        ejemplaresDisponibles: datos.ejemplaresDisponibles,
        ubicacion: datos.ubicacion?.trim(),
        resumen: datos.resumen?.trim(),
        year: datos.year?.trim()
      }
    });
  }

  /**
   * Eliminar un libro del acervo bibliográfico
   */
  async eliminarLibro(id: number) {
    const libro = await prisma.libro.findUnique({ where: { id } });
    if (!libro) {
      throw new AppError('Libro no encontrado', 404);
    }

    // Verificar si tiene préstamos activos
    const prestamosActivos = await prisma.prestamo.count({
      where: { libroId: id, estado: { in: ['ACTIVO', 'VENCIDO'] } }
    });

    if (prestamosActivos > 0) {
      throw new AppError('No se puede eliminar el libro: Existen ejemplares físicos prestados a estudiantes actualmente', 400);
    }

    return prisma.libro.delete({ where: { id } });
  }

  /**
   * Listar todas las categorías disponibles en el acervo
   */
  async obtenerCategorias() {
    const categorias = await prisma.libro.findMany({
      select: { categoria: true },
      distinct: ['categoria'],
      orderBy: { categoria: 'asc' }
    });
    return categorias.map((c) => c.categoria);
  }
}
