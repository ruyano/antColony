import java.io.File
import kotlin.random.Random

private val fileName = "distances.txt"
private val cityAmount = 150

fun main() {
    criaDistancias()
}

fun createFile(distancias: Array<IntArray>) {
    val file = File(fileName)
    if (file.exists()) file.delete()
    file.createNewFile()

    file.bufferedWriter().use { out ->
        distancias.forEach {cityx ->
            cityx.forEach {cityY ->
                out.write("$cityY ")
            }
            out.write("\n")
        }
    }
}

fun readFromFile() : Array<IntArray>{
    val finalArray: MutableList<IntArray> = ArrayList()
    File(fileName).forEachLine { lineStr ->
        val line: MutableList<Int> = ArrayList()
        lineStr.replace("\n","")
        lineStr.split(" ").forEach { number ->
            if (number.isNotBlank()) {
                line.add(number.toInt())
            }
        }
        finalArray.add(line.toIntArray())
    }
    return finalArray.toTypedArray()
}

private fun criaDistancias() {
    val finalArray : MutableList<IntArray> = ArrayList()
    for (i in 0 until cityAmount) {
        val array : MutableList<Int> = ArrayList()
        for (j in 0 until cityAmount) {
            array.add(0)
        }
        finalArray.add(array.toIntArray())
    }

    for (i in 0 until cityAmount) {
        for (j in 0 until cityAmount) {
            val distancia = if (i == j) 0
            else Random.nextInt(20) + 1
            finalArray[i][j] = distancia
            finalArray[j][i] = distancia
        }
    }
    createFile(finalArray.toTypedArray())
}