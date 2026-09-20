package com.alejandro.c30exercises

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 30 · Ejercicios
 *
 * Tres ejercicios que juntan lo de los veintinueve capítulos anteriores. Todos siguen
 * la misma estructura:
 *
 * ```
 * ENUNCIADO   → qué hay que construir y con qué reglas
 * PISTAS      → por dónde empezar, sin destripar la solución
 * SOLUCIÓN    → el código propuesto, ejecutándose de verdad
 * EXPLICACIÓN → por qué está hecho así y qué decisiones hay detrás
 * VARIANTE    → cómo seguir cuando el ejercicio ya te sale
 * ```
 *
 * ## Cómo usarlos (importante)
 * Lee **sólo el enunciado**, cierra el fichero y escribe tu versión. Sólo después
 * compara con la solución. Leer una solución no enseña casi nada; escribir la tuya,
 * compararla y entender las diferencias, muchísimo.
 *
 * Si te atascas, tira de las pistas antes que de la solución.
 *
 * ## El mapa
 * ```
 * 🟢 fácil     1. Calculadora de expresiones   switch, enum estrategia, sealed
 * 🟡 medio     2. Informe de inventario        colecciones, @Immutable, dinero
 * 🔴 difícil   3. Un DSL de validación         delegate, DELEGATE_ONLY, builders
 * ```
 *
 * ## Las soluciones están probadas
 * Cada ejercicio tiene su Spec en `src/test/groovy/com/alejandro/c30exercises/`. No
 * son adorno: si rompes una solución al experimentar, `./mvnw test` te lo dice. Y son
 * el mejor sitio para ver qué casos límite conviene comprobar.
 *
 * ```
 * ./mvnw test -Dtest=CalculadoraSpec
 * ```
 *
 * ## Si quieres más
 * Las variantes difíciles de cada ejercicio dan para semanas. Y si te quedas corto,
 * los bloques `// PARA EXPERIMENTAR` del final de cada fichero de los capítulos 1 a
 * 29 son otro centenar de ideas.
 */
@CompileStatic
class C30_00Index {

    static final Chapter CHAPTER = chapter(
        30,
        'Ejercicios',
        'tres ejercicios con enunciado, pistas, solución explicada y variante difícil',
    ) {
        demo('🟢 Calculadora de expresiones') { E01Calculadora.ejecutar() }
        demo('🟡 Informe de inventario') { E02Inventario.ejecutar() }
        demo('🔴 Un DSL de validación') { E03MiniDsl.ejecutar() }
    }

    /** Ejecuta el capítulo 30 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
