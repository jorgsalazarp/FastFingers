package cl.duoc.fastfingers.model

data class Word(
    val text: String,
    var x: Float,
    var y: Float,
    var speed: Float
) {
    var spawnedAt: Long = System.currentTimeMillis()

    //Marca el progreso escrit en la pabalabra
    var progress: String = ""
}