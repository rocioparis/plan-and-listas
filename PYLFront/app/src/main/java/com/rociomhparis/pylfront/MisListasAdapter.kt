package com.rociomhparis.pylfront

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.*
import com.rociomhparis.pylfront.ui.listascompras.ListaCompraActivity

class MisListasAdapter(private val listas: List<ListaComprasResumen>, private val idUser: Int) :
    RecyclerView.Adapter<MisListasAdapter.ListaViewHolder>() {
    inner class ListaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.tvTitleListML)
        val tvFecha: TextView = view.findViewById(R.id.tvDateTimeListML)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lista_compra, parent, false)
        return ListaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListaViewHolder, position: Int) {
        val lista = listas[position]
        holder.tvTitulo.text = lista.titulo
        holder.tvFecha.text = lista.fechaHora
        holder.itemView.setOnClickListener {
            val context = it.context
            val intent = Intent(context, ListaCompraActivity::class.java)
            intent.putExtra("ID_LISTA", lista.idLista)
            intent.putExtra("ID_USER", idUser)
            intent.putExtra("ORIGEN", "MIS_LISTAS")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listas.size
}