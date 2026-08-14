package com.kronosempire.casos.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kronosempire.casos.data.model.DetalleCaso
import kotlinx.coroutines.flow.Flow

@Dao
interface DetalleDao {
    @Insert
    suspend fun insert(detalle: DetalleCaso)

    @Insert
    suspend fun insertAll(detalles: List<DetalleCaso>)

    @Query("SELECT * FROM detalles_caso WHERE registroId = :registroId")
    fun getDetallesPorRegistro(registroId: Long): Flow<List<DetalleCaso>>

    @Query("DELETE FROM detalles_caso WHERE registroId = :registroId")
    suspend fun deleteDetallesPorRegistro(registroId: Long)

    @Query("DELETE FROM detalles_caso WHERE registroId IN (SELECT id FROM registros WHERE fecha BETWEEN :inicio AND :fin)")
    suspend fun deleteDetallesPorPeriodo(inicio: String, fin: String)
}
