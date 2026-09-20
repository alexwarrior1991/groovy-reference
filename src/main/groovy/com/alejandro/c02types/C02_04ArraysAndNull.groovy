package com.alejandro.c02types

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  02.4 · Arrays frente a listas, y qué hace Groovy con null
//
//  QUÉ ES
//    Groovy tiene los arrays de Java, pero su colección natural es `java.util.ArrayList`:
//    `[1, 2, 3]` es una lista, no un array. Y `null` se comporta distinto que en Java en
//    tres sitios concretos: al asignarlo a un primitivo, al compararlo y al ordenarlo.
//
//  POR QUÉ IMPORTA
//    El literal `[...]` es lo primero que se escribe en Groovy, y conviene saber desde
//    el minuto uno que NO es un array. Y la semántica de `null` explica por qué aquí
//    casi nunca se ve un NullPointerException donde en Java sí lo habría.
//
//  ERRORES COMUNES
//    · Escribir `int[] x = [1, 2, 3]` esperando que falle. No falla: coerciona.
//    · Imprimir un array y encontrarse `[I@5ca881b5`.
//    · Declarar `int x` y asignarle `null`: eso sí lanza.
// =====================================================================================

class C02_04ArraysAndNull {

    /**
     * El literal de corchetes es una lista, no un array.
     */
    static void demoArraysVsLists() {
        section('`[1, 2, 3]` es una ArrayList')

        def lista = [1, 2, 3]
        show('[1,2,3].class', lista.class.name)
        bullet('En Java esto sería un array. En Groovy, una lista que crece.')

        section('Para un array de verdad, hay que pedirlo')

        int[] array = [1, 2, 3]
        show('int[] x = [1,2,3] → clase', array.class.simpleName)
        def otroArray = [1, 2, 3] as int[]
        show('[1,2,3] as int[] → clase', otroArray.class.simpleName)
        show('new int[3]', new int[3])

        section('Imprimir un array: el GDK lo arregla, `toString()` no')

        // Éste es el motivo de que el helper `show` del repositorio trate los arrays
        // aparte: sin eso, aquí saldría algo como [I@5ca881b5.
        show('show(array)', array)
        show('array.toString()', array.toString())
        show('Arrays.toString(array)', Arrays.toString(array))
        show('(array as List)', array as List)
        bullet('`toListString()` del GDK es para COLECCIONES: sobre un `int[]` no existe.')
        bullet('Para un array de primitivos: `Arrays.toString()` o conviértelo a lista.')

        section('Qué se puede hacer con cada uno')

        show('lista + [4]', lista + [4])
        show('lista.size()', lista.size())
        show('array.length', array.length)
        show('array.size()', array.size())
        bullet('El GDK le da `size()`, `each` y `collect` también a los arrays.')

        show('array.collect { it * 2 }', array.collect { it * 2 })
        show('tipo del collect', array.collect { it * 2 }.class.simpleName)
        bullet('Pero `collect` sobre un array devuelve una LISTA, no un array.')

        section('Cuándo usar un array en Groovy')

        bullet('Cuando una API de Java lo exige.')
        bullet('Cuando el tamaño es fijo y el rendimiento importa de verdad.')
        bullet('En todo lo demás: lista. Es lo idiomático y tiene el GDK entero.')
    }

    /**
     * null en Groovy.
     */
    static void demoNull() {
        section('`null` es un valor normal para cualquier tipo de objeto')

        String texto = null
        show('String x = null', texto)
        show('texto?.length()', texto?.length())
        bullet('El `?.` devuelve null en vez de lanzar. Capítulo 04.')

        section('Pero un primitivo declarado NO admite null')

        try {
            int numero = null
            show('nunca llega', numero)
        } catch (Exception e) {
            show('int x = null', e.class.simpleName)
        }
        bullet('Con `def` o `Integer` sí se puede: el problema es el primitivo.')

        Integer conObjeto = null
        show('Integer x = null', conObjeto)

        section('null es "falso" para el Groovy Truth (capítulo 05)')

        show('if (null)', null ? 'cierto' : 'falso')
        show('null ?: "por defecto"', null ?: 'por defecto')

        section('Comparar con null nunca lanza')

        show('null == null', null == null)
        show('null == "algo"', null == 'algo')
        show('"algo" == null', 'algo' == null)
        bullet('El `==` de Groovy comprueba los nulos ANTES de llamar a equals().')
        bullet('Por eso `texto.equals("x")` puede lanzar y `texto == "x"` no.')

        section('Al ordenar, null va primero')

        def conNulos = ['b', null, 'a']
        show('sort()', conNulos.sort(false))
        bullet('`null` se considera menor que cualquier valor.')

        section('Y las operaciones de colección se lo saltan o no, según cuál')

        def lista = [1, null, 3]
        show('lista.findAll { it }', lista.findAll { it })
        show('lista.grep()', lista.grep())
        show('lista.count { it != null }', lista.count { it != null })
        bullet('`grep()` sin argumentos filtra por Groovy Truth: quita nulos y ceros.')
        show('[1, 0, null, 2].grep()', [1, 0, null, 2].grep())
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `int[] array = [1, 2, 3]` por `int[] array = ['a', 'b']` y lee el error.
//  2. Haz `println array` a secas y compáralo con `show('x', array)`.
//  3. Prueba `[1, null, 3].sum()` y mira qué pasa; después `[1, null, 3].grep().sum()`.
//  4. Ordena `['b', null, 'a']` con `sort { a, b -> a <=> b }` y compara con `sort()`.
