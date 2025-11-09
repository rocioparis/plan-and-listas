package com.rociomhparis.pylfront.ui.listascompras

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rociomhparis.pylfront.R
import com.rociomhparis.pylfront.ui.PantallaInicioActivity
import com.rociomhparis.pylfront.ui.recetas.MisRecetasActivity
import androidx.recyclerview.widget.RecyclerView
import com.rociomhparis.pylfront.ListaComprasResumen
import com.rociomhparis.pylfront.MisListasAdapter
import com.rociomhparis.pylfront.RetrofitClient
import kotlinx.coroutines.launch

class MisListasActivity : AppCompatActivity() {

    private lateinit var rvMisListas: RecyclerView
    private lateinit var bottomNav: BottomNavigationView
    private val listas = mutableListOf<ListaComprasResumen>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mis_listas)

        rvMisListas = findViewById(R.id.rvMisListas)
        bottomNav = findViewById(R.id.bottomNav)

        val idUser = intent.getIntExtra("ID_USER", 0)

        rvMisListas.layoutManager = LinearLayoutManager(this)
        rvMisListas.adapter = MisListasAdapter(listas, idUser)

        cargarListasDeCompras(idUser)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio -> {
                    val intent = Intent(this, PantallaInicioActivity::class.java)
                    intent.putExtra("ID_USER", idUser)
                    startActivity(intent)
                    true
                }
                R.id.navRecetas -> {
                    val intent = Intent(this, MisRecetasActivity::class.java)
                    intent.putExtra("ID_USER", idUser)
                    startActivity(intent)
                    true
                }
                R.id.navListas -> true
                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun cargarListasDeCompras(idUser: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerListasUsuario(idUser)
                if (response.isSuccessful) {
                    val listasBackend = response.body() ?: emptyList()
                    listas.clear()
                    listas.addAll(listasBackend)
                    rvMisListas.adapter?.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@MisListasActivity, getString(R.string.errorMisListas), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MisListasActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
            }
        }
    }
}