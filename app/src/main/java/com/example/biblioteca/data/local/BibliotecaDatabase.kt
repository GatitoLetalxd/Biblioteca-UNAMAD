package com.example.biblioteca.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.biblioteca.data.local.dao.LibroDao
import com.example.biblioteca.data.local.dao.MultaDao
import com.example.biblioteca.data.local.dao.PrestamoDao
import com.example.biblioteca.data.local.dao.SolicitudPrestamoDao
import com.example.biblioteca.data.local.dao.UsuarioDao
import com.example.biblioteca.data.local.entity.LibroEntity
import com.example.biblioteca.data.local.entity.MultaEntity
import com.example.biblioteca.data.local.entity.PrestamoEntity
import com.example.biblioteca.data.local.entity.SolicitudPrestamoEntity
import com.example.biblioteca.data.local.entity.UsuarioEntity
import com.example.biblioteca.model.EstadoMulta
import com.example.biblioteca.model.EstadoSolicitud
import com.example.biblioteca.model.FormatoLibro
import com.example.biblioteca.model.RolUsuario

@Database(
    entities = [
        LibroEntity::class,
        PrestamoEntity::class,
        UsuarioEntity::class,
        SolicitudPrestamoEntity::class,
        MultaEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class BibliotecaDatabase : RoomDatabase() {

    abstract fun libroDao(): LibroDao
    abstract fun prestamoDao(): PrestamoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun solicitudPrestamoDao(): SolicitudPrestamoDao
    abstract fun multaDao(): MultaDao

    companion object {
        @Volatile
        private var INSTANCE: BibliotecaDatabase? = null

        fun getDatabase(context: Context): BibliotecaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BibliotecaDatabase::class.java,
                    "biblioteca_unamad_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(
            libroDao: LibroDao,
            prestamoDao: PrestamoDao,
            usuarioDao: UsuarioDao,
            solicitudPrestamoDao: SolicitudPrestamoDao,
            multaDao: MultaDao
        ) {
            // Usuarios iniciales demo con roles asignados
            val initialUsers = listOf(
                UsuarioEntity(
                    id = 1,
                    codigo = "BIBLIO-01",
                    password = "admin123",
                    nombre = "Prof. Ronald",
                    apellidos = "Vásquez Arce",
                    carreraOArea = "Dirección de Biblioteca Central UNAMAD",
                    telefono = "982001122",
                    email = "biblioteca@unamad.edu.pe",
                    rol = RolUsuario.BIBLIOTECARIO.name,
                    activo = true
                ),
                UsuarioEntity(
                    id = 2,
                    codigo = "2023-04510",
                    password = "alumno123",
                    nombre = "Jhordy",
                    apellidos = "Pérez Ramos",
                    carreraOArea = "Ingeniería de Sistemas e Informática",
                    telefono = "982123456",
                    email = "202304510@unamad.edu.pe",
                    rol = RolUsuario.ESTUDIANTE.name,
                    activo = true
                ),
                UsuarioEntity(
                    id = 3,
                    codigo = "2022-01205",
                    password = "alumno123",
                    nombre = "Lucía",
                    apellidos = "Mendoza Ríos",
                    carreraOArea = "Ingeniería Forestal y Medio Ambiente",
                    telefono = "974556677",
                    email = "202201205@unamad.edu.pe",
                    rol = RolUsuario.ESTUDIANTE.name,
                    activo = true
                )
            )
            usuarioDao.insertAll(initialUsers)

            // Limpiar catálogo previo para garantizar exactamente los 7 títulos oficiales
            libroDao.deleteAllLibros()

            // Catálogo inicial de libros
            val initialBooks = listOf(
                LibroEntity(
                    id = 1,
                    titulo = "Ingeniería de Software: Un Enfoque Práctico",
                    autor = "Roger S. Pressman",
                    categoria = "Ingeniería de Sistemas",
                    isbn = "978-607-15-0314-5",
                    descripcion = "Texto clásico y de referencia fundamental sobre procesos, metodologías ágiles y calidad en desarrollo de software.",
                    formato = FormatoLibro.HIBRIDO.name,
                    ejemplaresTotales = 5,
                    ejemplaresDisponibles = 4,
                    ubicacion = "Estante A-12",
                    archivoUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    tamanoMb = 14.5,
                    portadaUrl = "portada_pressman"
                ),
                LibroEntity(
                    id = 2,
                    titulo = "Ecosistemas Amazónicos y Manejo Forestal",
                    autor = "Dr. Carlos Valdivia UNAMAD",
                    categoria = "Ingeniería Forestal",
                    isbn = "978-612-4589-12-0",
                    descripcion = "Investigación sobre la biodiversidad, conservación y aprovechamiento sostenible en la cuenca de Madre de Dios.",
                    formato = FormatoLibro.DIGITAL.name,
                    ejemplaresTotales = 1,
                    ejemplaresDisponibles = 1,
                    ubicacion = "Repositorio Digital",
                    archivoUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    tamanoMb = 8.2,
                    portadaUrl = "portada_ecosistemas"
                ),
                LibroEntity(
                    id = 3,
                    titulo = "Estructuras de Datos y Algoritmos en Java",
                    autor = "Robert Lafore",
                    categoria = "Ingeniería de Sistemas",
                    isbn = "978-0672324536",
                    descripcion = "Guía didáctica y completa sobre listas, árboles, grafos, algoritmos de ordenamiento y complejidad algorítmica.",
                    formato = FormatoLibro.FISICO.name,
                    ejemplaresTotales = 4,
                    ejemplaresDisponibles = 2,
                    ubicacion = "Estante A-08",
                    portadaUrl = "portada_lafore"
                ),
                LibroEntity(
                    id = 4,
                    titulo = "Bases de Datos: Diseño e Implementación Relacional",
                    autor = "Abraham Silberschatz & Henry Korth",
                    categoria = "Ingeniería de Sistemas",
                    isbn = "978-0073523323",
                    descripcion = "Conceptos de bases de datos relacionales, SQL avanzado, álgebra relacional y normalización.",
                    formato = FormatoLibro.FISICO.name,
                    ejemplaresTotales = 3,
                    ejemplaresDisponibles = 3,
                    ubicacion = "Estante B-03",
                    portadaUrl = "portada_silberschatz"
                ),
                LibroEntity(
                    id = 5,
                    titulo = "Biodiversidad y Fauna de Madre de Dios",
                    autor = "Instituto de Investigación UNAMAD",
                    categoria = "Medio Ambiente",
                    isbn = "978-612-4589-33-5",
                    descripcion = "Catálogo exhaustivo de especies endémicas y preservación en el parque nacional del Manu y Tambopata.",
                    formato = FormatoLibro.HIBRIDO.name,
                    ejemplaresTotales = 2,
                    ejemplaresDisponibles = 1,
                    ubicacion = "Estante C-01",
                    archivoUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    tamanoMb = 22.0,
                    portadaUrl = "portada_biodiversidad"
                ),
                LibroEntity(
                    id = 6,
                    titulo = "Cien Años de Soledad",
                    autor = "Gabriel García Márquez",
                    categoria = "Literatura",
                    isbn = "978-0307474728",
                    descripcion = "Obra cumbre del realismo mágico hispanoamericano y premio Nobel de Literatura.",
                    formato = FormatoLibro.FISICO.name,
                    ejemplaresTotales = 6,
                    ejemplaresDisponibles = 5,
                    ubicacion = "Estante L-04",
                    portadaUrl = "portada_marquez"
                ),
                LibroEntity(
                    id = 7,
                    titulo = "Metodología de la Investigación Científica",
                    autor = "Roberto Hernández Sampieri",
                    categoria = "Investigación",
                    isbn = "978-1456223960",
                    descripcion = "Guía esencial para tesis de pregrado y posgrado con enfoques cuantitativo, cualitativo y mixto.",
                    formato = FormatoLibro.HIBRIDO.name,
                    ejemplaresTotales = 8,
                    ejemplaresDisponibles = 7,
                    ubicacion = "Estante I-02",
                    archivoUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    tamanoMb = 18.4,
                    portadaUrl = "portada_sampieri"
                )
            )

            libroDao.insertAll(initialBooks)

            val now = System.currentTimeMillis()
            val dayMillis = 24L * 60 * 60 * 1000

            // Préstamos de demostración inicial
            prestamoDao.insertPrestamo(
                PrestamoEntity(
                    libroId = 1,
                    nombreLector = "Jhordy Pérez Ramos",
                    codigoLector = "2023-04510",
                    carreraOArea = "Ingeniería de Sistemas e Informática",
                    telefonoLector = "982123456",
                    fechaPrestamo = now - (3 * dayMillis),
                    fechaDevolucionEstimada = now + (4 * dayMillis),
                    estado = "ACTIVO",
                    notas = "Préstamo regular para proyecto académico."
                )
            )
            prestamoDao.insertPrestamo(
                PrestamoEntity(
                    libroId = 3,
                    nombreLector = "Lucía Mendoza Ríos",
                    codigoLector = "2022-01205",
                    carreraOArea = "Ingeniería Forestal y Medio Ambiente",
                    telefonoLector = "974556677",
                    fechaPrestamo = now - (10 * dayMillis),
                    fechaDevolucionEstimada = now - (2 * dayMillis),
                    estado = "ACTIVO",
                    notas = "Examen de estructuras de datos."
                )
            )

            // Solicitudes de préstamo iniciales para pruebas
            solicitudPrestamoDao.insertSolicitud(
                SolicitudPrestamoEntity(
                    usuarioId = 3,
                    libroId = 4,
                    codigoEstudiante = "2022-01205",
                    nombreEstudiante = "Lucía Mendoza Ríos",
                    carreraEstudiante = "Ingeniería Forestal y Medio Ambiente",
                    telefonoEstudiante = "974556677",
                    fechaSolicitud = now - (1 * dayMillis),
                    estado = EstadoSolicitud.PENDIENTE.name,
                    diasSolicitados = 7,
                    notas = "Solicito el libro para el taller de diseño relacional y modelado de datos forestales."
                )
            )

            solicitudPrestamoDao.insertSolicitud(
                SolicitudPrestamoEntity(
                    usuarioId = 2,
                    libroId = 1,
                    codigoEstudiante = "2023-04510",
                    nombreEstudiante = "Jhordy Pérez Ramos",
                    carreraEstudiante = "Ingeniería de Sistemas e Informática",
                    telefonoEstudiante = "982123456",
                    fechaSolicitud = now - (3 * dayMillis),
                    fechaRespuesta = now - (3 * dayMillis),
                    estado = EstadoSolicitud.APROBADA.name,
                    diasSolicitados = 7,
                    notas = "Solicitud aprobada por ventanilla central."
                )
            )

            // Multa inicial de prueba (Lucía tiene 2 días de retraso en libro #3)
            multaDao.insertMulta(
                MultaEntity(
                    prestamoId = 2,
                    usuarioId = 3,
                    codigoEstudiante = "2022-01205",
                    nombreEstudiante = "Lucía Mendoza Ríos",
                    tituloLibro = "Estructuras de Datos y Algoritmos en Java",
                    diasRetraso = 2,
                    montoTotal = 3.00, // 2 días * S/. 1.50
                    estado = EstadoMulta.PENDIENTE.name,
                    fechaGeneracion = now - (2 * dayMillis),
                    observaciones = "Generada por 2 días de retraso en fecha límite de devolución."
                )
            )
        }
    }
}
