package com.alejandro.c03strings

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.quoted
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  03.3 · Lo que el GDK le añade a String
//
//  QUÉ ES
//    Groovy no cambia `java.lang.String`: le añade métodos desde fuera, a través del
//    GDK (capítulo 20). Son decenas, y tapan casi todos los huecos de la API de Java.
//
//  POR QUÉ IMPORTA
//    Media librería de utilidades de texto de un proyecto Java sobra en Groovy. Saber
//    qué hay evita reescribir `capitalize`, `padLeft`, `stripIndent` o `tokenize`.
//
//  ERRORES COMUNES
//    · Buscar estos métodos en la documentación de Java: no están ahí, están en el GDK.
//    · Confundir `split` (expresión regular, devuelve array) con `tokenize`
//      (caracteres, devuelve lista).
//    · Usar `+` en un bucle para construir texto en vez de un StringBuilder o `join`.
// =====================================================================================

class C03_03StringGdk {

    /**
     * Indexar y trocear.
     */
    static void demoIndexingAndSlicing() {
        section('Una cadena se indexa como una lista')

        def texto = 'Groovy'
        show('texto[0]', quoted(texto[0]))
        show('texto[-1] (desde el final)', quoted(texto[-1]))
        show('texto[0..2] (rango)', quoted(texto[0..2]))
        show('texto[-3..-1]', quoted(texto[-3..-1]))
        show('texto[0, 2, 4]', quoted(texto[0, 2, 4]))
        bullet('Los índices negativos cuentan desde el final. En Java no existen.')

        section('Rango invertido: la cadena sale al revés')

        show('texto[2..0]', quoted(texto[2..0]))
        bullet('Sorprende la primera vez. Es coherente con los rangos (capítulo 09).')

        section('Trocear')

        def frase = 'uno,dos,,tres'
        show('split(",")', frase.split(','))
        show('tokenize(",")', frase.tokenize(','))
        bullet('`split` usa una EXPRESIÓN REGULAR y conserva los vacíos; devuelve array.')
        bullet('`tokenize` usa los caracteres como separadores, se come los vacíos y')
        bullet('devuelve una LISTA. Para texto suelto, casi siempre quieres tokenize.')

        show('"a.b".split(".")', 'a.b'.split('.'))
        bullet('Ahí está la trampa: `.` como expresión regular es "cualquier carácter".')
        show('"a.b".split("\\\\.")', 'a.b'.split('\\.'))
        show('"a.b".tokenize(".")', 'a.b'.tokenize('.'))
    }

    /**
     * Transformar.
     */
    static void demoTransforming() {
        section('Lo que Java no trae')

        show("'groovy'.capitalize()", 'groovy'.capitalize())
        show("'Groovy'.uncapitalize()", 'Groovy'.uncapitalize())
        show("'groovy'.reverse()", 'groovy'.reverse())
        show("'ab' * 3", 'ab' * 3)
        show("'groovy'.center(12, '-')", quoted('groovy'.center(12, '-')))
        show("'7'.padLeft(3, '0')", quoted('7'.padLeft(3, '0')))
        show("'x'.padRight(4, '.')", quoted('x'.padRight(4, '.')))

        section('Quitar cosas')

        show("'  hola  '.trim()", quoted('  hola  '.trim()))
        show("'xxhola'.replaceFirst('x', '')", quoted('xxhola'.replaceFirst('x', '')))
        show("'hola.txt' - '.txt'", quoted('hola.txt' - '.txt'))
        bullet('El operador `-` sobre cadenas quita la PRIMERA aparición. Capítulo 04.')

        section('Preguntar')

        show("'42'.isInteger()", '42'.isInteger())
        show("'3.5'.isNumber()", '3.5'.isNumber())
        show("''.isEmpty()", ''.isEmpty())
        show("'  '.isAllWhitespace()", '  '.isAllWhitespace())
        show("'abc'.contains('b')", 'abc'.contains('b'))

        section('Recorrer')

        show('each sobre caracteres', recogerCaracteres('abc'))
        show("'abc'.collect { it.toUpperCase() }", 'abc'.collect { it.toUpperCase() })
        show("'hola mundo'.split(' ')*.capitalize()", 'hola mundo'.split(' ')*.capitalize())
        bullet('`*.` aplica el método a cada elemento. Es el spread, capítulo 04.')
    }

    private static List<String> recogerCaracteres(String texto) {
        List<String> letras = []
        texto.each { letras << it.toString() }
        letras
    }

    /**
     * Construir texto sin concatenar en un bucle.
     */
    static void demoBuilding() {
        section('Lo que NO hay que hacer')

        bullet('`for (x in lista) { texto += x }` crea una cadena nueva en cada vuelta.')
        bullet('Con 10 elementos da igual; con 10.000, no.')

        section('join: la forma idiomática')

        def nombres = ['Ana', 'Luis', 'Eva']
        show('join(", ")', nombres.join(', '))
        show('con transformación', nombres.collect { it.toUpperCase() }.join(' | '))

        section('StringBuilder, con el operador <<')

        def sb = new StringBuilder()
        sb << 'Hola' << ', ' << 'mundo'
        show('resultado', sb.toString())
        bullet('`<<` es `leftShift`, sobrecargado por el GDK. Capítulo 14.')

        section('El truco: `<<` sobre un String devuelve un StringBuilder')

        def acumulado = 'a' << 'b' << 'c'
        show('valor', acumulado)
        show('clase', acumulado.class.simpleName)
        bullet('Por eso encadenar `<<` es eficiente aunque empiece en un String.')
        bullet('Pero cuidado: el resultado NO es un String. Conviértelo al guardarlo.')

        section('withWriter / StringWriter para algo más grande')

        def escrito = new StringWriter().with { w ->
            3.times { w.println "línea $it" }
            w.toString()
        }
        show('líneas escritas', escrito.readLines().size())
        bullet('`with` viene del GDK (capítulo 20) y evita repetir la variable.')
    }

    /**
     * Multilínea y texto por líneas.
     */
    static void demoLines() {
        section('readLines() y eachLine()')

        def texto = 'primera\nsegunda\ntercera'
        show('readLines()', texto.readLines())
        show('número de líneas', texto.readLines().size())

        section('Numerar las líneas')

        def numerado = texto.readLines()
            .withIndex(1)
            .collect { String linea, int i -> "$i: $linea" }
        show('numerado', numerado)
        bullet('`withIndex(1)` empieza a contar en 1 en vez de en 0.')

        section('Procesar un bloque de texto de principio a fin')

        def csv = '''\
            nombre,edad
            Ana,30
            Luis,25'''.stripIndent()

        def cabecera = csv.readLines().first().tokenize(',')
        def filas = csv.readLines().tail().collect { String linea ->
            [cabecera, linea.tokenize(',')].transpose().collectEntries()
        }
        show('cabecera', cabecera)
        show('filas', filas)
        bullet('`transpose` empareja las dos listas; `collectEntries` las hace mapa.')
        bullet('El CSV completo, con fichero y casos límite, está en el capítulo 21.')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Prueba `'a,b,,c'.split(',').size()` y `'a,b,,c'.tokenize(',').size()`.
//  2. Cambia `texto[0..2]` por `texto[0..<2]` y compara.
//  3. Construye una cadena de 100 elementos con `+=` y con `join`, y mide con
//     System.nanoTime() cuál tarda más.
//  4. Guarda el resultado de `'a' << 'b'` en una variable declarada `String` y mira
//     qué pasa.
