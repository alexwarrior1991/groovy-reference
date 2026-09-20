package com.alejandro.c15static

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  15.1 · @TypeChecked y @CompileStatic
//
//  QUÉ ES
//    Dos anotaciones que le piden al compilador que haga su trabajo:
//      @TypeChecked    comprueba los tipos al COMPILAR; el código sigue siendo dinámico
//      @CompileStatic  además genera llamadas DIRECTAS, saltándose el MOP
//
//  POR QUÉ IMPORTA
//    Son la respuesta de Groovy a "es lento" y "los errores salen en ejecución". Con
//    @CompileStatic, Groovy corre prácticamente como Java. El precio: pierdes la
//    metaprogramación en runtime dentro de ese código.
//
//  ERRORES COMUNES
//    · Poner @CompileStatic en una clase que usa `methodMissing` o `metaClass`.
//    · Esperar que comprueben lo que pasa DENTRO de un closure sin @DelegatesTo.
//    · Creer que es todo o nada: se puede mezclar con @CompileDynamic.
// =====================================================================================

class C15_01StaticTyping {

    /**
     * Qué comprueba cada una.
     */
    static void demoWhatTheyCheck() {
        section('Sin anotación: el error aparece al EJECUTAR')

        try {
            show('método que no existe', sinComprobar())
        } catch (Exception e) {
            show('método que no existe', e.class.simpleName)
        }
        bullet('`"texto".metodoInventado()` compila tan feliz. Falla al llamarlo.')

        section('Con @TypeChecked, eso ni compila')

        bullet('El mismo código con @TypeChecked da:')
        bullet('"Cannot find matching method java.lang.String#metodoInventado()"')
        bullet('Y lo dice al COMPILAR, no al ejecutar.')

        section('Lo que @TypeChecked comprueba')

        bullet('Que el método exista en el tipo declarado.')
        bullet('Que los tipos de los argumentos encajen.')
        bullet('Que la asignación sea compatible.')
        bullet('Que la variable esté declarada.')
        bullet('Que el tipo de retorno case con lo que se devuelve.')

        show('suma tipada', sumaTipada(2, 3))
        show('inferencia de tipos', tipoInferido())

        section('Lo que @TypeChecked NO hace')

        bullet('NO cambia cómo se ejecuta: las llamadas siguen pasando por el MOP.')
        bullet('Por eso @TypeChecked no da velocidad; sólo seguridad al compilar.')

        section('@CompileStatic hace lo de arriba MÁS llamadas directas')

        show('lo mismo, compilado estático', new Calculadora().sumar(2, 3))
        bullet('Genera `invokevirtual` como Java, sin pasar por la metaclase.')
        bullet('Es lo que hay que poner si te importa el rendimiento.')
    }

    private static String sinComprobar() {
        // Sin @TypeChecked esto compila: el error es en ejecución.
        'texto'.invokeMethod('metodoInventado', null)
    }

    @TypeChecked
    private static int sumaTipada(int a, int b) { a + b }

    @TypeChecked
    private static String tipoInferido() {
        def lista = ['a', 'bb', 'ccc']      // infiere List<String>
        def total = lista.sum { it.size() } // infiere el tipo del resultado
        "total: $total"
    }

    @CompileStatic
    static class Calculadora {
        int sumar(int a, int b) { a + b }
    }

    /**
     * Lo que se pierde.
     */
    static void demoWhatYouLose() {
        section('El código dinámico funciona')

        def dinamico = new Dinamica()
        show('método añadido en caliente', dinamico.metodoAñadido())
        bullet('Esto es el capítulo 16: añadir métodos a una clase en ejecución.')

        section('Dentro de @CompileStatic, eso NO compila')

        bullet('Lo que deja de poder hacerse:')
        bullet('· llamar a métodos añadidos con `metaClass`')
        bullet('· `methodMissing` y `propertyMissing`')
        bullet('· usar categorías con `use { }`')
        bullet('· llamar a un método de un `def` cuyo tipo no se puede inferir')
        bullet('· `obj."$nombreDinamico"()`')

        section('Lo que SÍ se puede seguir haciendo')

        show('closures', conClosuresEstatico())
        show('colecciones y el GDK', conGdkEstatico())
        show('GString', conGStringEstatico())
        bullet('El GDK entero funciona: no es dinámico, son métodos de verdad.')

        section('Mezclar: @CompileDynamic dentro de @CompileStatic')

        def mixto = new Mixta()
        show('la parte estática', mixto.rapido(5))
        show('la parte dinámica', mixto.flexible())
        bullet('`@CompileDynamic` abre un agujero en una clase estática.')
        bullet('Es lo correcto: pon estático el 95% y deja dinámico lo que lo pida.')
    }

    static class Dinamica {
        static {
            Dinamica.metaClass.metodoAñadido = { -> 'soy un método añadido en ejecución' }
        }
    }

    @CompileStatic
    private static List<String> conClosuresEstatico() {
        List<String> palabras = ['groovy', 'java']
        palabras.collect { String p -> p.toUpperCase() }
    }

    @CompileStatic
    private static Map<String, Integer> conGdkEstatico() {
        ['a', 'bb', 'ccc'].collectEntries { String p -> [p, p.length()] }
    }

    @CompileStatic
    private static String conGStringEstatico() {
        int n = 42
        "el valor es $n"
    }

    @CompileStatic
    static class Mixta {
        int rapido(int n) { n * 2 }

        @CompileDynamic
        String flexible() {
            def objeto = new Expando(saludo: 'hola desde un Expando')
            objeto.saludo
        }
    }

    /**
     * Cuánto se gana.
     */
    static void demoPerformance() {
        section('La misma cuenta, dinámica y estática')

        int vueltas = 3_000_000

        long dinamico = medir { bucleDinamico(vueltas) }
        long estatico = medir { bucleEstatico(vueltas) }

        show('dinámico', "${dinamico} ms")
        show('estático', "${estatico} ms")
        show('veces más rápido', estatico > 0 ? String.format('%.1f', dinamico / (double) estatico) : 'muchas')

        bullet('La diferencia está en el despacho: el dinámico consulta la metaclase')
        bullet('en CADA llamada; el estático hace un `invokevirtual` y ya.')

        section('Pero cuidado con leer esto mal')

        bullet('En código que hace E/S, consultas o red, esto no se nota.')
        bullet('Se nota en bucles cerrados con millones de operaciones.')
        bullet('Mide antes de anotar. Y después.')

        section('Dónde poner @CompileStatic, en la práctica')

        bullet('En bucles calientes y cálculo numérico: siempre.')
        bullet('En la lógica de negocio y los modelos: sí, por la seguridad de tipos.')
        bullet('En DSLs, builders y configuración: normalmente no, o con @DelegatesTo.')
        bullet('En tests con Spock: no, los bloques `where:` son dinámicos.')

        section('Y una forma de no repetirla en cada clase')

        bullet('Se puede aplicar a un paquete entero con un fichero de configuración')
        bullet('del compilador (`configscript`), o poniéndola en el `package-info`.')
    }

    private static long medir(Closure<?> bloque) {
        bloque()                      // una pasada en vacío para calentar el JIT
        long t0 = System.nanoTime()
        bloque()
        ((System.nanoTime() - t0) / 1_000_000L) as long
    }

    private static long bucleDinamico(int vueltas) {
        def total = 0
        for (def i = 0; i < vueltas; i++) {
            total = total + i % 7
        }
        total
    }

    @CompileStatic
    private static long bucleEstatico(int vueltas) {
        long total = 0
        for (int i = 0; i < vueltas; i++) {
            total = total + i % 7
        }
        total
    }

    /**
     * Genéricos y borrado de tipos.
     */
    static void demoGenerics() {
        section('Los genéricos son de COMPILACIÓN')

        List<String> textos = ['a', 'b']
        show('el tipo en ejecución', textos.getClass().simpleName)
        bullet('En el bytecode es una ArrayList pelada. Es el borrado de tipos (erasure).')

        section('Sin @TypeChecked, los genéricos no protegen nada')

        List<String> conIntruso = ['a']
        conIntruso.add(42 as Object)
        show('una lista <String> con un entero', conIntruso)
        bullet('Ni aviso. El genérico es documentación hasta que alguien comprueba.')

        section('Con @TypeChecked, sí')

        bullet('`conIntruso.add(42)` no compilaría: el tipo no encaja.')
        show('uso correcto y comprobado', sumaLongitudes(['a', 'bb']))

        section('Métodos genéricos')

        show('primero(["x","y"])', primero(['x', 'y']))
        show('primero([1, 2])', primero([1, 2]))
        show('primero([])', primero([]))

        section('Lo que el borrado impide')

        bullet('No se puede preguntar por el tipo genérico en ejecución.')
        bullet('No existe `new T()` ni `T.class`.')
        bullet('Dos métodos que sólo difieran en el genérico chocan al compilar.')
        bullet('Si necesitas el tipo en ejecución, pásalo: `metodo(List l, Class<T> t)`.')
        show('pasando la clase', convertir(['1', '2'], Integer))
    }

    @TypeChecked
    private static int sumaLongitudes(List<String> textos) {
        textos.sum { String t -> t.length() } as int
    }

    private static <T> T primero(List<T> lista) { lista.isEmpty() ? null : lista.first() }

    private static <T> List<T> convertir(List<String> textos, Class<T> tipo) {
        textos.collect { it.asType(tipo) }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade @CompileStatic a `Dinamica` y mira el error de compilación exacto.
//  2. Sube las vueltas del bucle a 30 millones y vuelve a medir.
//  3. Quita @CompileDynamic de `Mixta.flexible()` y lee lo que dice el compilador.
//  4. Pon @TypeChecked en `demoGenerics` y mira cuántas líneas dejan de compilar.
