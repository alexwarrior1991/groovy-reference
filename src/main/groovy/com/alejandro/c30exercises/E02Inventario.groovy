package com.alejandro.c30exercises

import groovy.transform.Canonical
import groovy.transform.Immutable

import static com.alejandro.c30exercises.Formato.enunciado
import static com.alejandro.c30exercises.Formato.explicacion
import static com.alejandro.c30exercises.Formato.pistas
import static com.alejandro.c30exercises.Formato.solucionEnMarcha
import static com.alejandro.c30exercises.Formato.testEn
import static com.alejandro.c30exercises.Formato.varianteDificil
import static com.alejandro.infra.Console.show

// =====================================================================================
//  Ejercicio 2 · Informe de inventario                                     🟡 medio
//
//  Repasa: colecciones, groupBy, inject, @Immutable, copias defensivas, dinero.
//  Capítulos: 08 (listas), 09 (mapas), 12 (@Immutable), 14 (operadores).
// =====================================================================================

/** Dinero en céntimos, inmutable y con aritmética. Capítulo 14. */
@Immutable
class Precio implements Comparable<Precio> {
    long centimos

    static Precio de(BigDecimal euros) { new Precio((euros * 100).toBigInteger().longValue()) }

    Precio plus(Precio otro) { new Precio(centimos + otro.centimos) }

    Precio multiply(int veces) { new Precio(centimos * veces) }

    @Override
    int compareTo(Precio otro) { centimos <=> otro.centimos }

    @Override
    String toString() {
        "${(centimos.intdiv(100))},${(centimos % 100).toString().padLeft(2, '0')} €"
    }
}

@Immutable
class Articulo {
    String referencia
    String nombre
    String categoria
    Precio precio
    int unidades

    Precio getValorTotal() { precio * unidades }

    boolean isBajoMinimos() { unidades < 5 }
}

@Canonical
class Informe {
    Precio valorTotal
    Map<String, Precio> valorPorCategoria
    List<String> bajoMinimos
    Map<String, Integer> unidadesPorCategoria
    String masCaro
}

class Inventario {

    private final List<Articulo> articulos

    Inventario(List<Articulo> articulos) {
        // Copia defensiva al entrar (capítulo 08).
        this.articulos = new ArrayList<Articulo>(articulos).asImmutable()
    }

    /** Copia (inmutable) al salir. */
    List<Articulo> getArticulos() { articulos }

    Informe generarInforme() {
        new Informe(
            valorTotal: articulos.inject(new Precio(0)) { Precio acumulado, Articulo a ->
                acumulado + a.valorTotal
            } as Precio,

            valorPorCategoria: articulos
                .groupBy { it.categoria }
                .collectEntries { String categoria, List<Articulo> lista ->
                    [categoria, lista.inject(new Precio(0)) { Precio acc, Articulo a ->
                        acc + a.valorTotal
                    }]
                },

            bajoMinimos: articulos.findAll { it.bajoMinimos }*.referencia.toSorted(),

            unidadesPorCategoria: articulos
                .groupBy { it.categoria }
                .collectEntries { String c, List<Articulo> l -> [c, l.sum { it.unidades }] },

            masCaro: articulos.max { it.precio }?.nombre,
        )
    }

    /** Las categorías cuyo valor supere un umbral, ordenadas de mayor a menor. */
    List<String> categoriasPorEncimaDe(Precio umbral) {
        generarInforme().valorPorCategoria
            .findAll { String c, Precio p -> p > umbral }
            .sort { -it.value.centimos }
            *.key
    }
}

class E02Inventario {

    private static final List<Articulo> CATALOGO = [
        new Articulo('A-1', 'Teclado', 'informática', Precio.de(45.90), 12),
        new Articulo('A-2', 'Ratón', 'informática', Precio.de(19.99), 3),
        new Articulo('A-3', 'Monitor', 'informática', Precio.de(229.00), 7),
        new Articulo('B-1', 'Silla', 'mobiliario', Precio.de(149.50), 4),
        new Articulo('B-2', 'Mesa', 'mobiliario', Precio.de(310.00), 2),
        new Articulo('C-1', 'Cuaderno', 'papelería', Precio.de(3.20), 150),
    ]

    static void ejecutar() {
        enunciado(
            'A partir de una lista de artículos (referencia, nombre, categoría,',
            'precio y unidades), genera un informe con:',
            '',
            '· el valor total del inventario',
            '· el valor por categoría',
            '· las referencias con menos de 5 unidades, ordenadas',
            '· las unidades por categoría',
            '· el nombre del artículo más caro',
            '',
            'Requisitos:',
            '· El dinero tiene que ser exacto y no se puede representar con Double.',
            '· Los artículos son inmutables.',
            '· El inventario no puede quedar expuesto a quien le pase la lista.',
        )

        pistas(
            'Guarda el dinero en CÉNTIMOS enteros y dale `plus` y `multiply` (cap. 14).',
            '@Immutable hace el artículo inmutable y le da equals/hashCode (cap. 12).',
            '`groupBy` + `collectEntries` es el idioma para "X por categoría" (cap. 09).',
            'Para sumar objetos propios, `inject` con un valor inicial (cap. 08).',
            'Copia al entrar y devuelve inmutable al salir (cap. 08).',
        )

        solucionEnMarcha()

        def inventario = new Inventario(CATALOGO)
        def informe = inventario.generarInforme()

        show('valor total', informe.valorTotal)
        show('valor por categoría', informe.valorPorCategoria)
        show('unidades por categoría', informe.unidadesPorCategoria)
        show('bajo mínimos', informe.bajoMinimos)
        show('el más caro', informe.masCaro)
        show('categorías por encima de 500 €', inventario.categoriasPorEncimaDe(Precio.de(500)))

        show('un artículo es inmutable', intentarModificar())
        show('la lista de entrada no afecta', laListaDeEntradaNoAfecta())
        show('la que devuelve tampoco', laQueDevuelveTampoco(inventario))

        explicacion(
            'El dinero va en céntimos enteros dentro de un @Immutable con `plus`,',
            '`multiply` y `compareTo`. Con eso, `inject` suma precios, `max` encuentra',
            'el más caro y `>` compara, sin escribir nada más (capítulo 14).',
            '',
            'El informe entero son cinco expresiones: `inject` para el total, dos',
            '`groupBy` + `collectEntries` para los desgloses, un `findAll` para los',
            'bajo mínimos y un `max`. Ni un bucle.',
            '',
            'El inventario copia la lista al construirse y la devuelve inmutable, así',
            'que ni quien se la pasó ni quien la recibe pueden cambiarle el estado.',
            'Es la lección del capítulo 08, aplicada.',
            '',
            'Y `bajoMinimos` es una propiedad calculada del artículo (`isBajoMinimos`),',
            'no un campo: la regla vive al lado del dato (capítulo 10).',
        )

        varianteDificil(
            'Añade un histórico: `valorEn(LocalDate)` con los movimientos de entrada',
            'y salida, para poder calcular el valor del inventario en una fecha pasada.',
            '',
            'Haz que el informe se genere en paralelo por categoría (capítulo 27) y',
            'comprueba con una medición si compensa. (Pista: casi seguro que no, y',
            'entender POR QUÉ es el objetivo del ejercicio.)',
            '',
            'Exporta el informe a JSON con JsonGenerator, decidiendo qué campos se',
            'publican (capítulo 22).',
        )

        testEn('InventarioSpec.groovy')
    }

    private static String intentarModificar() {
        try {
            CATALOGO[0].unidades = 99
            'se pudo modificar'
        } catch (Exception e) {
            e.class.simpleName
        }
    }

    private static String laListaDeEntradaNoAfecta() {
        List<Articulo> entrada = new ArrayList<Articulo>(CATALOGO)
        def inventario = new Inventario(entrada)
        entrada.clear()
        "el inventario sigue con ${inventario.articulos.size()} artículos"
    }

    private static String laQueDevuelveTampoco(Inventario inventario) {
        try {
            inventario.articulos << CATALOGO[0]
            'se pudo añadir'
        } catch (Exception e) {
            e.class.simpleName
        }
    }
}
