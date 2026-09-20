package com.alejandro.infra

import groovy.transform.CompileStatic

// =====================================================================================
//  Utilidad compartida por los tests de `infra`.
//
//  Las demos de este repositorio imprimen por consola: son su razón de ser. Para poder
//  testearlas hay que redirigir `System.out` a un buffer y, MUY IMPORTANTE, volver a
//  dejarlo como estaba pase lo que pase. Ése es exactamente el caso de uso de
//  `try`/`finally` (capítulo 18).
// =====================================================================================

@CompileStatic
class CapturaDeSalida {

    /**
     * Ejecuta [bloque] capturando todo lo que imprima.
     *
     * Devuelve el fallo que lanzara (o `null`) y el texto que escribió, en ese orden.
     * Se devuelven los dos juntos porque el test de humo necesita comprobar las dos
     * cosas en la misma pasada: que no lanza y que no se queda muda.
     */
    static List capturando(Closure<?> bloque) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream()
        PrintStream original = System.out
        // `true` activa el autoflush; UTF-8 para que los acentos lleguen enteros.
        System.setOut(new PrintStream(buffer, true, 'UTF-8'))
        Throwable fallo = null
        try {
            bloque.call()
        } catch (Throwable e) {
            fallo = e
        } finally {
            // Sin este `finally`, un fallo dejaría la consola rota para el resto de la
            // suite y los informes saldrían vacíos.
            System.setOut(original)
        }
        [fallo, buffer.toString('UTF-8')]
    }

    /** Como [capturando], pero cuando el texto no interesa. */
    static void silenciando(Closure<?> bloque) {
        capturando(bloque)
    }
}
