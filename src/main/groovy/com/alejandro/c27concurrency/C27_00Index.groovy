package com.alejandro.c27concurrency

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 27 · Concurrencia
 *
 * Groovy no tiene corrutinas ni un modelo propio: usa el de la JVM. Lo que añade son
 * atajos (`Thread.start { }`, `@Synchronized`, `@WithReadLock`) y la comodidad de
 * pasar closures a `ExecutorService` y `CompletableFuture`.
 *
 * ## Lo que ha cambiado el panorama
 * Los **hilos virtuales** del JDK 21: dos mil hilos que esperan ya no cuestan nada,
 * así que para tareas de E/S se acabó el afinado del tamaño del pool.
 *
 * ## Qué se cubre
 * - `Thread.start`, esperar a varios con `*.join()`, e hilos virtuales medidos.
 * - El contador que pierde incrementos, y las cuatro formas de arreglarlo por orden
 *   de preferencia.
 * - `@Synchronized`, `@WithReadLock`/`@WithWriteLock`, atómicos y colecciones
 *   concurrentes.
 * - `ExecutorService` con try-with-resources, y `CompletableFuture` componiendo,
 *   encadenando y tratando fallos.
 * - Paralelizar una colección, medido contra la versión secuencial.
 * - **Las dos trampas de Groovy**: las metaclases son globales y las categorías no
 *   cruzan al hilo que lanzas.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`valor++` no es atómico**: son leer, sumar y escribir. El contador sin proteger
 *   pierde incrementos, y en la demo se ve cuántos.
 * - **El orden correcto es: no compartir → compartir inmutables → atómicos y
 *   colecciones concurrentes → candados.** Los candados son el último recurso, no el
 *   primero.
 * - **`@Synchronized` usa un candado privado**, no `this`: nadie de fuera puede
 *   bloquearte sin querer.
 * - **Cierra siempre el `ExecutorService`.** Desde Java 19 implementan
 *   `AutoCloseable`, así que un try-with-resources basta.
 * - **`CompletableFuture` envuelve la excepción** en una `CompletionException`: en
 *   `exceptionally` lo que buscas es `e.cause`.
 * - **Las categorías no cruzan a otro hilo** (capítulo 17) y **las metaclases son
 *   globales** (capítulo 16). Las dos son fallos silenciosos.
 * - Hilos virtuales para E/S; un pool del tamaño de los núcleos para cálculo.
 *
 * Siguiente paso: capítulo 28, testing con Spock.
 */
@CompileStatic
class C27_00Index {

    static final Chapter CHAPTER = chapter(
        27,
        'Concurrencia',
        'hilos virtuales, estado compartido, ExecutorService, CompletableFuture y el MOP',
    ) {
        demo('Hilos, y los virtuales del JDK 21') { C27_01Concurrency.demoThreads() }
        demo('Estado compartido: el contador que pierde') { C27_01Concurrency.demoSharedState() }
        demo('ExecutorService y CompletableFuture') { C27_01Concurrency.demoExecutors() }
        demo('Paralelizar, y las trampas del MOP') { C27_01Concurrency.demoParallelAndMop() }
    }

    /** Ejecuta el capítulo 27 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
