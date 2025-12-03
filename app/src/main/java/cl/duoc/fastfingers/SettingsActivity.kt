package cl.duoc.fastfingers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private val backgrounds = listOf(
        BackgroundOption("Simplón", 0),
        BackgroundOption("Madera", R.drawable.bg_wood),
        BackgroundOption("Ladrillos", R.drawable.bg_bricks),
        BackgroundOption("Espacio", R.drawable.bg_space)
    )

    private var currentBgIndex = 0

    override fun onCreate(SavedInstanceState: Bundle?) {
        super.onCreate(SavedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnPrev = findViewById<Button>(R.id.btnPrevBg)
        val btnNext = findViewById<Button>(R.id.btnNextBg)
        val ivPreview = findViewById<ImageView>(R.id.ivBackgroundPreview)
        val tvBgName = findViewById<TextView>(R.id.tvBgName)
        val btnSave = findViewById<Button>(R.id.btnSaveSettings)
        val btnQuitToMenu = findViewById<Button>(R.id.btnQuitToMenu)

        val fromGame = intent.getBooleanExtra("FROM_GAME", false)

        if (fromGame) {
            btnQuitToMenu.visibility = View.VISIBLE

            btnQuitToMenu.setOnClickListener {
                val intent = Intent(this, StartActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }

        val prefs = getSharedPreferences("FastFingersPrefs", Context.MODE_PRIVATE)
        currentBgIndex = prefs.getInt("BG_INDEX", 0)

        if (currentBgIndex < 0 || currentBgIndex >= backgrounds.size) currentBgIndex = 0
        updatePreview(ivPreview, tvBgName)

        btnPrev.setOnClickListener {
            if (currentBgIndex > 0) {
                currentBgIndex--
            } else {
                currentBgIndex = backgrounds.lastIndex
            }
            updatePreview(ivPreview, tvBgName)
        }

        btnNext.setOnClickListener {
            if (currentBgIndex < backgrounds.lastIndex) {
                currentBgIndex++
            } else {
                currentBgIndex = 0
            }
            updatePreview(ivPreview, tvBgName)
        }

        btnSave.setOnClickListener {
            val prefs = getSharedPreferences("FastFingersPrefs", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putInt("BG_INDEX", currentBgIndex)
            editor.apply()

            Toast.makeText(this, "Fondo seleccionado.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updatePreview(iv: ImageView, tv: TextView) {
        val bg = backgrounds[currentBgIndex]
        tv.text = bg.name

        if (bg.resId != 0) {
            iv.setImageResource(bg.resId)
        } else {
            iv.setImageResource(android.R.color.black)
        }
    }
}

data class BackgroundOption(val name: String, val resId: Int)