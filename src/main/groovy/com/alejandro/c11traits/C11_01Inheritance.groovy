package com.alejandro.c11traits

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  11.1 · Herencia e interfaces
//
//  QUÉ ES
//    La herencia de Groovy es la de Java: una sola clase padre, tantas interfaces como
//    quieras. Con dos diferencias prácticas: las clases NO son final por defecto (igual
//    que Java, al contrario que Kotlin) y `@Override` es opcional… con lo que eso trae.
//
//  POR QUÉ IMPORTA
//    Casi todo lo que en Java se resuelve con herencia, en Groovy se resuelve mejor con
//    un trait (11.2) o con delegación (`@Delegate`, capítulo 12). Conviene saber qué
//    hace la herencia antes de decidir no usarla.
//
//  ERRORES COMUNES
//    · Olvidar `@Override` y escribir un método nuevo por una errata en el nombre.
//    · Heredar para reutilizar código en vez de para sustituir un tipo por otro.
//    · Llamar a un método redefinible desde el constructor (demo 10.8).
// =====================================================================================

class C11_01Inheritance {

    /**
     * Lo básico.
     */
    static void demoBasics() {
        section('Heredar y redefinir')

        def perro = new Perro('Toby')
        def gato = new Gato('Luna')

        show('perro.describir()', perro.describir())
        show('gato.describir()', gato.describir())
        bullet('`describir()` está en la base y usa `sonido()`, que cada uno redefine.')

        section('Polimorfismo: la misma llamada, distinto resultado')

        List<Animal> animales = [perro, gato, new Animal('Genérico')]
        show('todos', animales*.sonido())

        section('`super` para llamar a la implementación de la base')

        show('cachorro.sonido()', new Cachorro('Bola').sonido())

        section('`@Override` es opcional, y por eso conviene ponerlo')

        def conErrata = new ConErrata('X')
        show('lo que se esperaba', 'guau')
        show('lo que sale', conErrata.sonido())
        bullet('El método `sonidoo()` no redefine nada: es uno nuevo que nadie llama.')
        bullet('Con `@Override` el compilador lo habría rechazado. Ponlo siempre.')

        section('`instanceof` y el tipo real')

        show('perro instanceof Animal', perro instanceof Animal)
        show('perro.class.simpleName', perro.class.simpleName)
        show('perro.class.superclass.simpleName', perro.class.superclass.simpleName)
    }

    static class Animal {
        final String nombre

        Animal(String nombre) { this.nombre = nombre }

        String sonido() { '...' }

        String describir() { "$nombre hace ${sonido()}" }
    }

    static class Perro extends Animal {
        Perro(String nombre) { super(nombre) }

        @Override
        String sonido() { 'guau' }
    }

    static class Gato extends Animal {
        Gato(String nombre) { super(nombre) }

        @Override
        String sonido() { 'miau' }
    }

    static class Cachorro extends Perro {
        Cachorro(String nombre) { super(nombre) }

        @Override
        String sonido() { "${super.sonido()} (flojito)" }
    }

    static class ConErrata extends Animal {
        ConErrata(String nombre) { super(nombre) }

        /** Sin @Override, esta errata compila tan feliz. */
        String sonidoo() { 'guau' }
    }

    /**
     * Interfaces, y clases abstractas.
     */
    static void demoInterfaces() {
        section('Una interfaz con método por defecto')

        def texto = new DocumentoTexto('hola')
        show('texto.imprimir()', texto.imprimir())
        show('texto.resumen()', texto.resumen())
        bullet('`resumen()` es un `default` de la interfaz: no hay que implementarlo.')

        section('Implementar varias')

        def informe = new Informe()
        show('informe.imprimir()', informe.imprimir())
        show('informe.exportar()', informe.exportar())
        show('los tipos que cumple', [Imprimible, Exportable].findAll { it.isInstance(informe) }*.simpleName)

        section('Un closure vale como implementación (coerción SAM)')

        Imprimible desdeClosure = { 'impreso desde un closure' } as Imprimible
        show('desdeClosure.imprimir()', desdeClosure.imprimir())

        section('Y un mapa también, si implementas todos los métodos')

        Exportable desdeMapa = [exportar: { 'exportado desde un mapa' }] as Exportable
        show('desdeMapa.exportar()', desdeMapa.exportar())
        bullet('Es lo que se usa en los tests para hacer un doble sin librerías.')

        section('Clase abstracta frente a interfaz')

        bullet('interfaz          contrato, sin estado; se pueden implementar varias')
        bullet('clase abstracta   comparte ESTADO y constructor; sólo se hereda una')
        bullet('trait             lo mejor de los dos: estado Y varios a la vez (11.2)')
    }

    interface Imprimible {
        String imprimir()

        default String resumen() { "resumen de: ${imprimir()}" }
    }

    interface Exportable {
        String exportar()
    }

    static class DocumentoTexto implements Imprimible {
        final String contenido

        DocumentoTexto(String contenido) { this.contenido = contenido }

        @Override
        String imprimir() { contenido }
    }

    static class Informe implements Imprimible, Exportable {
        @Override
        String imprimir() { 'informe impreso' }

        @Override
        String exportar() { 'informe exportado' }
    }

    /**
     * Cuándo NO heredar.
     */
    static void demoCompositionOverInheritance() {
        section('El contador roto: por qué la herencia es frágil')

        def contador = new ListaQueCuenta()
        contador.addAll(['a', 'b', 'c'])

        show('elementos añadidos, de verdad', contador.size())
        show('lo que dice el contador', contador.añadidos)
        bullet('Debería decir 3 y dice 6: `addAll` de ArrayList llama a `add` por')
        bullet('dentro, y cada llamada vuelve a pasar por nuestro `add` redefinido.')
        bullet('Heredar te ata a DETALLES INTERNOS de la clase padre.')

        section('La versión con composición')

        def compuesto = new ContadorCompuesto()
        compuesto.addAll(['a', 'b', 'c'])
        show('elementos', compuesto.size())
        show('contador', compuesto.añadidos)
        bullet('Aquí el contador no depende de cómo esté hecho ArrayList por dentro.')

        section('La regla')

        bullet('Hereda cuando el hijo ES un padre y puede sustituirlo en cualquier')
        bullet('sitio (principio de sustitución).')
        bullet('Compón cuando sólo quieres REUTILIZAR código.')
        bullet('En Groovy hay dos herramientas mejores que heredar para reutilizar:')
        bullet('los traits (11.2) y @Delegate (capítulo 12).')
    }

    /** Herencia mal usada: depende de cómo ArrayList llame a sus propios métodos. */
    static class ListaQueCuenta extends ArrayList<String> {
        int añadidos = 0

        @Override
        boolean add(String elemento) {
            añadidos++
            super.add(elemento)
        }

        @Override
        boolean addAll(Collection<? extends String> elementos) {
            añadidos += elementos.size()
            super.addAll(elementos)
        }
    }

    /** Composición: la lista es un campo, no una superclase. */
    static class ContadorCompuesto {
        private final List<String> elementos = []
        int añadidos = 0

        void add(String elemento) {
            añadidos++
            elementos << elemento
        }

        void addAll(Collection<String> nuevos) {
            añadidos += nuevos.size()
            elementos.addAll(nuevos)
        }

        int size() { elementos.size() }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `@Override` a `sonidoo()` y lee el error del compilador.
//  2. Quita el `addAll` redefinido de `ListaQueCuenta` y vuelve a contar.
//  3. Implementa `Imprimible` con un mapa al que le falte un método y mira qué pasa.
//  4. Haz que `Cachorro` NO llame a `super.sonido()` y compara.
