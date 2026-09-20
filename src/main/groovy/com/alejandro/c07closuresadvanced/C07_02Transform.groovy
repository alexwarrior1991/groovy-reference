package com.alejandro.c07closuresadvanced

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  07.2 · curry, composición, memoize y trampoline
//
//  QUÉ ES
//    Un closure es un objeto, y por eso se le pueden aplicar transformaciones que
//    devuelven OTRO closure:
//      curry(...)      fija argumentos por la izquierda
//      >> y <<         encadena dos closures
//      memoize()       cachea los resultados
//      trampoline()    convierte la recursión en un bucle
//
//  POR QUÉ IMPORTA
//    Son cuatro herramientas que, bien usadas, quitan mucho código repetido: un
//    configurador que se reutiliza, una tubería de transformaciones, una función cara
//    que no se recalcula y una recursión que no revienta la pila.
//
//  ERRORES COMUNES
//    · Confundir el orden de `>>` y `<<`.
//    · Usar `memoize()` con argumentos mutables o con un closure que tiene efectos.
//    · Olvidar que `memoize()` sin límite es una fuga de memoria esperando a ocurrir.
// =====================================================================================

class C07_02Transform {

    /**
     * Currificación: fijar argumentos.
     */
    static void demoCurry() {
        section('`curry` fija argumentos por la IZQUIERDA')

        def saludar = { String saludo, String nombre -> "$saludo, $nombre" }
        def hola = saludar.curry('Hola')

        show('saludar("Hola", "Ana")', saludar('Hola', 'Ana'))
        show('hola("Ana")', hola('Ana'))
        show('hola("Luis")', hola('Luis'))
        bullet('`hola` es un closure nuevo que ya tiene el primer argumento puesto.')

        section('`rcurry` fija por la DERECHA')

        def dividir = { int a, int b -> a / b }
        def entreDos = dividir.rcurry(2)
        show('dividir(10, 2)', dividir(10, 2))
        show('entreDos(10)', entreDos(10))
        bullet('Útil cuando lo que se repite es el ÚLTIMO argumento.')

        section('`ncurry` fija el que tú digas')

        def entre = { String a, String b, String c -> "$a-$b-$c" }
        def conMedioFijo = entre.ncurry(1, 'MEDIO')
        show('conMedioFijo("A", "C")', conMedioFijo('A', 'C'))
        bullet('El índice empieza en 0.')

        section('Su uso real: configurar una vez y reutilizar')

        def formatear = { String moneda, int decimales, BigDecimal cantidad ->
            "${cantidad.setScale(decimales, java.math.RoundingMode.HALF_UP)} $moneda"
        }
        def enEuros = formatear.curry('€', 2)

        show('enEuros(19.999)', enEuros(19.999))
        show('enEuros(5)', enEuros(5))
        bullet('Sin curry harías un método con tres parámetros y lo llamarías tres')
        bullet('veces repitiendo los dos primeros.')

        section('Se puede currificar por pasos')

        show('formatear.curry("$").curry(0)(1234.5)', formatear.curry('$').curry(0)(1234.5))
    }

    /**
     * Composición.
     */
    static void demoComposition() {
        section('`>>` encadena: primero el de la izquierda')

        def doble = { int x -> x * 2 }
        def masUno = { int x -> x + 1 }

        def dobleYLuegoMasUno = doble >> masUno
        show('(doble >> masUno)(5)', dobleYLuegoMasUno(5))
        bullet('5 → doble → 10 → masUno → 11. Se lee de izquierda a derecha.')

        section('`<<` va al revés')

        def masUnoYLuegoDoble = doble << masUno
        show('(doble << masUno)(5)', masUnoYLuegoDoble(5))
        bullet('5 → masUno → 6 → doble → 12. Es la composición matemática (f∘g).')
        bullet('Regla: `>>` es una TUBERÍA; `<<` se lee "aplica doble DE masUno".')

        section('Encadenar varios: una tubería de limpieza')

        def quitarEspacios = { String s -> s.trim() }
        def enMinusculas = { String s -> s.toLowerCase() }
        def quitarAcentos = { String s ->
            java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll(/\p{InCombiningDiacriticalMarks}+/, '')
        }

        def normalizar = quitarEspacios >> enMinusculas >> quitarAcentos
        show('normalizar("  ÁlvarO  ")', normalizar('  ÁlvarO  '))
        bullet('Cada paso se prueba por separado y la tubería se lee como una receta.')

        section('Y se puede construir dinámicamente')

        List<Closure<String>> pasos = [quitarEspacios, enMinusculas]
        Closure<String> tuberia = pasos.inject { a, b -> a >> b } as Closure<String>
        show('compuesta con inject', tuberia('  HOLA  '))
    }

    /**
     * Memoización.
     */
    static void demoMemoize() {
        section('`memoize()` cachea por argumentos')

        int llamadas = 0
        def caro = { int n ->
            llamadas++
            (1..n).sum()
        }
        def cacheado = caro.memoize()

        show('primera llamada con 100', cacheado(100))
        show('segunda llamada con 100', cacheado(100))
        show('veces que se ejecutó el cuerpo', llamadas)
        bullet('La segunda vez ni entra: devuelve lo guardado.')

        show('con otro argumento', cacheado(10))
        show('llamadas ahora', llamadas)

        section('Fibonacci: el ejemplo clásico')

        show('fib(30) memoizado', fibonacci(30))
        bullet('Sin memoize, fib(30) hace más de un millón de llamadas.')
        bullet('Con memoize, treinta.')

        section('Las variantes con límite')

        bullet('memoize()              sin límite: cuidado con la memoria')
        bullet('memoizeAtMost(n)       guarda como mucho n resultados')
        bullet('memoizeAtLeast(n)      garantiza n, el resto se puede liberar')
        bullet('memoizeBetween(a, b)   un mínimo garantizado y un máximo')

        def limitado = { it * 2 }.memoizeAtMost(2)
        show('memoizeAtMost(2)', [limitado(1), limitado(2), limitado(3)])

        section('Cuándo NO memoizar')

        bullet('Si el closure tiene efectos secundarios: dejarán de ocurrir.')
        bullet('Si los argumentos son mutables: la caché usa equals/hashCode.')
        bullet('Si el resultado depende del tiempo o del estado de fuera.')
        bullet('Y nunca `memoize()` sin límite sobre argumentos que no se repiten.')
    }

    /** Fibonacci con memoización. El closure se refiere a sí mismo, de ahí las dos líneas. */
    private static BigInteger fibonacci(int n) {
        Closure<BigInteger> fib
        fib = { int k -> k < 2 ? (k as BigInteger) : fib(k - 1) + fib(k - 2) }
        fib = fib.memoize()
        fib(n)
    }

    /**
     * Trampolín: recursión sin desbordar la pila.
     */
    static void demoTrampoline() {
        section('El problema: la recursión profunda revienta la pila')

        show('suma recursiva con 100', sumaRecursiva(100, 0))
        try {
            show('con 100.000', sumaRecursiva(100_000, 0))
        } catch (StackOverflowError e) {
            show('con 100.000', 'StackOverflowError')
        }
        bullet('Cada llamada ocupa un marco de pila, y la pila se acaba.')

        section('`trampoline()` la convierte en un bucle')

        show('con trampolín, 100.000', sumaConTrampolin(100_000))
        bullet('En vez de llamarse, el closure DEVUELVE la siguiente llamada, y')
        bullet('el trampolín la ejecuta en un bucle. La pila no crece.')

        section('Cómo se escribe')

        bullet('1. El closure devuelve `suSiguiente.trampoline(args)` en vez de llamarse.')
        bullet('2. Se cierra con `.trampoline()` al crearlo.')
        bullet('3. Hace falta acumulador: tiene que ser recursión de cola.')

        section('Cuándo usarlo')

        bullet('Sólo cuando la profundidad depende de los datos y puede ser enorme.')
        bullet('Para casi todo, un bucle normal se lee mejor y es igual de rápido.')

        show('(1..100_000).sum()', (1..100_000).sum())
        bullet('¡Ojo! Ese número NO es el mismo. `sum()` sobre un rango de enteros')
        bullet('suma con aritmética de 32 bits y DESBORDA (capítulo 02).')
        show('(1..100_000).sum(0G)', (1..100_000).sum(0G))
        bullet('Con un acumulador BigInteger ya coincide con el del trampolín.')
    }

    private static BigInteger sumaRecursiva(int n, BigInteger acumulado) {
        n == 0 ? acumulado : sumaRecursiva(n - 1, acumulado + n)
    }

    private static BigInteger sumaConTrampolin(int n) {
        Closure<Object> suma
        suma = { int k, BigInteger acumulado ->
            k == 0 ? acumulado : suma.trampoline(k - 1, acumulado + k)
        }
        suma = suma.trampoline()
        suma(n, 0G) as BigInteger
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `doble >> masUno` por `masUno >> doble` y predice el resultado antes de
//     ejecutarlo.
//  2. Quita el `.memoize()` de `fibonacci` y prueba con 30: cuenta cuánto tarda.
//  3. Memoiza un closure que imprima algo y llámalo dos veces con el mismo argumento.
//  4. Quita el `.trampoline()` de `sumaConTrampolin` y mira qué error sale.
//  5. Currifica `formatear` por la derecha con `rcurry` y compara qué se puede fijar.
