package com.alejandro.c26javainterop

import groovy.transform.CompileStatic

/**
 * Una clase Groovy pensada para que Java la consuma.
 *
 * Es la que obliga al build a generar stubs: `ConsumidorDeGroovy.java` la
 * referencia, y javac necesita una firma antes de que exista el bytecode.
 *
 * `@CompileStatic` no hace falta para la interoperabilidad, pero sí deja las
 * firmas limpias y sin sorpresas para quien llame desde Java.
 */
@CompileStatic
class PedidoGroovy {

    /** Una propiedad: Groovy genera getCliente() y setCliente(). */
    String cliente

    /** Una lista de líneas del pedido. */
    List<String> lineas = []

    /** Un método normal, que Java ve tal cual. */
    String resumen() { "$cliente: ${lineas.size()} línea(s)" }

    /** Con valor por defecto: Groovy genera DOS métodos, y Java ve los dos. */
    String etiqueta(String prefijo = 'pedido') { "$prefijo/$cliente" }
}
