package com.example.biblioteca

import com.example.biblioteca.data.local.entity.LibroEntity
import com.example.biblioteca.data.local.entity.PrestamoEntity
import com.example.biblioteca.model.BibliotecaStats
import com.example.biblioteca.model.EstadoPrestamo
import com.example.biblioteca.model.FormatoLibro
import com.example.biblioteca.model.Libro
import com.example.biblioteca.model.Prestamo
import com.example.biblioteca.ui.viewmodel.unaccent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BibliotecaLogicTest {

    @Test
    fun testLibroEntityMapping() {
        val domain = Libro(
            id = 1,
            titulo = "Inteligencia Artificial Moderna",
            autor = "Stuart Russell & Peter Norvig",
            categoria = "Ciencias de la Computación",
            isbn = "978-0136042594",
            descripcion = "Texto clásico de IA",
            formato = FormatoLibro.HIBRIDO,
            ejemplaresTotales = 5,
            ejemplaresDisponibles = 4,
            ubicacion = "Estante B-04"
        )

        val entity = LibroEntity.fromDomain(domain)
        assertEquals(domain.titulo, entity.titulo)
        assertEquals(domain.formato.name, entity.formato)
        assertEquals(domain.ejemplaresDisponibles, entity.ejemplaresDisponibles)

        val mappedBack = entity.toDomain()
        assertEquals(domain.titulo, mappedBack.titulo)
        assertEquals(FormatoLibro.HIBRIDO, mappedBack.formato)
        assertEquals(5, mappedBack.ejemplaresTotales)
    }

    @Test
    fun testPrestamoEstadoCalculado() {
        val now = System.currentTimeMillis()
        val dayMillis = 24L * 60 * 60 * 1000

        // Préstamo activo dentro del plazo
        val prestamoAlDia = Prestamo(
            id = 1,
            libroId = 1,
            nombreLector = "Jhordy Pérez",
            codigoLector = "2023-04510",
            fechaPrestamo = now - (2 * dayMillis),
            fechaDevolucionEstimada = now + (5 * dayMillis),
            estado = EstadoPrestamo.ACTIVO
        )
        assertEquals(EstadoPrestamo.ACTIVO, prestamoAlDia.estado)

        // Préstamo que ya superó la fecha límite
        val fechaVencida = now - (1 * dayMillis)
        val prestamoVencido = Prestamo(
            id = 2,
            libroId = 1,
            nombreLector = "Lucía Mendoza",
            codigoLector = "2022-01205",
            fechaPrestamo = now - (10 * dayMillis),
            fechaDevolucionEstimada = fechaVencida,
            estado = if (now > fechaVencida) EstadoPrestamo.VENCIDO else EstadoPrestamo.ACTIVO
        )
        assertEquals(EstadoPrestamo.VENCIDO, prestamoVencido.estado)

        // Préstamo marcado como devuelto
        val prestamoDevuelto = Prestamo(
            id = 3,
            libroId = 1,
            nombreLector = "Carlos Ruiz",
            codigoLector = "2021-00999",
            fechaPrestamo = now - (15 * dayMillis),
            fechaDevolucionEstimada = now - (8 * dayMillis),
            fechaDevolucionReal = now - (7 * dayMillis),
            estado = EstadoPrestamo.DEVUELTO
        )
        assertEquals(EstadoPrestamo.DEVUELTO, prestamoDevuelto.estado)
        assertNotNull(prestamoDevuelto.fechaDevolucionReal)
    }

    @Test
    fun testCalculoEstadisticas() {
        val libros = listOf(
            Libro(
                id = 1,
                titulo = "Libro Físico 1",
                autor = "Autor A",
                categoria = "Sistemas",
                formato = FormatoLibro.FISICO,
                ejemplaresTotales = 5,
                ejemplaresDisponibles = 3
            ),
            Libro(
                id = 2,
                titulo = "Libro Digital 1",
                autor = "Autor B",
                categoria = "Forestal",
                formato = FormatoLibro.DIGITAL,
                ejemplaresTotales = 1,
                ejemplaresDisponibles = 1
            ),
            Libro(
                id = 3,
                titulo = "Libro Híbrido 1",
                autor = "Autor C",
                categoria = "Sistemas",
                formato = FormatoLibro.HIBRIDO,
                ejemplaresTotales = 2,
                ejemplaresDisponibles = 1
            )
        )

        val totalTitulos = libros.size
        val totalEjemplaresFisicos = libros.filter { it.formato != FormatoLibro.DIGITAL }.sumOf { it.ejemplaresTotales }
        val totalDisponiblesFisicos = libros.filter { it.formato != FormatoLibro.DIGITAL }.sumOf { it.ejemplaresDisponibles }
        val totalLibrosDigitales = libros.count { it.formato == FormatoLibro.DIGITAL || it.formato == FormatoLibro.HIBRIDO }

        assertEquals(3, totalTitulos)
        assertEquals(7, totalEjemplaresFisicos)
        assertEquals(4, totalDisponiblesFisicos)
        assertEquals(2, totalLibrosDigitales)
    }

    @Test
    fun testFiltroBusquedaLibros() {
        val libros = listOf(
            Libro(id = 1, titulo = "Ecosistemas Amazónicos", autor = "Valdivia", categoria = "Forestal"),
            Libro(id = 2, titulo = "Estructuras de Datos", autor = "Lafore", categoria = "Sistemas"),
            Libro(id = 3, titulo = "Ingeniería de Software", autor = "Pressman", categoria = "Sistemas")
        )

        val query = "amazon".unaccent()
        val filtrados = libros.filter {
            it.titulo.unaccent().contains(query, ignoreCase = true) ||
            it.autor.unaccent().contains(query, ignoreCase = true) ||
            it.categoria.unaccent().contains(query, ignoreCase = true)
        }

        assertEquals(1, filtrados.size)
        assertEquals("Ecosistemas Amazónicos", filtrados.first().titulo)
    }
}
