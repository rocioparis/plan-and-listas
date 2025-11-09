package com.rociomhparis.pylfront

data class NutrientesRequest(
    val id_user: Int,
    val id_estado: Int,
    val nutrientes: List<Int>
)
