package com.example.biblioteca.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.biblioteca.model.FormatoLibro
import com.example.biblioteca.model.Libro
import com.example.biblioteca.ui.components.BookCard
import com.example.biblioteca.ui.theme.UnamadGreenPrimary
import com.example.biblioteca.ui.viewmodel.BibliotecaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    viewModel: BibliotecaViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.catalogoUiState.collectAsState()
    val usuario by viewModel.usuarioActual.collectAsState()
    val esBibliotecario = usuario?.esBibliotecario == true

    Scaffold(
        floatingActionButton = {
            if (esBibliotecario) {
                FloatingActionButton(
                    onClick = { viewModel.abrirDialogoNuevoLibro(null) },
                    containerColor = UnamadGreenPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Registrar Libro")
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
            // Barra de Búsqueda
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por título, autor, categoría o ISBN...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.query.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
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

            // Chips de filtrado horizontal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Formato: Todos
                FilterChip(
                    selected = uiState.formatoSeleccionado == null,
                    onClick = { viewModel.onSelectFormato(null) },
                    label = { Text("Todos los formatos", fontSize = 12.sp) }
                )

                // Formato: Físico
                FilterChip(
                    selected = uiState.formatoSeleccionado == FormatoLibro.FISICO,
                    onClick = { viewModel.onSelectFormato(FormatoLibro.FISICO) },
                    label = { Text("Físicos", fontSize = 12.sp) }
                )

                // Formato: Digital
                FilterChip(
                    selected = uiState.formatoSeleccionado == FormatoLibro.DIGITAL,
                    onClick = { viewModel.onSelectFormato(FormatoLibro.DIGITAL) },
                    label = { Text("Digitales", fontSize = 12.sp) }
                )

                // Solo con ejemplares disponibles
                FilterChip(
                    selected = uiState.soloDisponibles,
                    onClick = { viewModel.onToggleSoloDisponibles() },
                    leadingIcon = if (uiState.soloDisponibles) {
                        { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    label = { Text("Solo disponibles", fontSize = 12.sp) }
                )

                // Categorías dinámicas
                uiState.categorias.forEach { cat ->
                    FilterChip(
                        selected = uiState.categoriaSeleccionada == cat,
                        onClick = { viewModel.onSelectCategoria(cat) },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Contador de resultados
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
            }

            // Lista de libros o estado vacío
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.libros.isEmpty()) {
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
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No se encontraron libros",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Prueba con otros términos de búsqueda o borra los filtros activos.",
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
                    items(uiState.libros, key = { it.id }) { libro ->
                        BookCard(
                            libro = libro,
                            onPrestarClick = { viewModel.abrirDialogoPrestamo(libro) },
                            onLeerClick = { viewModel.abrirVisorDigital(libro) },
                            onEditarClick = { viewModel.abrirDialogoNuevoLibro(libro) },
                            onEliminarClick = { viewModel.eliminarLibro(libro) },
                            esBibliotecario = esBibliotecario
                        )
                    }
                }
            }
        }
    }
}
