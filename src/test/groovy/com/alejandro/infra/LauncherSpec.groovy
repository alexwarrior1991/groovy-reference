package com.alejandro.infra

import spock.lang.Specification
import spock.lang.Subject

import static com.alejandro.infra.Registry.chapter

// =====================================================================================
//  El lanzador: que entienda sus comandos.
//
//  Estos tests NO usan los capítulos de verdad: construyen dos capítulos de mentira con
//  demos que sólo apuntan su identificador en una lista. Así el test es instantáneo, no
//  imprime medio repositorio y, sobre todo, no se rompe cada vez que se añade un
//  capítulo nuevo.
//
//  Es el mismo principio que enseña el capítulo 28: un FAKE pequeño en lugar del
//  sistema real.
// =====================================================================================

class LauncherSpec extends Specification {

    /** Lo que han ejecutado las demos falsas, en orden. */
    List<String> ejecutadas = []

    @Subject
    Launcher lanzador

    def setup() {
        Chapter primero = chapter(1, 'Capítulo de prueba', 'resumen del primero') {
            demo('demo sobre colecciones') { ejecutadas << '1.1' }
            demo('demo sobre closures') { ejecutadas << '1.2' }
        }
        Chapter segundo = chapter(2, 'Otro capítulo', 'resumen del segundo') {
            demo('demo única') { ejecutadas << '2.1' }
        }
        lanzador = new Launcher([primero, segundo])
    }

    private void lanza(String... args) {
        CapturaDeSalida.silenciando { lanzador.start(args) }
    }

    def 'un número ejecuta el capítulo entero'() {
        when: lanza('1')
        then: ejecutadas == ['1.1', '1.2']
    }

    def 'un identificador con punto ejecuta sólo esa demo'() {
        when: lanza('1.2')
        then: ejecutadas == ['1.2']
    }

    def 'ejecuta todas las demos de todos los capítulos'() {
        when: lanza(comando)
        then: ejecutadas == ['1.1', '1.2', '2.1']

        // Una tabla `where:` en lugar de cuatro tests iguales: esto es Spock.
        where:
        comando << ['all', 'todo', '--all', 'ALL']
    }

    def 'los comandos de consulta no ejecutan ninguna demo'() {
        when: lanza(comando)
        then: ejecutadas == []

        where:
        comando << ['list', 'indice', 'índice', 'help', 'ayuda', '-h', '-l']
    }

    def 'un objetivo que no existe no ejecuta nada y no lanza'() {
        when: lanza(comando)
        then:
        ejecutadas == []
        noExceptionThrown()

        where:
        comando << ['99', '1.99', 'chorrada', '0']
    }

    def 'search encuentra por título de demo'() {
        when:
        def (Throwable fallo, String salida) = CapturaDeSalida.capturando {
            lanzador.start(['search', 'closures'] as String[])
        }

        then:
        fallo == null
        salida.contains('1.2')
        !salida.contains('2.1')
        ejecutadas == []
    }

    def 'search sin texto explica cómo se usa'() {
        when:
        def (Throwable fallo, String salida) = CapturaDeSalida.capturando {
            lanzador.start(['search'] as String[])
        }

        then:
        salida.contains('Uso: search')
    }

    def 'el índice lista los dos capítulos y sus tres demos'() {
        when:
        def (Throwable fallo, String salida) = CapturaDeSalida.capturando {
            lanzador.start(['list'] as String[])
        }

        then:
        salida.contains('Capítulo de prueba')
        salida.contains('Otro capítulo')
        salida.contains('3 demos en 2 capítulos')
    }
}
