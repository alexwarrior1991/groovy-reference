package com.alejandro.c20gdk

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.quoted
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  20.1 · El GDK: lo que Groovy le añade al JDK
//
//  QUÉ ES
//    El Groovy Development Kit: cientos de métodos añadidos a las clases de Java a
//    través de módulos de extensión (capítulo 17). `'abc'.capitalize()`,
//    `[1,2].sum()`, `10.times { }` y `fichero.text` son todos del GDK, no de Java.
//
//  POR QUÉ IMPORTA
//    Media librería de utilidades de un proyecto Java sobra en Groovy. Y lo que hay en
//    el GDK está probado, es consistente y lo conoce cualquiera que lea tu código.
//
//  ERRORES COMUNES
//    · Buscar estos métodos en la documentación de Java: no están ahí.
//    · Reescribir a mano algo que ya existe.
//    · `with` y `tap` confundidos: uno devuelve el bloque, el otro el objeto.
// =====================================================================================

class C20_01Gdk {

    /**
     * Object: lo que TODO objeto gana.
     */
    static void demoObject() {
        section('`with`: hablar de un objeto sin repetir su nombre')

        def config = new Configuracion()
        String resultado = config.with {
            host = 'ejemplo.com'
            puerto = 8080
            "$host:$puerto"            // ← esto es lo que devuelve `with`
        }
        show('config', config)
        show('lo que devolvió with', resultado)
        bullet('`with` devuelve EL BLOQUE. Es `run` con receptor: internamente')
        bullet('cambia el delegate del closure (capítulo 07).')

        section('`tap`: igual, pero devuelve EL OBJETO')

        def otra = new Configuracion().tap {
            host = 'otro.com'
            puerto = 9090
        }
        show('lo que devolvió tap', otra)
        bullet('`tap` es para CONFIGURAR y quedarte el objeto; `with`, para CALCULAR')
        bullet('algo a partir de él. Es la única diferencia entre los dos.')

        section('Encadenar sin variables temporales')

        show('construido en una expresión', new Configuracion().tap {
            host = 'api.ejemplo.com'
            puerto = 443
        }.toString())

        section('Los demás métodos de Object')

        show('42.identity { it * 2 }', 42.identity { it * 2 })
        show('"x".is("x")', 'x'.is('x'))
        show('[1,2].asBoolean()', [1, 2].asBoolean())
        show('"texto".asType(List)', 'texto'.asType(List))
        show('42.respondsTo("intValue")', 42.respondsTo('intValue') as boolean)
        show('1.dump()', 1.dump())
        bullet('`dump()` e `inspect()` son para depurar: enseñan el objeto por dentro.')
        show("[1,'a'].inspect()", [1, 'a'].inspect())

        section('sleep, sin la excepción de Java')

        long t0 = System.nanoTime()
        sleep(5)
        show('sleep(5) tardó', "${((System.nanoTime() - t0) / 1_000_000L) as long} ms")
        bullet('El `sleep` del GDK no obliga a capturar InterruptedException.')
    }

    static class Configuracion {
        String host = ''
        int puerto = 0

        @Override
        String toString() { "$host:$puerto" }
    }

    /**
     * Números.
     */
    static void demoNumbers() {
        section('Repetir e iterar')

        show('3.times', recoger { List<Integer> l -> 3.times { l << it } })
        show('1.upto(3)', recoger { List<Integer> l -> 1.upto(3) { l << (it as Integer) } })
        show('3.downto(1)', recoger { List<Integer> l -> 3.downto(1) { l << (it as Integer) } })
        show('0.step(10, 4)', recoger { List<Integer> l -> 0.step(10, 4) { l << (it as Integer) } })

        section('Cálculo')

        show('(-5).abs()', (-5).abs())
        show('3.7.round()', 3.7.round())
        show('3.14159.round(2)', 3.14159.round(2))
        show('7.intdiv(2)', 7.intdiv(2))
        show('2 ** 10', 2 ** 10)
        show('10.power(3)', 10.power(3))

        section('Preguntar')

        show('5.compareTo(3)', 5.compareTo(3))
        show('"42".isInteger()', '42'.isInteger())
        show('"3.5".isBigDecimal()', '3.5'.isBigDecimal())

        section('Conversión explícita')

        show('42.toString()', quoted(42.toString()))
        show('42.toDouble()', 42.toDouble())
        show('"ff".toInteger() en hex', Integer.parseInt('ff', 16))
        show('42.toBigInteger()', 42.toBigInteger().getClass().simpleName)
    }

    private static List<Integer> recoger(Closure<?> bloque) {
        List<Integer> salida = []
        bloque(salida)
        salida
    }

    /**
     * Fechas: lo que el GDK le añade a java.time.
     */
    static void demoDates() {
        section('Aritmética con operadores')

        def hoy = LocalDate.of(2026, 9, 20)
        show('hoy', hoy)
        show('hoy + 1', hoy + 1)
        show('hoy - 7', hoy - 7)
        show('hoy++ (el siguiente)', hoy.next())
        bullet('`+` y `-` sobre una fecha suman y restan DÍAS. Es el operador `plus`')
        bullet('del capítulo 14, añadido por groovy-datetime.')

        section('Rangos de fechas')

        def semana = hoy..(hoy + 6)
        show('días de la semana', semana.size())
        show('los lunes del mes', (LocalDate.of(2026, 9, 1)..LocalDate.of(2026, 9, 30))
            .findAll { it.dayOfWeek == DayOfWeek.MONDAY }*.dayOfMonth)
        bullet('Un rango de fechas es una lista: `findAll`, `collect` y `each` valen.')

        section('Recorrer')

        List<String> dias = []
        hoy.upto(hoy + 3) { dias << it.dayOfWeek.toString().take(3) }
        show('upto', dias)

        section('Diferencias')

        def otraFecha = LocalDate.of(2026, 12, 25)
        show('días hasta Navidad', java.time.temporal.ChronoUnit.DAYS.between(hoy, otraFecha))

        def periodo = hoy.until(otraFecha)
        show('en meses y días', "${periodo.months} meses y ${periodo.days} días")
        bullet('`until` da un Period desglosado; `ChronoUnit.DAYS.between`, el total.')
        bullet('No los mezcles: sumar `months * 30` a los días NO da el total.')

        section('Formatear y parsear')

        def momento = LocalDateTime.of(2026, 9, 20, 14, 30)
        show('format', momento.format(DateTimeFormatter.ofPattern('dd/MM/yyyy HH:mm')))
        show('parse', LocalDate.parse('2026-09-20'))
        show('parse con patrón', LocalDate.parse('20/09/2026', DateTimeFormatter.ofPattern('dd/MM/yyyy')))

        section('Duraciones')

        def duracion = Duration.ofMinutes(135)
        show('duración', duracion)
        show('en horas y minutos', "${duracion.toHours()}h ${duracion.toMinutesPart()}m")

        section('Aviso sobre las fechas antiguas')

        bullet('`java.util.Date` y `Calendar` siguen existiendo y el GDK también les')
        bullet('añade métodos, pero en código nuevo usa `java.time`: es inmutable,')
        bullet('tiene zona horaria de verdad y no tiene las trampas de Date.')
    }

    /**
     * Colecciones: lo que aún no se ha visto.
     */
    static void demoCollections() {
        section('Estadística rápida')

        def numeros = [3, 1, 4, 1, 5, 9, 2, 6]
        show('sum()', numeros.sum())
        show('average()', numeros.average())
        show('min() / max()', [numeros.min(), numeros.max()])
        show('sort(false)', numeros.sort(false))
        show('countBy { par/impar }', numeros.countBy { it % 2 == 0 ? 'par' : 'impar' })

        section('Combinar colecciones')

        show('[1,2].plus([3])', [1, 2] + [3])
        show('intersect', [1, 2, 3].intersect([2, 3, 4]))
        show('disjoint', [1, 2].disjoint([3, 4]))
        show('union con Set', ([1, 2] as Set) + ([2, 3] as Set))
        show('combinations', [[1, 2], ['a', 'b']].combinations())
        show('subsequences', [1, 2, 3].subsequences().size())
        show('permutations', [1, 2, 3].permutations().size())

        section('Recorrer de otras formas')

        show('reverseEach', recogerTexto { List l -> [1, 2, 3].reverseEach { l << it } })
        show('eachWithIndex', recogerTexto { List l -> ['a', 'b'].eachWithIndex { v, i -> l << "$i$v" } })
        show('withIndex()', ['a', 'b'].withIndex().collect { v, i -> "$i:$v" })
        show('eachPermutation (primeras 2)', [1, 2].permutations().toList().take(2))

        section('Las que rellenan huecos')

        show('withDefault', unMapaConDefecto())
        show('flatten', [1, [2, [3]]].flatten())
        show('collate', (1..7).collate(3))
        show('indexed()', ['a', 'b'].indexed())
        show('takeRight / dropRight', [[1, 2, 3].takeRight(2), [1, 2, 3].dropRight(1)])

        section('Ordenación estable y con varios criterios')

        def gente = [[n: 'Ana', e: 30], [n: 'Luis', e: 25], [n: 'Eva', e: 30]]
        show('por edad y nombre', gente.toSorted { a, b -> (a.e <=> b.e) ?: (a.n <=> b.n) }*.n)
        bullet('El `sort` de Java es estable: a igualdad, mantiene el orden original.')
    }

    private static List<Object> recogerTexto(Closure<?> bloque) {
        List<Object> salida = []
        bloque(salida)
        salida
    }

    private static Map<String, Integer> unMapaConDefecto() {
        def m = [:].withDefault { 0 }
        ['a', 'b', 'a'].each { m[it] = (m[it] as int) + 1 }
        m as Map<String, Integer>
    }

    /**
     * Cómo encontrar lo que necesitas.
     */
    static void demoDiscovering() {
        section('Preguntarle al objeto qué sabe hacer')

        show('cuántos métodos tiene String', 'x'.metaClass.methods.size())
        show('los que empiezan por "to"', 'x'.metaClass.methods*.name.findAll {
            it.startsWith('to')
        }.sort().unique().take(8))

        section('Buscar por lo que quieres hacer')

        show('métodos de List con "each"', [].metaClass.methods*.name.findAll {
            it.toLowerCase().contains('each')
        }.sort().unique())

        show('métodos de Number con "to"', (1).metaClass.methods*.name.findAll {
            it.startsWith('to')
        }.sort().unique().take(8))

        section('Dónde está documentado')

        bullet('La documentación del GDK está separada de la de Java:')
        bullet('  https://docs.groovy-lang.org/latest/html/groovy-jdk/')
        bullet('Está organizada por la clase de Java a la que extiende.')

        section('La regla práctica')

        bullet('Antes de escribir un bucle, pregúntate si ya existe el método.')
        bullet('Antes de añadir una librería de utilidades, mira el GDK.')
        bullet('Y si escribes un `for` de tres líneas sobre una colección, casi')
        bullet('seguro que hay un método que lo dice en una.')

        section('El GDK es un módulo de extensión')

        bullet('Todo esto se añade con el mecanismo del capítulo 17: una clase con')
        bullet('métodos estáticos (`DefaultGroovyMethods`) y un fichero en META-INF.')
        bullet('Tú puedes escribir el tuyo exactamente igual.')
        show('la clase que lo implementa', org.codehaus.groovy.runtime.DefaultGroovyMethods.simpleName)
        show('cuántos métodos declara', org.codehaus.groovy.runtime.DefaultGroovyMethods.methods.length)
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia un `with` por un `tap` en la demo y mira qué devuelve cada uno.
//  2. Cuenta cuántos viernes 13 hay en 2026 con un rango de fechas y un `findAll`.
//  3. Busca en `metaClass.methods` de List un método que no conozcas y averigua qué hace.
//  4. Reescribe `unMapaConDefecto()` con `countBy` y compara.
