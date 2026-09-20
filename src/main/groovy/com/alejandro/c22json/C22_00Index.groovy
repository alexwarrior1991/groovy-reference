package com.alejandro.c22json

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 22 · JSON
 *
 * Un JSON parseado en Groovy es un **mapa normal**. No hay clases generadas ni
 * anotaciones: todo lo del capítulo 09 funciona directamente sobre él.
 *
 * ## Las cuatro piezas
 * ```
 * JsonSlurper     texto → mapas y listas
 * JsonOutput      objetos → texto, y prettyPrint
 * JsonBuilder     construirlo con un DSL
 * JsonGenerator   controlar CÓMO se serializa
 * ```
 *
 * ## Qué se cubre
 * - Parsear y navegar, incluida la ruta que no existe con `?.`.
 * - Consultar como cualquier colección: `findAll`, `groupBy`, `collectEntries`.
 * - Generar con `toJson`, `prettyPrint` y el DSL de `JsonBuilder`.
 * - `JsonGenerator` para excluir campos, quitar nulos y convertir fechas.
 * - Un caso completo: JSON → objetos de dominio → JSON controlado.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Serializar un objeto entero "porque es cómodo" es como acaban las contraseñas
 *   en los logs.** Configura un `JsonGenerator` o construye un mapa explícito con lo
 *   que de verdad quieres publicar.
 * - **`toJson` serializa las PROPIEDADES**, así que los getters calculados también
 *   salen, aunque no haya campo detrás.
 * - **Los decimales se parsean como `BigDecimal`**, coherente con el capítulo 02.
 * - **La conversión a objetos de dominio es donde se valida.** Un mapa acepta
 *   cualquier cosa; el constructor de tu clase, no.
 * - `parseText` de un JSON roto lanza: con datos de fuera, captúralo.
 *
 * Siguiente paso: capítulo 23, XML.
 */
@CompileStatic
class C22_00Index {

    static final Chapter CHAPTER = chapter(
        22,
        'JSON',
        'JsonSlurper, JsonOutput, JsonBuilder y JsonGenerator para controlar qué sale',
    ) {
        demo('Parsear y navegar') { C22_01Json.demoParsing() }
        demo('Generar: toJson y JsonBuilder') { C22_01Json.demoGenerating() }
        demo('JsonGenerator: controlar qué se publica') { C22_01Json.demoGenerator() }
        demo('Ida y vuelta completa') { C22_01Json.demoRoundTrip() }
    }

    /** Ejecuta el capítulo 22 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
