package com.alejandro.c04operators

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  04.3 · Los tres spread: `*.`, `*` y `*:`
//
//  QUÉ ES
//    Tres operadores distintos que comparten el asterisco y que la gente confunde:
//      lista*.metodo()   aplica el método a CADA elemento y devuelve la lista
//      f(*lista)         desparrama la lista como ARGUMENTOS de la llamada
//      [*:mapa, x: 1]    desparrama un mapa dentro de otro literal de mapa
//
//  POR QUÉ IMPORTA
//    `*.` es de lo más usado del lenguaje y hace muy legible el código de colecciones.
//    Los otros dos son la forma idiomática de reenviar argumentos y de combinar
//    configuraciones.
//
//  ERRORES COMUNES
//    · Usar `*.` esperando un `collect` con closure: `*.` sólo llama a UN método.
//    · Olvidar que `*.` aplica navegación segura y deja nulos en el resultado.
//    · Confundir `[*:a, *:b]` con `a + b` (hacen lo mismo, pero uno es literal).
// =====================================================================================

class C04_03Spread {

    /**
     * El spread de propiedades y métodos.
     */
    static void demoSpreadDot() {
        section('Aplicar un método a cada elemento')

        def palabras = ['groovy', 'java', 'kotlin']
        show('palabras*.size()', palabras*.size())
        show('palabras*.toUpperCase()', palabras*.toUpperCase())
        bullet('Es `collect { it.metodo() }`, pero más corto cuando no hay nada más.')

        section('Y una propiedad a cada elemento')

        def usuarios = [[nombre: 'Ana', edad: 30], [nombre: 'Luis', edad: 25]]
        show('usuarios*.nombre', usuarios*.nombre)
        show('usuarios*.edad', usuarios*.edad)
        bullet('Sobre objetos llama al getter; sobre mapas, busca la clave.')

        section('Se puede encadenar')

        show('usuarios*.nombre*.toUpperCase()', usuarios*.nombre*.toUpperCase())

        section('Aplica navegación segura: los nulos pasan como null')

        show("['a', null, 'ccc']*.size()", ['a', null, 'ccc']*.size())
        bullet('No lanza. Si no quieres los nulos, filtra antes o después.')
        show('… y limpiando', ['a', null, 'ccc']*.size().grep())

        section('Cuándo NO sirve: cuando hace falta una expresión')

        show('con collect', palabras.collect { it.size() * 2 })
        bullet('`*.` sólo aplica UN método sin argumentos extra. Para lo demás, collect.')

        show('con argumentos SÍ funciona', palabras*.take(3))
        bullet('Eso sí: el método puede llevar argumentos.')
    }

    /**
     * El spread de argumentos.
     */
    static void demoSpreadArguments() {
        section('Desparramar una lista como argumentos')

        def datos = [3, 4]
        show('distancia(*datos)', distancia(*datos))
        show('distancia(3, 4)', distancia(3, 4))
        bullet('`*lista` convierte los elementos en argumentos posicionales.')

        section('Mezclado con argumentos normales')

        show('concatenar("inicio", *["a", "b"])', concatenar('inicio', *['a', 'b']))

        section('Su uso real: reenviar argumentos')

        show('reenviando', invocarConLog('suma', [2, 3]))
        bullet('Sin el spread habría que escribir un caso por número de argumentos.')

        section('Con varargs')

        show('juntar(*["x", "y", "z"])', juntar(*['x', 'y', 'z']))
        show('juntar("x", "y", "z")', juntar('x', 'y', 'z'))
    }

    private static double distancia(int x, int y) { Math.sqrt((x * x + y * y) as double) }

    private static String concatenar(String inicio, String a, String b) { "$inicio-$a-$b" }

    private static String juntar(String... partes) { partes.join('+') }

    private static Object invocarConLog(String operacion, List argumentos) {
        // Un despachador diminuto: el spread evita escribir un caso por aridad.
        Closure<?> accion = [suma: { a, b -> a + b }, resta: { a, b -> a - b }][operacion]
        "$operacion${argumentos} = ${accion(*argumentos)}"
    }

    /**
     * El spread de mapas.
     */
    static void demoSpreadMap() {
        section('Desparramar un mapa dentro de otro literal')

        def porDefecto = [host: 'localhost', puerto: 8080, debug: false]
        def config = [*: porDefecto, puerto: 9090]

        show('por defecto', porDefecto)
        show('[*:porDefecto, puerto: 9090]', config)
        bullet('Lo de la derecha PISA a lo de la izquierda. El orden importa.')

        show('[puerto: 9090, *:porDefecto]', [puerto: 9090, *: porDefecto])
        bullet('Al revés, el valor por defecto gana: casi nunca es lo que quieres.')

        section('Es casi lo mismo que `+`, pero se lee mejor en un literal')

        show('porDefecto + [puerto: 9090]', porDefecto + [puerto: 9090])
        bullet('`+` devuelve un mapa nuevo; ninguno de los dos muta los originales.')
        show('porDefecto no ha cambiado', porDefecto)

        section('Combinar varios')

        def delEntorno = [debug: true]
        def deLaLinea = [puerto: 7070]
        show('tres niveles', [*: porDefecto, *: delEntorno, *: deLaLinea])
        bullet('Es el patrón de configuración por capas, en una línea.')

        section('Como argumentos con nombre de un método')

        show('crear(*:[nombre: "Ana", edad: 30])', crear(*: [nombre: 'Ana', edad: 30]))
        bullet('Los argumentos con nombre de Groovy son un mapa. Capítulo 10.')
    }

    private static String crear(Map datos) { "${datos.nombre} (${datos.edad})" }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `usuarios*.nombre` por `usuarios.collect { it.nombre }`: mismo resultado.
//  2. Prueba `palabras*.size() * 2` y explica por qué no hace lo que parece.
//  3. Dale la vuelta al orden de `[*:porDefecto, puerto: 9090]` y comprueba quién gana.
//  4. Llama a `distancia(*[3, 4, 5])` y lee el error.
