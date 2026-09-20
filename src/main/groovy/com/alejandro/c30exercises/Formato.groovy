package com.alejandro.c30exercises

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section

// =====================================================================================
//  El formato común de los ejercicios.
//
//  Los cinco bloques son siempre los mismos, en el mismo orden, para que se pueda
//  leer sólo el enunciado y cerrar el fichero sin destriparse la solución.
// =====================================================================================

class Formato {

    static void enunciado(String... lineas) {
        section('ENUNCIADO')
        lineas.each { bullet(it) }
    }

    static void pistas(String... lineas) {
        section('PISTAS')
        lineas.eachWithIndex { String l, int i -> bullet("${i + 1}. $l") }
    }

    static void solucionEnMarcha() {
        section('SOLUCIÓN, ejecutándose')
    }

    static void explicacion(String... lineas) {
        section('EXPLICACIÓN')
        lineas.each { bullet(it) }
    }

    static void varianteDificil(String... lineas) {
        section('VARIANTE DIFÍCIL')
        lineas.each { bullet(it) }
    }

    static void testEn(String fichero) {
        section('LA SOLUCIÓN ESTÁ PROBADA')
        bullet("src/test/groovy/com/alejandro/c30exercises/$fichero")
        bullet("./mvnw test -Dtest=${fichero - '.groovy'}")
    }
}
