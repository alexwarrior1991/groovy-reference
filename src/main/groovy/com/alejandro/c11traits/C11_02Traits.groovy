package com.alejandro.c11traits

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  11.2 · Traits: interfaces con estado
//
//  QUÉ ES
//    Un `trait` es una interfaz que además puede tener IMPLEMENTACIÓN y ESTADO. Una
//    clase puede implementar todos los que quiera, así que resuelve la reutilización
//    sin el problema de la herencia múltiple.
//
//  POR QUÉ IMPORTA
//    Es la herramienta que Java no tiene (sus `default methods` no pueden tener
//    estado) y la mejor respuesta de Groovy a "quiero reutilizar este comportamiento en
//    varias clases que no comparten jerarquía".
//
//  ERRORES COMUNES
//    · Dos traits con el mismo método y no saber cuál gana (gana el ÚLTIMO).
//    · Usar `++` sobre un campo de un trait: no compila.
//    · Meter en un trait estado que en realidad era de la clase.
// =====================================================================================

trait Identificable {
    /** Un trait puede declarar métodos abstractos, como una interfaz. */
    abstract String getId()

    String etiqueta() { "[${getId()}]" }
}

trait Auditable {
    /** Y también ESTADO. Esto es lo que una interfaz no puede hacer. */
    List<String> eventos = []

    void registrar(String evento) { eventos << evento }

    int cuantosEventos() { eventos.size() }
}

trait Versionable {
    int version = 0

    /** Ojo: `++version` NO compila sobre un campo de trait. Hay que escribirlo así. */
    int siguienteVersion() {
        version = version + 1
        version
    }
}

class Documento implements Identificable, Auditable, Versionable {
    String titulo

    Documento(String titulo) { this.titulo = titulo }

    @Override
    String getId() { titulo.toLowerCase().replace(' ', '-') }
}

// -- Conflictos ----------------------------------------------------------------------

trait Volador {
    String moverse() { 'volando' }

    String describir() { "soy un ${getClass().simpleName} y voy ${moverse()}" }
}

trait Nadador {
    String moverse() { 'nadando' }
}

/** Aquí elegimos a mano cuál queremos, en vez de dejar que decida el orden. */
class Pato implements Volador, Nadador {
    @Override
    String moverse() { Volador.super.moverse() + ' y ' + Nadador.super.moverse() }
}

/** Una clase sin nada, para añadirle traits en caliente y ver quién gana. */
class Animal {}

class C11_02Traits {

    /**
     * Lo que un trait puede hacer y una interfaz no.
     */
    static void demoStatefulTraits() {
        section('Un trait con estado')

        def doc = new Documento('Mi Informe Anual')
        doc.registrar('creado')
        doc.registrar('revisado')

        show('doc.eventos', doc.eventos)
        show('doc.cuantosEventos()', doc.cuantosEventos())
        bullet('`eventos` es un campo declarado EN EL TRAIT. Una interfaz no puede.')

        section('El estado es de cada instancia, no compartido')

        def otro = new Documento('Otro')
        otro.registrar('creado')
        show('doc', doc.cuantosEventos())
        show('otro', otro.cuantosEventos())
        bullet('Groovy le genera a la clase un campo propio por cada campo del trait.')

        section('Un trait puede declarar métodos abstractos')

        show('doc.getId()', doc.getId())
        show('doc.etiqueta()', doc.etiqueta())
        bullet('`etiqueta()` está en el trait y usa `getId()`, que implementa la clase.')

        section('Varios traits a la vez')

        show('doc.siguienteVersion()', doc.siguienteVersion())
        show('otra vez', doc.siguienteVersion())
        show('los tipos que cumple', [Identificable, Auditable, Versionable].findAll {
            it.isInstance(doc)
        }*.simpleName)

        section('La limitación que sorprende')

        bullet('`++version` sobre un campo de trait NO compila:')
        bullet('"Prefix expressions on trait fields/properties are not supported".')
        bullet('Hay que escribir `version = version + 1`. Es un detalle de cómo')
        bullet('Groovy implementa el estado de los traits.')
    }

    /**
     * Conflictos entre traits.
     */
    static void demoConflicts() {
        section('Dos traits con el mismo método: gana el ÚLTIMO')

        show('withTraits(Volador, Nadador)', new Animal().withTraits(Volador, Nadador).moverse())
        show('withTraits(Nadador, Volador)', new Animal().withTraits(Nadador, Volador).moverse())
        bullet('El mismo objeto y los mismos traits: sólo cambia el ORDEN.')
        bullet('Gana el último. No hay error ni aviso de ninguna clase.')

        section('Y el método heredado del otro trait usa al ganador')

        show('describir() con Nadador el último', new Animal().withTraits(Volador, Nadador).describir())
        bullet('`describir()` viene de Volador pero llama a `moverse()`, y ahí manda')
        bullet('quien haya ganado. Polimorfismo normal.')

        section('Resolver el conflicto a mano con `Trait.super`')

        show('new Pato().moverse()', new Pato().moverse())
        bullet('`Volador.super.moverse()` llama a la implementación de ESE trait.')
        bullet('Es lo que hay que hacer siempre que el orden importe de verdad.')

        section('El orden de precedencia completo')

        bullet('1. el método de la propia CLASE')
        bullet('2. el del ÚLTIMO trait declarado')
        bullet('3. …hacia atrás por la lista de traits')
        bullet('4. el de la superclase')

        section('Un aviso si compilas Groovy y Java juntos')

        bullet('Una clase que implementa dos traits con el mismo método y NO lo')
        bullet('redefine rompe la generación de stubs de Java ("method is already')
        bullet('defined"). Por eso aquí `Pato` lo redefine y el conflicto se enseña')
        bullet('con `withTraits`. Otra razón para ser explícito.')
    }

    /**
     * Traits en tiempo de ejecución.
     */
    static void demoRuntimeTraits() {
        section('Añadir un trait a un objeto YA creado')

        def simple = new Simple('cosa')
        def conAuditoria = simple.withTraits(Auditable)

        conAuditoria.registrar('añadido en caliente')
        show('eventos', conAuditoria.eventos)
        show('¿el original lo tiene?', simple instanceof Auditable)
        show('¿y la copia?', conAuditoria instanceof Auditable)
        bullet('`withTraits` devuelve un objeto NUEVO (un proxy): el original no cambia.')

        section('Con `as` se hace lo mismo')

        def comoAuditable = new Simple('otra') as Auditable
        comoAuditable.registrar('vía as')
        show('eventos', comoAuditable.eventos)

        section('Cuándo usar esto')

        bullet('Para pruebas: añadir trazas a un objeto sin tocar su clase.')
        bullet('Para decorar objetos que vienen de fuera.')
        bullet('En producción, con cuidado: cada proxy es una clase nueva en memoria.')
    }

    static class Simple {
        String nombre

        Simple(String nombre) { this.nombre = nombre }
    }

    /**
     * Cuándo usar un trait y cuándo no.
     */
    static void demoWhenToUse() {
        section('Trait frente a las alternativas')

        bullet('herramienta        cuándo')
        bullet('────────────────   ────────────────────────────────────────')
        bullet('interfaz           sólo contrato, sin código compartido')
        bullet('clase abstracta    una jerarquía de verdad, con constructor')
        bullet('trait              comportamiento reutilizable, con estado,')
        bullet('                   en clases que NO comparten jerarquía')
        bullet('@Delegate          reutilizar un objeto que ya existe (cap. 12)')

        section('El caso en el que un trait brilla')

        bullet('Auditoría, versionado, comparación, serialización: cosas que')
        bullet('necesitan varias clases sin relación entre ellas.')

        section('Cuándo NO usarlo')

        bullet('Si el estado que metes en el trait es en realidad de UNA clase.')
        bullet('Si acabas con cinco traits y no sabes de dónde sale cada método.')
        bullet('Si lo que quieres es sustituir un objeto por otro: eso es una')
        bullet('interfaz y composición, no un trait.')

        section('Un detalle práctico: los traits se ven desde Java')

        bullet('Groovy genera una interfaz más una clase de ayuda, así que el')
        bullet('código Java ve la interfaz y sus métodos abstractos, pero NO')
        bullet('hereda las implementaciones. Capítulo 26.')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `implements Volador, Nadador` por `implements Nadador, Volador` y vuelve
//     a ejecutar la demo de conflictos.
//  2. Escribe `++version` en `Versionable` y lee el error del compilador.
//  3. Añade un trait con un campo del mismo nombre que otro y mira qué pasa.
//  4. Usa `withTraits` con dos traits a la vez.
