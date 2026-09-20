package com.alejandro.c05truth

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  05.3 · Bucles: `for in`, `each` y cuándo usar cada uno
//
//  QUÉ ES
//    Groovy conserva el `for`, el `while` y el `do/while` de Java, añade `for (x in y)`
//    y, sobre todo, añade los métodos de iteración del GDK: `each`, `times`, `upto`,
//    `collect`, `findAll`… que casi siempre dicen mejor lo que se quiere hacer.
//
//  POR QUÉ IMPORTA
//    La diferencia práctica entre un bucle y un `each` no es el estilo: es que desde un
//    closure NO se puede hacer `break`. Saber eso decide cuál usar.
//
//  ERRORES COMUNES
//    · Intentar `break` dentro de un `each`.
//    · Usar `each` para construir una lista, en vez de `collect`.
//    · Modificar una colección mientras se recorre.
// =====================================================================================

class C05_03Loops {

    /**
     * Las formas de recorrer.
     */
    static void demoForms() {
        section('`for (x in y)`: la forma general')

        show('sobre una lista', recorrer(['a', 'b', 'c']))
        show('sobre un rango', sumar(1..5))
        show('sobre un mapa', recorrerMapa([a: 1, b: 2]))
        show('sobre una cadena', recorrer('abc'.toList()))

        section('El `for` clásico de Java sigue estando')

        show('for (int i = 0; …)', clasico(5))
        bullet('Útil cuando necesitas el índice y además control fino.')

        section('`each`: el equivalente del GDK')

        show('each', conEach(['a', 'b', 'c']))
        show('eachWithIndex', conIndice(['a', 'b']))
        bullet('`eachWithIndex` da el elemento y la posición.')

        section('`times`, `upto`, `downto`, `step`')

        show('3.times { }', contar(3))
        show('1.upto(4)', desdeHasta(1, 4))
        show('4.downto(1)', desdeHastaAlReves(4, 1))
        show('0.step(10, 3)', conPaso(0, 10, 3))

        section('Y el que casi siempre quieres: `collect`')

        show('each + lista a mano', conEach(['a', 'b']))
        show('collect', ['a', 'b'].collect { it.toUpperCase() })
        bullet('Si el bucle sólo sirve para construir otra lista, eso es `collect`.')
        bullet('Las operaciones de colección son el capítulo 08.')
    }

    private static List<String> recorrer(List<String> valores) {
        List<String> salida = []
        for (v in valores) salida << v.toUpperCase()
        salida
    }

    private static int sumar(IntRange rango) {
        int total = 0
        for (n in rango) total += n
        total
    }

    private static List<String> recorrerMapa(Map<String, Integer> mapa) {
        List<String> salida = []
        for (entrada in mapa) salida << "${entrada.key}=${entrada.value}"
        salida
    }

    private static List<Integer> clasico(int n) {
        List<Integer> salida = []
        for (int i = 0; i < n; i += 2) salida << i
        salida
    }

    private static List<String> conEach(List<String> valores) {
        List<String> salida = []
        valores.each { salida << it.toUpperCase() }
        salida
    }

    private static List<String> conIndice(List<String> valores) {
        List<String> salida = []
        valores.eachWithIndex { String v, int i -> salida << "$i:$v" }
        salida
    }

    private static int contar(int n) {
        int total = 0
        n.times { total += it }
        total
    }

    private static List<Integer> desdeHasta(int desde, int hasta) {
        List<Integer> salida = []
        desde.upto(hasta) { salida << (it as Integer) }
        salida
    }

    private static List<Integer> desdeHastaAlReves(int desde, int hasta) {
        List<Integer> salida = []
        desde.downto(hasta) { salida << (it as Integer) }
        salida
    }

    private static List<Integer> conPaso(int desde, int hasta, int paso) {
        List<Integer> salida = []
        desde.step(hasta, paso) { salida << (it as Integer) }
        salida
    }

    /**
     * La diferencia que decide: break.
     */
    static void demoBreakAndContinue() {
        section('En un bucle, `break` y `continue` funcionan')

        show('primer múltiplo de 7 > 20', primerMultiplo())
        show('saltándose los pares', soloImpares(1..8))

        section('En un `each`, NO hay break')

        bullet('`each` recibe un closure, y desde un closure no se puede salir del')
        bullet('método que lo llama. `break` ahí no compila.')
        bullet('`return` dentro del closure sólo termina ESA vuelta: es el `continue`.')

        show('return dentro de each = continue', returnEnEach(1..8))

        section('Qué usar en su lugar')

        show('find (para el primero que cumpla)', (1..100).find { it % 7 == 0 && it > 20 })
        show('any (¿hay alguno?)', [1, 3, 5].any { it % 2 == 0 })
        show('every (¿todos?)', [2, 4].every { it % 2 == 0 })
        show('takeWhile (hasta que falle)', [1, 2, 3, 10, 1].takeWhile { it < 5 })
        bullet('Casi todo "recorre hasta encontrar" tiene su método en el GDK.')
        bullet('Y esos SÍ cortan: `find` para en cuanto encuentra.')

        section('La regla')

        bullet('¿Necesitas cortar el recorrido? → `for` … o el método del GDK que toque.')
        bullet('¿Recorres todo y produces algo? → `collect`, `findAll`, `inject`.')
        bullet('¿Recorres todo por efecto secundario? → `each`.')
    }

    private static int primerMultiplo() {
        for (n in 1..100) {
            if (n % 7 == 0 && n > 20) return n
        }
        -1
    }

    private static List<Integer> soloImpares(IntRange rango) {
        List<Integer> salida = []
        for (n in rango) {
            if (n % 2 == 0) continue
            salida << n
        }
        salida
    }

    private static List<Integer> returnEnEach(IntRange rango) {
        List<Integer> salida = []
        rango.each {
            if (it % 2 == 0) return      // se comporta como `continue`
            salida << it
        }
        salida
    }

    /**
     * while, do/while y las etiquetas.
     */
    static void demoWhileAndLabels() {
        section('while y do/while')

        show('while', conWhile(5))
        show('do/while (se ejecuta al menos una vez)', conDoWhile(0))
        bullet('`do/while` existe en Groovy 3+. Antes había que simularlo.')

        section('Salir de bucles anidados: etiquetas')

        show('sin etiqueta (sólo sale del interno)', sinEtiqueta())
        show('con etiqueta (sale de los dos)', conEtiqueta())

        section('`continue` con etiqueta')

        show('continue externo', continueConEtiqueta())
        bullet('Salta a la siguiente vuelta del bucle EXTERNO.')

        section('Cuándo usar etiquetas')

        bullet('Casi nunca. Dos bucles anidados con etiqueta suelen pedir un método')
        bullet('aparte con un `return`, que se lee mucho mejor.')
        show('lo mismo, extrayendo un método', buscandoEnMetodo())
    }

    private static int conWhile(int limite) {
        int i = 0
        int total = 0
        while (i < limite) { total += i; i++ }
        total
    }

    private static List<Integer> conDoWhile(int limite) {
        List<Integer> salida = []
        int i = 0
        do { salida << i; i++ } while (i < limite)
        salida
    }

    private static List<String> sinEtiqueta() {
        List<String> salida = []
        for (i in 1..3) {
            for (j in 1..3) {
                if (j == 2) break
                salida << "$i$j"
            }
        }
        salida
    }

    private static List<String> conEtiqueta() {
        List<String> salida = []
        externo:
        for (i in 1..3) {
            for (j in 1..3) {
                if (i == 2) break externo
                salida << "$i$j"
            }
        }
        salida
    }

    private static List<String> continueConEtiqueta() {
        List<String> salida = []
        externo:
        for (i in 1..3) {
            for (j in 1..3) {
                if (j == 2) continue externo
                salida << "$i$j"
            }
        }
        salida
    }

    private static String buscandoEnMetodo() {
        for (i in 1..3) {
            for (j in 1..3) {
                if (i * j == 4) return "encontrado en $i,$j"
            }
        }
        'no encontrado'
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Intenta poner un `break` dentro de un `each` y lee el error del compilador.
//  2. Cambia el `return` de `returnEnEach` por nada y comprueba qué cambia.
//  3. Recorre una lista con `each` y añádele elementos dentro: mira qué excepción sale.
//  4. Reescribe `conEtiqueta()` con un método aparte y decide cuál lees mejor.
//  5. Sustituye `primerMultiplo()` por un `find` de una línea.
