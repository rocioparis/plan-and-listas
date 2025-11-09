package com.rociomhparis.pylfront

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Receta(
    val id: Int,
    val nombre: String,
    val imagen_url: String,
    val ingredientes: List<Ingrediente>,
    val procedimiento: String,
) : Parcelable

@Parcelize
data class Ingrediente(
    val idIngrediente: Int,
    val nombre: String?,
    val cantidad: Double?,
    val unidad: String?,
    val disponibleItem: Boolean?
) : Parcelable