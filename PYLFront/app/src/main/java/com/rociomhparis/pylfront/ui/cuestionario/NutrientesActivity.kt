package com.rociomhparis.pylfront.ui.cuestionario

import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import com.rociomhparis.pylfront.CuestionarioResponse
import com.rociomhparis.pylfront.MetasRequest
import com.rociomhparis.pylfront.Nutriente
import com.rociomhparis.pylfront.NutrientesRequest
import com.rociomhparis.pylfront.R
import com.rociomhparis.pylfront.RetrofitClient
import com.rociomhparis.pylfront.ui.PantallaInicioActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class NutrientesActivity : AppCompatActivity() {

    private val nutrientesSeleccionados = mutableSetOf<Int>()
    private lateinit var nutrientes: List<ImageButton>

    private val nutrientesIDs = listOf(
        1, 2, 8, 9, 10, 11, 3, 4, 7, 5, 6, 12, 13, 14, 15, 16, 17, 28, 29, 30, 31, 32, 33, 34, 18,
        22, 24, 25, 19, 20, 23, 26
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nutrientes)

        val btnTerminar = findViewById<Button>(R.id.btnTerminarCS)

        val idUserRecibido = intent.getIntExtra("ID_USER", 0)
        val deCuestionario = intent.getBooleanExtra("DE_CUESTIONARIO", false)
        val metasVacias = intent.getBooleanExtra("METAS_VACIAS", false)

        val btnOmitir = findViewById<Button>(R.id.btnOmitirPreguntaN)
        val btnCancelar = findViewById<Button>(R.id.btnCancelarN)

        nutrientes = listOf(
            findViewById<ImageButton>(R.id.btnVitaminaA),
            findViewById<ImageButton>(R.id.btnBetacaroteno),
            findViewById<ImageButton>(R.id.btnVitaminaB1),
            findViewById<ImageButton>(R.id.btnVitaminaB2),
            findViewById<ImageButton>(R.id.btnVitaminaB3),
            findViewById<ImageButton>(R.id.btnVitaminaB6),
            findViewById<ImageButton>(R.id.btnVitaminaB12),
            findViewById<ImageButton>(R.id.btnVitaminaC),
            findViewById<ImageButton>(R.id.btnVitaminaD),
            findViewById<ImageButton>(R.id.btnVitaminaE),
            findViewById<ImageButton>(R.id.btnVitaminaK),
            findViewById<ImageButton>(R.id.btnAcidoFolico),
            findViewById<ImageButton>(R.id.btnHierro),
            findViewById<ImageButton>(R.id.btnCalcio),
            findViewById<ImageButton>(R.id.btnMagnesio),
            findViewById<ImageButton>(R.id.btnZinc),
            findViewById<ImageButton>(R.id.btnSelenio),
            findViewById<ImageButton>(R.id.btnFosforo),
            findViewById<ImageButton>(R.id.btnSodio),
            findViewById<ImageButton>(R.id.btnYodo),
            findViewById<ImageButton>(R.id.btnPotasio),
            findViewById<ImageButton>(R.id.btnManganeso),
            findViewById<ImageButton>(R.id.btnBoro),
            findViewById<ImageButton>(R.id.btnCobre),
            findViewById<ImageButton>(R.id.btnProteinas),
            findViewById<ImageButton>(R.id.btnCarbohidratos),
            findViewById<ImageButton>(R.id.btnFibra),
            findViewById<ImageButton>(R.id.btnColesterol),
            findViewById<ImageButton>(R.id.btnGTotales),
            findViewById<ImageButton>(R.id.btnGSaturadas),
            findViewById<ImageButton>(R.id.btnAzucar),
            findViewById<ImageButton>(R.id.btnCafeina),
            findViewById<ImageButton>(R.id.btnTodos)
        )

        nutrientes.forEachIndexed { index, button ->
            button.setOnClickListener {
                onNutrienteClick(index, button)
            }
        }

        btnTerminar.setOnClickListener {
            if(nutrientesSeleccionados.isEmpty()){
                Toast.makeText(this, getString(R.string.opcionesvacias), Toast.LENGTH_SHORT).show()
            } else {
                if(deCuestionario){
                    guardarEstadoCuestionario(2, nutrientesSeleccionados.map{nutrientesIDs[it]}, idUserRecibido)
                    navigateToPantallaInicio(2,idUserRecibido)}
                else{
                    guardarEstadoCuestionario(3, nutrientesSeleccionados.map{nutrientesIDs[it]}, idUserRecibido)
                    navigateToPantallaInicio(3,idUserRecibido)
                }
            }
        }

        btnOmitir.setOnClickListener { AlertDialog.Builder(this)
            .setTitle(getString(R.string.titleomitirpreg))
            .setMessage(getString(R.string.textomitir))
            .setPositiveButton(getString(R.string.si)) { _, _ ->
                if(deCuestionario || metasVacias){
                    guardarEstadoCuestionario(1, emptyList(), idUserRecibido)
                    navigateToPantallaInicio(1,idUserRecibido)}
                else if(!metasVacias){
                    guardarEstadoCuestionario(2, emptyList(), idUserRecibido)
                    navigateToPantallaInicio(2,idUserRecibido)}
            }
            .setNegativeButton(getString(R.string.no), null)
            .show() }

        btnCancelar.setOnClickListener { AlertDialog.Builder(this)
            .setTitle(getString(R.string.titlesalir))
            .setMessage(getString(R.string.textsalir))
            .setPositiveButton(getString(R.string.si)) { _, _ ->
                guardarEstadoCuestionario(1, emptyList(), idUserRecibido)
                    navigateToCuestionario()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun guardarEstadoCuestionario(idEstado: Int, nutrientes: List<Int>, idUsuario: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = NutrientesRequest(
                    id_user = idUsuario,
                    id_estado = idEstado,
                    nutrientes = nutrientes
                )

                val response: Response<CuestionarioResponse> =
                    RetrofitClient.instance.guardarNutrientes(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val mensaje = response.body()?.message ?: "Guardado correctamente"
                        println("Estado del cuestionario actualizado: $mensaje")
                    } else {
                        println("Error al guardar estado: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NutrientesActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onNutrienteClick(index: Int, button: ImageButton) {
        if (nutrientesSeleccionados.contains(index)) {
            // Cancelar la selección
            nutrientesSeleccionados.remove(index)
            button.setBackgroundResource(android.R.color.transparent)
        } else {
                // Seleccionar
                nutrientesSeleccionados.add(index)
                button.setBackgroundResource(R.drawable.bg_meta_seleccionada)
        }
        // Si selecciona "Sin restricciones", cancela la selección de todos los demás
        if (index == 32) {
            if (nutrientesSeleccionados.contains(index)) {
                // Limpiar los demás
                nutrientesSeleccionados.clear()
                nutrientesSeleccionados.add(index)
                nutrientes.forEach { it.setBackgroundResource(android.R.color.transparent) }
                button.setBackgroundResource(R.drawable.bg_meta_seleccionada)
            }
        } else {
            // Si selecciona cualquier otro, cancela la selección de "Sin restricciones" si esta está seleccionada
            if (nutrientesSeleccionados.contains(32)) {
                nutrientesSeleccionados.remove(32)
                nutrientes[32].setBackgroundResource(android.R.color.transparent)
            }
        }
    }

    fun navigateToPantallaInicio(estado: Int, idUsuario: Int){
        val intent = Intent(this, PantallaInicioActivity::class.java)
        intent.putExtra("ESTADO_CUESTIONARIO", estado)
        intent.putExtra("USUARIO_LOGUEADO", true)
        intent.putExtra("ID_USER", idUsuario)
        startActivity(intent)
    }

    fun navigateToCuestionario(){
        val intent = Intent(this, CuestionarioActivity::class.java)
        startActivity(intent)
    }
}