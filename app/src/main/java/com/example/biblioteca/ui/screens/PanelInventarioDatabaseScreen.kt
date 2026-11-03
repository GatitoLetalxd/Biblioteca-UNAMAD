package com.example.biblioteca.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
    var libroParaDetalles by remember { mutableStateOf<LibroConCirculacion?>(null) }

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
                        containerColor = MaterialTheme.colorScheme.primary,
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
                    contentColor = MaterialTheme.colorScheme.primary
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
                                    onVerDetalles = { libroParaDetalles = item }
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

        // Modal Detallado con Portada, Código, Resumen, Inventario y Lectores
        libroParaDetalles?.let { itemSeleccionado ->
            // Se sincroniza automáticamente con el estado más reciente del inventario
            val itemActualizado = inventario.firstOrNull { it.libro.id == itemSeleccionado.libro.id } ?: itemSeleccionado
            DetalleLibroInventarioDialog(
                item = itemActualizado,
                onDismiss = { libroParaDetalles = null },
                onDevolverPrestamo = { prestamo -> viewModel.registrarDevolucion(prestamo) }
            )
        }
    }
}

@Composable
fun DatabaseBookRowCard(
    item: LibroConCirculacion,
    onVerDetalles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val libro = item.libro
    val tieneLectores = item.enPosesionDeLectores

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onVerDetalles() }
            .border(
                width = 1.dp,
                color = if (tieneLectores) AmberAccent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Fila 1: Badges de ID, Categoría y Formato
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ID Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "#${libro.id.toString().padStart(3, '0')}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Categoría Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = libro.categoria.ifBlank { "General" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Formato Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (libro.formato) {
                        FormatoLibro.DIGITAL -> Color(0xFFEDE9FE)
                        FormatoLibro.HIBRIDO -> Color(0xFFCCFBF1)
                        else -> Color(0xFFE0F2FE)
                    }
                ) {
                    Text(
                        text = when (libro.formato) {
                            FormatoLibro.DIGITAL -> "Digital"
                            FormatoLibro.HIBRIDO -> "Híbrido"
                            else -> "Físico"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (libro.formato) {
                            FormatoLibro.DIGITAL -> Color(0xFF6D28D9)
                            FormatoLibro.HIBRIDO -> Color(0xFF0F766E)
                            else -> Color(0xFF0369A1)
                        },
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila 2: Título del libro (Solo texto limpio)
            Text(
                text = libro.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Fila 3: Autor e ISBN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Autor: ${libro.autor}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (libro.isbn.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ISBN: ${libro.isbn}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila 4: Ubicación y Stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (libro.formato == FormatoLibro.DIGITAL) {
                        "Repositorio Digital UNAMAD"
                    } else if (libro.ubicacion.isNotBlank()) {
                        "${libro.ubicacion}"
                    } else {
                        "Estante General"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (libro.formato != FormatoLibro.DIGITAL) {
                    Text(
                        text = "Stock: ${libro.ejemplaresDisponibles}/${libro.ejemplaresTotales} disp.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (libro.ejemplaresDisponibles > 0) Color(0xFF10B981) else Color(0xFFDC2626)
                    )
                } else {
                    Text(
                        text = "Acceso Ilimitado (PDF)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6366F1)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila 5: Estado de circulación rápida
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
                        text = if (tieneLectores) "En posesión de ${item.ejemplaresPrestados} alumno(s)" else "0 en préstamo (Disponible en estante)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tieneLectores) Color(0xFF92400E) else StatusAvailableText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fila 6: Botón de Ver Detalles / Más Detalles
            Button(
                onClick = onVerDetalles,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ver más detalles",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DetalleLibroInventarioDialog(
    item: LibroConCirculacion,
    onDismiss: () -> Unit,
    onDevolverPrestamo: (Prestamo) -> Unit
) {
    val libro = item.libro
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Cabecera del diálogo modal
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ficha Bibliográfica y Detalles",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Código Institucional #${libro.id.toString().padStart(3, '0')}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Contenido Scrolleable
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Sección 1: Portada y Ficha Técnica Superior
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Portada destacada del libro
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .width(115.dp)
                                .height(165.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            BookCoverImage(
                                portadaUrl = libro.portadaUrl,
                                titulo = libro.titulo,
                                autor = libro.autor,
                                categoria = libro.categoria,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Metadatos al costado de la portada
                        Column(modifier = Modifier.weight(1f)) {
                            // Badges: Categoría y Formato con FlowRow para evitar truncamiento
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = libro.categoria.ifBlank { "General" },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (libro.formato) {
                                        FormatoLibro.DIGITAL -> Color(0xFFEDE9FE)
                                        FormatoLibro.HIBRIDO -> Color(0xFFCCFBF1)
                                        else -> Color(0xFFE0F2FE)
                                    }
                                ) {
                                    Text(
                                        text = when (libro.formato) {
                                            FormatoLibro.DIGITAL -> "Digital"
                                            FormatoLibro.HIBRIDO -> "Híbrido"
                                            else -> "Físico"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (libro.formato) {
                                            FormatoLibro.DIGITAL -> Color(0xFF6D28D9)
                                            FormatoLibro.HIBRIDO -> Color(0xFF0F766E)
                                            else -> Color(0xFF0369A1)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = libro.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Autor: ${libro.autor}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "ISBN: ${if (libro.isbn.isNotBlank()) libro.isbn else "No asignado"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Código ID: #${libro.id.toString().padStart(4, '0')}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Sección 2: Cuadro de Inventario y Ubicación
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Ubicación y Disponibilidad",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (libro.formato == FormatoLibro.DIGITAL) {
                                    "Repositorio: Repositorio Digital Institucional UNAMAD"
                                } else {
                                    "Ubicación en Sala: ${libro.ubicacion.ifBlank { "Estantería Principal General" }}"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Ejemplares Totales", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = if (libro.formato == FormatoLibro.DIGITAL) "Ilimitado" else "${libro.ejemplaresTotales}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text(text = "En Estante (Disponibles)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = if (libro.formato == FormatoLibro.DIGITAL) "Ilimitado" else "${libro.ejemplaresDisponibles}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (libro.ejemplaresDisponibles > 0 || libro.formato == FormatoLibro.DIGITAL) Color(0xFF10B981) else Color(0xFFDC2626)
                                    )
                                }
                                Column {
                                    Text(text = "En Préstamo Alumnos", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${item.ejemplaresPrestados}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.ejemplaresPrestados > 0) AmberAccent else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Sección 3: Resumen / Sinopsis del Libro
                    Column {
                        Text(
                            text = "Resumen y Sinopsis",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (libro.descripcion.isNotBlank()) libro.descripcion else "Este libro no posee una sinopsis o resumen registrado en el catálogo institucional.",
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = if (libro.descripcion.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Sección 4: Alumnos con ejemplares físicos prestados
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ejemplares en Posesión de Lectores",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (item.prestamosFisicosActivos.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = "${item.prestamosFisicosActivos.size} activo(s)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (item.prestamosFisicosActivos.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = StatusAvailableBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusAvailableText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Todos los ejemplares físicos están en el estante de la biblioteca. Ninguno se encuentra prestado actualmente.",
                                        fontSize = 11.sp,
                                        color = StatusAvailableText
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item.prestamosFisicosActivos.forEach { prestamo ->
                                    val isVencido = prestamo.estado == EstadoPrestamo.VENCIDO
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, if (isVencido) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
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
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    if (prestamo.telefonoLector.isNotBlank()) {
                                                        Text(
                                                            text = "${prestamo.telefonoLector}",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Text(
                                                        text = "Devolución límite: ${dateFormat.format(Date(prestamo.fechaDevolucionEstimada))}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isVencido) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                Button(
                                                    onClick = { onDevolverPrestamo(prestamo) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = Color.White
                                                    ),
                                                    modifier = Modifier.padding(start = 8.dp)
                                                ) {
                                                    Text("Devolver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Botón Inferior de Cierre
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Cerrar Ficha", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        text = "${prestamo.tituloLibro}",
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
                    containerColor = if (isVencido) AmberAccent else MaterialTheme.colorScheme.primary,
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
