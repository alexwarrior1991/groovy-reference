package com.alejandro.c30exercises

import spock.lang.Specification
import spock.lang.Unroll

class CalculadoraSpec extends Specification {

    @Unroll
    def 'evaluar "#expresion" da #esperado'() {
        expect:
        (Calculadora.evaluar(expresion) as Valor).cantidad == esperado

        where:
        expresion     || esperado
        '2 + 3'       || 5
        '10 - 4'      || 6
        '6 * 7'       || 42
        '10 / 4'      || 2.5
        '-3 + 5'      || 2
        '2.5 * 4'     || 10.0
        '0 + 0'       || 0
    }

    def 'los decimales son exactos, no de coma flotante'() {
        expect:
        (Calculadora.evaluar('0.1 + 0.2') as Valor).cantidad == 0.3
        (Calculadora.evaluar('1.1 * 3') as Valor).cantidad == 3.3
    }

    def 'tolera varios espacios entre los elementos'() {
        expect:
        (Calculadora.evaluar('  2   +   3  ') as Valor).cantidad == 5
    }

    @Unroll
    def 'evaluar "#expresion" da un error que dice "#fragmento"'() {
        when:
        def resultado = Calculadora.evaluar(expresion)

        then:
        resultado instanceof ErrorCalculo
        resultado.motivo.contains(fragmento)

        where:
        expresion      || fragmento
        '10 / 0'       || 'división entre cero'
        'dos + tres'   || 'no es un número'
        '2 + tres'     || 'no es un número'
        '2 ^ 3'        || 'operador desconocido'
        '2 +'          || 'se esperaban 3'
        '2 + 3 + 4'    || 'se esperaban 3'
        ''             || 'vacía'
        '   '          || 'vacía'
    }

    def 'una entrada mal formada NUNCA lanza'() {
        when:
        Calculadora.evaluar(entrada)

        then:
        noExceptionThrown()

        where:
        entrada << [null, '', '???', '1 1 1', 'a b c', '/ / /']
    }

    def 'el enum conoce sus símbolos'() {
        expect:
        Operacion.porSimbolo('+') == Operacion.SUMA
        Operacion.porSimbolo('/') == Operacion.DIVISION
        Operacion.porSimbolo('^') == null
        Operacion.values().size() == 4
    }

    def 'cada operación sabe aplicarse sola'() {
        expect:
        (Operacion.SUMA.aplicar(2, 3) as Valor).cantidad == 5
        (Operacion.DIVISION.aplicar(1, 0) as ErrorCalculo).motivo == 'división entre cero'
    }

    def 'evaluarComoTexto devuelve algo legible en los dos casos'() {
        expect:
        Calculadora.evaluarComoTexto('7 * 6') == '42'
        Calculadora.evaluarComoTexto('1 / 0').startsWith('error:')
    }
}
