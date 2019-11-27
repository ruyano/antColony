import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.lang.Math.pow
import java.lang.System.arraycopy
import java.lang.System.out

import java.util.LinkedHashMap
import java.util.Random
import javax.swing.JFrame

object CaixeiroViajanteFormigas {
    private var numCidades = 150
    private val numFormigas = 1
    private val epoch = 50
    private val alpha = 3.0 // peso do feromonio
    private val beta = 2.0 // peso da distancia
    private val reducaoFeromonio = 0.1 // 10% de perda de
    private val aumentoFeromonio = 1.0 // 200% de ganho de
    private var distancias: Array<DoubleArray>? = null
    private var formigas: Array<IntArray>? = null
    private var feromonios: Array<DoubleArray>? = null
    private var melhorRota: IntArray? = null
    private var melhorDistancia: Double = 0.0
    private val random = Random(System.currentTimeMillis())
    private var resultados: MutableMap<Int, Double>? = null

    @JvmStatic
    fun main(args: Array<String>) {
        readDistanciasFromFile()
        imprimeTabelaDistancias()
        distribuiFormigas()
        inicializaFeromonios()
        for (i in 0 until epoch) {
            atualizaFeromonios()
            if (i > 0) {
                atualizaRotasFormigas()
            }
            melhorRota()
            printMelhorRota(i)
            armazenaResultados(i)
        }
        apresentaGrafico()
    }

    fun printMelhorRota(epoca: Int) {
        println()
        print("Melhor rota da epoca $epoca = ")
        melhorRota?.forEach {
            print("$it ")
        }
        print(" distancia = $melhorDistancia")
    }

    private fun readDistanciasFromFile() {
        distancias = readFromFile()
        numCidades = distancias!!.size
    }

    private fun distribuiFormigas() {
        formigas = Array(numFormigas) { IntArray(numCidades + 1) }
        for (i in 0 until numFormigas) {
            for (j in 0 until numCidades) {
                formigas!![i][j] = j
            }
        }
        out.printf(formigas!!.toString())
        for (i in 0 until numFormigas) {
            for (j in 0 until numCidades) {
                val dest = random.nextInt(numCidades)
                if (dest != j) {
                    val aux = formigas!![i][j]
                    formigas!![i][j] = formigas!![i][dest]
                    formigas!![i][dest] = aux
                }
            }
            formigas!![i][numCidades] = formigas!![i][0] // volta para o inicio
        }
        out.printf(formigas!!.toString())
    }

    private fun distanciaPercorrida(cidades: IntArray): Double {
        var ret = 0.0
        for (i in 0 until cidades.size - 1) {
            ret += distancias!![cidades[i]][cidades[i + 1]]
        }
        return ret
    }

    private fun melhorRota() {
        for (i in 0 until numFormigas) {
            val dist = distanciaPercorrida(formigas!![i])
            if (melhorDistancia == 0.0 || dist < melhorDistancia) {
                if (melhorRota == null) {
                    melhorRota = IntArray(numCidades + 1)
                }
                melhorDistancia = dist
                arraycopy(formigas!![i], 0, melhorRota!!, 0, formigas!![i].size)
            }
        }
    }

    private fun inicializaFeromonios() {
        feromonios = Array(numCidades) { DoubleArray(numCidades) }
        for (i in 0 until numCidades) {
            for (j in 0 until numCidades) {
                feromonios!![i][j] = 0.01
            }
        }
    }

    private fun atualizaRotasFormigas() {
        for (i in 0 until numFormigas) {
            // cidade atual da formiga
            // int cidade = formigas[i][numCidades - 1];
            val cidade = random.nextInt(numCidades)
            formigas?.set(i, geraNovaRota(cidade))
        }
    }

    private fun geraNovaRota(inicio: Int): IntArray {
        val visitadas = BooleanArray(numCidades)
        val novaRota = IntArray(numCidades + 1)
        novaRota[0] = inicio
        visitadas[inicio] = true
        for (i in 0 until numCidades - 1) {
            val origem = novaRota[i]
            val destino = proximaCidade(origem, visitadas)
            novaRota[i + 1] = destino
            visitadas[destino] = true
        }
        novaRota[numCidades] = novaRota[0]
        return novaRota
    }

    private fun proximaCidade(origem: Int, visitadas: BooleanArray): Int {
        val aux = DoubleArray(numCidades)
        var soma = 0.0
        for (i in 0 until numCidades) {
            if (i != origem && !visitadas[i]) {
                aux[i] = pow(feromonios!![origem][i], alpha) * pow(0.1 / distancias!![origem][i], beta)
                soma += aux[i]
            }
        }
        for (i in 0 until numCidades) {
            aux[i] /= soma
        }
        val acum = DoubleArray(numCidades + 1)
        for (i in 0 until numCidades) {
            acum[i + 1] = acum[i] + aux[i]
        }
        val p = random.nextDouble()

        for (i in 0 until acum.size - 1) {
            if (p >= acum[i] && p < acum[i + 1]) {
                return i
            }
        }
        return 0
    }

    private fun atualizaFeromonios() {
        for (i in 0 until numCidades) {
            for (j in 0 until numCidades) {
                if (j == i) {
                    continue
                }
                for (f in 0 until numFormigas) {
                    val dist = distanciaPercorrida(formigas!![f])
                    val reducao = feromonios!![i][j] * (1 - reducaoFeromonio)
                    var aumento = 0.0
                    if (formigaFezCaminho(f, i, j)) {
                        aumento = aumentoFeromonio / dist
                    }
                    feromonios!![i][j] = reducao + aumento
                    if (feromonios!![i][j] < 0.0001) {
                        feromonios!![i][j] = 0.0001
                    }
                }
            }
        }
    }

    private fun formigaFezCaminho(formiga: Int, origem: Int, destino: Int): Boolean {
        if (origem == destino) {
            return false
        }
        for (i in 0 until numCidades + 1) {
            if (origem == formigas!![formiga][i]) {
                if (i == 0 && formigas!![formiga][i + 1] == destino) {
                    return true
                } else if (i == numCidades - 1 && formigas!![formiga][i - 1] == destino) {
                    return true
                } else if (i > 0 && i < numCidades - 1) {
                    if (formigas!![formiga][i - 1] == destino || formigas!![formiga][i + 1] == destino) {
                        return true
                    }
                }
                return false
            }
        }
        return false
    }

    private fun armazenaResultados(numViagem: Int) {
        if (resultados == null) {
            resultados = LinkedHashMap()
        }
        resultados!![numViagem + 1] = distanciaPercorrida(melhorRota!!)
    }

    private fun imprimeTabelaDistancias() {
        println("Distancias:")
        print("----")
        for (i in 0 until numCidades) {
            print("---")
        }
        println()
        print("  - ")
        for (i in 0 until numCidades) {
            print(" $i|")
        }
        println()
        for (i in 0 until numCidades) {
            print("$i - ")
            for (j in 0 until numCidades) {
                val dist = distancias!![i][j]
                if (dist < 10) {
                    print("0$dist|")
                } else {
                    print("$dist|")
                }
            }
            println()
        }
        print("----")
        for (i in 0 until numCidades) {
            print("---")
        }
        println()
    }

    private fun apresentaGrafico() {
        val jf = JFrame("Epoca X melhor distância")
        jf.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        val xy = XYSeries("Melhor distância")
        for (v in resultados!!.keys) {
            xy.add(v, resultados!![v])
        }
        val col = XYSeriesCollection(xy)
        val jfc = ChartFactory.createXYLineChart("Epoca X melhor distância", "Epoca", "Distância", col, PlotOrientation.VERTICAL,
                true, true, false)
        val cp = ChartPanel(jfc)
        jf.add(cp)
        jf.pack()
        jf.setLocationRelativeTo(null)
        jf.isVisible = true
    }

}
