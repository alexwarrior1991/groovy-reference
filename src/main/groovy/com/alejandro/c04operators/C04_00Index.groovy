package com.alejandro.c04operators

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 04 · Operadores
 *
 * Groovy redefine dos operadores de Java (`==` y `[]`), añade una docena nueva
 * (`?:`, `?.`, `*.`, `<=>`, `**`, `in`, `as`…) y —lo más importante— convierte
 * **todos** en llamadas a métodos con un nombre fijo. `a + b` es `a.plus(b)`.
 *
 * ## El cambio que hay que interiorizar
 * ```
 * a == b      // equals(), NO identidad          (en Java es al revés)
 * a.is(b)     // identidad, el == de Java
 * ```
 *
 * ## Qué se cubre
 * - `==`, `is()`, `<=>` y la sorpresa de `equals` frente a `compareTo` en BigDecimal.
 * - Los cuatro operadores de nulos: `?.`, `?:`, `?=` y `?[]`.
 * - Los tres spread: `*.` (por elemento), `*` (argumentos) y `*:` (mapas).
 * - `in` / `!in` y su relación con el `switch` a través de `isCase`.
 * - `<<` frente a `+`: cuál muta y cuál no.
 * - Rangos, incluidos los exclusivos y los descendentes.
 * - La tabla completa operador → método, y una clase que los sobrecarga.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`==` es `equals()`**, y además nunca lanza con `null`. Úsalo siempre.
 * - **`?:` no comprueba el null: comprueba el Groovy Truth.** Con un `0` o una cadena
 *   vacía legítimos, te los cambia por el valor por defecto. Es un bug clásico.
 * - **`<<` muta la colección y devuelve la misma**; `+` devuelve una nueva.
 * - **`'ell' in 'hello'` es `false`**: sobre cadenas, `isCase` es igualdad.
 * - **Todo operador es un método**, y por eso puedes sobrecargarlo (capítulo 14).
 * - `(x <=> y) ?: (a <=> b)` es el idioma para ordenar por varios criterios.
 *
 * ## Errores típicos
 * - Usar `?:` sobre un contador que puede valer 0.
 * - Esperar que `*.` acepte una expresión: sólo llama a un método.
 * - Creer que `lista << x` devuelve una copia.
 *
 * Siguiente paso: capítulo 05, el Groovy Truth y el control de flujo.
 */
@CompileStatic
class C04_00Index {

    static final Chapter CHAPTER = chapter(
        4,
        'Operadores',
        '== es equals, is(), <=>, ?., ?:, los tres spread, in, << y la tabla completa',
    ) {
        demo('`==` compara contenido, no referencias') { C04_01Equality.demoEqualsOperator() }
        demo('`is()` y la caché de enteros') { C04_01Equality.demoIdentity() }
        demo('El operador nave espacial `<=>`') { C04_01Equality.demoSpaceship() }
        demo('equals() frente a compareTo() en BigDecimal') { C04_01Equality.demoEqualsVsCompareTo() }
        demo('Navegación segura `?.` y `?[]`') { C04_02NullOperators.demoSafeNavigation() }
        demo('Elvis `?:` y `?=`, y su trampa') { C04_02NullOperators.demoElvis() }
        demo('Nulos dentro de las colecciones') { C04_02NullOperators.demoNullInCollections() }
        demo('El spread `*.`') { C04_03Spread.demoSpreadDot() }
        demo('El spread de argumentos `*`') { C04_03Spread.demoSpreadArguments() }
        demo('El spread de mapas `*:`') { C04_03Spread.demoSpreadMap() }
        demo('`in`, `!in` y isCase') { C04_04OtherOperators.demoIn() }
        demo('`<<` frente a `+`: qué muta') { C04_04OtherOperators.demoCollectionArithmetic() }
        demo('Rangos') { C04_04OtherOperators.demoRanges() }
        demo('La tabla operador → método') { C04_04OtherOperators.demoOperatorTable() }
    }

    /** Ejecuta el capítulo 04 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
