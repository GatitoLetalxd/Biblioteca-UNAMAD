package com.example.biblioteca.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.biblioteca.ui.dialogs.DigitalViewerDialog
import com.example.biblioteca.ui.dialogs.GestionMultasDialog
import com.example.biblioteca.ui.dialogs.NuevoPrestamoDialog
import com.example.biblioteca.ui.dialogs.RechazoSolicitudDialog
import com.example.biblioteca.ui.dialogs.RegistroLibroDialog
import com.example.biblioteca.ui.dialogs.ReportesDialog
import com.example.biblioteca.ui.dialogs.SolicitarPrestamoDialog
import com.example.biblioteca.ui.screens.CatalogoScreen
import com.example.biblioteca.ui.screens.DashboardScreen
import com.example.biblioteca.ui.screens.GestionSolicitudesScreen
import com.example.biblioteca.ui.screens.LoginScreen
import com.example.biblioteca.ui.screens.PanelInventarioDatabaseScreen
import com.example.biblioteca.ui.screens.PerfilEstudianteScreen
import com.example.biblioteca.ui.screens.PrestamosScreen
import com.example.biblioteca.ui.screens.RegistroEstudianteScreen
import com.example.biblioteca.ui.theme.AmberAccent
import com.example.biblioteca.ui.theme.UnamadGreenPrimary
import com.example.biblioteca.ui.viewmodel.BibliotecaViewModel

enum class AppDestination(val route: String, val title: String, val icon: ImageVector) {
    CATALOGO("catalogo", "Catálogo", Icons.AutoMirrored.Filled.MenuBook),
    SOLICITUDES("solicitudes", "Solicitudes", Icons.Default.PendingActions),
    PRESTAMOS("prestamos", "Préstamos", Icons.Default.SwapHoriz),
    DASHBOARD("dashboard", "Dashboard", Icons.Default.Dashboard),
    PERFIL("perfil", "Mi Perfil", Icons.Default.Person);
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliotecaApp(
    viewModel: BibliotecaViewModel,
    modifier: Modifier = Modifier
) {
    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados observados desde el ViewModel
    val mensajeNotificacion by viewModel.mensajeNotificacion.collectAsState()
    val isLibroDialogOpen by viewModel.isLibroDialogOpen.collectAsState()
    val libroAEditar by viewModel.libroAEditar.collectAsState()
    val isPrestamoDialogOpen by viewModel.isPrestamoDialogOpen.collectAsState()
    val libroSeleccionadoParaPrestamo by viewModel.libroSeleccionadoParaPrestamo.collectAsState()
    val libroSeleccionadoParaVisor by viewModel.libroSeleccionadoParaVisor.collectAsState()
    val catalogoUiState by viewModel.catalogoUiState.collectAsState()

    // Estados de Solicitudes (Fase 2)
    val totalPendientes by viewModel.totalSolicitudesPendientes.collectAsState()
    val libroParaSolicitar by viewModel.libroParaSolicitar.collectAsState()
    val solicitudParaRechazar by viewModel.solicitudParaRechazar.collectAsState()

    // Estados de Multas (Fase 3)
    val isGestionMultasDialogOpen by viewModel.isGestionMultasDialogOpen.collectAsState()

    // Estados de Reportes (Fase 4)
    val isReportesDialogOpen by viewModel.isReportesDialogOpen.collectAsState()

    // Panel Administrativo de Base de Datos e Inventario
    val isPanelInventarioOpen by viewModel.isPanelInventarioOpen.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Mostrar Notificaciones tipo Snackbar
    LaunchedEffect(mensajeNotificacion) {
        mensajeNotificacion?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.limpiarMensaje()
        }
    }

    var showRegistroScreen by remember { mutableStateOf(false) }

    // SI NO HAY USUARIO AUTENTICADO -> MOSTRAR PANTALLA DE LOGIN O REGISTRO
    if (usuarioActual == null) {
        if (showRegistroScreen) {
            RegistroEstudianteScreen(
                viewModel = viewModel,
                onNavigateToLogin = { showRegistroScreen = false },
                modifier = modifier
            )
        } else {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegistro = { showRegistroScreen = true },
                modifier = modifier
            )
        }
        return
    }

    val usuario = usuarioActual!!
    val esBibliotecario = usuario.esBibliotecario

    // Destinos según el rol activo
    val destinations = remember(esBibliotecario) {
        if (esBibliotecario) {
            listOf(
                AppDestination.CATALOGO,
                AppDestination.SOLICITUDES,
                AppDestination.PRESTAMOS,
                AppDestination.DASHBOARD
            )
        } else {
            listOf(
                AppDestination.CATALOGO,
                AppDestination.PRESTAMOS,
                AppDestination.PERFIL
            )
        }
    }

    var currentDestination by remember(esBibliotecario) {
        mutableStateOf(AppDestination.CATALOGO)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (currentDestination) {
                                AppDestination.CATALOGO -> "Catálogo Bibliográfico"
                                AppDestination.SOLICITUDES -> "Gestión de Solicitudes"
                                AppDestination.PRESTAMOS -> if (esBibliotecario) "Gestión de Préstamos" else "Mis Préstamos"
                                AppDestination.DASHBOARD -> "Panel Institucional UNAMAD"
                                AppDestination.PERFIL -> "Mi Carnet de Lector"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = if (esBibliotecario) "Portal de Administración" else "Portal del Estudiante",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    // Chip con Nombre y Rol del Usuario
                    Surface(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable {
                                if (!esBibliotecario) {
                                    currentDestination = AppDestination.PERFIL
                                } else {
                                    showLogoutDialog = true
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (esBibliotecario) AmberAccent else Color.White,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (esBibliotecario) Icons.Default.AdminPanelSettings else Icons.Default.School,
                                        contentDescription = null,
                                        tint = UnamadGreenPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = usuario.nombre.split(" ").firstOrNull() ?: usuario.nombre,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Botón para sincronizar catálogo con el servidor VPS
                    IconButton(onClick = { viewModel.sincronizarConServidor() }) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sincronizar con Servidor",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Botón para Cerrar Sesión
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = UnamadGreenPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                destinations.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            if (destination == AppDestination.SOLICITUDES && totalPendientes > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = AmberAccent,
                                            contentColor = Color.Black
                                        ) {
                                            Text(
                                                text = "$totalPendientes",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.title
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UnamadGreenPrimary,
                            selectedTextColor = UnamadGreenPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        // Contenido de la pantalla actual
        when (currentDestination) {
            AppDestination.CATALOGO -> {
                CatalogoScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.SOLICITUDES -> {
                GestionSolicitudesScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.PRESTAMOS -> {
                PrestamosScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.DASHBOARD -> {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToCatalogo = { currentDestination = AppDestination.CATALOGO },
                    onNavigateToPrestamos = { currentDestination = AppDestination.PRESTAMOS },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.PERFIL -> {
                PerfilEstudianteScreen(
                    viewModel = viewModel,
                    onNavigateToCatalogo = { currentDestination = AppDestination.CATALOGO },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // Diálogo de Confirmación de Cierre de Sesión
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text(text = "Cerrar Sesión") },
                text = {
                    Text(
                        text = "¿Estás seguro de que deseas salir de la cuenta de ${usuario.nombreCompleto} (${usuario.rol.etiqueta})?"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.cerrarSesion()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo para Solicitar Préstamo (Estudiante)
        libroParaSolicitar?.let { libro ->
            SolicitarPrestamoDialog(
                libro = libro,
                estudiante = usuario,
                onDismiss = { viewModel.cerrarDialogoSolicitudEstudiante() },
                onConfirmar = { libroId, dias, notas ->
                    viewModel.enviarSolicitudPrestamo(libroId, dias, notas)
                }
            )
        }

        // Diálogo para Rechazar Solicitud con Motivo (Bibliotecario)
        solicitudParaRechazar?.let { solicitud ->
            RechazoSolicitudDialog(
                solicitud = solicitud,
                onDismiss = { viewModel.cerrarDialogoRechazo() },
                onConfirmarRechazo = { motivo ->
                    viewModel.confirmarRechazoSolicitud(solicitud.id, motivo)
                }
            )
        }

        // Diálogo modal para Registrar / Editar Libro (solo bibliotecarios)
        if (isLibroDialogOpen && esBibliotecario) {
            RegistroLibroDialog(
                libroParaEditar = libroAEditar,
                onDismiss = { viewModel.cerrarDialogoLibro() },
                onGuardar = { libro -> viewModel.guardarLibro(libro) }
            )
        }

        // Diálogo de Nuevo Préstamo manual (solo bibliotecarios)
        if (isPrestamoDialogOpen && esBibliotecario) {
            NuevoPrestamoDialog(
                libroPreseleccionado = libroSeleccionadoParaPrestamo,
                librosDisponibles = catalogoUiState.libros,
                onDismiss = { viewModel.cerrarDialogoPrestamo() },
                onRegistrar = { libroId, nombre, codigo, carrera, telefono, dias, notas ->
                    val fechaEstimada = System.currentTimeMillis() + (dias.toLong() * 24L * 60 * 60 * 1000)
                    viewModel.registrarPrestamo(
                        libroId = libroId,
                        nombreLector = nombre,
                        codigoLector = codigo,
                        carreraOArea = carrera,
                        telefonoLector = telefono,
                        fechaDevolucionEstimada = fechaEstimada,
                        notas = notas
                    )
                }
            )
        }

        // Visor digital de PDF (accesible para estudiantes y bibliotecarios)
        libroSeleccionadoParaVisor?.let { libroDigital ->
            DigitalViewerDialog(
                libro = libroDigital,
                onDismiss = { viewModel.cerrarVisorDigital() }
            )
        }

        // Diálogo de Gestión de Multas y Cobros (solo bibliotecarios - Fase 3)
        if (isGestionMultasDialogOpen && esBibliotecario) {
            GestionMultasDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.cerrarGestionMultasDialog() }
            )
        }

        // Diálogo de Reportes y Métricas UNAMAD (solo bibliotecarios - Fase 4)
        if (isReportesDialogOpen && esBibliotecario) {
            ReportesDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.cerrarReportesDialog() }
            )
        }

        // Panel Administrativo de Base de Datos e Inventario de Circulación (solo bibliotecarios)
        if (isPanelInventarioOpen && esBibliotecario) {
            PanelInventarioDatabaseScreen(
                viewModel = viewModel,
                onClose = { viewModel.cerrarPanelInventario() }
            )
        }
    }
}
