package com.alejandro.c28spock

import groovy.transform.Canonical
import groovy.transform.CompileStatic

// =====================================================================================
//  El código de PRODUCCIÓN del capítulo 28.
//
//  Los Spec que lo prueban están en src/test/groovy/com/alejandro/c28spock/ y se
//  ejecutan de verdad con `./mvnw test`. No son ejemplos en un comentario.
// =====================================================================================

/** Un producto del catálogo. */
@Canonical
class Producto {
    String referencia
    String nombre
    long centimos
}

/**
 * Lo que devuelve un intento de añadir al carrito.
 *
 * Se llama `Agregado` y no `Añadido` a propósito: el nombre de una clase acaba siendo
 * el nombre de un FICHERO, y un carácter no ASCII ahí rompe la generación de stubs de
 * Java en cuanto la plataforma no está en UTF-8. Acentos y eñes, en los comentarios,
 * en las cadenas y en la salida; nunca en los identificadores.
 */
sealed interface ResultadoCarrito permits Agregado, SinStock, ProductoDesconocido {}

@Canonical
class Agregado implements ResultadoCarrito {
    String referencia
    int cantidad
}

@Canonical
class SinStock implements ResultadoCarrito {
    String referencia
    int disponible
}

@Canonical
class ProductoDesconocido implements ResultadoCarrito {
    String referencia
}

/** La dependencia que los tests van a sustituir por un doble. */
interface Almacen {
    Producto buscar(String referencia)

    int stockDe(String referencia)

    void reservar(String referencia, int cantidad)
}

/**
 * El carrito.
 *
 * Está escrito para ser TESTEABLE: recibe su dependencia por el constructor, no la
 * construye por dentro. Ésa es la decisión que hace posible todo el capítulo.
 */
@CompileStatic
class Carrito {

    private final Almacen almacen
    private final Map<String, Integer> lineas = [:]

    Carrito(Almacen almacen) { this.almacen = almacen }

    ResultadoCarrito añadir(String referencia, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("cantidad inválida: $cantidad")

        Producto producto = almacen.buscar(referencia)
        if (producto == null) return new ProductoDesconocido(referencia)

        int disponible = almacen.stockDe(referencia)
        int yaEnCarrito = lineas[referencia] ?: 0
        if (disponible < yaEnCarrito + cantidad) {
            return new SinStock(referencia, disponible)
        }

        almacen.reservar(referencia, cantidad)
        lineas[referencia] = yaEnCarrito + cantidad
        new Agregado(referencia, lineas[referencia])
    }

    long total() {
        lineas.inject(0L) { Long acumulado, Map.Entry<String, Integer> linea ->
            Producto p = almacen.buscar(linea.key)
            acumulado + (p ? p.centimos * linea.value : 0L)
        } as long
    }

    int unidades() { lineas.values().sum(0) as int }

    Map<String, Integer> getLineas() { lineas.asImmutable() }
}
