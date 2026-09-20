package com.alejandro.c12ast

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.MapConstructor
import groovy.transform.ToString
import groovy.transform.TupleConstructor

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  12.1 · Las que escriben el código aburrido
//
//  QUÉ ES
//    Una transformación AST es una anotación que, al COMPILAR, añade código a tu clase.
//    No es reflexión ni magia en ejecución: el bytecode que sale tiene los métodos
//    escritos, igual que si los hubieras puesto a mano.
//
//  POR QUÉ IMPORTA
//    `toString`, `equals`, `hashCode` y los constructores son el 90% del código
//    repetitivo de un modelo de dominio. Estas anotaciones lo eliminan sin coste en
//    ejecución.
//
//  ERRORES COMUNES
//    · `@EqualsAndHashCode` en una clase mutable que luego se usa como clave de mapa.
//    · `@Canonical` sin saber qué tres cosas activa.
//    · Esperar que `@TupleConstructor` respete un orden distinto al de declaración.
// =====================================================================================

class C12_01Generated {

    /**
     * @ToString y sus opciones.
     */
    static void demoToString() {
        section('Sin anotación')

        show('un POGO pelado', new Pelado(nombre: 'Ana', edad: 30).toString().replaceAll(/@\w+/, '@…'))
        bullet('El `toString` por defecto no dice nada útil.')

        section('@ToString: los valores, en orden de declaración')

        show('@ToString', new Simple(nombre: 'Ana', edad: 30).toString())

        section('Con los nombres de las propiedades')

        show('includeNames = true', new ConNombres(nombre: 'Ana', edad: 30).toString())
        bullet('Es la opción que casi siempre quieres: sin ella, dos campos del mismo')
        bullet('tipo se confunden.')

        section('Las opciones más útiles')

        bullet('includeNames      pone `nombre:valor` en vez de sólo el valor')
        bullet('excludes          deja fuera campos (contraseñas, datos personales)')
        bullet('includes          justo los que digas, en ESE orden')
        bullet('includePackage    false para que no salga com.ejemplo.…')
        bullet('includeSuper      añade los campos de la clase padre')
        bullet('ignoreNulls       se salta los que estén a null')

        show('con excludes', new ConSecreto(usuario: 'ana', contraseña: 'secreta').toString())
        bullet('La contraseña no sale en las trazas. Eso es una opción de SEGURIDAD.')
    }

    static class Pelado {
        String nombre
        int edad
    }

    @ToString(includePackage = false)
    static class Simple {
        String nombre
        int edad
    }

    @ToString(includeNames = true, includePackage = false)
    static class ConNombres {
        String nombre
        int edad
    }

    @ToString(includeNames = true, includePackage = false, excludes = 'contraseña')
    static class ConSecreto {
        String usuario
        String contraseña
    }

    /**
     * @EqualsAndHashCode.
     */
    static void demoEqualsAndHashCode() {
        section('Sin anotación, dos objetos iguales no son iguales')

        show('sin anotación', new Pelado(nombre: 'Ana', edad: 30) == new Pelado(nombre: 'Ana', edad: 30))

        section('Con @EqualsAndHashCode')

        def a = new ConIgualdad(nombre: 'Ana', edad: 30)
        def b = new ConIgualdad(nombre: 'Ana', edad: 30)
        show('a == b', a == b)
        show('mismo hashCode', a.hashCode() == b.hashCode())
        show('en un Set', ([a, b] as Set).size())
        bullet('Genera los DOS métodos: si generas sólo equals, rompes los mapas.')

        section('LA TRAMPA: un objeto mutable como clave')

        def clave = new ConIgualdad(nombre: 'Ana', edad: 30)
        Map<ConIgualdad, String> mapa = [:]
        mapa[clave] = 'guardado'
        show('antes de tocarlo', mapa[clave])

        clave.edad = 31
        show('tras cambiarle la edad', mapa[clave])
        show('el mapa sigue teniendo', mapa.size())
        bullet('Al cambiar un campo cambia el hashCode, y el objeto ya no se')
        bullet('encuentra ni consigo mismo. La entrada queda inaccesible.')
        bullet('Regla: si va a ser clave, que sea INMUTABLE (@Immutable, demo 12.6).')

        section('Elegir qué campos cuentan')

        def x = new PorClave(id: 1, nombre: 'Ana')
        def y = new PorClave(id: 1, nombre: 'ANA')
        show('sólo cuenta el id', x == y)
        bullet('`includes = "id"` es lo correcto cuando hay una clave de negocio.')
    }

    @EqualsAndHashCode
    static class ConIgualdad {
        String nombre
        int edad
    }

    @EqualsAndHashCode(includes = 'id')
    static class PorClave {
        int id
        String nombre
    }

    /**
     * Los constructores.
     */
    static void demoConstructors() {
        section('@TupleConstructor: uno posicional, en orden de declaración')

        show('new Config("h", 80)', new Config('h', 80).toString())
        show('parcial: new Config("h")', new Config('h').toString())
        show('vacío: new Config()', new Config().toString())
        bullet('Genera TODAS las variantes: (), (a), (a, b)… por eso el orden importa.')

        section('@MapConstructor: el de mapa, cuando ya hay otro')

        show('new ConAmbos(nombre: "Ana")', new ConAmbos(nombre: 'Ana').toString())
        show('new ConAmbos("Ana", 30)', new ConAmbos('Ana', 30).toString())
        bullet('Las dos anotaciones se combinan sin problema.')

        section('defaults = false para obligar a pasarlo todo')

        show('new Estricta("h", 80)', new Estricta('h', 80).toString())
        try {
            show('new Estricta("h")', new Estricta('h').toString())
        } catch (Exception e) {
            show('new Estricta("h")', e.class.simpleName)
        }
        bullet('Sin las variantes parciales, olvidarse de un campo no compila.')

        section('@Canonical: los tres de golpe')

        show('toString', new Producto('libro', 20).toString())
        show('equals', new Producto('libro', 20) == new Producto('libro', 20))
        show('constructor posicional', new Producto('libro', 20).nombre)
        bullet('@Canonical = @ToString + @EqualsAndHashCode + @TupleConstructor.')

        section('Cuándo NO usar @Canonical')

        bullet('Cuando quieres opciones distintas en cada una de las tres.')
        bullet('Cuando la igualdad debe mirar sólo una clave de negocio.')
        bullet('Cuando la clase es mutable y va a acabar en un Set o un mapa.')
        bullet('En esos casos, pon las tres anotaciones por separado y configúralas.')
    }

    @TupleConstructor
    @ToString(includeNames = true, includePackage = false)
    static class Config {
        String host
        int puerto
    }

    @TupleConstructor
    @MapConstructor
    @ToString(includeNames = true, includePackage = false)
    static class ConAmbos {
        String nombre
        int edad
    }

    @TupleConstructor(defaults = false)
    @ToString(includeNames = true, includePackage = false)
    static class Estricta {
        String host
        int puerto
    }

    @Canonical
    static class Producto {
        String nombre
        int precio
    }

    /**
     * Qué es en realidad una transformación AST.
     */
    static void demoHowItWorks() {
        section('El código se genera al COMPILAR')

        def metodos = Producto.class.methods*.name.findAll {
            it in ['toString', 'equals', 'hashCode', 'canEqual']
        }.sort().unique()
        show('métodos de @Canonical', metodos)

        def constructores = Producto.class.constructors.collect { it.parameterTypes*.simpleName }
        show('constructores generados', constructores)
        bullet('Están en el bytecode. Java los ve igual que si los hubieras escrito.')

        section('Por eso no cuestan nada en ejecución')

        bullet('No hay reflexión, ni interceptación, ni metaclase de por medio.')
        bullet('Es lo contrario a la metaprogramación en runtime (capítulo 16).')

        section('Local frente a global')

        bullet('LOCAL: se activa con una anotación (@ToString, @Immutable). Son éstas.')
        bullet('GLOBAL: se aplica a TODO lo que se compile, sin anotación. Las usan')
        bullet('frameworks como Spock, que reescribe los bloques given/when/then.')

        section('Lo que hay que saber para usarlas bien')

        bullet('1. Mira qué genera de verdad: `clase.methods` no miente.')
        bullet('2. Lee sus opciones. Casi todas tienen `includes` y `excludes`.')
        bullet('3. Si el IDE no autocompleta algo generado, no pasa nada: existe.')
        bullet('4. Y si necesitas un comportamiento distinto, escribe el método a')
        bullet('   mano: el tuyo gana sobre el generado.')

        show('con toString propio', new ConToStringPropio(nombre: 'Ana').toString())
    }

    @Canonical
    static class ConToStringPropio {
        String nombre

        @Override
        String toString() { "el mío: $nombre" }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita `excludes = 'contraseña'` y mira qué acaba en la traza.
//  2. Mete un `ConIgualdad` en un Set, cámbiale un campo y prueba `set.contains(obj)`.
//  3. Cambia el orden de los campos de `Config` y comprueba que cambia el constructor.
//  4. Imprime `Producto.class.methods*.name.sort()` entero y busca `canEqual`: ¿para
//     qué crees que sirve?
