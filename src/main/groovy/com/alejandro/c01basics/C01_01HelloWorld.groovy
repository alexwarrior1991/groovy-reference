package com.alejandro.c01basics

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.quoted
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  01.1 · El programa más corto, y por qué Groovy no necesita ceremonia
//
//  QUÉ ES
//    En Groovy, un fichero .groovy puede ser DOS cosas distintas:
//      · una clase normal, como en Java
//      · un script: sentencias sueltas, sin clase ni método `main` a la vista
//    Un script de una línea (`println 'Hola'`) es un programa completo y válido.
//
//  POR QUÉ IMPORTA
//    Es la primera diferencia grande con Java, y la que explica que Groovy se use para
//    Gradle, Jenkins y automatización: el coste de escribir "algo que se ejecuta" es
//    cero. Pero el repositorio está hecho de CLASES, no de scripts, y la razón está en
//    la demo 01.2: un script no deja llamar a sus métodos desde fuera.
//
//  ERRORES COMUNES
//    · Creer que el script no tiene clase. Sí la tiene: Groovy genera una que extiende
//      `groovy.lang.Script` y le pone el nombre del fichero.
//    · Declarar `static void main` DENTRO de un script cuyo nombre coincide: choca con
//      el `main` que Groovy ya genera.
//    · Pensar que `println 'Hola'` es magia. Es `this.println('Hola')`: paréntesis
//      opcionales y un método que el GDK añade a todo objeto (capítulo 20).
// =====================================================================================

class C01_01HelloWorld {

    /**
     * Hola mundo, y lo que hay debajo.
     */
    static void demoHelloWorld() {
        section('El programa entero')

        // Esto, en un fichero suelto, ya es un programa ejecutable:
        println '    ¡Hola, Groovy!'

        bullet('Sin clase, sin main, sin punto y coma, sin paréntesis.')
        bullet('`println \'x\'` es en realidad `this.println(\'x\')`.')

        section('Las comillas simples son String de Java')

        String texto = 'Hola'
        show('tipo', texto.class.name)
        show('longitud', texto.length())

        bullet('Comilla simple → java.lang.String, sin interpolación.')
        bullet('Comilla doble → GString si hay $, String si no. Capítulo 03.')
    }

    /**
     * Sentencia frente a expresión: en Groovy casi todo devuelve algo.
     */
    static void demoExpressions() {
        section('El `return` es opcional: se devuelve la última expresión')

        show('ultimaExpresion()', ultimaExpresion())
        show('conReturn()', conReturn())
        bullet('Las dos hacen lo mismo. La primera es la forma idiomática.')

        section('`if` NO es una expresión, pero el método sí devuelve su rama')

        show('clasifica(5)', clasifica(5))
        show('clasifica(-5)', clasifica(-5))
        show('clasifica(0)', clasifica(0))
        bullet('No existe `def x = if (...)`. Para eso está el ternario o el elvis.')

        section('El ternario y el elvis sí son expresiones')

        int n = 7
        show('n % 2 == 0 ? "par" : "impar"', n % 2 == 0 ? 'par' : 'impar')

        String vacio = ''
        // `?:` devuelve la izquierda si es "cierta" (capítulo 05), si no la derecha.
        show('vacio ?: "por defecto"', vacio ?: 'por defecto')
    }

    private static int ultimaExpresion() { 2 + 2 }

    private static int conReturn() { return 2 + 2 }

    private static String clasifica(int n) {
        if (n > 0) 'positivo'
        else if (n < 0) 'negativo'
        else 'cero'
    }

    /**
     * Los paréntesis y el punto y coma, y cuándo NO se pueden quitar.
     */
    static void demoOptionalSyntax() {
        section('Punto y coma: sólo hace falta para juntar dos sentencias en una línea')

        def a = 1; def b = 2
        show('a, b en una sola línea', "$a, $b")

        section('Paréntesis: opcionales en una llamada con argumentos')

        show('con paréntesis', 'hola'.toUpperCase())
        // Sin paréntesis sólo funciona si hay al menos un argumento:
        show('sin paréntesis (con argumento)', String.valueOf(42))

        section('Cuándo NO se pueden quitar')

        bullet('Sin argumentos: `texto.toUpperCase` es una PROPIEDAD, no la llamada.')
        bullet('Anidadas: `println foo bar` es ambiguo y no compila.')
        bullet('Como primera cosa de una expresión mayor.')

        // Demostración de la primera: sin paréntesis y sin argumentos, Groovy busca
        // la propiedad `bytes`, no el método.
        show('"ab".bytes (propiedad)', 'ab'.bytes)
        show('"ab".getBytes() (método)', 'ab'.getBytes())
        bullet('Aquí coinciden porque `getBytes()` ES el getter de la propiedad `bytes`.')

        section('Groovy Truth por adelantado: `if (lista)` ya comprueba si está vacía')

        List<String> vacia = []
        List<String> llena = ['a']
        show('if ([])', vacia ? 'cierto' : 'falso')
        show('if (["a"])', llena ? 'cierto' : 'falso')
        bullet('Esto es el capítulo 05, y es de lo más Groovy que hay.')
    }

    /**
     * Comentarios y la salida por consola del repositorio.
     */
    static void demoCommentsAndOutput() {
        section('Los tres tipos de comentario')

        // De línea, como en Java.
        /* De bloque, como en Java. */
        /** De GroovyDoc: el que documenta la clase o el método de abajo. */
        bullet('//  de línea')
        bullet('/* */ de bloque')
        bullet('/** */ GroovyDoc, el que sale en la documentación')
        bullet('#! shebang, sólo en la PRIMERA línea de un script ejecutable')

        section('Los helpers de salida de este repositorio')

        bullet('section("…")  abre un sub-apartado, como el de arriba')
        bullet('show(etiqueta, valor)  imprime alineado en dos columnas')
        bullet('bullet("…")  una viñeta suelta, como esta línea')

        section('Por qué show() y no println() a secas')

        show('sin comillas', '  hola  '.trim())
        show('con comillas', quoted('  hola  '.trim()))
        bullet('`quoted` se usa cuando lo importante son los BORDES de la cadena.')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita los paréntesis de `'hola'.toUpperCase()` y mira qué imprime ahora.
//  2. Cambia `ultimaExpresion()` para que su última línea sea `def x = 2 + 2` y
//     comprueba qué devuelve entonces el método.
//  3. Escribe `def a = 1 def b = 2` en una línea sin el `;` y lee el error.
//  4. Cambia las comillas simples de `texto` por dobles y comprueba que el tipo sigue
//     siendo String (no hay `$`, así que no hay GString).
