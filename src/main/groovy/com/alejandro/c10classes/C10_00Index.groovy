package com.alejandro.c10classes

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 10 · Clases y POGOs
 *
 * Una clase de Groovy es una clase de Java con dos reglas añadidas: un campo sin
 * modificador de acceso se convierte en **propiedad** con accesores generados, y la
 * selección de método se hace con el tipo **en ejecución**, no con el declarado.
 *
 * ## Las dos reglas, en cuatro líneas
 * ```
 * class Usuario { String nombre }   // campo privado + getNombre() + setNombre()
 * usuario.nombre                    // llama a getNombre()
 *
 * Object o = 'texto'
 * describir(o)                      // elige describir(String), no describir(Object)
 * ```
 *
 * ## Qué se cubre
 * - Qué genera Groovy a partir de un POGO, comprobado por reflexión.
 * - Cómo cambia todo el modificador de acceso, y `@PackageScope`.
 * - Accesores propios, `.@` para saltarse el getter, y propiedades calculadas.
 * - POGO frente a mapa: cuándo cada uno y dónde va la frontera.
 * - Los dos constructores que salen gratis, y cómo se pierden al escribir uno.
 * - Argumentos con nombre: que son un mapa, con lo bueno y lo malo.
 * - **Multimétodos**: el despacho dinámico y en qué se diferencia de Java.
 * - El orden de inicialización, y la trampa del campo declarado más abajo.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **Sin modificador es propiedad; con `private` es campo.** Ésa es toda la regla.
 * - **`objeto.nombre` llama al getter.** Si escribes el tuyo, gana el tuyo. Para el
 *   campo de verdad: `objeto.@nombre`.
 * - **Un getter sin campo detrás ya es una propiedad de sólo lectura.** Es la forma
 *   de exponer un valor derivado.
 * - **Los argumentos con nombre son un mapa y no validan nada.** Una errata en una
 *   clave no da ni un aviso.
 * - **Groovy elige la sobrecarga por el tipo real del objeto.** Código idéntico al de
 *   Java puede dar otro resultado.
 * - Al escribir un constructor propio pierdes el de mapa, salvo que lo recuperes con
 *   `@MapConstructor` o `@TupleConstructor` (capítulo 12).
 *
 * ## Errores típicos
 * - Un campo `private` esperando que siga habiendo accesores.
 * - Confiar en los argumentos con nombre como si fueran una firma.
 * - Inicializar un campo a partir de otro declarado más abajo.
 *
 * Siguiente paso: capítulo 11, herencia, interfaces y traits.
 */
@CompileStatic
class C10_00Index {

    static final Chapter CHAPTER = chapter(
        10,
        'Clases y POGOs',
        'propiedades frente a campos, accesores, argumentos con nombre, multimétodos',
    ) {
        demo('Qué genera Groovy a partir de un POGO') { C10_01Pogos.demoWhatIsGenerated() }
        demo('El modificador de acceso lo cambia todo') { C10_01Pogos.demoAccessModifiers() }
        demo('Accesores propios y propiedades calculadas') { C10_01Pogos.demoCustomAccessors() }
        demo('POGO o mapa: cuándo cada uno') { C10_01Pogos.demoPogoVsMap() }
        demo('Los constructores que salen gratis') { C10_02Constructors.demoGeneratedConstructors() }
        demo('Argumentos con nombre: un mapa disfrazado') { C10_02Constructors.demoNamedArguments() }
        demo('Multimétodos: el despacho es dinámico') { C10_02Constructors.demoMultimethods() }
        demo('El orden de inicialización') { C10_02Constructors.demoInitialization() }
    }

    /** Ejecuta el capítulo 10 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
