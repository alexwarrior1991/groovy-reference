package com.alejandro.c01basics

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 01 · Sintaxis básica
 *
 * Groovy se parece tanto a Java que casi todo el Java válido es también Groovy válido.
 * La gracia está en lo que Groovy te deja QUITAR: el punto y coma, los paréntesis, los
 * tipos, el `return`, la clase, el `main`. Este capítulo enseña qué se puede quitar,
 * qué no, y qué pasa por debajo cuando lo quitas.
 *
 * ## Qué se cubre
 * - El programa más corto posible, y la clase que Groovy genera sin que la veas.
 * - Script frente a clase: las dos formas de un fichero `.groovy`, y por qué este
 *   repositorio usa clases.
 * - Punto y coma, paréntesis y `return`: cuándo son opcionales y cuándo NO.
 * - El `Binding` de un script y la trampa de `def` en el cuerpo de un script.
 * - Comentarios, GroovyDoc y los tres helpers de salida del repositorio.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Un script ES una clase**: Groovy genera una que extiende `Script`, con el nombre
 *   del fichero y un `main` que llama a `run()`.
 * - **La última expresión se devuelve** sin escribir `return`.
 * - **Los paréntesis sólo son opcionales si hay al menos un argumento.** Sin
 *   argumentos, `texto.toUpperCase` es una propiedad, no una llamada.
 * - **`def x = 1` en un script es local a `run()`**; sin `def`, la variable va al
 *   `Binding` y sí la ven los métodos del script.
 * - Comilla simple → `String`. Comilla doble → `GString` si hay `$`. Capítulo 03.
 *
 * ## Errores típicos
 * - Poner `static void main` dentro de un script que ya tiene el suyo generado.
 * - Esperar que los métodos de un script vean las variables declaradas con `def`.
 * - Quitar paréntesis en llamadas anidadas: `println foo bar` no compila.
 *
 * Siguiente paso: capítulo 02, variables y tipos.
 */
@CompileStatic
class C01_00Index {

    static final Chapter CHAPTER = chapter(
        1,
        'Sintaxis básica',
        'script frente a clase, println, paréntesis y return opcionales, comentarios',
    ) {
        demo('Hola mundo, y lo que hay debajo') { C01_01HelloWorld.demoHelloWorld() }
        demo('El return es opcional') { C01_01HelloWorld.demoExpressions() }
        demo('Punto y coma y paréntesis: cuándo NO se pueden quitar') { C01_01HelloWorld.demoOptionalSyntax() }
        demo('Comentarios y la salida del repositorio') { C01_01HelloWorld.demoCommentsAndOutput() }
        demo('Qué genera Groovy a partir de un script') { C01_02ScriptsAndClasses.demoWhatAScriptCompilesTo() }
        demo('La trampa de def en un script') { C01_02ScriptsAndClasses.demoScriptVariableScope() }
        demo('El Binding: pasar datos a un script') { C01_02ScriptsAndClasses.demoBinding() }
        demo('Por qué este repositorio usa clases') { C01_02ScriptsAndClasses.demoWhyClassesHere() }
    }

    /** Ejecuta el capítulo 01 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
