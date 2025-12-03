package cl.duoc.fastfingers

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        val tvUserRankStatus = findViewById<TextView>(R.id.tvUserRankStatus)

        rvScores.layoutManager = LinearLayoutManager(this)

        //Carga de datos
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val allScores = repository.getTopScores().sortedByDescending { it.score }
            progressBar.visibility = View.GONE

            if (allScores.isNotEmpty()) {
                val top10 = allScores.take(10)
                rvScores.adapter = RankingAdapter(top10)

                val prefs = getSharedPreferences("FastFingersPrefs", Context.MODE_PRIVATE)
                val myName = prefs.getString("LAST_USERNAME", null)

                if (myName != null) {
                    val myIndex = allScores.indexOfFirst { it.username.equals(myName, ignoreCase = true ) }

                    if (myIndex != -1) {
                        val myRank = myIndex + 1
                        val myScore = allScores[myIndex].score

                        if (myRank == 1) {
                            tvUserRankStatus.text = "¡Tus dedos son los mas rapidos del oeste!"
                        } else {
                            val rival = allScores[myIndex - 1]
                            val pointsNeeded = (rival.score - myScore) + 1
                            val rivalRank = myRank - 1

                            tvUserRankStatus.text = "Estas en el puesto [$myRank].\nNecesitas [$pointsNeeded] para alcanzar al siguiente puesto."
                        }
                    } else {
                        tvUserRankStatus.text = "No estás en el ranking... Aún..."
                    }
                } else {
                    Toast.makeText(this@RankingActivity, "Aún no hay puntajes...", Toast.LENGTH_SHORT).show()
                }

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
        val rankNumber = position + 1
        holder.user.text = "$rankNumber. ${list[position].username}"
        holder.score.text = list[position].score.toString()
    }

    override fun getItemCount() = list.size
}