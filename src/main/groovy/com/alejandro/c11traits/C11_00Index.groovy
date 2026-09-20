package com.alejandro.c11traits

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 11 · Herencia, interfaces y traits
 *
 * La herencia de Groovy es la de Java. Lo que Java no tiene es el **trait**: una
 * interfaz que además puede llevar implementación **y estado**, y de la que una clase
 * puede implementar todas las que quiera.
 *
 * ## Las tres herramientas, en una línea
 * ```
 * interfaz          contrato, sin estado
 * clase abstracta   estado y constructor, pero sólo se hereda una
 * trait             estado Y varios a la vez  ← lo que Java no puede
 * ```
 *
 * ## Qué se cubre
 * - Heredar, redefinir, `super`, y por qué `@Override` —que es opcional— hay que
 *   ponerlo igualmente.
 * - Interfaces con métodos por defecto, y cómo un closure o un mapa las implementan.
 * - El contador roto: una demostración de por qué heredar para reutilizar es frágil.
 * - Traits con estado, con métodos abstractos y varios a la vez.
 * - Qué pasa cuando dos traits declaran el mismo método, y cómo elegir.
 * - Añadir un trait a un objeto ya creado con `withTraits` o `as`.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Un trait puede tener estado**, y Groovy le genera a cada clase su propio campo.
 * - **En un conflicto entre traits gana el ÚLTIMO de la lista**, sin aviso ninguno.
 *   Para elegir: `Volador.super.moverse()`.
 * - **`++campo` sobre un campo de trait no compila.** Hay que escribir
 *   `campo = campo + 1`.
 * - **`@Override` es opcional y por eso hay que ponerlo**: sin él, una errata en el
 *   nombre crea un método nuevo que nadie llama, y compila.
 * - **Heredar te ata a los detalles internos de la clase padre.** El contador que
 *   cuenta 6 en vez de 3 lo demuestra en diez líneas.
 * - `withTraits` devuelve un objeto NUEVO; el original no cambia.
 *
 * ## Errores típicos
 * - Extender `ArrayList` (o cualquier clase de la biblioteca) para añadirle algo.
 * - Meter en un trait un estado que era de una sola clase.
 * - Cinco traits y ni idea de dónde sale cada método.
 *
 * Siguiente paso: capítulo 12, las transformaciones AST.
 */
@CompileStatic
class C11_00Index {

    static final Chapter CHAPTER = chapter(
        11,
        'Herencia y traits',
        'herencia, interfaces, traits con estado, conflictos y composición',
    ) {
        demo('Heredar, redefinir y la errata sin @Override') { C11_01Inheritance.demoBasics() }
        demo('Interfaces, y closures que las implementan') { C11_01Inheritance.demoInterfaces() }
        demo('El contador roto: por qué heredar es frágil') { C11_01Inheritance.demoCompositionOverInheritance() }
        demo('Traits: interfaces con estado') { C11_02Traits.demoStatefulTraits() }
        demo('Conflictos entre traits: gana el último') { C11_02Traits.demoConflicts() }
        demo('Traits en tiempo de ejecución') { C11_02Traits.demoRuntimeTraits() }
        demo('Cuándo un trait y cuándo no') { C11_02Traits.demoWhenToUse() }
    }

    /** Ejecuta el capítulo 11 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
