package cl.duoc.fastfingers

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val btnSettingsIcon = findViewById<ImageView>(R.id.btnSettingsIcon)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnInstructions = findViewById<Button>(R.id.btnInstructions)
        val btnRanking = findViewById<Button>(R.id.btnRanking)
        val btnExit = findViewById<Button>(R.id.btnExit)

        btnSettingsIcon.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnStart.setOnClickListener {
            startActivity(Intent(this, MainActivity:: class.java))
        }

        btnInstructions.setOnClickListener {
            startActivity(Intent(this, InstructionsActivity:: class.java))
        }

        btnRanking.setOnClickListener {
            startActivity(Intent(this, RankingActivity:: class.java))
        }

        btnExit.setOnClickListener {
            finishAffinity()
        }
    }
}