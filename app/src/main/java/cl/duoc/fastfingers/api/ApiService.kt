package cl.duoc.fastfingers.api

import cl.duoc.fastfingers.model.Score
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    companion object{
        const val BASE_URL = "https://" //CAMBIAR VALOR DEL URL
    }

    @POST("scores")
    suspend fun submitScore(@Body request: ScoreRequest): Response<Score>

    @GET("scores")
    suspend fun getTopScores(): Response<List<Score>>
}