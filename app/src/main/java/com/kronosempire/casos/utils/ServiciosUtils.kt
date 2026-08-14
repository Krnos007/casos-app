package com.kronosempire.casos.utils

import org.json.JSONArray

object ServiciosUtils {
    fun serviciosToString(servicios: List<String>): String {
        val jsonArray = JSONArray()
        servicios.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    fun stringToServicios(serviciosStr: String): List<String> {
        val lista = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(serviciosStr)
            for (i in 0 until jsonArray.length()) {
                lista.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            // Si hay error, retornar lista vac¨ªa
        }
        return lista
    }

    fun contarServicios(serviciosStr: String): Int {
        return stringToServicios(serviciosStr).size
    }

    fun getServiciosFormateados(serviciosStr: String): String {
        val servicios = stringToServicios(serviciosStr)
        return servicios.joinToString(", ")
    }
}
