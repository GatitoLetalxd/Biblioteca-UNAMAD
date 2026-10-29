package com.example.biblioteca.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.biblioteca.model.EstadoSolicitud
import com.example.biblioteca.model.Prestamo
import com.example.biblioteca.model.SolicitudPrestamo
import com.example.biblioteca.ui.components.LoanCard
import com.example.biblioteca.ui.components.SwipeableLoanCard
import com.example.biblioteca.ui.theme.AmberAccent
import com.example.biblioteca.ui.theme.StatusAvailableBg
import com.example.biblioteca.ui.theme.StatusAvailableText
import com.example.biblioteca.ui.theme.StatusOutBg
import com.example.biblioteca.ui.theme.StatusOutText
import com.example.biblioteca.ui.theme.UnamadGreenPrimary
import com.example.biblioteca.ui.viewmodel.BibliotecaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrestamosScreen(
    viewModel: BibliotecaViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.prestamosUiState.collectAsState()
    val usuario by viewModel.usuarioActual.collectAsState()
    val misSolicitudes by viewModel.misSolicitudesEstudiante.collectAsState()
    val esBibliotecario = usuario?.esBibliotecario == true

    // Pestaña interna para estudiantes: 0 = Mis Solicitudes, 1 = En Préstamo, 2 = Historial
    var pestanaEstudiante by remember { mutableIntStateOf(0) }

    Scaffold(
        floatingActionButton = {
            if (esBibliotecario) {
                FloatingActionButton(
                    onClick = { viewModel.abrirDialogoPrestamo(null) },
                    containerColor = UnamadGreenPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Préstamo")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Pestañas diferenciadas según rol
            if (esBibliotecario) {
                PrimaryTabRow(
                    selectedTabIndex = uiState.pestanaSeleccionada,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = UnamadGreenPrimary
                ) {
                    Tab(
                        selected = uiState.pestanaSeleccionada == 0,
                        onClick = { viewModel.onPrestamosPestanaChange(0) },
                        text = {
                            Text(
                                text = "Activos (${uiState.prestamosActivos.size})",
                                fontWeight = if (uiState.pestanaSeleccionada == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = uiState.pestanaSeleccionada == 1,
                        onClick = { viewModel.onPrestamosPestanaChange(1) },
                        text = {
                            Text(
                                text = "Historial (${uiState.historialDevueltos.size})",
                                fontWeight = if (uiState.pestanaSeleccionada == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            } else {
                PrimaryTabRow(
                    selectedTabIndex = pestanaEstudiante,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = UnamadGreenPrimary
                ) {
                    Tab(
                        selected = pestanaEstudiante == 0,
                        onClick = { pestanaEstudiante = 0 },
                        text = {
                            Text(
                                text = "Solicitudes (${misSolicitudes.size})",
                                fontWeight = if (pestanaEstudiante == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = pestanaEstudiante == 1,
                        onClick = { pestanaEstudiante = 1 },
                        text = {
                            Text(
                                text = "En Préstamo (${uiState.prestamosActivos.size})",
                                fontWeight = if (pestanaEstudiante == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = pestanaEstudiante == 2,
                        onClick = { pestanaEstudiante = 2 },
                        text = {
                            Text(
                                text = "Historial (${uiState.historialDevueltos.size})",
                                fontWeight = if (pestanaEstudiante == 2) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Alerta si hay préstamos vencidos
            val hayVencidos = (esBibliotecario && uiState.pestanaSeleccionada == 0 && uiState.totalVencidos > 0) ||
                    (!esBibliotecario && pestanaEstudiante == 1 && uiState.totalVencidos > 0)

            if (hayVencidos) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (esBibliotecario)
                            "Atención: Hay ${uiState.totalVencidos} préstamo(s) que han superado la fecha límite de devolución."
                        else
                            "Atención: Tienes ${uiState.totalVencidos} préstamo(s) con fecha límite vencida.",
                        fontSize = 12.sp,
                        color = Color(0xFF991B1B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // VISTA DEL ESTUDIANTE: Pestaña 0 = Mis Solicitudes
            if (!esBibliotecario && pestanaEstudiante == 0) {
                if (misSolicitudes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Sin solicitudes registradas",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Cuando solicites un libro físico desde el Catálogo, podrás ver aquí su estado de aprobación en tiempo real.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(misSolicitudes, key = { it.id }) { solicitud ->
                            SolicitudStudentCard(
                                solicitud = solicitud,
                                onCancelar = { viewModel.cancelarSolicitudEstudiante(solicitud) }
                            )
                        }
                    }
                }
            } else {
                // VISTA DE PRÉSTAMOS (Bibliotecario o Estudiante en tabs 1 y 2)
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onPrestamosQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = {
                        Text(
                            if (esBibliotecario) "Buscar por estudiante, código o libro..."
                            else "Buscar por título o autor..."
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (uiState.query.isNotBlank()) {
                            IconButton(onClick = { viewModel.onPrestamosQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                val listaMostrar = if (esBibliotecario) {
                    if (uiState.pestanaSeleccionada == 0) uiState.prestamosActivos else uiState.historialDevueltos
                } else {
                    if (pestanaEstudiante == 1) uiState.prestamosActivos else uiState.historialDevueltos
                }

                if (esBibliotecario && uiState.pestanaSeleccionada == 0 && listaMostrar.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡 Tip: Puedes deslizar una tarjeta hacia la derecha 👉 para registrar la devolución de inmediato.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (listaMostrar.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay libros en este listado",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (esBibliotecario)
                                    "Presiona el botón (+) para registrar préstamos manuales."
                                else
                                    "Tus libros prestados o devueltos se visualizarán aquí.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listaMostrar, key = { it.id }) { prestamo ->
                            if (esBibliotecario && uiState.pestanaSeleccionada == 0) {
                                SwipeableLoanCard(
                                    prestamo = prestamo,
                                    onDevolverClick = { viewModel.registrarDevolucion(prestamo) }
                                )
                            } else {
                                LoanCard(
                                    prestamo = prestamo,
                                    onDevolverClick = {
                                        if (esBibliotecario) {
                                            viewModel.registrarDevolucion(prestamo)
                                        }
                                    },
                                    puedeDevolver = esBibliotecario
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SolicitudStudentCard(
    solicitud: SolicitudPrestamo,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val fechaSolicitudStr = dateFormat.format(Date(solicitud.fechaSolicitud))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = when (solicitud.estado) {
                    EstadoSolicitud.PENDIENTE -> AmberAccent.copy(alpha = 0.5f)
                    EstadoSolicitud.APROBADA -> Color(0xFF10B981).copy(alpha = 0.4f)
                    EstadoSolicitud.RECHAZADA -> MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                    EstadoSolicitud.CANCELADA -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solicitud.tituloLibro.ifBlank { "Libro #${solicitud.libroId}" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = when (solicitud.estado) {
                        EstadoSolicitud.PENDIENTE -> Color(0xFFFEF3C7)
                        EstadoSolicitud.APROBADA -> StatusAvailableBg
                        EstadoSolicitud.RECHAZADA -> StatusOutBg
                        EstadoSolicitud.CANCELADA -> MaterialTheme.colorScheme.surface
                    }
                ) {
                    Text(
                        text = when (solicitud.estado) {
                            EstadoSolicitud.PENDIENTE -> "⏳ En Revisión"
                            EstadoSolicitud.APROBADA -> "✅ Aprobada"
                            EstadoSolicitud.RECHAZADA -> "❌ Rechazada"
                            EstadoSolicitud.CANCELADA -> "🚫 Cancelada"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (solicitud.estado) {
                            EstadoSolicitud.PENDIENTE -> Color(0xFFB45309)
                            EstadoSolicitud.APROBADA -> StatusAvailableText
                            EstadoSolicitud.RECHAZADA -> StatusOutText
                            EstadoSolicitud.CANCELADA -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (solicitud.autorLibro.isNotBlank()) {
                Text(
                    text = "Autor: ${solicitud.autorLibro}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cuadro informativo según estado
            when (solicitud.estado) {
                EstadoSolicitud.PENDIENTE -> {
                    Surface(
                        color = Color(0xFFFEF3C7).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tu solicitud está en revisión por el bibliotecario. Plazo pedido: ${solicitud.diasSolicitados} días.",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
                EstadoSolicitud.APROBADA -> {
                    Surface(
                        color = StatusAvailableBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusAvailableText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¡Solicitud aprobada! Puedes retirar tu ejemplar en el mostrador central de la biblioteca.",
                                fontSize = 11.sp,
                                color = StatusAvailableText
                            )
                        }
                    }
                }
                EstadoSolicitud.RECHAZADA -> {
                    Surface(
                        color = StatusOutBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                tint = StatusOutText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Motivo de rechazo: ${solicitud.motivoRechazo ?: "No especificado"}",
                                fontSize = 11.sp,
                                color = StatusOutText
                            )
                        }
                    }
                }
                EstadoSolicitud.CANCELADA -> {
                    Text(
                        text = "Solicitud cancelada por el usuario.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enviada: $fechaSolicitudStr",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (solicitud.estado == EstadoSolicitud.PENDIENTE) {
                    TextButton(onClick = onCancelar) {
                        Text(
                            text = "Cancelar Solicitud",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
