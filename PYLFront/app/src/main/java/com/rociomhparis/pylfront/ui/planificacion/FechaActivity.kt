package com.rociomhparis.pylfront.ui.planificacion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rociomhparis.pylfront.R
import androidx.recyclerview.widget.RecyclerView
import com.rociomhparis.pylfront.ComidaPlanificada
import com.rociomhparis.pylfront.ComidasAdapter
import com.rociomhparis.pylfront.ConfirmacionResponse
import com.rociomhparis.pylfront.Receta
import com.rociomhparis.pylfront.RetrofitClient
import com.rociomhparis.pylfront.ui.recetas.RecetaActivity
import kotlinx.coroutines.launch

class FechaActivity : AppCompatActivity() {

    private lateinit var rvComidas: RecyclerView
    private val comidasPlanificadas = mutableListOf<ComidaPlanificada>()
    private lateinit var adapter: ComidasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fecha)

        rvComidas = findViewById(R.id.rvComidasPlanificadasFC)

        val fechaSeleccionada = intent.getStringExtra("FECHA_SELECCIONADA") ?: ""
        val idUser = intent.getIntExtra("ID_USER", -1)
        val comensales = intent.getIntExtra("COMENSALES", 1)

        if (fechaSeleccionada.isEmpty() || idUser == -1) {
            Toast.makeText(this, getString(R.string.errorFecha), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        adapter = ComidasAdapter(
            comidasPlanificadas,
            onVerReceta = { comida ->
                abrirReceta(comida.id, idUser, comensales)
            }
        )
        rvComidas.layoutManager = LinearLayoutManager(this)
        rvComidas.adapter = adapter
        cargarComidasPlanificadas(fechaSeleccionada, idUser)
    }

    private fun cargarComidasPlanificadas(fecha: String, idUser: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerComidasPlanificadas(idUser, fecha)
                if (response.isSuccessful) {
                    val comidasRespuesta = response.body() ?: emptyList()
                    comidasPlanificadas.clear()
                    comidasPlanificadas.addAll(comidasRespuesta)
                    adapter.notifyDataSetChanged()

                    if (comidasPlanificadas.isEmpty()) {
                        Toast.makeText(this@FechaActivity, getString(R.string.noComidasFecha), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@FechaActivity, getString(R.string.errorComidasFecha), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@FechaActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun abrirReceta(IDPlanificacion: Int, idUser: Int, comensales: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerRecetaDetalle(IDPlanificacion)
                if (response.isSuccessful) {
                    val recetaCompleta = response.body()
                    if(recetaCompleta != null) {
                        val intent = Intent(this@FechaActivity, RecetaActivity::class.java)
                        intent.putExtra("ID_RECETA", recetaCompleta.id)
                        intent.putExtra("NOMBRE_RECETA", recetaCompleta.nombre)
                        intent.putExtra("IMAGEN_RECETA", recetaCompleta.imagen_url)
                        intent.putParcelableArrayListExtra(
                            "INGREDIENTES_RECETA",
                            ArrayList(recetaCompleta.ingredientes ?: emptyList())
                        )
                        intent.putExtra("PROCEDIMIENTO_RECETA", recetaCompleta.procedimiento)
                        intent.putExtra("ID_USER", idUser)
                        intent.putExtra("DESDE_FECHA_ACTIVITY", true)
                        intent.putExtra("COMENSALES", comensales)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@FechaActivity, getString(R.string.errorRecetaPlan), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@FechaActivity, getString(R.string.errorRecetaPlan), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@FechaActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
            }
        }
    }

}
