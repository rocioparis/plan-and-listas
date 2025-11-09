package com.rociomhparis.pylfront

import com.google.gson.annotations.SerializedName

sealed class ListaItem {
    data class Header(val titulo: String) : ListaItem()
    data class Ingrediente(
        @SerializedName("IDIngrediente")
        val idIngrediente: Int? = null,
        val nombre: String,
        val cantidad: String,
        val unidad: String,
        var disponible: Boolean? = null
    ) : ListaItem()
}