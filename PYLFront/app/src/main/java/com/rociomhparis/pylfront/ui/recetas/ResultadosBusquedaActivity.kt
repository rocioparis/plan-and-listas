package com.rociomhparis.pylfront.ui.recetas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rociomhparis.pylfront.R
import com.rociomhparis.pylfront.Receta
import com.rociomhparis.pylfront.RecetasAdapter
import com.rociomhparis.pylfront.RetrofitClient
import kotlinx.coroutines.launch
import android.widget.TextView
import okhttp3.internal.cache.DiskLruCache

class ResultadosBusquedaActivity : AppCompatActivity() {

    private lateinit var recyclerRecetasBqda: RecyclerView
    private val recetas = mutableListOf<Receta>()

    private lateinit var tvSinResultados: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_resultados_busqueda)

        recyclerRecetasBqda = findViewById(R.id.recyclerRecetasBqda)
        recyclerRecetasBqda.layoutManager = GridLayoutManager(this,3)

        tvSinResultados = findViewById(R.id.tvSinResultados)

        val adapter = RecetasAdapter(recetas){ receta ->
            navigateToReceta(receta)
        }
        recyclerRecetasBqda.adapter = adapter

        val query = intent.getStringExtra("QUERY") ?: ""
        val idUserRecibido = intent.getIntExtra("ID_USER", 0)

        buscarRecetasBackend(adapter, idUserRecibido, query)

        val etBuscarRB = findViewById<EditText>(R.id.etBuscarRB)

        etBuscarRB.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE){
                val query = etBuscarRB.text.toString().trim()
                if(query.isNotEmpty()){
                    buscarRecetasBackend(adapter, idUserRecibido,query)
                } else {
                    Toast.makeText(this, getString(R.string.bqdavacia), Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun navigateToReceta(receta: Receta?) {
        if (receta == null) return

        val intent = Intent(this, RecetaActivity::class.java)
        intent.putExtra("ID_RECETA", receta.id)
        intent.putExtra("NOMBRE_RECETA", receta.nombre)
        intent.putExtra("IMAGEN_RECETA", receta.imagen_url)
        intent.putParcelableArrayListExtra(
            "INGREDIENTES_RECETA",
            ArrayList(receta.ingredientes ?: emptyList())
        )
        intent.putExtra("PROCEDIMIENTO_RECETA", receta.procedimiento)
        startActivity(intent)
    }

    private fun buscarRecetasBackend(adapter: RecetasAdapter, idUser: Int, query: String){
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.buscarRecetas(
                    idUser = idUser,
                    query = query
                )
                if(response.isSuccessful){
                    val recetasRespuesta = response.body() ?: emptyList()
                    recetas.clear()
                    recetas.addAll(response.body() ?: emptyList())
                    adapter.notifyDataSetChanged()
                    if (recetasRespuesta.isEmpty()) {
                        tvSinResultados.visibility = View.VISIBLE
                    } else {
                        tvSinResultados.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@ResultadosBusquedaActivity, getString(R.string.errorbqda),
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception){
                e.printStackTrace()
                Toast.makeText(this@ResultadosBusquedaActivity, getString(R.string.errorcon
                ), Toast.LENGTH_SHORT).show()
            }
        }
    }
}