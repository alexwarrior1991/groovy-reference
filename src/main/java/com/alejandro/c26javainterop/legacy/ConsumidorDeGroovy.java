package com.alejandro.c26javainterop.legacy;

import com.alejandro.c26javainterop.PedidoGroovy;

/**
 * Java llamando a una clase escrita en Groovy.
 *
 * <p>Esta clase es la que obliga al build a generar STUBS: javac necesita ver
 * una firma de {@link PedidoGroovy} antes de que el compilador de Groovy haya
 * producido el bytecode. Es la razón de los goals generateStubs/removeStubs
 * del pom.xml.
 */
public class ConsumidorDeGroovy {

    /** Groovy generó getCliente() a partir de la propiedad `cliente`. */
    public String leerCliente(PedidoGroovy pedido) {
        return pedido.getCliente();
    }

    /** Un método normal de la clase Groovy, visto desde Java. */
    public String resumir(PedidoGroovy pedido) {
        return pedido.resumen();
    }

    /** Lo que Java NO ve: los métodos que Groovy añade por metaprogramación. */
    public int contarLineas(PedidoGroovy pedido) {
        return pedido.getLineas().size();
    }
}
