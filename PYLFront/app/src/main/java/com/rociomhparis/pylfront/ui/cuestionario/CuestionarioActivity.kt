package com.rociomhparis.pylfront.ui.cuestionario

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rociomhparis.pylfront.R
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.rociomhparis.pylfront.CuestionarioRequest
import com.rociomhparis.pylfront.CuestionarioResponse
import com.rociomhparis.pylfront.RetrofitClient
import com.rociomhparis.pylfront.ui.PantallaInicioActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class CuestionarioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cuestionario)

        val idUserRecibido = intent.getIntExtra("ID_USER", 0)

        val btnOmitir = findViewById<Button>(R.id.btnOmitirCuestionarioCS)
        val btnPregunta1 = findViewById<Button>(R.id.btnPregunta1)
        val btnPregunta2 = findViewById<Button>(R.id.btnPregunta2)

        btnOmitir.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.titleomitir))
                .setMessage(getString(R.string.textomitir))
                .setPositiveButton(getString(R.string.si)) { _, _ ->
                    guardarEstadoCuestionario(1, idUserRecibido)
                    navigateToPantallaInicio(idUserRecibido,1)
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }
        btnPregunta1.setOnClickListener { navigateToMetasNutricionales(idUserRecibido, true) }
        btnPregunta2.setOnClickListener { navigateToNutrientes(idUserRecibido, true) }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun guardarEstadoCuestionario(idEstado: Int, idUserRecibido: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = CuestionarioRequest(
                    id_user = idUserRecibido,
                    id_estado = idEstado
                )

                val response: Response<CuestionarioResponse> =
                    RetrofitClient.instance.guardarCuestionario(request)

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
                    println(getString(R.string.errorcon))
                }
            }
        }
    }

    fun navigateToPantallaInicio(idUsuario: Int, estado: Int){
        val intent = Intent(this, PantallaInicioActivity::class.java)
        intent.putExtra("ESTADO_CUESTIONARIO", estado)
        intent.putExtra("USUARIO_LOGUEADO", true)
        intent.putExtra("ID_USER", idUsuario)
        startActivity(intent)
    }

    fun navigateToMetasNutricionales(idUsuario: Int, deCuestionario: Boolean){
        val intent = Intent(this, MetasNutricionalesActivity::class.java)
        intent.putExtra("ID_USER", idUsuario)
        intent.putExtra("DE_CUESTIONARIO", deCuestionario)
        startActivity(intent)
    }

    fun navigateToNutrientes(idUsuario: Int, deCuestionario: Boolean){
        val intent = Intent(this, NutrientesActivity::class.java)
        intent.putExtra("ID_USER", idUsuario)
        intent.putExtra("DE_CUESTIONARIO", deCuestionario)
        startActivity(intent)
    }
}