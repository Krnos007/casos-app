package com.kronosempire.casos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registros")
data class Registro(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fecha: String,
    val unidad: String,
    val tipo: String,
    val servicios: String,
    val descripcion: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
