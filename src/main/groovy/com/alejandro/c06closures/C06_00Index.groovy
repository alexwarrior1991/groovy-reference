package com.alejandro.c06closures

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 06 · Closures
 *
 * Un closure es un bloque de código que además es un **objeto**: se guarda, se pasa, se
 * devuelve y se llama. Es la pieza sobre la que están construidos las colecciones, los
 * builders, los DSL, Gradle y Jenkins.
 *
 * ## El mecanismo entero, en dos líneas
 * ```
 * def doble = { it * 2 }     // un objeto de clase Closure
 * doble(5)                   // azúcar para doble.call(5)
 * ```
 *
 * ## Qué se cubre
 * - `it`, la diferencia entre `{ }` y `{ -> }`, y cuántos parámetros acepta cada uno.
 * - Parámetros tipados, por defecto y variables.
 * - La regla de la última posición, que es lo que hace que `lista.each { }` parezca
 *   sintaxis del lenguaje.
 * - `return` dentro de un closure, y por qué no corta el método de fuera.
 * - Closures que devuelven closures: fábricas y estado sin clase.
 * - Qué captura un cierre —la variable, no el valor— y la trampa del bucle.
 * - Method pointers `.&` y `::`.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`{ }` acepta un argumento aunque no lo uses; `{ -> }` no acepta ninguno.**
 * - **Un closure captura la variable, no su valor.** Ve los cambios posteriores y
 *   puede modificarla. (Un GString hace justo lo contrario: congela el valor.)
 * - **Guardar closures dentro de un `for` los hace compartir la variable**, y eso vale
 *   tanto para el `for` clásico como para `for (x in …)`. Con `each` no pasa, porque
 *   lo que se captura es un parámetro.
 * - **`return` sale del closure, no del método.** Es un `continue`, no un `break`.
 * - El GDK mira `maximumNumberOfParameters` para decidir qué pasarte: por eso `each`
 *   sobre un mapa te da la entrada o la pareja clave/valor según lo que declares.
 *
 * ## Errores típicos
 * - Un `each` con una variable acumuladora donde hacía falta `collect` o `find`.
 * - Closures de veinte líneas: eso es un método con nombre.
 * - Guardar un closure que captura un objeto grande y provocar una fuga.
 *
 * Siguiente paso: capítulo 07, delegate, curry y memoize.
 */
@CompileStatic
class C06_00Index {

    static final Chapter CHAPTER = chapter(
        6,
        'Closures',
        'it, parámetros, la última posición, el cierre y la trampa del bucle',
    ) {
        demo('`it` y la diferencia entre { } y { -> }') { C06_01Basics.demoItParameter() }
        demo('Parámetros: tipos, defectos y varargs') { C06_01Basics.demoParameters() }
        demo('Closures como parámetros de método') { C06_01Basics.demoAsParameters() }
        demo('`return` dentro de un closure') { C06_01Basics.demoReturn() }
        demo('Closures que devuelven closures') { C06_01Basics.demoReturningClosures() }
        demo('Qué captura un cierre') { C06_02Capture.demoWhatIsCaptured() }
        demo('La trampa del bucle') { C06_02Capture.demoLoopTrap() }
        demo('Method pointers .& y ::') { C06_02Capture.demoMethodPointers() }
        demo('Closures como valores y predicados') { C06_02Capture.demoAsValues() }
    }

    /** Ejecuta el capítulo 06 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
