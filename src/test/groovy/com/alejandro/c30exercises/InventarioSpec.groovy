package com.alejandro.c30exercises

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class InventarioSpec extends Specification {

    @Shared
    List<Articulo> catalogo = [
        new Articulo('A-1', 'Teclado', 'informática', Precio.de(45.90), 12),
        new Articulo('A-2', 'Ratón', 'informática', Precio.de(19.99), 3),
        new Articulo('B-1', 'Silla', 'mobiliario', Precio.de(149.50), 4),
    ]

    Inventario inventario = new Inventario(catalogo)

    def 'el valor total suma precio por unidades de cada artículo'() {
        expect:
        inventario.generarInforme().valorTotal ==
            new Precio(45_90 * 12 + 19_99 * 3 + 149_50 * 4)
    }

    def 'el desglose por categoría cuadra con el total'() {
        given:
        def informe = inventario.generarInforme()

        expect:
        informe.valorPorCategoria.values().inject(new Precio(0)) { a, b -> a + b } ==
            informe.valorTotal
    }

    def 'bajo mínimos son los de menos de cinco unidades, ordenados'() {
        expect:
        inventario.generarInforme().bajoMinimos == ['A-2', 'B-1']
    }

    def 'el más caro se elige por precio unitario, no por valor total'() {
        expect:
        inventario.generarInforme().masCaro == 'Silla'
    }

    def 'las unidades por categoría se suman'() {
        expect:
        inventario.generarInforme().unidadesPorCategoria == ['informática': 15, 'mobiliario': 4]
    }

    // -- El dinero ---------------------------------------------------------------------

    @Unroll
    def 'Precio.de(#euros) son #centimos céntimos'() {
        expect:
        Precio.de(euros).centimos == centimos

        where:
        euros  || centimos
        0      || 0
        1      || 100
        0.01   || 1
        45.90  || 4590
        149.50 || 14950
    }

    def 'los precios se suman, se multiplican y se comparan'() {
        given:
        def diez = Precio.de(10)
        def cinco = Precio.de(5)

        expect:
        diez + cinco == Precio.de(15)
        cinco * 3 == Precio.de(15)
        diez > cinco
        [diez, cinco].max() == diez
        [diez, cinco].toSorted() == [cinco, diez]
    }

    def 'el precio es inmutable'() {
        when:
        Precio.de(10).centimos = 999

        then:
        thrown(ReadOnlyPropertyException)
    }

    // -- Encapsulamiento ---------------------------------------------------------------

    def 'modificar la lista de entrada no afecta al inventario'() {
        given:
        List<Articulo> entrada = new ArrayList<>(catalogo)
        def creado = new Inventario(entrada)

        when:
        entrada.clear()

        then:
        creado.articulos.size() == 3
    }

    def 'la lista que devuelve no se puede modificar'() {
        when:
        inventario.articulos << catalogo[0]

        then:
        thrown(UnsupportedOperationException)
    }

    def 'un artículo es inmutable'() {
        when:
        catalogo[0].unidades = 99

        then:
        thrown(ReadOnlyPropertyException)
    }

    // -- Casos límite ------------------------------------------------------------------

    def 'un inventario vacío da un informe con ceros, no un fallo'() {
        given:
        def vacio = new Inventario([])

        when:
        def informe = vacio.generarInforme()

        then:
        noExceptionThrown()
        informe.valorTotal == new Precio(0)
        informe.valorPorCategoria.isEmpty()
        informe.bajoMinimos.isEmpty()
        informe.masCaro == null
    }

    def 'categoriasPorEncimaDe filtra y ordena de mayor a menor'() {
        expect:
        inventario.categoriasPorEncimaDe(Precio.de(500)) == ['informática', 'mobiliario']
        inventario.categoriasPorEncimaDe(Precio.de(600)) == ['informática']
        inventario.categoriasPorEncimaDe(Precio.de(10_000)) == []
    }
}
