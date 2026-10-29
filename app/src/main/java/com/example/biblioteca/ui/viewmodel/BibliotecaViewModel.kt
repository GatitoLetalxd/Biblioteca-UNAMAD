package com.example.biblioteca.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.biblioteca.data.repository.BibliotecaRepository
import com.example.biblioteca.model.BibliotecaStats
import com.example.biblioteca.model.EstadoMulta
import com.example.biblioteca.model.EstadoPrestamo
import com.example.biblioteca.model.EstadoSolicitud
import com.example.biblioteca.model.FormatoLibro
import com.example.biblioteca.model.Libro
import com.example.biblioteca.model.LibroConCirculacion
import com.example.biblioteca.model.Multa
import com.example.biblioteca.model.Prestamo
import com.example.biblioteca.model.ReporteInstitucional
import com.example.biblioteca.model.RolUsuario
import com.example.biblioteca.model.SolicitudPrestamo
import com.example.biblioteca.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val codigo: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val passwordVisible: Boolean = false
)

data class RegistroUiState(
    val codigo: String = "",
    val password: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val carrera: String = "Ingeniería de Sistemas e Informática",
    val telefono: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val passwordVisible: Boolean = false
)

data class CatalogoUiState(
    val query: String = "",
    val categoriaSeleccionada: String? = null,
    val formatoSeleccionado: FormatoLibro? = null,
    val soloDisponibles: Boolean = false,
    val libros: List<Libro> = emptyList(),
    val categorias: List<String> = emptyList(),
    val isLoading: Boolean = false
)

data class PrestamosUiState(
    val pestanaSeleccionada: Int = 0, // 0: Activos, 1: Historial / Devueltos
    val query: String = "",
    val prestamosActivos: List<Prestamo> = emptyList(),
    val historialDevueltos: List<Prestamo> = emptyList(),
    val totalVencidos: Int = 0
)

data class SolicitudesUiState(
    val pestanaSeleccionada: Int = 0, // 0: Pendientes, 1: Historial (Aprobadas / Rechazadas)
    val query: String = "",
    val solicitudesPendientes: List<SolicitudPrestamo> = emptyList(),
    val historialSolicitudes: List<SolicitudPrestamo> = emptyList(),
    val totalPendientes: Int = 0
)

data class MultasUiState(
    val pestanaSeleccionada: Int = 0, // 0: Pendientes de pago, 1: Historial (Pagadas / Condonadas)
    val multasPendientes: List<Multa> = emptyList(),
    val historialMultas: List<Multa> = emptyList(),
    val totalMontoPendiente: Double = 0.0
)

class BibliotecaViewModel(
    private val repository: BibliotecaRepository
) : ViewModel() {

    // -------------------------------------------------------------------------
    // AUTENTICACIÓN Y SESIÓN
    // -------------------------------------------------------------------------
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _registroUiState = MutableStateFlow(RegistroUiState())
    val registroUiState: StateFlow<RegistroUiState> = _registroUiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.verificarYPrecargarDatos()
            // Recuperar sesión activa si ya existía
            val sesion = repository.obtenerUsuarioActual()
            _usuarioActual.value = sesion
        }
    }

    fun onLoginCodigoChange(newCodigo: String) {
        _loginUiState.update { it.copy(codigo = newCodigo, error = null) }
    }

    fun onLoginPasswordChange(newPassword: String) {
        _loginUiState.update { it.copy(password = newPassword, error = null) }
    }

    fun onTogglePasswordVisibility() {
        _loginUiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    // Handlers para el formulario de autoregistro de estudiantes
    fun onRegistroCodigoChange(newCodigo: String) {
        // Solo permitir números y máximo 8 dígitos
        val filtered = newCodigo.filter { it.isDigit() }.take(8)
        _registroUiState.update { it.copy(codigo = filtered, error = null) }
    }

    fun onRegistroPasswordChange(newPassword: String) {
        _registroUiState.update { it.copy(password = newPassword, error = null) }
    }

    fun onRegistroNombreChange(newNombre: String) {
        _registroUiState.update { it.copy(nombre = newNombre, error = null) }
    }

    fun onRegistroApellidoChange(newApellido: String) {
        _registroUiState.update { it.copy(apellido = newApellido, error = null) }
    }

    fun onRegistroCarreraChange(newCarrera: String) {
        _registroUiState.update { it.copy(carrera = newCarrera, error = null) }
    }

    fun onRegistroTelefonoChange(newTelefono: String) {
        val filtered = newTelefono.filter { it.isDigit() }.take(15)
        _registroUiState.update { it.copy(telefono = filtered, error = null) }
    }

    fun onToggleRegistroPasswordVisibility() {
        _registroUiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun registrarEstudiante(onSuccess: () -> Unit) {
        val state = _registroUiState.value
        if (state.codigo.length != 8) {
            _registroUiState.update { it.copy(error = "El código de estudiante debe tener exactamente 8 dígitos numéricos") }
            return
        }
        if (state.password.length < 6) {
            _registroUiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }
        if (state.nombre.isBlank()) {
            _registroUiState.update { it.copy(error = "Por favor ingresa tu primer nombre") }
            return
        }
        if (state.apellido.isBlank()) {
            _registroUiState.update { it.copy(error = "Por favor ingresa tu primer apellido") }
            return
        }
        if (state.telefono.isBlank()) {
            _registroUiState.update { it.copy(error = "Por favor ingresa tu número de teléfono de contacto") }
            return
        }

        viewModelScope.launch {
            _registroUiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.registrarEstudiante(
                codigo = state.codigo,
                password = state.password,
                nombre = state.nombre,
                apellido = state.apellido,
                carrera = state.carrera,
                telefono = state.telefono
            )
            result.fold(
                onSuccess = { usuario ->
                    _usuarioActual.value = usuario
                    _registroUiState.value = RegistroUiState()
                    _mensajeNotificacion.value = "¡Bienvenido, ${usuario.nombreCompleto}! Cuenta creada exitosamente."
                    onSuccess()
                },
                onFailure = { error ->
                    _registroUiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "No se pudo registrar la cuenta"
                        )
                    }
                }
            )
        }
    }

    fun sincronizarConServidor() {
        viewModelScope.launch {
            _mensajeNotificacion.value = "Sincronizando catálogo con el servidor..."
            val result = repository.sincronizarLibros()
            result.fold(
                onSuccess = { count ->
                    _mensajeNotificacion.value = "Sincronización completa: $count libros actualizados."
                },
                onFailure = { error ->
                    _mensajeNotificacion.value = "Sincronización offline: usando caché local."
                }
            )
        }
    }

    fun iniciarSesion() {
        val state = _loginUiState.value
        if (state.codigo.isBlank()) {
            _loginUiState.update { it.copy(error = "Por favor ingresa tu código institucional o de usuario") }
            return
        }
        if (state.password.isBlank()) {
            _loginUiState.update { it.copy(error = "Por favor ingresa tu contraseña") }
            return
        }

        iniciarSesionConCredenciales(state.codigo, state.password)
    }

    private fun iniciarSesionConCredenciales(codigo: String, pass: String) {
        viewModelScope.launch {
            _loginUiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.autenticar(codigo, pass)
            result.fold(
                onSuccess = { usuario ->
                    _usuarioActual.value = usuario
                    _loginUiState.value = LoginUiState()
                    _mensajeNotificacion.value = "¡Bienvenido, ${usuario.nombreCompleto}!"
                },
                onFailure = { error ->
                    _loginUiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Credenciales incorrectas"
                        )
                    }
                }
            )
        }
    }

    fun iniciarSesionRapida(rol: RolUsuario) {
        when (rol) {
            RolUsuario.ESTUDIANTE -> {
                iniciarSesionConCredenciales("20230451", "alumno123")
            }
            RolUsuario.BIBLIOTECARIO -> {
                iniciarSesionConCredenciales("BIBLIO-01", "biblio123")
            }
        }
    }

    fun cerrarSesion() {
        repository.cerrarSesion()
        _usuarioActual.value = null
        _loginUiState.value = LoginUiState()
        _mensajeNotificacion.value = "Sesión cerrada correctamente."
    }

    // -------------------------------------------------------------------------
    // CATÁLOGO DE LIBROS Y FILTROS
    // -------------------------------------------------------------------------
    private val _searchQuery = MutableStateFlow("")
    private val _categoriaSeleccionada = MutableStateFlow<String?>(null)
    private val _formatoSeleccionado = MutableStateFlow<FormatoLibro?>(null)
    private val _soloDisponibles = MutableStateFlow(false)

    private data class Filtros(
        val query: String,
        val categoria: String?,
        val formato: FormatoLibro?,
        val soloDisponibles: Boolean
    )

    private val _filtros = combine(
        _searchQuery,
        _categoriaSeleccionada,
        _formatoSeleccionado,
        _soloDisponibles
    ) { q, c, f, s ->
        Filtros(q, c, f, s)
    }

    val catalogoUiState: StateFlow<CatalogoUiState> = combine(
        repository.todosLosLibros,
        repository.categorias,
        _filtros
    ) { libros, categorias, filtros ->
        val query = filtros.query
        val cat = filtros.categoria
        val formato = filtros.formato
        val soloDisp = filtros.soloDisponibles
        val cleanQuery = query.trim().unaccent()

        val filtrados = libros.filter { libro ->
            val coincideQuery = if (cleanQuery.isBlank()) true else {
                libro.titulo.unaccent().contains(cleanQuery, ignoreCase = true) ||
                libro.autor.unaccent().contains(cleanQuery, ignoreCase = true) ||
                libro.isbn.contains(cleanQuery, ignoreCase = true) ||
                libro.categoria.unaccent().contains(cleanQuery, ignoreCase = true)
            }
            val coincideCategoria = if (cat.isNullOrBlank()) true else libro.categoria.equals(cat, ignoreCase = true)
            val coincideFormato = if (formato == null) true else (libro.formato == formato || libro.formato == FormatoLibro.HIBRIDO)
            val coincideDisponibilidad = if (!soloDisp) true else (libro.formato == FormatoLibro.DIGITAL || libro.ejemplaresDisponibles > 0)

            coincideQuery && coincideCategoria && coincideFormato && coincideDisponibilidad
        }

        CatalogoUiState(
            query = query,
            categoriaSeleccionada = cat,
            formatoSeleccionado = formato,
            soloDisponibles = soloDisp,
            libros = filtrados,
            categorias = categorias,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CatalogoUiState(isLoading = true)
    )

    // -------------------------------------------------------------------------
    // SISTEMA DE SOLICITUDES DE PRÉSTAMO (FASE 2)
    // -------------------------------------------------------------------------
    private val _solicitudesQuery = MutableStateFlow("")
    val solicitudesQuery: StateFlow<String> = _solicitudesQuery.asStateFlow()

    private val _pestanaSolicitudes = MutableStateFlow(0)
    val pestanaSolicitudes: StateFlow<Int> = _pestanaSolicitudes.asStateFlow()

    val totalSolicitudesPendientes: StateFlow<Int> = repository.conteoSolicitudesPendientes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val solicitudesUiState: StateFlow<SolicitudesUiState> = combine(
        repository.todasLasSolicitudes,
        _pestanaSolicitudes,
        _solicitudesQuery
    ) { solicitudes, pestana, query ->
        val queryClean = query.trim()
        val filtradas = if (queryClean.isBlank()) {
            solicitudes
        } else {
            solicitudes.filter {
                it.nombreEstudiante.contains(queryClean, ignoreCase = true) ||
                it.codigoEstudiante.contains(queryClean, ignoreCase = true) ||
                it.tituloLibro.contains(queryClean, ignoreCase = true) ||
                it.carreraEstudiante.contains(queryClean, ignoreCase = true)
            }
        }

        val pendientes = filtradas.filter { it.estado == EstadoSolicitud.PENDIENTE }
        val historial = filtradas.filter { it.estado != EstadoSolicitud.PENDIENTE }
        val countPendientes = solicitudes.count { it.estado == EstadoSolicitud.PENDIENTE }

        SolicitudesUiState(
            pestanaSeleccionada = pestana,
            query = query,
            solicitudesPendientes = pendientes,
            historialSolicitudes = historial,
            totalPendientes = countPendientes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SolicitudesUiState()
    )

    // Solicitudes del estudiante actualmente logueado
    val misSolicitudesEstudiante: StateFlow<List<SolicitudPrestamo>> = combine(
        repository.todasLasSolicitudes,
        _usuarioActual
    ) { todas, usuario ->
        if (usuario == null) emptyList()
        else todas.filter { it.codigoEstudiante.equals(usuario.codigo, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSolicitudesPestanaChange(index: Int) {
        _pestanaSolicitudes.value = index
    }

    fun onSolicitudesQueryChange(query: String) {
        _solicitudesQuery.value = query
    }

    // Modal para solicitar préstamo (Estudiante)
    private val _libroParaSolicitar = MutableStateFlow<Libro?>(null)
    val libroParaSolicitar = _libroParaSolicitar.asStateFlow()

    fun abrirDialogoSolicitudEstudiante(libro: Libro) {
        _libroParaSolicitar.value = libro
    }

    fun cerrarDialogoSolicitudEstudiante() {
        _libroParaSolicitar.value = null
    }

    fun enviarSolicitudPrestamo(libroId: Long, dias: Int, notas: String) {
        val estudiante = _usuarioActual.value
        if (estudiante == null) {
            _mensajeNotificacion.value = "Debes iniciar sesión para realizar solicitudes."
            return
        }

        viewModelScope.launch {
            val result = repository.crearSolicitudPrestamo(
                libroId = libroId,
                usuario = estudiante,
                dias = dias,
                notas = notas
            )
            result.fold(
                onSuccess = {
                    _mensajeNotificacion.value = "¡Solicitud registrada! El bibliotecario la revisará para su entrega."
                    cerrarDialogoSolicitudEstudiante()
                },
                onFailure = { error ->
                    _mensajeNotificacion.value = error.message ?: "No se pudo procesar la solicitud"
                }
            )
        }
    }

    // Modal para rechazar solicitud (Bibliotecario)
    private val _solicitudParaRechazar = MutableStateFlow<SolicitudPrestamo?>(null)
    val solicitudParaRechazar = _solicitudParaRechazar.asStateFlow()

    fun abrirDialogoRechazo(solicitud: SolicitudPrestamo) {
        _solicitudParaRechazar.value = solicitud
    }

    fun cerrarDialogoRechazo() {
        _solicitudParaRechazar.value = null
    }

    fun confirmarRechazoSolicitud(solicitudId: Long, motivo: String) {
        viewModelScope.launch {
            val result = repository.rechazarSolicitud(solicitudId, motivo)
            result.fold(
                onSuccess = {
                    _mensajeNotificacion.value = "Solicitud rechazada. Se notificó el motivo al estudiante."
                    cerrarDialogoRechazo()
                },
                onFailure = { error ->
                    _mensajeNotificacion.value = error.message ?: "No se pudo rechazar la solicitud"
                }
            )
        }
    }

    fun aprobarSolicitud(solicitud: SolicitudPrestamo) {
        viewModelScope.launch {
            val result = repository.aprobarSolicitud(solicitud.id)
            result.fold(
                onSuccess = {
                    _mensajeNotificacion.value = "¡Solicitud aprobada! Préstamo generado e inventario actualizado."
                },
                onFailure = { error ->
                    _mensajeNotificacion.value = error.message ?: "No se pudo aprobar la solicitud"
                }
            )
        }
    }

    fun cancelarSolicitudEstudiante(solicitud: SolicitudPrestamo) {
        viewModelScope.launch {
            val result = repository.cancelarSolicitud(solicitud.id)
            result.fold(
                onSuccess = {
                    _mensajeNotificacion.value = "Solicitud de préstamo cancelada."
                },
                onFailure = { error ->
                    _mensajeNotificacion.value = error.message ?: "No se pudo cancelar la solicitud"
                }
            )
        }
    }

    // -------------------------------------------------------------------------
    // SISTEMA DE MULTAS Y PENALIZACIONES (FASE 3)
    // -------------------------------------------------------------------------
    private val _pestanaMultas = MutableStateFlow(0)
    val pestanaMultas: StateFlow<Int> = _pestanaMultas.asStateFlow()

    private val _isGestionMultasDialogOpen = MutableStateFlow(false)
    val isGestionMultasDialogOpen: StateFlow<Boolean> = _isGestionMultasDialogOpen.asStateFlow()

    val todasLasMultas: StateFlow<List<Multa>> = repository.todasLasMultas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val multasPendientes: StateFlow<List<Multa>> = repository.multasPendientes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalMontoMultasPendientes: StateFlow<Double> = repository.totalMontoMultasPendientes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val multasUiState: StateFlow<MultasUiState> = combine(
        repository.todasLasMultas,
        _pestanaMultas,
        repository.totalMontoMultasPendientes
    ) { todas, pestana, monto ->
        val pendientes = todas.filter { it.estado == EstadoMulta.PENDIENTE }
        val historial = todas.filter { it.estado != EstadoMulta.PENDIENTE }
        MultasUiState(
            pestanaSeleccionada = pestana,
            multasPendientes = pendientes,
            historialMultas = historial,
            totalMontoPendiente = monto
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MultasUiState()
    )

    // Multas del estudiante en sesión
    val misMultasEstudiante: StateFlow<List<Multa>> = combine(
        repository.todasLasMultas,
        _usuarioActual
    ) { todas, usuario ->
        if (usuario == null) emptyList()
        else todas.filter { it.codigoEstudiante.equals(usuario.codigo, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val misMultasPendientesEstudiante: StateFlow<List<Multa>> = misMultasEstudiante.map { list ->
        list.filter { it.estado == EstadoMulta.PENDIENTE }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onMultasPestanaChange(index: Int) {
        _pestanaMultas.value = index
    }

    fun abrirGestionMultasDialog() {
        _isGestionMultasDialogOpen.value = true
    }

    fun cerrarGestionMultasDialog() {
        _isGestionMultasDialogOpen.value = false
    }

    fun cobrarMulta(multa: Multa) {
        viewModelScope.launch {
            val result = repository.cobrarMulta(multa.id)
            result.fold(
                onSuccess = {
                    _mensajeNotificacion.value = "Pago de multa registrado: S/. ${String.format(java.util.Locale.US, "%.2f", multa.montoTotal)}"
                },
                onFailure = { error ->
                    _mensajeNotificacion.value = error.message ?: "No se pudo registrar el pago"
                }
            )
        }
    }

    fun condonarMulta(multa: Multa, motivo: String) {
        viewModelScope.launch {
            val result = repository.condonarMulta(multa.id, motivo)
            result.fold(
                onSuccess = {
                    _mensajeNotificacion.value = "Multa condonada exitosamente."
                },
                onFailure = { error ->
                    _mensajeNotificacion.value = error.message ?: "No se pudo condonar la multa"
                }
            )
        }
    }

    // -------------------------------------------------------------------------
    // CONTROL DE PRÉSTAMOS (DIFERENCIADO POR ROL)
    // -------------------------------------------------------------------------
    private val _prestamosQuery = MutableStateFlow("")
    private val _pestanaPrestamos = MutableStateFlow(0)

    val prestamosUiState: StateFlow<PrestamosUiState> = combine(
        repository.todosLosPrestamos,
        _pestanaPrestamos,
        _prestamosQuery,
        _usuarioActual
    ) { todosPrestamos, pestana, query, usuario ->
        // Si es estudiante, solo ve sus propios préstamos
        val prestamosBase = if (usuario?.esEstudiante == true) {
            todosPrestamos.filter { it.codigoLector.equals(usuario.codigo, ignoreCase = true) }
        } else {
            todosPrestamos
        }

        val prestamosFiltrados = if (query.isBlank()) {
            prestamosBase
        } else {
            prestamosBase.filter {
                it.nombreLector.contains(query, ignoreCase = true) ||
                it.codigoLector.contains(query, ignoreCase = true) ||
                it.tituloLibro.contains(query, ignoreCase = true)
            }
        }

        val activos = prestamosFiltrados.filter { it.estado != EstadoPrestamo.DEVUELTO }
        val devueltos = prestamosFiltrados.filter { it.estado == EstadoPrestamo.DEVUELTO }
        val vencidosCount = prestamosBase.count { it.estado == EstadoPrestamo.VENCIDO }

        PrestamosUiState(
            pestanaSeleccionada = pestana,
            query = query,
            prestamosActivos = activos,
            historialDevueltos = devueltos,
            totalVencidos = vencidosCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PrestamosUiState()
    )

    val estadisticas: StateFlow<BibliotecaStats> = repository.estadisticas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BibliotecaStats()
    )

    val reporteInstitucional: StateFlow<ReporteInstitucional> = repository.reporteInstitucional.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReporteInstitucional()
    )

    private val _isReportesDialogOpen = MutableStateFlow(false)
    val isReportesDialogOpen: StateFlow<Boolean> = _isReportesDialogOpen.asStateFlow()

    fun abrirReportesDialog() {
        _isReportesDialogOpen.value = true
    }

    fun cerrarReportesDialog() {
        _isReportesDialogOpen.value = false
    }

    val inventarioConLectores: StateFlow<List<LibroConCirculacion>> = repository.inventarioConLectores.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isPanelInventarioOpen = MutableStateFlow(false)
    val isPanelInventarioOpen: StateFlow<Boolean> = _isPanelInventarioOpen.asStateFlow()

    fun abrirPanelInventario() {
        _isPanelInventarioOpen.value = true
    }

    fun cerrarPanelInventario() {
        _isPanelInventarioOpen.value = false
    }

    // -------------------------------------------------------------------------
    // DIÁLOGOS Y MODALES
    // -------------------------------------------------------------------------
    private val _isLibroDialogOpen = MutableStateFlow(false)
    val isLibroDialogOpen = _isLibroDialogOpen.asStateFlow()

    private val _libroAEditar = MutableStateFlow<Libro?>(null)
    val libroAEditar = _libroAEditar.asStateFlow()

    private val _isPrestamoDialogOpen = MutableStateFlow(false)
    val isPrestamoDialogOpen = _isPrestamoDialogOpen.asStateFlow()

    private val _libroSeleccionadoParaPrestamo = MutableStateFlow<Libro?>(null)
    val libroSeleccionadoParaPrestamo = _libroSeleccionadoParaPrestamo.asStateFlow()

    private val _libroSeleccionadoParaVisor = MutableStateFlow<Libro?>(null)
    val libroSeleccionadoParaVisor = _libroSeleccionadoParaVisor.asStateFlow()

    private val _mensajeNotificacion = MutableStateFlow<String?>(null)
    val mensajeNotificacion = _mensajeNotificacion.asStateFlow()

    // Acciones de Catálogo
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSelectCategoria(categoria: String?) {
        _categoriaSeleccionada.value = if (_categoriaSeleccionada.value == categoria) null else categoria
    }

    fun onSelectFormato(formato: FormatoLibro?) {
        _formatoSeleccionado.value = if (_formatoSeleccionado.value == formato) null else formato
    }

    fun onToggleSoloDisponibles() {
        _soloDisponibles.value = !_soloDisponibles.value
    }

    // Acciones de Préstamos
    fun onPrestamosPestanaChange(index: Int) {
        _pestanaPrestamos.value = index
    }

    fun onPrestamosQueryChange(query: String) {
        _prestamosQuery.value = query
    }

    // Modales de Libro (Solo Bibliotecario)
    fun abrirDialogoNuevoLibro(libro: Libro? = null) {
        _libroAEditar.value = libro
        _isLibroDialogOpen.value = true
    }

    fun cerrarDialogoLibro() {
        _libroAEditar.value = null
        _isLibroDialogOpen.value = false
    }

    fun guardarLibro(libro: Libro) {
        viewModelScope.launch {
            try {
                repository.guardarLibro(libro)
                _mensajeNotificacion.value = if (libro.id == 0L) "¡Libro registrado exitosamente!" else "Libro actualizado correctamente."
                cerrarDialogoLibro()
            } catch (e: Exception) {
                _mensajeNotificacion.value = "Error al guardar el libro: ${e.message}"
            }
        }
    }

    fun eliminarLibro(libro: Libro) {
        viewModelScope.launch {
            try {
                repository.eliminarLibro(libro)
                _mensajeNotificacion.value = "Libro eliminado del catálogo."
            } catch (e: Exception) {
                _mensajeNotificacion.value = "Error al eliminar: ${e.message}"
            }
        }
    }

    // Modales de Préstamo (Bibliotecario directo o Estudiante solicitud)
    fun abrirDialogoPrestamo(libro: Libro? = null) {
        val usuario = _usuarioActual.value
        if (usuario?.esEstudiante == true && libro != null) {
            // Abre el modal de solicitud de préstamo para estudiante
            abrirDialogoSolicitudEstudiante(libro)
        } else {
            // Flujo manual del bibliotecario
            _libroSeleccionadoParaPrestamo.value = libro
            _isPrestamoDialogOpen.value = true
        }
    }

    fun cerrarDialogoPrestamo() {
        _libroSeleccionadoParaPrestamo.value = null
        _isPrestamoDialogOpen.value = false
    }

    fun registrarPrestamo(
        libroId: Long,
        nombreLector: String,
        codigoLector: String,
        carreraOArea: String,
        telefonoLector: String,
        fechaDevolucionEstimada: Long,
        notas: String
    ) {
        viewModelScope.launch {
            val result = repository.registrarPrestamo(
                libroId = libroId,
                nombreLector = nombreLector,
                codigoLector = codigoLector,
                carreraOArea = carreraOArea,
                telefonoLector = telefonoLector,
                fechaDevolucionEstimada = fechaDevolucionEstimada,
                notas = notas
            )
            result.fold(
                onSuccess = {
                    _mensajeNotificacion.value = "Préstamo registrado. Stock actualizado."
                    cerrarDialogoPrestamo()
                },
                onFailure = { error ->
                    _mensajeNotificacion.value = error.message ?: "No se pudo registrar el préstamo"
                }
            )
        }
    }

    fun registrarDevolucion(prestamo: Prestamo) {
        viewModelScope.launch {
            val result = repository.registrarDevolucion(prestamo.id, prestamo.libroId)
            result.fold(
                onSuccess = { mensaje ->
                    _mensajeNotificacion.value = mensaje
                },
                onFailure = { error ->
                    _mensajeNotificacion.value = error.message ?: "No se pudo registrar la devolución"
                }
            )
        }
    }

    // Visor Digital
    fun abrirVisorDigital(libro: Libro) {
        _libroSeleccionadoParaVisor.value = libro
    }

    fun cerrarVisorDigital() {
        _libroSeleccionadoParaVisor.value = null
    }

    fun limpiarMensaje() {
        _mensajeNotificacion.value = null
    }

    fun recargarDatosDemo() {
        viewModelScope.launch {
            repository.recargarDatosIniciales()
            _mensajeNotificacion.value = "Datos de prueba recargados."
        }
    }
}

class BibliotecaViewModelFactory(
    private val repository: BibliotecaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BibliotecaViewModel::class.java)) {
            return BibliotecaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

fun String.unaccent(): String {
    val temp = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
    return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(temp, "")
}
