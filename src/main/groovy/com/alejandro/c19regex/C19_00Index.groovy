package com.alejandro.c19regex

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 19 · Expresiones regulares
 *
 * Groovy usa las expresiones regulares de Java y les pone encima tres operadores y un
 * literal que quitan casi todo el ruido.
 *
 * ## La tabla que resuelve la confusión
 * ```
 * ~/x/    Pattern   compila
 * =~      Matcher   busca DENTRO (parcial)   ← es "cierto" si encuentra algo
 * ==~     boolean   encaja la cadena ENTERA
 * ```
 *
 * ## Qué se cubre
 * - Los tres operadores y su equivalente explícito en la API de Java.
 * - Grupos por número, por nombre y con asignación múltiple.
 * - Recorrer varias coincidencias con `each`, que recibe los grupos por separado.
 * - `replaceAll` **con un closure**, que es lo que Java no tiene.
 * - `Pattern.quote` para texto que viene de fuera.
 * - Un caso completo: extraer entradas de un log y agruparlas.
 * - Compilar una vez fuera del bucle, medido.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`=~` busca dentro; `==~` exige que encaje la cadena entera.** Es la confusión
 *   número uno.
 * - **`=~` devuelve un Matcher, no un booleano**, pero un Matcher es "cierto" cuando
 *   encuentra algo, así que `if (texto =~ /x/)` funciona como esperas.
 * - **Usa el literal `/…/`**: con comillas hay que duplicar cada barra invertida.
 * - **`replaceAll` acepta un closure** que recibe la coincidencia y sus grupos. Es la
 *   forma de meter lógica dentro de una sustitución.
 * - **Si la expresión viene de fuera, `Pattern.quote`.** Si no, es una inyección.
 * - **Compila fuera del bucle** y guárdala en una constante.
 *
 * ## Errores típicos
 * - `split('.')` esperando que trocee por puntos.
 * - `$1` en una cadena con comillas dobles, que Groovy interpola antes.
 * - Validar correos con una expresión regular y creer que es suficiente.
 *
 * Siguiente paso: capítulo 20, el GDK.
 */
@CompileStatic
class C19_00Index {

    static final Chapter CHAPTER = chapter(
        19,
        'Expresiones regulares',
        '~, =~ y ==~, grupos con nombre, replaceAll con closure y un log completo',
    ) {
        demo('Los tres operadores') { C19_01Regex.demoThreeOperators() }
        demo('Grupos, por número y por nombre') { C19_01Regex.demoGroups() }
        demo('Sustituir, incluso con un closure') { C19_01Regex.demoReplacing() }
        demo('En la práctica: validar y leer un log') { C19_01Regex.demoPractical() }
    }

    /** Ejecuta el capítulo 19 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
