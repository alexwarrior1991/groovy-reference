package com.alejandro.c08lists

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  08.3 · Inmutabilidad y copias defensivas
//
//  QUÉ ES
//    `asImmutable()` envuelve una colección de forma que cualquier intento de
//    modificarla lanza. Pero la envoltura es SUPERFICIAL: protege la lista, no lo que
//    hay dentro.
//
//  POR QUÉ IMPORTA
//    Devolver una colección interna sin proteger es una de las formas más silenciosas
//    de romper el encapsulamiento: quien la recibe puede modificarte el estado.
//
//  ERRORES COMUNES
//    · Creer que `asImmutable()` es profundo.
//    · Devolver el campo directamente desde un getter.
//    · Guardar la lista que te pasan por el constructor sin copiarla.
// =====================================================================================

class C08_03Immutability {

    /**
     * asImmutable y sus límites.
     */
    static void demoImmutable() {
        section('Una lista inmutable rechaza los cambios')

        def congelada = [1, 2, 3].asImmutable()
        show('lectura', congelada[0])
        try {
            congelada << 4
        } catch (UnsupportedOperationException e) {
            show('congelada << 4', e.class.simpleName)
        }

        section('Pero la protección es SUPERFICIAL')

        def anidada = [[1, 2], [3]].asImmutable()
        anidada[0] << 99
        show('tras tocar el elemento interior', anidada)
        bullet('La lista de fuera está protegida; las de dentro, no.')

        section('Para que sea profundo, hay que congelar cada nivel')

        def profunda = [[1, 2], [3]].collect { it.asImmutable() }.asImmutable()
        try {
            profunda[0] << 99
        } catch (UnsupportedOperationException e) {
            show('tras congelar cada nivel', e.class.simpleName)
        }

        section('Las otras formas')

        show('List.of(1, 2)  (Java 9+)', List.of(1, 2).class.simpleName)
        show('Collections.unmodifiableList', Collections.unmodifiableList([1, 2]).class.simpleName)
        bullet('`List.of` copia y es inmutable de verdad; `unmodifiableList` es una')
        bullet('VISTA: si alguien cambia la lista original, la vista cambia con ella.')

        def original = [1, 2]
        def vista = Collections.unmodifiableList(original)
        original << 3
        show('la vista tras cambiar el original', vista)
        show('asImmutable() tras cambiar el original', comprobarCopia())
    }

    private static List<Integer> comprobarCopia() {
        def original = [1, 2]
        def copia = original.asImmutable()
        original << 3
        copia
    }

    /**
     * Copias defensivas en una clase.
     */
    static void demoDefensiveCopies() {
        section('Lo que pasa sin proteger nada')

        def malo = new CarritoRoto(['libro'])
        def robado = malo.productos
        robado << 'producto que nadie añadió'
        show('el carrito por dentro', malo.productos)
        bullet('Quien recibe el getter puede modificarte el estado. Sin avisar.')

        section('Y por el constructor, lo mismo')

        def lista = ['libro']
        def otroMalo = new CarritoRoto(lista)
        lista << 'colado por el constructor'
        show('el carrito por dentro', otroMalo.productos)
        bullet('Guardó la referencia que le pasaron, no una copia.')

        section('La versión correcta')

        def listaOriginal = ['libro']
        def bueno = new Carrito(listaOriginal)
        listaOriginal << 'intento por el constructor'
        show('tras tocar la lista original', bueno.productos)

        def copiaDelGetter = bueno.productos
        try {
            copiaDelGetter << 'intento por el getter'
        } catch (UnsupportedOperationException e) {
            show('modificando lo que devuelve el getter', e.class.simpleName)
        }
        show('el carrito sigue igual', bueno.productos)

        section('Las dos reglas')

        bullet('1. COPIA lo que te dan: `this.x = new ArrayList<>(x)`.')
        bullet('2. PROTEGE lo que devuelves: `x.asImmutable()`.')
        bullet('El capítulo 12 enseña @Immutable, que hace las dos cosas sola.')

        section('Modificar de verdad: métodos que expresan la intención')

        def carrito = new Carrito(['libro'])
        carrito.añadir('lápiz')
        show('tras añadir', carrito.productos)
        bullet('El estado sólo cambia por donde tú decides. Eso es encapsular.')
    }

    static class CarritoRoto {
        final List<String> productos

        CarritoRoto(List<String> productos) { this.productos = productos }
    }

    static class Carrito {
        private final List<String> productos

        Carrito(List<String> productos) {
            // 1. copia defensiva al entrar
            this.productos = new ArrayList<String>(productos)
        }

        /** 2. copia (o vista inmutable) al salir. */
        List<String> getProductos() { productos.asImmutable() }

        void añadir(String producto) { productos << producto }
    }

    /**
     * Elegir la estructura correcta.
     */
    static void demoChoosingStructure() {
        section('Una lista no siempre es la respuesta')

        bullet('estructura      buena para                     mala para')
        bullet('─────────────   ──────────────────────────     ────────────────')
        bullet('ArrayList       índice, recorrer                buscar, borrar en medio')
        bullet('LinkedList      insertar/borrar en los bordes   índice')
        bullet('HashSet         pertenencia, quitar duplicados  orden')
        bullet('LinkedHashSet   lo mismo, manteniendo el orden  memoria')
        bullet('TreeSet         mantener ordenado               insertar mucho')
        bullet('ArrayDeque      pila y cola                     índice')

        section('El caso que más se ve: contains() sobre una lista grande')

        def lista = (1..20_000).toList()
        def conjunto = lista as Set

        long t1 = medir { 5_000.times { 19_999 in lista } }
        long t2 = medir { 5_000.times { 19_999 in conjunto } }

        show('5.000 búsquedas en List', "${t1} ms")
        show('5.000 búsquedas en Set', "${t2} ms")
        bullet('La lista recorre hasta encontrarlo; el Set calcula el hash y va.')
        bullet('Si vas a preguntar "¿está?" muchas veces, conviértelo a Set una vez.')

        section('Quitar duplicados manteniendo el orden')

        def conRepes = [3, 1, 3, 2, 1]
        show('as Set (orden de inserción)', conRepes as Set)
        show('as TreeSet (ordenado)', conRepes as TreeSet)
        show('toUnique() (lista, orden original)', conRepes.toUnique())

        section('Pila y cola')

        def pila = [] as ArrayDeque
        pila.push('a'); pila.push('b')
        show('pila.pop()', pila.pop())

        def cola = [] as ArrayDeque
        cola.add('a'); cola.add('b')
        show('cola.poll()', cola.poll())
    }

    private static long medir(Closure<?> bloque) {
        long t0 = System.nanoTime()
        bloque()
        ((System.nanoTime() - t0) / 1_000_000L) as long
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `asImmutable()` del getter de `Carrito` y repite el ataque del getter.
//  2. Congela una lista de mapas y modifica un mapa de dentro.
//  3. Sube las búsquedas a 50.000 y vuelve a medir List frente a Set.
//  4. Cambia `new ArrayList<>(productos)` por `productos` en el constructor y
//     comprueba que vuelve el problema.
