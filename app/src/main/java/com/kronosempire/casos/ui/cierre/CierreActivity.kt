package com.kronosempire.casos.ui.cierre

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kronosempire.casos.CASOSApplication
import com.kronosempire.casos.R
import com.kronosempire.casos.databinding.ActivityCierreBinding
import com.kronosempire.casos.utils.DateUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CierreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCierreBinding
    private lateinit var adapter: ResumenAdapter
    private val meses = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCierreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupMesSpinner()
        setupListeners()
        cargarCierreMesActual()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Cierre de Casos"
    }

    private fun setupRecyclerView() {
        adapter = ResumenAdapter()
        binding.rvResumen.layoutManager = LinearLayoutManager(this)
        binding.rvResumen.adapter = adapter
    }

    private fun setupMesSpinner() {
        val calendar = Calendar.getInstance()
        for (i in 0..11) {
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -i)
            val mes = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
            meses.add(mes)
        }

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            meses.map { DateUtils.getMesAnio("$it-01") }
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMes.adapter = spinnerAdapter

        val mesActual = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val posicion = meses.indexOf(mesActual)
        if (posicion >= 0) {
            binding.spinnerMes.setSelection(posicion)
        }
    }

    private fun setupListeners() {
        binding.spinnerMes.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position >= 0 && position < meses.size) {
                    val mesSeleccionado = meses[position]
                    cargarCierre(mesSeleccionado)
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        binding.etFechaInicio.setOnClickListener { showDatePicker(true) }
        binding.etFechaFin.setOnClickListener { showDatePicker(false) }

        binding.btnAplicarFechas.setOnClickListener {
            val inicio = binding.etFechaInicio.text.toString().trim()
            val fin = binding.etFechaFin.text.toString().trim()

            if (inicio.isEmpty() || fin.isEmpty()) {
                Toast.makeText(this, "Seleccione ambas fechas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fechaInicio = DateUtils.getFechaDesdeDisplay(inicio)
            val fechaFin = DateUtils.getFechaDesdeDisplay(fin)

            if (fechaInicio == null || fechaFin == null) {
                Toast.makeText(this, "Fechas inv¨¢lidas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            cargarCierrePersonalizado(fechaInicio, fechaFin)
        }

        binding.btnExportar.setOnClickListener { exportarCierre() }
    }

    private fun cargarCierreMesActual() {
        val fecha = DateUtils.getFechaActual()
        val mes = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha) ?: Date()
        )
        cargarCierre(mes)
    }

    private fun cargarCierre(mes: String) {
        val inicio = "$mes-01"
        val fin = DateUtils.getUltimoDiaMes(inicio)
        cargarCierrePersonalizado(inicio, fin)
    }

    private fun cargarCierrePersonalizado(inicio: String, fin: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            try {
                binding.tvPeriodo.text = "${DateUtils.getFechaDisplay(inicio)} - ${DateUtils.getFechaDisplay(fin)}"

                CASOSApplication.repository.getResumenPorPeriodo(inicio, fin)
                    .collect { resumen ->
                        val items = mutableListOf<ResumenItem>()
                        val unidades = resumen.map { it.unidad }.distinct()

                        unidades.forEach { unidad ->
                            val epi = resumen.find { it.unidad == unidad && it.tipo == "EPI" }?.total ?: 0
                            val pio = resumen.find { it.unidad == unidad && it.tipo == "PIO" }?.total ?: 0
                            if (epi > 0 || pio > 0) {
                                items.add(ResumenItem(unidad, epi, pio))
                            }
                        }

                        items.sortBy { it.unidad }

                        val totalEpi = items.sumOf { it.epi }
                        val totalPio = items.sumOf { it.pio }

                        if (totalEpi > 0 || totalPio > 0) {
                            items.add(ResumenItem("TOTAL", totalEpi, totalPio, isTotal = true))
                        }

                        adapter.submitList(items)
                        binding.tvTotalRegistros.text = "Total: ${items.sumOf { it.epi + it.pio }} casos"
                        binding.progressBar.visibility = android.view.View.GONE
                    }
            } catch (e: Exception) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@CierreActivity, "Error al cargar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(isInicio: Boolean) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, yearSelected, monthSelected, dayOfMonth ->
                val fecha = String.format("%04d-%02d-%02d", yearSelected, monthSelected + 1, dayOfMonth)
                val editText = if (isInicio) binding.etFechaInicio else binding.etFechaFin
                editText.setText(DateUtils.getFechaDisplay(fecha))
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun exportarCierre() {
        val items = adapter.currentList
        if (items.isEmpty()) {
            Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = StringBuilder()
        builder.append("¨T".repeat(50)).append("\n")
        builder.append(" KRONOS EMPIRE - CASOS\n")
        builder.append(" CIERRE DE INVESTIGACIONES\n")
        builder.append("¨T".repeat(50)).append("\n\n")
        builder.append("Per¨ªodo: ${binding.tvPeriodo.text}\n")
        builder.append("Fecha: ${DateUtils.getFechaDisplay(DateUtils.getFechaActual())}\n\n")
        builder.append("UNIDAD\t\tEPI\tPIO\tTOTAL\n")
        builder.append("©¤".repeat(40)).append("\n")

        items.forEach { item ->
            if (item.isTotal) {
                builder.append("©¤".repeat(40)).append("\n")
                builder.append("${item.unidad}\t\t${item.epi}\t${item.pio}\t${item.epi + item.pio}\n")
            } else {
                val espacios = if (item.unidad.length < 8) "\t" else ""
                builder.append("${item.unidad}$espacios\t${item.epi}\t${item.pio}\t${item.epi + item.pio}\n")
            }
        }

        builder.append("\n")
        builder.append("¨T".repeat(50)).append("\n")
        builder.append("? 2026 Kronos Empire\n")
        builder.append("Todos los derechos reservados\n")
        builder.append("Soporte: kronosempire79@gmail.com\n")
        builder.append("¨T".repeat(50))

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, builder.toString())
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Cierre de Casos - CASOS")
        }
        startActivity(Intent.createChooser(intent, "Compartir Cierre"))
    }

    data class ResumenItem(
        val unidad: String,
        val epi: Int,
        val pio: Int,
        val isTotal: Boolean = false
    )

    class ResumenAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<ResumenAdapter.ViewHolder>() {
        private var items = listOf<ResumenItem>()
        val currentList: List<ResumenItem> get() = items

        fun submitList(list: List<ResumenItem>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_resumen, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(itemView: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            private val tvUnidad = itemView.findViewById<android.widget.TextView>(R.id.tvUnidad)
            private val tvEpi = itemView.findViewById<android.widget.TextView>(R.id.tvEpi)
            private val tvPio = itemView.findViewById<android.widget.TextView>(R.id.tvPio)
            private val tvTotal = itemView.findViewById<android.widget.TextView>(R.id.tvTotal)

            fun bind(item: ResumenItem) {
                tvUnidad.text = item.unidad
                tvEpi.text = item.epi.toString()
                tvPio.text = item.pio.toString()
                tvTotal.text = (item.epi + item.pio).toString()

                if (item.isTotal) {
                    itemView.setBackgroundColor(
                        itemView.context.getColor(com.google.android.material.R.attr.colorPrimary)
                    )
                    tvUnidad.setTextColor(android.graphics.Color.WHITE)
                    tvEpi.setTextColor(android.graphics.Color.WHITE)
                    tvPio.setTextColor(android.graphics.Color.WHITE)
                    tvTotal.setTextColor(android.graphics.Color.WHITE)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
