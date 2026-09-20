package com.alejandro.c30exercises

import groovy.transform.Canonical

import static com.alejandro.c30exercises.Formato.enunciado
import static com.alejandro.c30exercises.Formato.explicacion
import static com.alejandro.c30exercises.Formato.pistas
import static com.alejandro.c30exercises.Formato.solucionEnMarcha
import static com.alejandro.c30exercises.Formato.testEn
import static com.alejandro.c30exercises.Formato.varianteDificil
import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.show

// =====================================================================================
//  Ejercicio 3 · Un DSL de validación                                    🔴 difícil
//
//  Repasa: closures con delegate, @DelegatesTo, DELEGATE_ONLY, builders, errores
//  como datos.
//  Capítulos: 06-07 (closures), 24 (DSLs), 18 (errores como datos).
// =====================================================================================

@Canonical
class ErrorValidacion {
    String campo
    String mensaje
}

@Canonical
class ResultadoValidacion {
    List<ErrorValidacion> errores

    boolean isValido() { errores.isEmpty() }

    /** Groovy Truth: un resultado válido es "cierto" (capítulo 05). */
    boolean asBoolean() { valido }

    Map<String, List<String>> porCampo() {
        errores.groupBy { it.campo }.collectEntries { c, lista -> [c, lista*.mensaje] }
    }
}

/** Las reglas de UN campo. Su propia clase, para que el ámbito se pueda cerrar. */
class CampoBuilder {
    final String nombre
    private final List<Closure<ErrorValidacion>> reglas = []

    CampoBuilder(String nombre) { this.nombre = nombre }

    void obligatorio(String mensaje = 'es obligatorio') {
        reglas << { Object valor -> valor ? null : new ErrorValidacion(nombre, mensaje) }
    }

    void longitudMinima(int minimo, String mensaje = null) {
        reglas << { Object valor ->
            valor != null && valor.toString().length() < minimo
                ? new ErrorValidacion(nombre, mensaje ?: "necesita al menos $minimo caracteres")
                : null
        }
    }

    void entre(int minimo, int maximo, String mensaje = null) {
        reglas << { Object valor ->
            Integer n = valor instanceof Number ? (valor as Integer) : null
            n != null && n !in minimo..maximo
                ? new ErrorValidacion(nombre, mensaje ?: "tiene que estar entre $minimo y $maximo")
                : null
        }
    }

    void formato(String patron, String mensaje = 'no tiene el formato correcto') {
        reglas << { Object valor ->
            valor != null && !(valor.toString() ==~ patron)
                ? new ErrorValidacion(nombre, mensaje)
                : null
        }
    }

    /** La regla de escape: cualquier condición. */
    void cumple(String mensaje, Closure<Boolean> condicion) {
        reglas << { Object valor ->
            condicion(valor) ? null : new ErrorValidacion(nombre, mensaje)
        }
    }

    List<ErrorValidacion> aplicar(Object valor) {
        reglas.collect { it(valor) }.findAll { it != null } as List<ErrorValidacion>
    }
}

class ValidadorBuilder {
    private final Map<String, CampoBuilder> campos = [:]

    void campo(String nombre,
               @DelegatesTo(value = CampoBuilder, strategy = Closure.DELEGATE_ONLY) Closure bloque) {
        def builder = campos.computeIfAbsent(nombre) { new CampoBuilder(nombre) }
        bloque.delegate = builder
        // DELEGATE_ONLY: dentro de `campo { }` sólo existen las reglas (capítulo 24).
        bloque.resolveStrategy = Closure.DELEGATE_ONLY
        bloque()
    }

    Validador construir() {
        if (campos.isEmpty()) throw new IllegalStateException('un validador sin campos no valida nada')
        new Validador(campos.asImmutable())
    }
}

class Validador {
    private final Map<String, CampoBuilder> campos

    Validador(Map<String, CampoBuilder> campos) { this.campos = campos }

    ResultadoValidacion validar(Map<String, Object> datos) {
        List<ErrorValidacion> errores = campos.collectMany { String nombre, CampoBuilder builder ->
            builder.aplicar(datos[nombre])
        }
        new ResultadoValidacion(errores.asImmutable())
    }

    static Validador definir(
        @DelegatesTo(value = ValidadorBuilder, strategy = Closure.DELEGATE_FIRST) Closure bloque) {
        def builder = new ValidadorBuilder()
        bloque.delegate = builder
        bloque.resolveStrategy = Closure.DELEGATE_FIRST
        bloque()
        builder.construir()
    }
}

class E03MiniDsl {

    static void ejecutar() {
        enunciado(
            'Escribe un DSL para declarar validaciones que se lea así:',
            '',
            '    def validador = Validador.definir {',
            '        campo("nombre") {',
            '            obligatorio()',
            '            longitudMinima(3)',
            '        }',
            '        campo("edad") {',
            '            obligatorio()',
            '            entre(18, 120)',
            '        }',
            '    }',
            '',
            'Requisitos:',
            '· `validar(mapa)` devuelve TODOS los errores, no sólo el primero.',
            '· Un resultado válido tiene que ser "cierto" para un `if`.',
            '· Dentro de `campo { }` NO se debe poder llamar a `campo(...)` otra vez.',
            '· Tiene que haber una regla de escape para condiciones a medida.',
        )

        pistas(
            'El mecanismo son tres líneas: delegate, resolveStrategy y llamar (cap. 07).',
            'Una CLASE por tipo de bloque: ValidadorBuilder y CampoBuilder (cap. 24).',
            'Para cerrar el ámbito del bloque interior: DELEGATE_ONLY.',
            'Cada regla puede ser un closure que devuelve un error o null.',
            'Para que el resultado sea "cierto", implementa asBoolean() (cap. 05).',
        )

        solucionEnMarcha()

        def validador = Validador.definir {
            campo('nombre') {
                obligatorio()
                longitudMinima(3)
            }
            campo('edad') {
                obligatorio()
                entre(18, 120)
            }
            campo('email') {
                obligatorio()
                formato(/^[\w.+-]+@[\w-]+\.[\w.]{2,}$/)
            }
            campo('alias') {
                cumple('no puede contener espacios') { v -> v == null || !v.toString().contains(' ') }
            }
        }

        def correcto = [nombre: 'Ana García', edad: 30, email: 'ana@ejemplo.com', alias: 'ana_g']
        def resultado = validador.validar(correcto)
        show('datos correctos → válido', resultado.valido)
        show('y es "cierto" para un if', resultado ? 'cierto' : 'falso')

        def malos = [nombre: 'Al', edad: 15, email: 'no-es-un-email', alias: 'con espacios']
        def conErrores = validador.validar(malos)
        show('datos malos → válido', conErrores.valido)
        show('cuántos errores', conErrores.errores.size())
        bullet('Los errores, agrupados por campo:')
        conErrores.porCampo().each { String campo, List<String> mensajes ->
            show("  $campo", mensajes)
        }

        show('campo ausente', validador.validar([:]).errores.size())

        show('el ámbito está cerrado', elAmbitoEstaCerrado())
        show('un validador vacío se rechaza', unValidadorVacio())

        explicacion(
            'Hay DOS builders, uno por tipo de bloque. Eso es lo que hace que',
            '`obligatorio()` sólo exista dentro de `campo { }` y `campo(...)` sólo',
            'en el nivel de arriba.',
            '',
            'El bloque interior usa DELEGATE_ONLY, así que la fuga de ámbito del',
            'capítulo 24 no puede ocurrir: escribir `campo(...)` dentro de otro',
            '`campo { }` lanza en vez de hacer algo raro en silencio.',
            '',
            'Cada regla es un closure que devuelve un ErrorValidacion o null, así que',
            'añadir una regla nueva es añadir un método a CampoBuilder. Y `cumple` es',
            'la regla de escape: cualquier condición, sin tocar el DSL.',
            '',
            'El resultado lleva TODOS los errores como datos y tiene `asBoolean()`,',
            'así que encaja en un `if` y en un `?:` como cualquier otra cosa.',
            '',
            'Una decisión que conviene entender: las reglas de FORMA (longitud, rango,',
            'patrón) se saltan los nulos a propósito. Un campo ausente da UN error',
            '("es obligatorio"), no uno por cada regla. Así un campo opcional puede',
            'tener reglas de forma sin ser obligatorio, y los mensajes no se apilan.',
        )

        varianteDificil(
            'Haz que las reglas se puedan reutilizar entre validadores:',
            '    def reglasDeUsuario = { campo("nombre") { obligatorio() } }',
            '    Validador.definir { incluir reglasDeUsuario; campo("extra") { } }',
            '',
            'Añade validación CRUZADA entre campos: "fechaFin posterior a fechaInicio".',
            'Pista: necesitas que la regla reciba el mapa entero, no sólo un valor.',
            '',
            'Haz que el validador se pueda definir desde un fichero de texto con',
            'GroovyShell y un SecureASTCustomizer (capítulo 25). Y después decide si',
            'de verdad quieres eso en producción.',
            '',
            'Y escribe los Spec con tablas `where:` para cada regla (capítulo 28).',
        )

        testEn('MiniDslSpec.groovy')
    }

    private static String elAmbitoEstaCerrado() {
        try {
            Validador.definir {
                campo('a') {
                    campo('b') { obligatorio() }     // no debería poder
                }
            }
            'la fuga ocurrió'
        } catch (Exception e) {
            "rechazado con ${e.class.simpleName}"
        }
    }

    private static String unValidadorVacio() {
        try {
            Validador.definir { }
            'se permitió'
        } catch (IllegalStateException e) {
            e.message
        }
    }
}
