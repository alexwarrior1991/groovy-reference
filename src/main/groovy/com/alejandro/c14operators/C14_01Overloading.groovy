package com.alejandro.c14operators

import groovy.transform.Canonical
import groovy.transform.Immutable

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  14.1 · Sobrecargar operadores
//
//  QUÉ ES
//    Todo operador de Groovy es una llamada a un método con un nombre fijo (la tabla
//    del capítulo 04). Implementar ese método en tu clase es todo lo que hace falta
//    para que el operador funcione: no hay palabra clave ni anotación.
//
//  POR QUÉ IMPORTA
//    En un dominio con aritmética —dinero, medidas, vectores, fechas, matrices— la
//    diferencia entre `a.sumar(b).multiplicar(3)` y `(a + b) * 3` es enorme al leer.
//
//  ERRORES COMUNES
//    · Sobrecargar operadores donde no significan nada obvio.
//    · Devolver `this` mutado en vez de un objeto nuevo.
//    · Implementar `plus` y olvidar `equals`, `hashCode` y `compareTo`.
// =====================================================================================

/**
 * Dinero: el ejemplo canónico.
 *
 * Guarda CÉNTIMOS en un entero, no euros en un decimal: es la forma correcta de
 * representar dinero, y de paso evita el debate del capítulo 02.
 */
@Immutable
class Dinero implements Comparable<Dinero> {
    long centimos
    String moneda

    static Dinero euros(BigDecimal cantidad) {
        new Dinero((cantidad * 100).toBigInteger().longValue(), 'EUR')
    }

    Dinero plus(Dinero otro) {
        comprobarMoneda(otro)
        new Dinero(centimos + otro.centimos, moneda)
    }

    Dinero minus(Dinero otro) {
        comprobarMoneda(otro)
        new Dinero(centimos - otro.centimos, moneda)
    }

    Dinero multiply(int veces) { new Dinero(centimos * veces, moneda) }

    Dinero div(int partes) { new Dinero((centimos / partes).toLong(), moneda) }

    Dinero negative() { new Dinero(-centimos, moneda) }

    /** Habilita `-dinero` y también `dinero.abs()` a través del GDK. */
    Dinero positive() { new Dinero(Math.abs(centimos), moneda) }

    @Override
    int compareTo(Dinero otro) {
        comprobarMoneda(otro)
        centimos <=> otro.centimos
    }

    /** El Groovy Truth: un importe de cero es "falso". */
    boolean asBoolean() { centimos != 0 }

    @Override
    String toString() {
        String signo = centimos < 0 ? '-' : ''
        long abs = Math.abs(centimos)
        "$signo${abs.intdiv(100)},${(abs % 100).toString().padLeft(2, '0')} $moneda"
    }

    private void comprobarMoneda(Dinero otro) {
        if (moneda != otro.moneda) {
            throw new IllegalArgumentException("no se pueden mezclar $moneda y ${otro.moneda}")
        }
    }
}

/** Un vector en dos dimensiones. */
@Canonical
class Vector2D {
    double x
    double y

    Vector2D plus(Vector2D otro) { new Vector2D(x + otro.x, y + otro.y) }

    Vector2D minus(Vector2D otro) { new Vector2D(x - otro.x, y - otro.y) }

    Vector2D multiply(double escalar) { new Vector2D(x * escalar, y * escalar) }

    Vector2D negative() { new Vector2D(-x, -y) }

    /** Producto escalar: el operador `*` entre dos vectores. */
    double multiply(Vector2D otro) { x * otro.x + y * otro.y }

    double getModulo() { Math.sqrt(x * x + y * y) }

    @Override
    String toString() { "($x, $y)" }
}

class C14_01Overloading {

    /**
     * Aritmética.
     */
    static void demoArithmetic() {
        section('Dinero, con la aritmética que se espera')

        def precio = Dinero.euros(19.99)
        def envio = Dinero.euros(4.50)

        show('precio', precio)
        show('precio + envio', precio + envio)
        show('precio - envio', precio - envio)
        show('precio * 3', precio * 3)
        show('precio / 2', precio / 2)
        show('-precio', -precio)

        section('Sumar una lista')

        def carrito = [Dinero.euros(10), Dinero.euros(5.50), Dinero.euros(0.25)]
        show('inject', carrito.inject(Dinero.euros(0)) { a, b -> a + b })
        show('sum con valor inicial', carrito.sum(Dinero.euros(0)))
        bullet('`sum` funciona porque hemos definido `plus`. Nada más.')

        section('Y comparar, porque implementa Comparable')

        show('precio > envio', precio > envio)
        show('ordenados', carrito.toSorted()*.toString())
        show('el mayor', carrito.max().toString())
        bullet('`>`, `<`, `sort`, `max` y `min` salen todos de `compareTo`.')

        section('La validación vive en el operador')

        try {
            show('euros + dólares', Dinero.euros(10) + new Dinero(500, 'USD'))
        } catch (IllegalArgumentException e) {
            show('euros + dólares', e.message)
        }
        bullet('Es EL argumento a favor de un tipo propio para el dinero: el error')
        bullet('se detecta en la suma, no tres capas más abajo.')

        section('Y el Groovy Truth, de regalo')

        show('if (Dinero.euros(0))', Dinero.euros(0) ? 'cierto' : 'falso')
        show('if (Dinero.euros(1))', Dinero.euros(1) ? 'cierto' : 'falso')
        bullet('`asBoolean()` (capítulo 05) convierte "importe cero" en "falso".')
    }

    /**
     * Vectores, y la sobrecarga del mismo operador.
     */
    static void demoVectors() {
        section('Suma y resta')

        def a = new Vector2D(3, 4)
        def b = new Vector2D(1, 2)

        show('a', a)
        show('a + b', a + b)
        show('a - b', a - b)
        show('-a', -a)
        show('a.modulo', a.modulo)

        section('El MISMO operador, dos significados')

        show('a * 2  (escalar)', a * 2)
        show('a * b  (producto escalar)', a * b)
        bullet('Dos métodos `multiply` con parámetros distintos. Groovy elige por el')
        bullet('tipo del argumento, como con cualquier sobrecarga (capítulo 10).')

        section('¿Es buena idea?')

        bullet('En matemáticas `*` significa las dos cosas, así que aquí sí.')
        bullet('Si tuvieras que explicar en un comentario qué hace cada uno, no.')
    }

    /**
     * Cuándo y cuándo no.
     */
    static void demoWhenToOverload() {
        section('La regla de oro')

        bullet('Sobrecarga un operador sólo si su significado es OBVIO para cualquiera')
        bullet('que lea el código sin abrir tu clase.')

        section('Casos donde sí')

        bullet('`+` en dinero, medidas, vectores, duraciones, matrices')
        bullet('`<<` para "añadir a" en una colección propia')
        bullet('`[]` para acceder a un elemento de algo indexable')
        bullet('`compareTo` en cualquier cosa con orden natural')
        bullet('`call()` en algo que "se ejecuta"')

        section('Casos donde NO')

        bullet('`+` para "y también": `usuario + permiso` no significa nada.')
        bullet('`-` para borrar de una base de datos: esconde un efecto enorme.')
        bullet('`<<` para "enviar por la red": parece barato y no lo es.')
        bullet('Operadores que MUTAN: `a + b` no debe cambiar ni `a` ni `b`.')

        section('La trampa de mutar')

        def malo = new AcumuladorMalo(1)
        def resultado = malo + new AcumuladorMalo(2)
        show('el resultado', resultado.valor)
        show('pero el original ahora vale', malo.valor)
        bullet('`a + b` ha cambiado `a`. Nadie espera eso, y rompe cosas como `sum`.')

        section('La versión correcta devuelve uno nuevo')

        def bueno = new AcumuladorBueno(1)
        def suma = bueno + new AcumuladorBueno(2)
        show('el resultado', suma.valor)
        show('y el original sigue', bueno.valor)

        section('Si quieres que MUTE, existe el operador de asignación')

        bullet('`a += b` usa `plus` y REASIGNA la variable: a = a.plus(b).')
        bullet('Groovy no tiene `plusAssign` separado, así que con un objeto')
        bullet('inmutable `+=` hace lo correcto sin que tengas que hacer nada.')

        def contador = Dinero.euros(10)
        contador += Dinero.euros(5)
        show('tras contador += 5€', contador)
    }

    static class AcumuladorMalo {
        int valor

        AcumuladorMalo(int valor) { this.valor = valor }

        /** MAL: muta y devuelve this. */
        AcumuladorMalo plus(AcumuladorMalo otro) {
            valor += otro.valor
            this
        }
    }

    static class AcumuladorBueno {
        final int valor

        AcumuladorBueno(int valor) { this.valor = valor }

        AcumuladorBueno plus(AcumuladorBueno otro) { new AcumuladorBueno(valor + otro.valor) }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita `compareTo` de `Dinero` y mira qué operadores y métodos dejan de funcionar.
//  2. Añade `Dinero power(int)` y prueba `precio ** 2`: ¿tiene sentido?
//  3. Quita `asBoolean()` y comprueba qué devuelve `if (Dinero.euros(0))`.
//  4. Suma una lista de `AcumuladorMalo` con `sum` y explica el resultado.
