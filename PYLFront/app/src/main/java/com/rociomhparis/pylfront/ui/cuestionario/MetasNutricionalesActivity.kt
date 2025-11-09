package com.rociomhparis.pylfront.ui.cuestionario

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rociomhparis.pylfront.R
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.rociomhparis.pylfront.CuestionarioRequest
import com.rociomhparis.pylfront.CuestionarioResponse
import com.rociomhparis.pylfront.MetasRequest
import com.rociomhparis.pylfront.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class MetasNutricionalesActivity : AppCompatActivity() {

    private val metasSeleccionadas = mutableSetOf<Int>()
    private val MAX_SELECCIONES = 3
    private lateinit var metas: List<ImageButton>

    private val metasIDs = listOf(1,2,3,4,5,6,7,8,9,10,11)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_metas_nutricionales)

        val btnOmitir = findViewById<Button>(R.id.btnOmitirPreguntaMN)
        val btnCancelar = findViewById<Button>(R.id.btnCancelarMN)
        val idUserRecibido = intent.getIntExtra("ID_USER", 0)

        metas = listOf(
            findViewById<ImageButton>(R.id.btnMeta1),
            findViewById<ImageButton>(R.id.btnMeta2),
            findViewById<ImageButton>(R.id.btnMeta3),
            findViewById<ImageButton>(R.id.btnMeta4),
            findViewById<ImageButton>(R.id.btnMeta5),
            findViewById<ImageButton>(R.id.btnMeta6),
            findViewById<ImageButton>(R.id.btnMeta7),
            findViewById<ImageButton>(R.id.btnMeta8),
            findViewById<ImageButton>(R.id.btnMeta9),
            findViewById<ImageButton>(R.id.btnMeta10),
            findViewById<ImageButton>(R.id.btnNinguna)
        )

        metas.forEachIndexed { index, button ->
            button.setOnClickListener {
                onMetaClick(index, button)
            }
        }

        btnOmitir.setOnClickListener { AlertDialog.Builder(this)
            .setTitle(getString(R.string.titleomitirpreg))
            .setMessage(getString(R.string.textomitir))
            .setPositiveButton(getString(R.string.si)) { _, _ ->
                guardarEstadoCuestionario(1, emptyList(), idUserRecibido)
                navigateToNutrientes(idUserRecibido,false, true)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show() }

        btnCancelar.setOnClickListener { AlertDialog.Builder(this)
            .setTitle(getString(R.string.titleomitir))
            .setMessage(getString(R.string.textomitir))
            .setPositiveButton(getString(R.string.si)) { _, _ ->
                finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show() }

        findViewById<Button>(R.id.btnSiguienteMN).setOnClickListener {
            if(metasSeleccionadas.isEmpty()){
                Toast.makeText(this, getString(R.string.opcionesvacias), Toast.LENGTH_SHORT).show()
            } else {
                guardarEstadoCuestionario(2, metasSeleccionadas.map { metasIDs[it] }, idUserRecibido)
                navigateToNutrientes(idUserRecibido,false,false)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun guardarEstadoCuestionario(idEstado: Int, metas: List<Int>, idUserRecibido: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = MetasRequest(
                    id_user = idUserRecibido,
                    id_estado = idEstado,
                    metas = metas
                )

                val response: Response<CuestionarioResponse> =
                    RetrofitClient.instance.guardarMetas(request)

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
                    Toast.makeText(this@MetasNutricionalesActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun onMetaClick(index: Int, button: ImageButton) {
        if (metasSeleccionadas.contains(index)) {
            // Cancelar la selección
            metasSeleccionadas.remove(index)
            button.setBackgroundResource(android.R.color.transparent)
        } else {
            if (metasSeleccionadas.size < MAX_SELECCIONES) {
                // Seleccionar
                metasSeleccionadas.add(index)
                button.setBackgroundResource(R.drawable.bg_meta_seleccionada)
            } else {
                Toast.makeText(this, "Solo pueden seleccionarse hasta 3 metas", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        // Si selecciona "Ninguna", cancela la selección de todas las demás
        if (index == 10) {
            if (metasSeleccionadas.contains(index)) {
                // Limpiar las demás
                metasSeleccionadas.clear()
                metasSeleccionadas.add(index)
                metas.forEach { it.setBackgroundResource(android.R.color.transparent) }
                button.setBackgroundResource(R.drawable.bg_meta_seleccionada)
            }
        } else {
            // Si selecciona cualquier otra, cancela la selección de "Ninguna" si esta está seleccionada
            if (metasSeleccionadas.contains(10)) {
                metasSeleccionadas.remove(10)
                metas[10].setBackgroundResource(android.R.color.transparent)
            }
        }
    }
    fun navigateToNutrientes(idUsuario: Int, deCuestionario: Boolean, metasVacias: Boolean){
        val intent = Intent(this, NutrientesActivity::class.java)
        intent.putExtra("ID_USER", idUsuario)
        intent.putExtra("DE_CUESTIONARIO", deCuestionario)
        intent.putExtra("METAS_VACIAS", metasVacias)
        startActivity(intent)
    }
}