package com.alejandro.c30exercises

import spock.lang.Specification
import spock.lang.Unroll

class MiniDslSpec extends Specification {

    Validador validador = Validador.definir {
        campo('nombre') {
            obligatorio()
            longitudMinima(3)
        }
        campo('edad') {
            obligatorio()
            entre(18, 120)
        }
        campo('email') {
            formato(/^[\w.+-]+@[\w-]+\.[\w.]{2,}$/)
        }
    }

    def 'unos datos correctos son válidos'() {
        when:
        def resultado = validador.validar([nombre: 'Ana García', edad: 30, email: 'ana@ejemplo.com'])

        then:
        resultado.valido
        resultado.errores.isEmpty()
    }

    def 'un resultado válido es "cierto" para el Groovy Truth'() {
        expect:
        validador.validar([nombre: 'Ana', edad: 30]) ? true : false
        !(validador.validar([:]) ? true : false)
    }

    def 'devuelve TODOS los errores, no sólo el primero'() {
        when:
        def resultado = validador.validar([nombre: 'Al', edad: 15, email: 'no-vale'])

        then:
        !resultado.valido
        resultado.errores.size() == 3
        resultado.porCampo().keySet() == ['nombre', 'edad', 'email'] as Set
    }

    @Unroll
    def 'el campo nombre con "#valor" da #cuantos error(es)'() {
        expect:
        validador.validar([nombre: valor, edad: 30]).porCampo().nombre?.size() ?: 0 == cuantos

        where:
        valor      || cuantos
        'Ana'      || 0
        'Ana G'    || 0
        'Al'       || 1
        ''         || 1
        null       || 1
    }

    @Unroll
    def 'la edad #edad es válida: #esperado'() {
        expect:
        (validador.validar([nombre: 'Ana', edad: edad]).porCampo().edad == null) == esperado

        where:
        edad || esperado
        18   || true
        120  || true
        65   || true
        17   || false
        121  || false
        null || false
    }

    def 'sobre un valor ausente sólo dispara `obligatorio`, no las demás reglas'() {
        when:
        def resultado = validador.validar([:])

        then: 'un null da UN error por campo, no uno por regla'
        resultado.porCampo().nombre == ['es obligatorio']
        resultado.porCampo().edad == ['es obligatorio']

        and: 'y un campo sin `obligatorio` no da ninguno'
        resultado.porCampo().email == null
    }

    def 'es una decisión de diseño: las reglas de forma se saltan los nulos'() {
        given: 'un campo con longitud mínima pero SIN obligatorio'
        def opcional = Validador.definir {
            campo('apodo') { longitudMinima(3) }
        }

        expect: 'ausente es válido: no estaba obligado a venir'
        opcional.validar([:]).valido

        and: 'pero si viene, tiene que cumplir'
        !opcional.validar([apodo: 'ab']).valido
        opcional.validar([apodo: 'abc']).valido
    }

    def 'la regla de escape acepta cualquier condición'() {
        given:
        def conEscape = Validador.definir {
            campo('alias') {
                cumple('no puede tener espacios') { v -> v == null || !v.toString().contains(' ') }
            }
        }

        expect:
        conEscape.validar([alias: 'ana_g']).valido
        !conEscape.validar([alias: 'con espacios']).valido
        conEscape.validar([alias: 'con espacios']).errores[0].mensaje == 'no puede tener espacios'
    }

    def 'el ámbito del bloque interior está cerrado'() {
        when: 'se intenta declarar un campo dentro de otro campo'
        Validador.definir {
            campo('a') {
                campo('b') { obligatorio() }
            }
        }

        then: 'DELEGATE_ONLY lo impide'
        thrown(MissingMethodException)
    }

    def 'un validador sin campos se rechaza al construirlo'() {
        when:
        Validador.definir { }

        then:
        def e = thrown(IllegalStateException)
        e.message.contains('sin campos')
    }

    def 'las reglas de un mismo campo se acumulan entre bloques'() {
        given:
        def acumulado = Validador.definir {
            campo('x') { obligatorio() }
            campo('x') { longitudMinima(5) }
        }

        expect: 'las dos reglas están activas sobre el mismo campo'
        acumulado.validar([x: 'abc']).errores.size() == 1    // sólo la longitud
        acumulado.validar([x: 'abcde']).valido
        acumulado.validar([x: null]).errores.size() == 1     // sólo obligatorio
    }
}
