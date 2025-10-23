package cl.duoc.fastfingers

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.util.Log

class MainActivity : AppCompatActivity() {
    private val TAG = "FF/MainActivity"

    private lateinit var gameView: GameView
    private lateinit var input: EditText
    private lateinit var scoreView: TextView

    private var gameOverOverlay: View? = null
    private var tvFinalScore: TextView? = null
    private var btnRestart: Button? = null
    private var btnExitGame: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: start")
        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.gameView)
        input = findViewById(R.id.input)
        scoreView = findViewById(R.id.scoreView)
        Log.d(TAG, "Views inicializadas: gameView=${R.id.gameView} input=${R.id.input} scoreView=${R.id.scoreView}")

        gameOverOverlay = findViewById(R.id.gameOverOverlay)
        tvFinalScore = findViewById(R.id.tvFinalScore)
        btnRestart = findViewById(R.id.btnRestart)
        btnExitGame = findViewById(R.id.btnExitGame)

        if (gameOverOverlay == null) Log.w(TAG, "gameOverOverlay NO encontrado en layout (null)")
        if (tvFinalScore == null) Log.w(TAG, "tvFinalScore NO encontrado en layout (null)")
        if (btnRestart == null) Log.w(TAG, "btnRestart NO encontrado en layout (null)")
        if (btnExitGame == null) Log.w(TAG, "btnExitGame NO encontrado en layout (null)")

        gameView = findViewById(R.id.gameView)
        input = findViewById(R.id.input)
        scoreView = findViewById(R.id.scoreView)

        Log.d(TAG, "Views inicializadas: gameView=${R.id.gameView} input=${R.id.input}")

        //Actualizar puntaje y game over
        gameView.listener = object : GameView.GameEventListener {
            override fun onScoreUpdated(score: Int) {
                runOnUiThread { scoreView.text = "Puntaje: $score" }
            }
            override fun onGameOver() {
                runOnUiThread {
                    tvFinalScore?.text = "Puntaje final: ${gameView.score}"
                    input.isEnabled = false
                    gameOverOverlay?.visibility = View.VISIBLE
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

        btnRestart?.setOnClickListener {
            recreate()
        }
        btnExitGame?.setOnClickListener {
            finishAffinity()
        }

    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

}