package cl.duoc.fastfingers

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.fastfingers.data.ScoreRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("cl.duoc.fastfingers", appContext.packageName)
    }

    @Test
    fun testSubmitScoreToApi() = runBlocking {
        val repository = ScoreRepository()
        val testUser = "Usuario_Test"
        val testScore = 100
        val success = repository.submitScore(testUser, testScore)

        assertTrue("El envío del puntaje falló. ", success)
    }

    @Test
    fun testGetRankingFromApi() = runBlocking {
        val repository = ScoreRepository()
        val ranking = repository.getTopScores()
        assertNotNull("La lista de ranking no debería de ser nula", ranking)
    }
}