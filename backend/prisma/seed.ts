import { PrismaClient, RolUsuario, FormatoLibro, EstadoPrestamo, EstadoMulta } from '@prisma/client';
import * as bcrypt from 'bcryptjs';
import * as fs from 'fs';
import * as path from 'path';

const prisma = new PrismaClient();

async function main() {
  console.log('======================================================================');
  console.log(' INICIANDO SEEDING DE BASE DE DATOS — BIBLIOTECA CENTRAL UNAMAD');
  console.log('======================================================================');

  // 1. Limpieza de datos previa (orden de dependencias)
  await prisma.multa.deleteMany();
  await prisma.prestamo.deleteMany();
  await prisma.solicitudPrestamo.deleteMany();
  await prisma.libro.deleteMany();
  await prisma.usuario.deleteMany();

  console.log('[1/4] Base de datos limpia.');

  // 2. Creación de Usuarios Iniciales
  const passwordHashAdmin = await bcrypt.hash('admin123', 10);
  const passwordHashBiblio = await bcrypt.hash('biblio123', 10);
  const passwordHashAlumno = await bcrypt.hash('alumno123', 10);

  // 2.1 Superusuario Administrador
  const admin = await prisma.usuario.create({
    data: {
      codigo: 'ADMIN-01',
      password: passwordHashAdmin,
      nombre: 'Ronald',
      apellido: 'Vásquez',
      carrera: 'Dirección de Biblioteca Central',
      telefono: '982001122',
      email: 'admin@unamad.edu.pe',
      rol: RolUsuario.ADMIN,
      activo: true
    }
  });

  // 2.2 Bibliotecario de Ventanilla
  const bibliotecario = await prisma.usuario.create({
    data: {
      codigo: 'BIBLIO-01',
      password: passwordHashBiblio,
      nombre: 'Carlos',
      apellido: 'Gutiérrez',
      carrera: 'Atención en Sala y Circulación',
      telefono: '982334455',
      email: 'biblioteca@unamad.edu.pe',
      rol: RolUsuario.BIBLIOTECARIO,
      activo: true
    }
  });

  // 2.3 Estudiantes con código de 8 dígitos
  const estudiante1 = await prisma.usuario.create({
    data: {
      codigo: '20230451', // 8 dígitos numéricos
      password: passwordHashAlumno,
      nombre: 'Jhordy',
      apellido: 'Pérez',
      carrera: 'Ingeniería de Sistemas e Informática',
      telefono: '982123456',
      email: '20230451@unamad.edu.pe',
      rol: RolUsuario.ESTUDIANTE,
      activo: true
    }
  });

  const estudiante2 = await prisma.usuario.create({
    data: {
      codigo: '20220120', // 8 dígitos numéricos
      password: passwordHashAlumno,
      nombre: 'Lucía',
      apellido: 'Mendoza',
      carrera: 'Ingeniería Forestal y Medio Ambiente',
      telefono: '974556677',
      email: '20220120@unamad.edu.pe',
      rol: RolUsuario.ESTUDIANTE,
      activo: true
    }
  });

  console.log('[2/4] Usuarios creados: 1 ADMIN, 1 BIBLIOTECARIO, 2 ESTUDIANTES (8 dígitos).');

  // 3. Inserción de los 100 Libros Digitales desde catalogo_100.json
  const catalogoPath = path.join(__dirname, 'catalogo_100.json');
  let librosCargados = 0;

  if (fs.existsSync(catalogoPath)) {
    const rawData = fs.readFileSync(catalogoPath, 'utf-8');
    const librosData = JSON.parse(rawData);

    for (const item of librosData) {
      await prisma.libro.create({
        data: {
          id: item.id,
          titulo: item.titulo,
          autor: item.autor,
          categoria: item.categoria,
          isbn: item.isbn,
          formato: FormatoLibro.DIGITAL,
          ejemplaresTotales: 1,
          ejemplaresDisponibles: 1,
          ubicacion: item.ubicacion_estante || 'Repositorio Digital UNAMAD',
          archivoUrl: item.fuente_relativa || `/storage/libros/${item.archivo}`,
          archivoNombre: item.archivo,
          sha256: item.sha256,
          tamanoMb: item.tamano_mb,
          resumen: item.resumen,
          year: item.year || '2024'
        }
      });
      librosCargados++;
    }
  }

  // Asegurar los 7 libros físicos/híbridos oficiales con stock físico en estante
  const librosFisicos = [
    {
      titulo: 'Ingeniería de Software: Un Enfoque Práctico',
      autor: 'Roger S. Pressman',
      categoria: 'Ingeniería de Sistemas',
      isbn: '978-607-15-0314-5',
      formato: FormatoLibro.HIBRIDO,
      ejemplaresTotales: 5,
      ejemplaresDisponibles: 4,
      ubicacion: 'Estante A-12',
      archivoUrl: '/storage/libros/01_lit_Prohibido_Enamorarse.pdf',
      archivoNombre: '01_lit_Prohibido_Enamorarse.pdf',
      sha256: '852ea52b0452b98c365d6f42dd0e71fadc084ef27628916c993e78185e3ed66e',
      tamanoMb: 14.5,
      portadaUrl: 'portada_pressman',
      resumen: 'Texto clásico y de referencia fundamental sobre procesos, metodologías ágiles y calidad en desarrollo de software.'
    },
    {
      titulo: 'Ecosistemas Amazónicos y Manejo Forestal',
      autor: 'Dr. Carlos Valdivia UNAMAD',
      categoria: 'Ingeniería Forestal',
      isbn: '978-612-4589-12-0',
      formato: FormatoLibro.DIGITAL,
      ejemplaresTotales: 1,
      ejemplaresDisponibles: 1,
      ubicacion: 'Repositorio Digital',
      archivoUrl: '/storage/libros/02_lit_Entre_Visillos_Carmen_Martin_Gaite.pdf',
      archivoNombre: '02_lit_Entre_Visillos_Carmen_Martin_Gaite.pdf',
      sha256: '9797fc1440c4ec1a142193b1613a68276161f2c1a8cc61fe718b18fd2e6c1caa',
      tamanoMb: 8.2,
      portadaUrl: 'portada_ecosistemas',
      resumen: 'Investigación sobre la biodiversidad, conservación y aprovechamiento sostenible en la cuenca de Madre de Dios.'
    },
    {
      titulo: 'Estructuras de Datos y Algoritmos en Java',
      autor: 'Robert Lafore',
      categoria: 'Ingeniería de Sistemas',
      isbn: '978-0672324536',
      formato: FormatoLibro.FISICO,
      ejemplaresTotales: 4,
      ejemplaresDisponibles: 2,
      ubicacion: 'Estante A-08',
      archivoUrl: '/storage/libros/03_lit_El_Extranjero_Albert_Camus.pdf',
      archivoNombre: '03_lit_El_Extranjero_Albert_Camus.pdf',
      sha256: '40e5f0ef8b943e9b36e093d691d2eb7bc7e937d713fb88b597595354a7f2b754',
      tamanoMb: 6.8,
      portadaUrl: 'portada_lafore',
      resumen: 'Guía didáctica y completa sobre listas, árboles, grafos, algoritmos de ordenamiento y complejidad algorítmica.'
    }
  ];

  for (const f of librosFisicos) {
    await prisma.libro.upsert({
      where: { isbn: f.isbn },
      update: {},
      create: f
    });
  }

  console.log(`[3/4] Libros catalogados: ${librosCargados} digitales + libros físicos oficiales.`);

  // 4. Préstamos y Solicitudes de Demostración Iniciales
  const now = new Date();
  const dayMillis = 24 * 60 * 60 * 1000;

  // Préstamo 1: Jhordy (Al día)
  const libro1 = await prisma.libro.findFirst({ where: { isbn: '978-607-15-0314-5' } });
  if (libro1) {
    await prisma.prestamo.create({
      data: {
        usuarioId: estudiante1.id,
        libroId: libro1.id,
        nombreLector: `${estudiante1.nombre} ${estudiante1.apellido}`,
        codigoLector: estudiante1.codigo,
        carreraOArea: estudiante1.carrera || 'Ingeniería de Sistemas',
        telefonoLector: estudiante1.telefono || '982123456',
        fechaPrestamo: new Date(now.getTime() - 3 * dayMillis),
        fechaDevolucionEstimada: new Date(now.getTime() + 4 * dayMillis),
        estado: EstadoPrestamo.ACTIVO,
        notas: 'Préstamo regular para proyecto académico.'
      }
    });
  }

  // Préstamo 2: Lucía (Vencido por 2 días -> para probar el cobro de mora)
  const libro3 = await prisma.libro.findFirst({ where: { isbn: '978-0672324536' } });
  if (libro3) {
    const prestamoVencido = await prisma.prestamo.create({
      data: {
        usuarioId: estudiante2.id,
        libroId: libro3.id,
        nombreLector: `${estudiante2.nombre} ${estudiante2.apellido}`,
        codigoLector: estudiante2.codigo,
        carreraOArea: estudiante2.carrera || 'Ingeniería Forestal',
        telefonoLector: estudiante2.telefono || '974556677',
        fechaPrestamo: new Date(now.getTime() - 10 * dayMillis),
        fechaDevolucionEstimada: new Date(now.getTime() - 2 * dayMillis),
        estado: EstadoPrestamo.VENCIDO,
        notas: 'Examen de estructuras de datos.'
      }
    });

    // Multa inicial generada por el retraso de 2 días (S/. 1.50/día = S/. 3.00)
    await prisma.multa.create({
      data: {
        prestamoId: prestamoVencido.id,
        usuarioId: estudiante2.id,
        libroId: libro3.id,
        codigoEstudiante: estudiante2.codigo,
        nombreEstudiante: `${estudiante2.nombre} ${estudiante2.apellido}`,
        tituloLibro: libro3.titulo,
        diasRetraso: 2,
        montoTotal: 3.00,
        estado: EstadoMulta.PENDIENTE,
        fechaGeneracion: new Date(now.getTime() - 2 * dayMillis),
        observaciones: 'Generada por 2 días de retraso en fecha límite de devolución.'
      }
    });
  }

  console.log('[4/4] Préstamos y multas de prueba sembrados.');
  console.log('======================================================================');
  console.log(' SEEDING COMPLETADO EXITOSAMENTE');
  console.log(' Credenciales Demo:');
  console.log('  - ADMIN:         ADMIN-01  / admin123');
  console.log('  - BIBLIOTECARIO: BIBLIO-01 / biblio123');
  console.log('  - ESTUDIANTE 1:  20230451  / alumno123 (Jhordy Pérez)');
  console.log('  - ESTUDIANTE 2:  20220120  / alumno123 (Lucía Mendoza - Multa S/. 3.00)');
  console.log('======================================================================');
}

main()
  .catch((e) => {
    console.error('Error durante el seed:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
