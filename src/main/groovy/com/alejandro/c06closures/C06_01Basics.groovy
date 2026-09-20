package com.alejandro.c06closures

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  06.1 · Qué es un closure
//
//  QUÉ ES
//    Un bloque de código entre llaves que es un OBJETO: se guarda en una variable, se
//    pasa como argumento, se devuelve desde un método y se llama cuando haga falta.
//    Su clase es `groovy.lang.Closure`.
//
//  POR QUÉ IMPORTA
//    Es la pieza central de Groovy. Las colecciones, los builders, los DSL, Gradle y
//    Jenkins son todos closures. Si no se entienden, el resto del lenguaje no encaja.
//
//  ERRORES COMUNES
//    · Confundirlos con las lambdas de Java: un closure es un objeto con estado
//      (`delegate`, `owner`, `resolveStrategy`) y se puede modificar.
//    · Escribir `{ -> }` cuando se quería `{ it }`, o al revés.
//    · Poner el closure entre los paréntesis cuando va al final, fuera de ellos.
// =====================================================================================

class C06_01Basics {

    /**
     * La forma mínima y el parámetro implícito.
     */
    static void demoItParameter() {
        section('Un closure sin parámetros declarados recibe `it`')

        def doble = { it * 2 }
        show('doble(5)', doble(5))
        show('su clase', doble.class.superclass.simpleName)

        section('`it` existe SIEMPRE que no declares parámetros')

        def sinUsarlo = { 'no uso it' }
        show('sinUsarlo(99)', sinUsarlo(99))
        bullet('Acepta un argumento aunque no lo use: `it` está declarado igualmente.')

        section('Para uno que NO acepte nada: `{ -> }`')

        def ninguno = { -> 'cero parámetros' }
        show('ninguno()', ninguno())
        try {
            show('ninguno(1)', ninguno(1))
        } catch (Exception e) {
            show('ninguno(1)', e.class.simpleName)
        }
        bullet('La flecha sin nada delante declara CERO parámetros. Es la diferencia.')

        section('Cuántos parámetros acepta')

        show('{ it }.maximumNumberOfParameters', ({ it }).maximumNumberOfParameters)
        show('{ -> }.maximumNumberOfParameters', ({ -> }).maximumNumberOfParameters)
        show('{ a, b -> }.maximumNumberOfParameters', ({ a, b -> }).maximumNumberOfParameters)
        bullet('El GDK lo consulta para decidir qué pasarle. Por eso `each` sobre un')
        bullet('mapa te da la entrada entera o la clave y el valor, según lo que pidas.')

        show('each con un parámetro', conUnParametro([a: 1]))
        show('each con dos', conDosParametros([a: 1]))
    }

    private static List<String> conUnParametro(Map mapa) {
        List<String> salida = []
        mapa.each { entrada -> salida << "${entrada.key}=${entrada.value}" }
        salida
    }

    private static List<String> conDosParametros(Map mapa) {
        List<String> salida = []
        mapa.each { clave, valor -> salida << "$clave=$valor" }
        salida
    }

    /**
     * Parámetros explícitos, tipados y por defecto.
     */
    static void demoParameters() {
        section('Parámetros con nombre')

        def suma = { a, b -> a + b }
        show('suma(2, 3)', suma(2, 3))

        section('Con tipos')

        def concatenar = { String a, String b -> "$a-$b" }
        show('concatenar("x", "y")', concatenar('x', 'y'))
        show('parameterTypes', concatenar.parameterTypes*.simpleName)
        bullet('Los tipos se comprueban al llamar, no al declarar.')

        section('Valores por defecto')

        def saludar = { String nombre, String saludo = 'Hola' -> "$saludo, $nombre" }
        show('saludar("Ana")', saludar('Ana'))
        show('saludar("Ana", "Buenas")', saludar('Ana', 'Buenas'))

        section('Número variable de argumentos')

        def juntar = { String separador, Object... partes -> partes.join(separador) }
        show('juntar("-", "a", "b", "c")', juntar('-', 'a', 'b', 'c'))

        section('Las tres formas de llamarlo')

        show('suma(2, 3)', suma(2, 3))
        show('suma.call(2, 3)', suma.call(2, 3))
        show('suma.curry(2)(3)', suma.curry(2)(3))
        bullet('`suma(2, 3)` es azúcar para `suma.call(2, 3)`: el operador `()` es')
        bullet('`call()`, como cualquier otro operador (capítulo 04).')
    }

    /**
     * Closures como parámetros de método.
     */
    static void demoAsParameters() {
        section('Un método que recibe un closure')

        show('aplicar(5) { it * 3 }', aplicar(5) { it * 3 })
        show('aplicar(5) { it + 100 }', aplicar(5) { it + 100 })

        section('La regla de la última posición')

        bullet('Si el closure es el ÚLTIMO parámetro, va FUERA de los paréntesis.')
        bullet('Es lo que hace que `lista.each { }` parezca sintaxis del lenguaje.')

        show('con paréntesis', aplicar(5, { it * 3 }))
        show('fuera de ellos', aplicar(5) { it * 3 })
        bullet('Idénticos. El segundo es el idiomático.')

        section('Y si es el único parámetro, los paréntesis desaparecen')

        show('repetir { }', repetir { 'x' })

        section('Dos closures: el segundo también puede salirse')

        show('intentar/siFalla', intentar({ throw new IllegalStateException('roto') }) { "capturado: ${it.message}" })

        section('Medir cuánto tarda algo: el patrón más común')

        show('cronometrar { }', cronometrar { (1..1000).sum() }.keySet())
        bullet('Envolver un bloque para añadirle comportamiento alrededor es EL uso')
        bullet('de los closures: transacciones, reintentos, trazas, ficheros.')
    }

    private static Object aplicar(Object valor, Closure<?> transformacion) {
        transformacion(valor)
    }

    private static List<String> repetir(Closure<String> bloque) {
        (1..3).collect { bloque() }
    }

    private static String intentar(Closure<?> bloque, Closure<String> siFalla) {
        try {
            bloque().toString()
        } catch (Exception e) {
            siFalla(e)
        }
    }

    private static Map<String, Object> cronometrar(Closure<?> bloque) {
        long t0 = System.nanoTime()
        Object resultado = bloque()
        ['resultado': resultado, 'nanos': System.nanoTime() - t0]
    }

    /**
     * `return` dentro de un closure.
     */
    static void demoReturn() {
        section('`return` sale del CLOSURE, no del método que lo llama')

        show('buscarPrimerPar([1, 3, 4, 6])', buscarPrimerPar([1, 3, 4, 6]))
        bullet('El `return` de dentro del `each` sólo termina esa vuelta.')
        bullet('Por eso hace falta la variable, y por eso `find` es mejor idea.')

        show('con find', [1, 3, 4, 6].find { it % 2 == 0 })

        section('Y como en los métodos, el `return` es opcional')

        def conReturn = { int x -> return x * 2 }
        def sinReturn = { int x -> x * 2 }
        show('con return', conReturn(4))
        show('sin return', sinReturn(4))

        section('Cuidado con la última expresión "accidental"')

        def pareceVoid = { List<Integer> lista -> lista.each { } }
        show('devuelve la lista sin querer', pareceVoid([1, 2]))
        bullet('`each` devuelve la colección, así que el closure la devuelve también.')
        bullet('Si el closure es un efecto secundario, decláralo `Closure<Void>` o')
        bullet('termina con algo explícito.')
    }

    private static Integer buscarPrimerPar(List<Integer> numeros) {
        Integer encontrado = null
        numeros.each {
            if (encontrado == null && it % 2 == 0) {
                encontrado = it
                return           // sólo termina esta vuelta
            }
        }
        encontrado
    }

    /**
     * Closures que devuelven closures.
     */
    static void demoReturningClosures() {
        section('Una fábrica de closures')

        def multiplicadorPor = { int factor -> { int x -> x * factor } }

        def doble = multiplicadorPor(2)
        def triple = multiplicadorPor(3)

        show('doble(5)', doble(5))
        show('triple(5)', triple(5))
        bullet('Cada uno recuerda SU `factor`. Eso es el cierre (demo 06.7).')

        section('Un contador con estado')

        def contador = crearContador()
        show('primera llamada', contador())
        show('segunda', contador())
        show('tercera', contador())
        bullet('El estado vive en la variable capturada, no en un campo.')

        section('Dos contadores son independientes')

        def a = crearContador()
        def b = crearContador()
        a(); a()
        show('a', a())
        show('b', b())

        section('Para qué sirve esto de verdad')

        def validarLongitud = validadorDeLongitud(3)
        show('validar("ab")', validarLongitud('ab'))
        show('validar("abcd")', validarLongitud('abcd'))
        bullet('Configuras una vez y reutilizas. Es la alternativa a una clase con')
        bullet('un solo método y un campo.')
    }

    private static Closure<Integer> crearContador() {
        int cuenta = 0
        return { -> ++cuenta }
    }

    private static Closure<String> validadorDeLongitud(int minimo) {
        return { String texto ->
            texto.length() >= minimo ? 'válido' : "necesita $minimo caracteres"
        }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `{ -> 'x' }` por `{ 'x' }` y llama al closure con un argumento.
//  2. Escribe un `each` sobre un mapa con TRES parámetros y lee el error.
//  3. Haz que `crearContador()` devuelva dos closures (incrementar y leer) que
//     compartan la misma variable.
//  4. Pasa el closure DENTRO de los paréntesis en `repetir` y comprueba que funciona.
