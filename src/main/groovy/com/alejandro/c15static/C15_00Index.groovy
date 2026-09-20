package com.alejandro.c15static

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 15 · Tipado estático
 *
 * Groovy es de tipado dinámico **opcional**. Dos anotaciones cambian eso, y conviene
 * saber exactamente qué hace cada una porque se confunden constantemente.
 *
 * ## La diferencia, en dos líneas
 * ```
 * @TypeChecked     comprueba al compilar · sigue ejecutándose por el MOP
 * @CompileStatic   comprueba al compilar · Y genera llamadas directas
 * ```
 * Sólo la segunda da velocidad. Las dos dan seguridad de tipos.
 *
 * ## Qué se cubre
 * - Qué comprueba cada una y qué no.
 * - Lo que se pierde: `metaClass`, `methodMissing`, `use { }`, los nombres dinámicos.
 * - Lo que NO se pierde: closures, el GDK entero, los GString.
 * - Mezclar las dos cosas con `@CompileDynamic`.
 * - Cuánto se gana de verdad, medido sobre tres millones de vueltas.
 * - Genéricos y borrado de tipos: qué protegen y qué no.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`@TypeChecked` no acelera nada**: sólo comprueba. La velocidad la da
 *   `@CompileStatic`, porque genera `invokevirtual` en vez de consultar la metaclase.
 * - **No es todo o nada**: `@CompileDynamic` abre un agujero donde haga falta. Lo
 *   normal es estático el 95% y dinámico lo que lo pida.
 * - **Un genérico sin comprobación no protege nada**: `List<String>` acepta un entero
 *   sin decir ni mu, porque los genéricos se borran al compilar.
 * - **En código de E/S, red o base de datos la diferencia no se nota.** Se nota en
 *   bucles cerrados. Mide antes de anotar.
 * - DSLs y builders suelen necesitar `@DelegatesTo` para poder ser estáticos
 *   (capítulo 07).
 *
 * ## Errores típicos
 * - `@CompileStatic` sobre una clase que usa la metaprogramación del capítulo 16.
 * - Creer que `@TypeChecked` mejora el rendimiento.
 * - Anotar todo el proyecto sin medir, y perder los DSLs por el camino.
 *
 * Siguiente paso: capítulo 16, metaprogramación en tiempo de ejecución.
 */
@CompileStatic
class C15_00Index {

    static final Chapter CHAPTER = chapter(
        15,
        'Tipado estático',
        '@TypeChecked frente a @CompileStatic, qué se pierde, cuánto se gana, genéricos',
    ) {
        demo('Qué comprueba cada anotación') { C15_01StaticTyping.demoWhatTheyCheck() }
        demo('Lo que se pierde y lo que no') { C15_01StaticTyping.demoWhatYouLose() }
        demo('Cuánto se gana, medido') { C15_01StaticTyping.demoPerformance() }
        demo('Genéricos y borrado de tipos') { C15_01StaticTyping.demoGenerics() }
    }

    /** Ejecuta el capítulo 15 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
