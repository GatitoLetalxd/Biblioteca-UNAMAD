package com.example.biblioteca.data.repository

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
import com.example.biblioteca.model.SolicitudPrestamo
import com.example.biblioteca.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class BibliotecaRepository(
    private val libroDao: LibroDao,
    private val prestamoDao: PrestamoDao,
    private val usuarioDao: UsuarioDao,
    private val solicitudPrestamoDao: SolicitudPrestamoDao,
    private val multaDao: MultaDao,
    private val sessionManager: SessionManager
) {
    // -------------------------------------------------------------------------
    // USUARIOS Y AUTENTICACIÓN
    // -------------------------------------------------------------------------

    val todosLosUsuarios: Flow<List<Usuario>> = usuarioDao.getAllUsuarios().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun autenticar(codigo: String, password: String): Result<Usuario> {
        val cleanCodigo = codigo.trim()
        val cleanPassword = password.trim()

        val usuarioEntity = usuarioDao.autenticar(cleanCodigo, cleanPassword)
            ?: return Result.failure(Exception("Código institucional o contraseña no válidos"))

        if (!usuarioEntity.activo) {
            return Result.failure(Exception("Esta cuenta de usuario se encuentra inhabilitada"))
        }

        val usuario = usuarioEntity.toDomain()
        sessionManager.guardarSesion(usuario)
        return Result.success(usuario)
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
    // CATÁLOGO DE LIBROS
    // -------------------------------------------------------------------------

    val todosLosLibros: Flow<List<Libro>> = libroDao.getAllLibros().map { list ->
        list.map { it.toDomain() }
    }

    val categorias: Flow<List<String>> = libroDao.getAllCategorias()

    fun buscarLibros(query: String): Flow<List<Libro>> {
        return libroDao.searchLibros(query).map { list -> list.map { it.toDomain() } }
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
        libroDao.deleteLibro(LibroEntity.fromDomain(libro))
    }

    // -------------------------------------------------------------------------
    // SISTEMA DE SOLICITUDES DE PRÉSTAMO (FASE 2)
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

    suspend fun crearSolicitudPrestamo(
        libroId: Long,
        usuario: Usuario,
        dias: Int = 7,
        notas: String = ""
    ): Result<Long> {
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
        val solicitud = solicitudPrestamoDao.getSolicitudById(solicitudId)
            ?: return Result.failure(Exception("Solicitud no encontrada"))

        if (solicitud.estado != EstadoSolicitud.PENDIENTE.name) {
            return Result.failure(Exception("Esta solicitud ya fue procesada previamente (${solicitud.estado})."))
        }

        val libro = libroDao.getLibroById(solicitud.libroId)
            ?: return Result.failure(Exception("El libro solicitado ya no existe en el catálogo"))

        if (libro.formato != FormatoLibro.DIGITAL.name && libro.ejemplaresDisponibles <= 0) {
            return Result.failure(Exception("No hay stock suficiente para aprobar este préstamo."))
        }

        val now = System.currentTimeMillis()
        val plazoMillis = solicitud.diasSolicitados.toLong() * 24L * 60 * 60 * 1000
        val fechaEstimada = now + plazoMillis

        // 1. Crear el préstamo oficial
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

        // 2. Descontar stock físico del libro
        if (libro.formato != FormatoLibro.DIGITAL.name) {
            libroDao.decrementarStock(solicitud.libroId)
        }

        // 3. Actualizar estado de la solicitud
        solicitudPrestamoDao.actualizarEstado(
            id = solicitudId,
            nuevoEstado = EstadoSolicitud.APROBADA.name,
            fechaRespuesta = now,
            motivo = null
        )

        return Result.success(prestamoId)
    }

    suspend fun rechazarSolicitud(solicitudId: Long, motivo: String): Result<Unit> {
        val solicitud = solicitudPrestamoDao.getSolicitudById(solicitudId)
            ?: return Result.failure(Exception("Solicitud no encontrada"))

        if (solicitud.estado != EstadoSolicitud.PENDIENTE.name) {
            return Result.failure(Exception("Esta solicitud ya fue procesada anteriormente."))
        }

        val motivoLimpio = motivo.trim().ifBlank {
            "Solicitud no aprobada por administración de biblioteca."
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
        val solicitud = solicitudPrestamoDao.getSolicitudById(solicitudId)
            ?: return Result.failure(Exception("Solicitud no encontrada"))

        if (solicitud.estado != EstadoSolicitud.PENDIENTE.name) {
            return Result.failure(Exception("Solo se pueden cancelar solicitudes pendientes."))
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
    // SISTEMA DE MULTAS Y PENALIZACIONES (FASE 3)
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
        val multa = multaDao.getMultaById(multaId)
            ?: return Result.failure(Exception("Multa no encontrada"))

        if (multa.estado == EstadoMulta.PAGADA.name) {
            return Result.failure(Exception("Esta multa ya se encuentra cancelada."))
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
        val multa = multaDao.getMultaById(multaId)
            ?: return Result.failure(Exception("Multa no encontrada"))

        val motivoLimpio = motivo.trim().ifBlank { "Condonación autorizada por administración de biblioteca." }

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

        // Si es físico o híbrido, validar stock disponible
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

        // Decrementar stock físico si aplica
        if (libro.formato != FormatoLibro.DIGITAL.name) {
            libroDao.decrementarStock(libroId)
        }

        return Result.success(id)
    }

    suspend fun registrarDevolucion(prestamoId: Long, libroId: Long): Result<String> {
        val prestamo = prestamoDao.getPrestamoById(prestamoId)
            ?: return Result.failure(Exception("Préstamo no encontrado"))

        if (prestamo.estado == EstadoPrestamo.DEVUELTO.name) {
            return Result.failure(Exception("Este préstamo ya fue devuelto previamente"))
        }

        val now = System.currentTimeMillis()
        prestamoDao.marcarDevolucion(
            prestamoId = prestamoId,
            fechaDevolucion = now,
            nuevoEstado = EstadoPrestamo.DEVUELTO.name
        )

        // Devolver ejemplar al inventario disponible
        libroDao.incrementarStock(libroId)

        // Generar multa automática si hay días de retraso
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
            mensajeResultado = "Devolución con $diasRetraso día(s) de retraso. Se generó multa de S/. ${String.format(java.util.Locale.US, "%.2f", montoTotal)}."
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

        // Top Libros más solicitados / prestados
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

        // Top Carreras profesionales con más préstamos
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
    // INICIALIZACIÓN Y SEEDING
    // -------------------------------------------------------------------------

    suspend fun verificarYPrecargarDatos() {
        try {
            if (libroDao.getCountDirect() == 0 || usuarioDao.getCountDirect() == 0 ||
                solicitudPrestamoDao.getCountDirect() == 0 || multaDao.getCountDirect() == 0) {
                recargarDatosIniciales()
            }
        } catch (e: Exception) {
            android.util.Log.e("BibliotecaRepository", "Error al verificar datos iniciales", e)
        }
    }

    suspend fun recargarDatosIniciales() {
        BibliotecaDatabase.populateInitialData(libroDao, prestamoDao, usuarioDao, solicitudPrestamoDao, multaDao)
    }
}
