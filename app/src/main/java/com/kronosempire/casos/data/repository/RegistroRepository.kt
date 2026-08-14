package com.kronosempire.casos.data.repository

import com.kronosempire.casos.data.database.RegistroDao
import com.kronosempire.casos.data.database.DetalleDao
import com.kronosempire.casos.data.model.Registro
import com.kronosempire.casos.data.model.DetalleCaso
import com.kronosempire.casos.data.model.ResumenItem
import kotlinx.coroutines.flow.Flow

class RegistroRepository(
    private val registroDao: RegistroDao,
    private val detalleDao: DetalleDao
) {
    suspend fun insertRegistro(registro: Registro): Long {
        return registroDao.insert(registro)
    }

    fun getRegistrosPorFecha(fecha: String): Flow<List<Registro>> {
        return registroDao.getRegistrosPorFecha(fecha)
    }

    fun getRegistrosPorPeriodo(inicio: String, fin: String): Flow<List<Registro>> {
        return registroDao.getRegistrosPorPeriodo(inicio, fin)
    }

    fun buscarGlobal(query: String): Flow<List<Registro>> {
        return registroDao.buscarGlobal(query)
    }

    fun getResumenPorPeriodo(inicio: String, fin: String): Flow<List<ResumenItem>> {
        return registroDao.getResumenPorPeriodo(inicio, fin)
    }

    suspend fun deleteRegistrosPorPeriodo(inicio: String, fin: String) {
        registroDao.deleteRegistrosPorPeriodo(inicio, fin)
    }

    suspend fun insertDetalles(detalles: List<DetalleCaso>) {
        detalleDao.insertAll(detalles)
    }

    fun getDetallesPorRegistro(registroId: Long): Flow<List<DetalleCaso>> {
        return detalleDao.getDetallesPorRegistro(registroId)
    }

    suspend fun deleteDetallesPorRegistro(registroId: Long) {
        detalleDao.deleteDetallesPorRegistro(registroId)
    }

    suspend fun deleteDetallesPorPeriodo(inicio: String, fin: String) {
        detalleDao.deleteDetallesPorPeriodo(inicio, fin)
    }

    fun getRegistrosPorFechaList(fecha: String): List<Registro> {
        return registroDao.getRegistrosPorFechaList(fecha)
    }
}
