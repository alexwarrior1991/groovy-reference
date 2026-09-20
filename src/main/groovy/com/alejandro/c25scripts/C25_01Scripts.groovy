package com.alejandro.c25scripts

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  25.1 · Scripts, Binding y evaluar código en ejecución
//
//  QUÉ ES
//    Groovy puede compilar y ejecutar código en caliente. `GroovyShell` compila un
//    texto y lo ejecuta; el `Binding` es el puente de datos entre tu programa y el
//    script; y `CompilerConfiguration` decide qué puede hacer ese código.
//
//  POR QUÉ IMPORTA
//    Es la base de Jenkins, de Gradle y de cualquier sitio donde el usuario escribe
//    reglas que el programa ejecuta. También es la puerta de entrada más grande que
//    puedes abrirle a alguien en tu proceso.
//
//  ERRORES COMUNES
//    · Evaluar código que viene del usuario sin limitarlo en absoluto.
//    · Recompilar el mismo script en cada llamada.
//    · Esperar que las variables con `def` del script se vean desde sus métodos.
// =====================================================================================

class C25_01Scripts {

    /**
     * GroovyShell y el Binding.
     */
    static void demoShellAndBinding() {
        section('Evaluar una expresión')

        def shell = new GroovyShell()
        show('evaluate("2 + 3")', shell.evaluate('2 + 3'))
        show('evaluate con colecciones', shell.evaluate('[1,2,3].sum()'))
        show('el tipo del resultado', shell.evaluate('[1,2,3]').getClass().simpleName)

        section('Pasar datos con el Binding')

        def datos = new Binding(precio: 100, descuento: 20)
        def conDatos = new GroovyShell(datos)
        show('regla del usuario', conDatos.evaluate('precio * (100 - descuento) / 100'))

        section('Y recuperar lo que el script deje')

        conDatos.evaluate('resultado = precio * 2; mensaje = "calculado"')
        show('datos.resultado', datos.getVariable('resultado'))
        show('datos.mensaje', datos.getVariable('mensaje'))
        show('todas las variables', datos.variables.keySet().sort())
        bullet('Una variable SIN `def` va al Binding y sale del script. Con `def`')
        bullet('es local y se pierde. Es la trampa del capítulo 01.')

        section('Compilar una vez, ejecutar muchas')

        Script compilado = shell.parse('valor * factor')
        List<Integer> resultados = (1..3).collect { int f ->
            compilado.binding = new Binding(valor: 10, factor: f)
            compilado.run() as int
        }
        show('tres ejecuciones', resultados)
        bullet('`parse` compila; `run` ejecuta. Si el script se usa muchas veces,')
        bullet('compilarlo en cada llamada es el error de rendimiento típico.')

        section('Cuánto cuesta compilar')

        long compilando = medir { 200.times { new GroovyShell().evaluate('1 + 1') } }
        Script unaVez = new GroovyShell().parse('1 + 1')
        long reusando = medir { 200.times { unaVez.run() } }
        show('compilando cada vez', "${compilando} ms")
        show('reusando el compilado', "${reusando} ms")
    }

    private static long medir(Closure<?> bloque) {
        bloque()
        long t0 = System.nanoTime()
        bloque()
        ((System.nanoTime() - t0) / 1_000_000L) as long
    }

    /**
     * @Field y @BaseScript.
     */
    static void demoFieldAndBaseScript() {
        section('El problema: `def` en un script no llega a sus métodos')

        def shell = new GroovyShell()
        show('con def', shell.evaluate('''
            def contador = 10
            int leer() { contador }
            try { leer() } catch (MissingPropertyException e) { "falla: ${e.property}" }
        '''))

        section('@Field lo convierte en un campo de verdad')

        show('con @Field', shell.evaluate('''
            import groovy.transform.Field
            @Field int contador = 10
            int leer() { contador }
            int incrementar() { ++contador }
            incrementar(); incrementar()
            leer()
        '''))

        section('@BaseScript: darle una clase base al script')

        def config = new CompilerConfiguration(scriptBaseClass: ScriptDeReglas.name)
        def conBase = new GroovyShell(new Binding(edad: 25), config)

        show('el script usa métodos de la base', conBase.evaluate('''
            registrar("evaluando la regla")
            aprobar(edad >= 18)
        '''))
        bullet('Todos los scripts que se ejecuten con esa configuración heredan')
        bullet('`registrar` y `aprobar`. Es como Jenkins te da `sh`, `echo` y `stage`.')

        section('Añadir imports automáticos')

        def imports = new ImportCustomizer()
        imports.addStaticStars('java.lang.Math')
        imports.addImports('java.time.LocalDate')
        def conImports = new GroovyShell(new CompilerConfiguration().addCompilationCustomizers(imports))

        show('sin escribir el import', conImports.evaluate('sqrt(16.0)'))
        show('LocalDate importado', conImports.evaluate('LocalDate.of(2026, 1, 1).year'))
        bullet('Así el usuario escribe reglas cortas sin preocuparse de los imports.')
    }

    /**
     * Ejecutar reglas de usuario, con cabeza.
     */
    static void demoRulesEngine() {
        section('Un motor de reglas diminuto')

        List<Map> pedidos = [
            [id: 1, total: 150, cliente: 'premium', urgente: false],
            [id: 2, total: 40, cliente: 'normal', urgente: true],
            [id: 3, total: 500, cliente: 'normal', urgente: false],
        ]

        Map<String, String> reglas = [
            'envío gratis'  : 'total > 100',
            'atención rápida': 'urgente || cliente == "premium"',
            'revisión manual': 'total > 400',
        ]

        def shell = new GroovyShell()
        Map<String, Script> compiladas = reglas.collectEntries { nombre, texto ->
            [nombre, shell.parse(texto)]
        }

        pedidos.each { Map pedido ->
            List<String> aplican = compiladas.findAll { nombre, script ->
                script.binding = new Binding(pedido)
                script.run() as boolean
            }.keySet() as List
            show("pedido ${pedido.id}", aplican ?: ['ninguna'])
        }

        bullet('Las reglas están en datos, no en código: se pueden guardar en una')
        bullet('base de datos y cambiarlas sin desplegar.')
        bullet('Y se compilan UNA vez, al arrancar.')

        section('Una regla rota falla al COMPILAR, que es la buena noticia')

        try {
            shell.parse('total > ')
        } catch (Exception e) {
            show("parse('total > ')", e.class.simpleName)
        }
        bullet('Al compilar las reglas por adelantado, una errata salta al ARRANCAR')
        bullet('y no la primera vez que llega el pedido que la activa.')

        section('Y por eso conviene validarlas al guardarlas')

        show('validar("total > 100")', validarRegla(shell, 'total > 100'))
        show('validar("total > ")', validarRegla(shell, 'total > '))
        bullet('Compilar es la validación: si compila, la regla es sintácticamente')
        bullet('correcta. Lo que haga al ejecutarse ya es otra historia.')
    }

    private static String validarRegla(GroovyShell shell, String texto) {
        try {
            shell.parse(texto)
            'válida'
        } catch (Exception e) {
            "no compila: ${e.class.simpleName}"
        }
    }

    /**
     * Seguridad: evaluar código de fuera.
     */
    static void demoSandboxing() {
        section('El problema, dicho claro')

        bullet('`new GroovyShell().evaluate(textoDelUsuario)` ejecuta CUALQUIER cosa')
        bullet('con los permisos de tu proceso: leer ficheros, abrir conexiones,')
        bullet('llamar a System.exit. No es una exageración.')

        section('Primera capa: quitar imports peligrosos')

        def config = new CompilerConfiguration()
        def customizer = new org.codehaus.groovy.control.customizers.SecureASTCustomizer()
        customizer.with {
            // Nada de importar lo que no esté en la lista.
            importsWhitelist = ['java.lang.Math']
            staticImportsWhitelist = []
            staticStarImportsWhitelist = ['java.lang.Math']
            // Ni definir clases, métodos o cerrar el proceso.
            methodDefinitionAllowed = false
            closuresAllowed = true
            // Sólo estos tipos de sentencia.
            receiversClassesWhiteList = [Integer, Long, BigDecimal, Double, String,
                                         Boolean, List, Map, Math, Object]
        }
        config.addCompilationCustomizers(customizer)
        def seguro = new GroovyShell(new Binding(x: 10), config)

        show('una expresión permitida', seguro.evaluate('x * 2 + 1'))

        try {
            seguro.evaluate('System.exit(0)')
        } catch (Exception e) {
            show('System.exit(0)', e.class.simpleName)
        }

        try {
            seguro.evaluate('new File("/etc/passwd").text')
        } catch (Exception e) {
            show('leer un fichero', e.class.simpleName)
        }

        try {
            seguro.evaluate('def malicioso() { }')
        } catch (Exception e) {
            show('definir un método', e.class.simpleName)
        }

        section('Pero SecureASTCustomizer no es suficiente por sí solo')

        bullet('Sólo mira el AST al COMPILAR. No puede impedir lo que se resuelva')
        bullet('en ejecución por el MOP (capítulo 16), ni un bucle infinito, ni que')
        bullet('el script agote la memoria.')

        section('Las capas que hacen falta de verdad')

        bullet('1. SecureASTCustomizer, para lo obvio.')
        bullet('2. Un interceptor en ejecución (como el sandbox de Jenkins) que')
        bullet('   compruebe CADA llamada a método.')
        bullet('3. Un límite de TIEMPO: ejecutar en otro hilo y cancelarlo.')
        bullet('4. Un límite de MEMORIA, o un proceso aparte.')
        bullet('5. Y, para algo de verdad hostil, otro proceso con menos permisos,')
        bullet('   o un contenedor.')

        section('La regla honesta')

        bullet('Si el código lo escriben tus compañeros: SecureASTCustomizer y un')
        bullet('tiempo máximo bastan.')
        bullet('Si lo escribe un usuario cualquiera de internet: no lo ejecutes en')
        bullet('tu proceso. Ni con esto ni con nada parecido.')
    }
}

/** Una clase base para los scripts: les da métodos sin que los importen. */
abstract class ScriptDeReglas extends Script {
    private final List<String> traza = []

    void registrar(String mensaje) { traza << mensaje }

    String aprobar(boolean condicion) {
        "${condicion ? 'aprobado' : 'rechazado'} (${traza.size()} paso/s registrado/s)"
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `@Field` y mira qué excepción sale.
//  2. Añade una regla al motor que use un método que no esté permitido.
//  3. Intenta saltarte el SecureASTCustomizer con `'java.lang.System'.class` y
//     metaprogramación: verás por qué hace falta la capa de ejecución.
//  4. Mide cuánto tarda compilar un script de cien líneas frente a ejecutarlo.
