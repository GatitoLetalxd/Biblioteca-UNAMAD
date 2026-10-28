package com.example.biblioteca.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.biblioteca.data.local.entity.LibroEntity
import com.example.biblioteca.data.local.entity.MultaEntity
import com.example.biblioteca.data.local.entity.PrestamoEntity
import com.example.biblioteca.data.local.entity.SolicitudPrestamoEntity
import com.example.biblioteca.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

data class PrestamoWithLibroInfo(
    val id: Long = 0,
    val libroId: Long = 0,
    val nombreLector: String = "",
    val codigoLector: String = "",
    val carreraOArea: String? = "",
    val telefonoLector: String? = "",
    val fechaPrestamo: Long = 0L,
    val fechaDevolucionEstimada: Long = 0L,
    val fechaDevolucionReal: Long? = null,
    val estado: String = "ACTIVO",
    val notas: String? = "",
    val libroTitulo: String? = "",
    val libroAutor: String? = ""
)

data class SolicitudWithLibroInfo(
    val id: Long = 0,
    val usuarioId: Long = 0,
    val libroId: Long = 0,
    val codigoEstudiante: String = "",
    val nombreEstudiante: String = "",
    val carreraEstudiante: String? = "",
    val telefonoEstudiante: String? = "",
    val fechaSolicitud: Long = 0L,
    val estado: String = "PENDIENTE",
    val motivoRechazo: String? = null,
    val fechaRespuesta: Long? = null,
    val diasSolicitados: Int = 7,
    val notas: String? = "",
    val libroTitulo: String? = "",
    val libroAutor: String? = ""
)

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun getAllUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE codigo = :codigo LIMIT 1")
    suspend fun getUsuarioByCodigo(codigo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE codigo = :codigo AND password = :password LIMIT 1")
    suspend fun autenticar(codigo: String, password: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun getUsuarioById(id: Long): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE rol = :rol ORDER BY nombre ASC")
    fun getUsuariosByRol(rol: String): Flow<List<UsuarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: UsuarioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usuarios: List<UsuarioEntity>)

    @Update
    suspend fun updateUsuario(usuario: UsuarioEntity)

    @Delete
    suspend fun deleteUsuario(usuario: UsuarioEntity)

    @Query("SELECT COUNT(*) FROM usuarios")
    fun countUsuarios(): Flow<Int>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun getCountDirect(): Int
}

@Dao
interface LibroDao {
    @Query("SELECT * FROM libros ORDER BY fechaRegistro DESC")
    fun getAllLibros(): Flow<List<LibroEntity>>

    @Query("SELECT * FROM libros WHERE id = :id LIMIT 1")
    suspend fun getLibroById(id: Long): LibroEntity?

    @Query("""
        SELECT * FROM libros 
        WHERE titulo LIKE '%' || :query || '%' 
           OR autor LIKE '%' || :query || '%' 
           OR categoria LIKE '%' || :query || '%' 
           OR isbn LIKE '%' || :query || '%'
        ORDER BY titulo ASC
    """)
    fun searchLibros(query: String): Flow<List<LibroEntity>>

    @Query("SELECT DISTINCT categoria FROM libros WHERE categoria != '' ORDER BY categoria ASC")
    fun getAllCategorias(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLibro(libro: LibroEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(libros: List<LibroEntity>)

    @Update
    suspend fun updateLibro(libro: LibroEntity)

    @Delete
    suspend fun deleteLibro(libro: LibroEntity)

    @Query("DELETE FROM libros")
    suspend fun deleteAllLibros()

    @Query("UPDATE libros SET ejemplaresDisponibles = ejemplaresDisponibles - 1 WHERE id = :libroId AND ejemplaresDisponibles > 0")
    suspend fun decrementarStock(libroId: Long): Int

    @Query("UPDATE libros SET ejemplaresDisponibles = ejemplaresDisponibles + 1 WHERE id = :libroId AND ejemplaresDisponibles < ejemplaresTotales")
    suspend fun incrementarStock(libroId: Long): Int

    @Query("SELECT COUNT(*) FROM libros")
    fun countLibros(): Flow<Int>

    @Query("SELECT COUNT(*) FROM libros")
    suspend fun getCountDirect(): Int
}

@Dao
interface PrestamoDao {
    @Query("""
        SELECT p.id, p.libroId, p.nombreLector, p.codigoLector, p.carreraOArea, p.telefonoLector,
               p.fechaPrestamo, p.fechaDevolucionEstimada, p.fechaDevolucionReal, p.estado, p.notas,
               l.titulo AS libroTitulo, l.autor AS libroAutor
        FROM prestamos p
        LEFT JOIN libros l ON p.libroId = l.id
        ORDER BY p.fechaPrestamo DESC
    """)
    fun getAllPrestamosConLibro(): Flow<List<PrestamoWithLibroInfo>>

    @Query("""
        SELECT p.id, p.libroId, p.nombreLector, p.codigoLector, p.carreraOArea, p.telefonoLector,
               p.fechaPrestamo, p.fechaDevolucionEstimada, p.fechaDevolucionReal, p.estado, p.notas,
               l.titulo AS libroTitulo, l.autor AS libroAutor
        FROM prestamos p
        LEFT JOIN libros l ON p.libroId = l.id
        WHERE p.estado = 'ACTIVO'
        ORDER BY p.fechaDevolucionEstimada ASC
    """)
    fun getPrestamosActivosConLibro(): Flow<List<PrestamoWithLibroInfo>>

    @Query("""
        SELECT p.id, p.libroId, p.nombreLector, p.codigoLector, p.carreraOArea, p.telefonoLector,
               p.fechaPrestamo, p.fechaDevolucionEstimada, p.fechaDevolucionReal, p.estado, p.notas,
               l.titulo AS libroTitulo, l.autor AS libroAutor
        FROM prestamos p
        LEFT JOIN libros l ON p.libroId = l.id
        WHERE p.codigoLector = :codigoLector
        ORDER BY p.fechaPrestamo DESC
    """)
    fun getPrestamosPorCodigoLector(codigoLector: String): Flow<List<PrestamoWithLibroInfo>>

    @Query("SELECT * FROM prestamos WHERE id = :id LIMIT 1")
    suspend fun getPrestamoById(id: Long): PrestamoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrestamo(prestamo: PrestamoEntity): Long

    @Update
    suspend fun updatePrestamo(prestamo: PrestamoEntity)

    @Query("""
        UPDATE prestamos 
        SET estado = :nuevoEstado, fechaDevolucionReal = :fechaDevolucion 
        WHERE id = :prestamoId
    """)
    suspend fun marcarDevolucion(prestamoId: Long, fechaDevolucion: Long, nuevoEstado: String = "DEVUELTO"): Int

    @Delete
    suspend fun deletePrestamo(prestamo: PrestamoEntity)
}

@Dao
interface SolicitudPrestamoDao {
    @Query("""
        SELECT s.id, s.usuarioId, s.libroId, s.codigoEstudiante, s.nombreEstudiante,
               s.carreraEstudiante, s.telefonoEstudiante, s.fechaSolicitud, s.estado,
               s.motivoRechazo, s.fechaRespuesta, s.diasSolicitados, s.notas,
               l.titulo AS libroTitulo, l.autor AS libroAutor
        FROM solicitudes_prestamo s
        LEFT JOIN libros l ON s.libroId = l.id
        ORDER BY s.fechaSolicitud DESC
    """)
    fun getAllSolicitudesConLibro(): Flow<List<SolicitudWithLibroInfo>>

    @Query("""
        SELECT s.id, s.usuarioId, s.libroId, s.codigoEstudiante, s.nombreEstudiante,
               s.carreraEstudiante, s.telefonoEstudiante, s.fechaSolicitud, s.estado,
               s.motivoRechazo, s.fechaRespuesta, s.diasSolicitados, s.notas,
               l.titulo AS libroTitulo, l.autor AS libroAutor
        FROM solicitudes_prestamo s
        LEFT JOIN libros l ON s.libroId = l.id
        WHERE s.estado = 'PENDIENTE'
        ORDER BY s.fechaSolicitud ASC
    """)
    fun getSolicitudesPendientesConLibro(): Flow<List<SolicitudWithLibroInfo>>

    @Query("""
        SELECT s.id, s.usuarioId, s.libroId, s.codigoEstudiante, s.nombreEstudiante,
               s.carreraEstudiante, s.telefonoEstudiante, s.fechaSolicitud, s.estado,
               s.motivoRechazo, s.fechaRespuesta, s.diasSolicitados, s.notas,
               l.titulo AS libroTitulo, l.autor AS libroAutor
        FROM solicitudes_prestamo s
        LEFT JOIN libros l ON s.libroId = l.id
        WHERE s.codigoEstudiante = :codigoEstudiante
        ORDER BY s.fechaSolicitud DESC
    """)
    fun getSolicitudesPorEstudiante(codigoEstudiante: String): Flow<List<SolicitudWithLibroInfo>>

    @Query("SELECT * FROM solicitudes_prestamo WHERE id = :id LIMIT 1")
    suspend fun getSolicitudById(id: Long): SolicitudPrestamoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSolicitud(solicitud: SolicitudPrestamoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(solicitudes: List<SolicitudPrestamoEntity>)

    @Update
    suspend fun updateSolicitud(solicitud: SolicitudPrestamoEntity)

    @Query("""
        UPDATE solicitudes_prestamo 
        SET estado = :nuevoEstado, fechaRespuesta = :fechaRespuesta, motivoRechazo = :motivo 
        WHERE id = :id
    """)
    suspend fun actualizarEstado(id: Long, nuevoEstado: String, fechaRespuesta: Long, motivo: String?): Int

    @Query("SELECT COUNT(*) FROM solicitudes_prestamo WHERE estado = 'PENDIENTE'")
    fun countPendientes(): Flow<Int>

    @Query("SELECT COUNT(*) FROM solicitudes_prestamo WHERE estado = 'PENDIENTE'")
    suspend fun countPendientesDirect(): Int

    @Query("SELECT COUNT(*) FROM solicitudes_prestamo")
    suspend fun getCountDirect(): Int
}

@Dao
interface MultaDao {
    @Query("SELECT * FROM multas ORDER BY fechaGeneracion DESC")
    fun getAllMultas(): Flow<List<MultaEntity>>

    @Query("SELECT * FROM multas WHERE estado = 'PENDIENTE' ORDER BY fechaGeneracion DESC")
    fun getMultasPendientes(): Flow<List<MultaEntity>>

    @Query("SELECT * FROM multas WHERE codigoEstudiante = :codigoEstudiante ORDER BY fechaGeneracion DESC")
    fun getMultasPorEstudiante(codigoEstudiante: String): Flow<List<MultaEntity>>

    @Query("SELECT * FROM multas WHERE codigoEstudiante = :codigoEstudiante AND estado = 'PENDIENTE'")
    fun getMultasPendientesPorEstudiante(codigoEstudiante: String): Flow<List<MultaEntity>>

    @Query("SELECT * FROM multas WHERE id = :id LIMIT 1")
    suspend fun getMultaById(id: Long): MultaEntity?

    @Query("SELECT * FROM multas WHERE prestamoId = :prestamoId LIMIT 1")
    suspend fun getMultaByPrestamoId(prestamoId: Long): MultaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMulta(multa: MultaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(multas: List<MultaEntity>)

    @Update
    suspend fun updateMulta(multa: MultaEntity)

    @Query("UPDATE multas SET estado = :nuevoEstado, fechaPago = :fechaPago, observaciones = :observaciones WHERE id = :id")
    suspend fun actualizarEstadoMulta(id: Long, nuevoEstado: String, fechaPago: Long?, observaciones: String): Int

    @Query("SELECT COUNT(*) FROM multas WHERE estado = 'PENDIENTE'")
    fun countMultasPendientes(): Flow<Int>

    @Query("SELECT COALESCE(SUM(montoTotal), 0.0) FROM multas WHERE estado = 'PENDIENTE'")
    fun sumMontoMultasPendientes(): Flow<Double>

    @Query("SELECT COUNT(*) FROM multas")
    suspend fun getCountDirect(): Int
}

