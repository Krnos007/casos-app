package com.kronosempire.casos.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kronosempire.casos.CASOSApplication
import com.kronosempire.casos.data.model.Registro
import com.kronosempire.casos.databinding.ItemRegistroBinding
import com.kronosempire.casos.utils.DateUtils
import com.kronosempire.casos.utils.ServiciosUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegistroAdapter(
    private val onItemClick: (Registro) -> Unit
) : ListAdapter<Registro, RegistroAdapter.ViewHolder>(RegistroDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegistroBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val registro = getItem(position)
        holder.bind(registro)
        holder.binding.btnVerDetalles.setOnClickListener {
            onItemClick(registro)
        }
    }

    class ViewHolder(val binding: ItemRegistroBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(registro: Registro) {
            binding.tvFecha.text = DateUtils.getFechaDisplay(registro.fecha)
            binding.tvUnidad.text = registro.unidad
            binding.chipTipo.text = registro.tipo
            binding.chipTipo.chipBackgroundColor = if (registro.tipo == "EPI") {
                binding.root.context.getColorStateList(com.google.android.material.R.attr.colorPrimary)
            } else {
                binding.root.context.getColorStateList(com.google.android.material.R.attr.colorSecondary)
            }

            val servicios = ServiciosUtils.stringToServicios(registro.servicios)
            binding.tvServicios.text = "?? ${servicios.joinToString(", ")}"
            binding.tvDescripcion.text = registro.descripcion.ifEmpty { "Sin descripci¨®n" }

            CoroutineScope(Dispatchers.Main).launch {
                val detalles = CASOSApplication.repository.getDetallesPorRegistro(registro.id)
                detalles.collect { lista ->
                    if (lista.isNotEmpty()) {
                        val nombres = lista.filter { it.tipoInfo == "nombre" }.joinToString(", ") { it.valor }
                        val numeros = lista.filter { it.tipoInfo == "numero" }.joinToString(", ") { it.valor }
                        val detallesText = mutableListOf<String>()
                        if (nombres.isNotEmpty()) detallesText.add("? $nombres")
                        if (numeros.isNotEmpty()) detallesText.add("? $numeros")
                        binding.tvDetalles.text = detallesText.joinToString(" | ")
                        binding.tvDetalles.visibility = android.view.View.VISIBLE
                    } else {
                        binding.tvDetalles.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }

    class RegistroDiffCallback : DiffUtil.ItemCallback<Registro>() {
        override fun areItemsTheSame(oldItem: Registro, newItem: Registro): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Registro, newItem: Registro): Boolean {
            return oldItem == newItem
        }
    }
}
