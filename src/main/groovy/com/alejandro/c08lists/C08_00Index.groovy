package com.alejandro.c08lists

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 08 · Listas
 *
 * `[1, 2, 3]` es una `ArrayList`, y el GDK le añade indexado con rangos, índices
 * negativos y unos ciento cincuenta métodos. Este capítulo cubre los que se usan de
 * verdad, y —sobre todo— cuáles de ellos **mutan** la lista.
 *
 * ## La tabla que resuelve la mitad de los bugs
 * ```
 * sort()   MUTA        reverse()   no muta
 * unique() MUTA        collect()   no muta
 * <<  add  MUTAN       +  -  *     no mutan
 * ```
 * Si el nombre empieza por `to…` (`toSorted`, `toUnique`), devuelve una copia.
 *
 * ## Qué se cubre
 * - Crear, indexar con rangos y con índices negativos, y qué pasa fuera de rango.
 * - Añadir y quitar, con la trampa de `remove()`, que borra por ÍNDICE.
 * - `collect`, `collectMany`, `flatten`, `collectEntries`, `collate`, `transpose`.
 * - `findAll`, `find`, `grep`, `any`, `every`, `takeWhile`, `split`.
 * - `inject`, y sus dos casos límite: lista vacía sin valor inicial, y `[].sum()`.
 * - `groupBy`, `countBy` y el idioma de agrupar y agregar.
 * - `asImmutable()` y por qué es superficial; copias defensivas de entrada y salida.
 * - Cuándo una lista NO es la estructura, medido.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`lista.remove(2)` borra la POSICIÓN 2.** Para borrar el valor: `removeElement(2)`
 *   o el operador `-`. Con listas de enteros esto muerde siempre.
 * - **`sort()` muta y `reverse()` no.** No hay lógica; hay que saberlo.
 * - **`lista[10]` sobre una lista de 3 devuelve `null`**, no lanza. `get(10)` sí lanza.
 * - **`[].sum()` es `null`**, no 0. Y `[].inject { }` sin valor inicial lanza.
 * - **`asImmutable()` es superficial.** Protege la lista, no lo que hay dentro.
 * - Copia lo que te dan y protege lo que devuelves, o usa `@Immutable` (capítulo 12).
 *
 * ## Errores típicos
 * - Ordenar con `sort()` una lista que te han pasado como parámetro.
 * - `contains` o `in` sobre una lista grande dentro de un bucle.
 * - Modificar la lista dentro del `each` que la recorre.
 *
 * Siguiente paso: capítulo 09, mapas y rangos.
 */
@CompileStatic
class C08_00Index {

    static final Chapter CHAPTER = chapter(
        8,
        'Listas',
        'indexado, la trampa de remove, qué muta, collect/findAll/inject, groupBy',
    ) {
        demo('Crear listas') { C08_01Basics.demoCreating() }
        demo('Indexar: rangos e índices negativos') { C08_01Basics.demoAccessing() }
        demo('Añadir y quitar, y la trampa de remove') { C08_01Basics.demoAddingRemoving() }
        demo('Qué muta y qué no: la tabla') { C08_01Basics.demoMutationTable() }
        demo('Modificar mientras se recorre') { C08_01Basics.demoConcurrentModification() }
        demo('Transformar: collect y familia') { C08_02Operations.demoTransforming() }
        demo('Filtrar y buscar') { C08_02Operations.demoFiltering() }
        demo('Agregar: inject y sus casos límite') { C08_02Operations.demoAggregating() }
        demo('Agrupar y ordenar') { C08_02Operations.demoGroupingAndSorting() }
        demo('asImmutable y por qué es superficial') { C08_03Immutability.demoImmutable() }
        demo('Copias defensivas') { C08_03Immutability.demoDefensiveCopies() }
        demo('Cuándo una lista NO es la estructura') { C08_03Immutability.demoChoosingStructure() }
    }

    /** Ejecuta el capítulo 08 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
