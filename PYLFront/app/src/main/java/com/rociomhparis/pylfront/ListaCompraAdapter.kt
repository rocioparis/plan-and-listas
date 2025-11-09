package com.rociomhparis.pylfront

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListaCompraAdapter(private val items: List<ListaItem>, private val onIngredienteToggled: (idIngrediente: Int, disponible: Boolean) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_INGREDIENTE = 1
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHeader: TextView = view.findViewById(R.id.tvNombreRecetaHeader)
    }

    inner class IngredienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbIngrediente: CheckBox = view.findViewById(R.id.cbIngredienteLC)
        val tvNombre: TextView = view.findViewById(R.id.tvIngredienteNombreLC)
        val tvCantidad: TextView = view.findViewById(R.id.tvIngredienteCantidadLC)
        val tvUnidad: TextView = view.findViewById(R.id.tvIngredienteUnidadLC)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListaItem.Header -> TYPE_HEADER
            is ListaItem.Ingrediente -> TYPE_INGREDIENTE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_header_receta, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ingrediente, parent, false)
                IngredienteViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListaItem.Header -> {
                (holder as HeaderViewHolder).tvHeader.text = item.titulo
            }
            is ListaItem.Ingrediente -> {
                (holder as IngredienteViewHolder).apply {
                    cbIngrediente.setOnCheckedChangeListener(null)

                    cbIngrediente.isChecked = item.disponible ?: false
                    tvNombre.text = item.nombre
                    tvCantidad.text = item.cantidad
                    tvUnidad.text = item.unidad

                    cbIngrediente.setOnCheckedChangeListener { _, isChecked ->
                        item.disponible = isChecked

                        onIngredienteToggled(item.idIngrediente ?: 0, isChecked)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun actualizarIngrediente(idIngrediente: Int, disponible: Boolean) {
        val index = items.indexOfFirst {
            it is ListaItem.Ingrediente && it.idIngrediente == idIngrediente
        }
        if (index != -1) {
            val item = items[index] as ListaItem.Ingrediente
            item.disponible = disponible
            notifyItemChanged(index)
        }
    }

}