package com.alejandro.c04operators

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  04.1 · Igualdad: `==` es equals(), y para la identidad está `is()`
//
//  QUÉ ES
//    En Java, `==` compara REFERENCIAS y `equals()` compara contenido. En Groovy están
//    cambiados de sitio:
//      a == b      llama a equals() (o a compareTo() si el objeto es Comparable)
//      a.is(b)     compara la identidad, como el `==` de Java
//
//  POR QUÉ IMPORTA
//    Es la diferencia que más bugs evita al venir de Java: el clásico `if (s1 == s2)`
//    entre cadenas, que en Java funciona "a veces" por la caché de literales, aquí
//    hace siempre lo correcto.
//
//  ERRORES COMUNES
//    · Traducir mentalmente `==` a "misma referencia" y desconfiar de él.
//    · Usar `equals()` a mano sobre algo que puede ser null.
//    · Olvidar que en Comparable, `==` usa compareTo(), no equals(): con BigDecimal,
//      `1.0 == 1.00` es cierto aunque `equals` diga que no.
// =====================================================================================

class C04_01Equality {

    /**
     * `==` compara contenido.
     */
    static void demoEqualsOperator() {
        section('Cadenas: lo que en Java hay que escribir con equals()')

        String a = 'hola'
        String b = new String('hola')

        show('a == b', a == b)
        show('a.is(b)  (identidad)', a.is(b))
        bullet('En Java, `a == b` aquí daría false. Ése es el bug clásico.')

        section('Listas y mapas: compara elemento a elemento')

        show('[1, 2] == [1, 2]', [1, 2] == [1, 2])
        show('[a: 1] == [a: 1]', [a: 1] == [a: 1])
        show('[1, 2].is([1, 2])', [1, 2].is([1, 2]))
        bullet('Dos listas distintas con el mismo contenido son "iguales".')

        section('Y compara entre tipos distintos de colección')

        show('[1, 2] == [1, 2] as LinkedList', [1, 2] == [1, 2] as LinkedList)
        show('[1, 2] == [1, 2] as Set', [1, 2] == ([1, 2] as Set))
        bullet('Lista y Set NO son iguales aunque tengan lo mismo: su equals() difiere.')

        section('`==` nunca lanza con null')

        String nulo = null
        show('nulo == "hola"', nulo == 'hola')
        show('"hola" == nulo', 'hola' == nulo)
        show('nulo == null', nulo == null)
        try {
            show('nulo.equals("hola")', nulo.equals('hola'))
        } catch (Exception e) {
            show('nulo.equals("hola")', e.class.simpleName)
        }
        bullet('`==` comprueba los nulos ANTES de llamar a equals(). Por eso se prefiere.')
    }

    /**
     * `is()` y la caché de enteros.
     */
    static void demoIdentity() {
        section('`is()` es el `==` de Java')

        def lista1 = [1, 2]
        def lista2 = [1, 2]
        def mismaReferencia = lista1

        show('lista1 == lista2', lista1 == lista2)
        show('lista1.is(lista2)', lista1.is(lista2))
        show('lista1.is(mismaReferencia)', lista1.is(mismaReferencia))

        section('La caché de enteros de la JVM')

        Integer pequeñoA = 127
        Integer pequeñoB = 127
        Integer grandeA = 128
        Integer grandeB = 128

        show('127.is(127)', pequeñoA.is(pequeñoB))
        show('128.is(128)', grandeA.is(grandeB))
        bullet('La JVM cachea los Integer de -128 a 127: los de ahí son el mismo objeto.')
        bullet('Por encima, no. De ahí que en Java `==` entre Integer falle a partir de 128.')

        show('127 == 127', pequeñoA == 127)
        show('128 == 128', grandeA == 128)
        bullet('Con `==` de Groovy da igual: compara el valor y siempre acierta.')

        section('Cuándo necesitas `is()` de verdad')

        bullet('Comprobar que un método devuelve EL MISMO objeto y no una copia.')
        bullet('Detectar ciclos en una estructura de datos.')
        bullet('Implementar equals(): `if (this.is(otro)) return true`.')
        bullet('Fuera de eso, casi nunca.')
    }

    /**
     * El operador nave espacial y la comparación ordenada.
     */
    static void demoSpaceship() {
        section('`<=>` es compareTo()')

        show('1 <=> 2', 1 <=> 2)
        show('2 <=> 1', 2 <=> 1)
        show('1 <=> 1', 1 <=> 1)
        show("'a' <=> 'b'", 'a' <=> 'b')
        bullet('Negativo, positivo o cero: el contrato de Comparable de toda la vida.')

        section('Y tolera los nulos')

        show('null <=> 1', null <=> 1)
        show('1 <=> null', 1 <=> null)
        show('null <=> null', null <=> null)
        bullet('null se considera menor que todo. `compareTo` a secas lanzaría.')

        section('Su uso real: ordenar')

        def usuarios = [[n: 'Ana', e: 30], [n: 'Luis', e: 25], [n: 'Eva', e: 35]]
        show('por edad', usuarios.sort(false) { it.e }*.n)
        show('por edad descendente', usuarios.sort(false) { -it.e }*.n)
        show('con <=> explícito', usuarios.sort(false) { x, y -> x.e <=> y.e }*.n)

        section('Ordenar por varios criterios: encadenar con elvis')

        def gente = [[n: 'Ana', dep: 'IT'], [n: 'Beto', dep: 'RRHH'], [n: 'Carlos', dep: 'IT']]
        def ordenados = gente.sort(false) { x, y ->
            // Si el primer criterio empata (devuelve 0, que es "falso"), pasa al segundo.
            (x.dep <=> y.dep) ?: (y.n <=> x.n)
        }
        show('por departamento, y nombre descendente', ordenados.collect { "${it.dep}/${it.n}" })
        bullet('`?:` funciona porque 0 es "falso" en el Groovy Truth (capítulo 05).')
        bullet('Es el idioma estándar para ordenar por varios campos.')

        section('Lo que NO compara')

        try {
            show('[1, 2] <=> [1, 3]', [1, 2] <=> [1, 3])
        } catch (Exception e) {
            show('[1, 2] <=> [1, 3]', e.class.simpleName)
        }
        bullet('Una lista no es Comparable. Para ordenar listas, compara campo a campo.')
    }

    /**
     * `==` con Comparable: la sorpresa de BigDecimal.
     */
    static void demoEqualsVsCompareTo() {
        section('Cuando el objeto es Comparable, `==` usa compareTo()')

        def a = 1.0
        def b = 1.00

        show('1.0 == 1.00', a == b)
        show('1.0.equals(1.00)', a.equals(b))
        bullet('`equals` de BigDecimal compara TAMBIÉN la escala: 1.0 y 1.00 difieren.')
        bullet('`compareTo` compara sólo el valor numérico. Groovy usa éste.')

        section('Por qué esto importa')

        def precios = [1.0, 1.00, 1.000]
        show('lista', precios)
        show('unique()', precios.unique(false))
        show('as Set (usa equals/hashCode)', precios as Set)
        bullet('`unique` usa la comparación de Groovy; el Set, equals(). Distinto resultado.')

        section('La regla')

        bullet('Para comparar dinero o cantidades: `==` o `<=>`, no `equals()`.')
        bullet('Si necesitas que dos BigDecimal con distinta escala sean la MISMA clave,')
        bullet('normaliza con `stripTrailingZeros()` antes de guardarlos.')
        show('1.00.stripTrailingZeros()', 1.00.stripTrailingZeros())
        show('tras normalizar, en un Set', precios.collect { it.stripTrailingZeros() } as Set)
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia los 127 por 1000 en la demo de la caché y mira cómo cambia `is()`.
//  2. Ordena `gente` quitando el `?:` y comprueba que el segundo criterio se ignora.
//  3. Mete 1.0 y 1.00 en un mapa como claves y cuenta cuántas entradas quedan.
//  4. Compara `[1,2] == [1,2] as Set` con `[1,2] as Set == [1,2] as Set`.
