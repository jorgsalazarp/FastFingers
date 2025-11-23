package cl.duoc.fastfingers.data

import android.util.Log
import cl.duoc.fastfingers.api.ApiService
import cl.duoc.fastfingers.api.ScoreRequest
import cl.duoc.fastfingers.model.Score
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ScoreRepository {
    private val api: ApiService? by lazy {
        try {
            Retrofit.Builder()
                .baseUrl(ApiService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        } catch (e: Exception) {
            Log.e("ScoreRepository", "Error al inicializar Retrofit: Revisa la URL", e)
            null
        }
    }

    suspend fun submitScore(username: String, score: Int): Boolean {
        val service = api ?: return false

        return try {
            val request = ScoreRequest(username, score)
            val response = service.submitScore(request)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ScoreRepository", "Error de red al enviar puntaje", e)
            false
        }
    }

    suspend fun getTopScores(): List<Score> {
        val service = api ?: return emptyList()

        return try {
            val response = service.getTopScores()
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