package com.rociomhparis.pylfront.ui.recetas

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.rociomhparis.pylfront.AgregarRecetaRequest
import com.rociomhparis.pylfront.ComidasAdapter
import com.rociomhparis.pylfront.MetaCoherencia
import com.rociomhparis.pylfront.MisRecetasAdapter
import com.rociomhparis.pylfront.R
import com.rociomhparis.pylfront.Receta
import com.rociomhparis.pylfront.RecetaOut
import com.rociomhparis.pylfront.RetrofitClient
import com.rociomhparis.pylfront.ui.PantallaInicioActivity
import com.rociomhparis.pylfront.ui.listascompras.MisListasActivity
import com.rociomhparis.pylfront.ui.planificacion.DatosPlanificacionActivity
import kotlinx.coroutines.launch
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MisRecetasActivity : AppCompatActivity() {

    private val misRecetas: MutableList<Receta> = mutableListOf()
    private lateinit var adapter: MisRecetasAdapter
    private lateinit var rvRecetas: RecyclerView
    private val AGREGAR_MODIFICAR_REQUEST = 100
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mis_recetas)

        bottomNav = findViewById(R.id.bottomNav)
        rvRecetas = findViewById(R.id.rvMisRecetas)
        val btnAgregarMR: ImageButton = findViewById(R.id.btnAgregarMR)

        val idUserRecibido = intent.getIntExtra("ID_USER", 0)

        adapter = MisRecetasAdapter(
            misRecetas,
            onPlanificar = { receta ->
                elegirFecha(receta, idUserRecibido)
            },
            onVerReceta = { receta ->
                abrirReceta(receta.id, idUserRecibido)
            }
        )

        rvRecetas.layoutManager = LinearLayoutManager(this)
        rvRecetas.adapter = adapter

        obtenerRecetasUser(idUserRecibido)

        btnAgregarMR.setOnClickListener { abrirAgregarReceta(idUserRecibido) }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio -> {
                    val intent = Intent(this, PantallaInicioActivity::class.java)
                    intent.putExtra("ID_USER", idUserRecibido)
                    startActivity(intent)
                    true
                }
                R.id.navRecetas -> {
                    true
                }
                R.id.navListas -> {
                    val intent = Intent(this, MisListasActivity::class.java)
                    intent.putExtra("ID_USER", idUserRecibido)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun abrirAgregarReceta(idUser: Int) {
        val intent = Intent(this, AgregarRecetaActivity::class.java)
        intent.putExtra("ID_USER", idUser)
        startActivityForResult(intent, AGREGAR_MODIFICAR_REQUEST)
    }

    private fun obtenerRecetasUser(idUser: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerRecetas(idUser)
                if (response.isSuccessful) {
                    val recetasBackend: List<RecetaOut> = response.body() ?: emptyList()
                    misRecetas.clear()
                    recetasBackend.forEach { receta ->
                        misRecetas.add(
                            Receta(
                                nombre = receta.nombre,
                                procedimiento = receta.procedimiento,
                                imagen_url = receta.imagenUri ?: "",
                                id = receta.id,
                                ingredientes = listOf()
                            )
                        )
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        this@MisRecetasActivity,
                        getString(R.string.errorMisRecetas),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MisRecetasActivity,
                    getString(R.string.errorcon),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun abrirReceta(IDReceta: Int, idUser: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerRecetaDetalle(IDReceta)
                if (response.isSuccessful) {
                    val recetaCompleta = response.body()
                    if(recetaCompleta != null) {
                        val intent = Intent(this@MisRecetasActivity, RecetaActivity::class.java)
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
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MisRecetasActivity, getString(R.string.errorRecetaPlan), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MisRecetasActivity, getString(R.string.errorRecetaPlan), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MisRecetasActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun elegirFecha(receta: Receta, idUser: Int) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val fechaSeleccionada =
                    String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                planificarReceta(receta, idUser, fechaSeleccionada)
            },
            year, month, day
        )

        datePicker.show()
    }
    private fun planificarReceta(receta: Receta, idUser: Int, fechaSeleccionada: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.agregarRecetaAlPlan(
                    idReceta = receta.id,
                    idUser = idUser,
                    fecha = fechaSeleccionada
                )
                if (response.isSuccessful) {
                    navigateToDatosPlanificacion(receta, idUser, fechaSeleccionada)
                } else {
                    Toast.makeText(this@MisRecetasActivity, getString(R.string.errorplan), Toast.LENGTH_SHORT).show()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Toast.makeText(this@MisRecetasActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToDatosPlanificacion(receta: Receta?, idUser: Int, fecha: String) {
        if(receta==null) return
        val intent = Intent(this, DatosPlanificacionActivity::class.java)
        intent.putExtra("ID_RECETA", receta.id)
        intent.putExtra("NOMBRE_RECETA", receta.nombre)
        intent.putExtra("ID_USER", idUser)
        intent.putExtra("FECHA_PLAN", fecha)
        startActivity(intent)
    }
}