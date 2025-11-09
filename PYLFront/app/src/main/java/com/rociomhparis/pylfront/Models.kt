package com.rociomhparis.pylfront

data class Vitaminas(
    val vitaminaA: Float,
    val betacaroteno: Float,
    val vitaminaB1: Float,
    val vitaminaB2: Float,
    val vitaminaB3: Float,
    val vitaminaB6: Float,
    val vitaminaB12: Float,
    val vitaminaC: Float,
    val vitaminaD: Float,
    val vitaminaE: Float,
    val vitaminaK: Float,
    val acidoFolico: Float
)

data class Minerales(
    val hierro: Float,
    val calcio: Float,
    val magnesio: Float,
    val zinc: Float,
    val selenio: Float,
    val fosforo: Float,
    val sodio: Float,
    val yodo: Float,
    val potasio: Float,
    val manganeso: Float,
    val boro: Float,
    val cobre: Float
)

data class Macronutrientes(
    val proteinas: Float,
    val grasasTotales: Float,
    val grasasSaturadas: Float,
    val carbohidratos: Float,
    val azucar: Float,
    val fibra: Float
)

data class Otros(
    val colesterol: Float,
    val cafeina: Float
)

data class Inputs(
    val vitaminas: Vitaminas,
    val minerales: Minerales,
    val macronutrientes: Macronutrientes,
    val otros: Otros,
    val metas_seleccionadas: List<Int>,
    val nutrientes_no_consumibles: List<Int>? = emptyList<Int>()
)

data class ResultadoMeta(
    val porcentaje: Float,
    val nivel: String
)

data class ResultadosResponse(
    val resultados: Map<Int, ResultadoMeta>
)