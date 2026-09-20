package com.alejandro.c29traps

import groovy.transform.CompileStatic

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.quoted
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  29.1 · Las trampas, todas juntas
//
//  QUÉ ES
//    El repaso de las quince cosas que más tiempo cuestan en Groovy. Todas han
//    aparecido ya en su capítulo; aquí están juntas, ejecutándose, para poder volver
//    a este fichero cuando algo no haga lo que parece.
//
//  POR QUÉ IMPORTA
//    Casi todas tienen la misma forma: el código COMPILA, no lanza, y hace algo
//    distinto de lo que parece. Son las que no encuentra el compilador.
//
//  CÓMO USARLO
//    Ejecuta la demo, y cuando reconozcas un síntoma, vete al capítulo que se cita.
// =====================================================================================

class C29_01Traps {

    /**
     * Las que dan un valor equivocado sin avisar.
     */
    static void demoSilentlyWrong() {
        section('1 · El elvis sobre un cero legítimo (cap. 04, 05)')

        int temperatura = 0
        show('temperatura ?: 20', temperatura ?: 20)
        show('lo correcto', temperatura != null ? temperatura : 20)
        bullet('`?:` mira el Groovy Truth, no el null. Con contadores, precios,')
        bullet('temperaturas y cantidades, esto es un bug.')

        section('2 · GString como clave de mapa (cap. 03)')

        def nombre = 'Ana'
        GString clave = "Hola $nombre"
        Map<Object, String> mapa = [:]
        mapa.put(clave, 'guardado')
        show('mapa.get("Hola Ana")', mapa.get('Hola Ana'))
        mapa.put('Hola Ana', 'otro')
        show('claves, impresas', mapa.keySet().collect { quoted(it.toString()) })
        bullet('Dos claves idénticas a la vista. `mapa[k] = v` te salva; `put`, no.')

        section('3 · remove() borra por índice (cap. 08)')

        def numeros = [10, 20, 30]
        numeros.remove(1)
        show('[10,20,30].remove(1)', numeros)
        show('lo correcto', quitarPorValor([10, 20, 30], 20))

        section('4 · sort() muta la lista que te pasaron (cap. 08)')

        def original = [3, 1, 2]
        ordenarMal(original)
        show('la lista de quien llamó, tras ordenarMal', original)
        def otra = [3, 1, 2]
        ordenarBien(otra)
        show('tras ordenarBien', otra)

        section('5 · La variable de bucle capturada (cap. 06)')

        List<Closure<Integer>> closures = []
        for (i in 0..<3) closures << { i }
        show('los tres devuelven', closures*.call())
        bullet('Con `each` sí funciona, porque se captura un PARÁMETRO.')

        section('6 · [variable: 1] usa la palabra como clave (cap. 09)')

        def laClave = 'dinamica'
        show('[laClave: 1]', [laClave: 1])
        show('[(laClave): 1]', [(laClave): 1])

        section('7 · mapa.class no es la clase (cap. 09)')

        show('[a: 1].class', [a: 1].class)
        show('[a: 1].getClass()', [a: 1].getClass().simpleName)
        show('[a: 1].size  (busca la clave)', [a: 1].size)
        show('[a: 1].size()', [a: 1].size())
    }

    private static List<Integer> quitarPorValor(List<Integer> lista, int valor) {
        lista.removeElement(valor)
        lista
    }

    private static void ordenarMal(List<Integer> lista) { lista.sort() }

    private static void ordenarBien(List<Integer> lista) { lista.toSorted() }

    /**
     * Las que fallan en ejecución, lejos de donde está el problema.
     */
    static void demoLateFailures() {
        section('8 · La asignación coerciona, el despacho no (cap. 02)')

        show('int x = 3.9', truncado())
        mostrandoElFallo("int x = '42'") { int x = '42'; x }
        mostrandoElFallo('recibeInt(7L)') { recibeInt(7L) }
        bullet('La ASIGNACIÓN convierte números y produce cadenas. El DESPACHO de')
        bullet('métodos no convierte nada: elige, y si no encaja, lanza.')

        section('9 · [].sum() es null, no 0 (cap. 08)')

        show('[].sum()', [].sum())
        mostrandoElFallo('[].sum() + 1') { [].sum() + 1 }
        show('[].sum(0)', [].sum(0))

        section('10 · Una clave que no existe devuelve null (cap. 09)')

        def config = [hots: 'localhost']        // errata a propósito
        show('config.host', config.host)
        bullet('La errata no da ni un aviso. Es el precio de que un mapa sea un mapa.')
        bullet('Por eso los datos que VIVEN en el programa van en POGOs (cap. 10).')

        section('11 · Un nodo XML que no existe tampoco (cap. 23)')

        def xml = new groovy.xml.XmlSlurper().parseText('<a><b>texto</b></a>')
        show('xml.b.text()', xml.b.text())
        show('xml.noExiste.text()', "'${xml.noExiste.text()}'")
        show('pero xml.noExiste.size()', xml.noExiste.size())

        section('12 · get(clave, defecto) INSERTA (cap. 09)')

        def mapa = [a: 1]
        mapa.get('b', 99)
        show('el mapa tras un get', mapa)
    }

    private static int truncado() { int x = 3.9; x }

    private static int recibeInt(int n) { n }

    private static void mostrandoElFallo(String etiqueta, Closure<?> bloque) {
        try {
            show(etiqueta, bloque.call())
        } catch (Throwable e) {
            show(etiqueta, e.class.simpleName)
        }
    }

    /**
     * Las que sólo aparecen en producción.
     */
    static void demoProductionTraps() {
        section('13 · Una categoría no cruza al hilo (cap. 17, 27)')

        String resultado = 'pendiente'
        use(Duplicadora) {
            Thread hilo = Thread.start {
                try {
                    resultado = 'x'.duplicado()
                } catch (MissingMethodException e) {
                    resultado = 'el otro hilo NO la ve'
                }
            }
            hilo.join()
        }
        show('desde otro hilo', resultado)
        bullet('En desarrollo, con un hilo, funciona. En producción, con un pool, no.')

        section('14 · valor++ no es atómico (cap. 27)')

        def contador = new Contador()
        List<Thread> hilos = (1..4).collect {
            Thread.start { 1000.times { contador.incrementar() } }
        }
        hilos*.join()
        show('esperado', 4000)
        show('obtenido', contador.valor)

        section('15 · El switch sobre un sealed no es exhaustivo (cap. 13)')

        show('un caso sin tratar, sin default', sinDefault(new Triangulo()))
        bullet('Devuelve null en silencio. En Java 21 no compilaría.')

        section('16 · Serializar un objeto entero (cap. 22)')

        def usuario = new UsuarioConSecreto(usuario: 'ana', contraseña: 'secreta')
        show('toJson sin pensar', groovy.json.JsonOutput.toJson(usuario))
        bullet('Así es como las contraseñas acaban en los logs y en las respuestas.')

        section('17 · Un @EqualsAndHashCode mutable como clave (cap. 12)')

        def clave = new ClaveMutable(id: 1)
        Map<ClaveMutable, String> mapa = [:]
        mapa[clave] = 'guardado'
        clave.id = 2
        show('mapa[clave] tras cambiarle el id', mapa[clave])
        show('pero el mapa tiene', mapa.size())
        bullet('La entrada existe y es inalcanzable. Ni siquiera el propio objeto')
        bullet('que la metió la encuentra.')
    }

    private static String sinDefault(Forma f) {
        switch (f) {
            case Circulo -> 'círculo'
            case Cuadrado -> 'cuadrado'
        }
    }

    static class Contador {
        int valor = 0

        void incrementar() { valor = valor + 1 }
    }

    /**
     * Rendimiento: dónde se va el tiempo de verdad.
     */
    static void demoPerformance() {
        section('Lo que cuesta el despacho dinámico (cap. 15)')

        int vueltas = 2_000_000
        long dinamico = medir { bucleDinamico(vueltas) }
        long estatico = medir { bucleEstatico(vueltas) }
        show('dinámico', "${dinamico} ms")
        show('con @CompileStatic', "${estatico} ms")

        section('Lo que cuesta lanzar una excepción (cap. 18)')

        long lanzando = medir { 30_000.times { try { falla() } catch (ignored) { } } }
        long devolviendo = medir { 30_000.times { noFalla() } }
        show('lanzando', "${lanzando} ms")
        show('devolviendo un dato', "${devolviendo} ms")

        section('Lo que cuesta buscar en una lista (cap. 08)')

        def lista = (1..20_000).toList()
        def conjunto = lista as Set
        long enLista = medir { 3_000.times { 19_999 in lista } }
        long enSet = medir { 3_000.times { 19_999 in conjunto } }
        show('en List', "${enLista} ms")
        show('en Set', "${enSet} ms")

        section('El orden en que conviene mirar')

        bullet('1. ALGORITMO: un `in` sobre una lista dentro de un bucle es O(n²).')
        bullet('   Esto es lo que cuesta órdenes de magnitud.')
        bullet('2. E/S: una consulta por elemento en vez de una consulta.')
        bullet('3. Excepciones en el flujo normal.')
        bullet('4. Recompilar o volver a buscar lo que no cambia (regex, scripts).')
        bullet('5. Y SÓLO ENTONCES, @CompileStatic en los bucles calientes.')

        section('Lo que NO suele ser el problema')

        bullet('Usar `def` en vez de tipos: lo caro es el despacho, no la palabra.')
        bullet('Los closures: se compilan a clases, no son interpretados.')
        bullet('El GDK: `findAll` es un bucle, igual que el que escribirías tú.')
        bullet('BigDecimal, salvo en cálculo numérico masivo.')

        section('La regla')

        bullet('Mide antes de optimizar y mide después. En un servicio que habla')
        bullet('con una base de datos, todo lo de arriba es ruido comparado con')
        bullet('una consulta de más.')
    }

    private static void falla() { throw new IllegalStateException('x') }

    private static String noFalla() { 'x' }

    private static long bucleDinamico(int vueltas) {
        def total = 0
        for (def i = 0; i < vueltas; i++) total = total + i % 7
        total
    }

    @CompileStatic
    private static long bucleEstatico(int vueltas) {
        long total = 0
        for (int i = 0; i < vueltas; i++) total = total + i % 7
        total
    }

    private static long medir(Closure<?> bloque) {
        bloque()
        long t0 = System.nanoTime()
        bloque()
        ((System.nanoTime() - t0) / 1_000_000L) as long
    }

    /**
     * La lista de comprobación.
     */
    static void demoChecklist() {
        section('Antes de dar por terminado un código Groovy')

        bullet('¿Hay algún `?:` sobre algo que puede valer 0, "" o []?')
        bullet('¿Algún GString guardado en un mapa, un Set o comparado con equals?')
        bullet('¿Algún `remove(n)` sobre una lista de enteros?')
        bullet('¿Ordenas o modificas una colección que te pasaron por parámetro?')
        bullet('¿Guardas closures creados dentro de un `for`?')
        bullet('¿Alguna clave de mapa construida a partir de una variable?')
        bullet('¿Algún objeto mutable usado como clave?')
        bullet('¿Serializas objetos enteros a JSON sin decidir qué sale?')
        bullet('¿Algún `switch` sobre un sealed sin `default`?')
        bullet('¿Usas categorías o metaclases en código con hilos?')
        bullet('¿Cierras todos los ExecutorService?')
        bullet('¿Hay un `contains`/`in` sobre una lista dentro de un bucle?')

        section('Y las tres preguntas de diseño')

        bullet('¿Esto es un mapa porque VIENE de fuera, o porque me dio pereza')
        bullet('escribir la clase? (capítulo 10)')
        bullet('¿Este fallo lo espera quien me llama? Si sí, es un dato, no una')
        bullet('excepción. (capítulo 18)')
        bullet('¿Esta metaprogramación la resolvía un trait o @Delegate?')
        bullet('(capítulos 11, 12, 16)')

        section('Dónde volver')

        bullet('Los tres capítulos a los que más se vuelve: 03 (GString),')
        bullet('06-07 (closures) y 12 (transformaciones AST).')
        bullet('Los tres que más cuesta que hagan clic: 07 (delegate), 16 (MOP)')
        bullet('y 24 (DSLs). Son el mismo mecanismo visto tres veces.')
    }
}

@Category(String)
class Duplicadora {
    String duplicado() { this * 2 }
}

sealed interface Forma permits Circulo, Cuadrado, Triangulo {}

class Circulo implements Forma {}

class Cuadrado implements Forma {}

class Triangulo implements Forma {}

class UsuarioConSecreto {
    String usuario
    String contraseña
}

@groovy.transform.EqualsAndHashCode
class ClaveMutable {
    int id
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Recorre la lista de comprobación sobre el último código Groovy que escribiste.
//  2. Sube las vueltas de las mediciones y mira cuáles escalan y cuáles no.
//  3. Coge la trampa que menos esperabas y vete a su capítulo a releerla.
//  4. Escribe un test de Spock que demuestre una de estas trampas.
