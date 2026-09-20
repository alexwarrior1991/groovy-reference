package com.alejandro.c25scripts

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 25 · Scripts y Binding
 *
 * Groovy compila y ejecuta código en caliente. Es la base de Jenkins, de Gradle y de
 * cualquier sitio donde el usuario escribe reglas que el programa ejecuta. También es
 * la puerta más grande que puedes abrirle a alguien en tu proceso.
 *
 * ## Las tres piezas
 * ```
 * GroovyShell             compila y ejecuta
 * Binding                 el puente de datos, en los dos sentidos
 * CompilerConfiguration   qué puede hacer ese código
 * ```
 *
 * ## Qué se cubre
 * - `evaluate` y `parse`, y por qué compilar una vez importa (medido).
 * - El `Binding` de ida y de vuelta, y la trampa de `def` dentro de un script.
 * - `@Field` y `@BaseScript`, que es como Jenkins te da `sh` y `stage`.
 * - `ImportCustomizer` para que las reglas del usuario salgan cortas.
 * - Un motor de reglas diminuto, con las reglas guardadas como datos.
 * - **Seguridad**: qué para `SecureASTCustomizer` y, sobre todo, qué NO para.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Una variable sin `def` va al `Binding` y sale del script.** Con `def` es local
 *   y se pierde, y los métodos del script tampoco la ven: para eso está `@Field`.
 * - **Compila una vez y ejecuta muchas.** Recompilar en cada llamada es el error de
 *   rendimiento típico, y se mide.
 * - **`evaluate(textoDelUsuario)` ejecuta cualquier cosa con tus permisos**: leer
 *   ficheros, abrir conexiones, llamar a `System.exit`.
 * - **`SecureASTCustomizer` sólo mira el AST al compilar.** No para lo que se
 *   resuelva por el MOP en ejecución, ni un bucle infinito, ni agotar la memoria.
 * - **La regla honesta**: si el código lo escriben tus compañeros, con eso y un
 *   tiempo máximo basta. Si lo escribe cualquiera de internet, no lo ejecutes en tu
 *   proceso.
 *
 * Siguiente paso: capítulo 26, interoperabilidad con Java.
 */
@CompileStatic
class C25_00Index {

    static final Chapter CHAPTER = chapter(
        25,
        'Scripts y Binding',
        'GroovyShell, Binding, @Field, @BaseScript, un motor de reglas y la seguridad',
    ) {
        demo('GroovyShell y el Binding') { C25_01Scripts.demoShellAndBinding() }
        demo('@Field y @BaseScript') { C25_01Scripts.demoFieldAndBaseScript() }
        demo('Un motor de reglas diminuto') { C25_01Scripts.demoRulesEngine() }
        demo('Seguridad: evaluar código de fuera') { C25_01Scripts.demoSandboxing() }
    }

    /** Ejecuta el capítulo 25 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
