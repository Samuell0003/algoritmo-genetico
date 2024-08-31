import kotlin.random.Random

const val NUM_DIAS = 5
const val NUM_HORARIOS = 4
const val NUM_DISCIPLINAS = 25
const val NUM_PROFESSORES = 12
const val TAMANHO_POPULACAO = 100
const val TAXA_CROSSOVER = 0.8
const val TAXA_MUTACAO = 0.1
const val NUM_GERACOES = 100

val dias = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta")
val professores = (1..NUM_PROFESSORES).map { "Prof_$it" }
val disciplinas = (1..NUM_DISCIPLINAS).map { "Disc_$it" }

data class Solucao(
    val alocacao: Array<Array<Pair<String, String>?>>,
    val avaliacao: Int
)

fun avaliarSolucao(solucao: Solucao): Int {
    var conflitos = 0
    val cargaHorariaProfessor = mutableMapOf<String, Int>()
    val disciplinasAparecidas = mutableSetOf<String>()

    for (dia in 0 until NUM_DIAS) {
        for (horario in 0 until NUM_HORARIOS) {
            val alocacao = solucao.alocacao[dia][horario]
            if (alocacao != null) {
                val disciplina = alocacao.first
                val professor = alocacao.second

                if (disciplinasAparecidas.contains(disciplina)) {
                    conflitos++
                } else {
                    disciplinasAparecidas.add(disciplina)
                }

                cargaHorariaProfessor[professor] = cargaHorariaProfessor.getOrDefault(professor, 0) + 1
            }
        }
    }


    val cargaMedia = NUM_DIAS * NUM_HORARIOS / NUM_PROFESSORES.toDouble()
    val penalizacaoCarga = cargaHorariaProfessor.values.sumOf { carga ->
        Math.abs(carga - cargaMedia)
    }

    return -(conflitos + penalizacaoCarga.toInt())
}


fun gerarSolucaoInicial(): Solucao {
    val alocacao = Array(NUM_DIAS) { arrayOfNulls<Pair<String, String>>(NUM_HORARIOS) }
    val disciplinasShuffled = disciplinas.shuffled()
    var disciplinaIndex = 0

    for (disciplina in disciplinasShuffled) {
        if (disciplinaIndex < disciplinasShuffled.size) {
            val dia = Random.nextInt(NUM_DIAS)
            val horario = Random.nextInt(NUM_HORARIOS)
            if (alocacao[dia][horario] == null) {
                val professor = professores.random()
                alocacao[dia][horario] = Pair(disciplina, professor)
            }
        }
        disciplinaIndex++
    }

    return Solucao(alocacao, avaliarSolucao(Solucao(alocacao, 0)))
}

fun crossover(solucao1: Solucao, solucao2: Solucao): Solucao {
    val novoAlocacao = Array(NUM_DIAS) { arrayOfNulls<Pair<String, String>>(NUM_HORARIOS) }
    val disciplinasUtilizadas = mutableSetOf<String>()

    for (dia in 0 until NUM_DIAS) {
        for (horario in 0 until NUM_HORARIOS) {
            val alocacao1 = solucao1.alocacao[dia][horario]
            val alocacao2 = solucao2.alocacao[dia][horario]

            val escolha = if (Random.nextDouble() < TAXA_CROSSOVER) alocacao1 else alocacao2
            if (escolha != null && !disciplinasUtilizadas.contains(escolha.first)) {
                novoAlocacao[dia][horario] = escolha
                disciplinasUtilizadas.add(escolha.first)
            }
        }
    }

    return Solucao(novoAlocacao, avaliarSolucao(Solucao(novoAlocacao, 0)))
}

fun mutar(solucao: Solucao): Solucao {
    val novaAlocacao = solucao.alocacao.map { it.copyOf() }.toTypedArray()
    val disciplinasUtilizadas = solucao.alocacao.flatten().filterNotNull().map { it.first }.toMutableSet()

    if (Random.nextDouble() < TAXA_MUTACAO) {
        val dia = Random.nextInt(NUM_DIAS)
        val horario = Random.nextInt(NUM_HORARIOS)
        val novaDisciplina = disciplinas.filter { !disciplinasUtilizadas.contains(it) }.randomOrNull()

        if (novaDisciplina != null && novaAlocacao[dia][horario] == null) {
            val novoProfessor = professores.random()
            novaAlocacao[dia][horario] = Pair(novaDisciplina, novoProfessor)
            disciplinasUtilizadas.add(novaDisciplina)
        }
    }

    return Solucao(novaAlocacao, avaliarSolucao(Solucao(novaAlocacao, 0)))
}

fun algoritmoGenetico() {
    var populacao = List(TAMANHO_POPULACAO) { gerarSolucaoInicial() }
    repeat(NUM_GERACOES) {
        populacao = populacao.sortedBy { it.avaliacao }.take(TAMANHO_POPULACAO / 2)
        val novaPopulacao = mutableListOf<Solucao>()
        while (novaPopulacao.size < TAMANHO_POPULACAO) {
            val pai1 = populacao.random()
            val pai2 = populacao.random()
            val filho = crossover(pai1, pai2)
            if (Random.nextDouble() < TAXA_MUTACAO) {
                mutar(filho)
            }
            novaPopulacao.add(filho)
        }
        populacao = populacao + novaPopulacao
    }
    val melhorSolucao = populacao.maxByOrNull { it.avaliacao }!!
    println("Melhor solução encontrada com avaliação ${melhorSolucao.avaliacao}")
    for (dia in 0 until NUM_DIAS) {
        for (horario in 0 until NUM_HORARIOS) {
            println("Dia: ${dias[dia]}, Horário: ${horario + 1}, Alocações: ${melhorSolucao.alocacao[dia][horario]}")
        }
    }
}

fun main() {
    algoritmoGenetico()
}
