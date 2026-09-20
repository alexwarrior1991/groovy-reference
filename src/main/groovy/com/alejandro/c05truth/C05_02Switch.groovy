package com.alejandro.c05truth

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  05.2 · El switch que clasifica
//
//  QUÉ ES
//    El `switch` de Groovy no compara con `==`: llama a `caso.isCase(valor)`. Como cada
//    tipo implementa `isCase` a su manera, un `case` puede ser un valor, un rango, una
//    lista, una clase, una expresión regular o un closure.
//
//  POR QUÉ IMPORTA
//    Convierte el switch de Java —limitado a enteros, cadenas y enums— en una
//    herramienta de clasificación general. Muchas cadenas de `if/else if` se escriben
//    mucho mejor como un switch.
//
//  ERRORES COMUNES
//    · Olvidar el `break` en la sintaxis clásica: sigue cayendo al siguiente caso.
//    · Poner los casos generales antes que los específicos: gana el PRIMERO que encaja.
//    · Usar `switch` sobre un tipo cuando una jerarquía con polimorfismo diría más.
// =====================================================================================

class C05_02Switch {

    /**
     * Las dos sintaxis.
     */
    static void demoTwoSyntaxes() {
        section('La clásica, heredada de Java: necesita `break`')

        show('clasificaClasico(3)', clasificaClasico(3))
        show('clasificaClasico(99)', clasificaClasico(99))

        section('Sin el `break`, cae al siguiente caso')

        show('sinBreak(1)', sinBreak(1))
        bullet('Devuelve "uno y dos" porque ejecuta los dos bloques. Es el clásico bug.')

        section('La sintaxis con flecha: no cae, y no hace falta break')

        show('clasificaFlecha(3)', clasificaFlecha(3))
        show('clasificaFlecha(99)', clasificaFlecha(99))
        bullet('Añadida en Groovy 4. Es la que hay que usar en código nuevo.')

        section('Y además es una EXPRESIÓN: devuelve valor')

        def nota = 75
        String resultado = switch (nota) {
            case 0..<50 -> 'suspenso'
            case 50..<70 -> 'aprobado'
            case 70..<90 -> 'notable'
            default -> 'sobresaliente'
        }
        show('switch como expresión', resultado)
        bullet('Se asigna directamente. Ya no hace falta una variable mutable.')

        section('Con varias líneas, `yield` devuelve el valor')

        show('con yield', conYield(4))
    }

    private static String clasificaClasico(int n) {
        String r
        switch (n) {
            case 1..10:
                r = 'del uno al diez'
                break
            default:
                r = 'fuera'
        }
        r
    }

    private static String sinBreak(int n) {
        List<String> pasos = []
        switch (n) {
            case 1:
                pasos << 'uno'
                // falta el break, a propósito
            case 2:
                pasos << 'dos'
                break
            default:
                pasos << 'otro'
        }
        pasos.join(' y ')
    }

    private static String clasificaFlecha(int n) {
        switch (n) {
            case 1..10 -> 'del uno al diez'
            default -> 'fuera'
        }
    }

    private static String conYield(int n) {
        switch (n) {
            case 1..10 -> {
                String tipo = n % 2 == 0 ? 'par' : 'impar'
                yield "$n es $tipo y está en el rango"
            }
            default -> 'fuera'
        }
    }

    /**
     * Lo que puede ir en un `case`.
     */
    static void demoWhatCanBeACase() {
        section('Un valor, como en Java')

        show("porValor('alta')", porValor('alta'))

        section('Un RANGO')

        show('porRango(5)', porRango(5))
        show('porRango(50)', porRango(50))

        section('Una LISTA: encaja si el valor está dentro')

        show('porLista("sábado")', porLista('sábado'))
        show('porLista("lunes")', porLista('lunes'))

        section('Una CLASE: encaja si es de ese tipo')

        show('porTipo(42)', porTipo(42))
        show('porTipo("texto")', porTipo('texto'))
        show('porTipo([1, 2])', porTipo([1, 2]))
        show('porTipo(3.5)', porTipo(3.5))
        bullet('`Class.isCase(x)` es `isInstance(x)`: cubre también las subclases.')

        section('Una EXPRESIÓN REGULAR')

        show('porRegex("A-123")', porRegex('A-123'))
        show('porRegex("hola")', porRegex('hola'))

        section('Un CLOSURE: la condición que quieras')

        show('porClosure(10)', porClosure(10))
        show('porClosure(7)', porClosure(7))
        bullet('El closure recibe el valor y devuelve un booleano. Es el caso general.')

        section('El orden importa: gana el PRIMERO que encaja')

        show('ordenMal(5)', ordenMal(5))
        show('ordenBien(5)', ordenBien(5))
        bullet('Con el caso general arriba, los específicos no se alcanzan nunca.')
    }

    private static String porValor(String prioridad) {
        switch (prioridad) {
            case 'alta' -> 'atender ya'
            case 'baja' -> 'cuando se pueda'
            default -> 'normal'
        }
    }

    private static String porRango(int n) {
        switch (n) {
            case 1..9 -> 'un dígito'
            case 10..99 -> 'dos dígitos'
            default -> 'muchos dígitos'
        }
    }

    private static String porLista(String dia) {
        switch (dia) {
            case ['sábado', 'domingo'] -> 'fin de semana'
            default -> 'laborable'
        }
    }

    private static String porTipo(Object valor) {
        switch (valor) {
            case Integer -> 'un entero'
            case Number -> 'otro número'
            case CharSequence -> 'texto'
            case List -> "una lista de ${valor.size()}"
            default -> 'ni idea'
        }
    }

    private static String porRegex(String codigo) {
        switch (codigo) {
            case ~/[A-Z]-\d{3}/ -> 'código válido'
            default -> 'no es un código'
        }
    }

    private static String porClosure(int n) {
        switch (n) {
            case { it % 2 == 0 } -> 'par'
            case { it > 5 } -> 'impar y mayor que cinco'
            default -> 'impar y pequeño'
        }
    }

    private static String ordenMal(int n) {
        switch (n) {
            case Number -> 'un número'
            case 1..9 -> 'un dígito'
            default -> 'otro'
        }
    }

    private static String ordenBien(int n) {
        switch (n) {
            case 1..9 -> 'un dígito'
            case Number -> 'un número'
            default -> 'otro'
        }
    }

    /**
     * isCase propio: meter tus clases en un switch.
     */
    static void demoCustomIsCase() {
        section('Definiendo isCase(), tu clase clasifica')

        def mayorDeEdad = new Mayor(18)

        show('clasificaEdad(20)', clasificaEdad(20, mayorDeEdad))
        show('clasificaEdad(10)', clasificaEdad(10, mayorDeEdad))

        section('Y de paso funciona con `in`')

        show('20 in mayorDeEdad', 20 in mayorDeEdad)
        show('10 in mayorDeEdad', 10 in mayorDeEdad)
        bullet('`in` y `switch` comparten `isCase`: defines uno y tienes los dos.')

        section('Cuándo merece la pena')

        bullet('Cuando tienes un criterio con nombre que se repite: Mayor, Valido,')
        bullet('EnHorario, Premium. El switch queda leyéndose como el dominio.')

        section('Cuándo NO usar switch')

        bullet('Si cada rama es un comportamiento de un tipo, usa polimorfismo.')
        bullet('Si el switch sobre tipos crece, probablemente falta una interfaz.')
        bullet('Regla: si añadir un caso obliga a tocar varios switch, estás mal.')
    }

    private static String clasificaEdad(int edad, Mayor criterio) {
        switch (edad) {
            case criterio -> 'puede pasar'
            default -> 'no puede pasar'
        }
    }

    /** Un criterio con nombre. `isCase` lo hace usable en `switch` y en `in`. */
    static class Mayor {
        final int limite

        Mayor(int limite) { this.limite = limite }

        boolean isCase(Object valor) { valor instanceof Number && valor >= limite }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade un `break` al primer caso de `sinBreak` y comprueba la diferencia.
//  2. Cambia el orden de los casos de `porTipo` poniendo `Number` el primero.
//  3. Escribe un `case` con un closure que compruebe dos cosas a la vez.
//  4. Dale a `Mayor` un `toString()` y úsalo dentro del propio mensaje del switch.
//  5. Quita el `default` de `clasificaFlecha` y llámalo con 99: mira qué devuelve.
