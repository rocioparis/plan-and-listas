package com.rociomhparis.pylfront

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val usuario: UsuarioLoginData,
    val idSesion: Int
)

data class UsuarioLoginData(
    val id: Int,
    val username: String
)