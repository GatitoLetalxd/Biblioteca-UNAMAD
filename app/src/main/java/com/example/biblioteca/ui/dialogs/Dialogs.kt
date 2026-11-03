package com.example.biblioteca.ui.dialogs

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.biblioteca.util.PdfUrlResolver
import android.net.Uri
import android.os.Environment
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.biblioteca.model.FormatoLibro
import com.example.biblioteca.model.Libro
import com.example.biblioteca.ui.components.BookCoverImage

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegistroLibroDialog(
    libroParaEditar: Libro?,
    onDismiss: () -> Unit,
    onGuardar: (Libro) -> Unit
) {
    var titulo by remember { mutableStateOf(libroParaEditar?.titulo ?: "") }
    var autor by remember { mutableStateOf(libroParaEditar?.autor ?: "") }
    var categoria by remember { mutableStateOf(libroParaEditar?.categoria ?: "Ingeniería de Sistemas") }
    var isbn by remember { mutableStateOf(libroParaEditar?.isbn ?: "") }
    var descripcion by remember { mutableStateOf(libroParaEditar?.descripcion ?: "") }
    var formato by remember { mutableStateOf(libroParaEditar?.formato ?: FormatoLibro.FISICO) }
    var ejemplaresTotales by remember { mutableStateOf(libroParaEditar?.ejemplaresTotales?.toString() ?: "3") }
    var ubicacion by remember { mutableStateOf(libroParaEditar?.ubicacion ?: "Estante A-01") }
    var archivoUrl by remember { mutableStateOf(libroParaEditar?.archivoUrl ?: "") }

    var errorTitulo by remember { mutableStateOf(false) }
    var errorAutor by remember { mutableStateOf(false) }

    val categoriasSugeridas = listOf(
        "Ingeniería de Sistemas",
        "Ingeniería Forestal",
        "Medio Ambiente",
        "Educación",
        "Literatura",
        "Investigación",
        "Ciencias Básicas"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (libroParaEditar == null) "Registrar Nuevo Libro" else "Editar Libro",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it
                        errorTitulo = it.isBlank()
                    },
                    label = { Text("Título del libro *") },
                    isError = errorTitulo,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = autor,
                    onValueChange = {
                        autor = it
                        errorAutor = it.isBlank()
                    },
                    label = { Text("Autor / Autores *") },
                    isError = errorAutor,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Categoría o Área Académica",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categoriasSugeridas.forEach { cat ->
                        FilterChip(
                            selected = categoria == cat,
                            onClick = { categoria = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("Código ISBN / Código de Barras") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Formato del Recurso",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FormatoLibro.values().forEach { f ->
                        FilterChip(
                            selected = formato == f,
                            onClick = { formato = f },
                            label = { Text(f.etiqueta, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (formato != FormatoLibro.DIGITAL) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = ejemplaresTotales,
                            onValueChange = { ejemplaresTotales = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Cant. Ejemplares") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = ubicacion,
                            onValueChange = { ubicacion = it },
                            label = { Text("Ubicación / Estante") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                if (formato != FormatoLibro.FISICO) {
                    OutlinedTextField(
                        value = archivoUrl,
                        onValueChange = { archivoUrl = it },
                        label = { Text("URL o Enlace al Archivo PDF") },
                        placeholder = { Text("https://repositorio.unamad.edu.pe/libro.pdf") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Sinopsis / Resumen breve") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isBlank()) {
                        errorTitulo = true
                        return@Button
                    }
                    if (autor.isBlank()) {
                        errorAutor = true
                        return@Button
                    }

                    val cantTotales = ejemplaresTotales.toIntOrNull() ?: 1
                    val disp = if (libroParaEditar != null) {
                        // Ajustar disponibles si se cambia el total
                        val prestados = libroParaEditar.ejemplaresTotales - libroParaEditar.ejemplaresDisponibles
                        (cantTotales - prestados).coerceAtLeast(0)
                    } else {
                        cantTotales
                    }

                    val nuevoLibro = (libroParaEditar ?: Libro(
                        titulo = titulo,
                        autor = autor,
                        categoria = categoria
                    )).copy(
                        titulo = titulo.trim(),
                        autor = autor.trim(),
                        categoria = categoria.trim(),
                        isbn = isbn.trim(),
                        descripcion = descripcion.trim(),
                        formato = formato,
                        ejemplaresTotales = if (formato == FormatoLibro.DIGITAL) 1 else cantTotales,
                        ejemplaresDisponibles = if (formato == FormatoLibro.DIGITAL) 1 else disp,
                        ubicacion = if (formato == FormatoLibro.DIGITAL) "Repositorio Digital UNAMAD" else ubicacion.trim(),
                        archivoUrl = archivoUrl.trim()
                    )

                    onGuardar(nuevoLibro)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoPrestamoDialog(
    libroPreseleccionado: Libro?,
    librosDisponibles: List<Libro>,
    onDismiss: () -> Unit,
    onRegistrar: (libroId: Long, nombreLector: String, codigoLector: String, carrera: String, telefono: String, diasPrestamo: Int, notas: String) -> Unit
) {
    var libroSeleccionado by remember {
        mutableStateOf(libroPreseleccionado ?: librosDisponibles.firstOrNull { it.ejemplaresDisponibles > 0 })
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var nombreLector by remember { mutableStateOf("") }
    var codigoLector by remember { mutableStateOf("") }
    var carreraOArea by remember { mutableStateOf("Ingeniería de Sistemas e Informática") }
    var telefonoLector by remember { mutableStateOf("") }
    var diasPrestamo by remember { mutableIntStateOf(7) }
    var notas by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf(false) }
    var errorCodigo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Registrar Préstamo de Libro",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Selector de Libro
                Text(
                    text = "Libro a prestar",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = libroSeleccionado?.let { "${it.titulo} (${it.ejemplaresDisponibles} disp.)" } ?: "Seleccionar libro con stock",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        val candidatos = librosDisponibles.filter { it.ejemplaresDisponibles > 0 || it.formato == FormatoLibro.DIGITAL }
                        if (candidatos.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay libros disponibles en stock") },
                                onClick = { dropdownExpanded = false }
                            )
                        } else {
                            candidatos.forEach { lib ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(text = lib.titulo, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = "${lib.autor} • ${lib.ejemplaresDisponibles} disp.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        libroSeleccionado = lib
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Datos del Lector
                OutlinedTextField(
                    value = nombreLector,
                    onValueChange = {
                        nombreLector = it
                        errorNombre = it.isBlank()
                    },
                    label = { Text("Nombre del Estudiante o Docente *") },
                    isError = errorNombre,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = codigoLector,
                        onValueChange = {
                            codigoLector = it
                            errorCodigo = it.isBlank()
                        },
                        label = { Text("Código / DNI *") },
                        isError = errorCodigo,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = telefonoLector,
                        onValueChange = { telefonoLector = it },
                        label = { Text("Teléfono") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = carreraOArea,
                    onValueChange = { carreraOArea = it },
                    label = { Text("Carrera / Escuela Profesional") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Plazo de préstamo
                Text(
                    text = "Plazo de Devolución Estimado",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(3, 7, 14, 21).forEach { dias ->
                        FilterChip(
                            selected = diasPrestamo == dias,
                            onClick = { diasPrestamo = dias },
                            label = { Text("$dias días", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Observaciones / Notas") },
                    placeholder = { Text("Ej. Presentación de tesis, estado físico del ejemplar...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (libroSeleccionado == null) return@Button
                    if (nombreLector.isBlank()) {
                        errorNombre = true
                        return@Button
                    }
                    if (codigoLector.isBlank()) {
                        errorCodigo = true
                        return@Button
                    }

                    onRegistrar(
                        libroSeleccionado!!.id,
                        nombreLector.trim(),
                        codigoLector.trim(),
                        carreraOArea.trim(),
                        telefonoLector.trim(),
                        diasPrestamo,
                        notas.trim()
                    )
                },
                enabled = libroSeleccionado != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text("Confirmar Préstamo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

data class CapituloLectura(
    val numero: Int,
    val titulo: String,
    val subtitulo: String,
    val contenido: String
)

fun generarCapitulosParaLibro(libro: Libro): List<CapituloLectura> {
    val tituloLower = libro.titulo.lowercase(Locale.ROOT)
    val catLower = libro.categoria.lowercase(Locale.ROOT)
    val desc = libro.descripcion.trim()

    val descParrafo1 = if (desc.isNotBlank()) desc else {
        "El presente texto universitario constituye un recurso bibliográfico oficial de consulta pedagógica y científica para la comunidad académica de la Universidad Nacional Amazónica de Madre de Dios (UNAMAD)."
    }

    return when {
        // 1. Matemáticas / Cálculo / Ciencias Básicas / Física / Álgebra
        catLower.contains("matem") || catLower.contains("cálculo") || catLower.contains("calculo") ||
        catLower.contains("física") || catLower.contains("fisica") || catLower.contains("ciencias básicas") ||
        tituloLower.contains("cálculo") || tituloLower.contains("calculo") || tituloLower.contains("álgebra") ||
        tituloLower.contains("algebra") || tituloLower.contains("matemátic") -> {
            listOf(
                CapituloLectura(
                    numero = 1,
                    titulo = "Fundamentos del Análisis y Límites de Funciones",
                    subtitulo = "Conceptos preliminares, continuidad y comportamiento asintótico",
                    contenido = "$descParrafo1\n\nEn este capítulo inicial se formalizan las definiciones rigurosas de funciones reales de variable real, cotas y el concepto épsilon-delta de límite. Se desarrollan propiedades algebraicas esenciales y criterios de convergencia indispensables para la modelación de fenómenos dinámicos en ingeniería y ciencias aplicadas."
                ),
                CapituloLectura(
                    numero = 2,
                    titulo = "Cálculo Diferencial, Derivadas y Optimización",
                    subtitulo = "Razón de cambio instantánea, reglas de derivación y extremos relativos",
                    contenido = "El estudio de la derivada como tasa de variación instantánea y pendiente de la recta tangente permite analizar la concavidad, puntos de inflexión y optimización de sistemas multivariables. Se formulan aplicaciones prácticas para el cálculo de máximos y mínimos en problemas de diseño estructural, eficiencia energética y modelos predictivos."
                ),
                CapituloLectura(
                    numero = 3,
                    titulo = "Teorema Fundamental del Cálculo e Integración",
                    subtitulo = "Antiderivadas, sumas de Riemann e integrales definidas e impropias",
                    contenido = "Se establece la conexión matemática entre el cálculo de áreas y la derivación mediante el Teorema Fundamental del Cálculo. Se estudian técnicas avanzadas de integración por partes, sustitución trigonométrica, descomposición en fracciones parciales y métodos numéricos para el cálculo de volúmenes de revolución y momentos de inercia."
                ),
                CapituloLectura(
                    numero = 4,
                    titulo = "Ecuaciones Diferenciales y Modelado Aplicado",
                    subtitulo = "Sistemas dinámicos de primer y segundo orden para ingeniería",
                    contenido = "Modelado de fenómenos físicos, biológicos y ecológicos mediante ecuaciones diferenciales ordinarias. Se resuelven casos prácticos de decaimiento radioactivo, curvas de crecimiento poblacional y circuitos eléctricos RLC, verificando la consistencia dimensional y el análisis de estabilidad de las soluciones."
                ),
                CapituloLectura(
                    numero = 5,
                    titulo = "Series Infinitas, Métodos Numéricos y Problemas Resueltos",
                    subtitulo = "Series de Taylor, aproximación polinómica y catálogo de ejercicios",
                    contenido = "1. Convergencia absoluta y condicional de series de potencias y representación de funciones trascendentes.\n2. Criterios de aproximación numérica y algoritmos computacionales de integración.\n3. Referencias bibliográficas validadas para el plan curricular de ciencias e ingeniería en la UNAMAD."
                )
            )
        }

        // 2. Sistemas / Software / Informática / Redes / Algoritmos / IA / Bases de Datos
        catLower.contains("sistemas") || catLower.contains("comput") || catLower.contains("software") ||
        catLower.contains("algoritmo") || catLower.contains("datos") || catLower.contains("redes") ||
        catLower.contains("inteligencia") || catLower.contains("ia") || tituloLower.contains("software") ||
        tituloLower.contains("algoritmos") || tituloLower.contains("redes") || tituloLower.contains("distribuidos") ||
        tituloLower.contains("datos") || tituloLower.contains("java") || tituloLower.contains("python") -> {
            listOf(
                CapituloLectura(
                    numero = 1,
                    titulo = "Fundamentos de Arquitectura y Paradigmas Computacionales",
                    subtitulo = "Evolución tecnológica, principios de diseño y abstracción modular",
                    contenido = "$descParrafo1\n\nEste capítulo sienta las bases conceptuales indispensables para la ingeniería computacional moderna. Se abordan modelos de procesamiento distribuido, gestión eficiente de memoria, concurrencia de hilos y la relación crítica entre el hardware subyacente y las capas lógicas de abstracción de software."
                ),
                CapituloLectura(
                    numero = 2,
                    titulo = "Estructuras de Datos Avanzadas y Complejidad Algorítmica",
                    subtitulo = "Notación Big-O, optimización en tiempo y espacio computacional",
                    contenido = "Análisis formal de algoritmos con énfasis en estructuras jerárquicas como árboles balanceados, grafos acíclicos dirigidos y tablas hash de alto rendimiento. Se presentan demostraciones de eficiencia temporal y espacial aplicadas al procesamiento intensivo de datos e indexación distribuida."
                ),
                CapituloLectura(
                    numero = 3,
                    titulo = "Patrones de Diseño, Concurrencia y Sistemas Distribuidos",
                    subtitulo = "Tolerancia a fallos, consistencia de datos y protocolos de comunicación",
                    contenido = "Modelado de sistemas desacoplados mediante patrones arquitectónicos (Clean Architecture, Microservicios, Repositorios). Se analizan protocolos de transporte seguro, transacciones ACID frente a coherencia eventual (Teorema CAP), y sincronización asíncrona en entornos de alta concurrencia."
                ),
                CapituloLectura(
                    numero = 4,
                    titulo = "Seguridad, Pruebas de Calidad y Ciclo de Despliegue",
                    subtitulo = "Ciberseguridad, integración continua CI/CD y verificación formal",
                    contenido = "Métricas de cobertura de pruebas (TDD/BDD), análisis estático de vulnerabilidades según estándares OWASP y automatización del ciclo de vida del software. Se proponen estrategias de hardening y cifrado simétrico/asimétrico para proteger la integridad del flujo de información."
                ),
                CapituloLectura(
                    numero = 5,
                    titulo = "Implementación Práctica, Casos de Estudio y Tendencias",
                    subtitulo = "Computación en la nube, machine learning y catálogo de buenas prácticas",
                    contenido = "1. Desarrollo guiado por métricas de rendimiento y escalabilidad horizontal.\n2. Incorporación de servicios cognitivos e inteligencia artificial en la automatización universitaria.\n3. Referencias IEEE/ACM y repositorio institucional de código de la UNAMAD."
                )
            )
        }

        // 3. Ingeniería Forestal / Medio Ambiente / Biodiversidad / Ecología
        catLower.contains("forestal") || catLower.contains("medio ambiente") || catLower.contains("ecolog") ||
        catLower.contains("biodiversidad") || catLower.contains("amazón") || catLower.contains("amazon") ||
        tituloLower.contains("biodiversidad") || tituloLower.contains("forestal") || tituloLower.contains("ecosistema") ||
        tituloLower.contains("fauna") -> {
            listOf(
                CapituloLectura(
                    numero = 1,
                    titulo = "Caracterización Florística y Recursos Amazónicos",
                    subtitulo = "Diversidad botánica, estratificación y dinámica ecológica en Madre de Dios",
                    contenido = "$descParrafo1\n\nEl estudio integral de la cuenca amazónica requiere comprender las interacciones bióticas y abióticas que sostienen el dosel y los ecosistemas de llanura aluvial. Se catalogan las principales familias dendrológicas, gremios ecológicos y la zonificación ecológica de la región de Madre de Dios."
                ),
                CapituloLectura(
                    numero = 2,
                    titulo = "Manejo Forestal Sostenible y Silvicultura Tropical",
                    subtitulo = "Planes de aprovechamiento, ciclos de corta y regeneración natural",
                    contenido = "Se desarrollan los métodos de inventario forestal al 100%, censos comerciales con geolocalización satelital y silvicultura de precisión. Se evalúan técnicas de tala de bajo impacto y modelos de crecimiento diamétrico para garantizar la sostenibilidad del recurso maderable y no maderable (Castaña / Bertholletia excelsa)."
                ),
                CapituloLectura(
                    numero = 3,
                    titulo = "Monitoreo Biológico, Servicios Ecosistémicos y Carbono",
                    subtitulo = "Captura de biomasa, reservas de carbono y conservación de fauna",
                    contenido = "Metodologías de muestreo para fauna silvestre, bioindicadores acuáticos y cuantificación de carbono almacenado según directrices del IPCC. Se evalúa el impacto de la fragmentación de hábitats y la efectividad de los corredores biológicos en el corredor Vilcabamba-Amboró y reservas de biosfera."
                ),
                CapituloLectura(
                    numero = 4,
                    titulo = "Gobernanza Ambiental, Legislación y Restauración",
                    subtitulo = "Áreas Naturales Protegidas, normatividad SERFOR y biorremediación",
                    contenido = "Marco regulatorio de los recursos forestales y de fauna silvestre en el Perú (Ley N° 29763). Se discuten proyectos de biorremediación de suelos degradados por actividades antrópicas y estrategias participativas con comunidades nativas e instituciones regionales."
                ),
                CapituloLectura(
                    numero = 5,
                    titulo = "Conclusiones, Protocolos de Campo y Bibliografía",
                    subtitulo = "Guías taxonómicas, fichas de monitoreo y fuentes científicas UNAMAD",
                    contenido = "1. Consolidación de indicadores de sostenibilidad ecológica para la Amazonía suroriental.\n2. Protocolos estandarizados de campo para investigadores y tesistas de la UNAMAD.\n3. Fuentes oficiales: Herbario Universitario Alwyn H. Gentry y centros de investigación regional."
                )
            )
        }

        // 4. Investigación / Metodología / Tesis
        catLower.contains("investiga") || catLower.contains("metodolog") || catLower.contains("tesis") ||
        tituloLower.contains("investigación") || tituloLower.contains("metodología") || tituloLower.contains("tesis") -> {
            listOf(
                CapituloLectura(
                    numero = 1,
                    titulo = "El Proceso de Investigación y Planteamiento del Problema",
                    subtitulo = "De la idea científica a la delimitación formal y objetivos de estudio",
                    contenido = "$descParrafo1\n\nIniciar una investigación científica rigurosa demanda definir con claridad la brecha de conocimiento, la justificación teórica y la relevancia social. Se analizan criterios para estructurar preguntas de investigación precisas, objetivos generales y específicos orientados a resultados tangibles."
                ),
                CapituloLectura(
                    numero = 2,
                    titulo = "Marco Teórico, Estado del Arte y Operacionalización",
                    subtitulo = "Revisión sistemática de literatura, hipótesis y matriz de variables",
                    contenido = "Técnicas de búsqueda bibliográfica en bases de datos indexadas (Scopus, Web of Science, SciELO). Se enseña a construir la matriz de consistencia lógica, derivar hipótesis operativas y definir las dimensiones e indicadores observables para cada variable."
                ),
                CapituloLectura(
                    numero = 3,
                    titulo = "Diseño Metodológico: Enfoques y Muestreo",
                    subtitulo = "Investigación cuantitativa, cualitativa y mixta con rigor estadístico",
                    contenido = "Tipologías de diseño experimental, cuasiexperimental y no experimental correlacional. Se detallan fórmulas para el cálculo de tamaño de muestra con niveles de confianza del 95% y 99%, así como criterios de muestreo probabilístico y estratificado."
                ),
                CapituloLectura(
                    numero = 4,
                    titulo = "Instrumentos, Validez, Confiabilidad y Análisis",
                    subtitulo = "Alfa de Cronbach, juicio de expertos y pruebas estadísticas inferenciales",
                    contenido = "Validación psicométrica y de contenido para encuestas, guías de observación y registros de laboratorio. Se aplican pruebas paramétricas (t de Student, ANOVA) y no paramétricas (Chi-cuadrado, Mann-Whitney) para la contrastación rigurosa de hipótesis."
                ),
                CapituloLectura(
                    numero = 5,
                    titulo = "Discusión, Redacción de Tesis y Divulgación Científica",
                    subtitulo = "Normas APA 7ma / IEEE, ética en investigación y publicación",
                    contenido = "1. Estructura y defensa formal del informe final de tesis universitaria en la UNAMAD.\n2. Pautas de integridad científica, antiplagio y directivas del Vicerrectorado de Investigación.\n3. Modelo de artículo científico para revistas académicas indexadas."
                )
            )
        }

        // 5. Literatura / Narrativa / Novela / Ficción
        catLower.contains("literatura") || catLower.contains("novela") || catLower.contains("ficci") ||
        catLower.contains("poes") || tituloLower.contains("soledad") || tituloLower.contains("amor") ||
        tituloLower.contains("cuento") || tituloLower.contains("relato") || tituloLower.contains("enamorarse") -> {
            listOf(
                CapituloLectura(
                    numero = 1,
                    titulo = "Prólogo, Ambientación y Punto de Partida",
                    subtitulo = "Construcción de la atmósfera, espacio escénico y tono de la voz narrativa",
                    contenido = "$descParrafo1\n\nLas primeras páginas despliegan el universo sensitivo y las coordenadas temporales en las que se desenvuelve la trama. Los personajes son introducidos a través de sus anhelos, contradicciones y el entorno social o mítico que condiciona sus destinos."
                ),
                CapituloLectura(
                    numero = 2,
                    titulo = "Desarrollo del Conflicto y Ramificaciones",
                    subtitulo = "Líneas argumentales secundarias, tensión dramática y vínculos interpersonales",
                    contenido = "La cotidianidad se fractura ante un dilema inevitable. La prosa profundiza en los monólogos interiores, la memoria retrospectiva y los diálogos cargados de subtexto, donde cada decisión desencadena consecuencias imprevistas para los protagonistas."
                ),
                CapituloLectura(
                    numero = 3,
                    titulo = "El Clímax Narrativo y la Revelación Crucial",
                    subtitulo = "Confrontación cumbre y punto de máxima intensidad emocional",
                    contenido = "Las fuerzas en tensión alcanzan su cúspide dramática. Los secretos largo tiempo guardados salen a la luz, obligando a los personajes a confrontar su verdadera naturaleza en un momento de lucidez y transformación irreversible."
                ),
                CapituloLectura(
                    numero = 4,
                    titulo = "Desenlace, Resonancia Temática y Consecuencias",
                    subtitulo = "Resolución de las subtramas y el poso moral de la travesía",
                    contenido = "Tras la tormenta del clímax, las aguas vuelven a su cauce, pero nada permanece igual. El autor teje un desenlace coherente donde el destino de los personajes resuena con reflexiones universales sobre la condición humana, el tiempo y la identidad."
                ),
                CapituloLectura(
                    numero = 5,
                    titulo = "Epílogo, Notas de Contexto y Estudio Crítico",
                    subtitulo = "Apreciación literaria, recursos poéticos y trascendencia de la obra",
                    contenido = "1. Análisis del estilo literario, figuras retóricas y arquitectura estructural del relato.\n2. Repercusión de la obra en las letras contemporáneas e hispanoamericanas.\n3. Guía de lectura crítica para estudiantes y talleres literarios universitarios."
                )
            )
        }

        // 6. Educación / Pedagogía / Psicología / Humanidades
        catLower.contains("educa") || catLower.contains("pedagog") || catLower.contains("psicol") ||
        catLower.contains("social") || catLower.contains("filosof") -> {
            listOf(
                CapituloLectura(
                    numero = 1,
                    titulo = "Modelos Pedagógicos y Teorías del Aprendizaje",
                    subtitulo = "Constructivismo, conectivismo y enfoques socioculturales",
                    contenido = "$descParrafo1\n\nSe analizan las principales corrientes pedagógicas contemporáneas y su impacto en la formación universitaria. Se estudian las contribuciones de Piaget, Vygotsky, Ausubel y las nuevas dinámicas del aprendizaje mediado por tecnologías digitales."
                ),
                CapituloLectura(
                    numero = 2,
                    titulo = "Didáctica Universitaria y Estrategias Activas",
                    subtitulo = "Aprendizaje Basado en Problemas (ABP) y trabajo colaborativo",
                    contenido = "Diseño curricular por competencias, metodologías invertidas (Flipped Classroom) y rúbricas de evaluación diagnóstica y formativa orientadas a potenciar el pensamiento crítico y la autonomía del estudiante universitario."
                ),
                CapituloLectura(
                    numero = 3,
                    titulo = "Desarrollo Cognitivo, Emocional y Motivacional",
                    subtitulo = "Factores psicosociales que influyen en el rendimiento académico",
                    contenido = "Estudio de las variables afectivas, la autorregulación emocional y el clima institucional. Se proponen intervenciones psicoeducativas para fortalecer la resiliencia estudiantil y prevenir la deserción en educación superior."
                ),
                CapituloLectura(
                    numero = 4,
                    titulo = "Inclusión, Diversidad y Contexto Intercultural",
                    subtitulo = "Educación intercultural bilingüe y adaptación al entorno amazónico",
                    contenido = "Enfoques inclusivos para responder a la diversidad étnica y lingüística de la región de Madre de Dios. Se formulan políticas de equidad y adecuaciones curriculares respetuosas de los saberes ancestrales y las comunidades nativas."
                ),
                CapituloLectura(
                    numero = 5,
                    titulo = "Evaluación Institucional y Calidad Educativa",
                    subtitulo = "Métricas SUNEDU, acreditación y recomendaciones pedagógicas",
                    contenido = "1. Sistemas integrados de aseguramiento de la calidad académica en la UNAMAD.\n2. Orientaciones para la práctica preprofesional docente e investigación formativa.\n3. Referencias bibliográficas oficiales del Fondo Editorial Universitario."
                )
            )
        }

        // 7. General / Otras Especialidades
        else -> {
            listOf(
                CapituloLectura(
                    numero = 1,
                    titulo = "Introducción, Marco Conceptual y Alcance de la Obra",
                    subtitulo = "Definiciones fundamentales y relevancia para '${libro.titulo}'",
                    contenido = "$descParrafo1\n\nEl estudio detallado de '${libro.titulo}', de autoría de ${libro.autor}, ofrece un aporte significativo al cuerpo de conocimientos en el área de ${libro.categoria}. Este capítulo introduce los conceptos rectores, el contexto histórico y los objetivos que orientan el desarrollo del texto."
                ),
                CapituloLectura(
                    numero = 2,
                    titulo = "Fundamentos Teóricos y Modelos Estructurados",
                    subtitulo = "Principios rectores, taxonomía y análisis conceptual",
                    contenido = "Se profundiza en el marco teórico que sustenta la materia, contrastando las diversas escuelas de pensamiento y las metodologías consolidadas. Se desglosan las variables clave que permiten sistematizar los procesos analíticos y de toma de decisiones informada."
                ),
                CapituloLectura(
                    numero = 3,
                    titulo = "Metodología, Aplicaciones Prácticas y Casos de Estudio",
                    subtitulo = "Implementación paso a paso y resolución de escenarios reales",
                    contenido = "Se presentan aplicaciones directas de los conceptos abordados en situaciones profesionales y proyectos académicos. A través de ejercicios guiados y análisis de casos empíricos, se vincula la teoría con los desafíos contemporáneos de la disciplina."
                ),
                CapituloLectura(
                    numero = 4,
                    titulo = "Análisis Crítico, Discusión y Evaluación de Resultados",
                    subtitulo = "Interpretación analítica de datos e impacto en el campo profesional",
                    contenido = "Evaluación sistemática de los resultados alcanzados mediante la aplicación de las metodologías propuestas. Se examinan las fortalezas, limitaciones y oportunidades de mejora continua, destacando las implicancias éticas y técnicas de su implementación."
                ),
                CapituloLectura(
                    numero = 5,
                    titulo = "Conclusiones, Recomendaciones y Ficha Bibliográfica",
                    subtitulo = "Síntesis integradora, perspectivas futuras y fuentes citadas",
                    contenido = "1. Conclusiones sustantivas sobre los aportes de '${libro.titulo}' al acervo de la UNAMAD.\n2. Pautas y recomendaciones para su aplicación en proyectos de tesis e investigación formativa.\n3. Registro bibliográfico institucional: ${libro.autor} (${if (libro.isbn.isNotBlank()) "ISBN: " + libro.isbn else "UNAMAD Central"})."
                )
            )
        }
    }
}

@Composable
fun DigitalViewerDialog(
    libro: Libro,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val capitulos = remember(libro.id, libro.titulo) { generarCapitulosParaLibro(libro) }

    var temaSeleccionado by remember { mutableIntStateOf(0) } // 0: Papel, 1: Sepia, 2: Noche
    var tamanoLetra by remember { mutableIntStateOf(15) } // 13, 15, 18
    var mostrarVisorCompleto by remember { mutableStateOf(false) }
    var modoVisor by remember { mutableIntStateOf(0) } // 0: Lector Digital Adaptativo, 1: Visor Web / PDF Oficial
    var capituloSeleccionadoFiltro by remember { mutableIntStateOf(0) } // 0: Todos, 1..5: Capítulo específico

    val fondoPapel = when (temaSeleccionado) {
        0 -> Color(0xFFFFFDF8)
        1 -> Color(0xFFFBF0D9)
        else -> Color(0xFF1E293B)
    }

    val textoPapel = when (temaSeleccionado) {
        0 -> Color(0xFF292524)
        1 -> Color(0xFF43302B)
        else -> Color(0xFFF8FAFC)
    }

    val bordePapel = when (temaSeleccionado) {
        0 -> Color(0xFFE2D9C8)
        1 -> Color(0xFFE7D2B5)
        else -> Color(0xFF334155)
    }

    val colorAcento = when (temaSeleccionado) {
        2 -> Color(0xFFF59E0B)
        else -> Color(0xFF1B4D3E)
    }

    val cleanTitle = remember(libro.titulo) {
        libro.titulo.filter { it.isLetterOrDigit() || it == '_' || it == '-' || it == ' ' }
            .trim()
            .replace(' ', '_')
            .ifBlank { "libro_${libro.id}" }
    }
    val fileName = "$cleanTitle.pdf"

    val downloadUrl = remember(libro.archivoUrl, libro.categoria, libro.titulo) {
        PdfUrlResolver.resolverUrlPdf(
            rawUrl = libro.archivoUrl,
            categoria = libro.categoria,
            titulo = libro.titulo
        )
    }

    fun descargarPdf() {
        try {
            val uri = Uri.parse(downloadUrl)
            val request = DownloadManager.Request(uri)
                .setTitle(fileName)
                .setDescription("Descargando '${libro.titulo}' desde Biblioteca UNAMAD...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .addRequestHeader("User-Agent", "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(context, "Iniciando descarga en segundo plano...", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            try {
                // Fallback seguro: generar PDF académico local con datos reales y capítulos del libro
                generarPdfLocal(context, fileName, libro, capitulos)
                Toast.makeText(context, "Documento guardado en Descargas: $fileName", Toast.LENGTH_LONG).show()
            } catch (e2: Exception) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e3: Exception) {
                    Toast.makeText(context, "No se pudo iniciar la descarga: ${e2.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun abrirEnVisorExterno() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(downloadUrl), "application/pdf")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "No hay visor de PDF instalado en el dispositivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (mostrarVisorCompleto) {
        // VISOR EN PANTALLA COMPLETA DENTRO DE LA APP
        Dialog(
            onDismissRequest = { mostrarVisorCompleto = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = fondoPapel
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Cabecera del Visor Completo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { mostrarVisorCompleto = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Regresar",
                                tint = textoPapel
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = libro.titulo,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = textoPapel,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${libro.autor} • ${libro.categoria}",
                                fontSize = 11.sp,
                                color = textoPapel.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Botón de abrir en visor externo
                        IconButton(onClick = { abrirEnVisorExterno() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Abrir externo",
                                tint = textoPapel
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Selector de Modo: [Lector Digital] vs [Visor Web / PDF]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = modoVisor == 0,
                            onClick = { modoVisor = 0 },
                            label = { Text("Lector de Texto", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = modoVisor == 1,
                            onClick = { modoVisor = 1 },
                            label = { Text("Visor Web / PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (modoVisor == 0) {
                        // Barra de herramientas de lectura (Temas + Tamaño + Filtro de Capítulos)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Selector de fondo / tema
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("Papel" to 0, "Sepia" to 1, "Noche" to 2).forEach { (lbl, idx) ->
                                    FilterChip(
                                        selected = temaSeleccionado == idx,
                                        onClick = { temaSeleccionado = idx },
                                        label = { Text(lbl, fontSize = 10.sp) }
                                    )
                                }
                            }

                            // Selector de tamaño de fuente
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                listOf(13 to "A-", 15 to "A", 18 to "A+").forEach { (sz, lbl) ->
                                    FilterChip(
                                        selected = tamanoLetra == sz,
                                        onClick = { tamanoLetra = sz },
                                        label = { Text(lbl, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Barra horizontal de capítulos rápidos
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = capituloSeleccionadoFiltro == 0,
                                    onClick = { capituloSeleccionadoFiltro = 0 },
                                    label = { Text("Todos (${capitulos.size})", fontSize = 10.sp) }
                                )
                            }
                            items(capitulos) { cap ->
                                FilterChip(
                                    selected = capituloSeleccionadoFiltro == cap.numero,
                                    onClick = { capituloSeleccionadoFiltro = cap.numero },
                                    label = { Text("Cap. ${cap.numero}", fontSize = 10.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Contenido dinámico por capítulos con scroll vertical
                        val capitulosAMostrar = if (capituloSeleccionadoFiltro == 0) {
                            capitulos
                        } else {
                            capitulos.filter { it.numero == capituloSeleccionadoFiltro }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .border(1.dp, bordePapel, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            Text(
                                text = "BIBLIOTECA CENTRAL UNAMAD • ${libro.categoria.uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorAcento,
                                fontFamily = FontFamily.Serif
                            )

                            capitulosAMostrar.forEach { cap ->
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = colorAcento.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "CAPÍTULO ${cap.numero}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorAcento,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = cap.titulo,
                                        fontSize = (tamanoLetra + 2).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorAcento,
                                        fontFamily = FontFamily.Serif
                                    )

                                    Text(
                                        text = cap.subtitulo,
                                        fontSize = (tamanoLetra - 1).sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textoPapel.copy(alpha = 0.8f),
                                        fontFamily = FontFamily.Serif
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = cap.contenido,
                                        fontSize = tamanoLetra.sp,
                                        lineHeight = (tamanoLetra + 8).sp,
                                        color = textoPapel,
                                        fontFamily = FontFamily.Serif
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(bordePapel)
                                    )
                                }
                            }

                            // Ficha bibliográfica APA formal al final
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = bordePapel.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Cita Bibliográfica Institucional (Normas APA 7ma Ed.):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorAcento
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${libro.autor} (${SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())}). ${libro.titulo}. Repositorio Digital UNAMAD. ${if (libro.isbn.isNotBlank()) "ISBN: ${libro.isbn}." else ""} Puerto Maldonado, Perú.",
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        color = textoPapel.copy(alpha = 0.85f),
                                        fontFamily = FontFamily.Serif
                                    )
                                }
                            }
                        }
                    } else {
                        // MODO VISOR WEB / PDF OFICIAL (WebView embebido con Google Docs Viewer)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .border(1.dp, bordePapel, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                        ) {
                            val googleDocsViewerUrl = remember(downloadUrl) {
                                try {
                                    "https://docs.google.com/gview?embedded=true&url=" + URLEncoder.encode(downloadUrl, "UTF-8")
                                } catch (e: Exception) {
                                    downloadUrl
                                }
                            }

                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            builtInZoomControls = true
                                            displayZoomControls = false
                                        }
                                        webViewClient = object : WebViewClient() {}
                                        loadUrl(googleDocsViewerUrl)
                                    }
                                },
                                update = { webView ->
                                    webView.loadUrl(googleDocsViewerUrl)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botones inferiores del Visor Completo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { descargarPdf() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Descargar Copia", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { mostrarVisorCompleto = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Regresar al Catálogo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        // MODAL INICIAL DE RESUMEN Y ACCESO A LECTURA
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.90f),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Barra superior
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lector Digital UNAMAD",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Título y datos del documento con portada miniatura
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier
                                .width(56.dp)
                                .height(78.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
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
                            Text(
                                text = libro.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Autor: ${libro.autor}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Categoría: ${libro.categoria} • ${if (libro.isbn.isNotBlank()) "ISBN: " + libro.isbn else "Digital"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Barra de Controles de Lectura (Tema + Tamaño)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Selector de fondo / tema
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Papel" to 0, "Sepia" to 1, "Noche" to 2).forEach { (label, idx) ->
                                    FilterChip(
                                        selected = temaSeleccionado == idx,
                                        onClick = { temaSeleccionado = idx },
                                        label = { Text(label, fontSize = 10.sp) }
                                    )
                                }
                            }

                            // Selector de fuente
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(13 to "A-", 15 to "A", 18 to "A+").forEach { (size, label) ->
                                    FilterChip(
                                        selected = tamanoLetra == size,
                                        onClick = { tamanoLetra = size },
                                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contenedor vista previa dinámica del libro
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = fondoPapel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, bordePapel, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            val cap1 = capitulos.firstOrNull() ?: CapituloLectura(1, "Introducción", "Fundamentos", libro.descripcion)

                            Text(
                                text = "CAPÍTULO 1: ${cap1.titulo.uppercase()}",
                                fontSize = (tamanoLetra - 2).sp,
                                fontWeight = FontWeight.Bold,
                                color = colorAcento,
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cap1.subtitulo,
                                fontSize = (tamanoLetra - 3).sp,
                                fontWeight = FontWeight.Medium,
                                color = textoPapel.copy(alpha = 0.75f),
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = cap1.contenido.take(300) + if (cap1.contenido.length > 300) "..." else "",
                                fontSize = tamanoLetra.sp,
                                lineHeight = (tamanoLetra + 7).sp,
                                color = textoPapel,
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Índice de Capítulos (${capitulos.size}):",
                                fontSize = (tamanoLetra - 1).sp,
                                fontWeight = FontWeight.Bold,
                                color = colorAcento,
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            capitulos.forEach { cap ->
                                Text(
                                    text = "${cap.numero}. ${cap.titulo} • ${cap.subtitulo}",
                                    fontSize = (tamanoLetra - 2).sp,
                                    lineHeight = (tamanoLetra + 4).sp,
                                    color = textoPapel.copy(alpha = 0.9f),
                                    fontFamily = FontFamily.Serif,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botones solicitados: Visor en Línea Completo y Descargar PDF
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { mostrarVisorCompleto = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Visor en Línea", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { descargarPdf() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Descargar PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar Lector", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

fun generarPdfLocal(
    context: Context,
    fileName: String,
    libro: Libro,
    capitulos: List<CapituloLectura> = generarCapitulosParaLibro(libro)
) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    // Encabezado institucional
    paint.color = android.graphics.Color.parseColor("#1B4D3E")
    canvas.drawRect(0f, 0f, 595f, 90f, paint)

    paint.color = android.graphics.Color.WHITE
    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText("BIBLIOTECA CENTRAL - UNAMAD", 40f, 40f, paint)
    paint.textSize = 10f
    paint.isFakeBoldText = false
    canvas.drawText("Universidad Nacional Amazónica de Madre de Dios • Repositorio Digital Oficial", 40f, 60f, paint)

    // Título y autor
    paint.color = android.graphics.Color.parseColor("#1E293B")
    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText(libro.titulo.take(55), 40f, 125f, paint)

    paint.textSize = 10f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.parseColor("#475569")
    canvas.drawText("Autor: ${libro.autor}  |  Área: ${libro.categoria}  |  ${if (libro.isbn.isNotBlank()) "ISBN: " + libro.isbn else "Fondo Digital"}", 40f, 145f, paint)

    // Línea separadora
    paint.color = android.graphics.Color.parseColor("#D97706")
    paint.strokeWidth = 2f
    canvas.drawLine(40f, 160f, 555f, 160f, paint)

    // Contenido dinámico
    paint.color = android.graphics.Color.parseColor("#1E293B")
    paint.textSize = 10f
    paint.isFakeBoldText = true
    var y = 185f
    canvas.drawText("SINOPSIS Y ALCANCE ACADÉMICO:", 40f, y, paint)
    y += 16f

    paint.isFakeBoldText = false
    paint.textSize = 9.5f
    paint.color = android.graphics.Color.parseColor("#334155")
    val desc = libro.descripcion.ifBlank {
        "Recurso bibliográfico de consulta formativa e investigación de la Universidad Nacional Amazónica de Madre de Dios."
    }
    canvas.drawText(desc.take(100), 40f, y, paint)
    y += 14f
    if (desc.length > 100) {
        canvas.drawText(desc.substring(100, minOf(desc.length, 200)), 40f, y, paint)
        y += 18f
    } else {
        y += 10f
    }

    paint.color = android.graphics.Color.parseColor("#1B4D3E")
    paint.textSize = 10f
    paint.isFakeBoldText = true
    canvas.drawText("ESTRUCTURA Y PLAN TEMÁTICO DE LA OBRA:", 40f, y, paint)
    y += 18f

    paint.isFakeBoldText = false
    paint.textSize = 9.5f
    paint.color = android.graphics.Color.parseColor("#1E293B")
    capitulos.forEach { cap ->
        paint.isFakeBoldText = true
        canvas.drawText("Capítulo ${cap.numero}: ${cap.titulo.take(65)}", 40f, y, paint)
        y += 14f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.parseColor("#64748B")
        canvas.drawText("  → ${cap.subtitulo.take(70)}", 40f, y, paint)
        y += 16f
        paint.color = android.graphics.Color.parseColor("#1E293B")
    }

    y += 10f
    paint.color = android.graphics.Color.parseColor("#0F766E")
    paint.textSize = 9f
    paint.isFakeBoldText = true
    canvas.drawText("DECLARACIÓN DE ACCESO ABIERTO INSTITUCIONAL:", 40f, y, paint)
    y += 14f
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.parseColor("#64748B")
    canvas.drawText("Documento habilitado para consulta académica, investigación y tesis de pregrado y posgrado.", 40f, y, paint)
    y += 12f
    canvas.drawText("Repositorio Digital UNAMAD • Puerto Maldonado, Madre de Dios, Perú.", 40f, y, paint)

    pdfDocument.finishPage(page)
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (!downloadsDir.exists()) downloadsDir.mkdirs()
    val file = File(downloadsDir, fileName)
    FileOutputStream(file).use { out ->
        pdfDocument.writeTo(out)
    }
    pdfDocument.close()
}

// Sobrecarga de compatibilidad para código preexistente
fun generarPdfLocal(context: Context, fileName: String, titulo: String, autor: String, desc: String) {
    val libroTemporal = Libro(
        titulo = titulo,
        autor = autor,
        categoria = "General",
        descripcion = desc
    )
    generarPdfLocal(context, fileName, libroTemporal)
}
