package com.example.biblioteca.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
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
import com.example.biblioteca.model.EstadoMulta
import com.example.biblioteca.model.Multa
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
fun GestionMultasDialog(
    viewModel: BibliotecaViewModel,
    onDismiss: () -> Unit
) {
    val multasUiState by viewModel.multasUiState.collectAsState()
    var tabSeleccionada by remember { mutableIntStateOf(0) }

    val listaMostrar = if (tabSeleccionada == 0) multasUiState.multasPendientes else multasUiState.historialMultas

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Control de Multas y Deudas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
            ) {
                // Banner Resumen de recaudación pendiente
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monto Total por Cobrar",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "S/. ${String.format(Locale.US, "%.2f", multasUiState.totalMontoPendiente)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB45309)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFF59E0B)
                        ) {
                            Text(
                                text = "${multasUiState.multasPendientes.size} pendiente(s)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pestañas (Pendientes vs Historial)
                PrimaryTabRow(
                    selectedTabIndex = tabSeleccionada,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = UnamadGreenPrimary
                ) {
                    Tab(
                        selected = tabSeleccionada == 0,
                        onClick = { tabSeleccionada = 0 },
                        text = {
                            Text(
                                text = "Pendientes (${multasUiState.multasPendientes.size})",
                                fontWeight = if (tabSeleccionada == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = tabSeleccionada == 1,
                        onClick = { tabSeleccionada = 1 },
                        text = {
                            Text(
                                text = "Historial (${multasUiState.historialMultas.size})",
                                fontWeight = if (tabSeleccionada == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (listaMostrar.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (tabSeleccionada == 0) "No hay multas pendientes" else "Sin historial de multas",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (tabSeleccionada == 0)
                                    "Los estudiantes se encuentran al día con sus préstamos y entregas."
                                else
                                    "Aquí se registrarán los pagos y condonaciones efectuados.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listaMostrar, key = { it.id }) { multa ->
                            MultaCard(
                                multa = multa,
                                onCobrar = { viewModel.cobrarMulta(multa) },
                                onCondonar = { viewModel.condonarMulta(multa, "Condonación académica autorizada") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun MultaCard(
    multa: Multa,
    onCobrar: () -> Unit,
    onCondonar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val fechaGeneracionStr = dateFormat.format(Date(multa.fechaGeneracion))
    val isPendiente = multa.estado == EstadoMulta.PENDIENTE

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = when (multa.estado) {
                    EstadoMulta.PENDIENTE -> Color(0xFFFCA5A5)
                    EstadoMulta.PAGADA -> Color(0xFF10B981).copy(alpha = 0.4f)
                    EstadoMulta.CONDONADA -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Fila superior: Libro + Monto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = multa.tituloLibro,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "S/. ${String.format(Locale.US, "%.2f", multa.montoTotal)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isPendiente) Color(0xFFDC2626) else UnamadGreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Estudiante y días de retraso
            Text(
                text = "Lector: ${multa.nombreEstudiante} (${multa.codigoEstudiante})",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Días de mora: ${multa.diasRetraso} día(s)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309)
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = when (multa.estado) {
                        EstadoMulta.PENDIENTE -> StatusOutBg
                        EstadoMulta.PAGADA -> StatusAvailableBg
                        EstadoMulta.CONDONADA -> MaterialTheme.colorScheme.surface
                    }
                ) {
                    Text(
                        text = multa.estado.etiqueta,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (multa.estado) {
                            EstadoMulta.PENDIENTE -> StatusOutText
                            EstadoMulta.PAGADA -> StatusAvailableText
                            EstadoMulta.CONDONADA -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (multa.observaciones.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Obs: ${multa.observaciones}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Acciones para multas pendientes
            if (isPendiente) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCondonar,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Condonar", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onCobrar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UnamadGreenPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Paid,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cobrar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
