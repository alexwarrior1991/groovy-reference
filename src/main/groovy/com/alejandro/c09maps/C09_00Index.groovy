package com.alejandro.c09maps

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 09 · Mapas y rangos
 *
 * El mapa es la estructura de datos de Groovy: configuraciones, JSON, argumentos con
 * nombre, resultados de consultas. Y el rango es una lista que no guarda sus
 * elementos, lo que la hace gratis por grande que sea.
 *
 * ## Las dos trampas del literal de mapa
 * ```
 * def k = 'x'
 * [k: 1]        // clave "k"  ← la palabra, no el valor
 * [(k): 1]      // clave "x"  ← lo que querías
 *
 * mapa.class    // null: busca la CLAVE "class"
 * mapa.getClass()
 * ```
 *
 * ## Qué se cubre
 * - El literal, el `LinkedHashMap` que produce y el orden de inserción.
 * - Claves dinámicas, claves que no son cadenas y la trampa del GString como clave.
 * - Acceder con punto, con subíndice y con `get`; y por qué `.size` no es `.size()`.
 * - `get(clave, porDefecto)` y `withDefault`, que INSERTAN al leer.
 * - `each` con uno o dos parámetros, `collect` frente a `collectEntries`, `findAll`,
 *   `groupBy`, `sort` y el idioma de indexar una lista por una clave.
 * - Rangos: los cuatro tipos, descendentes, de fechas y de caracteres.
 * - Por qué un rango de diez millones no ocupa memoria, y qué operaciones lo rompen.
 * - Rangos de tus propias clases, con `Comparable` + `next()` + `previous()`.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`[variable: 1]` usa la palabra como clave.** Con paréntesis, `[(variable): 1]`,
 *   usa su valor.
 * - **`mapa.class` es `null`.** El acceso con punto busca la clave. Usa `getClass()`,
 *   y para los métodos pon siempre los paréntesis.
 * - **`get(clave, porDefecto)` y `withDefault` MUTAN el mapa al leer.** Si sólo
 *   consultas, usa el elvis.
 * - **`collect` sobre un mapa devuelve una lista**; `collectEntries`, un mapa.
 * - **`sort` sobre un mapa NO muta** (al contrario que sobre una lista).
 * - **`5..1` recorre hacia atrás**, no está vacío.
 *
 * ## Errores típicos
 * - Construir una clave con un GString y no encontrarla luego (capítulo 03).
 * - `toList()` sobre un rango enorme.
 * - Contar con `withDefault` y acabar con claves que nadie insertó.
 *
 * Siguiente paso: capítulo 10, clases y POGOs.
 */
@CompileStatic
class C09_00Index {

    static final Chapter CHAPTER = chapter(
        9,
        'Mapas y rangos',
        'el literal y sus trampas, collectEntries, withDefault, rangos perezosos',
    ) {
        demo('El literal y el orden de inserción') { C09_01Maps.demoCreating() }
        demo('Las claves, y la trampa de [variable: 1]') { C09_01Maps.demoKeys() }
        demo('Acceder, y por qué mapa.class es null') { C09_01Maps.demoAccessing() }
        demo('Modificar, y los get que insertan') { C09_01Maps.demoModifying() }
        demo('Recorrer: uno o dos parámetros') { C09_02MapOperations.demoIterating() }
        demo('collect frente a collectEntries') { C09_02MapOperations.demoTransforming() }
        demo('Filtrar, buscar y ordenar') { C09_02MapOperations.demoFilteringAndSorting() }
        demo('Mapas anidados y datos de fuera') { C09_02MapOperations.demoNested() }
        demo('Los cuatro tipos de rango') { C09_03Ranges.demoKinds() }
        demo('Por qué un rango enorme es gratis') { C09_03Ranges.demoLaziness() }
        demo('Dónde se usan los rangos') { C09_03Ranges.demoUses() }
        demo('Rangos de tus propias clases') { C09_03Ranges.demoCustomRanges() }
    }

    /** Ejecuta el capítulo 09 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
