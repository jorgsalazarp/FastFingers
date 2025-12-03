package cl.duoc.fastfingers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
    var wordProvider: (() -> String)? = null

    private val words = Collections.synchronizedList(ArrayList<Word>())
    @Volatile
    private var running = false
    private var thread: Thread? = null
    private val rnd = Random()

    private var preparedBackground: Bitmap? = null

    // LISTA DE FONDOS
    private val backgroundResources = listOf(
        0, // 0 = Default (Negro)
        R.drawable.bg_wood,
        R.drawable.bg_bricks,
        R.drawable.bg_space
    )

    // Cuenta regresiva
    private var isCountingDown = false
    private val countdownBitmaps = mutableListOf<Bitmap>()
    private var currentCountdownImage: Bitmap? = null

    // Velo oscuro (60% negro) para que el texto resalte sobre el fondo
    private val scrimPaint = Paint().apply {
        color = Color.argb(150, 0, 0, 0)
    }

    // Colores del texto
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

    private var baseFallSpeed = 100f
    private val spawnIntervalMs = 1400L
    private var lastSpawnAt = 0L
    private val penaltyPixels = 20f
    private val maxAllowedMillisForBonus = 4000L
    private val TAG = "FF/GameView"

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated")
        running = true
        thread = Thread {
            loadCountdownResources()
            runCountdownSequence()
            gameLoop() }.also { it.start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        generateFinalBackground(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        try {
            thread?.join()
        } catch (e: InterruptedException) { }
    }

    fun resume() {
        if (!running && holder.surface.isValid) {
            surfaceCreated(holder)
        }
    }
    fun pause() { surfaceDestroyed(holder) }

    // Recursos cuenta regresiva
    private fun loadCountdownResources() {
        countdownBitmaps.clear()
        try {
            countdownBitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.count3))
            countdownBitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.count2))
            countdownBitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.count1))
            countdownBitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.count_go))
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando imagenes de cuenta regresiva", e)
        }
    }

    // Logica cuenta regresiva
    private fun runCountdownSequence() {
        if (countdownBitmaps.isEmpty()) return

        isCountingDown = true
        Log.d(TAG, "Iniciando cuenta regresiva...")

        while (preparedBackground == null && running) {
            try {
                Thread.sleep(50)
            } catch (e: InterruptedException) {}
        }

        for (bitmap in countdownBitmaps) {
            if (!running) break //Corta la cuenta si el usuario se sale
            currentCountdownImage = bitmap
            draw()
            try {
                Thread.sleep(1000) // 1 Segundo
            } catch (e: InterruptedException) {
                break
            }
        }

        isCountingDown = false
        currentCountdownImage = null
        lastSpawnAt = System.currentTimeMillis()
        Log.d(TAG, "Cuenta regresiva terminada.")
    }

    private fun generateFinalBackground(w: Int, h: Int) {
        if (w <= 0 || h <= 0) return

        val prefs = context.getSharedPreferences("FastFingersPrefs", Context.MODE_PRIVATE)
        val bgIndex = prefs.getInt("BG_INDEX", 0)

        val finalBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalBmp)

        if (bgIndex > 0 && bgIndex < backgroundResources.size) {
            val resId = backgroundResources[bgIndex]
            try {
                val originalBitmap = BitmapFactory.decodeResource(resources, resId)

                // Esto asegura que la imagen llene la pantalla sin deformarse,
                // recortando lo que sobre de los lados o de arriba/abajo.
                val scale = max(w.toFloat() / originalBitmap.width, h.toFloat() / originalBitmap.height)
                val scaledWidth = originalBitmap.width * scale
                val scaledHeight = originalBitmap.height * scale
                val x = (w - scaledWidth) / 2f
                val y = (h - scaledHeight) / 2f

                val destRect = android.graphics.RectF(x, y, x + scaledWidth, y + scaledHeight)

                canvas.drawBitmap(originalBitmap, null, destRect, null)

            } catch (e: Exception) {
                Log.e(TAG, "Error generando fondo", e)
                canvas.drawColor(Color.BLACK)
            }
        } else {
            // Si es 0, fondo negro
            canvas.drawColor(Color.BLACK)
        }

        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), scrimPaint)

        // Guardar lienzo elegido
        preparedBackground = finalBmp
    }

    private fun gameLoop() {
        var lastTime = System.currentTimeMillis()
        lastSpawnAt = lastTime
        while (running) {
            if (isCountingDown) continue

            val now = System.currentTimeMillis()
            val dtMs = now - lastTime
            lastTime = now
            update(dtMs)
            draw()
            try { Thread.sleep(16) } catch (e: InterruptedException) { }
        }
    }

    private fun update (dtMs: Long) {
        if (isCountingDown) return

        val dt = dtMs / 1000f
        val h = height
        if (System.currentTimeMillis() - lastSpawnAt >= spawnIntervalMs) {
            spawnWord()
            lastSpawnAt = System.currentTimeMillis()
        }
        val it = words.iterator()
        while (it.hasNext()) {
            val w = it.next()
            w.y += w.speed * dt
            if (w.y >= h - 30) {
                running = false
                listener?.onGameOver()
            }
        }
    }

    private fun spawnWord() {
        val sample = wordProvider?.invoke() ?: "error"
        if (sample == "error") return

        val availableWidth = max(1, width - 200)
        val x = (rnd.nextInt(availableWidth) + 20).toFloat()
        val y = -20f
        val speed = baseFallSpeed + rnd.nextInt(80)

        val w = Word(sample, x, y, speed)
        w.spawnedAt = System.currentTimeMillis()
        words.add(w)
    }

    private fun draw() {
        if (!holder.surface.isValid) return
        val canvas: Canvas = holder.lockCanvas() ?: return

        try {
            if (preparedBackground != null) {
                canvas.drawBitmap(preparedBackground!!, 0f, 0f, null)
            } else {
                canvas.drawColor(Color.BLACK)
            }

            if (isCountingDown) {
                currentCountdownImage?.let { img ->
                    val x = (width - img.width) / 2f
                    val y = (height - img.height) / 2f
                    canvas.drawBitmap(img, x, y, null)
                }
            } else {
                synchronized(words) {
                    for (w in words) {
                        canvas.drawText(w.text, w.x + 4, w.y + 4, paintShadow)
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
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    // Métodos de interacción
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
        val base = w.text.length * 10
        val bonus = max(0, ((maxAllowedMillisForBonus - timeTaken) / 100).toInt())
        score += base + bonus
        listener?.onScoreUpdated(score)
        synchronized(words) { words.remove(w) }
    }

    fun penalizeActiveWord() {
        val w = getActiveWord() ?: return
        w.y += penaltyPixels
    }
}