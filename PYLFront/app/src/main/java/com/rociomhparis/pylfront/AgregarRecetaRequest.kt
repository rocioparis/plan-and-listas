package com.rociomhparis.pylfront

data class AgregarRecetaRequest(
    val nombre: String,
    val procedimiento: String,
    val ingredientes: List<IngredienteRequest>,
    val imagenUri: String? = null,
    val IDUser: Int? = null
)

data class IngredienteRequest(
    val nombre: String,
    val cantidad: Float?,
    val unidad: String?
)