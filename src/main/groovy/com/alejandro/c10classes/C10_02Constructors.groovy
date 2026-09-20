package com.alejandro.c10classes

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  10.2 · Constructores, argumentos con nombre y despacho de métodos
//
//  QUÉ ES
//    Groovy da gratis un constructor vacío y otro que acepta un mapa de propiedades.
//    Los "argumentos con nombre" no son sintaxis del lenguaje: son un mapa disfrazado.
//    Y la selección de método —esto es lo grande— se hace por el tipo EN EJECUCIÓN,
//    no por el tipo declarado como en Java.
//
//  POR QUÉ IMPORTA
//    Los multimétodos cambian el resultado de código que se ve idéntico al de Java.
//    Es una de las diferencias semánticas más importantes entre los dos lenguajes.
//
//  ERRORES COMUNES
//    · Creer que los argumentos con nombre comprueban algo: no comprueban nada.
//    · Escribir un constructor propio y perder el de mapa sin darse cuenta.
//    · Portar de Java una jerarquía que dependía del despacho estático.
// =====================================================================================

class C10_02Constructors {

    /**
     * Los constructores que salen gratis.
     */
    static void demoGeneratedConstructors() {
        section('Sin escribir ninguno, tienes dos')

        show('new Punto()', new Punto())
        show('new Punto(x: 1, y: 2)', new Punto(x: 1, y: 2))
        show('parcial: new Punto(x: 1)', new Punto(x: 1))
        bullet('El de mapa asigna propiedad a propiedad. Lo que no pongas, por defecto.')

        section('Pero si escribes UNO, pierdes los dos')

        show('new ConConstructor(1, 2)', new ConConstructor(1, 2))
        try {
            show('new ConConstructor(a: 1, b: 2)', new ConConstructor(a: 1, b: 2))
        } catch (Exception e) {
            show('new ConConstructor(a: 1, b: 2)', e.class.simpleName)
        }
        bullet('Al declarar un constructor, el de mapa deja de generarse.')
        bullet('Si lo quieres de vuelta: @TupleConstructor o @MapConstructor (cap. 12).')

        section('El constructor de mapa NO valida las claves')

        try {
            show('new Punto(inventado: 1)', new Punto(inventado: 1))
        } catch (Exception e) {
            show('new Punto(inventado: 1)', "${e.class.simpleName}: ${e.message?.take(45)}")
        }
        bullet('Aquí sí lanza, porque busca la propiedad y no existe.')
    }

    static class Punto {
        int x
        int y

        @Override
        String toString() { "($x, $y)" }
    }

    static class ConConstructor {
        int a
        int b

        ConConstructor(int a, int b) { this.a = a; this.b = b }

        @Override
        String toString() { "[$a, $b]" }
    }

    /**
     * Argumentos con nombre: un mapa disfrazado.
     */
    static void demoNamedArguments() {
        section('Un método que "tiene argumentos con nombre"')

        show('crear(nombre: "Ana", edad: 30)', crear(nombre: 'Ana', edad: 30))
        show('en otro orden', crear(edad: 30, nombre: 'Ana'))
        show('sólo uno', crear(nombre: 'Ana'))

        section('En realidad recibe UN mapa')

        show('lo que llega', queLlega(nombre: 'Ana', edad: 30))
        bullet('`f(a: 1, b: 2)` es `f([a: 1, b: 2])`. Nada más.')

        section('Por eso se pueden mezclar con argumentos normales…')

        show('con posicional al final', conMezcla('obligatorio', color: 'rojo'))
        bullet('El mapa va PRIMERO en la firma, aunque se escriba al final.')

        section('…y por eso no se comprueba nada')

        show('una clave con errata', crear(nombe: 'Ana'))
        bullet('Ni aviso ni error: la clave simplemente no se usa.')
        bullet('Es el precio de que sea un mapa. Valida tú si importa:')
        show('con validación', crearValidando(nombre: 'Ana'))
        try {
            show('con errata y validación', crearValidando(nombe: 'Ana'))
        } catch (IllegalArgumentException e) {
            show('con errata y validación', e.message)
        }

        section('Parámetros con valor por defecto')

        show('saludar("Ana")', saludar('Ana'))
        show('saludar("Ana", "Buenas")', saludar('Ana', 'Buenas'))
        bullet('Groovy genera una sobrecarga por cada combinación. Las ve Java también.')
        show('sobrecargas generadas', C10_02Constructors.class.methods.count { it.name == 'saludar' })
    }

    private static String crear(Map opciones) { "${opciones.nombre} (${opciones.edad})" }

    private static String queLlega(Map opciones) { "${opciones.getClass().simpleName}: $opciones" }

    private static String conMezcla(Map opciones, String obligatorio) { "$obligatorio $opciones" }

    private static String crearValidando(Map opciones) {
        Set<String> permitidas = ['nombre', 'edad'] as Set
        Set<String> desconocidas = opciones.keySet() - permitidas
        if (desconocidas) throw new IllegalArgumentException("opciones desconocidas: $desconocidas")
        "${opciones.nombre}"
    }

    static String saludar(String nombre, String saludo = 'Hola') { "$saludo, $nombre" }

    /**
     * Multimétodos: el despacho es dinámico.
     */
    static void demoMultimethods() {
        section('Dos sobrecargas y una variable declarada Object')

        Object valor = 'soy un texto'
        show('describir(valor)', describir(valor))
        bullet('Groovy elige por el tipo REAL del objeto: String.')
        bullet('Java elegiría por el tipo DECLARADO de la variable: Object.')

        section('Es la diferencia semántica más grande con Java')

        List<Object> mezcla = [1, 'texto', 3.5, [1, 2], null]
        show('cada uno a su sobrecarga', mezcla.collect { describir(it) })
        bullet('Sin un solo `instanceof`: lo resuelve el despacho.')

        section('Dónde esto ayuda')

        bullet('Sustituye cadenas de `if (x instanceof …)` por sobrecargas.')
        bullet('Es la base del patrón visitante sin ceremonia.')

        section('Y dónde muerde')

        bullet('Al portar código de Java que CONTABA con el despacho estático.')
        bullet('Con null, Groovy elige la sobrecarga más específica que lo admita.')
        show('describir(null)', describir(null))

        section('Y si dos sobrecargas empatan, lanza')

        try {
            show('ambiguo(null)', ambiguo(null))
        } catch (Exception e) {
            show('ambiguo(null)', e.class.simpleName)
        }
        bullet('Cuando pase, desambigua con un cast: `ambiguo((String) null)`.')
        show('ambiguo((String) null)', ambiguo((String) null))
    }

    private static String describir(String s) { 'un texto' }

    private static String describir(Integer i) { 'un entero' }

    private static String describir(Number n) { 'otro número' }

    private static String describir(List l) { "una lista de ${l.size()}" }

    private static String describir(Object o) { o == null ? 'un null' : 'algo' }

    private static String ambiguo(String s) { 'String' }

    private static String ambiguo(Integer i) { 'Integer' }

    /**
     * Orden de inicialización.
     */
    static void demoInitialization() {
        section('En qué orden ocurre todo')

        Registro.pasos.clear()
        new Hija(3)
        show('orden', Registro.pasos)

        bullet('1. inicializadores estáticos (una sola vez, al cargar la clase)')
        bullet('2. constructor de la PADRE')
        bullet('3. los inicializadores de los campos de la hija, en orden de declaración')
        bullet('4. cuerpo del constructor de la hija')

        section('Un detalle de sintaxis: Groovy no tiene bloques de inicialización')

        bullet('En Java se puede escribir un `{ ... }` suelto dentro de la clase.')
        bullet('En Groovy eso se interpreta como un CLOSURE, y casi siempre acaba en')
        bullet('un MissingMethodException raro. Si necesitas lógica de inicialización,')
        bullet('ponla en el constructor.')

        section('El orden de declaración NO importa dentro de la misma clase')

        show('campo usado antes de declararlo', new Orden().calculado)
        bullet('Todos los inicializadores de campo corren antes del cuerpo del')
        bullet('constructor, aunque el campo esté declarado más abajo. Aquí vale 20.')

        section('LA TRAMPA DE VERDAD: llamar a un método redefinible desde el constructor')

        Registro.pasos.clear()
        new Derivada()
        show('lo que vio la base', Registro.pasos)
        show('lo que se ve ya construido', new Derivada().describir())
        bullet('El constructor de la base corre ANTES de los campos de la derivada.')
        bullet('Si llama a un método que la derivada redefine, ese método ve los')
        bullet('campos todavía a null. Y no hay aviso de ninguna clase.')

        section('Cómo evitarlo')

        bullet('No llames a métodos redefinibles (no privados, no final) desde un')
        bullet('constructor.')
        bullet('Si necesitas el dato en la base, pásalo por el constructor:')
        show('pasándolo por el constructor', new DerivadaCorrecta().vistoPorLaBase)
    }

    static class Registro {
        static final List<String> pasos = []
    }

    static class Padre {
        Padre() { Registro.pasos << 'constructor de Padre' }
    }

    static class Hija extends Padre {
        String primero = registrar('primer campo de Hija')
        String segundo = registrar('segundo campo de Hija')

        Hija(int n) {
            super()
            Registro.pasos << 'cuerpo del constructor de Hija'
        }

        private static String registrar(String paso) {
            Registro.pasos << paso
            paso
        }
    }

    /** El orden de declaración no importa: `base` ya vale 10 en el constructor. */
    static class Orden {
        int calculado

        Orden() { calculado = base * 2 }

        int base = 10
    }

    static class Base {
        Base() { Registro.pasos << "la base ve: ${describir()}".toString() }

        String describir() { 'base' }
    }

    /** El clásico: la base llama a un método que esta clase redefine. */
    static class Derivada extends Base {
        String etiqueta = 'ya inicializada'

        @Override
        String describir() { "derivada, etiqueta=$etiqueta" }
    }

    static class BaseCorrecta {
        final String vistoPorLaBase

        /** El dato llega por parámetro, no por un método redefinible. */
        BaseCorrecta(String etiqueta) { vistoPorLaBase = "la base ve: $etiqueta" }
    }

    static class DerivadaCorrecta extends BaseCorrecta {
        DerivadaCorrecta() { super('ya inicializada') }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `@TupleConstructor` a `ConConstructor` y comprueba que vuelve el de mapa.
//  2. Declara `String valor = 'x'` en vez de `Object valor` y mira si cambia la
//     sobrecarga elegida (no debería: el objeto sigue siendo el mismo).
//  3. Mueve `int base = 10` encima del constructor de `Problematica` y vuelve a mirar.
//  4. Añade una sobrecarga `describir(CharSequence)` y decide cuál gana con un String.
