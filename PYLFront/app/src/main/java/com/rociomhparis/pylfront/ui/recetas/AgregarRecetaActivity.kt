package com.rociomhparis.pylfront.ui.recetas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.rociomhparis.pylfront.AgregarRecetaRequest
import com.rociomhparis.pylfront.Ingrediente
import com.rociomhparis.pylfront.IngredienteRequest
import com.rociomhparis.pylfront.R
import com.rociomhparis.pylfront.RetrofitClient
import kotlinx.coroutines.launch

class AgregarRecetaActivity : AppCompatActivity() {

    private lateinit var ibImgAR: ImageButton
    private lateinit var ibDeleteImgAR: ImageButton
    private lateinit var etNameAR: EditText
    private lateinit var etIngredientAR: EditText
    private lateinit var etCantIngAR: EditText
    private lateinit var spUnitIngrAR: Spinner
    private lateinit var etProcAR: EditText
    private lateinit var btnAddAR: Button
    private lateinit var tvTitleAR: TextView
    private lateinit var btnOKIngrAR: Button
    private lateinit var layoutIngredientes: LinearLayout

    private val PICK_IMAGE_REQUEST = 1
    private var imagenUri: Uri? = null
    private var recetaExistente: HashMap<String, Any>? = null
    private var indexExistente: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregar_receta)

        ibImgAR = findViewById(R.id.ibImgAR)
        ibDeleteImgAR = findViewById(R.id.ibImgDeleteAR)
        etNameAR = findViewById(R.id.etNameAR)
        etIngredientAR = findViewById(R.id.etIngredientAR)
        etCantIngAR = findViewById(R.id.etCantIngAR)
        spUnitIngrAR = findViewById(R.id.spUnitIngrAR)
        etProcAR = findViewById(R.id.etProcAR)
        btnAddAR = findViewById(R.id.btnAddAR)
        tvTitleAR = findViewById(R.id.tvTitleAR)
        val btnCancelAR: Button = findViewById(R.id.btnCancelAR)
        btnOKIngrAR = findViewById(R.id.btnOKIngrAR)
        layoutIngredientes = findViewById(R.id.layoutIngredientesAgregados)

        val idUserRecibido = intent.getIntExtra("ID_USER", 0)

        cargarUnidades()

        recetaExistente = intent.getSerializableExtra("recetaTemp") as? HashMap<String, Any>
        indexExistente = recetaExistente?.get("index") as? Int

        if (recetaExistente != null) {
            tvTitleAR.text = "Modificar receta"
            btnAddAR.text = "Guardar"

            etNameAR.setText(recetaExistente!!["nombre"] as? String)
            etIngredientAR.setText(recetaExistente!!["ingrediente"] as? String)
            etCantIngAR.setText(recetaExistente!!["cantidad"] as? String)
            spUnitIngrAR.setSelection(
                (spUnitIngrAR.adapter as ArrayAdapter<String>).getPosition(
                    recetaExistente!!["unidad"] as? String
                )
            )
            etProcAR.setText(recetaExistente!!["procedimiento"] as? String)
            (recetaExistente!!["imagenUri"] as? String)?.let { uriStr ->
                imagenUri = Uri.parse(uriStr)
                ibImgAR.setImageURI(imagenUri)
                ibDeleteImgAR.visibility = View.VISIBLE
            }
        }

        ibImgAR.setOnClickListener { abrirGaleria() }

        ibDeleteImgAR.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.titleeliminarimg))
                .setMessage(getString(R.string.texteliminarimg))
                .setPositiveButton(getString(R.string.si)) { dialog, _ ->
                    ibImgAR.setImageResource(R.drawable.ic_add_image)
                    ibDeleteImgAR.visibility = View.GONE
                    imagenUri = null
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
                .show()
        }

        btnOKIngrAR.setOnClickListener {
            val nombre = etIngredientAR.text.toString().trim()
            val cantidad = etCantIngAR.text.toString().trim()
            val unidad = spUnitIngrAR.selectedItem.toString()

            if (nombre.isNotEmpty() && cantidad.isNotEmpty()) {
                agregarIngrediente(nombre, cantidad, unidad)

                etIngredientAR.text.clear()
                etCantIngAR.text.clear()
                spUnitIngrAR.setSelection(0)
            } else {
                Toast.makeText(this, getString(R.string.camposvaciosingred), Toast.LENGTH_SHORT).show()
            }
        }


        btnCancelAR.setOnClickListener { finish() }

        btnAddAR.setOnClickListener {
            val ingredientes = mutableListOf<IngredienteRequest>()
            for (i in 0 until layoutIngredientes.childCount) {
                val item = layoutIngredientes.getChildAt(i)
                val tv = item.findViewById<TextView>(R.id.tvIngrediente)
                val parts = tv.text.split(" - ")
                if (parts.size == 2) {
                    val nombre = parts[0].trim()
                    val cantidadUnidad = parts[1].trim().split(" ")
                    val cantidad = cantidadUnidad[0].toFloatOrNull()
                    val unidad = cantidadUnidad.getOrElse(1) { "unidad" }
                    ingredientes.add(IngredienteRequest(nombre, cantidad, unidad))
                }
            }

            val recetaRequest = AgregarRecetaRequest(
                nombre = etNameAR.text.toString(),
                procedimiento = etProcAR.text.toString(),
                ingredientes = ingredientes,
                imagenUri = imagenUri?.toString(),
                IDUser = idUserRecibido
            )

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.agregarReceta(recetaRequest)
                    if (response.isSuccessful) {
                        val recetaCreada = response.body()
                        if (recetaCreada != null) {
                            val recetaTemp = hashMapOf<String, Any>(
                                "nombre" to recetaCreada.nombre,
                                "procedimiento" to recetaCreada.procedimiento,
                                "id" to recetaCreada.id
                            )
                            imagenUri?.let { recetaTemp["imagenUri"] = it.toString() }

                            val intent = Intent()
                            intent.putExtra("recetaTemp", recetaTemp)
                            setResult(RESULT_OK, intent)
                            Toast.makeText(this@AgregarRecetaActivity, getString(R.string.rcCorrecto), Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@AgregarRecetaActivity, getString(R.string.errorAgregarRec), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: getString(R.string.errorcon)
                        Toast.makeText(this@AgregarRecetaActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@AgregarRecetaActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imagenUri = data.data
            ibImgAR.setImageURI(imagenUri)
            ibDeleteImgAR.visibility = View.VISIBLE
        }
    }

    private fun cargarUnidades() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerUnidades()
                if (response.isSuccessful) {
                    val unidades = response.body()?.map { it.abreviacion } ?: emptyList()
                    val adapter = ArrayAdapter(
                        this@AgregarRecetaActivity,
                        android.R.layout.simple_spinner_item,
                        unidades
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spUnitIngrAR.adapter = adapter

                    recetaExistente?.let {
                        val unidad = it["unidad"] as? String
                        val pos = unidades.indexOf(unidad)
                        if (pos >= 0) spUnitIngrAR.setSelection(pos)
                    }
                } else {
                    Toast.makeText(this@AgregarRecetaActivity, getString(R.string.errorUnidades), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AgregarRecetaActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun agregarIngrediente(nombre: String, cantidad: String, unidad: String) {
        val itemView = layoutInflater.inflate(R.layout.item_ingrediente_agregado, null)

        val tvIngrediente = itemView.findViewById<TextView>(R.id.tvIngrediente)
        val btnDelete = itemView.findViewById<ImageButton>(R.id.btnDeleteIngrediente)

        tvIngrediente.text = "$nombre - $cantidad $unidad"

        btnDelete.setOnClickListener {
            layoutIngredientes.removeView(itemView)
        }

        layoutIngredientes.addView(itemView)
    }
}