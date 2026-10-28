package com.example.biblioteca.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.biblioteca.R
import com.example.biblioteca.model.EstadoPrestamo
import com.example.biblioteca.model.FormatoLibro
import com.example.biblioteca.model.Libro
import com.example.biblioteca.model.Prestamo
import com.example.biblioteca.ui.theme.AmberAccent
import com.example.biblioteca.ui.theme.StatusAvailableBg
import com.example.biblioteca.ui.theme.StatusAvailableText
import com.example.biblioteca.ui.theme.StatusDigitalBg
import com.example.biblioteca.ui.theme.StatusDigitalText
import com.example.biblioteca.ui.theme.StatusLoanBg
import com.example.biblioteca.ui.theme.StatusLoanText
import com.example.biblioteca.ui.theme.StatusOutBg
import com.example.biblioteca.ui.theme.StatusOutText
import com.example.biblioteca.ui.theme.UnamadGreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusBadge(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun BookCoverImage(
    portadaUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val drawableResId = remember(portadaUrl) {
        if (portadaUrl.startsWith("http://") || portadaUrl.startsWith("https://") || portadaUrl.isBlank()) {
            0
        } else {
            context.resources.getIdentifier(portadaUrl, "drawable", context.packageName)
        }
    }

    if (drawableResId != 0) {
        Image(
            painter = painterResource(id = drawableResId),
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
        )
    } else if (portadaUrl.startsWith("http://") || portadaUrl.startsWith("https://")) {
        AsyncImage(
            model = portadaUrl,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier,
            error = painterResource(id = R.drawable.logo_unamad),
            placeholder = painterResource(id = R.drawable.logo_unamad)
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_unamad),
                contentDescription = "UNAMAD",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(52.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookCard(
    libro: Libro,
    onPrestarClick: () -> Unit,
    onLeerClick: () -> Unit,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit,
    modifier: Modifier = Modifier,
    esBibliotecario: Boolean = true
) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expandido = !expandido }
            .animateContentSize()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Fila superior: Categoría + Acciones rápidas (Editar / Eliminar solo bibliotecario)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = libro.categoria.ifBlank { "General" },
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (esBibliotecario) {
                        IconButton(onClick = onEditarClick, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(onClick = onEliminarClick, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Icon(
                        imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandido) "Colapsar" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila con Portada del Libro + Título y Autor
            Row(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .width(76.dp)
                        .height(112.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    BookCoverImage(
                        portadaUrl = libro.portadaUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = libro.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (expandido) 4 else 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "por ${libro.autor}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expandido) 2 else 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (libro.descripcion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = libro.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            maxLines = if (expandido) 8 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Datos adicionales mostrados al expandir
            if (expandido) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (libro.isbn.isNotBlank()) {
                            Text(
                                text = "Código ISBN: ${libro.isbn}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (libro.formato != FormatoLibro.DIGITAL) {
                            Text(
                                text = "Ejemplares totales: ${libro.ejemplaresTotales} | En sala/préstamo: ${libro.ejemplaresDisponibles}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (libro.ubicacion.isNotBlank()) {
                            Text(
                                text = "Ubicación física: ${libro.ubicacion}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges de Formato y Disponibilidad
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Formato Badge
                when (libro.formato) {
                    FormatoLibro.FISICO -> {
                        StatusBadge(
                            text = "Físico",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    FormatoLibro.DIGITAL -> {
                        StatusBadge(
                            text = "Digital PDF",
                            icon = Icons.Default.PictureAsPdf,
                            backgroundColor = StatusDigitalBg,
                            textColor = StatusDigitalText
                        )
                    }
                    FormatoLibro.HIBRIDO -> {
                        StatusBadge(
                            text = "Físico + PDF",
                            icon = Icons.Default.AutoStories,
                            backgroundColor = StatusDigitalBg,
                            textColor = StatusDigitalText
                        )
                    }
                }

                // Disponibilidad Badge (para libros físicos/híbridos)
                if (libro.formato != FormatoLibro.DIGITAL) {
                    if (libro.ejemplaresDisponibles > 0) {
                        StatusBadge(
                            text = "${libro.ejemplaresDisponibles} de ${libro.ejemplaresTotales} disp.",
                            icon = Icons.Default.CheckCircle,
                            backgroundColor = StatusAvailableBg,
                            textColor = StatusAvailableText
                        )
                    } else {
                        StatusBadge(
                            text = "Agotado (0 disp.)",
                            icon = Icons.Default.ErrorOutline,
                            backgroundColor = StatusOutBg,
                            textColor = StatusOutText
                        )
                    }
                }

                if (libro.ubicacion.isNotBlank() && !expandido) {
                    Text(
                        text = "📍 ${libro.ubicacion}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tieneDigital = libro.formato == FormatoLibro.DIGITAL || libro.formato == FormatoLibro.HIBRIDO
                val tieneFisico = libro.formato != FormatoLibro.DIGITAL
                val puedePrestar = tieneFisico && libro.ejemplaresDisponibles > 0

                if (tieneDigital) {
                    OutlinedButton(
                        onClick = onLeerClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Leer PDF", fontSize = 13.sp)
                    }
                }

                if (tieneFisico) {
                    Button(
                        onClick = onPrestarClick,
                        enabled = puedePrestar,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UnamadGreenPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (puedePrestar) {
                                if (esBibliotecario) "Prestar" else "Solicitar Préstamo"
                            } else "Sin Stock",
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoanCard(
    prestamo: Prestamo,
    onDevolverClick: () -> Unit,
    modifier: Modifier = Modifier,
    puedeDevolver: Boolean = true
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaPrestamoStr = dateFormat.format(Date(prestamo.fechaPrestamo))
    val fechaDevolucionEstimadaStr = dateFormat.format(Date(prestamo.fechaDevolucionEstimada))
    val isDevuelto = prestamo.estado == EstadoPrestamo.DEVUELTO
    val isVencido = prestamo.estado == EstadoPrestamo.VENCIDO

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
                color = if (isVencido) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Encabezado: Título del libro + Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prestamo.tituloLibro.ifBlank { "Libro #${prestamo.libroId}" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                when (prestamo.estado) {
                    EstadoPrestamo.ACTIVO -> {
                        StatusBadge(
                            text = "En Préstamo",
                            icon = Icons.Default.AccessTime,
                            backgroundColor = StatusLoanBg,
                            textColor = StatusLoanText
                        )
                    }
                    EstadoPrestamo.VENCIDO -> {
                        StatusBadge(
                            text = "Vencido",
                            icon = Icons.Default.Warning,
                            backgroundColor = StatusOutBg,
                            textColor = StatusOutText
                        )
                    }
                    EstadoPrestamo.DEVUELTO -> {
                        StatusBadge(
                            text = "Devuelto",
                            icon = Icons.Default.CheckCircle,
                            backgroundColor = StatusAvailableBg,
                            textColor = StatusAvailableText
                        )
                    }
                }
            }

            if (prestamo.autorLibro.isNotBlank()) {
                Text(
                    text = prestamo.autorLibro,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información del lector
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = prestamo.nombreLector,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cód: ${prestamo.codigoLector}${if (prestamo.carreraOArea.isNotBlank()) " • " + prestamo.carreraOArea else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fechas de préstamo y devolución
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Prestado el",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = fechaPrestamoStr,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isDevuelto) "Devuelto el" else "Fecha límite",
                        fontSize = 11.sp,
                        color = if (isVencido) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isDevuelto && prestamo.fechaDevolucionReal != null)
                            dateFormat.format(Date(prestamo.fechaDevolucionReal))
                        else
                            fechaDevolucionEstimadaStr,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isVencido) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (!isDevuelto) {
                Spacer(modifier = Modifier.height(10.dp))
                if (puedeDevolver) {
                    Button(
                        onClick = onDevolverClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVencido) AmberAccent else UnamadGreenPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Registrar Devolución", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Devolución presencial en mostrador de Biblioteca",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente Swipeable: Envuelve LoanCard permitiendo deslizar hacia la derecha (StartToEnd)
 * para devolver el libro de forma rápida, acompañado de confirmación háptica.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableLoanCard(
    prestamo: Prestamo,
    onDevolverClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onDevolverClick()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> UnamadGreenPrimary
                    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                },
                label = "swipeBackground"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Devolver",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Desliza para Devolver",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        modifier = modifier
    ) {
        LoanCard(
            prestamo = prestamo,
            onDevolverClick = onDevolverClick
        )
    }
}

@Composable
fun StatMetricCard(
    titulo: String,
    valor: String,
    subtitulo: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
