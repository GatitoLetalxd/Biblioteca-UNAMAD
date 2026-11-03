package com.example.biblioteca.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.biblioteca.model.FormatoLibro
import com.example.biblioteca.ui.components.BookCard
import com.example.biblioteca.ui.components.BookCardSkeleton
import com.example.biblioteca.ui.components.EmptyStateView

import com.example.biblioteca.ui.viewmodel.BibliotecaViewModel
import com.example.biblioteca.ui.viewmodel.OrdenamientoCatalogo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatalogoScreen(
    viewModel: BibliotecaViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.catalogoUiState.collectAsState()
    val usuario by viewModel.usuarioActual.collectAsState()
    val esBibliotecario = usuario?.esBibliotecario == true
    val haptic = LocalHapticFeedback.current

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // Contador de filtros activos
    val activeFiltersCount = remember(
        uiState.categoriaSeleccionada,
        uiState.formatoSeleccionado,
        uiState.soloDisponibles,
        uiState.ordenamiento
    ) {
        var count = 0
        if (uiState.categoriaSeleccionada != null) count++
        if (uiState.formatoSeleccionado != null) count++
        if (uiState.soloDisponibles) count++
        if (uiState.ordenamiento != OrdenamientoCatalogo.TITULO_ASC) count++
        count
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            if (esBibliotecario) {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.abrirDialogoNuevoLibro(null)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Registrar Libro")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.sincronizarConServidor()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barra de Búsqueda Moderna y Acciones (Scanner y Filtros)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Buscar título, autor, ISBN...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = if (uiState.query.isNotBlank()) {
                            {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpiar",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    // Botón de Filtros Avanzados y Ordenamiento con Badge
                    BadgedBox(
                        badge = {
                            if (activeFiltersCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ) {
                                    Text(text = activeFiltersCount.toString())
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showBottomSheet = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filtros y Ordenamiento",
                                tint = if (activeFiltersCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                // Chips de Acceso Rápido
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chip para abrir ordenamiento rápido
                    FilterChip(
                        selected = uiState.ordenamiento != OrdenamientoCatalogo.TITULO_ASC,
                        onClick = { showBottomSheet = true },
                        leadingIcon = {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        label = { Text(uiState.ordenamiento.label, fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = uiState.formatoSeleccionado == FormatoLibro.FISICO,
                        onClick = { viewModel.onSelectFormato(if (uiState.formatoSeleccionado == FormatoLibro.FISICO) null else FormatoLibro.FISICO) },
                        label = { Text("Físicos", fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = uiState.formatoSeleccionado == FormatoLibro.DIGITAL,
                        onClick = { viewModel.onSelectFormato(if (uiState.formatoSeleccionado == FormatoLibro.DIGITAL) null else FormatoLibro.DIGITAL) },
                        label = { Text("Digitales", fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = uiState.soloDisponibles,
                        onClick = { viewModel.onToggleSoloDisponibles() },
                        leadingIcon = if (uiState.soloDisponibles) {
                            { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        label = { Text("Solo disponibles", fontSize = 11.sp) }
                    )

                    uiState.categorias.take(4).forEach { cat ->
                        FilterChip(
                            selected = uiState.categoriaSeleccionada == cat,
                            onClick = { viewModel.onSelectCategoria(cat) },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                // Barra de Conteo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.libros.size} libros encontrados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (activeFiltersCount > 0) {
                        Text(
                            text = "Limpiar filtros",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                // Contenido: Skeleton Shimmer o Lista de Libros o Empty State
                if (uiState.isLoading) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(5) {
                            BookCardSkeleton()
                        }
                    }
                } else if (uiState.libros.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.SearchOff,
                        title = "No se encontraron libros",
                        subtitle = if (uiState.query.isNotBlank())
                            "No hay coincidencias para \"${uiState.query}\". Revisa la ortografía o intenta buscar por autor o categoría."
                        else "No hay títulos que coincidan con los filtros seleccionados.",
                        actionLabel = "Restablecer filtros",
                        onActionClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.resetFiltrosCatalogo()
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.libros, key = { it.id }) { libro ->
                            BookCard(
                                libro = libro,
                                onPrestarClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.abrirDialogoPrestamo(libro)
                                },
                                onLeerClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.abrirVisorDigital(libro)
                                },
                                onEditarClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.abrirDialogoNuevoLibro(libro)
                                },
                                onEliminarClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.eliminarLibro(libro)
                                },
                                esBibliotecario = esBibliotecario
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal BottomSheet de Filtros y Ordenamiento
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtros y Ordenamiento",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.resetFiltrosCatalogo() }) {
                        Text("Restablecer", color = MaterialTheme.colorScheme.error)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Sección 1: Criterio de Ordenamiento
                Text(
                    text = "Ordenar por:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OrdenamientoCatalogo.values().forEach { criterio ->
                        FilterChip(
                            selected = uiState.ordenamiento == criterio,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onOrdenamientoChange(criterio)
                            },
                            label = { Text(criterio.label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sección 2: Formato del libro
                Text(
                    text = "Formato de Lectura:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.formatoSeleccionado == null,
                        onClick = { viewModel.onSelectFormato(null) },
                        label = { Text("Todos", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = uiState.formatoSeleccionado == FormatoLibro.FISICO,
                        onClick = { viewModel.onSelectFormato(FormatoLibro.FISICO) },
                        label = { Text("Físico", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = uiState.formatoSeleccionado == FormatoLibro.DIGITAL,
                        onClick = { viewModel.onSelectFormato(FormatoLibro.DIGITAL) },
                        label = { Text("Digital PDF", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sección 3: Disponibilidad inmediata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Solo disponibles para préstamo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Oculta libros con stock 0 en sala",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.soloDisponibles,
                        onCheckedChange = { viewModel.onToggleSoloDisponibles() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sección 4: Categorías profesionales UNAMAD
                Text(
                    text = "Categoría o Carrera Profesional:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.categorias.forEach { cat ->
                        FilterChip(
                            selected = uiState.categoriaSeleccionada == cat,
                            onClick = { viewModel.onSelectCategoria(cat) },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) showBottomSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Aplicar Filtros (${uiState.libros.size} libros)")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
