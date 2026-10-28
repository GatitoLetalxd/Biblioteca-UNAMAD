package com.example.biblioteca.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.biblioteca.model.EstadoMulta
import com.example.biblioteca.model.EstadoPrestamo
import com.example.biblioteca.model.EstadoSolicitud
import com.example.biblioteca.model.FormatoLibro
import com.example.biblioteca.model.Libro
import com.example.biblioteca.model.Multa
import com.example.biblioteca.model.Prestamo
import com.example.biblioteca.model.RolUsuario
import com.example.biblioteca.model.SolicitudPrestamo
import com.example.biblioteca.model.Usuario

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigo: String,
    val password: String,
    val nombre: String,
    val apellidos: String,
    val carreraOArea: String = "",
    val telefono: String = "",
    val email: String = "",
    val rol: String = RolUsuario.ESTUDIANTE.name,
    val activo: Boolean = true,
    val fechaRegistro: Long = System.currentTimeMillis()
) {
    fun toDomain(): Usuario {
        return Usuario(
            id = id,
            codigo = codigo,
            password = password,
            nombre = nombre,
            apellidos = apellidos,
            carreraOArea = carreraOArea,
            telefono = telefono,
            email = email,
            rol = try { RolUsuario.valueOf(rol) } catch (e: Exception) { RolUsuario.ESTUDIANTE },
            activo = activo,
            fechaRegistro = fechaRegistro
        )
    }

    companion object {
        fun fromDomain(usuario: Usuario): UsuarioEntity {
            return UsuarioEntity(
                id = usuario.id,
                codigo = usuario.codigo,
                password = usuario.password,
                nombre = usuario.nombre,
                apellidos = usuario.apellidos,
                carreraOArea = usuario.carreraOArea,
                telefono = usuario.telefono,
                email = usuario.email,
                rol = usuario.rol.name,
                activo = usuario.activo,
                fechaRegistro = usuario.fechaRegistro
            )
        }
    }
}

@Entity(tableName = "libros")
data class LibroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titulo: String,
    val autor: String,
    val categoria: String,
    val isbn: String = "",
    val descripcion: String = "",
    val formato: String = FormatoLibro.FISICO.name,
    val ejemplaresTotales: Int = 1,
    val ejemplaresDisponibles: Int = 1,
    val ubicacion: String = "",
    val archivoUrl: String = "",
    val tamanoMb: Double = 0.0,
    val portadaUrl: String = "",
    val fechaRegistro: Long = System.currentTimeMillis()
) {
    fun toDomain(): Libro {
        return Libro(
            id = id,
            titulo = titulo,
            autor = autor,
            categoria = categoria,
            isbn = isbn,
            descripcion = descripcion,
            formato = try { FormatoLibro.valueOf(formato) } catch (e: Exception) { FormatoLibro.FISICO },
            ejemplaresTotales = ejemplaresTotales,
            ejemplaresDisponibles = ejemplaresDisponibles,
            ubicacion = ubicacion,
            archivoUrl = archivoUrl,
            tamanoMb = tamanoMb,
            portadaUrl = portadaUrl,
            fechaRegistro = fechaRegistro
        )
    }

    companion object {
        fun fromDomain(libro: Libro): LibroEntity {
            return LibroEntity(
                id = libro.id,
                titulo = libro.titulo,
                autor = libro.autor,
                categoria = libro.categoria,
                isbn = libro.isbn,
                descripcion = libro.descripcion,
                formato = libro.formato.name,
                ejemplaresTotales = libro.ejemplaresTotales,
                ejemplaresDisponibles = libro.ejemplaresDisponibles,
                ubicacion = libro.ubicacion,
                archivoUrl = libro.archivoUrl,
                tamanoMb = libro.tamanoMb,
                portadaUrl = libro.portadaUrl,
                fechaRegistro = libro.fechaRegistro
            )
        }
    }
}

@Entity(
    tableName = "prestamos",
    foreignKeys = [
        ForeignKey(
            entity = LibroEntity::class,
            parentColumns = ["id"],
            childColumns = ["libroId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["libroId"])]
)
data class PrestamoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val libroId: Long,
    val nombreLector: String,
    val codigoLector: String,
    val carreraOArea: String = "",
    val telefonoLector: String = "",
    val fechaPrestamo: Long = System.currentTimeMillis(),
    val fechaDevolucionEstimada: Long,
    val fechaDevolucionReal: Long? = null,
    val estado: String = EstadoPrestamo.ACTIVO.name,
    val notas: String = ""
) {
    fun toDomain(tituloLibro: String = "", autorLibro: String = ""): Prestamo {
        return Prestamo(
            id = id,
            libroId = libroId,
            tituloLibro = tituloLibro,
            autorLibro = autorLibro,
            nombreLector = nombreLector,
            codigoLector = codigoLector,
            carreraOArea = carreraOArea,
            telefonoLector = telefonoLector,
            fechaPrestamo = fechaPrestamo,
            fechaDevolucionEstimada = fechaDevolucionEstimada,
            fechaDevolucionReal = fechaDevolucionReal,
            estado = try { EstadoPrestamo.valueOf(estado) } catch (e: Exception) { EstadoPrestamo.ACTIVO },
            notas = notas
        )
    }

    companion object {
        fun fromDomain(prestamo: Prestamo): PrestamoEntity {
            return PrestamoEntity(
                id = prestamo.id,
                libroId = prestamo.libroId,
                nombreLector = prestamo.nombreLector,
                codigoLector = prestamo.codigoLector,
                carreraOArea = prestamo.carreraOArea,
                telefonoLector = prestamo.telefonoLector,
                fechaPrestamo = prestamo.fechaPrestamo,
                fechaDevolucionEstimada = prestamo.fechaDevolucionEstimada,
                fechaDevolucionReal = prestamo.fechaDevolucionReal,
                estado = prestamo.estado.name,
                notas = prestamo.notas
            )
        }
    }
}

@Entity(
    tableName = "solicitudes_prestamo",
    foreignKeys = [
        ForeignKey(
            entity = LibroEntity::class,
            parentColumns = ["id"],
            childColumns = ["libroId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["libroId"]), Index(value = ["codigoEstudiante"])]
)
data class SolicitudPrestamoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val usuarioId: Long = 0,
    val libroId: Long,
    val codigoEstudiante: String,
    val nombreEstudiante: String,
    val carreraEstudiante: String = "",
    val telefonoEstudiante: String = "",
    val fechaSolicitud: Long = System.currentTimeMillis(),
    val estado: String = EstadoSolicitud.PENDIENTE.name,
    val motivoRechazo: String? = null,
    val fechaRespuesta: Long? = null,
    val diasSolicitados: Int = 7,
    val notas: String = ""
) {
    fun toDomain(tituloLibro: String = "", autorLibro: String = ""): SolicitudPrestamo {
        return SolicitudPrestamo(
            id = id,
            usuarioId = usuarioId,
            libroId = libroId,
            codigoEstudiante = codigoEstudiante,
            nombreEstudiante = nombreEstudiante,
            carreraEstudiante = carreraEstudiante,
            telefonoEstudiante = telefonoEstudiante,
            tituloLibro = tituloLibro,
            autorLibro = autorLibro,
            fechaSolicitud = fechaSolicitud,
            estado = try { EstadoSolicitud.valueOf(estado) } catch (e: Exception) { EstadoSolicitud.PENDIENTE },
            motivoRechazo = motivoRechazo,
            fechaRespuesta = fechaRespuesta,
            diasSolicitados = diasSolicitados,
            notas = notas
        )
    }

    companion object {
        fun fromDomain(solicitud: SolicitudPrestamo): SolicitudPrestamoEntity {
            return SolicitudPrestamoEntity(
                id = solicitud.id,
                usuarioId = solicitud.usuarioId,
                libroId = solicitud.libroId,
                codigoEstudiante = solicitud.codigoEstudiante,
                nombreEstudiante = solicitud.nombreEstudiante,
                carreraEstudiante = solicitud.carreraEstudiante,
                telefonoEstudiante = solicitud.telefonoEstudiante,
                fechaSolicitud = solicitud.fechaSolicitud,
                estado = solicitud.estado.name,
                motivoRechazo = solicitud.motivoRechazo,
                fechaRespuesta = solicitud.fechaRespuesta,
                diasSolicitados = solicitud.diasSolicitados,
                notas = solicitud.notas
            )
        }
    }
}

@Entity(
    tableName = "multas",
    foreignKeys = [
        ForeignKey(
            entity = PrestamoEntity::class,
            parentColumns = ["id"],
            childColumns = ["prestamoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["prestamoId"]), Index(value = ["codigoEstudiante"])]
)
data class MultaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val prestamoId: Long,
    val usuarioId: Long = 0,
    val codigoEstudiante: String,
    val nombreEstudiante: String,
    val tituloLibro: String,
    val diasRetraso: Int,
    val montoTotal: Double,
    val estado: String = EstadoMulta.PENDIENTE.name,
    val fechaGeneracion: Long = System.currentTimeMillis(),
    val fechaPago: Long? = null,
    val observaciones: String = ""
) {
    fun toDomain(): Multa {
        return Multa(
            id = id,
            prestamoId = prestamoId,
            usuarioId = usuarioId,
            codigoEstudiante = codigoEstudiante,
            nombreEstudiante = nombreEstudiante,
            tituloLibro = tituloLibro,
            diasRetraso = diasRetraso,
            montoTotal = montoTotal,
            estado = try { EstadoMulta.valueOf(estado) } catch (e: Exception) { EstadoMulta.PENDIENTE },
            fechaGeneracion = fechaGeneracion,
            fechaPago = fechaPago,
            observaciones = observaciones
        )
    }

    companion object {
        fun fromDomain(multa: Multa): MultaEntity {
            return MultaEntity(
                id = multa.id,
                prestamoId = multa.prestamoId,
                usuarioId = multa.usuarioId,
                codigoEstudiante = multa.codigoEstudiante,
                nombreEstudiante = multa.nombreEstudiante,
                tituloLibro = multa.tituloLibro,
                diasRetraso = multa.diasRetraso,
                montoTotal = multa.montoTotal,
                estado = multa.estado.name,
                fechaGeneracion = multa.fechaGeneracion,
                fechaPago = multa.fechaPago,
                observaciones = multa.observaciones
            )
        }
    }
}

