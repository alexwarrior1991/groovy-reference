package com.alejandro.c18exceptions

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 18 · Excepciones
 *
 * Groovy conserva toda la jerarquía de excepciones de Java y elimina una sola cosa:
 * la obligación. **Ninguna excepción es comprobada.** Puedes lanzar una `IOException`
 * sin `throws` y quien te llama no está obligado a capturarla.
 *
 * ## Lo que eso cambia
 * ```
 * // Groovy: compila
 * def leer() { throw new IOException('...') }
 *
 * // Java: no compila sin `throws IOException`
 * ```
 * Se acabaron los `catch` vacíos puestos para que compile. Y también el recordatorio
 * del compilador, así que la decisión de qué puede fallar pasa a ser tuya.
 *
 * ## Qué se cubre
 * - Qué cambia y qué no respecto a Java.
 * - Multi-catch, `finally`, y la trampa del `return` dentro de un `finally`.
 * - `withCloseable`, que es el try-with-resources de Groovy.
 * - Excepciones propias que llevan los errores como DATOS, no sólo como texto.
 * - Encadenar causas sin perder el origen, y recorrer la cadena entera.
 * - El error como dato (`sealed` + `record`) frente al error como excepción, con el
 *   coste de cada uno medido.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Ninguna excepción es comprobada.** Documenta con `@throws` lo que el compilador
 *   ya no te va a recordar.
 * - **Nunca pongas un `return` en un `finally`.** Se come el valor del `try` y, peor,
 *   se traga la excepción que iba a salir.
 * - **Nunca pierdas la causa**: `new MiError(mensaje, e)`. Sin eso, el `catch` de
 *   arriba no sabe qué pasó de verdad.
 * - **Un fallo esperado no es una excepción.** "El usuario no existe" en una búsqueda
 *   es un resultado; que el fichero de configuración esté corrupto, no.
 * - **Lanzar cuesta**, porque rellenar el stack trace es caro. No es sólo estilo: se
 *   mide.
 *
 * ## Errores típicos
 * - `catch (Exception e) { }` sin hacer nada.
 * - Excepciones para el flujo normal, con el `try/catch` rodeando el caso correcto.
 * - Relanzar con `throw new MiError(e.message)`, perdiendo el stack trace original.
 *
 * Siguiente paso: capítulo 19, expresiones regulares.
 */
@CompileStatic
class C18_00Index {

    static final Chapter CHAPTER = chapter(
        18,
        'Excepciones',
        'ninguna es checked, withCloseable, encadenar causas y el error como dato',
    ) {
        demo('Ninguna excepción es comprobada') { C18_01Exceptions.demoNoChecked() }
        demo('try, finally y withCloseable') { C18_01Exceptions.demoSyntax() }
        demo('Excepciones propias y la cadena de causas') { C18_01Exceptions.demoCustomExceptions() }
        demo('El error como dato, y lo que cuesta lanzar') { C18_01Exceptions.demoErrorsAsData() }
    }

    /** Ejecuta el capítulo 18 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
