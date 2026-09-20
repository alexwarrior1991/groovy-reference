package com.alejandro.c12ast

import groovy.transform.Immutable
import groovy.transform.Sortable
import groovy.transform.TupleConstructor
import groovy.transform.builder.Builder

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  12.2 · @Immutable, @Sortable y @Builder
//
//  QUÉ ES
//    Tres anotaciones que resuelven tres problemas concretos: hacer una clase
//    verdaderamente inmutable, darle un orden natural, y construirla cuando tiene
//    muchos campos opcionales.
//
//  POR QUÉ IMPORTA
//    `@Immutable` hace en una línea lo que en el capítulo 08 costaba media clase:
//    copias defensivas de entrada y de salida, campos finales y `equals`/`hashCode`.
//
//  ERRORES COMUNES
//    · Usar `@Sortable` sin `@TupleConstructor` y no poder construir el objeto.
//    · Esperar que `@Immutable` acepte cualquier tipo de campo: sólo admite los que
//      sabe congelar.
//    · Usar `@Builder` en una clase de dos campos, donde no aporta nada.
// =====================================================================================

class C12_02Immutable {

    /**
     * @Immutable.
     */
    static void demoImmutable() {
        section('Lo que genera')

        def usuario = new UsuarioInmutable('Ana', ['admin', 'editor'])
        show('toString', usuario.toString())
        show('equals', usuario == new UsuarioInmutable('Ana', ['admin', 'editor']))

        section('Los campos son finales: no hay setter')

        try {
            usuario.nombre = 'Luis'
        } catch (Exception e) {
            show('usuario.nombre = "Luis"', e.class.simpleName)
        }

        section('Y las colecciones vienen congeladas')

        try {
            usuario.roles << 'otro'
        } catch (Exception e) {
            show('usuario.roles << "otro"', e.class.simpleName)
        }
        bullet('Esto es lo que @Immutable hace y `final` por sí solo NO hace:')
        bullet('copia la lista al entrar y la devuelve inmutable al salir.')

        section('Comprobado: copia al construir')

        def rolesOriginales = ['admin']
        def conCopia = new UsuarioInmutable('Ana', rolesOriginales)
        rolesOriginales << 'colado'
        show('roles tras tocar la lista original', conCopia.roles)
        bullet('Es la copia defensiva del capítulo 08, gratis.')

        section('Para "cambiar" algo: copyWith')

        def otro = usuario.copyWith(nombre: 'Luis')
        show('copyWith(nombre: "Luis")', otro.toString())
        show('el original sigue', usuario.nombre)
        bullet('Hace falta `copyWith = true` en la anotación. Es el patrón de los')
        bullet('objetos de valor: no se modifican, se crea uno nuevo.')

        section('Qué tipos admite')

        bullet('primitivos, String, y los envoltorios')
        bullet('Date, LocalDate y compañía (los copia)')
        bullet('List, Map, Set (los congela)')
        bullet('otras clases @Immutable')
        bullet('enums')

        section('Y qué pasa con un tipo que no sabe congelar')

        show('con un campo de otro tipo', new ConTipoRaro(new StringBuilder('mutable')).toString())
        bullet('Lo acepta pero NO puede garantizar nada: si ese objeto es mutable,')
        bullet('la inmutabilidad es una mentira. Usa `knownImmutableClasses` sólo')
        bullet('cuando de verdad lo sean.')

        section('Cuándo usarlo')

        bullet('Objetos de valor: dinero, coordenadas, rangos, identificadores.')
        bullet('Todo lo que vaya a ser clave de un mapa o elemento de un Set.')
        bullet('Todo lo que se comparta entre hilos (capítulo 27).')
    }

    @Immutable(copyWith = true)
    static class UsuarioInmutable {
        String nombre
        List<String> roles
    }

    @Immutable(knownImmutableClasses = [StringBuilder])
    static class ConTipoRaro {
        StringBuilder texto
    }

    /**
     * @Sortable.
     */
    static void demoSortable() {
        section('Un orden natural, gratis')

        def gente = [
            new Persona('Zapata', 'Ana'),
            new Persona('Alonso', 'Luis'),
            new Persona('Alonso', 'Ana'),
        ]

        show('sin argumentos: por orden de declaración', gente.sort(false)*.toString())
        bullet('Primero por apellido, y a igualdad, por nombre. Ese es el orden')
        bullet('en que están declarados los campos.')

        section('Y además genera un Comparator por cada campo')

        show('comparatorByNombre()', gente.toSorted(Persona.comparatorByNombre())*.nombre)
        show('comparatorByApellido()', gente.toSorted(Persona.comparatorByApellido())*.apellido)
        bullet('Útil cuando necesitas ordenar por un criterio distinto del natural.')

        section('Elegir qué campos ordenan')

        show('includes = ["nombre"]', [
            new SoloPorNombre('Zapata', 'Ana'),
            new SoloPorNombre('Alonso', 'Luis'),
        ].sort(false)*.nombre)

        section('OJO: @Sortable no genera constructor')

        bullet('Implementa `Comparable` y poco más. Si quieres construir el objeto')
        bullet('posicionalmente, añade `@TupleConstructor` (o `@Canonical`).')
        bullet('Es el error más común con esta anotación.')
    }

    @Sortable
    @TupleConstructor
    static class Persona {
        String apellido
        String nombre

        @Override
        String toString() { "$apellido, $nombre" }
    }

    @Sortable(includes = ['nombre'])
    @TupleConstructor
    static class SoloPorNombre {
        String apellido
        String nombre
    }

    /**
     * @Builder.
     */
    static void demoBuilder() {
        section('El problema: muchos campos opcionales')

        bullet('new Peticion("GET", "/api", null, 30, true, null, "UTF-8")')
        bullet('¿Qué es cada cosa? ¿Y si mañana se añade un campo en medio?')

        section('Con @Builder')

        def peticion = Peticion.builder()
            .metodo('GET')
            .url('/api/usuarios')
            .tiempoDeEspera(30)
            .build()

        show('resultado', peticion.toString())
        bullet('Cada campo se nombra, el orden da igual y los que no pones quedan')
        bullet('a su valor por defecto.')

        section('Las estrategias')

        bullet('DefaultStrategy         Clase.builder()…build()   ← la de por defecto')
        bullet('SimpleStrategy          setX() encadenables sobre el propio objeto')
        bullet('ExternalStrategy        el builder va en OTRA clase')
        bullet('InitializerStrategy     obliga a poner los obligatorios, y lo comprueba')
        bullet('                        el COMPILADOR')

        section('Cuándo NO usarlo')

        bullet('Con dos o tres campos: el constructor de mapa ya hace ese trabajo.')
        show('el constructor de mapa, gratis', new Config2(host: 'h', puerto: 80).toString())
        bullet('@Builder empieza a compensar a partir de cinco o seis campos, o')
        bullet('cuando quieres validar en el `build()`.')
    }

    @Builder
    @groovy.transform.ToString(includeNames = true, includePackage = false, ignoreNulls = true)
    static class Peticion {
        String metodo
        String url
        String cuerpo
        int tiempoDeEspera
        boolean seguirRedirecciones
    }

    @groovy.transform.ToString(includeNames = true, includePackage = false)
    static class Config2 {
        String host
        int puerto
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita `copyWith = true` y mira qué error da `copyWith`.
//  2. Quita `@TupleConstructor` de `Persona` y comprueba que no se puede construir.
//  3. Añade un campo `Date` a `UsuarioInmutable` y comprueba si lo copia.
//  4. Cambia `@Builder` por `@Builder(builderStrategy = SimpleStrategy)` y mira cómo
//     cambia la forma de usarlo.
