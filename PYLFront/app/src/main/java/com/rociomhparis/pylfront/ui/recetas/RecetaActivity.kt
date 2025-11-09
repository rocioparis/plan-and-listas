package com.rociomhparis.pylfront.ui.recetas

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.unit.IntRect
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import com.rociomhparis.pylfront.R
import com.rociomhparis.pylfront.ui.planificacion.DatosPlanificacionActivity
import android.graphics.pdf.PdfDocument
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.rociomhparis.pylfront.Ingrediente
import com.rociomhparis.pylfront.MetaCoherencia
import com.rociomhparis.pylfront.Receta
import com.rociomhparis.pylfront.RetrofitClient
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RecetaActivity : AppCompatActivity() {

    private lateinit var tvTitleREC: TextView
    private lateinit var ivREC: ImageView
    private lateinit var tvIngredientsREC: TextView
    private lateinit var tvProcREC: TextView
    private lateinit var btnPlanificarREC: ImageButton

    private var receta: Receta? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receta)

        tvTitleREC = findViewById(R.id.tvTitleREC)
        ivREC = findViewById(R.id.ivREC)
        tvIngredientsREC = findViewById(R.id.tvIngredientsREC)
        tvProcREC = findViewById(R.id.tvProcREC)
        btnPlanificarREC = findViewById(R.id.btnPlanificarREC)

        val idUserRecibido = intent.getIntExtra("ID_USER", 0)

        val desdeFechaActivity = intent.getBooleanExtra("DESDE_FECHA_ACTIVITY", false)
        if (desdeFechaActivity || idUserRecibido == 0) {
            btnPlanificarREC.visibility = View.GONE
        }

        val recetaId = intent.getIntExtra("ID_RECETA", -1)
        if (recetaId <= 0) {
            Toast.makeText(this, getString(R.string.errorIDReceta), Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        val comensales = intent.getIntExtra("COMENSALES", 1)

        val ingredientes = intent.getParcelableArrayListExtra<Ingrediente>("INGREDIENTES_RECETA") ?: emptyList()
        receta = Receta(
            id = recetaId,
            nombre = intent.getStringExtra("NOMBRE_RECETA") ?: "",
            imagen_url = intent.getStringExtra("IMAGEN_RECETA") ?: "",
            ingredientes = ingredientes,
            procedimiento = intent.getStringExtra("PROCEDIMIENTO_RECETA") ?: ""
        )

        cargarRecetaDetalle(recetaId, comensales)

        btnPlanificarREC.setOnClickListener {
            receta?.let {
                elegirFecha(it, idUserRecibido)
            } ?: Toast.makeText(this, getString(R.string.errorRecetaPlan), Toast.LENGTH_SHORT).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun cargarRecetaDetalle(id: Int, comensales: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerRecetaDetalle(id)
                if (response.isSuccessful) {
                    response.body()?.let { r ->
                        receta = r.copy(
                            ingredientes = r.ingredientes.map { ing ->
                                ing.copy(cantidad = (ing.cantidad ?: 1.0) * comensales)
                            }
                        )
                        mostrarReceta(receta!!)
                        btnPlanificarREC.isEnabled = true
                        } ?: run {

                            Toast.makeText(this@RecetaActivity, getString(R.string.error),Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@RecetaActivity, getString(R.string.errorRecetaPlan), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@RecetaActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarReceta(receta: Receta) {
        tvTitleREC.text = receta.nombre
        Glide.with(this).load(receta.imagen_url).into(ivREC)
        val ingredientesTexto = receta.ingredientes.joinToString("\n") {
            "${it.nombre ?: ""} ${it.cantidad ?: ""} ${it.unidad ?: ""}".trim()
        }
        tvIngredientsREC.text = ingredientesTexto
        tvProcREC.text = receta.procedimiento
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
                    val coherencias = response.body()?.coherencia ?: emptyMap()
                    if(coherencias.isNullOrEmpty()){
                        navigateToDatosPlanificacion(receta, idUser, fechaSeleccionada)
                    }
                    else {
                        mostrarDialogCoherencia(receta, idUser, fechaSeleccionada, coherencias)
                    }
                } else {
                    Toast.makeText(this@RecetaActivity, getString(R.string.errorplan), Toast.LENGTH_SHORT).show()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Toast.makeText(this@RecetaActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogCoherencia(
        receta: Receta,
        idUser: Int,
        fecha: String,
        coherencias: Map<Int, MetaCoherencia>
    ) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_coherencia)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val layoutFondo = dialog.findViewById<LinearLayout>(R.id.rootLayout)
        val tvMensaje = dialog.findViewById<TextView>(R.id.tvMensaje)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelar)
        val btnPlanificar = dialog.findViewById<Button>(R.id.btnPlanificar)

        val mensaje = coherencias.entries.joinToString("\n") { (_, metaC) ->
            "${metaC.nombre}: ${metaC.porcentaje} - ${metaC.nivel}"
        }
        tvMensaje.text = mensaje

        val peorNivel = coherencias.values.minByOrNull { nivelToInt(it.nivel) }?.nivel ?: getString(R.string.media)

        val colorFondo = when (peorNivel) {
            getString(R.string.muybaja) -> getColor(R.color.muybaja)
            getString(R.string.baja) -> getColor(R.color.baja)
            getString(R.string.media) -> getColor(R.color.media)
            getString(R.string.alta) -> getColor(R.color.alta)
            getString(R.string.muyalta) -> getColor(R.color.muyalta)
            else -> Color.WHITE
        }
        layoutFondo.setBackgroundColor(colorFondo)

        btnPlanificar.setOnClickListener {
            navigateToDatosPlanificacion(receta, idUser, fecha)
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun nivelToInt(nivel: String) = when(nivel) {
        getString(R.string.muybaja) -> 1
        getString(R.string.baja) -> 2
        getString(R.string.media) -> 3
        getString(R.string.alta) -> 4
        getString(R.string.muyalta) -> 5
        else -> 3
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