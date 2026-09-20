package com.alejandro.c29traps

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 29 · Trampas y rendimiento
 *
 * Las diecisiete cosas que más tiempo cuestan en Groovy, todas juntas y
 * ejecutándose. Cada una ha salido ya en su capítulo; esto es el sitio al que volver
 * cuando algo no hace lo que parece.
 *
 * ## Lo que tienen en común
 * Casi todas **compilan, no lanzan y hacen algo distinto de lo que parece**. Son las
 * que no encuentra el compilador, y por eso hay que conocerlas de memoria.
 *
 * ## Qué se cubre
 * - Siete que dan un valor equivocado en silencio.
 * - Cinco que fallan en ejecución lejos de donde está el problema.
 * - Cinco que sólo aparecen en producción: hilos, concurrencia, datos publicados.
 * - Rendimiento: dónde se va el tiempo de verdad, medido, y en qué orden mirarlo.
 * - Una lista de comprobación para pasar antes de dar un código por terminado.
 *
 * ## Las cinco que más caro salen
 * - **`?:` sobre un cero legítimo.** Un contador a 0, una temperatura de 0 grados.
 * - **Un objeto mutable como clave de mapa.** La entrada queda inalcanzable, incluso
 *   para el objeto que la metió.
 * - **`sort()` sobre una lista que te pasaron.** Le acabas de cambiar los datos a
 *   quien te llamó.
 * - **Serializar un objeto entero a JSON.** Así acaban las contraseñas en los logs.
 * - **Una categoría dentro de un hilo.** Funciona en desarrollo y falla en producción.
 *
 * ## Sobre el rendimiento
 * El orden correcto para mirar es: **algoritmo → E/S → excepciones en el flujo normal
 * → recompilar lo que no cambia → y sólo entonces `@CompileStatic`**. Un `in` sobre
 * una lista dentro de un bucle cuesta órdenes de magnitud más que todo lo demás junto.
 *
 * Siguiente paso: capítulo 30, los ejercicios.
 */
@CompileStatic
class C29_00Index {

    static final Chapter CHAPTER = chapter(
        29,
        'Trampas y rendimiento',
        'las diecisiete trampas juntas, dónde se va el tiempo y la lista de comprobación',
    ) {
        demo('Las que dan un valor equivocado sin avisar') { C29_01Traps.demoSilentlyWrong() }
        demo('Las que fallan lejos del problema') { C29_01Traps.demoLateFailures() }
        demo('Las que sólo aparecen en producción') { C29_01Traps.demoProductionTraps() }
        demo('Rendimiento: dónde se va el tiempo') { C29_01Traps.demoPerformance() }
        demo('La lista de comprobación') { C29_01Traps.demoChecklist() }
    }

    /** Ejecuta el capítulo 29 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
