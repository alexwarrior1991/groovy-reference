package com.alejandro.c09maps

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  09.1 · Mapas: el literal, las claves y sus trampas
//
//  QUÉ ES
//    `[a: 1, b: 2]` es un `LinkedHashMap`: mantiene el orden de inserción. La sintaxis
//    del literal parece JavaScript, pero tiene dos reglas que sorprenden: las claves
//    sin comillas son CADENAS literales, y el acceso con punto es un acceso a la clave,
//    no a una propiedad.
//
//  POR QUÉ IMPORTA
//    Los mapas son la estructura de datos de Groovy: configuraciones, JSON, argumentos
//    con nombre, resultados de consultas. Y sus dos trampas —`[k: 1]` con una variable
//    y `mapa.class`— muerden a todo el mundo una vez.
//
//  ERRORES COMUNES
//    · `[variable: v]` creyendo que usa el VALOR de la variable como clave.
//    · `mapa.class` esperando la clase del mapa.
//    · `mapa.get(clave, porDefecto)` sin saber que INSERTA el valor por defecto.
// =====================================================================================

class C09_01Maps {

    /**
     * El literal y el orden.
     */
    static void demoCreating() {
        section('El literal produce un LinkedHashMap')

        def mapa = [nombre: 'Ana', edad: 30]
        show('getClass()', mapa.getClass().simpleName)
        show('mapa', mapa)
        bullet('LinkedHashMap: mantiene el ORDEN DE INSERCIÓN. No es un HashMap.')

        section('Y ese orden se respeta')

        show('[b: 1, a: 2].keySet()', [b: 1, a: 2].keySet())
        bullet('En un HashMap de Java el orden no estaría garantizado.')

        section('El mapa vacío es `[:]`, no `[]`')

        show('[:].getClass()', [:].getClass().simpleName)
        show('[].getClass()', [].getClass().simpleName)
        bullet('`[]` es una lista vacía. Los dos puntos hacen el mapa.')

        section('Otras implementaciones')

        show('as TreeMap (ordenado por clave)', [b: 1, a: 2] as TreeMap)
        show('as HashMap', ([b: 1, a: 2] as HashMap).getClass().simpleName)
        show('new HashMap([a: 1])', new HashMap([a: 1]))
    }

    /**
     * Las claves: la trampa más conocida de Groovy.
     */
    static void demoKeys() {
        section('Una clave sin comillas es una CADENA literal')

        def mapa = [nombre: 'Ana']
        show('[nombre: "Ana"]', mapa)
        show('la clave es', mapa.keySet().first().getClass().simpleName)
        bullet('`nombre` no es una variable: es la cadena "nombre".')

        section('LA TRAMPA: usar una variable como clave')

        def clave = 'dinamica'
        show('[clave: 1]', [clave: 1])
        bullet('Usa la palabra "clave" como clave, NO su valor.')

        show('[(clave): 1]', [(clave): 1])
        bullet('Con PARÉNTESIS sí evalúa la variable. Es la forma correcta.')

        section('Otras formas de poner una clave dinámica')

        def construido = [:]
        construido[clave] = 1
        show('mapa[clave] = 1', construido)
        show('con GString', ["${clave}": 1].keySet().first().getClass().simpleName)
        bullet('¡Ojo! Con GString la clave es un GStringImpl, no un String.')
        bullet('Es la trampa del capítulo 03. Usa `[(clave): …]` o `mapa[clave] = …`.')

        section('Las claves no tienen por qué ser cadenas')

        show('[1: "uno", 2: "dos"]', [1: 'uno', 2: 'dos'])
        show('acceso con [1]', [1: 'uno'][1])
        show('clave lista', [([1, 2]): 'par'])
        bullet('Cualquier objeto con equals/hashCode decente vale como clave.')

        section('Y por eso `mapa.1` no funciona')

        def numerico = [1: 'uno']
        show('numerico[1]', numerico[1])
        bullet('El acceso con punto sólo sirve para claves que son identificadores.')
    }

    /**
     * Acceder, y la trampa de `.class`.
     */
    static void demoAccessing() {
        section('Las tres formas de leer')

        def usuario = [nombre: 'Ana', edad: 30]
        show('usuario.nombre', usuario.nombre)
        show('usuario["nombre"]', usuario['nombre'])
        show('usuario.get("nombre")', usuario.get('nombre'))
        bullet('El acceso con punto es azúcar para el subíndice. Nada de propiedades.')

        section('Una clave que no existe devuelve null')

        show('usuario.apellido', usuario.apellido)
        show('usuario["apellido"]', usuario['apellido'])
        bullet('No lanza. Y como no lanza, una errata en la clave pasa inadvertida.')

        section('LA TRAMPA: `.class` busca la CLAVE "class"')

        show('usuario.class', usuario.class)
        show('usuario.getClass()', usuario.getClass().simpleName)
        bullet('`usuario.class` es `usuario["class"]`, que no existe: null.')
        bullet('Para la clase de un mapa hay que usar `getClass()`.')

        show('con una clave "class" de verdad', [class: 'la mía'].class)

        section('Lo mismo pasa con otras propiedades')

        show('[a: 1].size()  (método)', [a: 1].size())
        show('[a: 1].size  (¿clave o propiedad?)', [a: 1].size)
        bullet('`.size` sin paréntesis busca la clave "size" y devuelve null.')
        bullet('Regla: sobre un mapa, usa SIEMPRE los paréntesis para los métodos.')

        section('get con valor por defecto… MUTA el mapa')

        def config = [host: 'localhost']
        show('config.get("puerto", 8080)', config.get('puerto', 8080))
        show('el mapa ahora', config)
        bullet('Lo ha INSERTADO. Es lo que hace ese `get` de dos argumentos.')
        bullet('Si sólo quieres leer con un defecto, usa el elvis:')
        def otro = [host: 'localhost']
        show('otro.puerto ?: 8080', otro.puerto ?: 8080)
        show('y el mapa sigue', otro)
    }

    /**
     * Modificar.
     */
    static void demoModifying() {
        section('Añadir y cambiar')

        def mapa = [a: 1]
        mapa.b = 2
        mapa['c'] = 3
        mapa.put('d', 4)
        show('tras añadir de cuatro formas', mapa)

        mapa << [e: 5]
        show('tras << [e: 5]', mapa)
        bullet('`<<` MUTA el mapa, igual que en las listas.')

        section('`+` no muta')

        def base = [a: 1]
        show('base + [b: 2]', base + [b: 2])
        show('base sigue siendo', base)

        section('Quitar')

        def paraQuitar = [a: 1, b: 2, c: 3]
        paraQuitar.remove('b')
        show('tras remove("b")', paraQuitar)
        bullet('Aquí `remove` SÍ borra por clave: los mapas no tienen índices.')

        show('subMap(["a","c"])', [a: 1, b: 2, c: 3].subMap(['a', 'c']))
        show('menos unas claves', [a: 1, b: 2, c: 3].findAll { k, v -> k != 'b' })

        section('Fusionar, y quién gana')

        def porDefecto = [host: 'localhost', puerto: 80]
        def usuario = [puerto: 8080]
        show('porDefecto + usuario', porDefecto + usuario)
        show('[*:porDefecto, *:usuario]', [*: porDefecto, *: usuario])
        bullet('El de la derecha pisa. Es el patrón de configuración por capas.')

        section('withDefault: un valor por defecto para lo que no esté')

        def contadores = [:].withDefault { 0 }
        ['a', 'b', 'a'].each { contadores[it] += 1 }
        show('contando con withDefault', contadores)
        bullet('Sin esto haría falta `contadores[it] = (contadores[it] ?: 0) + 1`.')

        section('Pero cuidado: leer una clave la CREA')

        def conDefecto = [:].withDefault { 0 }
        conDefecto.loQueSea
        show('tras leer una clave que no existía', conDefecto)
        show('tamaño', conDefecto.size())
        bullet('Leer inserta. Si sólo quieres consultar, no uses withDefault.')
        bullet('`countBy` suele ser mejor idea para contar:')
        show('countBy', ['a', 'b', 'a'].countBy { it })
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Crea un mapa con `[(clave): 1]` y con `["${clave}": 1]` y compara las clases de
//     las claves.
//  2. Escribe `[a: 1].size` y `[a: 1].size()` y explica los dos resultados.
//  3. Usa `get(clave, porDefecto)` dentro de un bucle sobre claves que no existen y
//     mira cómo crece el mapa.
//  4. Mete un mapa dentro de otro como CLAVE y comprueba que funciona.
