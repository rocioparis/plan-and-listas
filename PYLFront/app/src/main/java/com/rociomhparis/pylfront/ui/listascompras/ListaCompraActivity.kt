package com.rociomhparis.pylfront.ui.listascompras

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rociomhparis.pylfront.ListaCompraAdapter
import com.rociomhparis.pylfront.ListaItem
import com.rociomhparis.pylfront.R
import com.rociomhparis.pylfront.RecetaConIngredientes
import com.rociomhparis.pylfront.RetrofitClient
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ListaCompraActivity : AppCompatActivity() {

    private lateinit var rvListaCompras: RecyclerView
    private lateinit var adapter: ListaCompraAdapter
    private val listaItems = mutableListOf<ListaItem>() //  headers e ingredientes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lista_compra)

        val idLista = intent.getIntExtra("ID_LISTA", -1)

        rvListaCompras = findViewById(R.id.rvListaCompras)
        adapter = ListaCompraAdapter(listaItems) { idIngrediente, disponible ->
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.actualizarEstadoIngrediente(
                        idLista = idLista,
                        idIngrediente = idIngrediente,
                        disponible = disponible
                    )
                    if (response.isSuccessful) {
                        adapter.actualizarIngrediente(idIngrediente, disponible)
                    } else {
                        Toast.makeText(this@ListaCompraActivity, getString(R.string.errorCBLC), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ListaCompraActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
                }
            }
        }
        rvListaCompras.layoutManager = LinearLayoutManager(this)
        rvListaCompras.adapter = adapter

        val listaRecetas: ArrayList<RecetaConIngredientes>? =
            intent.getParcelableArrayListExtra("LISTA_COMPRAS")

        val idUser = intent.getIntExtra("ID_USER", -1)

        if (idUser == -1) {
            Toast.makeText(this, getString(R.string.errorIDUser), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (listaRecetas.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.lcMostrando), Toast.LENGTH_SHORT).show()
        } else {
            listaRecetas.forEach { receta ->
                listaItems.add(ListaItem.Header(receta.nombre))
                receta.ingredientes.forEach { ing ->
                    listaItems.add(
                        ListaItem.Ingrediente(
                            idIngrediente = ing.idIngrediente ?: 0,
                            nombre = ing.nombre ?: "",
                            cantidad = ing.cantidad?.toString() ?: "1",
                            unidad = ing.unidad ?: "unidad",
                            disponible = ing.disponibleItem ?: false
                        )
                    )
                }
            }
            adapter.notifyDataSetChanged()
        }

        val origen = intent.getStringExtra("ORIGEN")

        if (origen == "MIS_LISTAS") {
            val idLista = intent.getIntExtra("ID_LISTA", -1)
            if (idLista != -1) {
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.instance.obtenerListaPorId(idLista)
                        if (response.isSuccessful) {
                            val lista = response.body()
                            if (lista != null) {
                                mostrarRecetas(lista.recetas)
                            } else {
                                Toast.makeText(this@ListaCompraActivity, getString(R.string.emptyLC), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@ListaCompraActivity, getString(R.string.errorObtenerLC), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@ListaCompraActivity, getString(R.string.errorObtenerLC), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.errorIDLista), Toast.LENGTH_SHORT).show()
            }
        } else {
            val listaRecetas: ArrayList<RecetaConIngredientes>? =
                intent.getParcelableArrayListExtra("LISTA_COMPRAS")
            if (!listaRecetas.isNullOrEmpty()) {
                mostrarRecetas(listaRecetas)
            } else {
                Toast.makeText(this, getString(R.string.errorRecetasLC), Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun mostrarRecetas(recetas: List<RecetaConIngredientes>) {
        listaItems.clear()
        recetas.forEach { receta ->
            listaItems.add(ListaItem.Header(receta.nombre))
            receta.ingredientes.forEach { ing ->
                listaItems.add(
                    ListaItem.Ingrediente(
                        idIngrediente = ing.idIngrediente,
                        nombre = ing.nombre ?: "",
                        cantidad = ing.cantidad?.toString() ?: "1",
                        unidad = ing.unidad ?: "unidad",
                        disponible = ing.disponibleItem
                    )
                )
            }
        }
        adapter.notifyDataSetChanged()
    }

}
