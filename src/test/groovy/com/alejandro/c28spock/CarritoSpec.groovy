package com.alejandro.c28spock

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

// =====================================================================================
//  El Spec que prueba el Carrito del capítulo 28.
//
//  Se ejecuta de verdad con `./mvnw test`. Léelo mientras ejecutas las demos del
//  capítulo: cada bloque de la demo se corresponde con algo de aquí.
// =====================================================================================

class CarritoSpec extends Specification {

    @Shared
    Producto libro = new Producto('LIB-1', 'Programación en Groovy', 3550)

    Almacen almacen = Mock(Almacen)

    @Subject
    Carrito carrito = new Carrito(almacen)

    // -- given / when / then ----------------------------------------------------------

    def 'añadir un producto con stock lo mete en el carrito'() {
        given: 'un almacén con cinco unidades'
        almacen.buscar('LIB-1') >> libro
        almacen.stockDe('LIB-1') >> 5

        when: 'se añaden dos'
        def resultado = carrito.añadir('LIB-1', 2)

        then: 'el resultado lo confirma y el carrito lo refleja'
        resultado instanceof Agregado
        resultado.cantidad == 2
        carrito.unidades() == 2
        carrito.total() == 7100
    }

    // -- expect, para lo que cabe en una línea ----------------------------------------

    def 'un carrito recién creado está vacío'() {
        expect:
        carrito.unidades() == 0
        carrito.total() == 0
        carrito.lineas.isEmpty()
    }

    // -- El power assert: lo que hace a Spock distinto --------------------------------

    def 'el total suma todas las líneas'() {
        given:
        almacen.buscar(_) >> libro
        almacen.stockDe(_) >> 10

        when:
        carrito.añadir('LIB-1', 3)

        then:
        // Si esto fallara, Spock imprimiría el valor de CADA subexpresión.
        carrito.total() == libro.centimos * 3
    }

    // -- where: una tabla en vez de cinco tests iguales -------------------------------

    @Unroll
    def 'con stock #stock, pedir #pedido da #esperado'() {
        given:
        almacen.buscar('LIB-1') >> libro
        almacen.stockDe('LIB-1') >> stock

        expect:
        carrito.añadir('LIB-1', pedido).class.simpleName == esperado

        where:
        stock | pedido || esperado
        10    | 1      || 'Agregado'
        10    | 10     || 'Agregado'
        10    | 11     || 'SinStock'
        0     | 1      || 'SinStock'
        1     | 1      || 'Agregado'
    }

    // -- thrown(): comprobar excepciones ----------------------------------------------

    def 'una cantidad no positiva se rechaza'() {
        when:
        carrito.añadir('LIB-1', cantidad)

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("cantidad inválida: $cantidad")

        where:
        cantidad << [0, -1, -100]
    }

    def 'un producto desconocido no lanza: devuelve un resultado'() {
        given:
        almacen.buscar('NO-EXISTE') >> null

        when:
        def resultado = carrito.añadir('NO-EXISTE', 1)

        then:
        noExceptionThrown()
        resultado instanceof ProductoDesconocido
    }

    // -- Interacciones: comprobar QUE se llamó, y cuántas veces ------------------------

    def 'añadir reserva el stock exactamente una vez'() {
        given:
        almacen.buscar('LIB-1') >> libro
        almacen.stockDe('LIB-1') >> 5

        when:
        carrito.añadir('LIB-1', 2)

        then:
        1 * almacen.reservar('LIB-1', 2)
    }

    def 'si no hay stock, NO se reserva nada'() {
        given:
        almacen.buscar('LIB-1') >> libro
        almacen.stockDe('LIB-1') >> 1

        when:
        def resultado = carrito.añadir('LIB-1', 5)

        then:
        resultado instanceof SinStock
        0 * almacen.reservar(_, _)
    }

    def 'un producto desconocido ni siquiera consulta el stock'() {
        given:
        almacen.buscar('NO-EXISTE') >> null

        when:
        carrito.añadir('NO-EXISTE', 1)

        then:
        0 * almacen.stockDe(_)
        0 * almacen.reservar(_, _)
    }

    // -- Respuestas que cambian entre llamadas ----------------------------------------

    def 'el stock se comprueba contra lo que ya hay en el carrito'() {
        given: 'el almacén siempre dice que hay 3'
        almacen.buscar('LIB-1') >> libro
        almacen.stockDe('LIB-1') >> 3

        when: 'se añaden dos y después dos más'
        def primero = carrito.añadir('LIB-1', 2)
        def segundo = carrito.añadir('LIB-1', 2)

        then: 'el segundo se rechaza, porque 2 + 2 > 3'
        primero instanceof Agregado
        segundo instanceof SinStock
        carrito.unidades() == 2
    }

    def 'un stub puede devolver valores distintos en cada llamada'() {
        given:
        almacen.buscar('LIB-1') >> libro
        almacen.stockDe('LIB-1') >>> [10, 0]

        expect:
        carrito.añadir('LIB-1', 1) instanceof Agregado
        carrito.añadir('LIB-1', 1) instanceof SinStock
    }
}
