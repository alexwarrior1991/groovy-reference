package com.alejandro.infra

import com.alejandro.Main
import spock.lang.Specification

// =====================================================================================
//  La red de seguridad del registro de capítulos.
//
//  No prueba Groovy: prueba que este repositorio es coherente. Con treinta capítulos,
//  el error más probable es olvidarse de registrar uno en `Main.groovy` o copiar un
//  `CNN_00Index` y no cambiarle el número. Estos tests lo detectan al instante y
//  cuestan cuatro líneas cada uno.
// =====================================================================================

class RegistrySpec extends Specification {

    def 'los capítulos van del 1 al último, sin huecos ni repetidos'() {
        expect:
        Main.CHAPTERS*.number == (1..Main.CHAPTERS.size()).toList()
    }

    def 'ningún capítulo está vacío'() {
        expect:
        Main.CHAPTERS.findAll { Chapter c -> c.demos.isEmpty() }*.number == []
    }

    def 'los identificadores de demo son únicos en todo el registro'() {
        expect:
        Main.CHAPTERS*.demos.flatten()
            .groupBy { Demo d -> d.id }
            .findAll { String id, List demos -> demos.size() > 1 }
            .keySet() as List == []
    }

    def 'el id de cada demo empieza por el número de su capítulo y va correlativo'() {
        expect:
        // Un `where:` con los capítulos de verdad: si falla, Spock dice cuál.
        capitulo.demos*.id == (1..capitulo.demos.size()).collect { "${capitulo.number}.${it}" }

        where:
        capitulo << Main.CHAPTERS
    }

    def 'ningún nombre, resumen ni título está en blanco'() {
        expect:
        capitulo.name.trim()
        capitulo.summary.trim()
        capitulo.demos.every { Demo d -> d.title.trim() }

        where:
        capitulo << Main.CHAPTERS
    }

    def 'el identificador del capítulo se escribe con dos dígitos'() {
        expect:
        Main.CHAPTERS.first().id == '01'
        Main.CHAPTERS.last().id == Main.CHAPTERS.size().toString().padLeft(2, '0')
    }

    def 'find devuelve la demo pedida y null si no existe'() {
        given:
        Chapter capitulo = Main.CHAPTERS.first()
        Demo primera = capitulo.demos.first()

        expect:
        capitulo.find(primera.id).is(primera)
        capitulo.find("${capitulo.number}.999") == null
    }
}
