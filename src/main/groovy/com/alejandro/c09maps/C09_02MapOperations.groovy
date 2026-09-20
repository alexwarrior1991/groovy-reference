package com.alejandro.c09maps

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  09.2 · Operar sobre mapas
//
//  QUÉ ES
//    Los mismos métodos que en las listas —`each`, `collect`, `findAll`, `groupBy`,
//    `inject`— pero aplicados a parejas clave/valor. La clave está en cuántos
//    parámetros declara tu closure: con uno recibes la `Map.Entry`, con dos la clave y
//    el valor por separado.
//
//  POR QUÉ IMPORTA
//    Casi todo lo que llega de fuera (JSON, una consulta, una configuración) es un
//    mapa o una lista de mapas. Saber transformarlos sin bucles es media batalla.
//
//  ERRORES COMUNES
//    · `collect` sobre un mapa devuelve una LISTA. Para seguir con un mapa,
//      `collectEntries`.
//    · Olvidar que `sort` sobre un mapa devuelve un mapa nuevo ordenado, no muta.
//    · Perder el orden al convertir a `HashMap` por el camino.
// =====================================================================================

class C09_02MapOperations {

    private static final Map<String, Integer> VENTAS = [
        madrid: 120, barcelona: 95, valencia: 60, sevilla: 45, bilbao: 30,
    ]

    /**
     * Recorrer.
     */
    static void demoIterating() {
        section('each con DOS parámetros: clave y valor')

        List<String> conDos = []
        [a: 1, b: 2].each { clave, valor -> conDos << "$clave=$valor" }
        show('resultado', conDos)

        section('each con UNO: la entrada entera')

        List<String> conUno = []
        [a: 1, b: 2].each { entrada -> conUno << "${entrada.key}→${entrada.value}" }
        show('resultado', conUno)
        show('el tipo que llega', tipoDeLaEntrada())
        bullet('El GDK mira cuántos parámetros declaras y te pasa lo que pides.')

        section('Con el índice')

        List<String> conIndice = []
        [a: 1, b: 2].eachWithIndex { entrada, i -> conIndice << "$i:${entrada.key}" }
        show('eachWithIndex', conIndice)

        section('Sólo las claves o sólo los valores')

        show('keySet()', VENTAS.keySet())
        show('values()', VENTAS.values())
        show('entrySet().size()', VENTAS.entrySet().size())
    }

    private static String tipoDeLaEntrada() {
        String tipo = ''
        [a: 1].each { entrada -> tipo = entrada.getClass().interfaces*.simpleName.find { it.contains('Entry') } ?: 'Entry' }
        tipo
    }

    /**
     * Transformar.
     */
    static void demoTransforming() {
        section('collect sobre un mapa devuelve una LISTA')

        show('collect { k, v -> "$k: $v" }', [a: 1, b: 2].collect { k, v -> "$k: $v" })
        show('su clase', [a: 1].collect { k, v -> k }.getClass().simpleName)
        bullet('Es lo lógico: has pedido una transformación, no un mapa.')

        section('collectEntries: para seguir teniendo un mapa')

        show('claves en mayúsculas', VENTAS.collectEntries { k, v -> [k.toUpperCase(), v] })
        show('valores por mil', VENTAS.collectEntries { k, v -> [k, v * 1000] })
        show('dándole la vuelta', [a: 1, b: 2].collectEntries { k, v -> [v, k] })
        bullet('El closure devuelve una lista de dos: [clave, valor].')

        section('De lista a mapa y al revés')

        show('lista → mapa', ['a', 'bb'].collectEntries { [it, it.size()] })
        show('mapa → lista de pares', [a: 1, b: 2].collect { k, v -> [k, v] })
        show('con toSpreadMap', ['a', 1, 'b', 2].toSpreadMap())

        section('Aplanar un mapa anidado')

        def anidado = [servidor: [host: 'localhost', puerto: 80], debug: true]
        show('anidado', anidado)
        show('aplanado', aplanar(anidado))
        bullet('Convertir a claves con punto es lo que hace un fichero .properties.')
    }

    private static Map<String, Object> aplanar(Map<String, Object> mapa, String prefijo = '') {
        mapa.inject([:] as Map<String, Object>) { Map<String, Object> acc, entrada ->
            String clave = prefijo ? "$prefijo.${entrada.key}" : entrada.key.toString()
            entrada.value instanceof Map
                ? acc + aplanar(entrada.value as Map<String, Object>, clave)
                : acc + [(clave): entrada.value]
        }
    }

    /**
     * Filtrar, buscar y ordenar.
     */
    static void demoFilteringAndSorting() {
        section('findAll sobre un mapa devuelve un MAPA')

        show('ventas > 50', VENTAS.findAll { k, v -> v > 50 })
        show('su clase', VENTAS.findAll { k, v -> v > 50 }.getClass().simpleName)
        bullet('A diferencia de `collect`, aquí sí conserva la forma de mapa.')

        section('find devuelve una ENTRADA')

        def encontrada = VENTAS.find { k, v -> v < 50 }
        show('find { v < 50 }', "${encontrada.key} = ${encontrada.value}")

        section('Preguntas')

        show('any { v > 100 }', VENTAS.any { k, v -> v > 100 })
        show('every { v > 10 }', VENTAS.every { k, v -> v > 10 })
        show('count { v > 50 }', VENTAS.count { k, v -> v > 50 })

        section('Agregar')

        show('sum de los valores', VENTAS.values().sum())
        show('la ciudad con más ventas', VENTAS.max { it.value }.key)
        show('media', VENTAS.values().average())
        show('inject', VENTAS.inject(0) { acc, entrada -> acc + entrada.value })

        section('Ordenar: devuelve un mapa NUEVO')

        show('por clave', VENTAS.sort { it.key })
        show('por valor descendente', VENTAS.sort { -it.value })
        show('el original sigue igual', VENTAS.keySet() as List)
        bullet('`sort` sobre un mapa NO muta, al contrario que sobre una lista.')

        section('El podio: ordenar y quedarse con los primeros')

        show('top 3', VENTAS.sort { -it.value }.take(3))
        bullet('`take` sobre un mapa ordenado conserva el orden. Muy útil.')

        section('groupBy sobre un mapa')

        show('por tamaño de venta', VENTAS.groupBy { k, v -> v >= 60 ? 'grande' : 'pequeña' }
            .collectEntries { k, v -> [k, v.keySet() as List] })
    }

    /**
     * Mapas anidados y datos de fuera.
     */
    static void demoNested() {
        def respuesta = [
            estado: 'ok',
            datos  : [
                usuarios: [
                    [nombre: 'Ana', roles: ['admin', 'editor']],
                    [nombre: 'Luis', roles: ['lector']],
                ],
            ],
        ]

        section('Navegar con seguridad')

        show('respuesta.datos.usuarios[0].nombre', respuesta.datos.usuarios[0].nombre)
        show('una ruta que no existe', respuesta.datos?.pedidos?.total)
        bullet('El `?.` evita el NullPointerException a media ruta. Capítulo 04.')

        section('Extraer con spread')

        show('todos los nombres', respuesta.datos.usuarios*.nombre)
        show('todos los roles, aplanados', respuesta.datos.usuarios*.roles.flatten().unique())

        section('Transformar la estructura entera')

        def porNombre = respuesta.datos.usuarios.collectEntries { [it.nombre, it.roles] }
        show('indexado por nombre', porNombre)
        bullet('Convertir una lista en un mapa indexado es LA operación más útil')
        bullet('cuando vas a consultar muchas veces por una clave.')

        section('Y al revés')

        show('vuelta a lista', porNombre.collect { nombre, roles -> [nombre: nombre, roles: roles] })

        section('Buscar en profundidad')

        show('¿quién es admin?', respuesta.datos.usuarios.findAll { 'admin' in it.roles }*.nombre)
        show('cuántos roles en total', respuesta.datos.usuarios.sum { it.roles.size() })
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `collectEntries` por `collect` y mira qué tipo sale.
//  2. Ordena VENTAS por valor y conviértelo a HashMap: comprueba que pierdes el orden.
//  3. Escribe `aplanar` con un bucle en vez de con `inject` y compara.
//  4. Invierte un mapa con `collectEntries { k, v -> [v, k] }` cuando hay valores
//     repetidos, y explica qué pasa.
