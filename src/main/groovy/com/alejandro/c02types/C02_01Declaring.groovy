package com.alejandro.c02types

import groovy.transform.CompileStatic

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  02.1 · def, tipos declarados y constantes
//
//  QUÉ ES
//    En Groovy una variable se declara de tres formas:
//      def x = 1      tipado dinámico: el tipo se comprueba al EJECUTAR
//      int x = 1      tipado declarado: se comprueba al ejecutar, y al COMPILAR si
//                     la clase lleva @TypeChecked o @CompileStatic
//      var x = 1      igual que `def`, sintaxis heredada de Java 10
//
//  POR QUÉ IMPORTA
//    Es la decisión que más veces vas a tomar en Groovy, y la que más discusiones
//    genera. La respuesta corta: `def` en scripts y código de pegamento; tipos
//    declarados en las APIs públicas y en todo lo que otro vaya a llamar.
//
//  ERRORES COMUNES
//    · Creer que `def` significa "sin tipo". El valor SIEMPRE tiene tipo; lo que no lo
//      tiene es la variable.
//    · Creer que `def` es más lento. Lo caro es el despacho dinámico del método, no la
//      declaración.
//    · Usar `def` en el cuerpo de un script y esperar verlo desde sus métodos (01.6).
// =====================================================================================

class C02_01Declaring {

    /**
     * def frente a un tipo declarado.
     */
    static void demoDefVsType() {
        section('`def` no quita el tipo: lo quita de la DECLARACIÓN')

        def a = 42
        show('def a = 42 → a.class', a.class.simpleName)

        def b = 'texto'
        show('def b = "texto" → b.class', b.class.simpleName)

        bullet('La variable acepta cualquier cosa; el valor siempre sabe qué es.')

        section('Con `def`, la misma variable puede cambiar de tipo')

        def cambiante = 1
        show('era', cambiante.class.simpleName)
        cambiante = 'ahora una cadena'
        show('ahora es', cambiante.class.simpleName)

        section('Con un tipo declarado, Groovy coerciona… hasta cierto punto')

        // Un número se adapta al tipo declarado, truncando si hace falta:
        int truncado = 3.9
        show('int truncado = 3.9', truncado)

        // Y cualquier cosa se convierte a String:
        String texto = 42
        show('String texto = 42', texto)

        section('Pero de cadena a número NO coerciona sola')

        try {
            int numero = '42'
            show('nunca llega aquí', numero)
        } catch (Exception e) {
            show('int numero = "42"', e.class.simpleName)
        }
        bullet('Sorprende, porque la conversión es obvia. Groovy no la hace en silencio.')

        section('Para eso hay que pedirla con `as`')

        int convertido = '42' as int
        show('int convertido = "42" as int', convertido)
        bullet('La asignación adapta números y produce cadenas; el resto, con `as`.')
        bullet('Todo esto ocurre al EJECUTAR. @CompileStatic lo adelanta (capítulo 15).')
    }

    /**
     * Qué es `def` por debajo.
     */
    static void demoWhatDefReallyIs() {
        section('`def` es literalmente Object')

        bullet('`def x` se compila exactamente igual que `Object x`.')
        bullet('El `def` existe para poder omitir el tipo donde la sintaxis lo exige.')

        section('Por eso estas dos declaraciones son la misma')

        def conDef = 3
        Object conObject = 3
        show('conDef.class', conDef.class.simpleName)
        show('conObject.class', conObject.class.simpleName)
        show('¿mismo valor?', conDef == conObject)

        section('`def` también vale para métodos y para el tipo de retorno')

        show('sumaDinamica(2, 3)', sumaDinamica(2, 3))
        show('sumaDinamica("a", "b")', sumaDinamica('a', 'b'))
        bullet('El mismo método sirve para Integer y para String: eso es duck typing.')
        bullet('Funciona porque `+` se resuelve EN EJECUCIÓN sobre el tipo real.')

        section('Y `def` en los parámetros equivale a no poner nada')

        show('sinTipos(2, 3)', sinTipos(2, 3))
    }

    private static def sumaDinamica(def a, def b) { a + b }

    private static sinTipos(a, b) { a + b }

    /**
     * var, final y las constantes de verdad.
     */
    static void demoVarAndFinal() {
        section('`var` es un sinónimo de `def`')

        var x = 10
        show('var x = 10', x)
        bullet('Añadido en Groovy 3 por familiaridad con Java 10. No infiere nada más.')

        section('`final` sí cambia algo: impide reasignar')

        final int constante = 5
        show('final int constante', constante)

        // El error de un `final` reasignado es de COMPILACIÓN, no de ejecución:
        // por eso este ejemplo va en un texto y no en código.
        bullet('`constante = 6` no compilaría: "cannot modify final field".')

        section('final protege la REFERENCIA, no el contenido')

        final List<String> lista = ['a', 'b']
        lista << 'c'                       // permitido: la referencia no cambia
        show('final List tras añadir', lista)
        bullet('Es la misma regla que el `final` de Java. Para contenido inmutable:')
        bullet('`asImmutable()` o la anotación @Immutable (capítulo 12).')

        section('Constantes de verdad: static final')

        show('LIMITE', LIMITE)
        show('NOMBRES', NOMBRES)
        bullet('`static final` en mayúsculas es la convención, igual que en Java.')

        section('asImmutable(): el contenido tampoco se toca')

        List<String> congelada = ['a', 'b'].asImmutable()
        try {
            congelada << 'c'
        } catch (UnsupportedOperationException e) {
            show('congelada << "c"', e.class.simpleName)
        }
    }

    static final int LIMITE = 100
    static final List<String> NOMBRES = ['Ana', 'Luis'].asImmutable()

    /**
     * Cuándo usar def y cuándo declarar el tipo.
     */
    static void demoWhenToUseWhich() {
        section('Usa `def` cuando…')

        bullet('Escribes un script o código de pegamento.')
        bullet('El tipo se lee en la misma línea: `def usuarios = cargarUsuarios()`.')
        bullet('Quieres duck typing a propósito.')

        section('Declara el tipo cuando…')

        bullet('Es una API pública: el tipo es la documentación.')
        bullet('Quieres @CompileStatic (capítulo 15): sin tipos no puede hacer su trabajo.')
        bullet('El valor viene de algo opaco: `def r = servicio.llamar()` no dice nada.')

        section('La diferencia medida: despacho dinámico frente a estático')

        // No es una prueba de rendimiento seria (una sola pasada, JIT calentando);
        // es para ver que el orden de magnitud es el mismo salvo en bucles calientes.
        int vueltas = 200_000
        long t1 = tiempoDe { def acc = 0; vueltas.times { acc += it }; acc }
        long t2 = tiempoDe { int acc = 0; for (int i = 0; i < vueltas; i++) acc += i; acc }

        show('con def y times()', "${t1} ms")
        show('con int y for', "${t2} ms")
        bullet('La diferencia está en el despacho dinámico, no en la palabra `def`.')
        bullet('Si te importa, @CompileStatic lo elimina. Capítulo 15.')

        section('La regla práctica')

        bullet('Dentro de un método: `def` si se lee bien, tipo si no.')
        bullet('En la firma de un método público: SIEMPRE el tipo.')
        bullet('En un campo: el tipo, salvo que de verdad admita cualquier cosa.')
    }

    private static long tiempoDe(Closure<?> bloque) {
        long t0 = System.nanoTime()
        bloque.call()
        (System.nanoTime() - t0) / 1_000_000L as long
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `int numero = '42' as int` por `int numero = '42'` y mira si sigue
//     funcionando (pista: la coerción en asignación tipada es aún más generosa).
//  2. Añade `@CompileStatic` a esta clase y comprueba cuáles de las demos dejan de
//     compilar. Eso es exactamente el capítulo 15.
//  3. Sube `vueltas` a 5_000_000 y vuelve a medir: ahí sí se separan los tiempos.
//  4. Quita el `asImmutable()` de NOMBRES y añádele un elemento desde otra demo.
