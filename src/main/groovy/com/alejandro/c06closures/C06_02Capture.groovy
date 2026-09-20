package com.alejandro.c06closures

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  06.2 · El cierre: qué captura un closure y qué trampas tiene
//
//  QUÉ ES
//    "Closure" significa CIERRE: el bloque se lleva consigo el entorno donde se
//    escribió. Y se lleva la VARIABLE, no una copia de su valor.
//
//  POR QUÉ IMPORTA
//    De ahí sale su utilidad (un contador sin clase, una fábrica configurada) y sus dos
//    trampas: el closure ve los cambios posteriores, y si se crea dentro de un bucle
//    todos comparten la misma variable si no se tiene cuidado.
//
//  ERRORES COMUNES
//    · Crear closures en un bucle capturando la variable del bucle.
//    · Suponer que el closure "congela" el valor al escribirlo (eso lo hace el GString).
//    · Guardar closures que capturan objetos grandes y provocar fugas de memoria.
// =====================================================================================

class C06_02Capture {

    /**
     * Qué se captura: la variable, no el valor.
     */
    static void demoWhatIsCaptured() {
        section('El closure ve los cambios posteriores')

        int x = 1
        def leer = { x }
        show('antes', leer())
        x = 2
        show('tras x = 2', leer())
        bullet('Captura la VARIABLE. No hay copia.')

        section('Contraste: un GString sí congela el valor')

        int n = 1
        def texto = "n vale $n"
        def closure = { "n vale $n" }
        n = 99
        show('el GString', texto.toString())
        show('el closure', closure().toString())
        bullet('El GString evaluó `n` al construirse; el closure lo evalúa al llamarlo.')
        bullet('Es la diferencia entre guardar un valor y guardar código.')

        section('Y puede MODIFICAR lo capturado')

        int contador = 0
        def incrementar = { contador++ }
        incrementar(); incrementar(); incrementar()
        show('contador tras tres llamadas', contador)
        bullet('En Java esto no compila: allí lo capturado tiene que ser final.')
        bullet('Es más potente y más peligroso.')

        section('Qué captura exactamente')

        bullet('Variables locales del ámbito donde se escribió.')
        bullet('Los parámetros del método que lo contiene.')
        bullet('`this` (el objeto), a través de `thisObject`. Capítulo 07.')
    }

    /**
     * La trampa del bucle.
     */
    static void demoLoopTrap() {
        section('Closures creados en un `for` clásico: comparten la variable')

        List<Closure<Integer>> malos = []
        for (int i = 0; i < 3; i++) {
            malos << { i }
        }
        show('los tres devuelven', malos*.call())
        bullet('Los tres capturaron LA MISMA `i`, que al terminar el bucle vale 3.')

        section('Con `for (x in …)` pasa EXACTAMENTE lo mismo')

        List<Closure<Integer>> conForIn = []
        for (i in 0..<3) {
            conForIn << { i }
        }
        show('los tres devuelven', conForIn*.call())
        bullet('Aquí devuelven 2, el último valor que tomó `i`. La variable del')
        bullet('`for in` también es UNA sola para todo el bucle, igual que la del for')
        bullet('clásico. Que la sintaxis se parezca al for-each de Java no ayuda.')

        section('Con `each` SÍ funciona: el parámetro es local a cada llamada')

        List<Closure<Integer>> conEach = []
        (0..<3).each { valor -> conEach << { valor } }
        show('los tres devuelven', conEach*.call())

        bullet('Cada vuelta llama al closure de `each` con su propio parámetro, y ESE')
        bullet('es el que se captura. Por eso no se pisan.')

        section('La solución cuando necesitas un `for`')

        List<Closure<Integer>> arreglados = []
        for (int i = 0; i < 3; i++) {
            int copia = i             // una variable nueva en cada vuelta
            arreglados << { copia }
        }
        show('los tres devuelven', arreglados*.call())

        section('La regla')

        bullet('Si guardas closures dentro de un bucle, usa `each` (o `collect`).')
        bullet('Con cualquier `for`, clásico o `in`, copia la variable dentro del')
        bullet('cuerpo antes de capturarla.')
        bullet('Regla rápida: lo que se captura sin riesgo es un PARÁMETRO, no la')
        bullet('variable de un bucle.')
    }

    /**
     * Method pointers: convertir un método en closure.
     */
    static void demoMethodPointers() {
        section('`.&` convierte un método en un closure')

        def maximo = Math.&max
        show('maximo(3, 7)', maximo(3, 7))
        show('su clase', maximo.class.simpleName)

        section('`::` hace casi lo mismo (sintaxis de Java)')

        def maximoRef = Math::max
        show('maximoRef(3, 7)', maximoRef(3, 7))
        bullet('`.&` produce SIEMPRE un Closure; `::` produce un Closure o una')
        bullet('interfaz funcional, según el contexto. Para guardar en una variable')
        bullet('`def`, los dos valen.')

        section('Sobre una instancia')

        String texto = 'groovy'
        def enMayusculas = texto.&toUpperCase
        show('enMayusculas()', enMayusculas())

        section('Su uso real: reutilizar un método existente')

        show('collect con un método', ['a', 'bb'].collect(this.&describir))
        show('lo mismo con closure', ['a', 'bb'].collect { describir(it) })
        bullet('Con el method pointer no repites los parámetros. Es lo que usa este')
        bullet('repositorio para registrar las demos de cada capítulo.')

        section('Y se pueden componer y currificar como cualquier closure')

        def repetido = this.&repetir.curry(3)
        show('repetido("ab")', repetido('ab'))
    }

    private static String describir(String texto) { "$texto (${texto.length()})" }

    private static String repetir(int veces, String texto) { texto * veces }

    /**
     * Closures como predicados y funciones de primera clase.
     */
    static void demoAsValues() {
        section('Guardar criterios en un mapa')

        Map<String, Closure<Boolean>> reglas = [
            'mayor de edad': { Map u -> u.edad >= 18 },
            'tiene email'  : { Map u -> u.email },
            'nombre corto' : { Map u -> u.nombre.length() <= 4 },
        ]

        def usuario = [nombre: 'Ana', edad: 30, email: null]
        reglas.each { String nombre, Closure<Boolean> regla ->
            show(nombre, regla(usuario) ? 'cumple' : 'no cumple')
        }
        bullet('Un validador declarativo en seis líneas, sin una sola clase.')

        section('Combinar predicados')

        def esPar = { int n -> n % 2 == 0 }
        def esGrande = { int n -> n > 10 }
        def ambos = { int n -> esPar(n) && esGrande(n) }

        show('[4, 12, 15].findAll(esPar)', [4, 12, 15].findAll(esPar))
        show('[4, 12, 15].findAll(ambos)', [4, 12, 15].findAll(ambos))

        section('Pasar el closure donde se espera una interfaz')

        Runnable tarea = { println '    (soy un Runnable)' }
        tarea.run()
        bullet('Groovy convierte solo el closure a la interfaz si ésta tiene un')
        bullet('único método. Es la coerción SAM del capítulo 02.')

        section('Cuándo NO usar un closure')

        bullet('Si el bloque tiene más de diez líneas: extrae un método con nombre.')
        bullet('Si necesitas varios métodos relacionados: eso es una clase.')
        bullet('Si el closure captura media docena de variables: pásalas por parámetro.')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. En `demoLoopTrap`, quita la variable `copia` y comprueba que vuelve el problema.
//  2. Crea un closure que capture una lista grande, guárdalo en un campo estático y
//     piensa qué impide que esa lista se libere.
//  3. Cambia `Math.&max` por `Math::max` en todos los sitios y comprueba que funciona.
//  4. Añade una regla más al mapa de `demoAsValues` sin tocar el bucle que las aplica.
