package com.alejandro.c26javainterop.legacy;

import java.util.ArrayList;
import java.util.List;

/**
 * Una clase Java de verdad, compilada por javac en este mismo proyecto.
 *
 * <p>Sirve para enseñar qué ve Groovy cuando llama a Java: getters explícitos,
 * campos privados, sobrecarga resuelta por el tipo declarado y devoluciones
 * que pueden ser null sin que nada lo indique.
 */
public class UsuarioJava {

    private final String nombre;
    private final int edad;
    private final List<String> roles;

    public UsuarioJava(String nombre, int edad) {
        this.nombre = nombre;
        this.edad = edad;
        this.roles = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public int getEdad() {
        return edad;
    }

    /** Devuelve la lista interna: es justo lo que NO hay que hacer (capítulo 08). */
    public List<String> getRoles() {
        return roles;
    }

    public void addRol(String rol) {
        roles.add(rol);
    }

    /** Puede devolver null, y la firma no lo dice. */
    public String getApodo() {
        return null;
    }

    /** Dos sobrecargas: Java elige por el tipo DECLARADO, Groovy por el real. */
    public String describir(Object valor) {
        return "Java dice: Object";
    }

    public String describir(String valor) {
        return "Java dice: String";
    }

    /**
     * Hace la llamada DESDE Java, para que la resuelva javac.
     *
     * <p>Si llamas a {@code describir(x)} desde Groovy, quien elige la sobrecarga es
     * Groovy, aunque el método sea de Java. Para ver el despacho estático de verdad
     * hace falta que la llamada esté escrita en Java, como aquí: javac fija
     * {@code describir(Object)} al compilar, y eso ya no cambia.
     */
    public String describirDesdeJava(Object valor) {
        return describir(valor);
    }

    /** Un método que declara una excepción comprobada. */
    public String operacionQuePuedeFallar(boolean falla) throws java.io.IOException {
        if (falla) {
            throw new java.io.IOException("fallo desde Java");
        }
        return "todo bien";
    }

    @Override
    public String toString() {
        return "UsuarioJava{" + nombre + ", " + edad + "}";
    }
}
