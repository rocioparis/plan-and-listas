package com.rociomhparis.pylfront

data class PlanificacionResponse(
    val mensaje: String,
    val idReceta: Int,
    val coherencia: Map<Int, MetaCoherencia>
)

data class MetaCoherencia(
    val nombre: String,
    val porcentaje: String,
    val nivel: String,
    val botones: List<String>
)

data class ConfirmacionResponse(
    val mensaje: String,
    val idPlanificacion: Int,
    val idReceta: Int,
    val comensales: Int
)