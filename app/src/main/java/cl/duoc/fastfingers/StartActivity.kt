package cl.duoc.fastfingers

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnExit = findViewById<Button>(R.id.btnExit)

        btnStart.setOnClickListener {
            startActivity(Intent(this, MainActivity:: class.java))
        }

        btnExit.setOnClickListener {
            finishAffinity()
        }
    }
}