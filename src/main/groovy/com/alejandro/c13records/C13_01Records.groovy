package com.alejandro.c13records

import groovy.transform.RecordOptions

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  13.1 · Records y clases selladas
//
//  QUÉ ES
//    Un `record` es una clase de datos inmutable en una línea: campos finales,
//    constructor, accesores, `equals`, `hashCode` y `toString`. Una interfaz `sealed`
//    declara exactamente qué tipos pueden implementarla.
//
//  POR QUÉ IMPORTA
//    Juntos son la forma moderna de modelar datos y alternativas: un `sealed interface`
//    con varios `record` describe "esto es una de estas cosas concretas" y el `switch`
//    lo recorre entero.
//
//  ERRORES COMUNES
//    · Meter lógica de negocio en un record: es un contenedor de datos.
//    · Esperar que `sealed` dé exhaustividad comprobada por el compilador en el
//      `switch`. En Groovy NO la da (a diferencia de Java 21).
//    · Usar un record con un campo `List`: es final, pero la lista sigue siendo mutable.
// =====================================================================================

// Un record de verdad: Groovy 4+ usa la palabra clave del lenguaje.
record Punto(int x, int y) {}

/** Con constructor compacto: valida antes de asignar. */
record Cantidad(String unidad, int valor) {
    Cantidad {
        if (valor < 0) throw new IllegalArgumentException("cantidad negativa: $valor")
        if (!unidad) throw new IllegalArgumentException('falta la unidad')
    }
}

/** Con `copyWith`, para crear variantes sin mutar. */
@RecordOptions(copyWith = true)
record Configuracion(String host, int puerto) {}

/** Un record puede tener métodos: derivados, no lógica de negocio. */
record Rectangulo(double ancho, double alto) {
    double area() { ancho * alto }

    boolean esCuadrado() { ancho == alto }
}

/** El campo es final; la LISTA no lo es. */
record ConLista(String nombre, List<String> etiquetas) {}

class C13_01Records {

    /**
     * Lo que un record te da.
     */
    static void demoWhatYouGet() {
        section('Una línea, y ya está todo')

        def p = new Punto(1, 2)
        show('toString', p.toString())
        show('equals', p == new Punto(1, 2))
        show('hashCode coincide', p.hashCode() == new Punto(1, 2).hashCode())

        section('Los accesores NO llevan `get`')

        show('p.x()', p.x())
        show('p.x  (Groovy lo permite igual)', p.x)
        bullet('En Java sólo existe `x()`. Groovy deja además el acceso con punto.')

        section('Es inmutable de verdad')

        show('la clase es final', java.lang.reflect.Modifier.isFinal(Punto.modifiers))
        try {
            p.x = 9
        } catch (Exception e) {
            show('p.x = 9', e.class.simpleName)
        }

        section('Y Groovy le añade el constructor de mapa')

        show('new Punto(x: 1, y: 2)', new Punto(x: 1, y: 2).toString())
        bullet('Eso es cosa de Groovy: en Java sólo existe el posicional.')

        section('Constructor compacto: validar sin repetir las asignaciones')

        show('Cantidad("kg", 5)', new Cantidad('kg', 5).toString())
        try {
            new Cantidad('kg', -1)
        } catch (IllegalArgumentException e) {
            show('Cantidad("kg", -1)', e.message)
        }
        bullet('El cuerpo `Cantidad { … }` corre ANTES de asignar los campos.')
        bullet('No hay que escribir `this.x = x`: lo hace el compilador.')

        section('copyWith para las variantes')

        def base = new Configuracion('localhost', 80)
        show('base', base.toString())
        show('base.copyWith(puerto: 8080)', base.copyWith(puerto: 8080).toString())
        show('base no ha cambiado', base.puerto())

        section('Métodos derivados: sí; lógica de negocio: no')

        def r = new Rectangulo(3, 4)
        show('r.area()', r.area())
        show('r.esCuadrado()', r.esCuadrado())
        bullet('Un método que calcula algo a partir de los campos está bien.')
        bullet('Uno que llama a la base de datos, no. Eso es un servicio.')
    }

    /**
     * El límite que hay que conocer.
     */
    static void demoLimits() {
        section('El campo es final; lo que hay DENTRO puede no serlo')

        def conLista = new ConLista('etiquetas', ['a', 'b'])
        conLista.etiquetas() << 'colada'
        show('tras modificar la lista', conLista.toString())
        bullet('El record no puede reasignar el campo, pero la lista sí se muta.')
        bullet('Es exactamente el problema del capítulo 08, y la solución es la misma:')

        def seguro = new ConLista('etiquetas', ['a', 'b'].asImmutable())
        try {
            seguro.etiquetas() << 'colada'
        } catch (UnsupportedOperationException e) {
            show('con la lista congelada', e.class.simpleName)
        }
        bullet('O usa `@Immutable` (capítulo 12), que congela las colecciones solo.')

        section('Record frente a @Immutable')

        bullet('                  record              @Immutable')
        bullet('───────────────   ─────────────────   ─────────────────────')
        bullet('sintaxis          una línea           anotación sobre clase')
        bullet('colecciones       NO las congela      SÍ las congela')
        bullet('copias defensivas no                  sí, al entrar y al salir')
        bullet('herencia          imposible (final)   imposible (final)')
        bullet('interoperable     es un record de     es una clase normal')
        bullet('  con Java        Java de verdad')

        section('Cuál usar')

        bullet('Record: datos que viajan, DTO, resultados, eventos, claves.')
        bullet('@Immutable: cuando hay colecciones dentro y quieres garantías.')
    }

    /**
     * Clases selladas.
     */
    static void demoSealed() {
        section('Una jerarquía CERRADA')

        show('quién puede implementar Forma', Forma.permittedSubclasses*.simpleName)
        bullet('`sealed interface Forma permits Circulo, Cuadrado, Triangulo`, y nadie más.')
        bullet('Ni siquiera desde otro módulo: lo comprueba el compilador.')

        section('Y por eso un switch sobre ella se puede razonar')

        List<Forma> formas = [new Circulo(2), new Cuadrado(3), new Triangulo(3, 4)]
        show('áreas', formas.collect { String.format('%.2f', area(it)) })
        show('nombres', formas.collect { nombre(it) })

        section('AVISO: en Groovy el switch NO comprueba exhaustividad')

        bullet('En Java 21, un switch sobre un tipo sellado sin todos los casos')
        bullet('no compila. En Groovy sí compila, y si no encaja ninguno…')
        show('sin default y sin caso', sinDefault(new Triangulo(1, 1)))
        bullet('…devuelve null, en silencio. Pon SIEMPRE un `default` que avise.')
        show('con default que avisa', conDefaultQueAvisa(new Triangulo(1, 1)))

        section('Para qué sirve entonces sellar')

        bullet('1. Documenta la lista completa de alternativas en un sitio.')
        bullet('2. Impide que alguien añada una variante desde fuera.')
        bullet('3. Te deja buscar todos los `switch` que hay que tocar al añadir una.')

        section('El patrón completo: sealed + record')

        show('un resultado correcto', describir(new Exito('todo bien')))
        show('un resultado con error', describir(new Fallo('se rompió', 500)))
        bullet('Modelar el error como un DATO, en vez de con excepciones, es lo que')
        bullet('permite que el `switch` de arriba lo trate como un caso más.')
        bullet('El capítulo 18 compara los dos enfoques.')
    }

    private static double area(Forma f) {
        switch (f) {
            case Circulo -> Math.PI * ((Circulo) f).radio() ** 2
            case Cuadrado -> ((Cuadrado) f).lado() ** 2
            case Triangulo -> ((Triangulo) f).base() * ((Triangulo) f).altura() / 2
            default -> 0d
        }
    }

    private static String nombre(Forma f) {
        switch (f) {
            case Circulo -> 'círculo'
            case Cuadrado -> 'cuadrado'
            case Triangulo -> 'triángulo'
            default -> 'desconocida'
        }
    }

    private static String sinDefault(Forma f) {
        switch (f) {
            case Circulo -> 'círculo'
            case Cuadrado -> 'cuadrado'
        }
    }

    private static String conDefaultQueAvisa(Forma f) {
        switch (f) {
            case Circulo -> 'círculo'
            case Cuadrado -> 'cuadrado'
            default -> "SIN TRATAR: ${f.getClass().simpleName}"
        }
    }

    private static String describir(Resultado r) {
        switch (r) {
            case Exito -> "ok: ${((Exito) r).mensaje()}"
            case Fallo -> "error ${((Fallo) r).codigo()}: ${((Fallo) r).mensaje()}"
            default -> 'resultado desconocido'
        }
    }
}

sealed interface Forma permits Circulo, Cuadrado, Triangulo {}

record Circulo(double radio) implements Forma {}

record Cuadrado(double lado) implements Forma {}

record Triangulo(double base, double altura) implements Forma {}

sealed interface Resultado permits Exito, Fallo {}

record Exito(String mensaje) implements Resultado {}

record Fallo(String mensaje, int codigo) implements Resultado {}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Intenta crear una clase que implemente `Forma` sin estar en `permits`.
//  2. Añade un cuarto tipo de forma y busca todos los `switch` que hay que tocar.
//  3. Quita el `default` de `area()` y llama con un Triangulo: mira qué devuelve.
//  4. Pon un campo `List` en un record y congélalo con `asImmutable()` en el
//     constructor compacto.
