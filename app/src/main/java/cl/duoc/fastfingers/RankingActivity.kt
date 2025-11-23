package cl.duoc.fastfingers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.layout.Layout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cl.duoc.fastfingers.data.ScoreRepository
import cl.duoc.fastfingers.model.Score
import kotlinx.coroutines.launch

class RankingActivity : AppCompatActivity() {
    private val repository = ScoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        val rvScores = findViewById<RecyclerView>(R.id.rvScores)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val btnBack = findViewById<Button>(R.id.btnBackRanking)

        rvScores.layoutManager = LinearLayoutManager(this)

        //Carga de datos
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val scores = repository.getTopScores()
            progressBar.visibility = View.GONE

            if (scores.isNotEmpty()) {
                rvScores.adapter = RankingAdapter(scores)
            } else {
                Toast.makeText(this@RankingActivity, "No se pudo cargar el ranking", Toast.LENGTH_SHORT).show()
            }
        }
        btnBack.setOnClickListener { finish() }
    }
}

class RankingAdapter(private val list: List<Score>) : RecyclerView.Adapter<RankingAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val user: TextView = view.findViewById(R.id.tvUsername)
        val score: TextView = view.findViewById(R.id.tvScoreValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_score, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.user.text = list[position].username
        holder.score.text = list[position].score.toString()
    }

    override fun getItemCount() = list.size
}