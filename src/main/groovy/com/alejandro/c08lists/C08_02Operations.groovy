package com.alejandro.c08lists

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  08.2 · Las operaciones: collect, findAll, inject y compañía
//
//  QUÉ ES
//    El GDK convierte cualquier colección en algo parecido a los Stream de Java, pero
//    sin tener que abrir y cerrar el flujo: los métodos están directamente en la lista.
//
//  POR QUÉ IMPORTA
//    Sustituyen al 80% de los bucles que escribirías. Y el nombre dice qué hace: al
//    leer `collect` ya sabes que sale una lista del mismo tamaño, y al leer `findAll`,
//    que sale una más corta o igual.
//
//  ERRORES COMUNES
//    · Encadenar diez operaciones sobre una lista grande creando nueve intermedias.
//    · Usar `collect` cuando el resultado se descarta: eso es `each`.
//    · Llamar a `inject` sobre una lista vacía sin valor inicial: lanza.
// =====================================================================================

class C08_02Operations {

    /**
     * Transformar.
     */
    static void demoTransforming() {
        section('collect: uno a uno, mismo tamaño')

        def palabras = ['groovy', 'java', 'kotlin']
        show('collect { it.size() }', palabras.collect { it.size() })
        show('collect con índice', palabras.withIndex().collect { p, i -> "$i:$p" })
        show('*.size() (equivalente)', palabras*.size())

        section('collectMany: uno a varios, y aplana')

        def pedidos = [[lineas: ['a', 'b']], [lineas: ['c']]]
        show('collectMany { it.lineas }', pedidos.collectMany { it.lineas })
        show('lo mismo con collect+flatten', pedidos.collect { it.lineas }.flatten())
        bullet('`collectMany` es el `flatMap` de otros lenguajes.')

        section('flatten: aplanar sin transformar')

        show('[1, [2, [3]]].flatten()', [1, [2, [3]]].flatten())
        bullet('Aplana a CUALQUIER profundidad, no sólo un nivel.')

        section('collectEntries: de lista a mapa')

        show('collectEntries', palabras.collectEntries { [it, it.size()] })
        show('con clave y valor', palabras.collectEntries { [(it.toUpperCase()): it.size()] })

        section('collate y chop: trocear')

        show('(1..5).collate(2)', (1..5).collate(2))
        show('(1..5).collate(2, false)', (1..5).collate(2, false))
        show('(1..5).chop(2, 3)', (1..5).chop(2, 3))
        bullet('`collate(n, false)` descarta el último trozo si no está completo.')

        section('transpose: girar una matriz')

        show('[[1,2],[3,4]].transpose()', [[1, 2], [3, 4]].transpose())
        show('emparejar dos listas', [['a', 'b'], [1, 2]].transpose())
        bullet('Emparejar dos listas y hacerlas mapa es el idioma para leer un CSV:')
        show('… y a mapa', [['a', 'b'], [1, 2]].transpose().collectEntries())
    }

    /**
     * Filtrar y buscar.
     */
    static void demoFiltering() {
        section('findAll: los que cumplan')

        def numeros = [1, 2, 3, 4, 5, 6]
        show('findAll { it % 2 == 0 }', numeros.findAll { it % 2 == 0 })
        show('grep { it > 3 }', numeros.grep { it > 3 })
        show('grep(3..5)  (con isCase)', numeros.grep(3..5))
        bullet('`grep` acepta cualquier cosa con `isCase`: rango, clase, regex, closure.')
        show('grep(Integer)', [1, 'a', 2].grep(Integer))

        section('find: el primero, y para en cuanto lo encuentra')

        show('find { it > 3 }', numeros.find { it > 3 })
        show('find que no existe', numeros.find { it > 99 })
        show('findResult', numeros.findResult { it > 3 ? "encontrado $it" : null })
        bullet('`findResult` devuelve el primer resultado NO nulo del closure.')

        section('Índices')

        show('findIndexOf { it > 3 }', numeros.findIndexOf { it > 3 })
        show('indexOf(4)', numeros.indexOf(4))
        show('findIndexValues { par }', numeros.findIndexValues { it % 2 == 0 })

        section('Preguntas de sí o no')

        show('any { it > 5 }', numeros.any { it > 5 })
        show('every { it > 0 }', numeros.every { it > 0 })
        show('count { par }', numeros.count { it % 2 == 0 })
        show('count(3)', numeros.count(3))
        bullet('`any` y `every` cortan en cuanto saben la respuesta.')

        section('Partir en dos')

        show('split { par }', numeros.split { it % 2 == 0 })
        bullet('Devuelve [los que cumplen, los que no]. En una sola pasada.')

        section('takeWhile y dropWhile: hasta que deje de cumplirse')

        def serie = [1, 2, 3, 10, 1, 2]
        show('takeWhile { it < 5 }', serie.takeWhile { it < 5 })
        show('dropWhile { it < 5 }', serie.dropWhile { it < 5 })
        bullet('Paran en el PRIMERO que falla, no filtran toda la lista.')
    }

    /**
     * Agregar: inject y los atajos.
     */
    static void demoAggregating() {
        section('inject: el fold de toda la vida')

        show('inject { a, b -> a + b }', [1, 2, 3, 4].inject { a, b -> a + b })
        show('con valor inicial', [1, 2, 3, 4].inject(100) { a, b -> a + b })
        bullet('El acumulador es el primer parámetro; el elemento, el segundo.')

        section('LA TRAMPA: sin valor inicial y con la lista vacía, lanza')

        try {
            show('[].inject { a, b -> a + b }', [].inject { a, b -> a + b })
        } catch (Exception e) {
            show('[].inject { }', e.class.simpleName)
        }
        show('[].inject(0) { a, b -> a + b }', [].inject(0) { a, b -> a + b })
        bullet('Pon SIEMPRE el valor inicial si la lista puede venir vacía.')

        section('Los atajos que ya existen')

        def numeros = [3, 1, 4, 1, 5]
        show('sum()', numeros.sum())
        show('min() / max()', [numeros.min(), numeros.max()])
        show('average()', numeros.average())
        show('sum { it * 2 }', numeros.sum { it * 2 })
        show('max { closure }', ['a', 'bbb', 'cc'].max { it.size() })

        section('Y el otro caso límite: sum() de una lista vacía')

        show('[].sum()', [].sum())
        bullet('Devuelve null, no 0. Si lo sumas a algo, NullPointerException.')
        show('[].sum(0)', [].sum(0))
        bullet('Con valor inicial, 0. Es la forma segura.')

        section('inject para algo que no sea sumar')

        def palabras = ['groovy', 'es', 'compacto']
        show('la más larga', palabras.inject { a, b -> a.size() >= b.size() ? a : b })
        show('construir un mapa', palabras.inject([:]) { Map acc, String p -> acc + [(p): p.size()] })
        bullet('Cuando `collect`, `findAll` y `sum` no llegan, `inject` siempre puede.')
    }

    /**
     * Agrupar y ordenar.
     */
    static void demoGroupingAndSorting() {
        def usuarios = [
            [nombre: 'Ana', dep: 'IT', edad: 30],
            [nombre: 'Luis', dep: 'RRHH', edad: 25],
            [nombre: 'Eva', dep: 'IT', edad: 35],
            [nombre: 'Marta', dep: 'RRHH', edad: 28],
        ]

        section('groupBy: un mapa de listas')

        show('groupBy { it.dep }', usuarios.groupBy { it.dep }.collectEntries { k, v -> [k, v*.nombre] })

        section('countBy: un mapa de cuentas')

        show('countBy { it.dep }', usuarios.countBy { it.dep })

        section('groupBy con varios niveles')

        def porDepYDecada = usuarios.groupBy({ it.dep }, { it.edad >= 30 ? '30+' : '20+' })
        show('dos niveles', porDepYDecada.collectEntries { k, v ->
            [k, v.collectEntries { k2, v2 -> [k2, v2*.nombre] }]
        })

        section('El idioma completo: agrupar y agregar')

        def edadMediaPorDep = usuarios
            .groupBy { it.dep }
            .collectEntries { dep, gente -> [dep, gente*.edad.average()] }
        show('edad media por departamento', edadMediaPorDep)

        section('Ordenar')

        show('sort(false) { it.edad }', usuarios.sort(false) { it.edad }*.nombre)
        show('descendente', usuarios.sort(false) { -it.edad }*.nombre)
        show('por varios criterios', usuarios.sort(false) { a, b ->
            (a.dep <=> b.dep) ?: (a.edad <=> b.edad)
        }*.nombre)
        bullet('`(x <=> y) ?: (a <=> b)` encadena criterios. Capítulo 04.')

        section('min y max con criterio')

        show('el más joven', usuarios.min { it.edad }.nombre)
        show('el más mayor', usuarios.max { it.edad }.nombre)

        section('unique con criterio')

        show('un usuario por departamento', usuarios.toUnique { it.dep }*.nombre)
        bullet('Se queda con el PRIMERO de cada grupo.')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Sustituye un `inject` por un bucle `for` y decide cuál se lee mejor.
//  2. Llama a `[].sum()` y súmale 1: mira dónde salta el error.
//  3. Cambia `collect { it.lineas }.flatten()` por `collectMany` en una lista con
//     elementos nulos y compara.
//  4. Agrupa `usuarios` por departamento y quédate sólo con el nombre del más mayor
//     de cada uno.
