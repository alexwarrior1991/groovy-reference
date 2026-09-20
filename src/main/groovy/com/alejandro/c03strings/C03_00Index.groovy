package com.alejandro.c03strings

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 03 · Cadenas y GString
 *
 * El texto es lo que más se escribe en cualquier programa, y es donde Groovy más se
 * separa de Java: seis literales distintos, interpolación, decenas de métodos añadidos
 * y un tipo nuevo, `GString`, que **no es un `String`** y que se comporta de forma
 * perezosa.
 *
 * ## El mecanismo, en dos líneas
 * ```
 * "Hola $nombre"     // no guarda el texto: guarda ['Hola '] y [nombre]
 *                    // y los junta cuando alguien llama a toString()
 * ```
 * Todo lo demás —la pereza, la trampa de la clave de mapa, el `equals` que dice
 * `false`— sale de ahí.
 *
 * ## Qué se cubre
 * - Los seis literales: `'…'`, `"…"`, `'''…'''`, `"""…"""`, `/…/` y `$/…/$`.
 * - Las dos sintaxis de interpolación y cuándo hacen falta las llaves.
 * - Qué hay dentro de un GString, y por qué su texto puede cambiar solo.
 * - La trampa de usar un GString como clave de mapa, y sus dos soluciones.
 * - Lo que el GDK añade a `String`: indexado, rangos, `tokenize`, `capitalize`,
 *   `padLeft`, `stripIndent`, `readLines` y compañía.
 * - Construir texto sin concatenar en un bucle.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Comilla simple por defecto.** Doble sólo cuando interpolas.
 * - **Comilla doble sin `$` sigue siendo un `String`**: el GString sólo aparece si hay
 *   algo que interpolar.
 * - **Un GString es perezoso**: si la expresión es un objeto mutable, el texto cambia
 *   después de escribirlo.
 * - **`gstring.equals(string)` es `false`** aunque `gstring == string` sea `true`. El
 *   subíndice `mapa[clave] = v` te salva porque convierte la clave, pero `put()`, el
 *   literal de mapa, los `Set` y `contains()` no: ahí acabas con dos claves que se
 *   imprimen igual.
 * - **Un GString es para imprimir.** En cuanto lo guardes: `.toString()`.
 * - `split` es expresión regular y devuelve array; `tokenize` es por caracteres y
 *   devuelve lista.
 *
 * ## Errores típicos
 * - Escribir una expresión regular con comillas dobles y pelearse con las barras.
 * - Meter GString en un `Set` o pasarlo a `map.put()` y acabar con duplicados
 *   invisibles.
 * - `'a.b'.split('.')` devolviendo una lista vacía, porque `.` es una expresión regular.
 *
 * Siguiente paso: capítulo 04, operadores.
 */
@CompileStatic
class C03_00Index {

    static final Chapter CHAPTER = chapter(
        3,
        'Cadenas y GString',
        'los seis literales, interpolación perezosa, la trampa de la clave de mapa',
    ) {
        demo('Comilla simple frente a comilla doble') { C03_01Literals.demoSingleVsDouble() }
        demo('Cadenas multilínea') { C03_01Literals.demoMultiline() }
        demo('Slashy: expresiones regulares y rutas') { C03_01Literals.demoSlashy() }
        demo('Cuál elegir: la tabla') { C03_01Literals.demoWhichToUse() }
        demo('Qué hay dentro de un GString') { C03_02GStringTraps.demoWhatIsInside() }
        demo('La pereza: el texto cambia solo') { C03_02GStringTraps.demoLaziness() }
        demo('La trampa: GString como clave de mapa') { C03_02GStringTraps.demoMapKeyTrap() }
        demo('GString frente a las APIs que piden String') { C03_02GStringTraps.demoInteropWithString() }
        demo('Indexar y trocear') { C03_03StringGdk.demoIndexingAndSlicing() }
        demo('Transformar texto con el GDK') { C03_03StringGdk.demoTransforming() }
        demo('Construir texto sin concatenar en bucle') { C03_03StringGdk.demoBuilding() }
        demo('Trabajar por líneas') { C03_03StringGdk.demoLines() }
    }

    /** Ejecuta el capítulo 03 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
