package com.alejandro.c16mop

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 16 · Metaprogramación (MOP)
 *
 * En Groovy, `objeto.metodo()` no es una llamada directa: es una consulta a la
 * **metaclase** del objeto, que decide qué ejecutar. Y esa metaclase se puede cambiar
 * en ejecución.
 *
 * ## El mecanismo entero, en una línea
 * ```
 * String.metaClass.gritar = { -> delegate.toUpperCase() + '!' }
 * 'hola'.gritar()   // HOLA!
 * ```
 * Eso es exactamente lo que hace el GDK para añadirle `capitalize` o `tokenize` a
 * `java.lang.String`, una clase que nadie puede modificar.
 *
 * ## Qué se cubre
 * - Qué es la metaclase y cómo se le pregunta (`respondsTo`, `hasProperty`, `methods`).
 * - Llamar métodos y leer propiedades por nombre, en ejecución.
 * - Añadir métodos, propiedades y estáticos: a una clase propia, a una de Java y a
 *   una sola instancia.
 * - `methodMissing` y `propertyMissing`, con el patrón "finder" que usan los ORM.
 * - El truco de registrar el método en la metaclase para no pagar la búsqueda dos veces.
 * - `GroovyInterceptable`, y por qué casi siempre hay algo mejor.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Modificar la metaclase de una clase de la biblioteca afecta a TODO el proceso**,
 *   incluidas las librerías de terceros. Si lo haces en un test, deshazlo con
 *   `GroovySystem.metaClassRegistry.removeMetaClass(…)`.
 * - **En `methodMissing`, si no sabes atender la llamada, RELÁNZALA.** Si no, las
 *   erratas se convierten en silencio, que es el peor fallo posible.
 * - **Registrar el método en la metaclase desde `methodMissing`** evita la búsqueda
 *   fallida… pero sólo para los objetos creados DESPUÉS. El que provocó el registro
 *   sigue cayendo en `methodMissing`, porque ya tenía su metaclase resuelta.
 * - **Dar metaclase a una instancia** es lo más contenido que puedes hacer: no afecta
 *   al resto de objetos de esa clase.
 * - **`@CompileStatic` desactiva todo esto** (capítulo 15). No se pueden tener las dos
 *   cosas en el mismo código.
 *
 * ## Cuándo NO usarlo
 * - Cuando una interfaz, un trait (capítulo 11) o `@Delegate` (capítulo 12) resuelven
 *   el problema. Casi siempre lo resuelven.
 * - Cuando el resultado es código que nadie puede seguir con el IDE.
 * - En bucles calientes: cada llamada interceptada cuesta.
 *
 * Siguiente paso: capítulo 17, categorías y extensiones.
 */
@CompileStatic
class C16_00Index {

    static final Chapter CHAPTER = chapter(
        16,
        'Metaprogramación (MOP)',
        'metaClass, añadir métodos en caliente, methodMissing y GroovyInterceptable',
    ) {
        demo('Qué es la metaclase') { C16_01MetaClass.demoWhatIsIt() }
        demo('Añadir métodos en caliente') { C16_01MetaClass.demoAddingMethods() }
        demo('methodMissing y propertyMissing') { C16_01MetaClass.demoMissing() }
        demo('Interceptar todas las llamadas') { C16_01MetaClass.demoInterceptable() }
    }

    /** Ejecuta el capítulo 16 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
