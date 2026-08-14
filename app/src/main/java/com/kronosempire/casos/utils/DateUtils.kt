package com.kronosempire.casos.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    fun getFechaActual(): String {
        return dateFormat.format(Date())
    }

    fun getFechaDisplay(fecha: String): String {
        return try {
            val date = dateFormat.parse(fecha) ?: Date()
            displayFormat.format(date)
        } catch (e: Exception) {
            fecha
        }
    }

    fun getFechaDesdeDisplay(fechaDisplay: String): String? {
        return try {
            val date = displayFormat.parse(fechaDisplay) ?: return null
            dateFormat.format(date)
        } catch (e: Exception) {
            null
        }
    }

    fun getMesAnio(fecha: String): String {
        return try {
            val date = dateFormat.parse(fecha) ?: Date()
            monthFormat.format(date)
        } catch (e: Exception) {
            fecha
        }
    }

    fun getMesAnioShort(fecha: String): String {
        return try {
            val date = dateFormat.parse(fecha) ?: Date()
            yearMonthFormat.format(date)
        } catch (e: Exception) {
            fecha
        }
    }

    fun getPrimerDiaMes(fecha: String): String {
        return try {
            val date = dateFormat.parse(fecha) ?: Date()
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            dateFormat.format(calendar.time)
        } catch (e: Exception) {
            fecha
        }
    }

    fun getUltimoDiaMes(fecha: String): String {
        return try {
            val date = dateFormat.parse(fecha) ?: Date()
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            dateFormat.format(calendar.time)
        } catch (e: Exception) {
            fecha
        }
    }

    fun getMesesDisponibles(): List<String> {
        val meses = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        for (i in 0..11) {
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -i)
            meses.add(dateFormat.format(calendar.time))
        }
        return meses
    }
}
