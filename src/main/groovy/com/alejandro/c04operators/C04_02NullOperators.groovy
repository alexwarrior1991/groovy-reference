package com.alejandro.c04operators

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  04.2 · Los operadores que se ocupan de los nulos
//
//  QUÉ ES
//    Groovy no tiene tipos nulables como Kotlin: cualquier referencia puede ser null.
//    Lo que da son cuatro operadores para que eso no llene el código de `if`:
//      a?.b      navegación segura: si `a` es null, la expresión entera vale null
//      a ?: b    elvis: `a` si es "cierto", si no `b`
//      a ?= b    asignación elvis: asigna sólo si `a` es "falso"
//      a?[i]     indexación segura
//
//  POR QUÉ IMPORTA
//    Son la diferencia entre una cadena de accesos legible y tres `if` anidados. Y
//    conviene entender exactamente qué comprueba cada uno: `?.` mira si es NULL, pero
//    `?:` mira el Groovy Truth entero (capítulo 05), que es mucho más ancho.
//
//  ERRORES COMUNES
//    · Creer que `?:` sólo mira el null. Una cadena vacía o un 0 también son "falsos".
//    · Encadenar `?.` por costumbre sobre algo que nunca es null: sólo añade ruido.
//    · Usar `?.` en una llamada cuyo resultado se asigna a un primitivo.
// =====================================================================================

class C04_02NullOperators {

    /**
     * Navegación segura.
     */
    static void demoSafeNavigation() {
        section('`?.` devuelve null en vez de lanzar')

        String nulo = null
        show('nulo?.length()', nulo?.length())
        show('nulo?.toUpperCase()?.trim()', nulo?.toUpperCase()?.trim())
        bullet('Toda la cadena se corta en cuanto uno es null. No lanza en ningún punto.')

        section('Sin `?.`, lo normal')

        try {
            show('nulo.length()', nulo.length())
        } catch (Exception e) {
            show('nulo.length()', e.class.simpleName)
        }

        section('Su uso real: navegar una estructura')

        def conPedido = [cliente: [direccion: [ciudad: 'Madrid']]]
        def sinDireccion = [cliente: [:]]

        show('con datos', conPedido.cliente?.direccion?.ciudad)
        show('sin dirección', sinDireccion.cliente?.direccion?.ciudad)
        bullet('En Java serían tres comprobaciones anidadas.')

        section('Indexación segura: `?[ ]`')

        List<String> lista = null
        show('lista?[0]', lista?[0])
        show('["a","b"]?[0]', ['a', 'b']?[0])
        bullet('Añadido en Groovy 3. Antes había que escribir `lista?.getAt(0)`.')

        section('Cuidado: `?.` devuelve null, y null no cabe en un primitivo')

        try {
            int longitud = nulo?.length()
            show('nunca llega', longitud)
        } catch (Exception e) {
            show('int x = nulo?.length()', e.class.simpleName)
        }
        bullet('Combina siempre `?.` con `?:` cuando el destino sea un primitivo:')
        int seguro = nulo?.length() ?: 0
        show('int x = nulo?.length() ?: 0', seguro)

        section('Cuándo NO usar `?.`')

        bullet('Si el valor no puede ser null, `?.` miente al que lee el código.')
        bullet('Si lo que quieres es enterarte de que era null, deja que lance.')
        bullet('Una cadena de cinco `?.` suele significar que el modelo está mal.')
    }

    /**
     * Elvis.
     */
    static void demoElvis() {
        section('`?:` da un valor por defecto')

        String nulo = null
        show('nulo ?: "por defecto"', nulo ?: 'por defecto')
        show('"valor" ?: "por defecto"', 'valor' ?: 'por defecto')

        section('Pero NO comprueba el null: comprueba el Groovy Truth')

        show('"" ?: "por defecto"', '' ?: 'por defecto')
        show('0 ?: 99', 0 ?: 99)
        show('[] ?: "vacía"', [] ?: 'vacía')
        show('false ?: "falso"', false ?: 'falso')
        bullet('Cadena vacía, cero, lista vacía y false también disparan el elvis.')
        bullet('ESTO es lo que más sorprende. Capítulo 05.')

        section('Cuando de verdad sólo quieres comprobar el null')

        Integer cantidad = 0
        show('cantidad ?: 10  (mal)', cantidad ?: 10)
        show('cantidad != null ? cantidad : 10', cantidad != null ? cantidad : 10)
        bullet('Con un 0 legítimo, el elvis te lo cambia por el valor por defecto.')
        bullet('Es un bug clásico en contadores, precios y cantidades.')

        section('El idioma completo: `?.` y `?:` juntos')

        def usuario = [nombre: null]
        show('usuario.nombre?.trim() ?: "anónimo"', usuario.nombre?.trim() ?: 'anónimo')

        section('`?=`: asigna sólo si no había valor')

        def config = [:]
        config.tiempo = null
        config.tiempo ?= 30
        show('tras ?= sobre null', config.tiempo)

        config.tiempo ?= 60
        show('tras ?= sobre un valor existente', config.tiempo)
        bullet('`x ?= v` es exactamente `x = x ?: v`, y hereda su misma trampa del 0.')
    }

    /**
     * Comparar y agrupar con nulos.
     */
    static void demoNullInCollections() {
        section('Filtrar los nulos')

        def conNulos = [1, null, 2, null, 3]
        show('findAll { it != null }', conNulos.findAll { it != null })
        show('grep()', conNulos.grep())
        bullet('`grep()` sin argumentos filtra por Groovy Truth: quita también los ceros.')
        show('[1, 0, null].grep()', [1, 0, null].grep())
        show('[1, 0, null].findAll { it != null }', [1, 0, null].findAll { it != null })

        section('`*.` se salta la llamada sobre los nulos')

        show("['a', null, 'ccc']*.size()", ['a', null, 'ccc']*.size())
        bullet('El spread aplica `?.`, no `.`: donde hay null, devuelve null.')

        section('Ordenar con nulos dentro')

        show("['b', null, 'a'].sort()", ['b', null, 'a'].sort(false))
        bullet('null va primero, porque `<=>` lo considera el más pequeño.')

        section('Sumar con nulos: eso sí lanza')

        try {
            show('[1, null, 3].sum()', [1, null, 3].sum())
        } catch (Exception e) {
            show('[1, null, 3].sum()', e.class.simpleName)
        }
        show('[1, null, 3].grep().sum()', [1, null, 3].grep().sum())
        bullet('Limpia antes de agregar. Es la regla general con datos de fuera.')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `cantidad = 0` por `cantidad = null` y mira cómo las dos formas coinciden.
//  2. Quita un `?.` de la cadena de `conPedido` y prueba con `sinDireccion`.
//  3. Prueba `def x = ''; x ?= 'otro'` y explica el resultado con lo del capítulo 05.
//  4. Sustituye `grep()` por `grep { it != null }` y compara con `findAll`.
