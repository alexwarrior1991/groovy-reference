package com.alejandro.c12ast

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 12 · Transformaciones AST
 *
 * Una anotación que, **al compilar**, escribe código en tu clase. No es reflexión ni
 * magia en ejecución: el bytecode que sale tiene los métodos escritos, igual que si
 * los hubieras puesto a mano. Java los ve. Y no cuestan nada.
 *
 * ## Las que se usan todos los días
 * ```
 * @ToString @EqualsAndHashCode @TupleConstructor   → o las tres: @Canonical
 * @Immutable   objeto de valor, con copias defensivas incluidas
 * @Delegate    componer sin escribir quince reenvíos
 * @Sortable @Builder @Lazy @Memoized @Singleton @InheritConstructors
 * ```
 *
 * ## Qué se cubre
 * - Qué genera cada una, comprobado por reflexión sobre la clase compilada.
 * - Las opciones que de verdad se usan: `includeNames`, `excludes`, `includes`,
 *   `defaults`, `copyWith`.
 * - La trampa de `@EqualsAndHashCode` en una clase mutable usada como clave.
 * - `@Immutable`: qué tipos sabe congelar y qué pasa con los que no.
 * - `@Delegate` resolviendo el contador roto del capítulo 11.
 * - Local frente a global, y por qué esto no es metaprogramación en runtime.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`@Canonical` son tres anotaciones**: `@ToString` + `@EqualsAndHashCode` +
 *   `@TupleConstructor`. Si necesitas configurarlas distinto, ponlas por separado.
 * - **`@ToString(includeNames = true)` casi siempre**, y `excludes` para las
 *   contraseñas: lo que no excluyas acabará en una traza.
 * - **Un objeto mutable con `@EqualsAndHashCode` como clave de mapa se pierde**: al
 *   cambiarle un campo, cambia su hashCode y no se encuentra ni a sí mismo.
 * - **`@Delegate` NO hace la clase `instanceof` del delegado.** Eso es lo bueno: hay
 *   reenvío sin relación de tipos.
 * - **`@Sortable` no genera constructor.** Acompáñalo de `@TupleConstructor`.
 * - Todo esto ocurre al compilar: coste cero en ejecución, y visible desde Java.
 *
 * ## Errores típicos
 * - `@Canonical` en una clase mutable que acaba en un `Set`.
 * - `@Memoized` sin límite sobre argumentos que no se repiten.
 * - `@Builder` en una clase de dos campos, donde el constructor de mapa ya bastaba.
 *
 * Siguiente paso: capítulo 13, records, sealed y enums.
 */
@CompileStatic
class C12_00Index {

    static final Chapter CHAPTER = chapter(
        12,
        'Transformaciones AST',
        '@ToString, @Canonical, @Immutable, @Delegate, @Builder y el resto del catálogo',
    ) {
        demo('@ToString y sus opciones') { C12_01Generated.demoToString() }
        demo('@EqualsAndHashCode y la clave mutable') { C12_01Generated.demoEqualsAndHashCode() }
        demo('Los constructores y @Canonical') { C12_01Generated.demoConstructors() }
        demo('Qué es una transformación AST') { C12_01Generated.demoHowItWorks() }
        demo('@Immutable: el objeto de valor') { C12_02Immutable.demoImmutable() }
        demo('@Sortable') { C12_02Immutable.demoSortable() }
        demo('@Builder') { C12_02Immutable.demoBuilder() }
        demo('@Delegate: componer sin reenviar a mano') { C12_03Others.demoDelegate() }
        demo('@Lazy y @Memoized') { C12_03Others.demoLazyAndMemoized() }
        demo('@Singleton, @InheritConstructors, @NullCheck') { C12_03Others.demoOthers() }
    }

    /** Ejecuta el capítulo 12 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
