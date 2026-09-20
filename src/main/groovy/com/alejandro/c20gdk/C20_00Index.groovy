package com.alejandro.c20gdk

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 20 · El GDK
 *
 * El Groovy Development Kit: cientos de métodos añadidos a las clases de Java.
 * `'abc'.capitalize()`, `[1,2].sum()`, `10.times { }` y `fichero.text` no están en
 * Java: los añade Groovy con el mecanismo de módulos de extensión del capítulo 17.
 *
 * ## Los dos que más se confunden
 * ```
 * obj.with { … }   devuelve EL BLOQUE   → para CALCULAR a partir del objeto
 * obj.tap { … }    devuelve EL OBJETO   → para CONFIGURARLO y quedártelo
 * ```
 *
 * ## Qué se cubre
 * - `with`, `tap`, `identity`, `dump`, `inspect` y el `sleep` sin excepción.
 * - Números: `times`, `upto`, `step`, `intdiv`, `round(n)`, `power`.
 * - Fechas de `java.time` con operadores, rangos y `upto`.
 * - Colecciones: estadística, `intersect`, `combinations`, `collate`, `withIndex`.
 * - Cómo ENCONTRAR el método que necesitas preguntándole al propio objeto.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`with` devuelve el bloque y `tap` devuelve el objeto.** Es la única diferencia,
 *   y decide cuál usar.
 * - **`fecha + 1` suma un día** y un rango de fechas es una lista sobre la que
 *   `findAll` funciona.
 * - **Antes de escribir un bucle sobre una colección, busca el método.** Si tu `for`
 *   ocupa tres líneas, casi seguro que hay uno que lo dice en una.
 * - **La documentación del GDK está aparte** de la de Java:
 *   `docs.groovy-lang.org/latest/html/groovy-jdk/`.
 * - Y siempre puedes preguntarle al objeto: `obj.metaClass.methods*.name`.
 *
 * Siguiente paso: capítulo 21, ficheros e IO.
 */
@CompileStatic
class C20_00Index {

    static final Chapter CHAPTER = chapter(
        20,
        'El GDK',
        'with y tap, números, fechas de java.time, colecciones y cómo encontrar métodos',
    ) {
        demo('Object: with, tap y compañía') { C20_01Gdk.demoObject() }
        demo('Números') { C20_01Gdk.demoNumbers() }
        demo('Fechas de java.time') { C20_01Gdk.demoDates() }
        demo('Colecciones: lo que faltaba por ver') { C20_01Gdk.demoCollections() }
        demo('Cómo encontrar el método que buscas') { C20_01Gdk.demoDiscovering() }
    }

    /** Ejecuta el capítulo 20 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
