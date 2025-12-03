package cl.duoc.fastfingers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import cl.duoc.fastfingers.data.ScoreRepository
import cl.duoc.fastfingers.data.WordDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    private val TAG = "FF/MainActivity"

    private lateinit var gameView: GameView
    private lateinit var input: EditText
    private lateinit var scoreView: TextView

    private var gameOverOverlay: View? = null
    private var tvFinalScore: TextView? = null
    private var etUsername: EditText? = null
    private var btnSaveScore: Button? = null
    private var btnRestart: Button? = null
    private var btnGoToRanking: Button? = null
    private var btnExitGame: Button? = null

    private val scoreRepository = ScoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val wordDao = WordDatabase.getDatabase(applicationContext).wordDao()

        Log.d(TAG, "onCreate: start")
        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.gameView)
        input = findViewById(R.id.input)
        scoreView = findViewById(R.id.scoreView)
        Log.d(TAG, "Views inicializadas: gameView=${R.id.gameView} input=${R.id.input} scoreView=${R.id.scoreView}")

        gameOverOverlay = findViewById(R.id.gameOverOverlay)
        tvFinalScore = findViewById(R.id.tvFinalScore)
        etUsername = findViewById(R.id.etUsername)
        btnSaveScore = findViewById(R.id.btnSaveScore)
        btnGoToRanking = findViewById(R.id.btnGoToRanking)
        btnRestart = findViewById(R.id.btnRestart)
        btnExitGame = findViewById(R.id.btnExitGame)

        if (gameOverOverlay == null) Log.w(TAG, "gameOverOverlay NO encontrado en layout (null)")
        if (tvFinalScore == null) Log.w(TAG, "tvFinalScore NO encontrado en layout (null)")
        if (btnRestart == null) Log.w(TAG, "btnRestart NO encontrado en layout (null)")
        if (btnExitGame == null) Log.w(TAG, "btnExitGame NO encontrado en layout (null)")

        gameView.wordProvider = {
            runBlocking {
                wordDao.getRandomWord() ?: "error"
            }
        }

        //Actualizar puntaje y game over
        gameView.listener = object : GameView.GameEventListener {
            override fun onScoreUpdated(score: Int) {
                runOnUiThread { scoreView.text = "Puntaje: $score" }
            }
            override fun onGameOver() {
                runOnUiThread {
                    tvFinalScore?.text = "Puntaje final: ${gameView.score}"
                    input.isEnabled = false

                    hideKeyboard()

                    gameOverOverlay?.visibility = View.VISIBLE
                    etUsername?.text?.clear()
                    btnSaveScore?.isEnabled = true
                }
            }
        }

        input.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) { /*no-op*/ }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /*no-op*/ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val typed = s?.toString() ?: ""
                val active = gameView.getActiveWord()
                if (active == null) return

                // escribir = prefijo => ok (progreso)
                if (active.text.startsWith(typed)) {
                    // marcar progreso en la palabra
                    gameView.setUserProgressForActiveWord(typed)
                    // por si completa la palabra
                    if (typed == active.text) {
                        gameView.completeActiveWord()
                        input.text?.clear()
                    }
                } else {
                    // por si el usuario se equivoca (penalizacion y reinicar palabra)
                    gameView.penalizeActiveWord()
                    input.text?.clear()
                }
            }
        })

        //Boton para guardar puntaje
        btnSaveScore?.setOnClickListener {
            val name = etUsername?.text.toString().trim()
            val score = gameView.score

            if (name.isEmpty()) {
                Toast.makeText(this, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Deshabilita para evitar que se envie 2 veces
            btnSaveScore?.isEnabled = false
            Toast.makeText(this, "Enviando puntaje...", Toast.LENGTH_SHORT).show()

            //Couroutines con red
            CoroutineScope(Dispatchers.IO).launch {
                val success = scoreRepository.submitScore(name, score)
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this@MainActivity, "¡Puntaje guardado!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Error al guardar... Es culpa del internet, no mía...", Toast.LENGTH_SHORT).show()
                        btnSaveScore?.isEnabled = true //Permite intenter de nuevo en caso de error
                    }
                }
            }
        }

        btnGoToRanking?.setOnClickListener {
            startActivity(Intent(this, RankingActivity::class.java))
            finish()
        }

        //boton de reinciar
        btnRestart?.setOnClickListener { recreate() }
        //boton para salir del juego
        btnExitGame?.setOnClickListener { finish() }

        val btnGameSettings = findViewById<ImageView>(R.id.btnGameSettings)

        btnGameSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("FROM_GAME", true)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
        showKeyboard()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    private fun showKeyboard() {
        input.requestFocus()
        input.postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}