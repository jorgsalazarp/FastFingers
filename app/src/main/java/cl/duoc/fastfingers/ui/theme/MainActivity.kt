package cl.duoc.fastfingers.ui.theme

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cl.duoc.fastfingers.R
import cl.duoc.fastfingers.model.FallingWord
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var gameContainer: FrameLayout
    private lateinit var inputWord: EditText
    private lateinit var scoreText: TextView

    private val active = mutableListOf<ActiveWord>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val frameMs = 30L

    private val gameRunnable = object : Runnable {
        override fun run() {
            updateGame()
            mainHandler.postDelayed(this, frameMs)
        }
    }

    private val spawnHandler = Handler(Looper.getMainLooper())
    private val spawnIntervalMs = 1500L
    private val spawnRunnable = object : Runnable {
        override fun run() {
            spawnRandomWord()
            spawnHandler.postDelayed(this, spawnIntervalMs)
        }
    }

    private var score = 0
    private var lastSpawnTime = System.currentTimeMillis()

    private val words: List<String> by lazy { loadWordsFromAssestsOrDefault() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameContainer = findViewById(R.id.gameContainer)
        inputWord = findViewById(R.id.inputWord)
        scoreText = findViewById(R.id.scoreText)

        // Presionar "Done" en el teclado
        inputWord.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                ) {
                checkInputAndClear()
                true
            } else {
                false
            }
        }
    }

    override fun onResume(){
        super.onResume()
        mainHandler.post(gameRunnable)
        spawnHandler.postDelayed(spawnRunnable, 500)
    }

    override fun onPause(){
        super.onPause()
        mainHandler.removeCallbacks(gameRunnable)
        spawnHandler.removeCallbacks(spawnRunnable)
    }

    override fun onDestroy(){
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
        spawnHandler.removeCallbacksAndMessages(null)
    }

    private fun checkInputAndClear(){
        val text = inputWord.text.toString().trim()
        if (text.isEmpty()) return

        val iterator = active.iterator()
        while (iterator.hasNext()) {
            val aw = iterator.next()
            if (aw.model.text.equals(text, ignoreCase = true)) {
                val x = aw.view.x
                val y = aw.view.y

                //Remueve la palabra de la pantalla
                gameContainer.removeView(aw.view)
                iterator.remove()

                val basePoints = aw.model.text.length
                val timeTaken = System.currentTimeMillis() - lastSpawnTime
                val bonus = (100 - (timeTaken / 100)).coerceAtLeast(0).toInt()
                val points = basePoints + bonus

                score += bonus
                updateScore()

                showBonusEffect(points, x, y)

                lastSpawnTime = System.currentTimeMillis()

                break
            }
        }
        inputWord.text.clear()
    }

    private fun updateScore(){
        scoreText.text = "Puntaje: $score"
    }

    private fun updateGame(){
        val containerHeight = gameContainer.height
        if (containerHeight == 0) return

        val toRemove = mutableListOf<ActiveWord>()
        for (aw in active){
            aw.model.posY += aw.model.speed
            aw.view.y = aw.model.posY

            if (aw.model.posY + aw.view.height >= containerHeight) {
                toRemove.add(aw)
            }
        }

        for (aw in toRemove) {
            val x = aw.view.x
            val y = (aw.view.y).coerceAtMost((containerHeight - aw.view.height).toFloat())

            gameContainer.removeView(aw.view)
            active.remove(aw)

            //Penalizacion
            score -= 5
            updateScore()
            showPenaltyEffect(5, x, y)
        }
    }

    private fun spawnRandomWord() {
        if (gameContainer.width == 0) {
            gameContainer.post { spawnRandomWord() }
            return
        }
        if (words.isEmpty()) return
        val wordText = words[Random.nextInt(words.size)]
        spawnWord(wordText)
    }

    private fun spawnWord(wordText: String) {
        if (gameContainer.width == 0) {
            gameContainer.post { spawnWord(wordText) }
            return
        }

        val speed = Random.nextDouble(2.0, 8.0).toFloat()
        val model = FallingWord(text = wordText, posY = -100f, speed = speed)

        val tv = TextView(this).apply {
            text = model.text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(Color.BLACK)
            setPadding(12, 6, 12, 6)
            gravity = Gravity.CENTER
        }

        val specW = View.MeasureSpec.makeMeasureSpec(gameContainer.width, View.MeasureSpec.AT_MOST)
        val specH = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        tv.measure(specW, specH)
        val tvWidth = tv.measuredWidth
        val maxX = (gameContainer.width - tvWidth).coerceAtLeast(0)
        val xPos = Random.nextInt(0, maxX + 1).toFloat()
        tv.x = xPos
        model.posY = -tv.measuredHeight.toFloat()
        tv.y = model.posY

        gameContainer.addView(tv)
        active.add(ActiveWord(model, tv))

        lastSpawnTime = System.currentTimeMillis()
    }

    private fun showBonusEffect(bonus: Int, x: Float, y: Float) {
        val bonusView = TextView(this).apply {
            text = "+$bonus"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(Color.parseColor("FF5722"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            this.x = x
            this.y = y
        }

        gameContainer.addView(bonusView)

        val moveUp = ObjectAnimator.ofFloat(bonusView, "translationY", y, y - 150f)
        val fadeOut = ObjectAnimator.ofFloat(bonusView, "alpha", 1f, 0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(moveUp, fadeOut)
        animatorSet.duration = 1000
        animatorSet.addListener(object : AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator) {
                gameContainer.post { gameContainer.removeView(bonusView) }
            }
        })
        animatorSet.start()
    }

    private fun showPenaltyEffect(penalty: Int, x: Float, y: Float) {
        val p = TextView(this).apply {
            text = "-$penalty"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(Color.parseColor("#D32F2F"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            this.x = x
            this.y = y
        }
        gameContainer.addView(p)

        val moveUp = ObjectAnimator.ofFloat(p, "translationY", 0f, -100f)
        val fadeOut = ObjectAnimator.ofFloat(p, "alpha", 1f, 0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(moveUp, fadeOut)
        animatorSet.duration = 800
        animatorSet.addListener(object  : AnimatorListenerAdapter(){
            override fun onAnimationEnd(animation: Animator?) {
                gameContainer.post { gameContainer.removeView(p) }
            }
        })
        animatorSet.start()
    }

    private fun loadWordsFromAssetsOrDefault(): List<String> {
        return try {
            assets.open("words.txt").bufferedReader().useLines { seq ->
                seq.map { it.trim() }.filter { it.isNotEmpty() }.toList()
            }
        } catch (e: Exception) {
            // fallback
            listOf("hola", "android", "kotlin", "juego", "palabra",
                "rápido", "test", "ordenar", "codigo", "vista")
        }
    }

    private data class ActiveWord(val model: FallingWord, val view: TextView)
}

