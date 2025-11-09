package com.rociomhparis.pylfront

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rociomhparis.pylfront.R

class IntervaloFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intervalo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnCancelar = view.findViewById<Button>(R.id.btnCancelarIV)
        val btnAceptar = view.findViewById<Button>(R.id.btnAceptarIV)

        btnCancelar.setOnClickListener {
            dismiss()
        }

        btnAceptar.setOnClickListener {
            val fechaInicio = view.findViewById<EditText>(R.id.etFechaInicioIV).text.toString()
            val fechaFinal = view.findViewById<EditText>(R.id.etFechaFinalIV).text.toString()

            if (fechaInicio.isBlank() || fechaFinal.isBlank()) {
                Toast.makeText(requireContext(), "Por favor, complete ambas fechas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fechaInicio > fechaFinal) {
                Toast.makeText(requireContext(), "La fecha inicial no puede ser posterior a la final", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
