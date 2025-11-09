package com.rociomhparis.pylfront

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @Multipart
    @POST("usuarios/registrar")
    suspend fun registrarUsuario(
        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody,
        @Part("password_conf") passwordConf: RequestBody,
        @Part("fechaNacimiento") fechaNacimiento: RequestBody
    ): Response<RegistroResponse>

    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("logout/{id_user}")
    suspend fun cerrarSesion(
        @Path("id_user") idUser: Int
    ): Response<LogoutResponse>

    @POST("guardar_cuestionario")
    suspend fun guardarCuestionario(
        @Body request: CuestionarioRequest
    ): Response<CuestionarioResponse>

    @POST("guardar_metas")
    suspend fun guardarMetas(
        @Body request: MetasRequest
    ): Response<CuestionarioResponse>

    @POST("guardar_nutrientes")
    suspend fun guardarNutrientes(
        @Body request: NutrientesRequest
    ): Response<CuestionarioResponse>

    @GET("recetas/recomendadas/{id_user}")
    suspend fun obtenerRecetasRecomendadas(
        @Path("id_user") idUser: Int
    ): Response<List<Receta>>

    @GET("buscar_recetas/")
    suspend fun buscarRecetas(
        @Query("id_user") idUser: Int?,
        @Query("query") query: String
    ): Response<List<Receta>>

    @GET("detalle_receta/{id_receta}")
    suspend fun obtenerRecetaDetalle(
        @Path("id_receta") idReceta: Int
    ): Response<Receta>

    @POST("/plan_comida/agregar")
    suspend fun agregarRecetaAlPlan(
        @Query("idReceta") idReceta: Int,
        @Query("idUser") idUser: Int,
        @Query("fecha") fecha: String
    ): Response<PlanificacionResponse>

    @POST("/plan_comida/confirmar")
    suspend fun confirmarPlanificacion(
        @Query("idReceta") idReceta: Int,
        @Query("idUser") idUser: Int,
        @Query("fecha") fecha: String,
        @Query("comensales") comensales: Int,
        @Query("tipoComida") tipoComida: String
    ): Response<ConfirmacionResponse>

    @GET("/plan_comida/por_fecha")
    suspend fun obtenerComidasPlanificadas(
        @Query("idUser") idUser: Int,
        @Query("fecha") fecha: String
    ): Response<List<ComidaPlanificada>>

    @DELETE("/plan_comida/eliminar")
    suspend fun eliminarComidaPlanificada(
        @Query("idComida") idComida: Int,
        @Query("idUser") idUser: Int,
        @Query("fecha") fecha: String
    ): Response<Any>

    @GET("/lista_compras/obtener")
    suspend fun obtenerListaCompras(
        @Query("idUser") idUser: Int,
        @Query("fechaInicio") fechaInicio: String,
        @Query("fechaFinal") fechaFinal: String
    ): Response<ListaComprasResponse>

    @GET("lista_compras/usuario")
    suspend fun obtenerListasUsuario(
        @Query("idUser") idUser: Int
    ): Response<List<ListaComprasResumen>>

    @GET("lista_compras/{idLista}")
    suspend fun obtenerListaPorId(
        @Path("idLista") idLista: Int
    ): Response<ListaComprasResponse>

    @POST("recetas/agregar")
    suspend fun agregarReceta(
        @Body request: AgregarRecetaRequest
    ): Response<Receta>

    @GET("recetas/recetas_por_user/{IDUser}")
    suspend fun obtenerRecetas(@Path("IDUser") IDUser: Int): Response<List<RecetaOut>>

    @GET("unidades")
    suspend fun obtenerUnidades(): Response<List<UnidadResponse>>
    fun baseUrl(): String

    @PUT("lista_compras/listas/{idLista}/ingrediente/{idIngrediente}/estado")
    suspend fun actualizarEstadoIngrediente(
        @Path("idLista") idLista: Int,
        @Path("idIngrediente") idIngrediente: Int,
        @Query("disponible") disponible: Boolean
    ): Response<Void>
}