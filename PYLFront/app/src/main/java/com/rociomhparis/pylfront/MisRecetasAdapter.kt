package com.rociomhparis.pylfront

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.*
import com.bumptech.glide.Glide
import com.rociomhparis.pylfront.ComidasAdapter.ComidaViewHolder

class MisRecetasAdapter(
    private val recetas: MutableList<Receta>,
    private val onPlanificar: (Receta) -> Unit,
    private val onVerReceta: (Receta) -> Unit
) : RecyclerView.Adapter<MisRecetasAdapter.RecetaViewHolder>() {

    inner class RecetaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivReceta: ImageView = view.findViewById(R.id.ivRecetaItemMR)
        private val tvNombre: TextView = view.findViewById(R.id.tvNombreRecetaItemMR)
        private val btnPlanificar: Button = view.findViewById(R.id.btnPlanificarItemMR)

        fun bind(receta: Receta) {
            tvNombre.text = receta.nombre
            Glide.with(ivReceta.context)
                .load(receta.imagen_url)
                .into(ivReceta)

            btnPlanificar.setOnClickListener { onPlanificar(receta) }
            ivReceta.setOnClickListener { onVerReceta(receta) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_misrecetas, parent, false)
        return RecetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        holder.bind(recetas[position])
    }

    override fun getItemCount(): Int = recetas.size
}