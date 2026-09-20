package com.alejandro.c05truth

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 05 · Groovy Truth y control de flujo
 *
 * Dos ideas que se apoyan la una en la otra: **cualquier objeto puede usarse como
 * condición**, y **el `switch` clasifica en lugar de comparar**. Las dos salen del
 * mismo sitio: un método que cada tipo implementa a su manera (`asBoolean` para la
 * verdad, `isCase` para el switch).
 *
 * ## La tabla que hay que memorizar
 * ```
 * falso:  null · false · 0 · "" · [] · [:] · Matcher sin coincidencia
 * cierto: todo lo demás, incluidos " ", "false", [0] y [null]
 * ```
 *
 * ## Qué se cubre
 * - Qué es "cierto" en Groovy, y el `asBoolean()` que hay detrás.
 * - Definir la verdad de tus propias clases, y cuándo NO hacerlo.
 * - Dónde se aplica el Groovy Truth, y la trampa del 0 legítimo.
 * - Las dos sintaxis de `switch`, y el `switch` como expresión con `yield`.
 * - Lo que puede ir en un `case`: valor, rango, lista, clase, regex o closure.
 * - `isCase()` propio, que de regalo te da el operador `in`.
 * - `for in`, `each`, `times`, `upto`, y por qué desde un `each` no se puede `break`.
 * - Etiquetas, y por qué casi siempre sobran.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`"false"` es cierto y `[0]` también**: lo que se mira es si hay contenido.
 * - **El Groovy Truth responde "¿hay algo?", no "¿tiene valor?".** Si el 0, la cadena
 *   vacía o la lista vacía son datos legítimos, compara explícitamente con `null`.
 * - **`switch` llama a `caso.isCase(valor)`**, y por eso admite rangos, clases y
 *   expresiones regulares. Gana el primer caso que encaja: ordena de específico a
 *   general.
 * - **La sintaxis con flecha no cae al siguiente caso** y además devuelve valor.
 * - **Desde un `each` no se puede `break`.** Si necesitas cortar, usa `for` o el
 *   método del GDK que ya corta (`find`, `any`, `takeWhile`).
 *
 * ## Errores típicos
 * - `if (contador)` con un contador que puede valer 0.
 * - Olvidar el `break` en la sintaxis clásica del switch.
 * - Un `each` con una variable acumuladora donde hacía falta un `collect`.
 *
 * Siguiente paso: capítulo 06, closures.
 */
@CompileStatic
class C05_00Index {

    static final Chapter CHAPTER = chapter(
        5,
        'Groovy Truth y control',
        'qué es cierto, asBoolean, el switch que clasifica, bucles y por qué each no corta',
    ) {
        demo('La tabla del Groovy Truth') { C05_01GroovyTruth.demoTheTable() }
        demo('El mecanismo: asBoolean()') { C05_01GroovyTruth.demoAsBoolean() }
        demo('La verdad de tus propias clases') { C05_01GroovyTruth.demoCustomTruth() }
        demo('Dónde se aplica, y la trampa del 0') { C05_01GroovyTruth.demoWhereItApplies() }
        demo('Las dos sintaxis de switch') { C05_02Switch.demoTwoSyntaxes() }
        demo('Lo que puede ir en un case') { C05_02Switch.demoWhatCanBeACase() }
        demo('isCase propio: tu clase en un switch') { C05_02Switch.demoCustomIsCase() }
        demo('Las formas de recorrer') { C05_03Loops.demoForms() }
        demo('break, continue y por qué each no corta') { C05_03Loops.demoBreakAndContinue() }
        demo('while, do/while y etiquetas') { C05_03Loops.demoWhileAndLabels() }
    }

    /** Ejecuta el capítulo 05 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
