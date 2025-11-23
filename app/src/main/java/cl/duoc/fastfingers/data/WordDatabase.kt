package cl.duoc.fastfingers.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.fastfingers.model.WordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [WordEntity::class], version = 1, exportSchema = false)
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordDao() : WordDao

    companion object {
        @Volatile
        private var INSTANCE: WordDatabase? = null

        fun getDatabase(context: Context): WordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordDatabase::class.java,
                    "word_database"
                )
                    .addCallback(WordDatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
    private class WordDatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getDatabase(context).wordDao()
                dao.insertAll(PREPOPULATE_WORDS)
            }
        }
    }
}

private val PREPOPULATE_WORDS = listOf(
    "android", "kotlin", "java", "xml", "json", "api", "base", "datos", "codigo", "debug",
    "compilar", "error", "bug", "pixel", "byte", "red", "wifi", "nube", "server", "cliente",
    "movil", "app", "web", "html", "css", "script", "logica", "clase", "objeto", "metodo",
    "variable", "funcion", "ciclo", "bucle", "matriz", "lista", "vector", "pila", "cola",
    "juego", "gamer", "nivel", "score", "vida", "bonus", "boss", "play", "pause", "start",
    "game", "over", "win", "lose", "retry", "menu", "intro", "final", "etapa", "mapa",
    "perro", "gato", "leon", "tigre", "oso", "lobo", "zorro", "panda", "koala", "pez",
    "tiburon", "ballena", "delfin", "pulpo", "aguila", "buho", "pato", "pollo", "vaca", "toro",
    "casa", "mesa", "silla", "cama", "sofa", "puerta", "piso", "techo", "muro", "ventana",
    "calle", "plaza", "parque", "cine", "metro", "bus", "auto", "tren", "barco", "avion",
    "correr", "saltar", "volar", "nadar", "comer", "beber", "dormir", "soñar", "vivir", "morir",
    "amar", "odiar", "crear", "borrar", "mirar", "oir", "tocar", "sentir", "pensar", "decir",
    "rapido", "lento", "fuerte", "debil", "grande", "chico", "alto", "bajo", "nuevo", "viejo",
    "bueno", "malo", "facil", "dificil", "claro", "oscuro", "rojo", "azul", "verde", "gris",
    "uno", "dos", "tres", "mil", "cero", "tiempo", "reloj", "ahora", "luego", "nunca",
    "siempre", "quizas", "talvez", "hola", "adios", "gracias", "mundo", "gente", "persona", "amigo"
).map { WordEntity(text = it) }