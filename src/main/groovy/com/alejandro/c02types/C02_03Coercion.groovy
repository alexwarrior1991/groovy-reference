package com.alejandro.c02types

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.quoted
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  02.3 · Coerción: `as`, asType y la asignación a variable tipada
//
//  QUÉ ES
//    Convertir un valor de un tipo a otro. Groovy ofrece tres caminos:
//      valor as Tipo        el operador de coerción
//      valor.asType(Tipo)   lo mismo, en forma de método
//      Tipo x = valor       la asignación a variable tipada coerciona sola
//    Y además los métodos explícitos del GDK: `toInteger()`, `toList()`, `toString()`.
//
//  POR QUÉ IMPORTA
//    Es lo que hace que `'42' as int` funcione y que un mapa se convierta en un objeto
//    con una línea. También es la fuente de sorpresas cuando la conversión se dispara
//    sin que la pidas.
//
//  ERRORES COMUNES
//    · Creer que `(Tipo) x` sólo comprueba, como en Java. En Groovy también CONVIERTE.
//    · Usar `as` con datos de entrada sin validar: lanza en ejecución.
//    · Olvidar que la coerción de una lista a un objeto usa el constructor por posición.
// =====================================================================================

class C02_03Coercion {

    /**
     * El operador `as` y sus conversiones más útiles.
     */
    static void demoAsOperator() {
        section('Cadenas a números')

        show("'42' as Integer", '42' as Integer)
        show("'42' as int", '42' as int)
        show("'3.14' as BigDecimal", '3.14' as BigDecimal)
        show("'42' as Double", '42' as Double)

        section('Números a cadena, y entre sí')

        show('42 as String', quoted(42 as String))
        show('3.9 as Integer (trunca)', 3.9 as Integer)
        show('-3.9 as Integer (trunca hacia cero)', -3.9 as Integer)
        bullet('`as Integer` TRUNCA, no redondea. Para redondear: `.round()`.')
        show('3.9.round()', 3.9.round())

        section('Entre colecciones')

        show("[1, 2, 2, 3] as Set", [1, 2, 2, 3] as Set)
        show("[1, 2, 3] as int[]", [1, 2, 3] as int[])
        show("'abc' as List", 'abc' as List)
        show('[a: 1] as TreeMap', ([a: 1] as TreeMap).getClass().simpleName)
        bullet('Fíjate en `getClass()` y no en `.class`: sobre un mapa, `.class` busca')
        bullet('la CLAVE "class". Es una de las trampas del capítulo 09.')

        section('En Groovy, el casting también CONVIERTE')

        // Viniendo de Java uno espera que `(Tipo) x` sólo compruebe. Aquí no:
        show('(int) 3.9', (int) 3.9)
        show('(String) 42', quoted((String) 42))
        bullet('`(Tipo) x` y `x as Tipo` hacen casi lo mismo: los dos coercionan.')

        section('Y los dos fallan igual cuando no pueden')

        Object obj = 'soy una cadena'
        try {
            Integer roto = (Integer) obj
            show('nunca llega', roto)
        } catch (Exception e) {
            show('(Integer) "soy una cadena"', e.class.simpleName)
        }
        try {
            show('"soy una cadena" as Integer', obj as Integer)
        } catch (Exception e) {
            show('"soy una cadena" as Integer', e.class.simpleName)
        }
        bullet('La misma excepción: GroovyCastException. No es el ClassCastException')
        bullet('de Java, porque no ha habido comprobación de tipos: ha habido intento')
        bullet('de conversión.')
    }

    /**
     * La coerción implícita de la asignación tipada.
     */
    static void demoImplicitCoercion() {
        section('Lo que la asignación SÍ convierte sola')

        // Un número se adapta al tipo declarado. Ojo: trunca, no redondea.
        int truncado = 3.9
        show('int x = 3.9', truncado)

        long aLong = 42
        show('long x = 42', aLong)

        // Y cualquier objeto se convierte a String con su toString().
        String desdeNumero = 42
        show('String x = 42', quoted(desdeNumero))

        String desdeLista = [1, 2]
        show('String x = [1, 2]', quoted(desdeLista))

        section('Lo que NO convierte: de cadena a número')

        mostrandoElFallo("int x = '42'") { int desdeCadena = '42'; desdeCadena }
        bullet('Es deliberado: convertir texto a número puede fallar, y Groovy prefiere')
        bullet("que lo pidas tú: \"42\" as int, o \"42\".toInteger().")

        section('La misma regla en el retorno de un método')

        show('devuelveInt() declara int y devuelve 3.9', devuelveInt())
        mostrandoElFallo('devuelveIntDesdeTexto()') { devuelveIntDesdeTexto() }

        section('En los parámetros, en cambio, NO hay coerción ninguna')

        // Ésta es la asimetría que más despista: la ASIGNACIÓN convierte, pero la
        // SELECCIÓN de método no. Groovy busca un método cuyo parámetro admita el tipo
        // del argumento; si no lo encuentra, lanza MissingMethodException.
        show('recibeInt(7)', recibeInt(7))
        mostrandoElFallo('recibeInt(7.9)') { recibeInt(7.9) }
        mostrandoElFallo('recibeInt(7L)') { recibeInt(7L) }
        mostrandoElFallo("recibeInt('7')") { recibeInt('7') }
        show('recibeInt(7.9 as int)', recibeInt(7.9 as int))

        bullet('Ni siquiera un `long` entra en un parámetro `int`: no es una conversión')
        bullet('que se pierda datos "sin avisar", es que el método no encaja.')
        bullet('La asignación CONVIERTE; el despacho de métodos SELECCIONA.')

        section('Por eso un parámetro `Number` es mucho más flexible')

        show('recibeNumero(7.9)', recibeNumero(7.9))
        show('recibeNumero(7L)', recibeNumero(7L))
        bullet('Si quieres aceptar cualquier número, pide `Number`, no `int`.')

        section('Cuidado: un rango asignado a List sigue siendo un rango')

        List<Integer> desdeRango = 1..3
        show('List x = 1..3', desdeRango)
        show('su clase', desdeRango.getClass().simpleName)
        bullet('No lo copia a un ArrayList: un IntRange YA es una List.')
        bullet('Si necesitas una lista mutable: `(1..3).toList()`.')
        show('(1..3).toList().getClass()', (1..3).toList().getClass().simpleName)

        section('Dónde esto muerde')

        bullet('Un dato de entrada llega casi siempre como texto.')
        bullet('`int edad = fila.edad` no da problemas al compilar: lanza al ejecutar,')
        bullet('y lo hace en la línea de la asignación, lejos de donde leíste el dato.')
        bullet('Valida en la frontera y convierte tú:')

        String entrada = '12x'
        show('"12x".isInteger()', entrada.isInteger())
        show('"12".isInteger()', '12'.isInteger())
        show('"3.5".isNumber()', '3.5'.isNumber())
        show('conversión segura de "12x"', entrada.isInteger() ? entrada as int : 0)
    }

    private static int devuelveInt() { 3.9 }

    private static int devuelveIntDesdeTexto() { '99' }

    private static int recibeInt(int n) { n * 2 }

    private static int recibeNumero(Number n) { n.intValue() * 2 }

    /** Ejecuta el bloque y enseña la excepción en vez de dejarla salir. */
    private static void mostrandoElFallo(String etiqueta, Closure<?> bloque) {
        try {
            show(etiqueta, bloque.call())
        } catch (Exception e) {
            show(etiqueta, e.class.simpleName)
        }
    }

    /**
     * asType a medida: coerción de mapas y listas a objetos.
     */
    static void demoCoercingToObjects() {
        section('Una lista se convierte en objeto por el constructor POSICIONAL')

        def punto = [3, 4] as Punto
        show('[3, 4] as Punto', punto)
        bullet('Busca el constructor que encaje por número y TIPO de argumentos.')

        // Y si no encaja, no fuerza las conversiones: falla.
        try {
            show("['3', '4'] as Punto", ['3', '4'] as Punto)
        } catch (Exception e) {
            show("['3', '4'] as Punto", e.class.simpleName)
        }
        bullet('Con cadenas no encuentra `Punto(String, String)` y lanza. La coerción de')
        bullet('lista a objeto NO convierte los elementos por el camino.')

        section('Un mapa se convierte usando las PROPIEDADES')

        def otro = [x: 10, y: 20] as Punto
        show('[x:10, y:20] as Punto', otro)
        bullet('Aquí llama al constructor vacío y después asigna propiedad a propiedad.')
        bullet('Por eso el POGO necesita un constructor sin argumentos para esta vía.')

        section('Un mapa se convierte en una interfaz, con closures como métodos')

        // Ésta es una de las cosas más elegantes de Groovy: implementar una interfaz
        // al vuelo, sin clase anónima.
        Comparator<String> porLongitud = [compare: { String a, String b -> a.length() <=> b.length() }] as Comparator
        show('compare("aa", "b")', porLongitud.compare('aa', 'b'))
        show('ordenado por longitud', ['ccc', 'a', 'bb'].toSorted(porLongitud))

        section('Un closure se convierte en una interfaz de un solo método')

        Runnable tarea = { println '    (ejecutada como Runnable)' } as Runnable
        tarea.run()
        bullet('Es la coerción SAM. Groovy la hace sola si el parámetro lo pide.')
    }

    /**
     * Asignación múltiple.
     */
    static void demoMultipleAssignment() {
        section('Desempaquetar una lista en varias variables')

        def (a, b) = [1, 2]
        show('def (a, b) = [1, 2]', "a=$a, b=$b")

        def (String nombre, int edad) = ['Ana', 30]
        show('con tipos', "$nombre, $edad")

        section('Si sobran o faltan elementos')

        def (x, y, z) = [1, 2]
        show('def (x, y, z) = [1, 2]', "x=$x, y=$y, z=$z")
        bullet('Los que faltan quedan a null: NO lanza.')

        def (p, q) = [1, 2, 3]
        show('def (p, q) = [1, 2, 3]', "p=$p, q=$q")
        bullet('Los que sobran se descartan, también sin protestar.')

        section('El intercambio en una línea')

        def uno = 'primero'
        def dos = 'segundo'
        (uno, dos) = [dos, uno]
        show('tras intercambiar', "$uno, $dos")
        bullet('Sin variable temporal. Ojo: los paréntesis son obligatorios.')

        section('Muy útil con métodos que devuelven varios valores')

        def (min, max) = minMax([4, 9, 1, 7])
        show('minMax([4,9,1,7])', "min=$min, max=$max")
        bullet('Devolver una lista de dos es el equivalente Groovy de un `out` parameter.')
    }

    private static List<Integer> minMax(List<Integer> valores) {
        [valores.min(), valores.max()]
    }

    static class Punto {
        int x
        int y

        Punto() {}

        Punto(int x, int y) { this.x = x; this.y = y }

        @Override
        String toString() { "Punto($x, $y)" }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Prueba `[1, 2, 3] as Punto` y lee el error: el constructor no encaja.
//  2. Quita el constructor vacío de `Punto` y vuelve a probar `[x:10, y:20] as Punto`.
//  3. Cambia `3.9 as Integer` por `-3.9 as Integer` y por `(-3.9).round()`: tres
//     resultados distintos para "convertir a entero".
//  4. Convierte `['a', 'b'] as Comparator` y mira qué excepción da: la coerción de mapa
//     a interfaz necesita las CLAVES con el nombre de los métodos.
