package com.alejandro.c17categories

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 17 · Categorías y extensiones
 *
 * Tres formas de añadirle un método a una clase que no es tuya, ordenadas de más
 * contenida a más permanente: la **categoría** (un bloque), el **módulo de extensión**
 * (un jar) y el `metaClass` del capítulo 16 (todo el proceso).
 *
 * ## La tabla de decisión
 * ```
 * herramienta            alcance              @CompileStatic
 * metaClass              TODO el proceso      no
 * use(Categoria) { }     el bloque, un hilo   no
 * módulo de extensión    todo, desde el jar   SÍ    ← lo que usa el GDK
 * ```
 *
 * ## Qué se cubre
 * - Categorías a mano y con `@Category`.
 * - `TimeCategory` del propio Groovy: `1.week`, `3.days`.
 * - Los tres límites: el bloque, el hilo y `@CompileStatic`.
 * - Cuánto cuesta una llamada dentro de un `use`, medido.
 * - Cómo se escribe un módulo de extensión, que es como el GDK añade sus métodos.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Una categoría sólo existe dentro del bloque y sólo en el hilo actual.** Se
 *   guarda en un `ThreadLocal`: otro hilo no la ve. Por eso no sirve en código
 *   concurrente.
 * - **Si el método lo quieres siempre, escribe un módulo de extensión.** Son dos
 *   ficheros, funciona con `@CompileStatic`, lo ve el IDE y se distribuye como una
 *   librería en vez de como un efecto secundario.
 * - **Si la clase es tuya, no hagas nada de esto**: ponle el método.
 * - El capítulo 20 entero (el GDK) es un módulo de extensión enorme.
 *
 * Siguiente paso: capítulo 18, excepciones.
 */
@CompileStatic
class C17_00Index {

    static final Chapter CHAPTER = chapter(
        17,
        'Categorías y extensiones',
        'use(Categoria), @Category, los tres límites y los módulos de extensión',
    ) {
        demo('Cómo se usa una categoría') { C17_01Categories.demoUsing() }
        demo('Los tres límites, y lo que cuestan') { C17_01Categories.demoLimits() }
        demo('Módulos de extensión: lo que hace el GDK') { C17_01Categories.demoExtensionModules() }
    }

    /** Ejecuta el capítulo 17 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
