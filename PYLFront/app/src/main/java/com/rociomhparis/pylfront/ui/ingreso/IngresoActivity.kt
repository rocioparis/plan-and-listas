package com.rociomhparis.pylfront.ui.ingreso

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rociomhparis.pylfront.R
import com.rociomhparis.pylfront.ui.PantallaInicioActivity

class IngresoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ingreso)

        val btnRegistrarse = findViewById<Button>(R.id.btnRegistrarse)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)
        val tvSinCuenta = findViewById<TextView>(R.id.tvSinCuenta)

        btnRegistrarse.setOnClickListener{ navigateToRegistrarse() }
        btnIniciarSesion.setOnClickListener { navigateToIniciarSesion() }
        tvSinCuenta.setOnClickListener { navigateToPantallaInicio() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun navigateToRegistrarse(){
        val intent = Intent(this, RegistroActivity::class.java)
        startActivity(intent)
    }

    fun navigateToIniciarSesion(){
        val intent = Intent(this, InicioSesionActivity::class.java)
        startActivity(intent)
    }

    fun navigateToPantallaInicio(){
        val intent = Intent(this, PantallaInicioActivity::class.java)
        startActivity(intent)
    }
}