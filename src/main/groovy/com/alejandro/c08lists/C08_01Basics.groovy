package com.alejandro.c08lists

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  08.1 · Crear, acceder y modificar
//
//  QUÉ ES
//    `[1, 2, 3]` es una `java.util.ArrayList`. Groovy le añade el indexado con rangos,
//    los índices negativos y un montón de métodos del GDK.
//
//  POR QUÉ IMPORTA
//    Es la estructura que más se usa, y la que más métodos "parecidos" tiene. Saber
//    cuáles MUTAN y cuáles devuelven una copia evita la mitad de los bugs con
//    colecciones.
//
//  ERRORES COMUNES
//    · `lista.remove(2)` borrando por ÍNDICE cuando querías borrar el valor 2.
//    · `sort()` mutando la lista original sin darse cuenta.
//    · Modificar una lista mientras se recorre.
// =====================================================================================

class C08_01Basics {

    /**
     * Crear listas.
     */
    static void demoCreating() {
        section('El literal y lo que produce')

        def lista = [1, 2, 3]
        show('[1, 2, 3].class', lista.class.name)
        show('vacía', [])
        show('mezclada', [1, 'dos', 3.0, null])
        bullet('Sin tipo declarado, una lista admite cualquier cosa.')

        section('Con tipo')

        List<String> tipada = ['a', 'b']
        show('List<String>', tipada)
        bullet('Los genéricos se borran al compilar: es documentación y ayuda al IDE.')
        bullet('Sin @CompileStatic, meter un número ahí dentro NO falla.')

        section('Otras implementaciones')

        show('as LinkedList', ([1, 2] as LinkedList).class.simpleName)
        show('as Set', [1, 2, 2] as Set)
        show('as TreeSet (ordenado)', [3, 1, 2] as TreeSet)
        show('new ArrayList(1..3)', new ArrayList(1..3))

        section('Listas generadas')

        show('(1..5).toList()', (1..5).toList())
        show('[0] * 5', [0] * 5)
        show('(1..3).collect { it * it }', (1..3).collect { it * it })
        show("'abc'.toList()", 'abc'.toList())

        section('Listas de listas')

        def matriz = [[1, 2], [3, 4]]
        show('matriz', matriz)
        show('matriz[1][0]', matriz[1][0])
        bullet('Cuidado con `[0] * 3` de listas: repite LA MISMA referencia.')
        def repetida = [[0]] * 3
        repetida[0] << 9
        show('[[0]] * 3 tras tocar el primero', repetida)
        bullet('Las tres son el mismo objeto. Para copias de verdad: collect { [0] }.')
    }

    /**
     * Acceder a los elementos.
     */
    static void demoAccessing() {
        section('Índices, también negativos')

        def lista = ['a', 'b', 'c', 'd', 'e']
        show('lista[0]', lista[0])
        show('lista[-1] (el último)', lista[-1])
        show('lista[-2]', lista[-2])
        bullet('Los índices negativos cuentan desde el final. En Java no existen.')

        section('Rangos')

        show('lista[1..3]', lista[1..3])
        show('lista[1..<3]', lista[1..<3])
        show('lista[-3..-1]', lista[-3..-1])
        show('lista[2..0] (al revés)', lista[2..0])

        section('Varios índices sueltos')

        show('lista[0, 2, 4]', lista[0, 2, 4])
        show('lista[0..1, 4]', lista[0..1, 4])

        section('Fuera de rango')

        show('lista[10]', lista[10])
        bullet('Devuelve null, NO lanza. Distinto de Java.')
        try {
            show('lista.get(10)', lista.get(10))
        } catch (Exception e) {
            show('lista.get(10)', e.class.simpleName)
        }
        bullet('`get()` sí lanza. El subíndice `[]` es `getAt()`, que es más blando.')

        section('Los primeros y los últimos')

        show('head()', lista.head())
        show('tail()', lista.tail())
        show('init()', lista.init())
        show('last()', lista.last())
        show('first()', lista.first())
        show('take(2)', lista.take(2))
        show('drop(2)', lista.drop(2))
        show('takeRight(2)', lista.takeRight(2))
        bullet('`head`/`first` lanzan si la lista está vacía; `take(1)` no.')
        show('[].take(1)', [].take(1))
    }

    /**
     * Añadir y quitar. Aquí están las trampas.
     */
    static void demoAddingRemoving() {
        section('Añadir')

        def lista = [1, 2]
        lista << 3
        show('tras <<', lista)
        lista.add(4)
        show('tras add(4)', lista)
        lista.addAll([5, 6])
        show('tras addAll', lista)
        bullet('Los tres MUTAN la lista.')

        show('[1, 2] + 3  (no muta)', [1, 2] + 3)
        show('[1, 2] + [3, 4]', [1, 2] + [3, 4])

        section('LA TRAMPA: remove() borra por ÍNDICE')

        def numeros = [10, 20, 30]
        numeros.remove(1)
        show('[10,20,30].remove(1)', numeros)
        bullet('Ha borrado el de la POSICIÓN 1, no el valor 1.')

        section('Y pedir que borre por valor tampoco es evidente')

        def otros = [10, 20, 30]
        try {
            otros.remove(Integer.valueOf(20))
            show('remove(Integer.valueOf(20))', otros)
        } catch (Exception e) {
            show('remove(Integer.valueOf(20))', e.class.simpleName)
            bullet('Ni así: Groovy sigue eligiendo remove(int) y se va de rango.')
        }

        section('Las formas que SÍ borran por valor')

        def porValor = [10, 20, 30]
        porValor.removeElement(20)
        show('removeElement(20)', porValor)
        show('[10,20,30] - 20', [10, 20, 30] - 20)
        show('[10,20,30] - [10, 30]', [10, 20, 30] - [10, 30])
        show('removeAll { it > 15 }', quitarMayoresDe15())
        bullet('Con listas de enteros, usa SIEMPRE `removeElement` o el operador `-`.')

        section('Vaciar')

        def paraVaciar = [1, 2, 3]
        paraVaciar.clear()
        show('tras clear()', paraVaciar)
    }

    private static List<Integer> quitarMayoresDe15() {
        def lista = [10, 20, 30]
        lista.removeAll { it > 15 }
        lista
    }

    /**
     * Qué muta y qué no: la tabla.
     */
    static void demoMutationTable() {
        section('La tabla que hay que tener clara')

        bullet('método            ¿muta?   devuelve')
        bullet('───────────────   ──────   ─────────────────────')
        bullet('sort()            SÍ       la misma lista')
        bullet('sort(false)       no       una copia ordenada')
        bullet('toSorted()        no       una copia ordenada')
        bullet('unique()          SÍ       la misma lista')
        bullet('toUnique()        no       una copia')
        bullet('reverse()         no       una copia')
        bullet('reverseEach()     no       la misma (sólo recorre)')
        bullet('collect/findAll   no       una lista nueva')
        bullet('<<  add  addAll   SÍ       la misma lista')
        bullet('+  -  *           no       una lista nueva')

        section('Comprobado: sort() muta')

        def a = [3, 1, 2]
        a.sort()
        show('tras a.sort()', a)

        def b = [3, 1, 2]
        b.sort(false)
        show('tras b.sort(false)', b)

        def c = [3, 1, 2]
        c.toSorted()
        show('tras c.toSorted()', c)

        section('Pero reverse() NO muta')

        def d = [1, 2, 3]
        d.reverse()
        show('tras d.reverse()', d)
        show('d.reverse() devuelve', d.reverse())
        bullet('`sort` muta y `reverse` no. No hay lógica: hay que saberlo.')

        section('Y unique() SÍ muta')

        def e = [1, 1, 2]
        e.unique()
        show('tras e.unique()', e)
        def f = [1, 1, 2]
        show('f.toUnique() devuelve', f.toUnique())
        show('y f sigue siendo', f)

        section('La regla para no equivocarse')

        bullet('Si el nombre empieza por `to…`, devuelve una copia: toSorted, toUnique.')
        bullet('Si dudas, pasa `false` como primer argumento: sort(false), unique(false).')
        bullet('Y si la lista es un parámetro que te han dado, NUNCA la mutes.')
    }

    /**
     * Modificar mientras se recorre.
     */
    static void demoConcurrentModification() {
        section('Lo que no se puede hacer')

        def lista = [1, 2, 3]
        try {
            lista.each { if (it == 1) lista << 9 }
            show('no llega', lista)
        } catch (Exception e) {
            show('añadir dentro de un each', e.class.simpleName)
        }
        bullet('Es la misma regla de Java: el iterador se invalida.')

        section('Las formas correctas')

        show('construir otra lista', [1, 2, 3].collect { it == 1 ? [it, 9] : [it] }.flatten())
        show('filtrar a una nueva', [1, 2, 3].findAll { it != 2 })
        show('removeAll con predicado', conRemoveAll())
        bullet('`removeAll { }` sí puede: usa el iterador por dentro.')

        section('O recorrer una copia')

        def original = [1, 2, 3]
        original.toList().each { if (it == 1) original << 9 }
        show('recorriendo una copia', original)
        bullet('`toList()` copia. `each` sobre la copia, modificaciones en la original.')
    }

    private static List<Integer> conRemoveAll() {
        def lista = [1, 2, 3, 4]
        lista.removeAll { it % 2 == 0 }
        lista
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Prueba `[10,20,30].remove(30)` y explica el error.
//  2. Haz `def l = [[0]] * 3` y modifica `l[0]`: mira qué pasa con los otros dos.
//  3. Ordena una lista que te pasen como parámetro con `sort()` y piensa a quién le
//     acabas de cambiar los datos.
//  4. Prueba `lista[10] = 'x'` sobre una lista de 3 elementos y mira qué tamaño queda.
