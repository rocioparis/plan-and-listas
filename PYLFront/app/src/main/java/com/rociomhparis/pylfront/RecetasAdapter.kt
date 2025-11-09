package com.rociomhparis.pylfront

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import android.widget.*
import com.bumptech.glide.Glide

class RecetasAdapter(
    private val recetas: List<Receta>,
    private val onClick: (Receta) -> Unit
) : RecyclerView.Adapter<RecetasAdapter.RecetaViewHolder>() {

    private var onItemLongClick: ((View, Receta) -> Boolean)? = null

    inner class RecetaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivReceta = view.findViewById<ImageView>(R.id.ivRecetaItemVM)
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreRecetaItemVM)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receta, parent, false)
        return RecetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        val receta = recetas[position]
        holder.tvNombre.text = receta.nombre

        Glide.with(holder.ivReceta.context)
            .load(receta.imagen_url)
            .into(holder.ivReceta)

        holder.itemView.setOnClickListener {
            onClick(receta)
        }
        holder.itemView.setOnLongClickListener { view ->
            onItemLongClick?.invoke(view, receta) ?: false
        }
    }

    override fun getItemCount() = recetas.size

    fun setOnItemLongClickListener(listener: (View, Receta) -> Boolean) {
        onItemLongClick = listener
    }
}
