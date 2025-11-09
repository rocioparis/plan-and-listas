package com.rociomhparis.pylfront

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class RecetaConIngredientes(
    val id: Int,
    val nombre: String,
    val ingredientes: List<Ingrediente>
) : Parcelable
