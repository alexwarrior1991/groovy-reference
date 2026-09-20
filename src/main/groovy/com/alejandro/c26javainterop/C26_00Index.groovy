package com.alejandro.c26javainterop

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 26 · Interoperabilidad con Java
 *
 * Groovy compila a bytecode de la JVM, así que la interoperabilidad es total y en las
 * dos direcciones. Lo que muerde no es la técnica: son las diferencias de
 * **semántica** al cruzar la frontera.
 *
 * ## Este capítulo tiene Java de verdad
 * En `src/main/java/com/alejandro/c26javainterop/legacy/` hay tres clases `.java` que
 * compila javac en este mismo build. Una de ellas (`ConsumidorDeGroovy`) llama a una
 * clase Groovy, y es la que obliga al `pom.xml` a generar **stubs**.
 *
 * ## Qué se cubre
 * - Los getters de Java vistos como propiedades desde Groovy.
 * - El GDK funcionando sobre las colecciones que devuelve Java… incluida la que era
 *   la lista interna del objeto.
 * - La coerción SAM: un closure donde Java espera `Predicate`, `Function` o `Runnable`.
 * - Java llamando a Groovy, y qué NO ve (todo lo del capítulo 16).
 * - Cómo compila este proyecto los dos lenguajes juntos, y por qué hacen falta stubs.
 * - La tabla de diferencias de semántica, y el orden en que portar código.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Lo que decide el despacho es el lenguaje del código que HACE LA LLAMADA**, no
 *   el del método llamado. Llamar desde Groovy a un método sobrecargado de Java elige
 *   por el tipo real; la misma llamada escrita dentro de Java elige por el declarado.
 *   La demo enseña los dos resultados sobre el mismo objeto.
 * - **Java NO ve la metaprogramación.** Lo añadido con `metaClass`, `methodMissing` o
 *   una categoría no existe en el bytecode, así que javac no lo conoce.
 * - **Lo que Java vaya a consumir, escríbelo con `@CompileStatic` y tipos declarados.**
 * - **Un método de Java puede devolver `null` sin avisar.** Cruza con `?.`.
 * - `[1, 2]` es una lista, no un array: si la API de Java pide `int[]`, dilo.
 * - Al portar de Java: compila, pasa los tests, revisa los `==` y las sobrecargas, y
 *   sólo entonces empieza a quitar ceremonia.
 *
 * Siguiente paso: capítulo 27, concurrencia.
 */
@CompileStatic
class C26_00Index {

    static final Chapter CHAPTER = chapter(
        26,
        'Interoperabilidad Java',
        'llamar a Java y al revés, coerción SAM, stubs y las diferencias de semántica',
    ) {
        demo('Groovy llamando a Java') { C26_01Interop.demoCallingJava() }
        demo('Closures donde Java espera una interfaz') { C26_01Interop.demoSamCoercion() }
        demo('Java llamando a Groovy, y los stubs') { C26_01Interop.demoJavaCallingGroovy() }
        demo('Las diferencias de semántica') { C26_01Interop.demoSemanticDifferences() }
    }

    /** Ejecuta el capítulo 26 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
