package com.alejandro.c02types

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  02.2 · Números: el literal decimal es BigDecimal
//
//  QUÉ ES
//    Groovy elige el tipo de un literal numérico por su forma y su tamaño. La decisión
//    que sorprende a todo el mundo: `1.5` NO es un `double`, es un `BigDecimal`.
//
//  POR QUÉ IMPORTA
//    Es probablemente la diferencia más útil de Groovy respecto a Java, y la que más
//    desconcierta al principio. `0.1 + 0.2 == 0.3` es CIERTO en Groovy y falso en Java.
//    El día que calcules dinero, lo agradecerás; el día que portes una fórmula de Java
//    esperando la aritmética de coma flotante, te morderá.
//
//  ERRORES COMUNES
//    · Asumir que `/` entre enteros da un entero. Da un BigDecimal.
//    · Escribir `double` "porque es más rápido" en cálculos de dinero.
//    · Sorprenderse de que un entero grande no desborde: Groovy lo promociona.
// =====================================================================================

class C02_02Numbers {

    /**
     * Qué tipo tiene cada literal.
     */
    static void demoLiteralTypes() {
        section('Enteros: el tipo más pequeño que quepa, a partir de Integer')

        show('42', 42.class.simpleName)
        show('42L', 42L.class.simpleName)
        show('42G', 42G.class.simpleName)
        show('2147483648 (no cabe en Integer)', 2147483648.class.simpleName)
        show('99999999999999999999', 99999999999999999999.class.simpleName)

        bullet('Groovy sube a Long y después a BigInteger según haga falta.')

        section('Decimales: BigDecimal por defecto')

        show('1.5', 1.5.class.simpleName)
        show('1.5d', 1.5d.class.simpleName)
        show('1.5f', 1.5f.class.simpleName)
        show('1.5G', 1.5G.class.simpleName)

        bullet('El sufijo `d` fuerza Double; `f`, Float; `G`, BigDecimal explícito.')
        bullet('Sin sufijo: BigDecimal. ÉSTA es la diferencia con Java.')

        section('Notación cómoda')

        show('1_000_000', 1_000_000)
        show('0b1010 (binario)', 0b1010)
        show('0xFF (hexadecimal)', 0xFF)
        show('1e3', 1e3.class.simpleName + ' → ' + 1e3)
        bullet('Ojo con `1e3`: la notación científica SÍ produce un BigDecimal.')
    }

    /**
     * La consecuencia práctica: la aritmética decimal es exacta.
     */
    static void demoDecimalIsExact() {
        section('El ejemplo que todo el mundo ha sufrido en Java')

        show('0.1 + 0.2', 0.1 + 0.2)
        show('¿es igual a 0.3?', (0.1 + 0.2) == 0.3)
        bullet('En Java esto imprime 0.30000000000000004 y `false`.')

        section('Con double explícito, vuelve el problema')

        show('0.1d + 0.2d', 0.1d + 0.2d)
        show('¿es igual a 0.3d?', (0.1d + 0.2d) == 0.3d)
        bullet('Mismo cálculo, mismo resultado que Java: el tipo lo cambia todo.')

        section('Por eso el dinero va en BigDecimal (o en céntimos enteros)')

        def precio = 19.99
        def unidades = 3
        show('19.99 * 3', precio * unidades)
        show('tipo del resultado', (precio * unidades).class.simpleName)

        def conDouble = 19.99d * 3
        show('19.99d * 3', conDouble)
        bullet('El segundo ya arrastra error. Con tres cifras no se nota; con mil, sí.')

        section('Lo que cuesta: BigDecimal es más lento y ocupa más')

        bullet('Para dinero y cantidades exactas: BigDecimal, sin dudarlo.')
        bullet('Para geometría, señales o simulación: `double` explícito.')
        bullet('La decisión es tuya; el valor por defecto de Groovy es el prudente.')
    }

    /**
     * División y las sorpresas que trae.
     */
    static void demoDivision() {
        section('`/` entre enteros NO trunca')

        show('7 / 2', 7 / 2)
        show('tipo', (7 / 2).class.simpleName)
        bullet('En Java, `7 / 2` es 3. Aquí es 3.5, y es un BigDecimal.')

        section('Para la división entera está `intdiv()`')

        show('7.intdiv(2)', 7.intdiv(2))
        show('(-7).intdiv(2)', (-7).intdiv(2))
        show('7 % 2 (resto)', 7 % 2)
        show('(-7) % 2', (-7) % 2)

        section('Cuando la división no es exacta, BigDecimal decide una escala')

        show('1 / 3', 1 / 3)
        bullet('No lanza ArithmeticException: Groovy le da una escala razonable.')
        bullet('Con BigDecimal "pelado" de Java, `divide` sin MathContext sí lanzaría.')

        section('División entre cero: depende del tipo')

        try {
            show('1 / 0', 1 / 0)
        } catch (ArithmeticException e) {
            show('1 / 0', "${e.class.simpleName}: ${e.message}")
        }
        show('1.0d / 0', 1.0d / 0)
        bullet('Con enteros y BigDecimal lanza; con `double` da Infinity.')
        show('0.0d / 0', 0.0d / 0)
        bullet('Y 0.0d/0 da NaN, que NO es igual a sí mismo.')
        show('NaN == NaN', Double.NaN == Double.NaN)
    }

    /**
     * Desbordamiento: Groovy promociona en lugar de dar la vuelta.
     */
    static void demoOverflow() {
        section('Con `def`, un entero grande se promociona solo')

        def grande = Integer.MAX_VALUE
        show('Integer.MAX_VALUE', grande)
        show('tipo', grande.class.simpleName)

        def masUno = grande + 1
        show('MAX_VALUE + 1', masUno)
        show('tipo', masUno.class.simpleName)
        bullet('En Java esto daría -2147483648. Groovy sube a Long.')

        section('Y de Long pasa a BigInteger')

        def enorme = Long.MAX_VALUE + 1
        show('Long.MAX_VALUE + 1', enorme)
        show('tipo', enorme.class.simpleName)

        section('Pero si declaras `int`, mandas tú y vuelve el desbordamiento')

        int declarado = Integer.MAX_VALUE
        int desbordado = declarado + 1
        show('int + 1', desbordado)
        bullet('El tipo declarado fuerza la aritmética de 32 bits: da la vuelta.')
        bullet('Es la trampa: `def` te protege, `int` no.')
    }

    /**
     * El operador de potencia y otros métodos numéricos.
     */
    static void demoPowerAndMath() {
        section('`**` es la potencia (en Java no existe)')

        show('2 ** 10', 2 ** 10)
        show('tipo', (2 ** 10).class.simpleName)
        show('2 ** 0.5', 2 ** 0.5)
        show('2 ** 100', 2 ** 100)
        bullet('Con exponente entero da entero exacto; con decimal, Double.')

        section('Métodos que el GDK añade a los números (capítulo 20)')

        show('10.times { } → veces', contarTimes(10))
        show('(1..5).sum()', (1..5).sum())
        show('3.14159.round(2)', 3.14159.round(2))
        show('(-7).abs()', (-7).abs())
        show('[5, 3].max()', [5, 3].max())
        bullet('`max` es de colecciones: `5.max(3)` NO existe. Para dos números sueltos,')
        bullet('`Integer.max(5, 3)` (de Java) o mete los dos en una lista.')
        show('Integer.max(5, 3)', Integer.max(5, 3))
        show('Integer.toString(255, 16)', Integer.toString(255, 16))
        bullet('Ojo: `255.toString(16)` compila y devuelve "16". No es el radix.')
        show('10.upto(13) → lista', recogerUpto(10, 13))
    }

    private static int contarTimes(int n) {
        int total = 0
        n.times { total++ }
        total
    }

    private static List<Integer> recogerUpto(int desde, int hasta) {
        List<Integer> acumulado = []
        desde.upto(hasta) { acumulado << (it as Integer) }
        acumulado
    }

    /**
     * Comparar números de tipos distintos.
     */
    static void demoComparingNumbers() {
        section('`==` compara VALOR, aunque los tipos no coincidan')

        show('1 == 1L', 1 == 1L)
        show('1 == 1.0', 1 == 1.0)
        show('1 == 1.0d', 1 == 1.0d)
        show('1.0 == 1.00', 1.0 == 1.00)
        bullet('Esto en Java con objetos (`Integer.equals(Long)`) sería `false`.')
        bullet('Groovy compara números por valor numérico, no por clase.')

        section('La prueba: equals() directo dice otra cosa')

        show('Integer.valueOf(1).equals(1L)', Integer.valueOf(1).equals(1L))
        show('1 == 1L (operador de Groovy)', 1 == 1L)
        bullet('El `==` de Groovy en números NO es `equals`: usa `compareTo`.')

        section('`is()` compara identidad, como el `==` de Java')

        Integer a = 1000
        Integer b = 1000
        show('a == b', a == b)
        show('a.is(b)', a.is(b))
        bullet('Dos objetos distintos con el mismo valor. Capítulo 04.')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia `def precio = 19.99` por `double precio = 19.99` y compara el total.
//  2. Calcula `(0.1 + 0.2) * 1000` con BigDecimal y con double, y mira los decimales.
//  3. Sustituye `7.intdiv(2)` por `(int) (7 / 2)` y comprueba que da lo mismo.
//  4. Declara `int contador = Integer.MAX_VALUE` y súmale 1 en un bucle: verás la
//     vuelta al negativo. Después cámbialo a `def` y mira qué pasa.
//  5. Prueba `2 ** 1000` y comprueba qué tipo devuelve.
