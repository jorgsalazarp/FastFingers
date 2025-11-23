package cl.duoc.fastfingers.data

import android.util.Log
import cl.duoc.fastfingers.api.ApiService
import cl.duoc.fastfingers.api.ScoreRequest
import cl.duoc.fastfingers.model.Score
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ScoreRepository {
    private val api: ApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ApiService::class.java)
    }

    suspend fun submitScore(username: String, score: Int): Boolean {
        return try {
            val request = ScoreRequest(username, score)
            val response = api.submitScore(request)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ScoreRepository", "Error de red al enviar puntaje", e)
            false
        }
    }

    suspend fun getTopScores(): List<Score> {
        return try {
            val response = api.getTopScores()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ScoreRepository", "Error de red al obtener ranking", e)
            emptyList()
        }
    }
}