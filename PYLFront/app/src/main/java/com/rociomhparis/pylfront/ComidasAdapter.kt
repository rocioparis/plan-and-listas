package com.rociomhparis.pylfront

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.*
import com.bumptech.glide.Glide

class ComidasAdapter(
    private val comidas: MutableList<ComidaPlanificada>,
    private val onVerReceta: (ComidaPlanificada) -> Unit
) : RecyclerView.Adapter<ComidasAdapter.ComidaViewHolder>() {

    inner class ComidaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivComida = view.findViewById<ImageView>(R.id.ivComidaF)
        val tvNombre = view.findViewById<TextView>(R.id.tvNombreComidaF)
        val tvTipo = view.findViewById<TextView>(R.id.tvComidaDiariaF)
        val rootLayout = itemView.findViewById<LinearLayout>(R.id.rootLayout)

        fun bind(comida: ComidaPlanificada) {
            tvNombre.text = comida.nombre
            tvTipo.text = comida.tipoComida
            Glide.with(itemView.context)
                .load(comida.imagen_url)
                .into(ivComida)

            rootLayout.setOnClickListener { onVerReceta(comida) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComidaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comida, parent, false)
        return ComidaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComidaViewHolder, position: Int) {
        holder.bind(comidas[position])
    }

    override fun getItemCount() = comidas.size
}