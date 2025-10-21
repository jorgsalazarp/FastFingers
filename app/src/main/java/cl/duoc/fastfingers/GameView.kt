package cl.duoc.fastfingers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import cl.duoc.fastfingers.model.Word

class GameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
): SurfaceView(context, attrs), SurfaceHolder.Callback {

    interface GameEventListener {
        fun onScoreUpdated(score: Int)
        fun onGameOver()
    }

    var listener: GameEventListener? = null

    private val words = Collections.synchronizedList(ArrayList<Word>())
    @Volatile
    private var running = false
    private var thread: Thread? = null
    private val rnd = Random()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 64f
    }
    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        textSize = 64f
    }
    private val paintShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(120, 0, 0, 0)
        textSize = 64f
    }

    var score = 0
        private set

    // Configurables (cambiar atributos del juego)
    private val spawnIntervalMs = 1400L
    private var lastSpawnAt = 0L
    private val baseFallSpeed = 100f
    private val penaltyPixels = 20f
    private val maxAllowedMillisForBonus = 4000L
    private val TAG = "FF/GameView"

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated - starting thread")
        running = true
        thread = Thread { gameLoop() }.also { it.start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed - starting thread")
        running = false
        try {
            thread?.join()
        } catch (e: InterruptedException) {
            // ignore
        }
    }

    fun resume() {
        // Lanza el loop si el surface ya está creado, si no, se encarga surfaceCreated
        if (!running && holder.surface.isValid) {
            surfaceCreated(holder)
        }
    }
    fun pause() { surfaceDestroyed(holder) }

    private fun gameLoop() {
        Log.d(TAG, "gameLoop started")
        var lastTime = System.currentTimeMillis()
        lastSpawnAt = lastTime
        while (running) {
            val now = System.currentTimeMillis()
            val dtMs = now - lastTime
            lastTime = now
            update(dtMs)
            draw()
            try { Thread.sleep(16) } catch (e: InterruptedException) { /*ignore*/ }
        }
        Log.d(TAG, "gameLoop ended")
    }

    private fun update (dtMs: Long) {
        val dt = dtMs / 1000f
        val h = height
        // spawn
        if (System.currentTimeMillis() - lastSpawnAt >= spawnIntervalMs) {
            spawnWord()
            lastSpawnAt = System.currentTimeMillis()
        }

        //mover palabras
        val it = words.iterator()
        while (it.hasNext()) {
            val w = it.next()
            w.y += w.speed * dt
            // game over al tocar al suelo (puede cambiarse por vidas)
            if (w.y >= h - 30) {
                running = false
                listener?.onGameOver()
            }
        }
    }

    private fun spawnWord() {
        Log.d(TAG, "spawnWord: width=$width height=$height score=$score")

        val sample = sampleWord()
        val availableWidth = max(1, width - 200)
        val x = (rnd.nextInt(availableWidth) + 20).toFloat()
        val y = -20f
        val speed = baseFallSpeed + rnd.nextInt(80)
        val w = Word(sample, x, y, speed)
        w.spawnedAt = System.currentTimeMillis()
        words.add(w)
    }

    private fun sampleWord(): String {
        val list = listOf(
            "hola", "mundo", "android", "kotlin", "zombie", "codigo", "teclado", "juego", "accion", "rapido", "puntaje", "vida"
        )
        return list [rnd.nextInt(list.size)]
    }

    private fun draw() {
        if (!holder.surface.isValid) {
            Log.d(TAG, "draw: surface not valid")
            return
        }

        val canvas: Canvas = holder.lockCanvas()
        if (canvas == null) {
            Log.w(TAG, "draw: lockCanvas returned null")
            return
        }
        try {
            canvas.drawColor(Color.BLACK)
            synchronized(words) {
                for (w in words) {
                    //sombra
                    canvas.drawText(w.text, w.x + 4, w.y + 4, paintShadow)
                    // palabra (con resalte segun el progreso de escritura)
                    if (w.progress.isNotEmpty()) {
                        val completed = w.progress
                        val remaining = w.text.substring(completed.length)
                        canvas.drawText(completed, w.x, w.y, paintProgress)
                        val offset = paintProgress.measureText(completed)
                        canvas.drawText(remaining, w.x + offset, w.y, paint)
                    } else {
                        canvas.drawText(w.text, w.x, w.y, paint)
                    }
                }
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    // --- Metodos llamados desde Activity / input ---

    // Focus en la palabra activa mas antigua (la que esté mas abajo)
    fun getActiveWord(): Word? {
        synchronized(words) {
            if (words.isEmpty()) return null
            return words.maxByOrNull { it.y }
        }
    }

    fun setUserProgressForActiveWord(progress: String) {
        val w = getActiveWord() ?: return
        w.progress = progress
    }

    fun completeActiveWord() {
        val w = getActiveWord() ?: return
        val now = System.currentTimeMillis()
        val timeTaken = now - w.spawnedAt
        // puntaje base * largo palabra
        val base = w.text.length * 10
        // puntaje bono por rapidez
        val bonus = max(0, ((maxAllowedMillisForBonus - timeTaken) / 100).toInt())
        score += base + bonus
        listener?.onScoreUpdated(score)
        // eliminar palabra cuando es escrita
        synchronized(words) { words.remove(w) }
    }

    // Penalizacion por equivocarse (aumentar cercania a gameover + reiniciar)
    fun penalizeActiveWord() {
        val w = getActiveWord() ?: return
        w.y += penaltyPixels
        //Se puede añadir vibracion por equivocación
    }

}