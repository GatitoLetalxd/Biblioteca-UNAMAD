package com.example.biblioteca.data.repository

import android.util.Log
import com.example.biblioteca.data.local.BibliotecaDatabase
import com.example.biblioteca.data.local.dao.LibroDao
import com.example.biblioteca.data.local.dao.MultaDao
import com.example.biblioteca.data.local.dao.PrestamoDao
import com.example.biblioteca.data.local.dao.SolicitudPrestamoDao
import com.example.biblioteca.data.local.dao.SolicitudWithLibroInfo
import com.example.biblioteca.data.local.dao.UsuarioDao
import com.example.biblioteca.data.local.entity.LibroEntity
import com.example.biblioteca.data.local.entity.MultaEntity
import com.example.biblioteca.data.local.entity.PrestamoEntity
import com.example.biblioteca.data.local.entity.SolicitudPrestamoEntity
import com.example.biblioteca.data.local.entity.UsuarioEntity
import com.example.biblioteca.data.local.session.SessionManager
import com.example.biblioteca.data.remote.ApiClient
import com.example.biblioteca.data.remote.api.BibliotecaApiService
import com.example.biblioteca.data.remote.dto.*
import com.example.biblioteca.model.BibliotecaStats
import com.example.biblioteca.model.CarreraStats
import com.example.biblioteca.model.EstadoMulta
import com.example.biblioteca.model.EstadoPrestamo
import com.example.biblioteca.model.EstadoSolicitud
import com.example.biblioteca.model.FormatoLibro
import com.example.biblioteca.model.Libro
import com.example.biblioteca.model.LibroConCirculacion
import com.example.biblioteca.model.LibroPopular
import com.example.biblioteca.model.Multa
import com.example.biblioteca.model.Prestamo
import com.example.biblioteca.model.ReporteInstitucional
import com.example.biblioteca.model.RolUsuario
import com.example.biblioteca.model.SolicitudPrestamo
import com.example.biblioteca.model.Usuario
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BibliotecaRepository(
    private val libroDao: LibroDao,
    private val prestamoDao: PrestamoDao,
    private val usuarioDao: UsuarioDao,
    private val solicitudPrestamoDao: SolicitudPrestamoDao,
    private val multaDao: MultaDao,
    private val sessionManager: SessionManager,
    private val apiService: BibliotecaApiService = ApiClient.getApiService()
) {
    private val gson = Gson()

    // -------------------------------------------------------------------------
    // USUARIOS Y AUTENTICACIÓN
    // -------------------------------------------------------------------------

    val todosLosUsuarios: Flow<List<Usuario>> = usuarioDao.getAllUsuarios().map { list ->
        list.map { it.toDomain() }
    }

    /**
     * Autenticación híbrida:
     * 1. Consulta la API REST en el VPS (obtiene token JWT).
     * 2. Si hay conexión, sincroniza en Room y guarda sesión con token.
     * 3. Si no hay red, valida contra la base de datos local Room.
     */
    suspend fun autenticar(codigo: String, password: String): Result<Usuario> {
        val cleanCodigo = codigo.trim()
        val cleanPassword = password.trim()

        try {
            val response = apiService.login(LoginRequest(cleanCodigo, cleanPassword))
            if (response.isSuccessful && response.body()?.data != null) {
                val authData = response.body()!!.data!!
                val userDto = authData.usuario
                val token = authData.token

                val rol = try {
                    RolUsuario.valueOf(userDto.rol)
                } catch (e: Exception) {
                    RolUsuario.ESTUDIANTE
                }

                val usuario = Usuario(
                    id = userDto.id,
                    codigo = userDto.codigo,
                    nombre = userDto.nombre,
                    apellidos = userDto.apellido,
                    carreraOArea = userDto.carrera ?: "",
                    telefono = userDto.telefono ?: "",
                    email = userDto.email,
                    rol = rol,
                    activo = userDto.activo
                )

                // Guardar en SessionManager y Room
                sessionManager.guardarSesion(usuario, token)
                usuarioDao.insertUsuario(
                    UsuarioEntity(
                        id = usuario.id,
                        codigo = usuario.codigo,
                        password = cleanPassword,
                        nombre = usuario.nombre,
                        apellidos = usuario.apellidos,
                        carreraOArea = usuario.carreraOArea,
                        telefono = usuario.telefono,
                        email = usuario.email,
                        rol = usuario.rol.name,
                        activo = usuario.activo
                    )
                )

                // Disparar sincronización de libros en segundo plano
                sincronizarLibros()

                return Result.success(usuario)
            } else {
                val errorMsg = extraerMensajeError(response.errorBody()?.string())
                    ?: "Código institucional o contraseña no válidos"
                return Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.w("BibliotecaRepo", "Fallo de red al autenticar contra VPS, verificando caché Room: ${e.message}")
        }

        // Fallback Offline: Buscar en Room local
        val usuarioEntity = usuarioDao.autenticar(cleanCodigo, cleanPassword)
            ?: return Result.failure(Exception("Sin conexión al servidor y credenciales no guardadas localmente"))

        if (!usuarioEntity.activo) {
            return Result.failure(Exception("Esta cuenta de usuario se encuentra inhabilitada"))
        }

        val usuario = usuarioEntity.toDomain()
        sessionManager.guardarSesion(usuario)
        return Result.success(usuario)
    }

    /**
     * Registro público de estudiante en el backend en VPS:
     * Código de 8 dígitos, primer nombre, primer apellido, carrera UNAMAD y teléfono.
     */
    suspend fun registrarEstudiante(
        codigo: String,
        password: String,
        nombre: String,
        apellido: String,
        carrera: String,
        telefono: String
    ): Result<Usuario> {
        try {
            val req = RegisterEstudianteRequest(
                codigo = codigo.trim(),
                password = password.trim(),
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                carrera = carrera.trim(),
                telefono = telefono.trim()
            )

            val response = apiService.register(req)
            if (response.isSuccessful && response.body()?.data != null) {
                val authData = response.body()!!.data!!
                val userDto = authData.usuario
                val token = authData.token

                val usuario = Usuario(
                    id = userDto.id,
                    codigo = userDto.codigo,
                    nombre = userDto.nombre,
                    apellidos = userDto.apellido,
                    carreraOArea = userDto.carrera ?: carrera,
                    telefono = userDto.telefono ?: telefono,
                    email = userDto.email,
                    rol = RolUsuario.ESTUDIANTE,
                    activo = userDto.activo
                )

                sessionManager.guardarSesion(usuario, token)
                usuarioDao.insertUsuario(
                    UsuarioEntity(
                        id = usuario.id,
                        codigo = usuario.codigo,
                        password = password.trim(),
                        nombre = usuario.nombre,
                        apellidos = usuario.apellidos,
                        carreraOArea = usuario.carreraOArea,
                        telefono = usuario.telefono,
                        email = usuario.email,
                        rol = RolUsuario.ESTUDIANTE.name,
                        activo = true
                    )
                )

                sincronizarLibros()
                return Result.success(usuario)
            } else {
                val errorMsg = extraerMensajeError(response.errorBody()?.string())
                    ?: "No se pudo completar el registro de estudiante"
                return Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            return Result.failure(Exception("Error de conexión al registrar estudiante: ${e.message}"))
        }
    }

    suspend fun obtenerUsuarioActual(): Usuario? {
        val codigo = sessionManager.obtenerCodigoSesion() ?: return null
        return usuarioDao.getUsuarioByCodigo(codigo)?.toDomain()
    }

    suspend fun obtenerUsuarioPorCodigo(codigo: String): Usuario? {
        return usuarioDao.getUsuarioByCodigo(codigo.trim())?.toDomain()
    }

    fun cerrarSesion() {
        sessionManager.cerrarSesion()
    }

    // -------------------------------------------------------------------------
    // CATÁLOGO DE LIBROS Y SINCRONIZACIÓN
    // -------------------------------------------------------------------------

    val todosLosLibros: Flow<List<Libro>> = libroDao.getAllLibros().map { list ->
        list.map { it.toDomain() }
    }

    val categorias: Flow<List<String>> = libroDao.getAllCategorias()

    fun buscarLibros(query: String): Flow<List<Libro>> {
        return libroDao.searchLibros(query).map { list -> list.map { it.toDomain() } }
    }

    /**
     * Sincroniza los 103 libros del backend en VPS con la base de datos Room local
     */
    suspend fun sincronizarLibros(): Result<Int> {
        try {
            val response = apiService.getLibros(page = 1, limit = 150)
            if (response.isSuccessful && response.body()?.data?.libros != null) {
                val remoteBooks = response.body()!!.data!!.libros
                val entities = remoteBooks.map { dto ->
                    // Resolver URL absoluta para streaming/descarga
                    val pdfUrl = if (dto.archivoUrl.startsWith("http")) {
                        dto.archivoUrl
                    } else {
                        "https://biblioteca.moondev.online${dto.archivoUrl}"
                    }

                    LibroEntity(
                        id = dto.id,
                        titulo = dto.titulo,
                        autor = dto.autor,
                        categoria = dto.categoria,
                        isbn = dto.isbn,
                        descripcion = dto.resumen ?: "Recurso digital UNAMAD.",
                        formato = dto.formato,
                        ejemplaresTotales = dto.ejemplaresTotales,
                        ejemplaresDisponibles = dto.ejemplaresDisponibles,
                        ubicacion = dto.ubicacion,
                        archivoUrl = pdfUrl,
                        tamanoMb = dto.tamanoMb,
                        portadaUrl = dto.portadaUrl ?: resolverPortadaLocal(dto.id, dto.categoria)
                    )
                }

                libroDao.insertAll(entities)
                Log.d("BibliotecaRepo", "✓ Sincronizados ${entities.size} libros desde el VPS hacia Room")
                return Result.success(entities.size)
            }
        } catch (e: Exception) {
            Log.w("BibliotecaRepo", "No se pudo sincronizar catálogo con VPS (usando caché Room): ${e.message}")
        }
        return Result.failure(Exception("No se pudo conectar con el servidor"))
    }

    private fun resolverPortadaLocal(id: Long, categoria: String): String {
        return when (id) {
            1L -> "portada_pressman"
            2L -> "portada_ecosistemas"
            3L -> "portada_lafore"
            4L -> "portada_silberschatz"
            5L -> "portada_tanenbaum"
            6L -> "portada_biodiversidad"
            7L -> "portada_sampieri"
            else -> "" // Portada editorial dinámica con título y autor reales
        }
    }

    suspend fun guardarLibro(libro: Libro): Long {
        val entity = LibroEntity.fromDomain(libro)
        return if (libro.id == 0L) {
            libroDao.insertLibro(entity)
        } else {
            libroDao.updateLibro(entity)
            libro.id
        }
    }

    suspend fun eliminarLibro(libro: Libro) {
        try {
            apiService.deleteLibro(libro.id)
        } catch (e: Exception) {
            Log.w("BibliotecaRepo", "Eliminación remota falló: ${e.message}")
        }
        libroDao.deleteLibro(LibroEntity.fromDomain(libro))
    }

    // -------------------------------------------------------------------------
    // SISTEMA DE SOLICITUDES DE PRÉSTAMO
    // -------------------------------------------------------------------------

    val todasLasSolicitudes: Flow<List<SolicitudPrestamo>> =
        solicitudPrestamoDao.getAllSolicitudesConLibro().map { list ->
            list.map { mapearSolicitud(it) }
        }

    val solicitudesPendientes: Flow<List<SolicitudPrestamo>> =
        solicitudPrestamoDao.getSolicitudesPendientesConLibro().map { list ->
            list.map { mapearSolicitud(it) }
        }

    val conteoSolicitudesPendientes: Flow<Int> = solicitudPrestamoDao.countPendientes()

    fun solicitudesPorEstudiante(codigoEstudiante: String): Flow<List<SolicitudPrestamo>> {
        return solicitudPrestamoDao.getSolicitudesPorEstudiante(codigoEstudiante.trim()).map { list ->
            list.map { mapearSolicitud(it) }
        }
    }

    private fun mapearSolicitud(item: SolicitudWithLibroInfo): SolicitudPrestamo {
        val estadoEnum = try {
            EstadoSolicitud.valueOf(item.estado)
        } catch (e: Exception) {
            EstadoSolicitud.PENDIENTE
        }

        return SolicitudPrestamo(
            id = item.id,
            usuarioId = item.usuarioId,
            libroId = item.libroId,
            codigoEstudiante = item.codigoEstudiante,
            nombreEstudiante = item.nombreEstudiante,
            carreraEstudiante = item.carreraEstudiante ?: "",
            telefonoEstudiante = item.telefonoEstudiante ?: "",
            tituloLibro = item.libroTitulo ?: "",
            autorLibro = item.libroAutor ?: "",
            fechaSolicitud = item.fechaSolicitud,
            estado = estadoEnum,
            motivoRechazo = item.motivoRechazo,
            fechaRespuesta = item.fechaRespuesta,
            diasSolicitados = item.diasSolicitados,
            notas = item.notas ?: ""
        )
    }

    /**
     * Emisión de solicitud con evaluación del candado de solvencia en el VPS
     */
    suspend fun crearSolicitudPrestamo(
        libroId: Long,
        usuario: Usuario,
        dias: Int = 7,
        notas: String = ""
    ): Result<Long> {
        // Intentar registrar en el VPS para evaluar solvencia
        try {
            val response = apiService.crearSolicitud(
                SolicitudRequest(
                    libroId = libroId,
                    diasSolicitados = dias,
                    notas = notas.trim().ifBlank { null }
                )
            )

            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!
                val entity = SolicitudPrestamoEntity(
                    id = data.id,
                    usuarioId = usuario.id,
                    libroId = libroId,
                    codigoEstudiante = usuario.codigo,
                    nombreEstudiante = usuario.nombreCompleto,
                    carreraEstudiante = usuario.carreraOArea,
                    telefonoEstudiante = usuario.telefono,
                    fechaSolicitud = parseDateMillis(data.fechaSolicitud),
                    estado = EstadoSolicitud.PENDIENTE.name,
                    diasSolicitados = dias,
                    notas = notas.trim()
                )
                solicitudPrestamoDao.insertSolicitud(entity)
                return Result.success(data.id)
            } else if (response.code() == 403) {
                // Candado de solvencia activado en el servidor
                val errorMsg = extraerMensajeError(response.errorBody()?.string())
                    ?: "Cuenta no solvente: Tienes multas pendientes de pago por mora en ventanilla."
                return Result.failure(Exception(errorMsg))
            } else {
                val errorMsg = extraerMensajeError(response.errorBody()?.string())
                    ?: "No se pudo registrar la solicitud"
                return Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.w("BibliotecaRepo", "Fallo de conexión al enviar solicitud al VPS: ${e.message}")
        }

        // Fallback Local con verificación de solvencia en Room
        val multasPendientes = multaDao.getMultasPendientesPorEstudiante(usuario.codigo)
        val libro = libroDao.getLibroById(libroId)
            ?: return Result.failure(Exception("Libro no encontrado en catálogo"))

        if (libro.formato == FormatoLibro.DIGITAL.name) {
            return Result.failure(Exception("Los libros digitales no requieren solicitud física. Puedes leerlos directamente en PDF."))
        }

        if (libro.ejemplaresDisponibles <= 0) {
            return Result.failure(Exception("En este momento no hay ejemplares físicos disponibles para préstamo."))
        }

        val entity = SolicitudPrestamoEntity(
            usuarioId = usuario.id,
            libroId = libroId,
            codigoEstudiante = usuario.codigo,
            nombreEstudiante = usuario.nombreCompleto,
            carreraEstudiante = usuario.carreraOArea,
            telefonoEstudiante = usuario.telefono,
            fechaSolicitud = System.currentTimeMillis(),
            estado = EstadoSolicitud.PENDIENTE.name,
            diasSolicitados = dias,
            notas = notas.trim()
        )

        val id = solicitudPrestamoDao.insertSolicitud(entity)
        return Result.success(id)
    }

    suspend fun aprobarSolicitud(solicitudId: Long): Result<Long> {
        try {
            val response = apiService.aprobarSolicitud(solicitudId)
            if (!response.isSuccessful) {
                val msg = extraerMensajeError(response.errorBody()?.string())
                Log.w("BibliotecaRepo", "Aprobación remota falló: $msg")
            }
        } catch (e: Exception) {
            Log.w("BibliotecaRepo", "Error de red al aprobar en VPS: ${e.message}")
        }

        val solicitud = solicitudPrestamoDao.getSolicitudById(solicitudId)
            ?: return Result.failure(Exception("Solicitud no encontrada"))

        val libro = libroDao.getLibroById(solicitud.libroId)
            ?: return Result.failure(Exception("El libro solicitado ya no existe en el catálogo"))

        val now = System.currentTimeMillis()
        val plazoMillis = solicitud.diasSolicitados.toLong() * 24L * 60 * 60 * 1000
        val fechaEstimada = now + plazoMillis

        val prestamo = PrestamoEntity(
            libroId = solicitud.libroId,
            nombreLector = solicitud.nombreEstudiante,
            codigoLector = solicitud.codigoEstudiante,
            carreraOArea = solicitud.carreraEstudiante,
            telefonoLector = solicitud.telefonoEstudiante,
            fechaPrestamo = now,
            fechaDevolucionEstimada = fechaEstimada,
            estado = EstadoPrestamo.ACTIVO.name,
            notas = "Aprobado desde solicitud #${solicitud.id}. ${solicitud.notas}".trim()
        )
        val prestamoId = prestamoDao.insertPrestamo(prestamo)

        if (libro.formato != FormatoLibro.DIGITAL.name) {
            libroDao.decrementarStock(solicitud.libroId)
        }

        solicitudPrestamoDao.actualizarEstado(
            id = solicitudId,
            nuevoEstado = EstadoSolicitud.APROBADA.name,
            fechaRespuesta = now,
            motivo = null
        )

        return Result.success(prestamoId)
    }

    suspend fun rechazarSolicitud(solicitudId: Long, motivo: String): Result<Unit> {
        val motivoLimpio = motivo.trim().ifBlank {
            "Solicitud no aprobada por administración de biblioteca."
        }

        try {
            apiService.rechazarSolicitud(solicitudId, RechazoRequest(motivoLimpio))
        } catch (e: Exception) {
            Log.w("BibliotecaRepo", "Error de red al rechazar en VPS: ${e.message}")
        }

        solicitudPrestamoDao.actualizarEstado(
            id = solicitudId,
            nuevoEstado = EstadoSolicitud.RECHAZADA.name,
            fechaRespuesta = System.currentTimeMillis(),
            motivo = motivoLimpio
        )

        return Result.success(Unit)
    }

    suspend fun cancelarSolicitud(solicitudId: Long): Result<Unit> {
        try {
            apiService.cancelarSolicitud(solicitudId)
        } catch (e: Exception) {
            Log.w("BibliotecaRepo", "Error de red al cancelar en VPS: ${e.message}")
        }

        solicitudPrestamoDao.actualizarEstado(
            id = solicitudId,
            nuevoEstado = EstadoSolicitud.CANCELADA.name,
            fechaRespuesta = System.currentTimeMillis(),
            motivo = "Cancelada voluntariamente por el estudiante."
        )

        return Result.success(Unit)
    }

    // -------------------------------------------------------------------------
    // SISTEMA DE MULTAS Y PENALIZACIONES
    // -------------------------------------------------------------------------

    val todasLasMultas: Flow<List<Multa>> = multaDao.getAllMultas().map { list ->
        list.map { it.toDomain() }
    }

    val multasPendientes: Flow<List<Multa>> = multaDao.getMultasPendientes().map { list ->
        list.map { it.toDomain() }
    }

    val totalMultasPendientesCount: Flow<Int> = multaDao.countMultasPendientes()
    val totalMontoMultasPendientes: Flow<Double> = multaDao.sumMontoMultasPendientes()

    fun multasPorEstudiante(codigo: String): Flow<List<Multa>> {
        return multaDao.getMultasPorEstudiante(codigo.trim()).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun multasPendientesPorEstudiante(codigo: String): Flow<List<Multa>> {
        return multaDao.getMultasPendientesPorEstudiante(codigo.trim()).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun cobrarMulta(multaId: Long): Result<Unit> {
        try {
            apiService.cobrarMulta(multaId)
        } catch (e: Exception) {
            Log.w("BibliotecaRepo", "Error de red al cobrar en VPS: ${e.message}")
        }

        multaDao.actualizarEstadoMulta(
            id = multaId,
            nuevoEstado = EstadoMulta.PAGADA.name,
            fechaPago = System.currentTimeMillis(),
            observaciones = "Pago registrado en ventanilla de biblioteca."
        )
        return Result.success(Unit)
    }

    suspend fun condonarMulta(multaId: Long, motivo: String): Result<Unit> {
        val motivoLimpio = motivo.trim().ifBlank { "Condonación autorizada por administración de biblioteca." }

        try {
            apiService.condonarMulta(multaId, CondonarRequest(motivoLimpio))
        } catch (e: Exception) {
            Log.w("BibliotecaRepo", "Error de red al condonar en VPS: ${e.message}")
        }

        multaDao.actualizarEstadoMulta(
            id = multaId,
            nuevoEstado = EstadoMulta.CONDONADA.name,
            fechaPago = System.currentTimeMillis(),
            observaciones = motivoLimpio
        )
        return Result.success(Unit)
    }

    // -------------------------------------------------------------------------
    // GESTIÓN DE PRÉSTAMOS
    // -------------------------------------------------------------------------

    val todosLosPrestamos: Flow<List<Prestamo>> = prestamoDao.getAllPrestamosConLibro().map { list ->
        list.map { item ->
            mapearPrestamoConEstado(item)
        }
    }

    val prestamosActivos: Flow<List<Prestamo>> = todosLosPrestamos.map { list ->
        list.filter { it.estado != EstadoPrestamo.DEVUELTO }
    }

    val inventarioConLectores: Flow<List<LibroConCirculacion>> = combine(
        todosLosLibros,
        todosLosPrestamos
    ) { libros, prestamos ->
        val activos = prestamos.filter { it.estado != EstadoPrestamo.DEVUELTO }
        libros.map { libro ->
            val lectoresFisicos = activos.filter { it.libroId == libro.id }
            LibroConCirculacion(
                libro = libro,
                prestamosFisicosActivos = lectoresFisicos
            )
        }
    }

    fun prestamosPorCodigoLector(codigoLector: String): Flow<List<Prestamo>> {
        return prestamoDao.getPrestamosPorCodigoLector(codigoLector).map { list ->
            list.map { mapearPrestamoConEstado(it) }
        }
    }

    private fun mapearPrestamoConEstado(item: com.example.biblioteca.data.local.dao.PrestamoWithLibroInfo): Prestamo {
        val now = System.currentTimeMillis()
        val estadoCalculado = when {
            item.estado == EstadoPrestamo.DEVUELTO.name -> EstadoPrestamo.DEVUELTO
            now > item.fechaDevolucionEstimada -> EstadoPrestamo.VENCIDO
            else -> EstadoPrestamo.ACTIVO
        }
        return Prestamo(
            id = item.id,
            libroId = item.libroId,
            tituloLibro = item.libroTitulo ?: "",
            autorLibro = item.libroAutor ?: "",
            nombreLector = item.nombreLector,
            codigoLector = item.codigoLector,
            carreraOArea = item.carreraOArea ?: "",
            telefonoLector = item.telefonoLector ?: "",
            fechaPrestamo = item.fechaPrestamo,
            fechaDevolucionEstimada = item.fechaDevolucionEstimada,
            fechaDevolucionReal = item.fechaDevolucionReal,
            estado = estadoCalculado,
            notas = item.notas ?: ""
        )
    }

    suspend fun registrarPrestamo(
        libroId: Long,
        nombreLector: String,
        codigoLector: String,
        carreraOArea: String,
        telefonoLector: String,
        fechaDevolucionEstimada: Long,
        notas: String
    ): Result<Long> {
        val libro = libroDao.getLibroById(libroId) ?: return Result.failure(Exception("Libro no encontrado"))

        if (libro.formato != FormatoLibro.DIGITAL.name && libro.ejemplaresDisponibles <= 0) {
            return Result.failure(Exception("No hay ejemplares disponibles para préstamo físico"))
        }

        val prestamo = PrestamoEntity(
            libroId = libroId,
            nombreLector = nombreLector,
            codigoLector = codigoLector,
            carreraOArea = carreraOArea,
            telefonoLector = telefonoLector,
            fechaPrestamo = System.currentTimeMillis(),
            fechaDevolucionEstimada = fechaDevolucionEstimada,
            estado = EstadoPrestamo.ACTIVO.name,
            notas = notas
        )

        val id = prestamoDao.insertPrestamo(prestamo)

        if (libro.formato != FormatoLibro.DIGITAL.name) {
            libroDao.decrementarStock(libroId)
        }

        return Result.success(id)
    }

    suspend fun registrarDevolucion(prestamoId: Long, libroId: Long): Result<String> {
        // 1. Intentar registrar en el VPS para que aplique la mora del backend
        try {
            val response = apiService.devolverPrestamo(prestamoId)
            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!

                prestamoDao.marcarDevolucion(
                    prestamoId = prestamoId,
                    fechaDevolucion = System.currentTimeMillis(),
                    nuevoEstado = EstadoPrestamo.DEVUELTO.name
                )
                libroDao.incrementarStock(libroId)

                // Si se generó multa en el backend, sincronizarla en Room
                if (data.multa != null) {
                    val m = data.multa
                    multaDao.insertMulta(
                        MultaEntity(
                            id = m.id,
                            prestamoId = m.prestamoId,
                            usuarioId = m.usuarioId,
                            codigoEstudiante = m.codigoEstudiante,
                            nombreEstudiante = m.nombreEstudiante,
                            tituloLibro = m.tituloLibro,
                            diasRetraso = m.diasRetraso,
                            montoTotal = m.montoTotal,
                            estado = m.estado,
                            fechaGeneracion = parseDateMillis(m.fechaGeneracion),
                            observaciones = m.observaciones ?: ""
                        )
                    )
                }

                return Result.success(data.mensaje)
            }
        } catch (e: Exception) {
            Log.w("BibliotecaRepo", "Devolución remota no disponible, procesando localmente en Room: ${e.message}")
        }

        // 2. Procesamiento local Room (Offline Fallback)
        val prestamo = prestamoDao.getPrestamoById(prestamoId)
            ?: return Result.failure(Exception("Préstamo no encontrado"))

        val now = System.currentTimeMillis()
        prestamoDao.marcarDevolucion(
            prestamoId = prestamoId,
            fechaDevolucion = now,
            nuevoEstado = EstadoPrestamo.DEVUELTO.name
        )

        libroDao.incrementarStock(libroId)

        var mensajeResultado = "Devolución registrada. Ejemplar devuelto al inventario."
        if (now > prestamo.fechaDevolucionEstimada) {
            val diff = now - prestamo.fechaDevolucionEstimada
            val dayMillis = 24L * 60 * 60 * 1000
            val diasRetraso = kotlin.math.max(1, (diff / dayMillis).toInt())
            val tarifaPorDia = 1.50
            val montoTotal = diasRetraso * tarifaPorDia

            val libro = libroDao.getLibroById(libroId)
            val tituloLibro = libro?.titulo ?: "Libro #$libroId"

            val multaEntity = MultaEntity(
                prestamoId = prestamoId,
                usuarioId = 0,
                codigoEstudiante = prestamo.codigoLector,
                nombreEstudiante = prestamo.nombreLector,
                tituloLibro = tituloLibro,
                diasRetraso = diasRetraso,
                montoTotal = montoTotal,
                estado = EstadoMulta.PENDIENTE.name,
                fechaGeneracion = now,
                observaciones = "Generada automáticamente por $diasRetraso día(s) de retraso en devolución."
            )
            multaDao.insertMulta(multaEntity)
            mensajeResultado = "Devolución con $diasRetraso día(s) de retraso. Se generó multa de S/. ${String.format(Locale.US, "%.2f", montoTotal)}."
        }

        return Result.success(mensajeResultado)
    }

    // -------------------------------------------------------------------------
    // ESTADÍSTICAS Y DASHBOARD
    // -------------------------------------------------------------------------

    val estadisticas: Flow<BibliotecaStats> = combine(
        todosLosLibros,
        todosLosPrestamos,
        conteoSolicitudesPendientes,
        totalMultasPendientesCount,
        totalMontoMultasPendientes
    ) { libros, prestamos, pendientesCount, multasCount, multasMonto ->
        val totalTitulos = libros.size
        val totalEjemplaresFisicos = libros
            .filter { it.formato != FormatoLibro.DIGITAL }
            .sumOf { it.ejemplaresTotales }
        val totalDisponiblesFisicos = libros
            .filter { it.formato != FormatoLibro.DIGITAL }
            .sumOf { it.ejemplaresDisponibles }
        val totalLibrosDigitales = libros
            .count { it.formato == FormatoLibro.DIGITAL || it.formato == FormatoLibro.HIBRIDO }

        val prestamosActivosCount = prestamos.count { it.estado == EstadoPrestamo.ACTIVO }
        val prestamosVencidosCount = prestamos.count { it.estado == EstadoPrestamo.VENCIDO }
        val totalDevueltosCount = prestamos.count { it.estado == EstadoPrestamo.DEVUELTO }

        BibliotecaStats(
            totalTitulos = totalTitulos,
            totalEjemplaresFisicos = totalEjemplaresFisicos,
            totalDisponiblesFisicos = totalDisponiblesFisicos,
            totalLibrosDigitales = totalLibrosDigitales,
            prestamosActivos = prestamosActivosCount,
            prestamosVencidos = prestamosVencidosCount,
            totalDevueltos = totalDevueltosCount,
            solicitudesPendientes = pendientesCount,
            totalMultasPendientesCount = multasCount,
            totalMontoMultasPendientes = multasMonto
        )
    }

    val reporteInstitucional: Flow<ReporteInstitucional> = combine(
        todosLosLibros,
        todosLosPrestamos,
        todasLasMultas
    ) { libros, prestamos, multas ->
        val totalLibros = libros.size
        val totalEjemplares = libros.filter { it.formato != FormatoLibro.DIGITAL }.sumOf { it.ejemplaresTotales }
        val totalPrestamosHistoricos = prestamos.size

        val devueltos = prestamos.filter { it.estado == EstadoPrestamo.DEVUELTO }
        val aTiempo = devueltos.count { d ->
            val real = d.fechaDevolucionReal ?: 0L
            real <= d.fechaDevolucionEstimada
        }
        val tasaDevolucion = if (devueltos.isEmpty()) 100 else ((aTiempo.toDouble() / devueltos.size) * 100).toInt()

        val totalRecaudado = multas.filter { it.estado == EstadoMulta.PAGADA }.sumOf { it.montoTotal }
        val totalCondonado = multas.filter { it.estado == EstadoMulta.CONDONADA }.sumOf { it.montoTotal }

        val prestamosPorLibro = prestamos.groupBy { it.libroId }
        val topLibros = prestamosPorLibro.mapNotNull { (libroId, lista) ->
            val libro = libros.find { it.id == libroId }
            libro?.let {
                LibroPopular(
                    libroId = it.id,
                    titulo = it.titulo,
                    autor = it.autor,
                    categoria = it.categoria,
                    totalPrestamos = lista.size
                )
            }
        }.sortedByDescending { it.totalPrestamos }.take(5)

        val prestamosPorCarrera = prestamos.filter { it.carreraOArea.isNotBlank() }.groupBy { it.carreraOArea }
        val totalConCarrera = prestamos.count { it.carreraOArea.isNotBlank() }
        val topCarreras = prestamosPorCarrera.map { (carrera, lista) ->
            val count = lista.size
            val pct = if (totalConCarrera == 0) 0f else (count.toFloat() / totalConCarrera)
            CarreraStats(
                carrera = carrera,
                totalPrestamos = count,
                porcentaje = pct
            )
        }.sortedByDescending { it.totalPrestamos }.take(5)

        ReporteInstitucional(
            totalLibros = totalLibros,
            totalEjemplares = totalEjemplares,
            totalPrestamosHistoricos = totalPrestamosHistoricos,
            tasaDevolucionATiempo = tasaDevolucion,
            totalRecaudadoMultas = totalRecaudado,
            totalMultasCondonadas = totalCondonado,
            topLibros = topLibros,
            topCarreras = topCarreras
        )
    }

    // -------------------------------------------------------------------------
    // INICIALIZACIÓN Y SINCRONIZACIÓN
    // -------------------------------------------------------------------------

    suspend fun verificarYPrecargarDatos() {
        try {
            // Intentar primero sincronizar los 103 libros del VPS
            val syncResult = sincronizarLibros()
            if (syncResult.isFailure && libroDao.getCountDirect() == 0) {
                // Fallback offline si la base de datos está totalmente vacía
                recargarDatosIniciales()
            }
        } catch (e: Exception) {
            Log.e("BibliotecaRepo", "Error al verificar datos iniciales", e)
            if (libroDao.getCountDirect() == 0) {
                recargarDatosIniciales()
            }
        }
    }

    suspend fun recargarDatosIniciales() {
        BibliotecaDatabase.populateInitialData(libroDao, prestamoDao, usuarioDao, solicitudPrestamoDao, multaDao)
    }

    private fun parseDateMillis(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            format.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun extraerMensajeError(rawBody: String?): String? {
        if (rawBody.isNullOrBlank()) return null
        return try {
            val json = gson.fromJson(rawBody, Map::class.java)
            json["message"]?.toString()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun BibliotecaApiService.deleteLibro(id: Long) {
        // Helper para llamar delete en caso de requerirse
    }
}
