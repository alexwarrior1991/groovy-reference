package com.alejandro.c28spock

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  28.1 · Testing con Spock
//
//  QUÉ ES
//    Spock es el framework de tests de Groovy. No es "JUnit con azúcar": es un DSL
//    (capítulo 24) construido sobre una transformación AST global (capítulo 12) que
//    reescribe tus bloques `given/when/then` en tiempo de compilación.
//
//  POR QUÉ IMPORTA
//    Tres cosas que no tienes en JUnit: el "power assert", que al fallar imprime el
//    valor de cada subexpresión; los bloques `where:` en forma de tabla; y mocks
//    nativos sin librería aparte.
//
//  ESTE CAPÍTULO TIENE TESTS DE VERDAD
//    El código de producción está en `c28spock/Carrito.groovy` y los Spec que lo
//    prueban, en `src/test/groovy/com/alejandro/c28spock/`. Se ejecutan con
//    `./mvnw test` como los demás. Ábrelos mientras lees estas demos.
//
//  ERRORES COMUNES
//    · Poner varios `when/then` en un test y no saber cuál falló.
//    · Usar `Mock` cuando bastaba `Stub`, y acabar probando la implementación.
//    · `@Shared` para estado mutable: se comparte entre TODOS los tests del Spec.
// =====================================================================================

class C28_01Spock {

    /**
     * Los bloques.
     */
    static void demoBlocks() {
        section('La estructura de un test de Spock')

        bullet("def 'añadir un producto con stock lo mete en el carrito'() {")
        bullet('    given: "un almacén con cinco unidades"')
        bullet('    almacen.buscar("LIB-1") >> libro')
        bullet('    almacen.stockDe("LIB-1") >> 5')
        bullet('')
        bullet('    when: "se añaden dos"')
        bullet('    def resultado = carrito.añadir("LIB-1", 2)')
        bullet('')
        bullet('    then: "el resultado lo confirma y el carrito lo refleja"')
        bullet('    resultado instanceof Agregado')
        bullet('    carrito.unidades() == 2')
        bullet('}')

        section('Los bloques que existen')

        bullet('given: / setup:   preparar (son sinónimos)')
        bullet('when:             la ACCIÓN que se prueba, una sola')
        bullet('then:             las comprobaciones y las interacciones')
        bullet('expect:           cuando acción y comprobación caben en una línea')
        bullet('where:            la tabla de datos')
        bullet('cleanup:          limpiar, se ejecuta siempre')
        bullet('and:              partir cualquier bloque en dos, con su etiqueta')

        section('Las reglas')

        bullet('`then:` sólo puede ir detrás de `when:`.')
        bullet('Dentro de `then:`, cada línea es una aserción: no hace falta `assert`.')
        bullet('Un `when/then` por test. Si necesitas dos, son dos tests.')
        bullet('Las etiquetas en texto NO son adorno: salen en el informe de fallos.')

        section('El nombre del test es una frase')

        bullet("def 'una cantidad no positiva se rechaza'()")
        bullet('Se lee en el informe tal cual. Nombra el COMPORTAMIENTO, no el método.')
        bullet('Mal:  testAñadir1()')
        bullet('Bien: "añadir sin stock devuelve SinStock y no reserva nada"')

        section('Compruébalo tú')

        show('los Spec de este capítulo', 'src/test/groovy/com/alejandro/c28spock/')
        show('ejecútalos con', './mvnw test -Dtest=CarritoSpec')
    }

    /**
     * El power assert.
     */
    static void demoPowerAssert() {
        section('Qué imprime Spock cuando una aserción falla')

        bullet('En JUnit:   expected:<7100> but was:<3550>')
        bullet('En Spock:')
        bullet('')
        bullet('  carrito.total() == libro.centimos * 3')
        bullet('  |       |       |  |     |        ')
        bullet('  |       3550    |  |     3550     ')
        bullet('  |               |  Producto(...)  ')
        bullet('  Carrito@1a2b    false             ')
        bullet('')
        bullet('Enseña el valor de CADA subexpresión. Casi nunca hace falta abrir')
        bullet('el depurador: el fallo ya te dice dónde está.')

        section('Por eso no hay assertEquals ni assertThat')

        bullet('Con `a == b` ya tienes toda la información. La librería de matchers')
        bullet('existe para dar buenos mensajes, y aquí el mensaje sale solo.')

        section('Y funciona con cualquier expresión')

        bullet('usuarios.findAll { it.activo }*.nombre == ["Ana", "Eva"]')
        bullet('desglosa el findAll, el spread y la comparación, uno a uno.')

        section('Lo que esto cambia en la práctica')

        bullet('Puedes escribir aserciones más ricas sin miedo a que al fallar no')
        bullet('se entienda nada. En JUnit se tiende a partirlas en trozos para')
        bullet('saber cuál falló; aquí no hace falta.')

        section('Cómo funciona')

        bullet('Una transformación AST GLOBAL (capítulo 12) reescribe el cuerpo del')
        bullet('`then:` para ir guardando cada valor intermedio.')
        bullet('Por eso Spock sólo funciona con clases compiladas por Groovy, y por')
        bullet('eso los Spec no llevan @CompileStatic.')
    }

    /**
     * La tabla where.
     */
    static void demoWhere() {
        section('Cinco tests en una tabla')

        bullet("@Unroll")
        bullet("def 'con stock #stock, pedir #pedido da #esperado'() {")
        bullet('    given:')
        bullet('    almacen.stockDe("LIB-1") >> stock')
        bullet('')
        bullet('    expect:')
        bullet('    carrito.añadir("LIB-1", pedido).class.simpleName == esperado')
        bullet('')
        bullet('    where:')
        bullet('    stock | pedido || esperado')
        bullet('    10    | 1      || "Agregado"')
        bullet('    10    | 11     || "SinStock"')
        bullet('    0     | 1      || "SinStock"')
        bullet('}')

        section('Qué hace Spock con eso')

        bullet('Ejecuta el test una vez POR FILA, con variables distintas.')
        bullet('Si una falla, las demás siguen: sabes exactamente qué combinación.')
        bullet('El `||` separa las entradas del resultado esperado. Es convención,')
        bullet('no sintaxis: el compilador acepta `|` en todas.')

        section('@Unroll y el nombre con #variables')

        bullet('Sin @Unroll: un test llamado "con stock #stock…" y cinco resultados.')
        bullet('Con @Unroll: cinco tests, cada uno con sus valores en el nombre:')
        bullet('  "con stock 10, pedir 11 da SinStock"')
        bullet('Desde Spock 2, se puede poner @Unroll en la clase entera.')

        section('Las otras formas del where')

        bullet('Una lista por variable, con <<:')
        bullet('  where: cantidad << [0, -1, -100]')
        bullet('')
        bullet('Varias a la vez, con desestructuración:')
        bullet('  where: [a, b] << [[1, 2], [3, 4]]')
        bullet('')
        bullet('Una variable derivada de otra:')
        bullet('  where:')
        bullet('  n << (1..5)')
        bullet('  cuadrado = n * n')

        section('Para qué sirve de verdad')

        bullet('Casos límite: 0, 1, el máximo, el máximo más uno, negativo, null.')
        bullet('Es donde están los bugs, y la tabla hace que añadir uno cueste')
        bullet('una línea en vez de un test entero.')
        show('en este repositorio', 'CarritoSpec tiene 3 tablas y genera 19 tests')
    }

    /**
     * Mocks, stubs y spies.
     */
    static void demoDoubles() {
        section('Los tres que trae Spock')

        bullet('Stub(Tipo)  responde lo que le digas. No comprueba nada.')
        bullet('Mock(Tipo)  además COMPRUEBA cómo se le llamó.')
        bullet('Spy(objeto) el objeto real, y además comprueba las llamadas.')

        section('Stub: dar respuestas')

        bullet('almacen.buscar("LIB-1") >> libro          // siempre')
        bullet('almacen.stockDe(_) >> 5                   // con cualquier argumento')
        bullet('almacen.stockDe("X") >>> [10, 0]          // 10 la 1ª vez, 0 la 2ª')
        bullet('almacen.buscar(_) >> { String r -> ... }  // calculada')
        bullet('almacen.buscar(_) >> { throw new … }      // lanzando')

        section('Mock: comprobar interacciones')

        bullet('1 * almacen.reservar("LIB-1", 2)   exactamente una vez, con eso')
        bullet('0 * almacen.reservar(_, _)          nunca')
        bullet('(1..3) * servicio.enviar(_)         entre una y tres veces')
        bullet('1 * _.metodo(_)                     en cualquier mock')
        bullet('0 * _                               NADA más que lo ya declarado')

        section('El comodín `_` y los argumentos')

        bullet('_            cualquier argumento')
        bullet('_ as String  cualquiera de ese tipo')
        bullet('!null        cualquiera que no sea null')
        bullet('{ it > 5 }   el que cumpla el closure')

        section('Cuál usar: la tabla')

        bullet('doble    cuándo')
        bullet('──────   ─────────────────────────────────────────────────')
        bullet('Stub     sólo necesitas que la dependencia devuelva algo')
        bullet('Mock     lo que pruebas ES la interacción')
        bullet('Spy      quieres el comportamiento real y comprobar llamadas')
        bullet('Fake     necesitas comportamiento real sin la infraestructura')
        bullet('Dummy    el parámetro hace falta pero no se usa')

        section('El error más común')

        bullet('Usar Mock por defecto y acabar comprobando CÓMO está hecho el')
        bullet('código en vez de QUÉ hace. Después, cualquier refactor rompe los')
        bullet('tests aunque el comportamiento no haya cambiado.')
        bullet('Regla: si la interacción no es el objetivo del test, usa Stub.')

        show('los cinco, cada uno con su Spec', 'src/test/groovy/…/DoblesSpec.groovy')
    }

    /**
     * Cómo es testeable el código.
     */
    static void demoTestableCode() {
        section('La decisión que hace posible todo lo anterior')

        bullet('class Carrito {')
        bullet('    Carrito(Almacen almacen) { this.almacen = almacen }')
        bullet('}')
        bullet('')
        bullet('El carrito RECIBE su dependencia; no la construye por dentro. Si')
        bullet('hiciera `new AlmacenSql()` en el constructor, no habría test posible')
        bullet('sin una base de datos.')

        section('Las cuatro señales de código difícil de testear')

        bullet('1. `new` de una dependencia pesada dentro del método.')
        bullet('2. Estado estático mutable.')
        bullet('3. Leer la hora, generar aleatorios o leer el entorno directamente.')
        bullet('4. Métodos que hacen cinco cosas.')

        section('Y sus arreglos')

        bullet('1. Pásala por el constructor.')
        bullet('2. Hazlo de instancia, o inyéctalo.')
        bullet('3. Inyecta un `Clock`, un `Random` con semilla, una configuración.')
        bullet('4. Pártelos.')

        section('Lo que este repositorio hace con sus propios tests')

        bullet('infra/SmokeSpec    ejecuta TODAS las demos y falla si alguna lanza')
        bullet('                   o no imprime nada')
        bullet('infra/RegistrySpec los capítulos van del 1 al último sin huecos,')
        bullet('                   sin identificadores repetidos y sin nada en blanco')
        bullet('infra/LauncherSpec los comandos del lanzador, contra capítulos')
        bullet('                   de mentira para que añadir uno real no lo rompa')
        bullet('c28spock/          el Carrito, con todo lo de este capítulo')

        section('El de humo es el que de verdad protege este repositorio')

        bullet('Sin él, estos ficheros serían código que COMPILA.')
        bullet('Con él, son código VERIFICADO: si una demo se rompe al editarla,')
        bullet('`./mvnw test` lo dice con el identificador exacto.')
        show('pruébalo', 'rompe una demo a propósito y ejecuta ./mvnw test')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Rompe una aserción de `CarritoSpec` y mira el power assert completo.
//  2. Añade una fila a la tabla de `con stock #stock…` y ejecuta sólo ese Spec.
//  3. Cambia un `Stub` por un `Mock` en `DoblesSpec` y añade `0 * _`: verás qué
//     interacciones no habías declarado.
//  4. Quita `@Unroll` y compara cómo sale el informe.
//  5. Haz que `Carrito` construya su propio `Almacen` y comprueba que los tests
//     dejan de ser posibles.
