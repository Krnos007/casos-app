package com.kronosempire.casos.ui.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kronosempire.casos.CASOSApplication
import com.kronosempire.casos.R
import com.kronosempire.casos.databinding.ActivityDetailBinding
import com.kronosempire.casos.utils.DateUtils
import com.kronosempire.casos.utils.ServiciosUtils
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private var registroId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        cargarRegistro()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalle del Caso"
    }

    private fun cargarRegistro() {
        registroId = intent.getLongExtra("registro_id", 0)
        if (registroId == 0L) {
            Toast.makeText(this, "Error: ID de registro inv¨¢lido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val fecha = DateUtils.getFechaActual()
                val registros = CASOSApplication.repository.getRegistrosPorFechaList(fecha)
                val registro = registros.find { it.id == registroId }

                if (registro != null) {
                    mostrarRegistro(registro)
                } else {
                    Toast.makeText(
                        this@DetailActivity,
                        "Caso no encontrado",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@DetailActivity,
                    "Error al cargar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun mostrarRegistro(registro: com.kronosempire.casos.data.model.Registro) {
        binding.tvFecha.text = DateUtils.getFechaDisplay(registro.fecha)
        binding.tvUnidad.text = registro.unidad
        binding.tvTipo.text = "Tipo: ${registro.tipo}"

        val servicios = ServiciosUtils.stringToServicios(registro.servicios)
        binding.tvServicios.text = servicios.joinToString("\n? ")

        binding.tvDescripcion.text = registro.descripcion.ifEmpty { "Sin descripci¨®n" }

        lifecycleScope.launch {
            CASOSApplication.repository.getDetallesPorRegistro(registro.id)
                .collect { detalles ->
                    if (detalles.isNotEmpty()) {
                        val nombres = detalles.filter { it.tipoInfo == "nombre" }
                        val numeros = detalles.filter { it.tipoInfo == "numero" }
                        val redes = detalles.filter { it.tipoInfo == "red_social" }
                        val otros = detalles.filter { it.tipoInfo == "otro" }

                        var detallesText = ""

                        if (nombres.isNotEmpty()) {
                            detallesText += "? Nombres:\n"
                            nombres.forEach { detallesText += " ? ${it.valor}\n" }
                        }

                        if (numeros.isNotEmpty()) {
                            detallesText += "? N¨²meros:\n"
                            numeros.forEach { detallesText += " ? ${it.valor}\n" }
                        }

                        if (redes.isNotEmpty()) {
                            detallesText += "? Redes Sociales:\n"
                            redes.forEach { detallesText += " ? ${it.valor}\n" }
                        }

                        if (otros.isNotEmpty()) {
                            detallesText += "? Otra Informaci¨®n:\n"
                            otros.forEach { detallesText += " ? ${it.valor}\n" }
                        }

                        binding.tvDetalles.text = detallesText
                        binding.tvDetalles.visibility = android.view.View.VISIBLE
                    } else {
                        binding.tvDetalles.text = "Sin detalles adicionales"
                        binding.tvDetalles.visibility = android.view.View.VISIBLE
                    }
                }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
