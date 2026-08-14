package com.kronosempire.casos.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kronosempire.casos.data.model.Registro
import com.kronosempire.casos.data.model.ResumenItem
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {
    @Insert
    suspend fun insert(registro: Registro): Long

    @Query("SELECT * FROM registros WHERE fecha = :fecha ORDER BY id DESC")
    fun getRegistrosPorFecha(fecha: String): Flow<List<Registro>>

    @Query("SELECT * FROM registros WHERE fecha BETWEEN :inicio AND :fin ORDER BY fecha DESC, id DESC")
    fun getRegistrosPorPeriodo(inicio: String, fin: String): Flow<List<Registro>>

    @Query("""
        SELECT DISTINCT r.* FROM registros r
        LEFT JOIN detalles_caso d ON r.id = d.registroId
        WHERE 
            r.descripcion LIKE '%' || :query || '%'
            OR r.unidad LIKE '%' || :query || '%'
            OR r.tipo LIKE '%' || :query || '%'
            OR r.fecha LIKE '%' || :query || '%'
            OR r.servicios LIKE '%' || :query || '%'
            OR d.valor LIKE '%' || :query || '%'
            OR d.tipoInfo LIKE '%' || :query || '%'
            OR d.observacion LIKE '%' || :query || '%'
        ORDER BY r.fecha DESC, r.id DESC
    """)
    fun buscarGlobal(query: String): Flow<List<Registro>>

    @Query("""
        SELECT 
            r.unidad,
            r.tipo,
            COUNT(*) as total
        FROM registros r
        WHERE r.fecha BETWEEN :inicio AND :fin
        GROUP BY r.unidad, r.tipo
        ORDER BY r.unidad, r.tipo
    """)
    fun getResumenPorPeriodo(inicio: String, fin: String): Flow<List<ResumenItem>>

    @Query("SELECT * FROM registros WHERE fecha = :fecha ORDER BY id DESC")
    fun getRegistrosPorFechaList(fecha: String): List<Registro>

    @Query("DELETE FROM registros WHERE fecha BETWEEN :inicio AND :fin")
    suspend fun deleteRegistrosPorPeriodo(inicio: String, fin: String)
}
