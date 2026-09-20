package com.alejandro.c17categories

import groovy.time.TimeCategory
import groovy.lang.Category

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  17.1 · Categorías: metaprogramación con fecha de caducidad
//
//  QUÉ ES
//    Una categoría es una clase con métodos estáticos cuyo primer parámetro es el
//    objeto receptor. Dentro de un bloque `use(MiCategoria) { … }`, esos métodos se
//    comportan como si pertenecieran a la clase.
//
//  POR QUÉ IMPORTA
//    Es la alternativa CONTENIDA al `metaClass` del capítulo 16: el cambio sólo existe
//    dentro del bloque y sólo en ese hilo. Nada se filtra al resto del programa.
//
//  ERRORES COMUNES
//    · Esperar que el método siga existiendo fuera del `use`.
//    · Olvidar que `use` no afecta a otros hilos.
//    · Usar una categoría donde un módulo de extensión (17.2) era lo correcto.
// =====================================================================================

/** Una categoría "a mano": métodos estáticos cuyo primer parámetro es el receptor. */
class TextoCategoria {
    static String gritar(String self) { self.toUpperCase() + '!' }

    static String entrecomillar(String self, String comilla = '"') { "$comilla$self$comilla" }

    static boolean esPalindromo(String self) {
        String limpio = self.toLowerCase().replaceAll(/[^a-z0-9]/, '')
        limpio == limpio.reverse()
    }
}

/** Con @Category no hace falta el parámetro: `this` ES el receptor. */
@Category(Integer)
class NumeroCategoria {
    String enRomano() {
        def valores = [1000: 'M', 900: 'CM', 500: 'D', 400: 'CD', 100: 'C', 90: 'XC',
                       50: 'L', 40: 'XL', 10: 'X', 9: 'IX', 5: 'V', 4: 'IV', 1: 'I']
        int resto = this
        StringBuilder salida = new StringBuilder()
        valores.each { int valor, String simbolo ->
            while (resto >= valor) { salida.append(simbolo); resto -= valor }
        }
        salida.toString()
    }

    boolean esPrimo() {
        if (this < 2) return false
        if (this < 4) return true
        // Los paréntesis importan: sin ellos, `as int` se aplica al RANGO entero
        // en vez de a la raíz, y falla al convertir.
        int limite = Math.sqrt(this.doubleValue()) as int
        (2..limite).every { this % it != 0 }
    }
}

class C17_01Categories {

    /**
     * Cómo se usa una categoría.
     */
    static void demoUsing() {
        section('Fuera del bloque, el método no existe')

        try {
            'hola'.gritar()
        } catch (MissingMethodException e) {
            show("'hola'.gritar()", e.class.simpleName)
        }

        section('Dentro del `use`, sí')

        use(TextoCategoria) {
            show("'hola'.gritar()", 'hola'.gritar())
            show("'texto'.entrecomillar()", 'texto'.entrecomillar())
            show("'texto'.entrecomillar('*')", 'texto'.entrecomillar('*'))
            show("'Anita lava la tina'.esPalindromo()", 'Anita lava la tina'.esPalindromo())
        }

        section('Y al salir, vuelve a no existir')

        show('¿responde ahora?', 'hola'.respondsTo('gritar') as boolean)
        bullet('Ésta es LA diferencia con `metaClass`: el cambio tiene fecha de')
        bullet('caducidad y no se filtra a nadie más.')

        section('Con @Category, sin el parámetro `self`')

        use(NumeroCategoria) {
            show('2026.enRomano()', 2026.enRomano())
            show('4.enRomano()', 4.enRomano())
            show('17.esPrimo()', 17.esPrimo())
            show('18.esPrimo()', 18.esPrimo())
            show('los primos hasta 20', (1..20).findAll { it.esPrimo() })
        }
        bullet('`@Category(Integer)` convierte `this` en el número receptor. Se lee')
        bullet('mucho mejor que arrastrar `self` en cada firma.')

        section('Varias categorías a la vez')

        use(TextoCategoria, NumeroCategoria) {
            show('combinadas', "${7.enRomano()} ${'vii'.gritar()}")
        }

        section('Una del GDK: TimeCategory')

        use(TimeCategory) {
            def dentroDeUnaSemana = new Date() + 1.week
            def diferencia = dentroDeUnaSemana - new Date()
            show('1.week es un objeto Duration', 1.week.getClass().simpleName)
            show('días de diferencia', diferencia.days)
        }
        bullet('`1.week`, `3.days`, `2.hours`: sólo existen dentro del `use`.')
    }

    /**
     * Los límites.
     */
    static void demoLimits() {
        section('El alcance es el BLOQUE y el HILO actual')

        use(TextoCategoria) {
            show('en este hilo', 'ok'.gritar())

            String resultadoDeOtroHilo = null
            Thread hilo = Thread.start {
                try {
                    resultadoDeOtroHilo = 'otro'.gritar()
                } catch (MissingMethodException e) {
                    resultadoDeOtroHilo = e.class.simpleName
                }
            }
            hilo.join()
            show('en otro hilo', resultadoDeOtroHilo)
        }
        bullet('`use` guarda las categorías en un ThreadLocal. Otro hilo no las ve.')
        bullet('Es la razón principal para NO usar categorías en código concurrente.')

        section('Tampoco valen con @CompileStatic')

        bullet('El compilador estático no sabe que dentro del `use` hay métodos')
        bullet('nuevos, así que no compila. Es la misma limitación del capítulo 15.')

        section('Y cuestan en rendimiento')

        int vueltas = 200_000
        long conCategoria = medir {
            use(TextoCategoria) { vueltas.times { 'x'.gritar() } }
        }
        long conMetodo = medir {
            vueltas.times { TextoCategoria.gritar('x') }
        }
        show('llamando por la categoría', "${conCategoria} ms")
        show('llamando al estático directamente', "${conMetodo} ms")
        bullet('Cada llamada dentro del `use` pasa por la búsqueda del MOP.')

        section('La tabla de decisión')

        bullet('herramienta            alcance              @CompileStatic')
        bullet('────────────────────   ──────────────────   ──────────────')
        bullet('metaClass              TODO el proceso      no')
        bullet('use(Categoria) { }     el bloque, un hilo   no')
        bullet('módulo de extensión    todo, desde el jar   SÍ')
        bullet('trait / @Delegate      las clases que uses  SÍ')

        section('La regla')

        bullet('Si el método lo quieres SIEMPRE: módulo de extensión (17.2).')
        bullet('Si es un apaño local y acotado: categoría.')
        bullet('Si es tu propia clase: ponle el método y ya está.')
    }

    private static long medir(Closure<?> bloque) {
        bloque()
        long t0 = System.nanoTime()
        bloque()
        ((System.nanoTime() - t0) / 1_000_000L) as long
    }

    /**
     * Módulos de extensión: lo que hace el GDK.
     */
    static void demoExtensionModules() {
        section('Cómo añade el GDK sus métodos')

        bullet('`capitalize`, `tokenize`, `each`, `collect`… no están en las clases')
        bullet('de Java. Los añade un MÓDULO DE EXTENSIÓN.')

        section('Qué hace falta para escribir uno')

        bullet('1. Una clase con métodos estáticos cuyo primer parámetro es el')
        bullet('   receptor (igual que una categoría a mano):')
        bullet('')
        bullet('   class MisExtensiones {')
        bullet('       static String gritar(String self) { self.toUpperCase() + "!" }')
        bullet('   }')
        bullet('')
        bullet('2. Un fichero META-INF/groovy/org.codehaus.groovy.runtime.ExtensionModule')
        bullet('   con:')
        bullet('')
        bullet('   moduleName=mis-extensiones')
        bullet('   moduleVersion=1.0')
        bullet('   extensionClasses=com.ejemplo.MisExtensiones')
        bullet('')
        bullet('3. Y ya está: en cuanto el jar esté en el classpath, el método')
        bullet('   existe SIEMPRE, sin `use` y sin tocar ninguna metaclase.')

        section('Para métodos estáticos, otra clave')

        bullet('staticExtensionClasses=com.ejemplo.MisExtensionesEstaticas')
        bullet('Así se añaden cosas como `Date.ayer()` o `String.aleatoria(10)`.')

        section('Por qué es la forma correcta')

        bullet('Funciona con @CompileStatic: el compilador los ve.')
        bullet('El IDE los autocompleta.')
        bullet('No hay coste de búsqueda en ejecución más allá del normal.')
        bullet('Y se distribuye como una librería, no como un efecto secundario.')

        section('Los métodos de este repositorio que ya vienen de ahí')

        show("'abc'.toList()", 'abc'.toList())
        show('[1,2].sum()', [1, 2].sum())
        show('42.times { } existe', 42.respondsTo('times') as boolean)
        bullet('Todo el capítulo 20 es, literalmente, un módulo de extensión enorme.')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Llama a `gritar()` fuera del `use` y lee el error.
//  2. Escribe una categoría para `List` que añada `segundo()` y `penultimo()`.
//  3. Pon @CompileStatic en `demoUsing` y mira qué deja de compilar.
//  4. Compara `use(TextoCategoria) { }` con hacer lo mismo por `String.metaClass` y
//     comprueba cuál se filtra al resto del programa.
