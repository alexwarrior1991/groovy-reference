package com.alejandro.infra

import groovy.transform.CompileStatic

// =====================================================================================
//  El registro de demos del repositorio.
//
//  Cada capítulo declara su lista de demos con el builder `chapter { ... }` y el
//  lanzador (Launcher.groovy) las agrega todas. Si te fijas, este fichero es en sí mismo
//  un ejemplo de "closure con delegate" y de DSL: exactamente lo que se explica en los
//  capítulos 07 y 24. Cuando llegues allí, vuelve a leerlo.
//
//  (Un fichero .groovy puede declarar varias clases: es lo natural cuando son piezas de
//  la misma idea. Sólo la que se llame como el fichero puede ser `public`.)
// =====================================================================================

/**
 * Una demo ejecutable: el trozo de código más pequeño que se puede lanzar por separado.
 *
 * `id` es un identificador estable con forma `"13.4"` (capítulo . posición). Lo genera
 * [ChapterBuilder]; nunca se escribe a mano, y por eso no puede duplicarse.
 *
 * `skipInSmokeTest` marca las demos que el test de humo no debe ejecutar (por ejemplo,
 * una que dependa del entorno). Deberían ser poquísimas.
 */
@CompileStatic
class Demo {
    String id
    String title
    boolean skipInSmokeTest
    Closure<?> action
}

/**
 * Un capítulo del recorrido: un tema con sus demos.
 */
@CompileStatic
class Chapter {
    int number
    String name
    String summary
    List<Demo> demos

    /** `"07"`: el número con dos dígitos, para que el índice quede alineado. */
    String getId() { number.toString().padLeft(2, '0') }

    /** Busca una demo por su identificador completo (`"13.4"`). */
    Demo find(String demoId) { demos.find { Demo d -> d.id == demoId } }

    /** Ejecuta todas las demos del capítulo, en orden. */
    void runAll() {
        Console.heading("Capítulo $id · $name")
        println "  $summary"
        demos.each { Demo d -> Registry.runDemo(d) }
    }
}

/**
 * Constructor de capítulos.
 *
 * Se instancia sólo desde [Registry.chapter], nunca directamente.
 */
@CompileStatic
class ChapterBuilder {

    private final int number
    private final List<Demo> demos = []

    ChapterBuilder(int number) { this.number = number }

    /**
     * Registra una demo del capítulo.
     *
     * El identificador se calcula solo a partir de la posición: `"13.1"`, `"13.2"`…
     * Escribirlos a mano en treinta ficheros índice sería una fuente segura de
     * duplicados.
     */
    void demo(String title, Closure<?> action, boolean skipInSmokeTest = false) {
        demos << new Demo(
            id: "${number}.${demos.size() + 1}",
            title: title,
            skipInSmokeTest: skipInSmokeTest,
            action: action,
        )
    }

    Chapter build(String name, String summary) {
        new Chapter(number: number, name: name, summary: summary, demos: demos.asImmutable())
    }
}

/** El punto de entrada del registro. Se importa estáticamente desde cada índice. */
@CompileStatic
class Registry {

    /**
     * Declara un capítulo.
     *
     * ```
     * static final Chapter CHAPTER = chapter(13, 'Colecciones', 'List, Set y Map') {
     *     demo('Creación y tipos') { C13_01Creacion.demoCreation() }
     * }
     * ```
     *
     * El `@DelegatesTo(ChapterBuilder)` es lo que hace que dentro de las llaves se pueda
     * llamar a `demo(...)` sin prefijo: le dice al compilador (y al IDE) quién va a
     * resolver esos nombres. Sin esa anotación el código sigue funcionando en dinámico,
     * pero `@CompileStatic` lo rechaza y el autocompletado desaparece. Capítulo 24.
     */
    static Chapter chapter(int number, String name, String summary,
                           @DelegatesTo(ChapterBuilder) Closure body) {
        ChapterBuilder builder = new ChapterBuilder(number)
        body.delegate = builder
        // DELEGATE_FIRST: el delegate manda sobre el owner. Capítulo 07.
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.call()
        builder.build(name, summary)
    }

    /**
     * Ejecuta una demo aislando los fallos.
     *
     * El `try/catch` es importante: con cientos de demos, una sola excepción no
     * controlada abortaría el recorrido completo (`all`) y dejaría el resto sin
     * ejecutar. Aun así, la regla del repositorio es que **ninguna demo debe llegar aquí
     * lanzando**: los ejemplos que enseñan excepciones las capturan ellos mismos.
     */
    static void runDemo(Demo demo) {
        Console.demoHeader(demo.id, demo.title)
        try {
            demo.action.call()
        } catch (Throwable e) {
            println ''
            println "    ✗ La demo ${demo.id} lanzó ${e.class.simpleName}: ${e.message}"
        }
    }
}
