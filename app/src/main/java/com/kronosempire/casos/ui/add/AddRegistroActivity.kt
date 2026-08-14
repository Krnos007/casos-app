package com.kronosempire.casos.ui.add

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kronosempire.casos.CASOSApplication
import com.kronosempire.casos.R
import com.kronosempire.casos.data.model.DetalleCaso
import com.kronosempire.casos.data.model.Registro
import com.kronosempire.casos.databinding.ActivityAddRegistroBinding
import com.kronosempire.casos.utils.DataConstants
import com.kronosempire.casos.utils.DateUtils
import com.kronosempire.casos.utils.ServiciosUtils
import kotlinx.coroutines.launch
import java.util.*

class AddRegistroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddRegistroBinding
    private val detallesList = mutableListOf<DetalleCaso>()
    private lateinit var detallesAdapter: DetallesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupFecha()
        setupUnidad()
        setupServicios()
        setupDetallesRecyclerView()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nuevo Caso"
    }

    private fun setupFecha() {
        val fechaActual = DateUtils.getFechaActual()
        binding.etFecha.setText(DateUtils.getFechaDisplay(fechaActual))
        binding.etFecha.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, yearSelected, monthSelected, dayOfMonth ->
                val fecha = String.format("%04d-%02d-%02d", yearSelected, monthSelected + 1, dayOfMonth)
                binding.etFecha.setText(DateUtils.getFechaDisplay(fecha))
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun setupUnidad() {
        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            DataConstants.UNIDADES
        )
        binding.actUnidad.setAdapter(adapter)

        binding.actUnidad.setOnItemClickListener { _, _, position, _ ->
            val unidad = DataConstants.UNIDADES[position]
            if (DataConstants.UNIDADES_EPI.contains(unidad)) {
                binding.rbEPI.isChecked = true
            } else {
                binding.rbPIO.isChecked = true
            }
        }
        binding.actUnidad.threshold = 1
    }

    private fun setupServicios() {
        DataConstants.SERVICIOS.forEach { servicio ->
            val checkBox = CheckBox(this).apply {
                text = servicio
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(8, 8, 8, 8)
            }
            binding.llServicios.addView(checkBox)
        }

        binding.btnSeleccionarTodos.setOnClickListener {
            for (i in 0 until binding.llServicios.childCount) {
                val child = binding.llServicios.getChildAt(i)
                if (child is CheckBox) child.isChecked = true
            }
        }

        binding.btnLimpiarTodos.setOnClickListener {
            for (i in 0 until binding.llServicios.childCount) {
                val child = binding.llServicios.getChildAt(i)
                if (child is CheckBox) child.isChecked = false
            }
        }
    }

    private fun setupDetallesRecyclerView() {
        detallesAdapter = DetallesAdapter { detalle ->
            detallesList.remove(detalle)
            detallesAdapter.submitList(detallesList.toList())
        }
        binding.rvDetalles.layoutManager = LinearLayoutManager(this)
        binding.rvDetalles.adapter = detallesAdapter
    }

    private fun setupListeners() {
        binding.btnAgregarNombre.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            if (nombre.isNotEmpty()) {
                detallesList.add(DetalleCaso(tipoInfo = "nombre", valor = nombre, registroId = 0))
                binding.etNombre.text?.clear()
                actualizarDetalles()
            } else {
                Toast.makeText(this, "Ingrese un nombre", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAgregarNumero.setOnClickListener {
            val numero = binding.etNumero.text.toString().trim()
            if (numero.isNotEmpty()) {
                detallesList.add(DetalleCaso(tipoInfo = "numero", valor = numero, registroId = 0))
                binding.etNumero.text?.clear()
                actualizarDetalles()
            } else {
                Toast.makeText(this, "Ingrese un n¨²mero", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAgregarRedSocial.setOnClickListener {
            val red = binding.etRedSocial.text.toString().trim()
            if (red.isNotEmpty()) {
                detallesList.add(DetalleCaso(tipoInfo = "red_social", valor = red, registroId = 0))
                binding.etRedSocial.text?.clear()
                actualizarDetalles()
            } else {
                Toast.makeText(this, "Ingrese una red social", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAgregarOtraInfo.setOnClickListener {
            val info = binding.etOtraInfo.text.toString().trim()
            if (info.isNotEmpty()) {
                detallesList.add(DetalleCaso(tipoInfo = "otro", valor = info, registroId = 0))
                binding.etOtraInfo.text?.clear()
                actualizarDetalles()
            } else {
                Toast.makeText(this, "Ingrese informaci¨®n", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGuardar.setOnClickListener { guardarRegistro() }
    }

    private fun actualizarDetalles() {
        detallesAdapter.submitList(detallesList.toList())
        binding.rvDetalles.visibility = if (detallesList.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun getServiciosSeleccionados(): List<String> {
        val servicios = mutableListOf<String>()
        for (i in 0 until binding.llServicios.childCount) {
            val child = binding.llServicios.getChildAt(i)
            if (child is CheckBox && child.isChecked) {
                servicios.add(child.text.toString())
            }
        }
        return servicios
    }

    private fun guardarRegistro() {
        val unidad = binding.actUnidad.text.toString().trim()
        if (unidad.isEmpty()) {
            Toast.makeText(this, "Seleccione una Unidad", Toast.LENGTH_SHORT).show()
            return
        }

        val serviciosSeleccionados = getServiciosSeleccionados()
        if (serviciosSeleccionados.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos un Servicio", Toast.LENGTH_SHORT).show()
            return
        }

        val fechaDisplay = binding.etFecha.text.toString().trim()
        val fecha = DateUtils.getFechaDesdeDisplay(fechaDisplay)
        if (fecha == null) {
            Toast.makeText(this, "Fecha inv¨¢lida", Toast.LENGTH_SHORT).show()
            return
        }

        val tipo = if (binding.rbEPI.isChecked) "EPI" else "PIO"
        val descripcion = binding.etDescripcion.text.toString().trim()

        val registro = Registro(
            fecha = fecha,
            unidad = unidad,
            tipo = tipo,
            servicios = ServiciosUtils.serviciosToString(serviciosSeleccionados),
            descripcion = descripcion
        )

        lifecycleScope.launch {
            try {
                val registroId = CASOSApplication.repository.insertRegistro(registro)
                if (detallesList.isNotEmpty()) {
                    val detallesConId = detallesList.map { it.copy(registroId = registroId) }
                    CASOSApplication.repository.insertDetalles(detallesConId)
                }

                Toast.makeText(
                    this@AddRegistroActivity,
                    "? Caso guardado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@AddRegistroActivity,
                    "? Error al guardar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
