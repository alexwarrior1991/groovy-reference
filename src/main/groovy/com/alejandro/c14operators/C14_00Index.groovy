package com.alejandro.c14operators

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 14 · Sobrecarga de operadores
 *
 * El capítulo 04 enseñó que todo operador es una llamada a un método con nombre fijo.
 * Éste usa esa tabla en la otra dirección: implementando esos métodos, una clase propia
 * se comporta como si el lenguaje la conociera.
 *
 * ## El mecanismo entero, en una línea
 * ```
 * Dinero plus(Dinero otro) { new Dinero(centimos + otro.centimos) }   // y ya hay `+`
 * ```
 * No hay palabra clave, ni anotación, ni registro. Sólo el nombre del método.
 *
 * ## Qué se cubre
 * - Una clase `Dinero` completa: aritmética, comparación, negación, Groovy Truth y la
 *   validación de moneda dentro del propio operador.
 * - Un `Vector2D` donde `*` significa dos cosas según el argumento.
 * - La regla de oro, y la trampa de un operador que muta.
 * - `getAt`/`putAt` con varios índices, con rangos y con índices negativos.
 * - `call()`, que hace invocable un objeto… y por qué aun así no encaja donde se
 *   espera un `Closure` sin pasar por `obj.&call`.
 * - `asType`, `iterator()`, `next`/`previous`, y la tabla completa.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Sobrecarga sólo si el significado es obvio sin abrir tu clase.** `precio + envio`
 *   sí; `usuario + permiso` no.
 * - **Un operador NUNCA muta sus operandos.** `a + b` que cambia `a` rompe `sum`,
 *   `inject` y la confianza de quien lee.
 * - **`compareTo` te da `<`, `>`, `sort`, `max`, `min` y los rangos** de una vez.
 * - **`iterator()` te da toda la API de colecciones** sin heredar de nada.
 * - **Tener `call` no convierte tu objeto en un `Closure`.** `collect(obj)` falla;
 *   `collect(obj.&call)` funciona. La coerción automática es para interfaces.
 * - La validación puesta dentro del operador (sumar euros con dólares) detecta el
 *   error donde ocurre, no tres capas más abajo.
 *
 * ## Errores típicos
 * - `getAt` que sólo entiende enteros y falla con rangos.
 * - `call()` en una clase con otros cinco métodos.
 * - Implementar `plus` y olvidar `equals`/`hashCode`/`compareTo`.
 *
 * Siguiente paso: capítulo 15, tipado estático.
 */
@CompileStatic
class C14_00Index {

    static final Chapter CHAPTER = chapter(
        14,
        'Sobrecarga de operadores',
        'Dinero y Vector2D, getAt/putAt, call, asType, iterator y la tabla completa',
    ) {
        demo('Aritmética: una clase Dinero completa') { C14_01Overloading.demoArithmetic() }
        demo('Vectores y el mismo operador con dos usos') { C14_01Overloading.demoVectors() }
        demo('Cuándo sobrecargar y cuándo no') { C14_01Overloading.demoWhenToOverload() }
        demo('El subíndice: getAt y putAt') { C14_02Indexing.demoSubscript() }
        demo('call: un objeto invocable') { C14_02Indexing.demoCall() }
        demo('asType, iterator, next y la tabla') { C14_02Indexing.demoOthers() }
    }

    /** Ejecuta el capítulo 14 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
