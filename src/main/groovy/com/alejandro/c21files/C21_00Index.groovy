package com.alejandro.c21files

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 21 · Ficheros e IO
 *
 * Leer un fichero entero es `fichero.text`. Escribirlo, `fichero.text = '…'`. Lo que
 * en Java son diez líneas con try-with-resources, aquí es una.
 *
 * ## Seguridad de las demos
 * Todo este capítulo trabaja en un directorio temporal que se crea al empezar cada
 * demo y se borra al terminar. Ninguna toca tu proyecto ni tu disco fuera de ahí.
 *
 * ## Qué se cubre
 * - `.text`, `.bytes`, `readLines()`, `<<` y la codificación.
 * - Leer sin cargarlo todo: `eachLine`, `withReader`, `splitEachLine`, `filterLine`.
 * - `Path` con los mismos métodos, gracias a `groovy-nio`.
 * - Recorrer árboles con `eachFileRecurse` y `traverse` con filtros.
 * - Un CSV completo, con línea en blanco, dato que falta y dato corrupto, y las
 *   decisiones de diseño de cada caso.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`.text` carga el fichero entero en memoria.** Con algo grande, `eachLine`.
 * - **Di la codificación explícitamente.** `.text` usa la de la plataforma, y ahí es
 *   donde los acentos se rompen. En la demo se ve el mismo fichero leído bien y mal.
 * - **`withReader`/`withWriter` cierran el recurso pase lo que pase.** Es la versión
 *   especializada de `withCloseable` (capítulo 18).
 * - **`fichero << texto` añade; `fichero.text = texto` sustituye.**
 * - En el parseo, los problemas viajan como DATOS junto al resultado, no como una
 *   excepción que aborta el fichero entero.
 * - Y partir por comas no es parsear CSV: en cuanto haya comillas, usa una librería.
 *
 * Siguiente paso: capítulo 22, JSON.
 */
@CompileStatic
class C21_00Index {

    static final Chapter CHAPTER = chapter(
        21,
        'Ficheros e IO',
        '.text, eachLine, withReader, traverse y un CSV completo con sus casos límite',
    ) {
        demo('Leer y escribir') { C21_01Files.demoReadWrite() }
        demo('Leer sin cargarlo todo en memoria') { C21_01Files.demoStreaming() }
        demo('Path, directorios y traverse') { C21_01Files.demoPathsAndDirectories() }
        demo('Un CSV completo, con sus casos límite') { C21_01Files.demoCsvCompleto() }
    }

    /** Ejecuta el capítulo 21 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
