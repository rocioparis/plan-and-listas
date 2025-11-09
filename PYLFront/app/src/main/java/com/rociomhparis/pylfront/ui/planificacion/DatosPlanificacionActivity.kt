package com.rociomhparis.pylfront.ui.planificacion

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rociomhparis.pylfront.R
import android.widget.*
import android.os.Handler
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import android.os.Looper
import android.util.Log
import androidx.core.os.postDelayed
import com.rociomhparis.pylfront.Receta
import com.rociomhparis.pylfront.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DatosPlanificacionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_planificacion)

        val idReceta = intent.getIntExtra("ID_RECETA",0)
        val idUser = intent.getIntExtra("ID_USER", -1)
        val fecha = intent.getStringExtra("FECHA_PLAN") ?: ""

        val etFecha = findViewById<EditText>(R.id.etFechaDP)
        val etComensales = findViewById<EditText>(R.id.etComensalesDP)
        val spComidaDiariaDP = findViewById<Spinner>(R.id.spComidaDiariaDP)

        val btnAceptarDP = findViewById<Button>(R.id.btnAceptarDP)
        val btnCancelarDP = findViewById<Button>(R.id.btnCancelarDP)

        btnCancelarDP.setOnClickListener { onBackPressed() }

        if (fecha.isNotEmpty()) {
            etFecha.setText(fecha)
            etFecha.isEnabled = false
        } else {
            etFecha.isEnabled = true
            etFecha.setOnClickListener {
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                DatePickerDialog(this, { _, y, m, d ->
                    val fechaSeleccionada = "${d}/${m + 1}/$y"
                    etFecha.setText(fechaSeleccionada)
                }, year, month, day).show()
            }
        }

        etComensales.setOnFocusChangeListener { _, hasFocus ->
            etComensales.hint =
                if (hasFocus) getString(R.string.PL_validosComensalesDP) else getString(R.string.PL_nroComensalesDP)
        }

        val comidasDiarias = listOf(
            getString(R.string.CS_seleccionar),
            getString(R.string.CS_breakfast),
            getString(R.string.CS_lunch),
            getString(R.string.CS_tea),
            getString(R.string.CS_dinner)
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            comidasDiarias
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spComidaDiariaDP.adapter = adapter

        btnAceptarDP.setOnClickListener {
            val fechaStr = etFecha.text.toString().trim()
            val comensalesStr = etComensales.text.toString().trim()
            val mealStr = spComidaDiariaDP.selectedItem.toString()

            val camposEditText = listOf(etFecha, etComensales)
            camposEditText.forEach {
                it.background = ContextCompat.getDrawable(this, android.R.drawable.edit_text)
            }

            if (spComidaDiariaDP.selectedItemPosition == 0) {
                Toast.makeText(this, getString(R.string.emptymeals), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val camposVacios = camposEditText.filter { it.text.toString().trim().isEmpty() }
            if (camposVacios.isNotEmpty()) {
                camposVacios.forEach { campo ->
                    campo.setBackgroundResource(R.drawable.campo_vacio_border)
                }
                camposVacios.first().requestFocus()
                Toast.makeText(this, getString(R.string.camposvacios), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val formatoMostrar = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            val formatoISO = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val fechaIngresada: Date? = try {
                if (fechaStr.contains("-")) formatoISO.parse(fechaStr)
                else formatoMostrar.parse(fechaStr)
            } catch (e: Exception) {
                null
            }

            if (fechaIngresada == null) {
                Toast.makeText(this, getString(R.string.fechainvalida), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fechaActual = Calendar.getInstance().time
            if (fechaIngresada.before(fechaActual)) {
                etFecha.setBackgroundResource(R.drawable.campo_vacio_border)
                etFecha.requestFocus()
                Toast.makeText(this, getString(R.string.fechaanterior), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fechaISO = formatoISO.format(fechaIngresada)


            val comensales = comensalesStr.toIntOrNull()
            if (comensales == null || comensales < 1 || comensales > 20) {
                etComensales.setBackgroundResource(R.drawable.campo_vacio_border)
                etComensales.requestFocus()
                Toast.makeText(
                    this,
                    getString(R.string.nrocominvalido),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.obtenerRecetaDetalle(idReceta)
                    if (response.isSuccessful) {
                        val receta = response.body()!!

                        val ingredientesAjustados = receta.ingredientes.map { ing ->
                            ing.copy(cantidad = (ing.cantidad ?: 1.0) * comensales)
                        }
                        receta.copy(ingredientes = ingredientesAjustados)

                        val planResponse = RetrofitClient.instance.confirmarPlanificacion(
                            idReceta = idReceta,
                            idUser = idUser,
                            fecha = fechaISO,
                            comensales = comensales,
                            tipoComida = mealStr
                        )
                        val confirmacion = planResponse.body()
                        val comensalesConfirmados = confirmacion?.comensales ?: 1

                        if (planResponse.isSuccessful) {
                            Toast.makeText(this@DatosPlanificacionActivity, getString(R.string.plCorrecto), Toast.LENGTH_SHORT).show()
                            val intent = Intent()
                            intent.putExtra("COMENSALES", comensalesConfirmados)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        } else {
                            Toast.makeText(this@DatosPlanificacionActivity, getString(R.string.errorplan), Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(this@DatosPlanificacionActivity, getString(R.string.errorRecetaPlan), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@DatosPlanificacionActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}