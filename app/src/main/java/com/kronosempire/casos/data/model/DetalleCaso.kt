package com.kronosempire.casos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detalles_caso")
data class DetalleCaso(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val registroId: Long,
    val tipoInfo: String,
    val valor: String,
    val observacion: String = ""
)
