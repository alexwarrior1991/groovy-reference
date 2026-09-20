package com.alejandro.c28spock

import spock.lang.Specification

// =====================================================================================
//  Los cinco tipos de doble de test, cada uno con su Spec.
//
//  Spock los llama Stub, Mock y Spy; los otros dos (dummy y fake) se hacen a mano.
// =====================================================================================

class DoblesSpec extends Specification {

    def 'STUB: responde lo que le digas, y no comprueba nada'() {
        given:
        Almacen stub = Stub(Almacen)
        stub.buscar(_) >> new Producto('X', 'algo', 100)
        stub.stockDe(_) >> 99

        when:
        def carrito = new Carrito(stub)
        carrito.añadir('X', 1)

        then: 'no se comprueba ninguna interacción: sólo el resultado'
        carrito.unidades() == 1
    }

    def 'MOCK: además comprueba CÓMO se le llamó'() {
        given:
        Almacen mock = Mock(Almacen)

        when:
        new Carrito(mock).añadir('X', 2)

        then:
        1 * mock.buscar('X') >> new Producto('X', 'algo', 100)
        1 * mock.stockDe('X') >> 10
        1 * mock.reservar('X', 2)
        0 * _          // y NADA más
    }

    def 'SPY: el objeto real, pero mirando lo que hace'() {
        given:
        Almacen real = new AlmacenEnMemoria(['X': new Producto('X', 'algo', 100)], ['X': 5])
        Almacen spy = Spy(real)

        when:
        new Carrito(spy).añadir('X', 2)

        then: 'se ejecuta el método de verdad Y se comprueba la llamada'
        1 * spy.reservar('X', 2)
        real.stockDe('X') == 3
    }

    def 'FAKE: una implementación de verdad, pero simplificada'() {
        given: 'un almacén en memoria en vez de una base de datos'
        Almacen fake = new AlmacenEnMemoria(
            ['LIB-1': new Producto('LIB-1', 'libro', 3550)],
            ['LIB-1': 2])

        when:
        def carrito = new Carrito(fake)
        def primero = carrito.añadir('LIB-1', 2)
        def segundo = carrito.añadir('LIB-1', 1)

        then: 'el fake lleva su propia contabilidad, como el real'
        primero instanceof Agregado
        segundo instanceof SinStock
        fake.stockDe('LIB-1') == 0
    }

    def 'la tabla: cuál usar'() {
        expect:
        true

        where:
        doble  | cuando
        'Stub' | 'sólo necesitas que la dependencia devuelva algo'
        'Mock' | 'lo que pruebas ES la interacción (que se llamó, con qué)'
        'Spy'  | 'quieres el comportamiento real y además comprobar llamadas'
        'Fake' | 'necesitas comportamiento de verdad sin la infraestructura'
        'Dummy'| 'el parámetro hace falta pero no se usa'
    }
}

/** Un FAKE: implementación real y simplificada, escrita a mano. */
class AlmacenEnMemoria implements Almacen {
    private final Map<String, Producto> catalogo
    private final Map<String, Integer> stock

    AlmacenEnMemoria(Map<String, Producto> catalogo, Map<String, Integer> stock) {
        this.catalogo = new LinkedHashMap<>(catalogo)
        this.stock = new LinkedHashMap<>(stock)
    }

    @Override
    Producto buscar(String referencia) { catalogo[referencia] }

    @Override
    int stockDe(String referencia) { stock[referencia] ?: 0 }

    @Override
    void reservar(String referencia, int cantidad) {
        stock[referencia] = stockDe(referencia) - cantidad
    }
}
