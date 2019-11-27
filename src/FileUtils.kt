import java.io.File
import kotlin.random.Random

private val fileName = "distances.txt"
private val cityAmount = 150

fun main() {
    criaDistancias()
}

fun createFile(distancias: Array<DoubleArray>) {
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

fun readFromFile() : Array<DoubleArray>{
    val finalArray: MutableList<DoubleArray> = ArrayList()
    File(fileName).forEachLine { lineStr ->
        val line: MutableList<Double> = ArrayList()
        lineStr.replace("\n","")
        lineStr.split(" ").forEach { number ->
            if (number.isNotBlank()) {
                line.add(number.toDouble())
            }
        }
        finalArray.add(line.toDoubleArray())
    }
    return finalArray.toTypedArray()
}

private fun criaDistancias() {
    val finalArray : MutableList<DoubleArray> = ArrayList()
    for (i in 0 until cityAmount) {
        val array : MutableList<Double> = ArrayList()
        for (j in 0 until cityAmount) {
            array.add(0.0)
        }
        finalArray.add(array.toDoubleArray())
    }

    for (i in 0 until cityAmount) {
        for (j in 0 until cityAmount) {
            val distancia = if (i == j) 0.0
            else Random.nextDouble(20.0) + 1
            finalArray[i][j] = distancia
            finalArray[j][i] = distancia
        }
    }
    createFile(finalArray.toTypedArray())
}