package com.example.biblioteca.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.biblioteca.model.EstadoPrestamo
import com.example.biblioteca.model.FormatoLibro
import com.example.biblioteca.model.LibroConCirculacion
import com.example.biblioteca.model.Prestamo
import com.example.biblioteca.ui.components.BookCoverImage
import com.example.biblioteca.ui.theme.AmberAccent
import com.example.biblioteca.ui.theme.StatusAvailableBg
import com.example.biblioteca.ui.theme.StatusAvailableText
import com.example.biblioteca.ui.theme.StatusOutBg
import com.example.biblioteca.ui.theme.StatusOutText
import com.example.biblioteca.ui.theme.UnamadGreenDark
import com.example.biblioteca.ui.theme.UnamadGreenPrimary
import com.example.biblioteca.ui.viewmodel.BibliotecaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelInventarioDatabaseScreen(
    viewModel: BibliotecaViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inventario by viewModel.inventarioConLectores.collectAsState()
    val prestamosState by viewModel.prestamosUiState.collectAsState()
    var tabSeleccionada by remember { mutableIntStateOf(0) } // 0: Base de Datos de Libros, 1: Lectores en Posesión
    var searchQuery by remember { mutableStateOf("") }
    var soloConPrestamos by remember { mutableStateOf(false) }

    val todosPrestamosActivos = prestamosState.prestamosActivos

    // Métricas para la cabecera
    val totalTitulos = inventario.size
    val totalEjemplares = inventario.filter { it.libro.formato != FormatoLibro.DIGITAL }.sumOf { it.libro.ejemplaresTotales }
    val totalDisponibles = inventario.filter { it.libro.formato != FormatoLibro.DIGITAL }.sumOf { it.libro.ejemplaresDisponibles }
    val totalEnManos = todosPrestamosActivos.size

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Base de Datos e Inventario",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Acervo bibliográfico y lectores con libros físicos en posesión",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = UnamadGreenPrimary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Barra de Métricas Rápidas de Base de Datos
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalTitulos", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Títulos", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalEjemplares", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Total Físicos", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalDisponibles", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF10B981))
                            Text(text = "En Estante", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$totalEnManos", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AmberAccent)
                            Text(text = "En Lectores", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Pestañas Principales
                PrimaryTabRow(
                    selectedTabIndex = tabSeleccionada,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = UnamadGreenPrimary
                ) {
                    Tab(
                        selected = tabSeleccionada == 0,
                        onClick = { tabSeleccionada = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Registro Libros ($totalTitulos)", fontSize = 12.sp, fontWeight = if (tabSeleccionada == 0) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                    Tab(
                        selected = tabSeleccionada == 1,
                        onClick = { tabSeleccionada = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AssignmentInd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lectores con Libros ($totalEnManos)", fontSize = 12.sp, fontWeight = if (tabSeleccionada == 1) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }

                // Barra de Búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (tabSeleccionada == 0) "Buscar por título, autor, categoría o ISBN..."
                            else "Buscar por alumno, código, carrera o libro..."
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (tabSeleccionada == 0) {
                    // PESTAÑA 0: REGISTRO DE BASE DE DATOS DE LIBROS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = soloConPrestamos,
                            onClick = { soloConPrestamos = !soloConPrestamos },
                            label = { Text("Solo con ejemplares en préstamo (${inventario.count { it.enPosesionDeLectores }})", fontSize = 11.sp) }
                        )
                    }

                    val filtrados = inventario.filter { item ->
                        val cleanQuery = searchQuery.trim()
                        val coincide = cleanQuery.isBlank() ||
                                item.libro.titulo.contains(cleanQuery, ignoreCase = true) ||
                                item.libro.autor.contains(cleanQuery, ignoreCase = true) ||
                                item.libro.categoria.contains(cleanQuery, ignoreCase = true) ||
                                item.libro.isbn.contains(cleanQuery, ignoreCase = true) ||
                                item.libro.ubicacion.contains(cleanQuery, ignoreCase = true)

                        val coincideFiltro = !soloConPrestamos || item.enPosesionDeLectores
                        coincide && coincideFiltro
                    }

                    if (filtrados.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No se encontraron registros coincidentes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtrados, key = { it.libro.id }) { item ->
                                DatabaseBookRowCard(
                                    item = item,
                                    onDevolverPrestamo = { prestamo -> viewModel.registrarDevolucion(prestamo) }
                                )
                            }
                        }
                    }
                } else {
                    // PESTAÑA 1: LECTORES CON LIBROS FÍSICOS EN POSESIÓN
                    val cleanQuery = searchQuery.trim()
                    val lectoresFiltrados = if (cleanQuery.isBlank()) {
                        todosPrestamosActivos
                    } else {
                        todosPrestamosActivos.filter {
                            it.nombreLector.contains(cleanQuery, ignoreCase = true) ||
                            it.codigoLector.contains(cleanQuery, ignoreCase = true) ||
                            it.carreraOArea.contains(cleanQuery, ignoreCase = true) ||
                            it.tituloLibro.contains(cleanQuery, ignoreCase = true)
                        }
                    }

                    if (lectoresFiltrados.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusAvailableText,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Ningún lector tiene libros físicos pendientes",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Todos los ejemplares físicos de la universidad están en sus respectivos estantes.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(lectoresFiltrados, key = { it.id }) { prestamo ->
                                LectorEnPosesionCard(
                                    prestamo = prestamo,
                                    onDevolver = { viewModel.registrarDevolucion(prestamo) }
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
fun DatabaseBookRowCard(
    item: LibroConCirculacion,
    onDevolverPrestamo: (Prestamo) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    val libro = item.libro
    val tieneLectores = item.enPosesionDeLectores

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { expandido = !expandido }
            .animateContentSize()
            .border(
                width = 1.dp,
                color = if (tieneLectores) AmberAccent.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Miniatura de portada
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .width(46.dp)
                        .height(66.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    BookCoverImage(
                        portadaUrl = libro.portadaUrl,
                        titulo = libro.titulo,
                        autor = libro.autor,
                        categoria = libro.categoria,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "#${libro.id.toString().padStart(2, '0')} • ${libro.categoria.ifBlank { "General" }}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Icon(
                            imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = libro.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (expandido) 3 else 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Autor: ${libro.autor}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Indicadores de stock y ubicación física
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (libro.ubicacion.isNotBlank()) "📍 ${libro.ubicacion}" else "📍 General",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (libro.formato != FormatoLibro.DIGITAL) {
                            Text(
                                text = "Stock: ${libro.ejemplaresDisponibles}/${libro.ejemplaresTotales} disp.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (libro.ejemplaresDisponibles > 0) Color(0xFF10B981) else Color(0xFFDC2626)
                            )
                        } else {
                            Text(
                                text = "Digital (PDF Ilimitado)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6366F1)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badge de circulación de alumnos
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (tieneLectores) Color(0xFFFEF3C7) else StatusAvailableBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (tieneLectores) "👥 En posesión de ${item.ejemplaresPrestados} alumno(s)" else "✅ 0 en préstamo (Todo disponible en estante)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tieneLectores) Color(0xFF92400E) else StatusAvailableText
                    )
                    Text(
                        text = if (expandido) "Ocultar lectores ▲" else "Ver quiénes lo tienen ▼",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Sección Expandida: Detalle de Alumnos que tienen el libro físico
            if (expandido) {
                Spacer(modifier = Modifier.height(10.dp))
                if (item.prestamosFisicosActivos.isEmpty()) {
                    Text(
                        text = "Ningún ejemplar físico de este título se encuentra prestado actualmente.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(4.dp)
                    )
                } else {
                    Text(
                        text = "Lectores con ejemplares prestados:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        item.prestamosFisicosActivos.forEach { prestamo ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prestamo.nombreLector,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Cód: ${prestamo.codigoLector} • ${prestamo.carreraOArea}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Vence: ${dateFormat.format(Date(prestamo.fechaDevolucionEstimada))}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (prestamo.estado == EstadoPrestamo.VENCIDO) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Button(
                                        onClick = { onDevolverPrestamo(prestamo) },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = UnamadGreenPrimary,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Devolver", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LectorEnPosesionCard(
    prestamo: Prestamo,
    onDevolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val isVencido = prestamo.estado == EstadoPrestamo.VENCIDO

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isVencido) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Fila Alumno
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = prestamo.nombreLector,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cód: ${prestamo.codigoLector} • ${prestamo.carreraOArea}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isVencido) StatusOutBg else StatusAvailableBg
                ) {
                    Text(
                        text = if (isVencido) "VENCIDO" else "AL DÍA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isVencido) StatusOutText else StatusAvailableText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cuadro del Libro en posesión
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "📖 ${prestamo.tituloLibro}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Autor: ${prestamo.autorLibro}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Prestado: ${dateFormat.format(Date(prestamo.fechaPrestamo))}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Límite: ${dateFormat.format(Date(prestamo.fechaDevolucionEstimada))}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isVencido) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (prestamo.telefonoLector.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Contacto: ${prestamo.telefonoLector}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onDevolver,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVencido) AmberAccent else UnamadGreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Registrar Devolución y Liberar Stock", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
