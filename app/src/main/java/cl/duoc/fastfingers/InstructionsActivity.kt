package cl.duoc.fastfingers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.layout.Layout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class InstructionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val instructions = listOf (
            Instruction("¡Bienvenido!", "Fast Fingers es un juego de escribir rápido"),
            Instruction("Reglas: ", "¡Las palabras caen del cielo! ¡Escribelas para hacer que desaparezcan!"),
            Instruction("Puntaje: ", "Si escribes rápido, ganarás más puntaje"),
            Instruction("¡Cuidado!: ", "¡Perderás si una palabra cae al suelo!")
        )

        viewPager.adapter = InstructionsAdapter(instructions)

        btnBack.setOnClickListener { finish() }
    }
}

data class Instruction(val title: String, val description: String)

class InstructionsAdapter(private val list: List<Instruction>) : RecyclerView.Adapter<InstructionsAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val description: TextView = view.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_instruction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = list[position].title
        holder.description.text = list[position].description
    }

    override fun getItemCount() = list.size
}