package com.rociomhparis.pylfront

data class RegistroResponse(
    val message: String,
    val usuario: UsuarioData
)

data class UsuarioData(
    val username: String,
    val fecha_nacimiento: String
)