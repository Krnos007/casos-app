package com.kronosempire.casos.ui.add

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kronosempire.casos.data.model.DetalleCaso
import com.kronosempire.casos.databinding.ItemDetalleBinding

class DetallesAdapter(
    private val onDeleteClick: (DetalleCaso) -> Unit
) : RecyclerView.Adapter<DetallesAdapter.ViewHolder>() {

    private var items = listOf<DetalleCaso>()

    fun submitList(list: List<DetalleCaso>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetalleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.binding.btnEliminar.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemDetalleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetalleCaso) {
            val icono = when (item.tipoInfo) {
                "nombre" -> "?"
                "numero" -> "?"
                "red_social" -> "?"
                else -> "?"
            }
            binding.tvDetalle.text = "$icono ${item.valor}"
        }
    }
}
