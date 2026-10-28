package com.example.biblioteca.model

enum class FormatoLibro(val etiqueta: String) {
    FISICO("Físico"),
    DIGITAL("Digital"),
    HIBRIDO("Físico + Digital")
}

enum class EstadoPrestamo(val etiqueta: String) {
    ACTIVO("Activo"),
    DEVUELTO("Devuelto"),
    VENCIDO("Vencido")
}

enum class RolUsuario(val etiqueta: String, val descripcion: String) {
    ESTUDIANTE("Estudiante", "Acceso a catálogo, lectura de PDF y solicitudes de préstamo"),
    BIBLIOTECARIO("Bibliotecario / Admin", "Gestión total de préstamos, catálogo, solicitudes y métricas")
}

enum class EstadoSolicitud(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada"),
    CANCELADA("Cancelada")
}

enum class EstadoMulta(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    PAGADA("Pagada"),
    CONDONADA("Condonada")
}
