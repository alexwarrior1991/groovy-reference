package com.alejandro.c28spock

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 28 · Testing con Spock
 *
 * Spock no es "JUnit con azúcar": es un DSL (capítulo 24) construido sobre una
 * transformación AST global (capítulo 12) que reescribe tus bloques en tiempo de
 * compilación.
 *
 * ## Las tres cosas que no tienes en JUnit
 * ```
 * power assert   al fallar, imprime el valor de CADA subexpresión
 * where:         una tabla de casos en vez de cinco tests iguales
 * Mock/Stub/Spy  dobles nativos, sin librería aparte
 * ```
 *
 * ## Este capítulo tiene tests de verdad
 * El código de producción está en `c28spock/Carrito.groovy` y los Spec que lo prueban
 * en `src/test/groovy/com/alejandro/c28spock/`. Se ejecutan con `./mvnw test` como
 * todos los demás. Ábrelos mientras lees las demos.
 *
 * ## Qué se cubre
 * - Los bloques, sus reglas y por qué el nombre del test es una frase.
 * - El power assert: qué imprime y cómo funciona.
 * - `where:` en sus tres formas, y `@Unroll`.
 * - `Stub`, `Mock`, `Spy`, más el fake y el dummy escritos a mano.
 * - Qué hace que un código sea testeable, y las cuatro señales de que no lo es.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Un `when/then` por test.** Si necesitas dos, son dos tests.
 * - **Dentro de `then:` cada línea es una aserción**: no hace falta `assert`.
 * - **El power assert desglosa la expresión entera**, así que puedes escribir
 *   aserciones ricas sin miedo a que al fallar no se entienda nada.
 * - **`where:` es para los casos límite**: 0, 1, el máximo, el máximo más uno, null.
 *   Añadir un caso cuesta una línea.
 * - **Usa `Stub` salvo que la interacción SEA lo que pruebas.** Con `Mock` por
 *   defecto acabas comprobando cómo está hecho el código, y cualquier refactor
 *   rompe los tests sin que el comportamiento haya cambiado.
 * - **El código testeable RECIBE sus dependencias**, no las construye.
 * - Los Spec no llevan `@CompileStatic`: Spock necesita el AST dinámico.
 *
 * Siguiente paso: capítulo 29, trampas y rendimiento.
 */
@CompileStatic
class C28_00Index {

    static final Chapter CHAPTER = chapter(
        28,
        'Testing con Spock',
        'bloques, power assert, tablas where, Mock/Stub/Spy y código testeable',
    ) {
        demo('Los bloques: given, when, then') { C28_01Spock.demoBlocks() }
        demo('El power assert') { C28_01Spock.demoPowerAssert() }
        demo('La tabla where y @Unroll') { C28_01Spock.demoWhere() }
        demo('Mocks, stubs y spies') { C28_01Spock.demoDoubles() }
        demo('Qué hace que un código sea testeable') { C28_01Spock.demoTestableCode() }
    }

    /** Ejecuta el capítulo 28 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
