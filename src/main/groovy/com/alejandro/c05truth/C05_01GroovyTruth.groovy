package com.alejandro.c05truth

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.quoted
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  05.1 · El Groovy Truth
//
//  QUÉ ES
//    En Groovy, cualquier objeto puede usarse donde se espera un booleano. La regla no
//    es "distinto de null": cada tipo decide qué significa ser "cierto", a través del
//    método `asBoolean()`.
//
//  POR QUÉ IMPORTA
//    Es lo que permite escribir `if (lista)` en vez de `if (lista != null &&
//    !lista.isEmpty())`. Es de lo más cómodo del lenguaje y, a la vez, la causa de la
//    trampa del elvis (04.6): un 0 o una cadena vacía legítimos son "falsos".
//
//  ERRORES COMUNES
//    · Creer que sólo `null` es falso.
//    · `if (contador)` cuando el contador puede valer 0 de forma legítima.
//    · Creer que la cadena "false" es falsa. No lo es: tiene contenido.
// =====================================================================================

class C05_01GroovyTruth {

    /**
     * La tabla completa.
     */
    static void demoTheTable() {
        section('Qué es FALSO')

        bullet('tipo                 falso cuando')
        bullet('──────────────────   ────────────────────────────')
        bullet('null                 siempre')
        bullet('Boolean              es false')
        bullet('Número               vale cero')
        bullet('String / GString     está vacía ("")')
        bullet('Colección / array    está vacía')
        bullet('Map                  está vacío')
        bullet('Iterator             no le quedan elementos')
        bullet('Matcher              no ha encontrado nada')
        bullet('cualquier otro       nunca: un objeto es cierto')

        section('Comprobado')

        show('null', verdad(null))
        show('false', verdad(false))
        show('0', verdad(0))
        show('0.0', verdad(0.0))
        show('0.0d', verdad(0.0d))
        show(quoted(''), verdad(''))
        show(quoted(' '), verdad(' '))
        show('[]', verdad([]))
        show('[:]', verdad([:]))
        show('[0]', verdad([0]))
        show('new Object()', verdad(new Object()))

        section('Los dos que más sorprenden')

        show(quoted('false'), verdad('false'))
        bullet('Una cadena es cierta si NO está vacía. "false" tiene cinco letras.')
        show('[0] (lista con un cero)', verdad([0]))
        bullet('La lista no está vacía, así que es cierta, aunque contenga un falso.')
    }

    private static String verdad(Object valor) { valor ? 'cierto' : 'falso' }

    /**
     * El mecanismo: asBoolean().
     */
    static void demoAsBoolean() {
        section('Todo pasa por `asBoolean()`')

        show("''.asBoolean()", ''.asBoolean())
        show("'x'.asBoolean()", 'x'.asBoolean())
        show('[].asBoolean()', [].asBoolean())
        show('0.asBoolean()', 0.asBoolean())
        bullet('`if (x)` es literalmente `if (x.asBoolean())`, con el null aparte.')

        section('Un Matcher es cierto si ha encontrado algo')

        show("'abc' =~ /b/", verdadDeMatcher('abc', /b/))
        show("'abc' =~ /z/", verdadDeMatcher('abc', /z/))
        bullet('Por eso `if (texto =~ /patrón/)` se lee tan bien. Capítulo 19.')

        section('Y un Iterator, si le quedan elementos')

        show('[].iterator()', [].iterator() ? 'cierto' : 'falso')
        show('[1].iterator()', [1].iterator() ? 'cierto' : 'falso')
    }

    private static String verdadDeMatcher(String texto, String patron) {
        (texto =~ patron) ? 'cierto' : 'falso'
    }

    /**
     * Definir la verdad de tus propias clases.
     */
    static void demoCustomTruth() {
        section('Por defecto, un objeto siempre es cierto')

        def vacio = new Carrito()
        show('carrito recién creado (sin asBoolean)', new SinVerdad() ? 'cierto' : 'falso')

        section('Definiendo asBoolean() decides tú')

        show('carrito vacío', vacio ? 'cierto' : 'falso')

        def conProductos = new Carrito()
        conProductos.añadir('libro')
        show('carrito con un producto', conProductos ? 'cierto' : 'falso')

        section('Lo que eso te permite escribir')

        show('if (carrito) …', conProductos ? 'hay productos' : 'está vacío')
        show('carrito ?: "vacío"', (vacio ?: 'vacío').toString())
        bullet('Todo lo que usa el Groovy Truth empieza a funcionar con tu clase.')

        section('Cuándo merece la pena')

        bullet('Cuando "vacío" o "nada" tiene un significado obvio en tu dominio.')
        bullet('Carrito, Resultado, Página, Rango, Colección propia.')
        bullet('Si hay que pararse a pensar qué significa, NO lo definas: confunde.')
    }

    static class SinVerdad {}

    static class Carrito {
        private final List<String> productos = []

        void añadir(String producto) { productos << producto }

        /** Un carrito es "cierto" si tiene algo dentro. */
        boolean asBoolean() { !productos.isEmpty() }

        @Override
        String toString() { "Carrito(${productos.size()})" }
    }

    /**
     * Dónde se aplica, y la trampa.
     */
    static void demoWhereItApplies() {
        section('Todos estos sitios usan el Groovy Truth')

        bullet('if (x) / while (x)')
        bullet('x ?: valorPorDefecto')
        bullet('x ? a : b')
        bullet('!x')
        bullet('lista.grep()')
        bullet('lista.findAll { it }')
        bullet('assert x')
        bullet('x && y  /  x || y')

        show('[1, 0, null, "", "a"].grep()', [1, 0, null, '', 'a'].grep())
        show('[1, 0, null].findAll { it }', [1, 0, null].findAll { it })
        bullet('`grep()` y `findAll { it }` filtran por verdad: se llevan los ceros.')

        section('`&&` y `||` no devuelven booleanos')

        show("'a' && 'b'", 'a' && 'b')
        show("null ?: 'b'", null ?: 'b')
        bullet('`&&` sí devuelve un booleano de verdad; el que devuelve el operando')
        bullet('es el elvis. Es un detalle que se confunde a menudo:')
        show("('a' && 'b').class", ('a' && 'b').class.simpleName)
        show("(null ?: 'b').class", (null ?: 'b').class.simpleName)

        section('LA TRAMPA')

        int stock = 0
        show('if (stock) "hay" : "no hay"', stock ? 'hay' : 'no hay')
        bullet('Con stock = 0 dice "no hay", que aquí hasta es correcto…')

        int temperatura = 0
        show('temperatura ?: 20', temperatura ?: 20)
        bullet('…pero con una temperatura de 0 grados, esto es un bug.')

        section('La regla para no caer')

        bullet('Si el valor puede ser 0, "" o [] DE FORMA LEGÍTIMA, sé explícito:')
        show('temperatura != null ? temperatura : 20', temperatura != null ? temperatura : 20)
        bullet('El Groovy Truth es para "¿hay algo?", no para "¿tiene valor?".')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `asBoolean()` de `Carrito` y mira qué pasa con el carrito vacío.
//  2. Prueba `[null, null].grep()` y `[null, null] ? 'cierto' : 'falso'`: la lista no
//     está vacía, aunque todo lo que tenga dentro sea falso.
//  3. Escribe `if ('false')` y explica el resultado.
//  4. Haz que `Carrito.asBoolean()` devuelva siempre false y observa cuántas cosas
//     dejan de funcionar (`?:`, `grep`, `if`, `assert`).
