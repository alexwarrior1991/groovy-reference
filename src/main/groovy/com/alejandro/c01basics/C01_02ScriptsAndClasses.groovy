package com.alejandro.c01basics

import groovy.lang.Binding
import groovy.lang.GroovyShell

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  01.2 · Script frente a clase, y por qué este repositorio usa clases
//
//  QUÉ ES
//    Un fichero .groovy con sentencias sueltas se compila a una clase que extiende
//    `groovy.lang.Script`, con el nombre del fichero y un `main` generado. Un fichero
//    con `class Foo { ... }` se compila como en Java.
//
//  POR QUÉ IMPORTA
//    Decide cómo se organiza cualquier proyecto Groovy. Los scripts son inmejorables
//    para automatización (Gradle, Jenkins, un apaño de diez líneas); las clases, para
//    código que otro código va a llamar. Este repositorio necesita lo segundo: el
//    lanzador tiene que poder invocar cada demo por separado.
//
//  ERRORES COMUNES
//    · Poner `static void main` en un script: choca con el que Groovy genera.
//    · Esperar que las variables de un script sean accesibles desde sus métodos. Una
//      `def x = 1` en el cuerpo del script es LOCAL al método `run()` generado.
//    · Mezclar sentencias sueltas y una clase con el mismo nombre en el mismo fichero.
// =====================================================================================

class C01_02ScriptsAndClasses {

    /**
     * Qué genera Groovy a partir de un script.
     */
    static void demoWhatAScriptCompilesTo() {
        section('Un script es una clase, aunque no se vea')

        // `GroovyShell` compila y ejecuta código en caliente: es el capítulo 25, pero
        // aquí sirve para mirar por dentro qué genera Groovy.
        GroovyShell shell = new GroovyShell()
        Script guion = shell.parse('println "desde el script"; 42')

        show('la clase generada extiende', guion.class.superclass.name)
        show('nombre de la clase', guion.class.simpleName.replaceAll(/\d+$/, 'N'))
        bullet('Groovy le pone el nombre del fichero (aquí, uno autogenerado).')

        section('Ejecutarlo devuelve su última expresión')

        show('guion.run()', guion.run())
        bullet('El `run()` generado contiene el cuerpo del script.')
        bullet('El `main` generado se limita a llamar a `run()`.')
    }

    /**
     * La trampa: las variables de un script no llegan a sus métodos.
     */
    static void demoScriptVariableScope() {
        section('`def x = 1` en un script es LOCAL al run() generado')

        GroovyShell shell = new GroovyShell()
        String conDef = '''
            def contador = 10
            int leer() { contador }       // no ve la variable: es local a run()
            try { leer() } catch (MissingPropertyException e) { "falla: ${e.property}" }
        '''
        show('con def', shell.evaluate(conDef))

        section('Sin `def`, la variable va al Binding y sí se ve')

        String sinDef = '''
            contador = 10                 // sin def: va al Binding del script
            int leer() { contador }
            leer()
        '''
        show('sin def', shell.evaluate(sinDef))

        bullet('Sin `def` la variable es GLOBAL del script (vive en su Binding).')
        bullet('Con `def` es local, y los métodos del script no la alcanzan.')
        bullet('La forma correcta de tener un campo de verdad es `@Field`. Capítulo 25.')

        section('@Field: una variable de script que sí es un campo')

        String conField = '''
            import groovy.transform.Field
            @Field int contador = 10
            int leer() { contador }
            leer()
        '''
        show('con @Field', shell.evaluate(conField))
    }

    /**
     * El Binding: pasar datos a un script desde fuera.
     */
    static void demoBinding() {
        section('Inyectar variables en un script')

        Binding datos = new Binding(nombre: 'Alejandro', veces: 3)
        GroovyShell shell = new GroovyShell(datos)

        show('resultado', shell.evaluate('([nombre] * veces).join(", ")'))
        bullet('Es el mecanismo de Jenkins y de Gradle para exponerte su API.')

        section('El script también puede devolver datos por el Binding')

        shell.evaluate('resultado = veces * 2')
        show('datos.resultado', datos.getVariable('resultado'))
    }

    /**
     * Por qué el repositorio está hecho de clases con métodos estáticos.
     */
    static void demoWhyClassesHere() {
        section('Lo que necesita el lanzador')

        bullet('Registrar cada demo por separado, para poder ejecutar sólo la 13.4.')
        bullet('Llamarla desde otra clase (el índice del capítulo).')
        bullet('Que el test de humo pueda invocarlas todas, una a una.')

        section('Con un script eso no se puede')

        bullet('Los métodos de un script son suyos: hay que instanciarlo para llamarlos.')
        bullet('Y un script no expone sus métodos como estáticos.')

        section('La solución del repositorio')

        bullet('Cada fichero de contenido es una CLASE con métodos `static void demoXxx()`.')
        bullet('El índice del capítulo los registra: demo("Título") { Clase.demoXxx() }.')
        bullet('El identificador (13.4) se genera solo, por orden de declaración.')

        section('A diferencia de otros lenguajes, aquí SÍ podría haber un main por fichero')

        bullet('Groovy permite un `static void main` en cada clase.')
        bullet('No se hace: cien mains de ceremonia contradicen el Groovy idiomático')
        bullet('que este repositorio pretende enseñar. Hay uno por capítulo.')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. En `demoScriptVariableScope`, quita el `@Field` y mira qué excepción sale.
//  2. Pásale al Binding una variable que el script no use y comprueba que no pasa nada;
//     después quita una que sí use y lee el error.
//  3. Cambia `shell.parse(...)` por un script cuya última línea sea `def x = 1` y mira
//     qué devuelve `run()` entonces.
