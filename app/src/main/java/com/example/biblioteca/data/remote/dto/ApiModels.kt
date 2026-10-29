package com.example.biblioteca.data.remote.dto

import com.google.gson.annotations.SerializedName

// ---------------------------------------------------------------------
// ENVOLTORIO GENÉRICO DE RESPUESTA DEL BACKEND
// ---------------------------------------------------------------------

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("errores") val errores: List<Any>? = null
)

// ---------------------------------------------------------------------
// AUTENTICACIÓN Y USUARIOS
// ---------------------------------------------------------------------

data class LoginRequest(
    @SerializedName("codigo") val codigo: String,
    @SerializedName("password") val password: String
)

data class RegisterEstudianteRequest(
    @SerializedName("codigo") val codigo: String,
    @SerializedName("password") val password: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("carrera") val carrera: String,
    @SerializedName("telefono") val telefono: String
)

data class AuthDataResponse(
    @SerializedName("usuario") val usuario: UsuarioRemoteDto,
    @SerializedName("token") val token: String
)

data class UsuarioRemoteDto(
    @SerializedName("id") val id: Long,
    @SerializedName("codigo") val codigo: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("carrera") val carrera: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("email") val email: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("activo") val activo: Boolean
)

// ---------------------------------------------------------------------
// LIBROS Y CATÁLOGO
// ---------------------------------------------------------------------

data class LibroListResponse(
    @SerializedName("total") val total: Int,
    @SerializedName("pagina") val pagina: Int,
    @SerializedName("limite") val limite: Int,
    @SerializedName("totalPaginas") val totalPaginas: Int,
    @SerializedName("libros") val libros: List<LibroRemoteDto>
)

data class LibroRemoteDto(
    @SerializedName("id") val id: Long,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("autor") val autor: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("isbn") val isbn: String,
    @SerializedName("formato") val formato: String,
    @SerializedName("ejemplaresTotales") val ejemplaresTotales: Int,
    @SerializedName("ejemplaresDisponibles") val ejemplaresDisponibles: Int,
    @SerializedName("ubicacion") val ubicacion: String,
    @SerializedName("archivoUrl") val archivoUrl: String,
    @SerializedName("archivoNombre") val archivoNombre: String,
    @SerializedName("sha256") val sha256: String,
    @SerializedName("tamanoMb") val tamanoMb: Double,
    @SerializedName("portadaUrl") val portadaUrl: String? = null,
    @SerializedName("resumen") val resumen: String? = null,
    @SerializedName("year") val year: String? = null
)

// ---------------------------------------------------------------------
// SOLICITUDES DE PRÉSTAMO
// ---------------------------------------------------------------------

data class SolicitudRequest(
    @SerializedName("libroId") val libroId: Long,
    @SerializedName("diasSolicitados") val diasSolicitados: Int,
    @SerializedName("notas") val notas: String? = null
)

data class RechazoRequest(
    @SerializedName("motivoRechazo") val motivoRechazo: String
)

data class SolicitudRemoteDto(
    @SerializedName("id") val id: Long,
    @SerializedName("usuarioId") val usuarioId: Long,
    @SerializedName("libroId") val libroId: Long,
    @SerializedName("codigoEstudiante") val codigoEstudiante: String,
    @SerializedName("nombreEstudiante") val nombreEstudiante: String,
    @SerializedName("carreraEstudiante") val carreraEstudiante: String,
    @SerializedName("telefonoEstudiante") val telefonoEstudiante: String,
    @SerializedName("fechaSolicitud") val fechaSolicitud: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("diasSolicitados") val diasSolicitados: Int,
    @SerializedName("notas") val notas: String? = null,
    @SerializedName("fechaRespuesta") val fechaRespuesta: String? = null,
    @SerializedName("motivoRechazo") val motivoRechazo: String? = null,
    @SerializedName("libro") val libro: LibroRemoteSimpleDto? = null
)

data class LibroRemoteSimpleDto(
    @SerializedName("id") val id: Long,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("autor") val autor: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("portadaUrl") val portadaUrl: String? = null,
    @SerializedName("ubicacion") val ubicacion: String? = null,
    @SerializedName("ejemplaresDisponibles") val ejemplaresDisponibles: Int? = null,
    @SerializedName("ejemplaresTotales") val ejemplaresTotales: Int? = null
)

// ---------------------------------------------------------------------
// PRÉSTAMOS Y CIRCULACIÓN
// ---------------------------------------------------------------------

data class PrestamoDirectoRequest(
    @SerializedName("libroId") val libroId: Long,
    @SerializedName("codigoLector") val codigoLector: String,
    @SerializedName("nombreLector") val nombreLector: String,
    @SerializedName("carreraOArea") val carreraOArea: String,
    @SerializedName("telefonoLector") val telefonoLector: String? = null,
    @SerializedName("diasPrestamo") val diasPrestamo: Int = 7,
    @SerializedName("notas") val notas: String? = null
)

data class PrestamoRemoteDto(
    @SerializedName("id") val id: Long,
    @SerializedName("solicitudId") val solicitudId: Long? = null,
    @SerializedName("usuarioId") val usuarioId: Long,
    @SerializedName("libroId") val libroId: Long,
    @SerializedName("nombreLector") val nombreLector: String,
    @SerializedName("codigoLector") val codigoLector: String,
    @SerializedName("carreraOArea") val carreraOArea: String,
    @SerializedName("telefonoLector") val telefonoLector: String? = null,
    @SerializedName("fechaPrestamo") val fechaPrestamo: String,
    @SerializedName("fechaDevolucionEstimada") val fechaDevolucionEstimada: String,
    @SerializedName("fechaDevolucionReal") val fechaDevolucionReal: String? = null,
    @SerializedName("estado") val estado: String,
    @SerializedName("notas") val notas: String? = null,
    @SerializedName("libro") val libro: LibroRemoteSimpleDto? = null
)

data class DevolucionResponseData(
    @SerializedName("prestamo") val prestamo: PrestamoRemoteDto,
    @SerializedName("multa") val multa: MultaRemoteDto? = null,
    @SerializedName("mensaje") val mensaje: String
)

// ---------------------------------------------------------------------
// MULTAS Y SOLVENCIA
// ---------------------------------------------------------------------

data class MultaRemoteDto(
    @SerializedName("id") val id: Long,
    @SerializedName("prestamoId") val prestamoId: Long,
    @SerializedName("usuarioId") val usuarioId: Long,
    @SerializedName("libroId") val libroId: Long,
    @SerializedName("codigoEstudiante") val codigoEstudiante: String,
    @SerializedName("nombreEstudiante") val nombreEstudiante: String,
    @SerializedName("tituloLibro") val tituloLibro: String,
    @SerializedName("diasRetraso") val diasRetraso: Int,
    @SerializedName("montoTotal") val montoTotal: Double,
    @SerializedName("estado") val estado: String,
    @SerializedName("fechaGeneracion") val fechaGeneracion: String,
    @SerializedName("fechaPago") val fechaPago: String? = null,
    @SerializedName("observaciones") val observaciones: String? = null
)

data class SolvenciaRemoteData(
    @SerializedName("esSolvente") val esSolvente: Boolean,
    @SerializedName("totalMultasPendientes") val totalMultasPendientes: Int,
    @SerializedName("deudaTotal") val deudaTotal: Double,
    @SerializedName("multas") val multas: List<MultaRemoteDto>,
    @SerializedName("mensaje") val mensaje: String
)

data class CondonarRequest(
    @SerializedName("motivo") val motivo: String
)

// ---------------------------------------------------------------------
// REPORTES Y DASHBOARD
// ---------------------------------------------------------------------

data class DashboardRemoteData(
    @SerializedName("totalTitulos") val totalTitulos: Int,
    @SerializedName("ejemplaresFisicos") val ejemplaresFisicos: EjemplaresFisicosDto,
    @SerializedName("librosDigitales") val librosDigitales: Int,
    @SerializedName("prestamos") val prestamos: PrestamosStatsDto,
    @SerializedName("solicitudesPendientes") val solicitudesPendientes: Int,
    @SerializedName("multasPendientes") val multasPendientes: MultasStatsDto
)

data class EjemplaresFisicosDto(
    @SerializedName("totales") val totales: Int,
    @SerializedName("enEstante") val enEstante: Int,
    @SerializedName("enManosDeLectores") val enManosDeLectores: Int
)

data class PrestamosStatsDto(
    @SerializedName("activos") val activos: Int,
    @SerializedName("vencidos") val vencidos: Int,
    @SerializedName("alDia") val alDia: Int,
    @SerializedName("historicoDevueltos") val historicoDevueltos: Int
)

data class MultasStatsDto(
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("montoTotal") val montoTotal: Double
)

data class InventarioLectoresRemoteData(
    @SerializedName("metricas") val metricas: InventarioMetricasDto,
    @SerializedName("libros") val libros: List<LibroConLectoresDto>,
    @SerializedName("lectoresEnPosesion") val lectoresEnPosesion: List<LectorEnPosesionDto>
)

data class InventarioMetricasDto(
    @SerializedName("totalTitulos") val totalTitulos: Int,
    @SerializedName("totalEjemplaresFisicos") val totalEjemplaresFisicos: Int,
    @SerializedName("totalEnEstante") val totalEnEstante: Int,
    @SerializedName("totalEnLectores") val totalEnLectores: Int
)

data class LibroConLectoresDto(
    @SerializedName("id") val id: Long,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("autor") val autor: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("isbn") val isbn: String,
    @SerializedName("formato") val formato: String,
    @SerializedName("ejemplaresTotales") val ejemplaresTotales: Int,
    @SerializedName("ejemplaresDisponibles") val ejemplaresDisponibles: Int,
    @SerializedName("ubicacion") val ubicacion: String,
    @SerializedName("portadaUrl") val portadaUrl: String? = null,
    @SerializedName("ejemplaresPrestados") val ejemplaresPrestados: Int,
    @SerializedName("enPosesionDeLectores") val enPosesionDeLectores: Boolean,
    @SerializedName("lectoresActivos") val lectoresActivos: List<LectorActivoDto>
)

data class LectorActivoDto(
    @SerializedName("prestamoId") val prestamoId: Long,
    @SerializedName("nombreLector") val nombreLector: String,
    @SerializedName("codigoLector") val codigoLector: String,
    @SerializedName("carrera") val carrera: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("fechaPrestamo") val fechaPrestamo: String,
    @SerializedName("fechaDevolucionEstimada") val fechaDevolucionEstimada: String,
    @SerializedName("esVencido") val esVencido: Boolean
)

data class LectorEnPosesionDto(
    @SerializedName("prestamoId") val prestamoId: Long,
    @SerializedName("nombreLector") val nombreLector: String,
    @SerializedName("codigoLector") val codigoLector: String,
    @SerializedName("carrera") val carrera: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("libroId") val libroId: Long,
    @SerializedName("tituloLibro") val tituloLibro: String,
    @SerializedName("autorLibro") val autorLibro: String,
    @SerializedName("fechaPrestamo") val fechaPrestamo: String,
    @SerializedName("fechaDevolucionEstimada") val fechaDevolucionEstimada: String,
    @SerializedName("esVencido") val esVencido: Boolean
)

data class ReporteInstitucionalRemoteData(
    @SerializedName("tasaDevolucionATiempo") val tasaDevolucionATiempo: Float,
    @SerializedName("totalPrestamosHistoricos") val totalPrestamosHistoricos: Int,
    @SerializedName("recaudacionMultas") val recaudacionMultas: RecaudacionMultasDto,
    @SerializedName("topLibros") val topLibros: List<TopLibroDto>,
    @SerializedName("topCarreras") val topCarreras: List<TopCarreraDto>
)

data class RecaudacionMultasDto(
    @SerializedName("totalRecaudado") val totalRecaudado: Double,
    @SerializedName("totalCondonado") val totalCondonado: Double
)

data class TopLibroDto(
    @SerializedName("libroId") val libroId: Long,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("autor") val autor: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("portadaUrl") val portadaUrl: String? = null,
    @SerializedName("totalPrestamos") val totalPrestamos: Int
)

data class TopCarreraDto(
    @SerializedName("carrera") val carrera: String,
    @SerializedName("totalPrestamos") val totalPrestamos: Int,
    @SerializedName("porcentaje") val porcentaje: Float
)
