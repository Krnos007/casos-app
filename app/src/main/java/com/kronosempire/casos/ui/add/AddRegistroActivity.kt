package com.kronosempire.casos.ui.add

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.kronosempire.casos.R
import com.kronosempire.casos.databinding.ActivityAddRegistroBinding
import com.kronosempire.casos.models.DetalleModel
import com.kronosempire.casos.models.RegistroModel
import com.kronosempire.casos.utils.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class AddRegistroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddRegistroBinding
    private lateinit var adapter: DetalleAdapter
    private val detalles = mutableListOf<DetalleModel>()
    private val serviciosSeleccionados = mutableListOf<String>()
    private var tipoCaso = "EPI"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSpinners()
        setupListeners()
        cargarServicios()
        setupDatePicker()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nuevo Caso"
    }

    private fun setupRecyclerView() {
        adapter = DetalleAdapter { detalle, position ->
            detalles.removeAt(position)
            adapter.submitList(detalles.toList())
            actualizarVisibilidadRecyclerView()
        }
        binding.rvDetalles.layoutManager = LinearLayoutManager(this)
        binding.rvDetalles.adapter = adapter
    }

    private fun setupSpinners() {
        // Cargar unidades usando la función local
        val unidades = getListaUnidades()
        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            unidades
        )
        binding.actUnidad.setAdapter(adapter)
    }

    private fun setupListeners() {
        // Tipo de caso
        binding.rgTipo.setOnCheckedChangeListener { _, checkedId ->
            tipoCaso = when (checkedId) {
                R.id.rbEPI -> "EPI"
                R.id.rbPIO -> "PIO"
                else -> "EPI"
            }
        }

        // Servicios
        binding.btnSeleccionarTodos.setOnClickListener {
            serviciosSeleccionados.clear()
            for (i in 0 until binding.llServicios.childCount) {
                val child = binding.llServicios.getChildAt(i)
                if (child is Chip) {
                    child.isChecked = true
                    serviciosSeleccionados.add(child.text.toString())
                }
            }
        }

        binding.btnLimpiarTodos.setOnClickListener {
            serviciosSeleccionados.clear()
            for (i in 0 until binding.llServicios.childCount) {
                val child = binding.llServicios.getChildAt(i)
                if (child is Chip) {
                    child.isChecked = false
                }
            }
        }

        // Agregar detalles
        binding.btnAgregarNombre.setOnClickListener { agregarDetalle("nombre") }
        binding.btnAgregarNumero.setOnClickListener { agregarDetalle("numero") }
        binding.btnAgregarRedSocial.setOnClickListener { agregarDetalle("redsocial") }
        binding.btnAgregarOtraInfo.setOnClickListener { agregarDetalle("otra") }

        // Guardar
        binding.btnGuardar.setOnClickListener { guardarCaso() }
    }

    private fun agregarDetalle(tipo: String) {
        val editText = when (tipo) {
            "nombre" -> binding.etNombre
            "numero" -> binding.etNumero
            "redsocial" -> binding.etRedSocial
            "otra" -> binding.etOtraInfo
            else -> return
        }

        val texto = editText.text.toString().trim()
        if (texto.isEmpty()) {
            Toast.makeText(this, "Ingrese un valor", Toast.LENGTH_SHORT).show()
            return
        }

        val emoji = when (tipo) {
            "nombre" -> "👤"
            "numero" -> "📱"
            "redsocial" -> "🌐"
            "otra" -> "📌"
            else -> ""
        }

        detalles.add(DetalleModel("$emoji $texto", tipo))
        adapter.submitList(detalles.toList())
        editText.text?.clear()
        actualizarVisibilidadRecyclerView()
    }

    private fun actualizarVisibilidadRecyclerView() {
        binding.rvDetalles.visibility = if (detalles.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun cargarServicios() {
        val servicios = listOf("SMS", "Flujo", "Antena", "Cobertura", "Redes", "Otros")
        servicios.forEach { servicio ->
            val chip = Chip(this).apply {
                text = servicio
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        if (!serviciosSeleccionados.contains(servicio)) {
                            serviciosSeleccionados.add(servicio)
                        }
                    } else {
                        serviciosSeleccionados.remove(servicio)
                    }
                }
                setChipBackgroundColorResource(com.google.android.material.R.attr.colorPrimary)
                setTextColor(resources.getColor(android.R.color.white, theme))
            }
            binding.llServicios.addView(chip)
        }
    }

    private fun setupDatePicker() {
        binding.etFecha.setOnClickListener {
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
    }

    private fun guardarCaso() {
        val fecha = binding.etFecha.text.toString().trim()
        val unidad = binding.actUnidad.text.toString().trim()

        if (fecha.isEmpty()) {
            Toast.makeText(this, "Seleccione una fecha", Toast.LENGTH_SHORT).show()
            return
        }

        if (unidad.isEmpty()) {
            Toast.makeText(this, "Seleccione una unidad", Toast.LENGTH_SHORT).show()
            return
        }

        if (serviciosSeleccionados.isEmpty()) {
            Toast.makeText(this, "Seleccione al menos un servicio", Toast.LENGTH_SHORT).show()
            return
        }

        val descripcion = binding.etDescripcion.text.toString().trim()
        val detalleTexto = detalles.joinToString { it.texto }

        val registro = RegistroModel(
            fecha = DateUtils.getFechaDesdeDisplay(fecha) ?: "",
            unidad = unidad,
            tipo = tipoCaso,
            servicios = serviciosSeleccionados.joinToString(", "),
            descripcion = descripcion,
            detalles = detalleTexto,
            timestamp = System.currentTimeMillis()
        )

        lifecycleScope.launch {
            binding.btnGuardar.isEnabled = false
            try {
                Toast.makeText(this@AddRegistroActivity, "✅ Caso guardado", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AddRegistroActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnGuardar.isEnabled = true
            }
        }
    }

    // ==================== FUNCIÓN AGREGADA ====================
    private fun getListaUnidades(): List<String> {
        return listOf(
            "San Luis",
            "Santiago",
            "Holguín",
            "Camagüey",
            "La Habana",
            "Pinar del Río"
        )
    }
    // =========================================================

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // Adaptador interno para los detalles
    inner class DetalleAdapter(
        private val onDelete: (DetalleModel, Int) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<DetalleAdapter.ViewHolder>() {

        private var items = listOf<DetalleModel>()

        fun submitList(list: List<DetalleModel>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_detalle, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item)
            holder.btnEliminar.setOnClickListener {
                onDelete(item, position)
            }
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            private val tvDetalle = itemView.findViewById<TextView>(R.id.tvDetalle)
            val btnEliminar = itemView.findViewById<ImageView>(R.id.btnEliminar)

            fun bind(item: DetalleModel) {
                tvDetalle.text = item.texto
            }
        }
    }
}
