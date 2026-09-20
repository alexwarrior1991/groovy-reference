package com.alejandro.c18exceptions

import groovy.transform.Canonical

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  18.1 · Excepciones: en Groovy ninguna es "checked"
//
//  QUÉ ES
//    Groovy mantiene toda la jerarquía de excepciones de Java, pero elimina la
//    obligación de declararlas o capturarlas: NINGUNA excepción es comprobada. Puedes
//    lanzar una `IOException` sin `throws` y sin que nadie proteste.
//
//  POR QUÉ IMPORTA
//    Se acabaron los `try/catch` vacíos puestos para que compile. Pero también
//    desaparece el recordatorio del compilador, así que hay que decidir a conciencia
//    qué puede fallar y dónde se trata.
//
//  ERRORES COMUNES
//    · Usar excepciones para el flujo normal (un dato no encontrado NO es excepcional).
//    · Capturar `Exception` y no hacer nada.
//    · Perder la causa original al relanzar.
// =====================================================================================

class C18_01Exceptions {

    /**
     * Lo que cambia respecto a Java.
     */
    static void demoNoChecked() {
        section('Un método que lanza IOException, sin `throws`')

        try {
            leerFicheroQueNoExiste()
        } catch (IOException e) {
            show('capturada', "${e.class.simpleName}: ${e.message}")
        }
        bullet('En Java, ese método NO compilaría sin `throws IOException`.')
        bullet('Y quien lo llama no está obligado a capturarla.')

        section('Lo bueno')

        bullet('Se acabaron los `catch (IOException e) { }` vacíos.')
        bullet('Y las firmas con cinco excepciones declaradas que nadie lee.')
        bullet('Los closures pueden lanzar lo que sea: en Java, una lambda no puede')
        bullet('lanzar una excepción comprobada sin envolverla.')

        show('un closure que lanza', conClosureQueLanza())

        section('Lo malo')

        bullet('Nadie te recuerda que ese método puede fallar.')
        bullet('La documentación (`@throws` en el GroovyDoc) pasa a ser importante.')
        bullet('Y hay que decidir DÓNDE se trata cada fallo, sin ayuda del compilador.')

        section('Sigue existiendo toda la jerarquía')

        bullet('Throwable')
        bullet('├── Error              no se captura: la JVM está rota')
        bullet('└── Exception')
        bullet('    └── RuntimeException')
        bullet('Groovy sólo quita la OBLIGACIÓN, no las clases.')
    }

    private static void leerFicheroQueNoExiste() {
        throw new IOException('no existe /ruta/inventada')
    }

    private static String conClosureQueLanza() {
        [1, 2, 3].collect {
            try {
                if (it == 2) throw new IOException('fallo en el 2')
                "ok $it"
            } catch (IOException e) {
                "recuperado: ${e.message}"
            }
        }.join(', ')
    }

    /**
     * try, catch, finally y multi-catch.
     */
    static void demoSyntax() {
        section('Multi-catch')

        show('con número', clasificar('42'))
        show('con texto', clasificar('abc'))
        show('con null', clasificar(null))
        bullet('`catch (NumberFormatException | NullPointerException e)` en una línea.')

        section('El orden importa: de específico a general')

        bullet('`catch (Exception e)` antes que `catch (IOException e)` deja el')
        bullet('segundo inalcanzable. En Groovy ni siquiera avisa.')

        section('finally se ejecuta pase lo que pase')

        show('camino normal', conFinally(false))
        show('camino con error', conFinally(true))
        bullet('Incluso con un `return` dentro del try, el `finally` corre antes')
        bullet('de devolver.')

        section('La trampa: un return en el finally se come el resultado')

        show('returnEnFinally()', returnEnFinally())
        bullet('Devuelve "del finally", no "del try". Y si el try hubiera lanzado,')
        bullet('la excepción se habría perdido. NUNCA pongas un return en un finally.')

        section('try-with-resources: en Groovy es `withCloseable`')

        show('con withCloseable', conWithCloseable())
        show('¿se cerró?', RecursoDePrueba.ultimoCerrado)
        bullet('`withCloseable` cierra el recurso pase lo que pase, como el')
        bullet('try-with-resources de Java pero sin sintaxis especial.')

        show('y si el bloque lanza, también cierra', cierraAunqueFalle())
        show('¿se cerró?', RecursoDePrueba.ultimoCerrado)
    }

    private static String clasificar(String entrada) {
        try {
            int n = Integer.parseInt(entrada)
            "número: $n"
        } catch (NumberFormatException | NullPointerException e) {
            "no es un número (${e.class.simpleName})"
        }
    }

    private static List<String> conFinally(boolean falla) {
        List<String> pasos = []
        try {
            pasos << 'try'
            if (falla) throw new IllegalStateException('roto')
            pasos << 'sin error'
        } catch (IllegalStateException e) {
            pasos << 'catch'
        } finally {
            pasos << 'finally'
        }
        pasos
    }

    @SuppressWarnings('ReturnInFinallyBlock')
    private static String returnEnFinally() {
        try {
            return 'del try'
        } finally {
            return 'del finally'
        }
    }

    private static String conWithCloseable() {
        new RecursoDePrueba('datos').withCloseable { RecursoDePrueba r -> r.leer() }
    }

    private static String cierraAunqueFalle() {
        try {
            new RecursoDePrueba('otro').withCloseable { RecursoDePrueba r ->
                throw new IllegalStateException('falló usando el recurso')
            }
        } catch (IllegalStateException e) {
            "capturada: ${e.message}"
        }
    }

    static class RecursoDePrueba implements Closeable {
        static String ultimoCerrado = '(ninguno)'

        final String nombre

        RecursoDePrueba(String nombre) {
            this.nombre = nombre
            ultimoCerrado = '(abierto, sin cerrar)'
        }

        String leer() { "leído de $nombre" }

        @Override
        void close() { ultimoCerrado = nombre }
    }

    /**
     * Excepciones propias, y cómo no perder la causa.
     */
    static void demoCustomExceptions() {
        section('Una excepción propia')

        try {
            validar([nombre: '', edad: -5])
        } catch (ValidacionException e) {
            show('mensaje', e.message)
            show('los errores, como DATOS', e.errores)
        }
        bullet('Llevar los datos del fallo en la excepción, y no sólo un texto,')
        bullet('es lo que permite tratarla de verdad en vez de sólo imprimirla.')

        section('Encadenar: NUNCA pierdas la causa')

        try {
            capaDeServicio()
        } catch (ServicioException e) {
            show('mensaje', e.message)
            show('causa', "${e.cause.class.simpleName}: ${e.cause.message}")
            show('causa de la causa', e.cause.cause?.message)
        }
        bullet('`new MiError(mensaje, e)` conserva el stack trace original.')
        bullet('Sin eso, el `catch` de arriba no sabe qué pasó de verdad.')

        section('La cadena entera')

        try {
            capaDeServicio()
        } catch (Exception e) {
            show('cadena de causas', cadenaDeCausas(e))
        }

        section('@InheritConstructors ahorra los cuatro constructores')

        bullet('Sin la anotación (capítulo 12) hay que escribir a mano:')
        bullet('  MiError(), MiError(String), MiError(String, Throwable), MiError(Throwable)')
        show('con la anotación', new ServicioException('directo').message)
    }

    private static void validar(Map datos) {
        List<String> errores = []
        if (!datos.nombre) errores << 'el nombre está vacío'
        if ((datos.edad as int) < 0) errores << 'la edad es negativa'
        if (errores) throw new ValidacionException(errores)
    }

    private static void capaDeDatos() {
        throw new IOException('la conexión se cerró')
    }

    private static void capaDeRepositorio() {
        try {
            capaDeDatos()
        } catch (IOException e) {
            throw new RepositorioException('no se pudo leer el usuario', e)
        }
    }

    private static void capaDeServicio() {
        try {
            capaDeRepositorio()
        } catch (RepositorioException e) {
            throw new ServicioException('falló al registrar el pedido', e)
        }
    }

    private static String cadenaDeCausas(Throwable e) {
        List<String> cadena = []
        Throwable actual = e
        while (actual != null) {
            cadena << "${actual.class.simpleName}(${actual.message})"
            actual = actual.cause
        }
        cadena.join(' → ')
    }

    /**
     * Cuándo NO usar una excepción.
     */
    static void demoErrorsAsData() {
        section('Un fallo ESPERADO no es una excepción')

        bullet('"el usuario no existe" en una búsqueda: esperado.')
        bullet('"el fichero de configuración está corrupto": excepcional.')
        bullet('La pregunta no es "¿es un error?", sino "¿lo esperaba quien llama?".')

        section('La versión con excepción, para lo esperado')

        show('buscando uno que existe', conExcepcion('ana'))
        show('buscando uno que no', conExcepcion('nadie'))
        bullet('Funciona, pero obliga a un try/catch para el caso NORMAL.')

        section('La versión con el resultado como dato')

        show('uno que existe', describir(buscar('ana')))
        show('uno que no', describir(buscar('nadie')))
        bullet('Aquí el `switch` trata los dos casos por igual: son datos.')
        bullet('Es el patrón sealed + record del capítulo 13.')

        section('Y encadenar sin try/catch')

        show('encadenado con éxito', procesar('ana'))
        show('encadenado con fallo', procesar('nadie'))

        section('Cuánto cuesta una excepción')

        int vueltas = 50_000
        long conExc = medir { vueltas.times { try { fallaSiempre() } catch (ignored) { } } }
        long conDato = medir { vueltas.times { buscar('nadie') } }
        show('lanzando y capturando', "${conExc} ms")
        show('devolviendo un dato', "${conDato} ms")
        bullet('Lo caro es rellenar el stack trace. Por eso no se usan excepciones')
        bullet('para el flujo normal: no es sólo estilo, se nota.')

        section('La regla')

        bullet('Excepción: algo que no debería pasar y que quien llama no puede')
        bullet('arreglar en ese momento.')
        bullet('Dato: algo previsto que forma parte del resultado.')
    }

    private static void fallaSiempre() { throw new IllegalStateException('x') }

    private static final Map<String, String> USUARIOS = [ana: 'Ana García', luis: 'Luis Pérez']

    private static String conExcepcion(String id) {
        try {
            String nombre = USUARIOS[id]
            if (!nombre) throw new NoSuchElementException("no existe el usuario $id")
            nombre
        } catch (NoSuchElementException e) {
            "error: ${e.message}"
        }
    }

    private static Resultado buscar(String id) {
        String nombre = USUARIOS[id]
        nombre ? new Encontrado(nombre) : new NoEncontrado(id)
    }

    private static String describir(Resultado r) {
        switch (r) {
            case Encontrado -> "encontrado: ${((Encontrado) r).nombre}"
            case NoEncontrado -> "no existe: ${((NoEncontrado) r).id}"
            default -> 'resultado desconocido'
        }
    }

    private static String procesar(String id) {
        Resultado r = buscar(id)
        r instanceof Encontrado
            ? "procesado ${((Encontrado) r).nombre.toUpperCase()}"
            : 'nada que procesar'
    }

    private static long medir(Closure<?> bloque) {
        bloque()
        long t0 = System.nanoTime()
        bloque()
        ((System.nanoTime() - t0) / 1_000_000L) as long
    }
}

/** Los errores viajan como datos, no sólo como texto. */
class ValidacionException extends RuntimeException {
    final List<String> errores

    ValidacionException(List<String> errores) {
        super("la validación falló: ${errores.size()} error(es)")
        this.errores = errores.asImmutable()
    }
}

@groovy.transform.InheritConstructors
class RepositorioException extends RuntimeException {}

@groovy.transform.InheritConstructors
class ServicioException extends RuntimeException {}

sealed interface Resultado permits Encontrado, NoEncontrado {}

@Canonical
class Encontrado implements Resultado {
    String nombre
}

@Canonical
class NoEncontrado implements Resultado {
    String id
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `, e` de `new ServicioException('…', e)` y mira qué le pasa a la cadena.
//  2. Cambia el orden de los `catch` poniendo `Exception` primero.
//  3. Sube las vueltas de la medición a 500.000 y compara otra vez.
//  4. Convierte `conExcepcion` para que devuelva un `Resultado` y compara los dos
//     estilos de llamada.
