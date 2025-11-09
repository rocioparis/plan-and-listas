package com.rociomhparis.pylfront

data class ListaComprasResponse(
    val idListaDeCompras: Int,
    val fechaInicialLista: String,
    val fechaFinalLista: String,
    val recetas: List<RecetaConIngredientes>
)

