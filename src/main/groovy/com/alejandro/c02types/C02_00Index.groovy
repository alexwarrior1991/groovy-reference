package com.alejandro.c02types

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 02 · Variables y tipos
 *
 * Groovy es de tipado dinámico **opcional**: puedes escribir todo con `def` y que el
 * compilador no compruebe nada, o declarar los tipos y añadir `@CompileStatic` y que
 * compruebe tanto como Java. Las dos formas conviven en el mismo fichero.
 *
 * ## La sorpresa que trae este capítulo
 * ```
 * 0.1 + 0.2 == 0.3     // true en Groovy, false en Java
 * 7 / 2                // 3.5, no 3
 * Integer.MAX_VALUE+1  // se promociona a Long, no da la vuelta
 * ```
 * Los tres salen de la misma decisión: **un literal decimal es `BigDecimal`**, y con
 * `def` Groovy promociona el tipo en lugar de desbordar.
 *
 * ## Qué se cubre
 * - `def`, `var`, `final` y `static final`: qué cambia cada uno de verdad.
 * - Qué tipo tiene cada literal numérico, y por qué `1.5` no es un `double`.
 * - División, resto, `intdiv()`, potencia `**` y el desbordamiento que no ocurre.
 * - Coerción: `as`, `asType`, la asignación a variable tipada y los `toXxx()`.
 * - Convertir un mapa o un closure en un objeto o en una interfaz.
 * - Asignación múltiple: `def (a, b) = [1, 2]`.
 * - Arrays frente a listas, y la semántica de `null`.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`def` no quita el tipo**: lo quita de la declaración. El valor siempre lo tiene.
 * - **Un decimal sin sufijo es `BigDecimal`**: aritmética exacta por defecto. Si
 *   quieres la de coma flotante, pide `d` explícitamente.
 * - **`/` entre enteros no trunca.** Para eso está `intdiv()`.
 * - **`as` convierte; `(Tipo)` comprueba.** No son lo mismo.
 * - **Un tipo declarado coerciona en silencio**, y ahí es donde se cuelan los errores
 *   de datos mal formados. Valida en la frontera con `isInteger()`/`isNumber()`.
 * - **`[1, 2, 3]` es una lista**, no un array.
 *
 * ## Errores típicos
 * - Portar una fórmula de Java y perder la aritmética de coma flotante sin darse cuenta
 *   (o al revés: usar `double` para dinero).
 * - Declarar `int` y creerse protegido del desbordamiento: es justo al contrario.
 * - Imprimir un array con `println` y encontrarse `[I@5ca881b5`.
 *
 * Siguiente paso: capítulo 03, cadenas y GString.
 */
@CompileStatic
class C02_00Index {

    static final Chapter CHAPTER = chapter(
        2,
        'Variables y tipos',
        'def frente a tipos, BigDecimal por defecto, coerción con as, arrays y null',
    ) {
        demo('def frente a un tipo declarado') { C02_01Declaring.demoDefVsType() }
        demo('Qué es `def` por debajo') { C02_01Declaring.demoWhatDefReallyIs() }
        demo('var, final y las constantes') { C02_01Declaring.demoVarAndFinal() }
        demo('Cuándo usar def y cuándo el tipo') { C02_01Declaring.demoWhenToUseWhich() }
        demo('Qué tipo tiene cada literal numérico') { C02_02Numbers.demoLiteralTypes() }
        demo('0.1 + 0.2 == 0.3, y por qué') { C02_02Numbers.demoDecimalIsExact() }
        demo('División: 7 / 2 no es 3') { C02_02Numbers.demoDivision() }
        demo('El desbordamiento que no ocurre') { C02_02Numbers.demoOverflow() }
        demo('Potencia ** y métodos numéricos') { C02_02Numbers.demoPowerAndMath() }
        demo('Comparar números de distinto tipo') { C02_02Numbers.demoComparingNumbers() }
        demo('El operador `as`') { C02_03Coercion.demoAsOperator() }
        demo('La coerción implícita de la asignación') { C02_03Coercion.demoImplicitCoercion() }
        demo('Coercionar mapas y closures a objetos') { C02_03Coercion.demoCoercingToObjects() }
        demo('Asignación múltiple') { C02_03Coercion.demoMultipleAssignment() }
        demo('Arrays frente a listas') { C02_04ArraysAndNull.demoArraysVsLists() }
        demo('Qué hace Groovy con null') { C02_04ArraysAndNull.demoNull() }
    }

    /** Ejecuta el capítulo 02 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
