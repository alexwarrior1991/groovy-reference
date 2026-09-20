package com.alejandro.c07closuresadvanced

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 07 · Closures avanzados
 *
 * Aquí está el mecanismo que hace posibles los DSL de Groovy —Gradle, Jenkins,
 * `MarkupBuilder`, el `chapter { }` de este repositorio— y las cuatro
 * transformaciones que convierten un closure en otro.
 *
 * ## El mecanismo entero, en tres líneas
 * ```
 * bloque.delegate = miObjeto
 * bloque.resolveStrategy = Closure.DELEGATE_FIRST
 * bloque()
 * ```
 * Todo lo demás —`@DelegatesTo`, `@DslMarker` de otros lenguajes, los builders— son
 * mejoras sobre ese patrón.
 *
 * ## Qué se cubre
 * - `thisObject`, `owner` y `delegate`: quién resuelve cada nombre.
 * - Las cinco `resolveStrategy`, comparadas sobre un método que existe en los dos.
 * - `@DelegatesTo`, que es lo que devuelve el autocompletado y `@CompileStatic`.
 * - `curry`, `rcurry` y `ncurry`.
 * - Composición con `>>` y `<<`.
 * - `memoize()` y sus variantes con límite.
 * - `trampoline()`: recursión profunda sin desbordar la pila.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **La estrategia por defecto es `OWNER_FIRST`, y casi nunca es la que quieres en un
 *   DSL.** Si el delegate no recibe las llamadas, es esto.
 * - **Cambiar `delegate` MUTA el closure.** Si se reutiliza, clónalo o usa
 *   `rehydrate()`.
 * - **`@DelegatesTo` no cambia el comportamiento**, cambia lo que el compilador y el
 *   IDE saben. Sin él no hay `@CompileStatic` ni autocompletado.
 * - **`>>` es una tubería** (izquierda primero); `<<` es la composición matemática.
 * - **`memoize()` sin límite es una fuga**: usa `memoizeAtMost` salvo que el conjunto
 *   de argumentos sea pequeño y cerrado.
 *
 * ## Errores típicos
 * - Un DSL que "no ve" sus métodos: falta `DELEGATE_FIRST`.
 * - Memoizar un closure con efectos secundarios y que dejen de ocurrir.
 * - Usar un trampolín donde un bucle se leía mejor.
 *
 * Siguiente paso: capítulo 08, listas.
 */
@CompileStatic
class C07_00Index {

    static final Chapter CHAPTER = chapter(
        7,
        'Closures avanzados',
        'delegate, resolveStrategy, @DelegatesTo, curry, composición, memoize, trampoline',
    ) {
        demo('thisObject, owner y delegate') { C07_01Delegate.demoTheThree() }
        demo('Cambiar el delegate: esto ya es un DSL') { C07_01Delegate.demoChangingDelegate() }
        demo('Las cinco resolveStrategy') { C07_01Delegate.demoResolveStrategy() }
        demo('@DelegatesTo: decírselo al compilador') { C07_01Delegate.demoDelegatesTo() }
        demo('curry, rcurry y ncurry') { C07_02Transform.demoCurry() }
        demo('Composición con >> y <<') { C07_02Transform.demoComposition() }
        demo('memoize y sus variantes') { C07_02Transform.demoMemoize() }
        demo('trampoline: recursión sin desbordar') { C07_02Transform.demoTrampoline() }
    }

    /** Ejecuta el capítulo 07 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
