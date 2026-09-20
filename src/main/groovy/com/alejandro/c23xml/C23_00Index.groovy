package com.alejandro.c23xml

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 23 · XML
 *
 * Navegar XML en Groovy se parece a navegar un mapa: `catalogo.libro[0].titulo.text()`.
 * Sin XPath, sin DOM y sin doscientas líneas de ceremonia.
 *
 * ## Las cuatro piezas
 * ```
 * XmlSlurper               leer y navegar · perezoso · SÓLO LECTURA
 * XmlParser                leer y MODIFICAR el árbol
 * MarkupBuilder            escribir XML o HTML con un DSL
 * StreamingMarkupBuilder   lo mismo, sin árbol en memoria
 * ```
 *
 * ## Qué se cubre
 * - Navegar, atributos con `@`, `**` para bajar a cualquier profundidad.
 * - Filtrar y agregar con los métodos de colección de siempre.
 * - `XmlParser` modificando, añadiendo y quitando nodos, y volviendo a serializar.
 * - `MarkupBuilder` con bucles dentro del DSL, y el escapado automático.
 * - Espacios de nombres, con y sin declararlos.
 * - **XXE**: cómo parsear XML que viene de fuera sin abrir un agujero.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **`.text()` es obligatorio.** Sin él tienes un nodo, no una cadena, y comparar
 *   un nodo con `'algo'` siempre da `false`.
 * - **Si sólo lees, `XmlSlurper`. Si modificas, `XmlParser`.** El Slurper no deja
 *   cambiar el árbol.
 * - **Un nodo que no existe no lanza**: devuelve una colección vacía. Cómodo, pero
 *   una errata en el nombre pasa inadvertida. Comprueba `size()`.
 * - **Un builder escapa solo.** Concatenar cadenas para generar XML es como se
 *   generan documentos rotos y vulnerabilidades de inyección.
 * - **XML de fuera: prohíbe el DOCTYPE** y desactiva las entidades externas. Groovy 4+
 *   ya lo trae desactivado por defecto, pero si construyes el parser a mano, ponlo.
 *
 * Siguiente paso: capítulo 24, builders y DSLs.
 */
@CompileStatic
class C23_00Index {

    static final Chapter CHAPTER = chapter(
        23,
        'XML',
        'XmlSlurper frente a XmlParser, MarkupBuilder, espacios de nombres y XXE',
    ) {
        demo('Navegar con XmlSlurper') { C23_01Xml.demoSlurper() }
        demo('XmlParser: modificar el árbol') { C23_01Xml.demoParser() }
        demo('Generar XML y HTML con builders') { C23_01Xml.demoBuilders() }
        demo('Espacios de nombres y seguridad (XXE)') { C23_01Xml.demoNamespacesAndSafety() }
    }

    /** Ejecuta el capítulo 23 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
