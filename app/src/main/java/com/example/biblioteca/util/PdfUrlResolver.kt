package com.example.biblioteca.util

/**
 * Utilidad para resolver y sanear URLs de recursos PDF académicos de la Biblioteca UNAMAD.
 * Resuelve discrepancias del seeder inicial del backend (donde libros de ingeniería tenían asignados
 * placeholders de literatura) y garantiza enlaces absolutos válidos para streaming y descarga.
 */
object PdfUrlResolver {

    private const val BASE_STORAGE_URL = "https://biblioteca.moondev.online"

    // Recursos técnicos y científicos reales validados en el repositorio
    private const val PDF_SISTEMAS_SOFTWARE = "$BASE_STORAGE_URL/storage/libros/77_ing_Patrones_de_diseo.pdf"
    private const val PDF_ALGORITMOS = "$BASE_STORAGE_URL/storage/libros/24_ing_Algoritmos_Resueltos_Con_Diagramas_De_Flujo_Y.pdf"
    private const val PDF_FORESTAL_AMBIENTAL = "$BASE_STORAGE_URL/storage/libros/94_amb_La_Red_Oculta_de_la_Vida.pdf"
    private const val PDF_LITERATURA_DEFAULT = "$BASE_STORAGE_URL/storage/libros/01_lit_Prohibido_Enamorarse.pdf"

    fun resolverUrlPdf(rawUrl: String?, categoria: String = "", titulo: String = ""): String {
        val cleanUrl = (rawUrl ?: "").trim()
        val titLower = titulo.lowercase()
        val catLower = categoria.lowercase()

        val esForestalOAmbiental = "forestal" in catLower || "ambiental" in catLower || "ecosistema" in titLower ||
                "recursos" in catLower || "amazonia" in titLower || "amazónic" in titLower || "amazonic" in titLower ||
                "árbol" in titLower || "selva" in titLower

        val esSistemasOIngenieria = "sistemas" in catLower || "software" in catLower || "computac" in catLower ||
                "informatic" in catLower || "algoritmo" in titLower || "software" in titLower ||
                "programac" in titLower || "datos" in titLower

        val urlLower = cleanUrl.lowercase()
        val apuntaALiteratura = "01_lit_prohibido_enamorarse" in urlLower ||
                "02_lit_entre_visillos" in urlLower ||
                "03_lit_el_extranjero" in urlLower

        // Si es un libro técnico/científico pero apunta por error a un archivo de literatura del seed inicial:
        if (apuntaALiteratura) {
            return when {
                "algoritmo" in titLower || "estructura" in titLower -> PDF_ALGORITMOS
                esForestalOAmbiental -> PDF_FORESTAL_AMBIENTAL
                esSistemasOIngenieria -> PDF_SISTEMAS_SOFTWARE
                else -> cleanUrl.ifBlank { PDF_LITERATURA_DEFAULT }
            }
        }

        // Si ya es una URL absoluta HTTP/HTTPS
        if (cleanUrl.isNotBlank() && cleanUrl.startsWith("http", ignoreCase = true)) {
            return cleanUrl
        }

        // Si es una ruta relativa del backend (/storage/libros/...)
        if (cleanUrl.isNotBlank() && cleanUrl.startsWith("/")) {
            return "$BASE_STORAGE_URL$cleanUrl"
        }

        // Si viene vacía o inválida, asignar según disciplina académica
        return when {
            "algoritmo" in titLower || "estructura" in titLower -> PDF_ALGORITMOS
            esForestalOAmbiental -> PDF_FORESTAL_AMBIENTAL
            esSistemasOIngenieria -> PDF_SISTEMAS_SOFTWARE
            else -> PDF_LITERATURA_DEFAULT
        }
    }
}
