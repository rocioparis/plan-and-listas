package com.rociomhparis.pylfront.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prolificinteractive.materialcalendarview.*
import com.rociomhparis.pylfront.Ingrediente
import java.lang.Exception
import com.rociomhparis.pylfront.IntervaloFragment
import com.rociomhparis.pylfront.ListaComprasResponse
import com.rociomhparis.pylfront.MetaCoherencia
import com.rociomhparis.pylfront.R
import com.rociomhparis.pylfront.Receta
import com.rociomhparis.pylfront.RecetaConIngredientes
import com.rociomhparis.pylfront.RecetasAdapter
import com.rociomhparis.pylfront.RetrofitClient
import com.rociomhparis.pylfront.ui.ingreso.IngresoActivity
import com.rociomhparis.pylfront.ui.ingreso.InicioSesionActivity
import com.rociomhparis.pylfront.ui.ingreso.RegistroActivity
import com.rociomhparis.pylfront.ui.listascompras.ListaCompraActivity
import com.rociomhparis.pylfront.ui.listascompras.MisListasActivity
import com.rociomhparis.pylfront.ui.planificacion.DatosPlanificacionActivity
import com.rociomhparis.pylfront.ui.planificacion.FechaActivity
import com.rociomhparis.pylfront.ui.recetas.MisRecetasActivity
import com.rociomhparis.pylfront.ui.recetas.RecetaActivity
import com.rociomhparis.pylfront.ui.recetas.ResultadosBusquedaActivity
import com.rociomhparis.pylfront.ui.recetas.VerMasActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class PantallaInicioActivity : AppCompatActivity() {

    private lateinit var recyclerRecetas: RecyclerView
    private lateinit var materialCalendarView: MaterialCalendarView
    private lateinit var btnListaComprasPI: Button
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var etBarraBqda: EditText
    private lateinit var btnVerMasPI: ImageButton
    private lateinit var barraAviso: LinearLayout
    private lateinit var panelPlanificador: LinearLayout

    private lateinit var btnLogout: LinearLayout

    private val recetas = mutableListOf<Receta>()

    private var fechaSeleccionada: CalendarDay? = null

    companion object {
        const val REQUEST_DATOS_PLANIFICACION = 100
    }

    private var comensalesConfirmados: Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_inicio)

        val idUserRecibido = intent.getIntExtra("ID_USER", 0)

        barraAviso = findViewById(R.id.barraAviso)
        btnLogout = findViewById(R.id.btnLogout)
        materialCalendarView = findViewById(R.id.materialCalendarView)
        btnListaComprasPI = findViewById(R.id.btnListaComprasPI)
        bottomNav = findViewById(R.id.bottomNav)
        panelPlanificador = findViewById(R.id.panelPlanificador)
        recyclerRecetas = findViewById(R.id.recyclerRecetas)
        etBarraBqda = findViewById(R.id.etBuscarPI)
        btnVerMasPI = findViewById(R.id.btnVerMasPI)

        if (idUserRecibido==0) {
            barraAviso.visibility = View.VISIBLE
            panelPlanificador.visibility = View.GONE
            recyclerRecetas.visibility = View.VISIBLE
            btnLogout.visibility = View.GONE

            val columnas = 3
            recyclerRecetas.layoutManager =
                GridLayoutManager(this, columnas, GridLayoutManager.VERTICAL, false)

            materialCalendarView.visibility = View.GONE
            btnListaComprasPI.visibility = View.GONE
            bottomNav.visibility = View.GONE

            barraAviso.findViewById<Button>(R.id.btnRegistrarseBarra).setOnClickListener {
                startActivity(Intent(this, RegistroActivity::class.java))
            }
            barraAviso.findViewById<Button>(R.id.btnIniciarSesionBarra).setOnClickListener {
                startActivity(Intent(this, InicioSesionActivity::class.java))
            }
        } else {
            barraAviso.visibility = View.GONE
            panelPlanificador.visibility = View.VISIBLE
            btnLogout.visibility = View.VISIBLE
            recyclerRecetas.visibility = View.VISIBLE
            recyclerRecetas.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }

        val adapter = RecetasAdapter(recetas) { receta ->
            navigateToReceta(receta, idUserRecibido)
        }

        adapter.setOnItemLongClickListener { view, receta ->
            val dragData = ClipData.newPlainText("", "")
            val shadow = View.DragShadowBuilder(view)
            view.startDragAndDrop(dragData, shadow, receta, 0)
            true
        }
        recyclerRecetas.adapter = adapter

        cargarRecetasDesdeBackend(adapter, idUserRecibido)

        materialCalendarView.setOnDateChangedListener { _, date, selected ->
            if (selected) {
                fechaSeleccionada = date
                mostrarOpcionesFecha(date, idUserRecibido)
            }
        }

        materialCalendarView.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DRAG_ENTERED -> {
                    v.alpha = 0.8f
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    v.alpha = 1f
                    true
                }
                DragEvent.ACTION_DROP -> {
                    v.alpha = 1f
                    val receta = event.localState as? Receta
                    if (receta != null && fechaSeleccionada != null) {
                        planificarReceta(receta, idUserRecibido, fechaSeleccionada!!)
                    } else {
                        Toast.makeText(this, getString(R.string.dragdropinv), Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

        btnLogout.findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.IGO_logout))
                .setMessage(getString(R.string.textlogout))
                .setPositiveButton(getString(R.string.si)) { dialog, which ->
                    cerrarSesionBackend(idUserRecibido)
                }
                .setNegativeButton(getString(R.string.no)) { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }

        etBarraBqda.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = etBarraBqda.text.toString().trim()
                if (query.isNotEmpty()) {
                    navigateToResultadosBusqueda(query, idUserRecibido)
                } else {
                    Toast.makeText(this, getString(R.string.bqdavacia), Toast.LENGTH_SHORT)
                        .show()
                }
                true
            } else {
                false
            }
        }

        btnVerMasPI.setOnClickListener { navigateToVerMas(idUserRecibido) }

        btnListaComprasPI.setOnClickListener { mostrarDialogListaCompras(idUserRecibido) }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navInicio -> true
                R.id.navRecetas -> {
                    val intent = Intent(this, MisRecetasActivity::class.java)
                    intent.putExtra("ID_USER", idUserRecibido)
                    startActivity(intent)
                    true
                }

                R.id.navListas -> {
                    val intent = Intent(this, MisListasActivity::class.java)
                    intent.putExtra("ID_USER", idUserRecibido)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun cargarRecetasDesdeBackend(adapter: RecetasAdapter, idUser: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerRecetasRecomendadas(idUser)
                if (response.isSuccessful) {
                    val recetasRespuesta = response.body() ?: emptyList()
                    recetas.clear()
                    recetas.addAll(recetasRespuesta.take(25))
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        this@PantallaInicioActivity,
                        getString(R.string.recetasnotfound),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PantallaInicioActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun navigateToVerMas(idUser: Int) {
        val intent = Intent(this, VerMasActivity::class.java)
        intent.putExtra("ID_USER", idUser)
        startActivity(intent)
    }

    fun navigateToResultadosBusqueda(query: String, idUser: Int) {
        val intent = Intent(this, ResultadosBusquedaActivity::class.java)
        intent.putExtra("QUERY", query)
        intent.putExtra("ID_USER", idUser)
        startActivity(intent)
    }

    fun navigateToReceta(receta: Receta?, idUser: Int) {
        if (receta == null) return

        val intent = Intent(this, RecetaActivity::class.java)
        intent.putExtra("ID_RECETA", receta.id)
        intent.putExtra("NOMBRE_RECETA", receta.nombre)
        intent.putExtra("IMAGEN_RECETA", receta.imagen_url)
        intent.putParcelableArrayListExtra(
            "INGREDIENTES_RECETA",
            ArrayList(receta.ingredientes ?: emptyList())
        )
        intent.putExtra("PROCEDIMIENTO_RECETA", receta.procedimiento)
        intent.putExtra("ID_USER", idUser)
        startActivity(intent)
    }

    private fun planificarReceta(receta: Receta, idUser: Int, fecha: CalendarDay) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaFormateada = sdf.format(fecha.date)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.agregarRecetaAlPlan(
                    idReceta = receta.id,
                    idUser = idUser,
                    fecha = fechaFormateada
                )
                if (response.isSuccessful) {
                    val coherencias = response.body()?.coherencia ?: emptyMap()
                    if(coherencias.isNullOrEmpty()){
                        navigateToDatosPlanificacion(receta, idUser, fecha)
                    }
                    else{
                        mostrarDialogCoherencia(receta, idUser, fecha, coherencias)
                    }
                } else {
                    Toast.makeText(this@PantallaInicioActivity, getString(R.string.errorplan), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PantallaInicioActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogCoherencia(
        receta: Receta,
        idUser: Int,
        fecha: CalendarDay,
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

    private fun navigateToDatosPlanificacion(receta: Receta?, idUser: Int, fecha: CalendarDay) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaFormateada = sdf.format(fecha.date)
        if(receta==null) return
        val intent = Intent(this, DatosPlanificacionActivity::class.java)
        intent.putExtra("ID_RECETA", receta.id)
        intent.putExtra("NOMBRE_RECETA", receta.nombre)
        intent.putExtra("ID_USER", idUser)
        intent.putExtra("FECHA_PLAN", fechaFormateada)
        startActivityForResult(intent, REQUEST_DATOS_PLANIFICACION)
    }

    private fun mostrarOpcionesFecha(fecha: CalendarDay, idUser: Int) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.titledate))
        dialog.setMessage(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fecha.date))
        dialog.setNegativeButton(getString(R.string.btnCancelar)) { d, _ -> d.dismiss() }
        dialog.setNeutralButton(getString(R.string.verplanif)) { _, _ ->
            navigateToFechaActivity(fecha,idUser)
        }
        dialog.setPositiveButton(getString(R.string.planificar)) { _, _ ->
            Toast.makeText(this, getString(R.string.instruccionesplan), Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }

    private fun navigateToFechaActivity(fecha: CalendarDay,idUser: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaFormateada = sdf.format(fecha.date)
        val intent = Intent(this, FechaActivity::class.java)
        intent.putExtra("ID_USER", idUser)
        intent.putExtra("FECHA_SELECCIONADA", fechaFormateada)
        intent.putExtra("COMENSALES", comensalesConfirmados)
        startActivity(intent)
    }

    private fun mostrarDialogListaCompras(idUser: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.fragment_intervalo)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etFechaInicio = dialog.findViewById<EditText>(R.id.etFechaInicioIV)
        val etFechaFinal = dialog.findViewById<EditText>(R.id.etFechaFinalIV)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarIV)
        val btnAceptar = dialog.findViewById<Button>(R.id.btnAceptarIV)

        etFechaInicio.setOnClickListener {
            mostrarDatePicker { fechaSeleccionada ->
                etFechaInicio.setText(fechaSeleccionada)
            }
        }

        etFechaFinal.setOnClickListener {
            mostrarDatePicker { fechaSeleccionada ->
                etFechaFinal.setText(fechaSeleccionada)
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnAceptar.setOnClickListener {
            val fechaInicio = etFechaInicio.text.toString()
            val fechaFinal = etFechaFinal.text.toString()
            if(fechaInicio.isEmpty() || fechaFinal.isEmpty()) {
                Toast.makeText(this, getString(R.string.ambasfechas), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            obtenerListaDeCompras(idUser,fechaInicio, fechaFinal)

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarDatePicker(onFechaSeleccionada: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val fecha = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                onFechaSeleccionada(fecha)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun cerrarSesionBackend(idUser: Int){
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.cerrarSesion(idUser)
                if(response.isSuccessful){
                    Toast.makeText(this@PantallaInicioActivity, getString(R.string.lgtCorrecto), Toast.LENGTH_SHORT).show()
                    navigateToIngreso()
                }
                else {
                    Toast.makeText(this@PantallaInicioActivity, getString(R.string.errorlgt), Toast.LENGTH_SHORT).show()
                }
            }
            catch (e: kotlin.Exception){
                Toast.makeText(this@PantallaInicioActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun navigateToIngreso(){
        val intent = Intent(this, IngresoActivity::class.java)
        startActivity(intent)
    }

    private fun obtenerListaDeCompras(idUser: Int, fechaInicio: String, fechaFinal: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.obtenerListaCompras(idUser, fechaInicio, fechaFinal)
                if (response.isSuccessful) {
                    val listaCompras: ListaComprasResponse? = response.body()

                    if (listaCompras != null) {


                        val listaRecetasAjustadas = listaCompras.recetas.map { receta ->
                            receta.copy(
                                ingredientes = receta.ingredientes.map { ing ->
                                    ing.copy(cantidad = (ing.cantidad ?: 1.0) * comensalesConfirmados)
                                }
                            )
                        }

                        AlertDialog.Builder(this@PantallaInicioActivity)
                            .setTitle(getString(R.string.lcCorrecto))
                            .setMessage(getString(R.string.queLC))
                            .setPositiveButton(getString(R.string.verahora)) { _, _ ->
                                val intent = Intent(this@PantallaInicioActivity, ListaCompraActivity::class.java)
                                intent.putParcelableArrayListExtra(
                                    "LISTA_COMPRAS",
                                    ArrayList(listaRecetasAjustadas)
                                )
                                intent.putExtra("ID_USER", idUser)
                                val idLista = listaCompras.idListaDeCompras
                                intent.putExtra("ID_LISTA", idLista)
                                startActivity(intent)
                            }
                            .setNegativeButton(getString(R.string.vermastarde)) { d, _ ->
                                Toast.makeText(
                                    this@PantallaInicioActivity,
                                    getString(R.string.vermasLC),
                                    Toast.LENGTH_SHORT
                                ).show()
                                d.dismiss()
                            }
                            .show()
                    } else {
                        Toast.makeText(this@PantallaInicioActivity, getString(R.string.recetasnotfound), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PantallaInicioActivity, getString(R.string.errorLC), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PantallaInicioActivity, getString(R.string.errorcon), Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_DATOS_PLANIFICACION && resultCode == Activity.RESULT_OK) {
            comensalesConfirmados = data?.getIntExtra("COMENSALES", 1) ?: 1
        }
    }

}