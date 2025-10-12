package cl.duoc.fastfingers

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var input: EditText
    private lateinit var scoreView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.gameView)
        input = findViewById(R.id.input)
        scoreView = findViewById(R.id.scoreView)

        //Actualizar puntaje y game over
        gameView.listener = object : GameView.GameEventListener {
            override fun onScoreUpdated(score: Int) {
                runOnUiThread { scoreView.text = "Puntaje: $score" }
            }
            override fun onGameOver() {
                runOnUiThread {
                    scoreView.text = "Fin del juego - Puntaje final: ${gameView.score}"
                    input.isEnabled = false
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