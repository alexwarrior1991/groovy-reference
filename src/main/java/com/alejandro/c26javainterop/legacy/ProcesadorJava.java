package com.alejandro.c26javainterop.legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Código Java que recibe interfaces funcionales.
 *
 * <p>Enseña que un closure de Groovy se convierte solo en cualquiera de ellas
 * (la coerción SAM del capítulo 02), así que una API de Java se usa desde
 * Groovy sin envoltorios.
 */
public class ProcesadorJava {

    public <T> List<T> filtrar(List<T> entrada, Predicate<T> criterio) {
        List<T> salida = new ArrayList<>();
        for (T elemento : entrada) {
            if (criterio.test(elemento)) {
                salida.add(elemento);
            }
        }
        return salida;
    }

    public <T, R> List<R> transformar(List<T> entrada, Function<T, R> funcion) {
        List<R> salida = new ArrayList<>();
        for (T elemento : entrada) {
            salida.add(funcion.apply(elemento));
        }
        return salida;
    }

    /** Recibe un Runnable: el caso SAM más simple. */
    public String ejecutar(Runnable tarea) {
        tarea.run();
        return "ejecutado";
    }

    /** Varargs, para ver cómo se llaman desde Groovy. */
    public String juntar(String separador, String... partes) {
        return String.join(separador, partes);
    }
}
