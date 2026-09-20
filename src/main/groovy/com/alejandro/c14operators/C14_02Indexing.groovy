package com.alejandro.c14operators

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  14.2 · getAt, putAt, call, asType, next y iterator
//
//  QUÉ ES
//    El resto de los operadores que se pueden dar a una clase propia: el subíndice, la
//    llamada, la conversión, el recorrido y la secuencia.
//
//  POR QUÉ IMPORTA
//    Son los que convierten una clase en algo que "se comporta como el lenguaje":
//    indexable como una lista, invocable como un closure, recorrible en un `for`.
//
//  ERRORES COMUNES
//    · Implementar `getAt` y olvidar los índices negativos y los rangos.
//    · `call()` en algo que no es conceptualmente una función.
//    · `asType` haciendo conversiones sorprendentes.
// =====================================================================================

class C14_02Indexing {

    /**
     * El subíndice: getAt y putAt.
     */
    static void demoSubscript() {
        section('getAt: acceder con [ ]')

        def matriz = new Matriz(2, 3)
        matriz[0, 0] = 1
        matriz[1, 2] = 6

        show('matriz[0, 0]', matriz[0, 0])
        show('matriz[1, 2]', matriz[1, 2])
        show('matriz[0, 1] (sin asignar)', matriz[0, 1])
        bullet('`m[a, b]` llama a `getAt(a, b)`; `m[a, b] = v`, a `putAt`.')

        section('Con un solo índice: una fila entera')

        show('matriz[1]', matriz[1])

        section('La rejilla completa')

        matriz.imprimir()

        section('Soportar rangos e índices negativos')

        def serie = new Serie([10, 20, 30, 40, 50])
        show('serie[0]', serie[0])
        show('serie[-1]', serie[-1])
        show('serie[1..3]', serie[1..3])
        bullet('Aceptar `Object` en `getAt` y mirar el tipo es lo que hace que una')
        bullet('clase propia se comporte como una lista de verdad.')

        section('Y el Groovy Truth y el tamaño, de paso')

        show('serie.size()', serie.size())
        show('if (serie)', serie ? 'tiene datos' : 'vacía')
        show('if (new Serie([]))', new Serie([]) ? 'tiene datos' : 'vacía')
    }

    static class Matriz {
        private final List<List<Integer>> filas

        Matriz(int alto, int ancho) {
            filas = (1..alto).collect { (1..ancho).collect { 0 } }
        }

        int getAt(int fila, int columna) { filas[fila][columna] }

        void putAt(List<Integer> posicion, int valor) {
            filas[posicion[0]][posicion[1]] = valor
        }

        List<Integer> getAt(int fila) { filas[fila].asImmutable() }

        void imprimir() {
            filas.each { fila -> bullet(fila.collect { it.toString().padLeft(3) }.join(' ')) }
        }
    }

    static class Serie {
        private final List<Integer> valores

        Serie(List<Integer> valores) { this.valores = valores.asImmutable() }

        /** Un `getAt` que entiende enteros, negativos y rangos. */
        Object getAt(Object indice) {
            switch (indice) {
                case Integer -> valores[(int) indice]
                case Range -> new Serie(valores[(Range) indice])
                default -> throw new IllegalArgumentException("índice no soportado: $indice")
            }
        }

        int size() { valores.size() }

        boolean asBoolean() { !valores.isEmpty() }

        @Override
        String toString() { valores.toString() }
    }

    /**
     * call: hacer que un objeto sea invocable.
     */
    static void demoCall() {
        section('Definiendo `call`, el objeto se usa como una función')

        def porcentaje = new Descuento(10)
        show('porcentaje(100)', porcentaje(100))
        show('porcentaje.call(100)', porcentaje.call(100))
        bullet('`obj(args)` es `obj.call(args)`. Es el mismo operador que usan los')
        bullet('closures (capítulo 06).')

        section('Pero OJO: no encaja solo donde se espera un Closure')

        try {
            show('collect(porcentaje)', [100, 200, 50].collect(porcentaje))
        } catch (Exception e) {
            show('collect(porcentaje)', e.class.simpleName)
        }
        bullet('`collect` pide un `Closure` concreto, y tener `call` no convierte')
        bullet('tu objeto en uno. La coerción SAM (capítulo 02) es para INTERFACES.')

        section('La forma de usarlo ahí: el method pointer')

        show('collect(porcentaje.&call)', [100, 200, 50].collect(porcentaje.&call))
        show('o un closure que lo llame', [100, 200, 50].collect { porcentaje(it) })
        bullet('`obj.&call` produce un Closure de verdad (capítulo 06).')

        section('Cuándo tiene sentido')

        bullet('Cuando el objeto ES conceptualmente una función con configuración:')
        bullet('un descuento, un validador, una política, una estrategia.')
        bullet('Si el objeto tiene cinco métodos más, `call` confunde.')

        section('Comparado con un closure currificado')

        def conClosure = { int pct, int cantidad -> cantidad - (cantidad * pct / 100) }.curry(10)
        show('con closure', conClosure(100))
        show('con objeto', porcentaje(100))
        bullet('El objeto gana cuando necesita estado, validación o un toString útil.')
        show('su toString', porcentaje.toString())
    }

    static class Descuento {
        final int porcentaje

        Descuento(int porcentaje) {
            if (!(porcentaje in 0..100)) throw new IllegalArgumentException('0..100')
            this.porcentaje = porcentaje
        }

        BigDecimal call(int cantidad) { cantidad - (cantidad * porcentaje / 100) }

        @Override
        String toString() { "Descuento del $porcentaje%" }
    }

    /**
     * asType, iterator y next.
     */
    static void demoOthers() {
        section('asType: controlar el `as`')

        def temp = new Temperatura(25.0)
        show('temp as String', temp as String)
        show('temp as BigDecimal', temp as BigDecimal)
        show('temp as Map', temp as Map)
        bullet('`x as T` llama a `x.asType(T)`. Si no lo defines, Groovy improvisa.')

        section('iterator: recorrer con for y each')

        def semana = new Semana()
        show('con for', recorrer(semana))
        show('con collect', semana.collect { it })
        show('con findAll', semana.findAll { it.startsWith('S') })
        bullet('Definiendo `iterator()`, toda la API de colecciones del GDK funciona')
        bullet('sobre tu clase sin heredar de nada.')

        section('next y previous: habilitar ++ , -- y los rangos')

        def v = new Version(1, 4)
        show('v', v)
        show('v.next()', v.next())
        show('++v', ++v)
        bullet('`++` es `next()`. Fíjate en que reasigna la variable: el objeto')
        bullet('puede seguir siendo inmutable.')

        show('rango de versiones', (new Version(1, 1)..new Version(1, 4))*.toString())
        bullet('Con `next`, `previous` y `compareTo` ya se pueden hacer rangos')
        bullet('(capítulo 09).')

        section('La tabla completa de lo que se puede dar a una clase')

        bullet('plus minus multiply div mod power     aritmética')
        bullet('negative positive                     signo unario')
        bullet('compareTo                             < > <= >= y sort')
        bullet('equals                                ==')
        bullet('getAt putAt                           subíndice [ ]')
        bullet('call                                  obj(args)')
        bullet('leftShift rightShift                  << y >>')
        bullet('next previous                         ++ -- y rangos')
        bullet('iterator                              for, each, collect…')
        bullet('asType                                as')
        bullet('asBoolean                             el Groovy Truth')
        bullet('isCase                                switch e in')
        bullet('getProperty setProperty               acceso con punto (capítulo 16)')
        bullet('methodMissing propertyMissing         lo que no existe (capítulo 16)')
    }

    private static List<String> recorrer(Semana semana) {
        List<String> salida = []
        for (dia in semana) salida << dia
        salida
    }

    static class Temperatura {
        final BigDecimal grados

        Temperatura(BigDecimal grados) { this.grados = grados }

        Object asType(Class destino) {
            switch (destino) {
                // `.toString()` importa: sin él esto devuelve un GString, y el
                // `as String` de quien llama falla al intentar castearlo.
                case String -> "$grados °C".toString()
                case BigDecimal -> grados
                case Map -> [grados: grados, fahrenheit: grados * 9 / 5 + 32]
                default -> throw new IllegalArgumentException("no sé convertir a $destino")
            }
        }
    }

    static class Semana {
        private static final List<String> DIAS =
            ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo']

        Iterator<String> iterator() { DIAS.iterator() }
    }

    static class Version implements Comparable<Version> {
        final int mayor
        final int menor

        Version(int mayor, int menor) { this.mayor = mayor; this.menor = menor }

        Version next() { new Version(mayor, menor + 1) }

        Version previous() { menor > 0 ? new Version(mayor, menor - 1) : new Version(mayor - 1, 9) }

        @Override
        int compareTo(Version otra) { (mayor <=> otra.mayor) ?: (menor <=> otra.menor) }

        @Override
        String toString() { "$mayor.$menor" }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade soporte para índices negativos en `Serie.getAt`.
//  2. Quita `iterator()` de `Semana` y mira qué deja de funcionar.
//  3. Define `asType(Class)` para convertir `Version` a `List` y prueba
//     `def (mayor, menor) = version as List`.
//  4. Implementa `isCase` en `Version` para poder usarla en un `switch`.
