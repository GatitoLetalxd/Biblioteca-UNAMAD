package com.example.biblioteca.model

data class Usuario(
    val id: Long = 0,
    val codigo: String,
    val password: String = "",
    val nombre: String,
    val apellidos: String,
    val carreraOArea: String = "",
    val telefono: String = "",
    val email: String = "",
    val rol: RolUsuario = RolUsuario.ESTUDIANTE,
    val activo: Boolean = true,
    val fechaRegistro: Long = System.currentTimeMillis()
) {
    val nombreCompleto: String get() = "$nombre $apellidos".trim()

    val iniciales: String get() {
        val p1 = nombre.firstOrNull()?.uppercaseChar() ?: 'U'
        val p2 = apellidos.firstOrNull()?.uppercaseChar() ?: 'N'
        return "$p1$p2"
    }

    val esBibliotecario: Boolean get() = rol == RolUsuario.BIBLIOTECARIO
    val esEstudiante: Boolean get() = rol == RolUsuario.ESTUDIANTE
}

data class Libro(
    val id: Long = 0,
    val titulo: String,
    val autor: String,
    val categoria: String,
    val isbn: String = "",
    val descripcion: String = "",
    val formato: FormatoLibro = FormatoLibro.FISICO,
    val ejemplaresTotales: Int = 1,
    val ejemplaresDisponibles: Int = 1,
    val ubicacion: String = "",
    val archivoUrl: String = "",
    val tamanoMb: Double = 0.0,
    val portadaUrl: String = "",
    val fechaRegistro: Long = System.currentTimeMillis()
)

data class SolicitudPrestamo(
    val id: Long = 0,
    val usuarioId: Long = 0,
    val libroId: Long,
    val codigoEstudiante: String,
    val nombreEstudiante: String,
    val carreraEstudiante: String = "",
    val telefonoEstudiante: String = "",
    val tituloLibro: String = "",
    val autorLibro: String = "",
    val fechaSolicitud: Long = System.currentTimeMillis(),
    val estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE,
    val motivoRechazo: String? = null,
    val fechaRespuesta: Long? = null,
    val diasSolicitados: Int = 7,
    val notas: String = ""
)

data class Prestamo(
    val id: Long = 0,
    val libroId: Long,
    val tituloLibro: String = "",
    val autorLibro: String = "",
    val nombreLector: String,
    val codigoLector: String,
    val carreraOArea: String = "",
    val telefonoLector: String = "",
    val fechaPrestamo: Long = System.currentTimeMillis(),
    val fechaDevolucionEstimada: Long,
    val fechaDevolucionReal: Long? = null,
    val estado: EstadoPrestamo = EstadoPrestamo.ACTIVO,
    val notas: String = ""
)

data class Multa(
    val id: Long = 0,
    val prestamoId: Long,
    val usuarioId: Long = 0,
    val codigoEstudiante: String,
    val nombreEstudiante: String,
    val tituloLibro: String,
    val diasRetraso: Int,
    val montoTotal: Double,
    val estado: EstadoMulta = EstadoMulta.PENDIENTE,
    val fechaGeneracion: Long = System.currentTimeMillis(),
    val fechaPago: Long? = null,
    val observaciones: String = ""
)

data class BibliotecaStats(
    val totalTitulos: Int = 0,
    val totalEjemplaresFisicos: Int = 0,
    val totalDisponiblesFisicos: Int = 0,
    val totalLibrosDigitales: Int = 0,
    val prestamosActivos: Int = 0,
    val prestamosVencidos: Int = 0,
    val totalDevueltos: Int = 0,
    val solicitudesPendientes: Int = 0,
    val totalMultasPendientesCount: Int = 0,
    val totalMontoMultasPendientes: Double = 0.0
)

data class LibroPopular(
    val libroId: Long,
    val titulo: String,
    val autor: String,
    val categoria: String,
    val totalPrestamos: Int
)

data class CarreraStats(
    val carrera: String,
    val totalPrestamos: Int,
    val porcentaje: Float = 0f
)

data class ReporteInstitucional(
    val totalLibros: Int = 0,
    val totalEjemplares: Int = 0,
    val totalPrestamosHistoricos: Int = 0,
    val tasaDevolucionATiempo: Int = 100, // porcentaje 0-100
    val totalRecaudadoMultas: Double = 0.0,
    val totalMultasCondonadas: Double = 0.0,
    val topLibros: List<LibroPopular> = emptyList(),
    val topCarreras: List<CarreraStats> = emptyList()
)

data class LibroConCirculacion(
    val libro: Libro,
    val prestamosFisicosActivos: List<Prestamo> = emptyList()
) {
    val ejemplaresPrestados: Int get() = prestamosFisicosActivos.size
    val enPosesionDeLectores: Boolean get() = prestamosFisicosActivos.isNotEmpty()
}


