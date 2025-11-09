package com.rociomhparis.pylfront.ui.ingreso

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rociomhparis.pylfront.R
import com.rociomhparis.pylfront.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        val username = findViewById<EditText>(R.id.etUsernameREG)
        val password = findViewById<EditText>(R.id.etPasswordREG)
        val passwordConf = findViewById<EditText>(R.id.etPasswordConfREG)
        val fechaNacimiento = findViewById<EditText>(R.id.etFechaNacimientoREG)

        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)
        val btnSalir = findViewById<Button>(R.id.btnSalirREG)

        username.setOnFocusChangeListener { _, hasFocus -> username.hint = if(hasFocus) getString(R.string.ING_validosUserREG) else getString(R.string.ING_usernameIS) }
        password.setOnFocusChangeListener { _, hasFocus -> password.hint = if(hasFocus) getString(R.string.ING_validosPassREG)  else getString(R.string.ING_passwordIS) }
        passwordConf.setOnFocusChangeListener { _, hasFocus -> passwordConf.hint = if(hasFocus) getString(R.string.ING_validosPassREG)  else getString(R.string.ING_passConfREG) }
        fechaNacimiento.setOnFocusChangeListener { _, hasFocus -> fechaNacimiento.hint = if(hasFocus) getString(R.string.ING_validosFechaREG) else getString(R.string.fechaNacimiento) }

        btnRegistrarse.setOnClickListener {
            val usernameStr = username.text.toString().trim()
            val passwordStr = password.text.toString().trim()
            val passwordConfStr = passwordConf.text.toString().trim()
            val fechaStr = fechaNacimiento.text.toString().trim()

            val campos = listOf(username, fechaNacimiento)

            val camposVacios = campos.filter { it.text.toString().trim().isEmpty() }
            if (camposVacios.isNotEmpty()) {
                camposVacios.forEach { it.setBackgroundResource(R.drawable.campo_vacio_border) }
                camposVacios.first().requestFocus()
                Toast.makeText(this, getString(R.string.camposvacios), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (usernameStr.length < 5 || usernameStr.length > 15 || usernameStr.contains(" ")) { username.error = getString(R.string.userinvalido); username.requestFocus(); return@setOnClickListener }
            try {
                val formatter = SimpleDateFormat("dd/MM/yyyy")
                formatter.isLenient = false
                val fecha = formatter.parse(fechaStr)
                val minDate = formatter.parse(getString(R.string.fechaMinima))
                val maxDate = formatter.parse(getString(R.string.fechaMaxima))
                if (fecha.before(minDate) || fecha.after(maxDate)) { fechaNacimiento.error = getString(R.string.fecharango); fechaNacimiento.requestFocus(); return@setOnClickListener }
            } catch (e: Exception) { fechaNacimiento.error = getString(R.string.fechainvalida); fechaNacimiento.requestFocus(); return@setOnClickListener }

            if (passwordStr.isNotEmpty() || passwordConfStr.isNotEmpty()) {
                val passRegex = Regex("""^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@#\$%^+=!])(?=\S+$).{8,20}$""")
                if (!passwordStr.matches(passRegex)) { password.error = getString(R.string.passinvalida); password.requestFocus(); return@setOnClickListener }
                if (passwordStr != passwordConfStr) { passwordConf.error = getString(R.string.passdiferentes); passwordConf.requestFocus(); return@setOnClickListener }
            }

            CoroutineScope(Dispatchers.IO).launch {
                try{
                    val api = RetrofitClient.instance

                    val usernameRB = RequestBody.create("text/plain".toMediaTypeOrNull(), usernameStr)
                    val passwordRB = RequestBody.create("text/plain".toMediaTypeOrNull(), passwordStr)
                    val passwordConfRB = RequestBody.create("text/plain".toMediaTypeOrNull(), passwordConfStr)
                    val fechaRB = RequestBody.create("text/plain".toMediaTypeOrNull(), fechaStr)

                    val response = api.registrarUsuario(
                        usernameRB, passwordRB, passwordConfRB, fechaRB
                    )

                    withContext(Dispatchers.Main){
                        if(response.isSuccessful){
                            Toast.makeText(this@RegistroActivity, getString(R.string.regCorrecto), Toast.LENGTH_LONG).show()
                            navigateToInicioSesion(usernameStr, passwordStr)
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: getString(R.string.unknownError)
                            Toast.makeText(this@RegistroActivity, "${getString(R.string.error)}: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: java.lang.Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@RegistroActivity, "${getString(R.string.error)}: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        btnSalir.setOnClickListener { navigateToIngreso() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun navigateToInicioSesion(username: String, password: String){
        val intent = Intent(this, InicioSesionActivity::class.java)
        intent.putExtra("username", username)
        intent.putExtra("password", password)
        startActivity(intent)
    }

    fun navigateToIngreso(){
        val intent = Intent(this, IngresoActivity::class.java)
        startActivity(intent)
    }
}