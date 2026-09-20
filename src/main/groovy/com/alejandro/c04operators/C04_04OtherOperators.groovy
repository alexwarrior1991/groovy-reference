package com.alejandro.c04operators

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.quoted
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  04.4 · `in`, `<<`, la aritmética de colecciones y quién llama a quién
//
//  QUÉ ES
//    El resto de operadores que Groovy añade o redefine, y —lo más importante— el hecho
//    de que TODOS ellos son llamadas a métodos con un nombre concreto. `a + b` es
//    `a.plus(b)`, `a << b` es `a.leftShift(b)`, `a in b` es `b.isCase(a)`.
//
//  POR QUÉ IMPORTA
//    Entender esa correspondencia explica por qué `+` funciona sobre listas y mapas, y
//    es lo que permite sobrecargarlos en tus propias clases (capítulo 14).
//
//  ERRORES COMUNES
//    · Esperar que `in` sobre una cadena compruebe si contiene el texto. No lo hace.
//    · Usar `<<` sobre una lista creyendo que devuelve una copia: MUTA la original.
//    · Confundir `+` de listas (concatena) con `+` de Set (unión sin duplicados).
// =====================================================================================

class C04_04OtherOperators {

    /**
     * Pertenencia: `in` y `isCase`.
     */
    static void demoIn() {
        section('`a in b` es `b.isCase(a)`')

        show('2 in [1, 2, 3]', 2 in [1, 2, 3])
        show('5 in 1..10', 5 in 1..10)
        show("'a' in [a: 1]", 'a' in [a: 1])
        bullet('Sobre un mapa mira las CLAVES, no los valores.')

        section('La sorpresa: sobre una cadena NO es "contiene"')

        show("'ell' in 'hello'", 'ell' in 'hello')
        show("'hello'.contains('ell')", 'hello'.contains('ell'))
        bullet('`isCase` de String es IGUALDAD, no contención. Usa `contains`.')
        show("'hello' in 'hello'", 'hello' in 'hello')

        section('Negación: `!in`')

        show('4 !in [1, 2, 3]', 4 !in [1, 2, 3])
        bullet('Más legible que `!(4 in [1, 2, 3])`. Añadido en Groovy 3.')

        section('Sobre una clase: comprueba el tipo')

        show("'texto' in String", 'texto' in String)
        show('42 in String', 42 in String)
        bullet('`Class.isCase(x)` es `isInstance(x)`. Es lo que hace funcionar el switch.')

        section('Por qué esto importa: `in` y `switch` comparten mecanismo')

        bullet('`case X:` llama a `X.isCase(valor)`, igual que `valor in X`.')
        bullet('Por eso un `case` puede ser un rango, una lista, una clase o una regex.')
        bullet('Y por eso puedes hacer que tus clases funcionen en un switch (cap. 05).')
    }

    /**
     * `<<` y la aritmética de colecciones.
     */
    static void demoCollectionArithmetic() {
        section('`<<` añade, y MUTA el original')

        def lista = [1, 2]
        def resultado = lista << 3
        show('lista tras <<', lista)
        show('¿devuelve la misma lista?', resultado.is(lista))
        bullet('`<<` es `leftShift` y devuelve la MISMA colección, no una copia.')

        section('`+` no muta: devuelve una nueva')

        def original = [1, 2]
        def nueva = original + 3
        show('original', original)
        show('original + 3', nueva)
        bullet('Ésta es la diferencia práctica entre `<<` y `+`. Elige a conciencia.')

        section('El resto de la aritmética de listas')

        show('[1, 2] + [3, 4]', [1, 2] + [3, 4])
        show('[1, 2, 3] - 2', [1, 2, 3] - 2)
        show('[1, 2, 3] - [1, 3]', [1, 2, 3] - [1, 3])
        show('[1, 2] * 2', [1, 2] * 2)
        show('[1, 2] * 0', [1, 2] * 0)

        section('Con Set, `+` es unión')

        def a = [1, 2] as Set
        def b = [2, 3] as Set
        show('unión', a + b)
        show('diferencia', a - b)
        show('intersección', a.intersect(b))

        section('Con mapas')

        show('[a: 1] + [b: 2]', [a: 1] + [b: 2])
        show('[a: 1] + [a: 9]', [a: 1] + [a: 9])
        bullet('El de la derecha pisa. Igual que el spread `*:` de la demo anterior.')

        def mapa = [:]
        mapa << [x: 1]
        show('mapa << [x: 1]', mapa)

        section('Con cadenas')

        show("'hola' + ' mundo'", 'hola' + ' mundo')
        show("'hola.txt' - '.txt'", quoted('hola.txt' - '.txt'))
        show("'ab' * 3", 'ab' * 3)
        bullet('`-` sobre cadenas quita la PRIMERA aparición, no todas.')
        show("'aaa' - 'a'", quoted('aaa' - 'a'))
    }

    /**
     * Rangos.
     */
    static void demoRanges() {
        section('Los tres tipos de rango')

        show('1..5 (inclusivo)', (1..5).toList())
        show('1..<5 (exclusivo por arriba)', (1..<5).toList())
        show('1<..<5 (exclusivo por los dos)', (1<..<5).toList())
        bullet('`1<..5` también existe: exclusivo sólo por abajo.')
        show('1<..5', (1<..5).toList())

        section('Al revés')

        show('5..1', (5..1).toList())
        bullet('Groovy lo recorre descendente. No hace falta `reverse()`.')

        section('No sólo números')

        show("'a'..'e'", ('a'..'e').toList())
        show('tamaño de 1..100', (1..100).size())
        show('¿contiene?', 50 in 1..100)
        bullet('Un rango no materializa sus elementos: `1..1000000` no ocupa nada.')

        section('Dónde se usan')

        show('en un for', sumaDe(1..10))
        show('para indexar', 'Groovy'[0..2])
        show('para trocear listas', [10, 20, 30, 40][1..2])
        show('en un case', clasificar(75))
    }

    private static int sumaDe(IntRange rango) {
        int total = 0
        for (int i in rango) total += i
        total
    }

    private static String clasificar(int nota) {
        switch (nota) {
            case 0..<50 -> 'suspenso'
            case 50..<70 -> 'aprobado'
            case 70..100 -> 'notable'
            default -> 'fuera de rango'
        }
    }

    /**
     * La tabla: qué operador llama a qué método.
     */
    static void demoOperatorTable() {
        section('Todos los operadores son llamadas a métodos')

        bullet('operador        método')
        bullet('─────────────   ────────────────────')
        bullet('a + b           a.plus(b)')
        bullet('a - b           a.minus(b)')
        bullet('a * b           a.multiply(b)')
        bullet('a / b           a.div(b)')
        bullet('a % b           a.mod(b)')
        bullet('a ** b          a.power(b)')
        bullet('a << b          a.leftShift(b)')
        bullet('a >> b          a.rightShift(b)')
        bullet('a[b]            a.getAt(b)')
        bullet('a[b] = c        a.putAt(b, c)')
        bullet('a == b          a.equals(b) o a.compareTo(b)')
        bullet('a <=> b         a.compareTo(b)')
        bullet('a < b           a.compareTo(b) < 0')
        bullet('a in b          b.isCase(a)')
        bullet('a as T          a.asType(T)')
        bullet('a()             a.call()')
        bullet('-a              a.negative()')
        bullet('a++             a.next()')
        bullet('a--             a.previous()')

        section('Comprobémoslo')

        show('3.plus(4)', 3.plus(4))
        show('[1].leftShift(2)', [1].leftShift(2))
        show('[1, 2].getAt(0)', [1, 2].getAt(0))
        show('[1, 2, 3].isCase(2)', [1, 2, 3].isCase(2))
        show("'a'.next()", 'a'.next())

        section('Y por eso puedes sobrecargarlos')

        def a = new Dinero(10)
        def b = new Dinero(5)
        show('Dinero(10) + Dinero(5)', a + b)
        show('Dinero(10) - Dinero(5)', a - b)
        show('Dinero(10) * 3', a * 3)
        show('Dinero(10) > Dinero(5)', a > b)
        bullet('Basta definir `plus`, `minus`, `multiply` y `compareTo`. Capítulo 14.')
    }

    /** Una clase mínima con operadores. El capítulo 14 la desarrolla en serio. */
    static class Dinero implements Comparable<Dinero> {
        final int centimos

        Dinero(int centimos) { this.centimos = centimos }

        Dinero plus(Dinero otro) { new Dinero(centimos + otro.centimos) }

        Dinero minus(Dinero otro) { new Dinero(centimos - otro.centimos) }

        Dinero multiply(int veces) { new Dinero(centimos * veces) }

        @Override
        int compareTo(Dinero otro) { centimos <=> otro.centimos }

        @Override
        String toString() { "${centimos}c" }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Prueba `'hello' in ['hello', 'adiós']` y compáralo con `'ell' in 'hello'`.
//  2. Quita el `compareTo` de `Dinero` y mira qué operadores dejan de funcionar.
//  3. Haz `def a = [1,2]; def b = a << 3` y comprueba que `a` y `b` son el mismo objeto.
//  4. Define `Dinero.div(int)` y prueba `Dinero(10) / 2`.
//  5. Añade `isCase` a `Dinero` y úsalo en un `switch`.
