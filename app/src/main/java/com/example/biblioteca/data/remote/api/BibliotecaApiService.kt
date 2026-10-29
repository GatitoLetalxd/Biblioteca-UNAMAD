package com.example.biblioteca.data.remote.api

import com.example.biblioteca.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface BibliotecaApiService {

    // -----------------------------------------------------------------
    // AUTENTICACIÓN Y USUARIOS
    // -----------------------------------------------------------------

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterEstudianteRequest
    ): Response<ApiResponse<AuthDataResponse>>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthDataResponse>>

    @GET("auth/me")
    suspend fun getMe(): Response<ApiResponse<UsuarioRemoteDto>>

    // -----------------------------------------------------------------
    // CATÁLOGO DE LIBROS
    // -----------------------------------------------------------------

    @GET("libros")
    suspend fun getLibros(
        @Query("search") search: String? = null,
        @Query("categoria") categoria: String? = null,
        @Query("formato") formato: String? = null,
        @Query("soloDisponibles") soloDisponibles: String? = null,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 100
    ): Response<ApiResponse<LibroListResponse>>

    @GET("libros/categorias")
    suspend fun getCategorias(): Response<ApiResponse<List<String>>>

    @GET("libros/{id}")
    suspend fun getLibroById(
        @Path("id") id: Long
    ): Response<ApiResponse<LibroRemoteDto>>

    // -----------------------------------------------------------------
    // SOLICITUDES DE PRÉSTAMO FÍSICO
    // -----------------------------------------------------------------

    @POST("solicitudes")
    suspend fun crearSolicitud(
        @Body request: SolicitudRequest
    ): Response<ApiResponse<SolicitudRemoteDto>>

    @GET("solicitudes/mis-solicitudes")
    suspend fun getMisSolicitudes(): Response<ApiResponse<List<SolicitudRemoteDto>>>

    @GET("solicitudes")
    suspend fun getSolicitudes(
        @Query("estado") estado: String? = null
    ): Response<ApiResponse<List<SolicitudRemoteDto>>>

    @PUT("solicitudes/{id}/aprobar")
    suspend fun aprobarSolicitud(
        @Path("id") id: Long
    ): Response<ApiResponse<Any>>

    @PUT("solicitudes/{id}/rechazar")
    suspend fun rechazarSolicitud(
        @Path("id") id: Long,
        @Body request: RechazoRequest
    ): Response<ApiResponse<Any>>

    @DELETE("solicitudes/{id}/cancelar")
    suspend fun cancelarSolicitud(
        @Path("id") id: Long
    ): Response<ApiResponse<Any>>

    // -----------------------------------------------------------------
    // PRÉSTAMOS Y CIRCULACIÓN
    // -----------------------------------------------------------------

    @GET("prestamos")
    suspend fun getPrestamos(
        @Query("estado") estado: String? = null
    ): Response<ApiResponse<List<PrestamoRemoteDto>>>

    @GET("prestamos/mis-prestamos")
    suspend fun getMisPrestamos(): Response<ApiResponse<List<PrestamoRemoteDto>>>

    @POST("prestamos")
    suspend fun crearPrestamoDirecto(
        @Body request: PrestamoDirectoRequest
    ): Response<ApiResponse<PrestamoRemoteDto>>

    @PUT("prestamos/{id}/devolver")
    suspend fun devolverPrestamo(
        @Path("id") id: Long
    ): Response<ApiResponse<DevolucionResponseData>>

    // -----------------------------------------------------------------
    // MULTAS Y SOLVENCIA
    // -----------------------------------------------------------------

    @GET("multas/mi-solvencia")
    suspend fun getMiSolvencia(): Response<ApiResponse<SolvenciaRemoteData>>

    @GET("multas")
    suspend fun getMultas(
        @Query("estado") estado: String? = null
    ): Response<ApiResponse<Any>>

    @PUT("multas/{id}/cobrar")
    suspend fun cobrarMulta(
        @Path("id") id: Long
    ): Response<ApiResponse<Any>>

    @PUT("multas/{id}/condonar")
    suspend fun condonarMulta(
        @Path("id") id: Long,
        @Body request: CondonarRequest
    ): Response<ApiResponse<Any>>

    // -----------------------------------------------------------------
    // REPORTES Y DASHBOARD
    // -----------------------------------------------------------------

    @GET("reportes/dashboard")
    suspend fun getDashboard(): Response<ApiResponse<DashboardRemoteData>>

    @GET("reportes/inventario-lectores")
    suspend fun getInventarioLectores(): Response<ApiResponse<InventarioLectoresRemoteData>>

    @GET("reportes/institucional")
    suspend fun getReporteInstitucional(): Response<ApiResponse<ReporteInstitucionalRemoteData>>
}
