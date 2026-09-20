package com.alejandro.c24dsl

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 24 · Builders y DSLs
 *
 * Un DSL en Groovy no es una extensión del lenguaje: es código Groovy normal que,
 * gracias al `delegate` de los closures (capítulo 07), se lee como si fuera otra cosa.
 *
 * ## El mecanismo entero, en tres líneas
 * ```
 * bloque.delegate = elBuilder
 * bloque.resolveStrategy = Closure.DELEGATE_FIRST
 * bloque()
 * ```
 * Gradle, Jenkins, `MarkupBuilder`, `JsonBuilder` y el `chapter { }` de este
 * repositorio son todos eso.
 *
 * ## Qué se cubre
 * - El camino completo: función posicional → argumentos con nombre → DSL, con la
 *   parada intermedia que resuelve la mayoría de los casos.
 * - Las cinco reglas de un builder decente.
 * - **La fuga de ámbito** en un DSL anidado, y cómo cerrarla con `DELEGATE_ONLY`.
 * - Los builders que ya trae Groovy, y `NodeBuilder`.
 * - Plantillas, para cuando el resultado es texto y no una estructura.
 * - Cuándo merece la pena y cuánto cuesta.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Antes de escribir un DSL, prueba con argumentos con nombre.** Resuelven la
 *   mayoría de los casos con cero código.
 * - **Un DSL gana cuando hay anidamiento o elementos repetidos.**
 * - **El builder es mutable; el resultado es inmutable.** Nunca devuelvas el builder.
 * - **La validación va en `construir()`**, no en cada asignación: así el orden de las
 *   líneas del bloque no importa.
 * - **En cuanto haya dos niveles, cierra el ámbito con `DELEGATE_ONLY`.** Sin eso,
 *   una llamada escrita en el bloque de dentro puede cambiar el objeto de fuera, y
 *   compila sin decir nada. En la demo se ve pasar.
 * - **Una clase por tipo de bloque**, no una genérica: es lo que hace que `metodo`
 *   sólo exista dentro de `ruta { }`.
 *
 * ## El precio
 * Un DSL en código obliga a recompilar para cambiar un valor. Si la configuración la
 * edita otra persona o cambia por entorno, un fichero externo sigue siendo la
 * respuesta correcta.
 *
 * Siguiente paso: capítulo 25, scripts y Binding.
 */
@CompileStatic
class C24_00Index {

    static final Chapter CHAPTER = chapter(
        24,
        'Builders y DSLs',
        'de los argumentos con nombre al DSL, la fuga de ámbito, builders y plantillas',
    ) {
        demo('De una función con parámetros a un DSL') { C24_01Dsl.demoStepByStep() }
        demo('Las cinco reglas de un builder decente') { C24_01Dsl.demoBuilderRules() }
        demo('La fuga de ámbito, y cómo cerrarla') { C24_01Dsl.demoScopeLeak() }
        demo('Otros builders y las plantillas') { C24_01Dsl.demoOtherBuilders() }
        demo('Cuándo merece la pena un DSL') { C24_01Dsl.demoWhenWorthIt() }
    }

    /** Ejecuta el capítulo 24 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
