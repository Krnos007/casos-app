package com.kronosempire.casos.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kronosempire.casos.CASOSApplication
import com.kronosempire.casos.R
import com.kronosempire.casos.data.model.Registro
import com.kronosempire.casos.databinding.ActivitySearchBinding
import com.kronosempire.casos.ui.detail.DetailActivity
import com.kronosempire.casos.ui.main.RegistroAdapter
import com.kronosempire.casos.utils.DateUtils
import kotlinx.coroutines.launch
import java.util.*

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: RegistroAdapter
    private var allRegistros: List<Registro> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupFilters()
        cargarTodosLosRegistros()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "B¨²squeda Avanzada"
    }

    private fun setupRecyclerView() {
        adapter = RegistroAdapter { registro ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("registro_id", registro.id)
            startActivity(intent)
        }
        binding.rvResultados.layoutManager = LinearLayoutManager(this)
        binding.rvResultados.adapter = adapter
    }

    private fun cargarTodosLosRegistros() {
        val fecha = DateUtils.getFechaActual()
        val inicio = DateUtils.getPrimerDiaMes(fecha)
        val fin = DateUtils.getUltimoDiaMes(fecha)

        lifecycleScope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            try {
                CASOSApplication.repository.getRegistrosPorPeriodo(inicio, fin)
                    .collect { registros ->
                        allRegistros = registros
                        binding.tvTotalRegistros.text = "${registros.size} registros disponibles"
                        binding.progressBar.visibility = android.view.View.GONE
                    }
            } catch (e: Exception) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@SearchActivity, "Error al cargar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch() {
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.length >= 2) {
                    realizarBusqueda(query)
                } else {
                    adapter.submitList(emptyList())
                    binding.tvResultados.text = "Escriba al menos 2 caracteres"
                }
            }
        })

        binding.btnBuscar.setOnClickListener {
            val query = binding.etBuscar.text.toString().trim()
            if (query.length >= 2) {
                realizarBusqueda(query)
            } else {
                Toast.makeText(this, "Escriba al menos 2 caracteres", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLimpiar.setOnClickListener {
            binding.etBuscar.text?.clear()
            adapter.submitList(emptyList())
            binding.tvResultados.text = "Resultados de b¨²squeda"
        }
    }

    private fun realizarBusqueda(query: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            try {
                val fecha = DateUtils.getFechaActual()
                val inicio = DateUtils.getPrimerDiaMes(fecha)
                val fin = DateUtils.getUltimoDiaMes(fecha)

                CASOSApplication.repository.getRegistrosPorPeriodo(inicio, fin)
                    .collect { registros ->
                        val resultados = registros.filter { registro ->
                            registro.descripcion.contains(query, ignoreCase = true) ||
                                    registro.unidad.contains(query, ignoreCase = true) ||
                                    registro.tipo.contains(query, ignoreCase = true) ||
                                    registro.fecha.contains(query, ignoreCase = true) ||
                                    registro.servicios.contains(query, ignoreCase = true)
                        }

                        val detallesResultados = mutableListOf<Registro>()
                        lifecycleScope.launch {
                            registros.forEach { registro ->
                                CASOSApplication.repository.getDetallesPorRegistro(registro.id)
                                    .collect { detalles ->
                                        val coincide = detalles.any { detalle ->
                                            detalle.valor.contains(query, ignoreCase = true) ||
                                                    detalle.tipoInfo.contains(query, ignoreCase = true) ||
                                                    detalle.observacion.contains(query, ignoreCase = true)
                                        }
                                        if (coincide && !resultados.contains(registro)) {
                                            detallesResultados.add(registro)
                                        }
                                    }
                            }

                            val todosLosResultados = (resultados + detallesResultados).distinct()
                            adapter.submitList(todosLosResultados)
                            binding.tvResultados.text = "${todosLosResultados.size} resultados encontrados"
                            binding.progressBar.visibility = android.view.View.GONE
                        }
                    }
            } catch (e: Exception) {
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@SearchActivity, "Error en b¨²squeda: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFilters() {
        binding.etFechaInicio.setOnClickListener { showDatePicker(true) }
        binding.etFechaFin.setOnClickListener { showDatePicker(false) }

        val unidadAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            listOf("Todas") + com.kronosempire.casos.utils.DataConstants.UNIDADES
        )
        binding.actUnidadFiltro.setAdapter(unidadAdapter)
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
