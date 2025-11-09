package com.rociomhparis.pylfront.ui.ingreso

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rociomhparis.pylfront.R
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.rociomhparis.pylfront.LoginRequest
import com.rociomhparis.pylfront.RetrofitClient
import com.rociomhparis.pylfront.ui.PantallaInicioActivity
import com.rociomhparis.pylfront.ui.cuestionario.CuestionarioActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InicioSesionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio_sesion)

        val username = findViewById<EditText>(R.id.etUsernameIS)
        val password = findViewById<EditText>(R.id.etPasswordIS)

        val usernameRecibido = intent.getStringExtra("username")
        val passwordRecibida = intent.getStringExtra("password")

        username.setText(usernameRecibido ?: "")
        password.setText(passwordRecibida ?: "")

        val btnSalir = findViewById<Button>(R.id.btnSalirIS)
        val btnLogin = findViewById<Button>(R.id.btnLoginIS)

        btnSalir.setOnClickListener { navigateToIngreso() }

        btnLogin.setOnClickListener {
            val usernameStr = username.text.toString().trim()
            val passwordStr = password.text.toString().trim()

            val campos = listOf<EditText>(
                username,
                password,
            )

            campos.forEach { campo ->
                campo.background = ContextCompat.getDrawable(this, android.R.drawable.edit_text)
            }

            val camposVacios = campos.filter { it.text.toString().trim().isEmpty() }
            if (camposVacios.isNotEmpty()) {
                camposVacios.forEach { campo ->
                    campo.setBackgroundResource(R.drawable.campo_vacio_border)
                }
                camposVacios.first().requestFocus()
                Toast.makeText(this, getString(R.string.camposvacios), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            iniciarSesion(usernameStr,passwordStr)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun iniciarSesion(username: String, password: String){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val request = LoginRequest(username, password)
                val response = RetrofitClient.instance.loginUser(request)

                withContext(Dispatchers.Main){
                    if(response.isSuccessful){
                        val body = response.body()
                        if(body != null){
                            Toast.makeText(
                                this@InicioSesionActivity,
                                body.message,
                                Toast.LENGTH_SHORT
                            ).show()

                            val sharedPref = getSharedPreferences("PlanListasSesion", MODE_PRIVATE)
                            with(sharedPref.edit()){
                                putString("usuarioLogueado", body.usuario.username)
                                putInt("idUsuario", body.usuario.id)
                                putInt("idSesion", body.idSesion)
                                apply()
                            }

                            // Redirigir dependiendo si es la primera vez que inicia sesión
                            // (si es la primera vez se acaba de registrar y se lo manda al cuestionario, si no a la pantalla inicio)
                            val primeraVez = sharedPref.getBoolean("primeraVez_${body.usuario.username}", true)
                            if(primeraVez){
                                navigateToCuestionario(body.usuario.id)
                                with(sharedPref.edit()){
                                    putBoolean("primeraVez_${body.usuario.username}", false)
                                    apply()
                                }
                            } else {
                                navigateToPantallaInicio(body.usuario.id)
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@InicioSesionActivity,
                            getString(R.string.userorpassinc),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(
                        this@InicioSesionActivity,
                        getString(R.string.errorcon),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun navigateToIngreso() {
        val intent = Intent(this, IngresoActivity::class.java)
        startActivity(intent)
    }

    fun navigateToCuestionario(idUsuario: Int) {
        val intent = Intent(this, CuestionarioActivity::class.java)
        intent.putExtra("ID_USER", idUsuario)
        startActivity(intent)
    }

    fun navigateToPantallaInicio(idUsuario: Int) {
        val intent = Intent(this, PantallaInicioActivity::class.java)
        intent.putExtra("ID_USER", idUsuario)
        startActivity(intent)
    }
}