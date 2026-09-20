package com.alejandro.c30exercises

import groovy.transform.Canonical

import static com.alejandro.c30exercises.Formato.enunciado
import static com.alejandro.c30exercises.Formato.explicacion
import static com.alejandro.c30exercises.Formato.pistas
import static com.alejandro.c30exercises.Formato.solucionEnMarcha
import static com.alejandro.c30exercises.Formato.testEn
import static com.alejandro.c30exercises.Formato.varianteDificil
import static com.alejandro.infra.Console.show

// =====================================================================================
//  Ejercicio 1 · Calculadora de expresiones                                 🟢 fácil
//
//  Repasa: switch con flecha, enum estrategia, sealed + record, validación.
//  Capítulos: 05 (switch), 13 (enums y sealed), 18 (errores como datos).
// =====================================================================================

/** Lo que devuelve un cálculo: o sale bien, o sale mal con un motivo. */
sealed interface ResultadoCalculo permits Valor, ErrorCalculo {}

@Canonical
class Valor implements ResultadoCalculo {
    BigDecimal cantidad
}

@Canonical
class ErrorCalculo implements ResultadoCalculo {
    String motivo
}

/** El enum estrategia del capítulo 13: cada operación sabe aplicarse. */
enum Operacion {
    SUMA('+') {
        @Override
        ResultadoCalculo aplicar(BigDecimal a, BigDecimal b) { new Valor(a + b) }
    },
    RESTA('-') {
        @Override
        ResultadoCalculo aplicar(BigDecimal a, BigDecimal b) { new Valor(a - b) }
    },
    MULTIPLICACION('*') {
        @Override
        ResultadoCalculo aplicar(BigDecimal a, BigDecimal b) { new Valor(a * b) }
    },
    DIVISION('/') {
        @Override
        ResultadoCalculo aplicar(BigDecimal a, BigDecimal b) {
            b == 0 ? new ErrorCalculo('división entre cero') : new Valor(a / b)
        }
    }

    final String simbolo

    Operacion(String simbolo) { this.simbolo = simbolo }

    abstract ResultadoCalculo aplicar(BigDecimal a, BigDecimal b)

    static Operacion porSimbolo(String simbolo) { values().find { it.simbolo == simbolo } }
}

class Calculadora {

    /**
     * Evalúa una expresión de la forma "número operador número".
     *
     * Devuelve un ResultadoCalculo: nunca lanza por una entrada mal formada, porque
     * una entrada mal formada es algo ESPERADO (capítulo 18).
     */
    static ResultadoCalculo evaluar(String expresion) {
        if (!expresion?.trim()) return new ErrorCalculo('la expresión está vacía')

        List<String> partes = expresion.trim().split(/\s+/).toList()
        if (partes.size() != 3) {
            return new ErrorCalculo("se esperaban 3 elementos y hay ${partes.size()}")
        }

        def (String izquierda, String simbolo, String derecha) = partes

        if (!izquierda.isBigDecimal()) return new ErrorCalculo("'$izquierda' no es un número")
        if (!derecha.isBigDecimal()) return new ErrorCalculo("'$derecha' no es un número")

        Operacion operacion = Operacion.porSimbolo(simbolo)
        if (!operacion) return new ErrorCalculo("operador desconocido: '$simbolo'")

        operacion.aplicar(izquierda.toBigDecimal(), derecha.toBigDecimal())
    }

    /** Lo mismo, pero devolviendo texto: útil para una interfaz de línea de comandos. */
    static String evaluarComoTexto(String expresion) {
        switch (evaluar(expresion)) {
            case Valor -> (evaluar(expresion) as Valor).cantidad.toString()
            case ErrorCalculo -> "error: ${(evaluar(expresion) as ErrorCalculo).motivo}"
            default -> 'resultado desconocido'
        }
    }
}

class E01Calculadora {

    static void ejecutar() {
        enunciado(
            'Escribe una calculadora que evalúe expresiones de la forma',
            '"número operador número", por ejemplo "12.5 * 4".',
            '',
            'Requisitos:',
            '· Soporta +, -, * y /.',
            '· Los decimales tienen que ser EXACTOS: 0.1 + 0.2 debe dar 0.3.',
            '· Una entrada mal formada NO debe lanzar: devuelve un error tratable.',
            '· La división entre cero también es un error, no una excepción.',
            '· Añadir una operación nueva no debe obligar a tocar el evaluador.',
        )

        pistas(
            'Un literal decimal ya es BigDecimal: no hagas nada raro (capítulo 02).',
            'Para "no lanzar", el resultado tiene que ser un DATO: sealed + record.',
            'Para "añadir una operación sin tocar el evaluador", enum estrategia.',
            '`isBigDecimal()` te dice si una cadena es un número antes de convertirla.',
            'Trocea con `split(/\\s+/)` para tolerar varios espacios.',
        )

        solucionEnMarcha()

        show('"2 + 3"', Calculadora.evaluar('2 + 3'))
        show('"12.5 * 4"', Calculadora.evaluar('12.5 * 4'))
        show('"0.1 + 0.2"', Calculadora.evaluar('0.1 + 0.2'))
        show('¿es exactamente 0.3?', (Calculadora.evaluar('0.1 + 0.2') as Valor).cantidad == 0.3)
        show('"10 / 4"', Calculadora.evaluar('10 / 4'))
        show('"10 / 0"', Calculadora.evaluar('10 / 0'))
        show('"dos + tres"', Calculadora.evaluar('dos + tres'))
        show('"2 ^ 3"', Calculadora.evaluar('2 ^ 3'))
        show('"2 +"', Calculadora.evaluar('2 +'))
        show('""', Calculadora.evaluar(''))
        show('como texto', Calculadora.evaluarComoTexto('7 * 6'))

        explicacion(
            'El resultado es un `sealed interface` con dos records: quien llama tiene',
            'que decidir qué hace en cada caso, y el compilador le recuerda que hay dos.',
            '',
            'Las operaciones son un enum con un método ABSTRACTO. Añadir POTENCIA es',
            'añadir una constante: el compilador obliga a implementar `aplicar`, y el',
            'evaluador no se toca. Si fuera un `switch` sobre una cadena, habría que',
            'buscar todos los sitios donde se usa.',
            '',
            'La división entre cero devuelve ErrorCalculo en vez de lanzar porque es',
            'un caso ESPERADO en una calculadora. Un fichero de configuración corrupto',
            'sería otra cosa (capítulo 18).',
            '',
            'Y los decimales son exactos sin hacer nada: es el valor por defecto de',
            'Groovy (capítulo 02).',
        )

        varianteDificil(
            'Soporta expresiones con varios operadores y precedencia: "2 + 3 * 4".',
            'Pista: un parser descendente recursivo cabe en cuarenta líneas.',
            '',
            'Soporta paréntesis.',
            'Soporta funciones: "sqrt 16", "max 3 7".',
            'Y hazlo sin usar GroovyShell: evaluar la entrada del usuario como código',
            'es exactamente lo que el capítulo 25 dice que no hagas.',
        )

        testEn('CalculadoraSpec.groovy')
    }
}
