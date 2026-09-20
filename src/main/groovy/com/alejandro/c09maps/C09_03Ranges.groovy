package com.alejandro.c09maps

import java.time.LocalDate

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  09.3 · Rangos
//
//  QUÉ ES
//    `1..10` es un objeto `Range`, que ES una `List` pero no guarda sus elementos: los
//    calcula cuando hacen falta. Funciona con cualquier tipo que sea `Comparable` y
//    sepa decir cuál es su siguiente (`next()`) y su anterior (`previous()`).
//
//  POR QUÉ IMPORTA
//    Un rango de un millón de elementos no ocupa memoria. Y como es una lista, encaja
//    en todo: `each`, `collect`, `in`, `switch`, indexado de cadenas.
//
//  ERRORES COMUNES
//    · Llamar a `toList()` sobre un rango enorme sin querer.
//    · Olvidar que `5..1` recorre hacia atrás en vez de estar vacío.
//    · Esperar que `1..<5` incluya el 5.
// =====================================================================================

class C09_03Ranges {

    /**
     * Los tipos de rango.
     */
    static void demoKinds() {
        section('Inclusivo y exclusivo')

        show('1..5', (1..5).toList())
        show('1..<5  (sin el final)', (1..<5).toList())
        show('1<..5  (sin el principio)', (1<..5).toList())
        show('1<..<5 (sin ninguno)', (1<..<5).toList())

        section('Hacia atrás')

        show('5..1', (5..1).toList())
        bullet('NO está vacío: recorre descendente. Distinto de casi todos los')
        bullet('lenguajes, donde un rango invertido no tiene elementos.')
        show('5..1 es reverso', (5..1).reverse)

        section('De cualquier Comparable, no sólo enteros')

        show("'a'..'e'", ('a'..'e').toList())
        show('1.0..3.0', (1.0..3.0).toList())
        bullet('Sólo tres elementos: `next()` de un BigDecimal suma 1, no 0.1.')

        def hoy = LocalDate.of(2026, 9, 20)
        show('rango de fechas', (hoy..hoy.plusDays(3)).toList())
        bullet('`LocalDate` es Comparable y el GDK le da `next()`. Capítulo 20.')

        section('Con paso')

        show('(1..10).step(3)', (1..10).step(3))
        show('(10..1).step(3)', (10..1).step(3))
        bullet('`Range.step(n)` salta de n en n POSICIONES, y sólo acepta un entero.')

        section('Para un paso fraccionario, el `step` de los números')

        List<BigDecimal> conDecimales = []
        1.0.step(3.0, 0.5) { conDecimales << (it as BigDecimal) }
        show('1.0.step(3.0, 0.5)', conDecimales)
        bullet('Es otro método: `Number.step(hasta, paso) { }`, y NO incluye el final.')
    }

    /**
     * Por qué es barato.
     */
    static void demoLaziness() {
        section('Un rango no guarda sus elementos')

        def enorme = 1..10_000_000
        show('size()', enorme.size())
        show('contains(9_999_999)', 9_999_999 in enorme)
        show('primer elemento', enorme.first())
        show('último', enorme.last())
        bullet('Nada de esto ha creado diez millones de objetos.')

        section('Pero `toList()` sí los crea')

        bullet('`(1..10_000_000).toList()` reserva memoria para los diez millones.')
        bullet('Y lo mismo hacen `collect`, `findAll` o `sort` sobre el rango.')

        section('Lo que sí corta')

        show('find sobre un rango enorme', (1..10_000_000).find { it > 5 })
        show('any', (1..10_000_000).any { it > 5 })
        show('take(3)', (1..10_000_000).take(3))
        bullet('`find`, `any`, `every` y `take` paran en cuanto pueden.')

        section('Un rango ES una lista')

        show('(1..5) instanceof List', (1..5) instanceof List)
        show('(1..5).class.simpleName', (1..5).getClass().simpleName)
        show('(1..5)[2]', (1..5)[2])
        show('(1..5).reverse()', (1..5).reverse())
    }

    /**
     * Dónde se usan.
     */
    static void demoUses() {
        section('Recorrer')

        show('for (i in 1..3)', recorrer())
        show('(1..5).sum()', (1..5).sum())
        show('(1..5).collect { it * it }', (1..5).collect { it * it })

        section('Indexar cadenas y listas')

        show("'Groovy'[0..2]", 'Groovy'[0..2])
        show("'Groovy'[-3..-1]", 'Groovy'[-3..-1])
        show('[10,20,30,40][1..2]', [10, 20, 30, 40][1..2])

        section('Comprobar pertenencia')

        show('5 in 1..10', 5 in 1..10)
        show('15 in 1..10', 15 in 1..10)
        bullet('Es O(1): compara con los extremos, no recorre.')

        section('En un switch')

        show('clasificar(5)', clasificar(5))
        show('clasificar(75)', clasificar(75))
        show('clasificar(500)', clasificar(500))

        section('Trocear en lotes')

        show('(1..10).collate(3)', (1..10).collate(3))
        bullet('El patrón para procesar en tandas: consultas, envíos, ficheros.')

        section('Generar datos de prueba')

        show('cinco usuarios', (1..5).collect { [id: it, nombre: "usuario$it"] }*.nombre)
    }

    private static List<Integer> recorrer() {
        List<Integer> salida = []
        for (i in 1..3) salida << i
        salida
    }

    private static String clasificar(int n) {
        switch (n) {
            case 0..9 -> 'unidades'
            case 10..99 -> 'decenas'
            case 100..999 -> 'centenas'
            default -> 'más'
        }
    }

    /**
     * Rangos de tus propias clases.
     */
    static void demoCustomRanges() {
        section('Para que una clase sirva en un rango hacen falta tres cosas')

        bullet('1. implementar Comparable')
        bullet('2. un método `next()`')
        bullet('3. un método `previous()`')

        section('Una talla de ropa')

        def desde = new Talla('S')
        def hasta = new Talla('XL')

        show('S..XL', (desde..hasta).toList())
        show('¿M está dentro?', new Talla('M') in (desde..hasta))
        show('cuántas tallas', (desde..hasta).size())

        section('Y funciona en un switch, claro')

        show('clasificaTalla(M)', clasificaTalla(new Talla('M')))
        show('clasificaTalla(XXL)', clasificaTalla(new Talla('XXL')))

        section('Cuándo merece la pena')

        bullet('Cuando el dominio tiene una secuencia natural: tallas, niveles,')
        bullet('categorías, versiones, turnos.')
        bullet('Si tienes que forzar qué significa "el siguiente", no lo hagas.')
    }

    private static String clasificaTalla(Talla t) {
        switch (t) {
            case new Talla('S')..new Talla('L') -> 'disponible'
            default -> 'bajo pedido'
        }
    }

    /** Una talla de ropa, ordenable y con sucesor: ya sirve para hacer rangos. */
    static class Talla implements Comparable<Talla> {
        static final List<String> ORDEN = ['XS', 'S', 'M', 'L', 'XL', 'XXL']

        final String etiqueta

        Talla(String etiqueta) {
            assert etiqueta in ORDEN, "talla desconocida: $etiqueta"
            this.etiqueta = etiqueta
        }

        Talla next() {
            int i = ORDEN.indexOf(etiqueta)
            i < ORDEN.size() - 1 ? new Talla(ORDEN[i + 1]) : this
        }

        Talla previous() {
            int i = ORDEN.indexOf(etiqueta)
            i > 0 ? new Talla(ORDEN[i - 1]) : this
        }

        @Override
        int compareTo(Talla otra) { ORDEN.indexOf(etiqueta) <=> ORDEN.indexOf(otra.etiqueta) }

        @Override
        boolean equals(Object otro) { otro instanceof Talla && otro.etiqueta == etiqueta }

        @Override
        int hashCode() { etiqueta.hashCode() }

        @Override
        String toString() { etiqueta }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Mide cuánto tarda `(1..10_000_000).size()` y cuánto `(1..10_000_000).toList()`.
//  2. Quita el `previous()` de `Talla` y mira si el rango sigue funcionando.
//  3. Haz un rango de `LocalDate` de un año y cuenta los lunes.
//  4. Prueba `(5..1).step(2)` y explica el resultado.
