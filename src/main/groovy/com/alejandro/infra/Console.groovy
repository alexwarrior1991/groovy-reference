package com.alejandro.infra

import groovy.transform.CompileStatic

// =====================================================================================
//  Utilidades de impresión por consola.
//
//  Su único objetivo es que las demos del repositorio tengan TODAS el mismo aspecto:
//  así, cuando leas la salida de un capítulo que no conoces, ya sabes dónde mirar.
//  No aprendas nada de Groovy aquí; empieza por el capítulo 01.
// =====================================================================================

@CompileStatic
class Console {

    /** Ancho de los separadores. 88 columnas entran en cualquier terminal razonable. */
    static final int WIDTH = 88

    /** Línea horizontal de separación. */
    static void separator(String ch = '─') {
        println ch * WIDTH
    }

    /** Banner grande. Se usa para abrir un capítulo entero. */
    static void heading(String text) {
        println ''
        separator('━')
        println "  $text"
        separator('━')
    }

    /** Cabecera de una demo concreta, con su identificador ("13.4"). */
    static void demoHeader(String id, String title) {
        println ''
        println "▶ $id · $title"
        separator()
    }

    /**
     * Sub-apartado dentro de una demo. Úsalo para separar las variantes de un mismo
     * concepto (por ejemplo: "con each" / "con for in").
     */
    static void section(String text) {
        println ''
        println "· $text"
    }

    /** Viñeta para una explicación suelta. */
    static void bullet(String text) {
        println "    - $text"
    }

    /**
     * Imprime `etiqueta → valor` con las columnas alineadas.
     *
     * Es la forma de salida más usada del repositorio: al quedar todo alineado, comparar
     * dos resultados parecidos (por ejemplo `==` frente a `is()`) es inmediato.
     */
    static void show(String label, Object value) {
        printf('    %-44s %s%n', label, valueToText(value))
    }

    /**
     * Entrecomilla un texto para que se vean sus bordes.
     *
     * Úsalo cuando lo importante sea el contenido exacto de una cadena: sin comillas,
     * `'  hola  '.trim()` y `'hola'` se imprimen igual y no se aprecia qué ha pasado.
     */
    static String quoted(String text) {
        text == null ? 'null' : "\"$text\""
    }

    /**
     * Representación legible de un valor.
     *
     * `toString()` de un array imprime algo como `[I@5ca881b5`, que no dice nada; aquí lo
     * convertimos en su contenido real. Es un detalle pequeño pero evita mucha confusión
     * en el capítulo 02, donde los arrays aparecen por primera vez.
     *
     * Las cadenas se imprimen tal cual, sin comillas: la mayoría de las veces el valor
     * que se muestra es una explicación, no un dato. Cuando quieras ver las comillas,
     * envuelve el valor con [quoted].
     */
    private static String valueToText(Object value) {
        if (value == null) return 'null'
        // `.class.isArray()` cubre de una vez los arrays de objetos y los de primitivos.
        if (value.class.isArray()) return arrayToText(value)
        value.toString()
    }

    /**
     * Contenido de un array, incluidos los de primitivos y los anidados.
     *
     * `java.lang.reflect.Array` es la única forma de recorrer un `int[]` y un `String[]`
     * con el mismo código: en la JVM no comparten supertipo más allá de `Object`.
     */
    private static String arrayToText(Object array) {
        int n = java.lang.reflect.Array.getLength(array)
        List<String> items = new ArrayList<String>(n)
        for (int i = 0; i < n; i++) {
            Object item = java.lang.reflect.Array.get(array, i)
            items.add(item != null && item.class.isArray() ? arrayToText(item) : String.valueOf(item))
        }
        '[' + items.join(', ') + ']'
    }
}
