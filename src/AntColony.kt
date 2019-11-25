import kotlin.math.pow
import kotlin.random.Random

val numeroCidades = 10
val numeroFormigas = 5
val epocas = 20
val taxaReducaoFeromonio = 0.1 // 10%
val taxaAumentoFeromonio = 2.0 // 200%
val alpha = 3.0 // peso do feromonio
val beta = 2.0 // peso da distancia

var distancias =  Array(numeroCidades) { IntArray(numeroCidades) }
var feromonios =  Array(numeroCidades) { DoubleArray(numeroCidades) }
var formigas = Array(numeroFormigas) { IntArray(numeroCidades + 1) }
var melhorDistancia = 0
val melhorRota = IntArray(numeroCidades + 1)
val resultados = mutableMapOf<Int, Int>()

fun main() {
    println("Distancias iniciais: ")
    inicializaDistancias()
    printDistancias()
//    println("Formigas iniciais: ")
    inicializaFormigas()
//    printFormigas()
//    println("Feromonios iniciais: ")
    inicializaFeromonios()
//    printFeromonios()
    for (i in 0 until epocas) {
//        println("Inicio da epoca: $i")
        atualizaFeromonios()
//        printFeromonios()
        if (i > 0) {
            atualizaRotas()
        }
        melhorRota()

//        println("Fim da epoca: $i")
        printMelhorRota(i)
        salvaMelhorResultado(i)
    }

    resultados.forEach {
        println()
        print("${it.key} - ${it.value}")
    }

}

fun printMelhorRota(epoca: Int) {
    println()
    print("Melhor rota da epoca $epoca = ")
    melhorRota.forEach {
        print("$it ")
    }
}

fun salvaMelhorResultado(epoca: Int) {
    resultados[epoca] = distanciaPercorrida(melhorRota)
}

fun melhorRota() {
    for (i in 0 until numeroFormigas) {
        val distancia = distanciaPercorrida(formigas[i])
        if (melhorDistancia == 0 || distancia < melhorDistancia) {
                melhorDistancia = distancia
                System.arraycopy(formigas[i], 0, melhorRota, 0, formigas[i].size)
        }
    }
}

fun atualizaRotas() {
    for (i in 0 until numeroFormigas) {
        val cidadeInicial = Random.nextInt(numeroCidades)
        formigas[i] = gerarNovaRota(cidadeInicial)
    }
}

fun gerarNovaRota(cidadeInicial: Int): IntArray {
    val visitadas = BooleanArray(numeroCidades)
    val novaRota = IntArray(numeroCidades + 1)
    novaRota[0] = cidadeInicial
    visitadas[cidadeInicial] = true
    for (i in 0 until numeroCidades -1) {
        val origem = novaRota[i]
        val destino = geraDestinoPara(origem, visitadas)
        novaRota[i+1] = destino
        visitadas[destino] = true
    }
    novaRota[numeroCidades] = novaRota[0]
    return novaRota
}

fun geraDestinoPara(origem: Int, visitadas: BooleanArray): Int {
    val aux = DoubleArray(numeroCidades)
    var soma = 0.0
    for (i in 0 until numeroCidades) {
        if (i != origem && !visitadas[i]) {
            aux[i] = feromonios[origem][i].pow(alpha) * (0.1 / distancias[origem][i]).pow(beta)
            soma += aux[i]
        }
    }
    for (i in 0 until numeroCidades) {
        aux[i] /= soma
    }
    val acumulado = DoubleArray(numeroCidades + 1)
    for (i in 0 until numeroCidades) {
        acumulado[i+1] = acumulado[i] + aux[i]
    }
    val p = Random.nextDouble()
    for (i in 0 until acumulado.size -1) {
        if (p >= acumulado[i] && p < acumulado[i+1]) {
            return i
        }
    }
    return 0
}

fun atualizaFeromonios() {
    for (cidadeOrigem in 0 until numeroCidades) {
        for (cidadeDestino in 0 until numeroCidades) {
            if (cidadeOrigem == cidadeDestino) {
                continue
            }
            for (formiga in 0 until numeroFormigas) {
                val distancia =  distanciaPercorrida(formigas[formiga])
                val feromonioReduzido = feromonios[cidadeOrigem][cidadeDestino] * (1-taxaReducaoFeromonio)
                var aumento = 0.0
                if (formigaFezCaminho(formiga, cidadeOrigem, cidadeDestino)) {
                    aumento = taxaAumentoFeromonio/distancia
                }
                feromonios[cidadeOrigem][cidadeDestino] = feromonioReduzido + aumento
                if (feromonios[cidadeOrigem][cidadeDestino] < 0.0001) {
                    feromonios[cidadeOrigem][cidadeDestino] = 0.0001
                }
            }
        }
    }
}


fun formigaFezCaminho(formiga: Int, origem: Int, destino: Int): Boolean {
    if (origem == destino) return false
    for (i in 0 until numeroCidades) {
        if (origem == formigas[formiga][i]) {
            if (i == 0 && formigas[formiga][i+1] == destino) {
                return true
            } else if (i == numeroCidades-1 && formigas[formiga][i-1] == destino) {
                return true
            } else if (i > 0 && i < numeroCidades -1) {
                if (formigas[formiga][i-1] == destino || formigas[formiga][i+1] == destino) {
                    return true;
                }
            }
            return false
        }
    }
    return false
}

fun distanciaPercorrida(formiga: IntArray): Int {
    var distancia = 0
    for (i in 0 until formiga.size - 1) {
        distancia += distancias[formiga[i]][formiga[i+1]]
    }
    return distancia
}

fun inicializaDistancias() {
    for (i in 0 until numeroCidades) {
        for (j in 0 until numeroCidades) {
            var dist = 0
            if (i != j) {
                dist = Random.nextInt(20)
            }
            distancias[i][j] = dist
            distancias[j][i] = dist
        }
    }
}

fun inicializaFormigas() {
    formigas.forEach {formiga ->
        for (i in 0 until numeroCidades) {
            formiga[i] = i
        }
    }

    formigas.forEach { formiga ->
        for (i in 0 until numeroCidades) {
            val destino = Random.nextInt(numeroCidades)
            if (destino != i) {
                val aux = formiga[i]
                formiga[i] = formiga[destino]
                formiga[destino] = aux
            }
        }
        formiga[numeroCidades] = formiga[0]
    }
}

fun inicializaFeromonios() {
    for (i in 0 until numeroCidades) {
        for (j in 0 until numeroCidades) {
            feromonios[i][j] = 0.01
        }
    }
}

fun printDistancias() {
    distancias.forEach { array ->
        array.forEach { value ->
            if (value < 10) {
                print("0$value ")
            } else {
                print("$value ")
            }
        }
        println()
    }
}

fun printFormigas() {
    formigas.forEach { array ->
        array.forEach { value ->
            if (value < 10) {
                print("0$value ")
            } else {
                print("$value ")
            }
        }
        println()
    }
}

fun printFeromonios() {
    feromonios.forEach { array ->
        array.forEach { value ->
            print("$value ")
        }
        println()
    }
}