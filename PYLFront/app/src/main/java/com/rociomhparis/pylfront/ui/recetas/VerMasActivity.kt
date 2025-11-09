package com.rociomhparis.pylfront.ui.recetas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.rociomhparis.pylfront.R
import androidx.recyclerview.widget.RecyclerView
import com.rociomhparis.pylfront.Receta
import com.rociomhparis.pylfront.RecetasAdapter
import com.rociomhparis.pylfront.RetrofitClient
import kotlinx.coroutines.launch

class VerMasActivity : AppCompatActivity() {

    private lateinit var rvRecetas: RecyclerView

    private val recetas = mutableListOf<Receta>()

    private lateinit var tvSinResultados: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ver_mas)

        val idUserRecibido = intent.getIntExtra("ID_USER", 0)
        rvRecetas = findViewById(R.id.rvRecetasVerMas)
        tvSinResultados = findViewById(R.id.tvSinResultados)

        rvRecetas.layoutManager = GridLayoutManager(this, 3)

        val adapter = RecetasAdapter(recetas) { receta ->
            navigateToReceta(receta, idUserRecibido)
        }
        rvRecetas.adapter = adapter

        cargarRecetasDesdeBackend(adapter,idUserRecibido)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun cargarRecetasDesdeBackend(adapter: RecetasAdapter, idUser: Int) {
        val api = RetrofitClient.instance
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerRecetasRecomendadas(idUser)
                if (response.isSuccessful) {
                    val recetasRespuesta = response.body() ?: emptyList()
                    recetas.clear()
                    recetas.addAll(recetasRespuesta.take(200))
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@VerMasActivity, getString(R.string.recetasnotfound), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@VerMasActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun navigateToReceta(receta: Receta?, idUser: Int) {
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
        intent.putExtra("ID_USER", idUser)
        startActivity(intent)
    }
}