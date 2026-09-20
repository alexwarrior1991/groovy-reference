package com.alejandro.c12ast

import groovy.transform.InheritConstructors
import groovy.transform.Memoized
import groovy.transform.NullCheck
import groovy.transform.TupleConstructor

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  12.3 · @Delegate, @Lazy, @Memoized, @Singleton y las demás
//
//  QUÉ ES
//    El resto del catálogo: las que añaden comportamiento en vez de sólo generar
//    métodos aburridos. `@Delegate` es la más importante: es la alternativa de Groovy
//    a heredar para reutilizar.
//
//  POR QUÉ IMPORTA
//    `@Delegate` resuelve en una línea el problema del "contador roto" del capítulo 11.
//    Y `@Memoized` y `@Lazy` son optimizaciones que normalmente cuestan una clase
//    entera.
//
//  ERRORES COMUNES
//    · Creer que `@Delegate` hace la clase `instanceof` del tipo delegado. NO lo hace.
//    · `@Lazy` sobre algo que se usa siempre: sólo añade una comprobación.
//    · `@Memoized` sobre un método con efectos secundarios.
// =====================================================================================

class C12_03Others {

    /**
     * @Delegate: composición sin escribir los reenvíos.
     */
    static void demoDelegate() {
        section('El problema: reenviar métodos a mano')

        bullet('Componer en vez de heredar está bien… hasta que hay quince métodos')
        bullet('que sólo hacen `return interno.loMismo(args)`.')

        section('@Delegate los escribe por ti')

        def coche = new Coche()
        show('coche.arrancar()', coche.arrancar())
        show('coche.parar()', coche.parar())
        show('coche.marca', coche.marca)
        bullet('`arrancar` y `parar` son del Motor. La clase Coche no los declara.')

        section('Se ve en los métodos generados')

        show('métodos que vienen del Motor', Coche.class.methods*.name.findAll {
            it in ['arrancar', 'parar']
        }.sort())

        section('LA DIFERENCIA con heredar: no hay relación de tipos')

        show('coche instanceof Motor', coche instanceof Motor)
        bullet('NO es un Motor: sólo reenvía. Eso es justo lo que quieres cuando')
        bullet('no hay una relación "es un".')
        bullet('Si además necesitas el tipo, usa `@Delegate(interfaces = true)` sobre')
        bullet('una INTERFAZ, no sobre una clase.')

        section('Resolviendo el contador roto del capítulo 11')

        def contador = new ListaQueCuenta()
        contador.addAll(['a', 'b', 'c'])
        show('elementos', contador.size())
        show('añadidos contados', contador.añadidos)
        bullet('Ahora sí cuenta 3. Al delegar, `addAll` del ArrayList interno llama')
        bullet('a SU `add`, no al nuestro. Se acabó la fragilidad.')

        section('Las opciones útiles')

        bullet('excludes / includes   qué métodos reenviar')
        bullet('interfaces            implementar también las interfaces del delegado')
        bullet('deprecated            incluir los métodos marcados @Deprecated')
    }

    static class Motor {
        String arrancar() { 'motor en marcha' }

        String parar() { 'motor parado' }
    }

    static class Coche {
        @Delegate
        Motor motor = new Motor()

        String marca = 'genérica'
    }

    static class ListaQueCuenta {
        @Delegate
        private final List<String> elementos = []

        int añadidos = 0

        boolean add(String elemento) {
            añadidos++
            elementos.add(elemento)
        }

        boolean addAll(Collection<? extends String> nuevos) {
            añadidos += nuevos.size()
            elementos.addAll(nuevos)
        }
    }

    /**
     * @Lazy y @Memoized.
     */
    static void demoLazyAndMemoized() {
        section('@Lazy: no lo calcules hasta que alguien lo pida')

        Traza.pasos.clear()
        def objeto = new ConLazy()
        show('tras crear el objeto', Traza.pasos)
        bullet('El objeto existe y el campo caro todavía no se ha calculado.')

        show('primera lectura', objeto.caro)
        show('la traza ahora', Traza.pasos)

        show('segunda lectura', objeto.caro)
        show('la traza sigue igual', Traza.pasos)
        bullet('Se calcula UNA vez y se guarda.')

        section('Cuándo usarlo')

        bullet('Cuando el cálculo es caro Y hay caminos donde no se usa.')
        bullet('Si se usa siempre, `@Lazy` sólo añade una comprobación de null.')
        bullet('Para inicialización segura entre hilos: `@Lazy(soft = true)` usa')
        bullet('una referencia débil; sin eso, ojo con la concurrencia.')

        section('@Memoized: lo mismo, pero por argumentos')

        Traza.pasos.clear()
        def calculadora = new ConMemoized()
        show('caro(5)', calculadora.caro(5))
        show('caro(5) otra vez', calculadora.caro(5))
        show('caro(6)', calculadora.caro(6))
        show('cuántas veces se ejecutó', Traza.pasos.size())
        bullet('Es el `memoize()` de los closures (capítulo 07), en forma de anotación.')

        section('Con límite')

        bullet('@Memoized(maxCacheSize = 100)          tope duro')
        bullet('@Memoized(protectedCacheSize = 10)     mínimo garantizado')
        bullet('Sin límite, un método memoizado que reciba muchos argumentos')
        bullet('distintos es una fuga de memoria.')
    }

    static class Traza {
        static final List<String> pasos = []
    }

    static class ConLazy {
        @Lazy
        String caro = {
            Traza.pasos << 'calculado'
            'el valor caro'
        }()
    }

    static class ConMemoized {
        @Memoized
        int caro(int n) {
            Traza.pasos << "calculado $n"
            n * 2
        }
    }

    /**
     * @Singleton, @InheritConstructors, @NullCheck.
     */
    static void demoOthers() {
        section('@Singleton')

        show('Configuracion.instance.valor', Configuracion.instance.valor)
        show('siempre el mismo objeto', Configuracion.instance.is(Configuracion.instance))
        try {
            new Configuracion()
        } catch (Exception e) {
            show('new Configuracion()', e.class.simpleName)
        }
        bullet('El constructor queda privado. `lazy = true` lo crea al primer uso.')
        bullet('Y como todo singleton: cuesta de testear. Úsalo poco.')

        section('@InheritConstructors: heredar los constructores del padre')

        show('new MiError("algo falló")', new MiError('algo falló').message)
        show('new MiError(mensaje, causa)', new MiError('x', new RuntimeException('causa')).cause.message)
        bullet('Sin la anotación habría que escribir los cuatro constructores de')
        bullet('Exception a mano. Es EL caso de uso: excepciones propias.')

        section('@NullCheck: rechazar nulos en los parámetros')

        show('procesar("dato")', procesar('dato'))
        try {
            show('procesar(null)', procesar(null))
        } catch (Exception e) {
            show('procesar(null)', "${e.class.simpleName}: ${e.message}")
        }
        bullet('Genera el `if (x == null) throw …` al principio del método.')

        section('El catálogo que falta por ver')

        bullet('@Synchronized, @WithReadLock, @WithWriteLock   capítulo 27')
        bullet('@CompileStatic, @TypeChecked                   capítulo 15')
        bullet('@Category                                      capítulo 17')
        bullet('@Field, @BaseScript                            capítulo 25')
        bullet('@AutoClone, @AutoImplement, @Newify, @IndexedProperty, @Trait…')
        bullet('La lista entera está en groovy.transform. Merece un vistazo.')
    }

    @Singleton
    static class Configuracion {
        String valor = 'soy la única configuración'
    }

    @InheritConstructors
    static class MiError extends RuntimeException {}

    @NullCheck
    private static String procesar(String dato) { "procesado: $dato" }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita `@Delegate` de `ListaQueCuenta` y mira cuántos métodos hay que escribir.
//  2. Prueba `@Delegate(interfaces = true)` sobre un campo de tipo `List` y comprueba
//     si ahora `instanceof List` es cierto.
//  3. Quita `@Lazy` y mira en qué momento aparece la traza.
//  4. Pon `@Memoized` en un método que imprima algo y llámalo dos veces.
//  5. Quita `@InheritConstructors` de `MiError` y cuenta los constructores que
//     necesitas escribir.
