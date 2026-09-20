package com.alejandro.c24dsl

import groovy.text.SimpleTemplateEngine
import groovy.transform.Canonical

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  24.1 · De una función con muchos parámetros a un DSL
//
//  QUÉ ES
//    Un DSL en Groovy no es una extensión del lenguaje ni un preprocesador: es código
//    Groovy normal que, gracias al `delegate` de los closures (capítulo 07), se lee
//    como si fuera otra cosa.
//
//  POR QUÉ IMPORTA
//    `build.gradle`, un `Jenkinsfile`, `MarkupBuilder` y el `chapter { }` de este
//    repositorio son todos el mismo patrón. Entenderlo una vez sirve para los cuatro.
//
//  ERRORES COMUNES
//    · Escribir un DSL donde bastaban argumentos con nombre.
//    · Dejar la `resolveStrategy` por defecto y que el delegate no reciba nada.
//    · Devolver el builder en vez del objeto construido.
//    · No validar, y dejar que el objeto salga a medias.
// =====================================================================================

class C24_01Dsl {

    /**
     * El camino, paso a paso.
     */
    static void demoStepByStep() {
        section('Paso 0: una función con muchos parámetros')

        show('resultado', crearServidorPosicional('api.ejemplo.com', 443, true, 30, 'UTF-8'))
        bullet('¿Qué era el `true`? ¿Y el 30? Hay que ir a la firma para saberlo.')

        section('Paso 1: argumentos con nombre (capítulo 10)')

        show('resultado', crearServidorConNombre(
            host: 'api.ejemplo.com', puerto: 443, seguro: true, tiempoDeEspera: 30))
        bullet('Ya se lee. **Y para mucha gente, aquí se acaba el problema.**')
        bullet('Antes de escribir un DSL, prueba esto: son cero líneas de código.')

        section('Paso 2: cuándo eso NO basta')

        bullet('Cuando hay ANIDAMIENTO: un servidor con varias rutas, cada una con')
        bullet('sus cabeceras.')
        bullet('Cuando hay ELEMENTOS REPETIDOS de longitud variable.')
        bullet('Cuando quieres que el bloque se lea como una configuración y no')
        bullet('como una estructura de datos.')

        section('Paso 3: el DSL')

        def servidor = servidor {
            host 'api.ejemplo.com'
            puerto 443
            seguro true

            ruta('/usuarios') {
                metodo 'GET'
                cabecera 'Accept', 'application/json'
                cabecera 'X-Version', '2'
            }

            ruta('/pedidos') {
                metodo 'POST'
                cabecera 'Content-Type', 'application/json'
            }
        }

        show('host', servidor.host)
        show('rutas', servidor.rutas*.camino)
        show('cabeceras de /usuarios', servidor.rutas[0].cabeceras)

        section('El mecanismo entero, en tres líneas')

        bullet('bloque.delegate = elBuilder')
        bullet('bloque.resolveStrategy = Closure.DELEGATE_FIRST')
        bullet('bloque()')
        bullet('Todo lo demás son mejoras sobre eso.')
    }

    private static String crearServidorPosicional(String host, int puerto, boolean seguro,
                                                  int tiempoDeEspera, String codificacion) {
        "$host:$puerto seguro=$seguro espera=$tiempoDeEspera cod=$codificacion"
    }

    private static String crearServidorConNombre(Map opciones) {
        "${opciones.host}:${opciones.puerto} seguro=${opciones.seguro} espera=${opciones.tiempoDeEspera}"
    }

    /**
     * Las reglas de un builder decente.
     */
    static void demoBuilderRules() {
        section('1. El builder es mutable; el RESULTADO es inmutable')

        def config = servidor {
            host 'localhost'
            puerto 8080
        }
        show('tipo devuelto', config.getClass().simpleName)
        try {
            config.rutas << new Ruta(camino: '/colada')
        } catch (UnsupportedOperationException e) {
            show('intentando modificar el resultado', e.class.simpleName)
        }
        bullet('Nunca devuelvas el builder: quien lo reciba podría seguir cambiándolo.')

        section('2. La VALIDACIÓN va en el construir(), no en cada asignación')

        try {
            servidor {
                puerto 443
                // falta el host
            }
        } catch (IllegalStateException e) {
            show('sin host', e.message)
        }
        bullet('Así el ORDEN de las líneas del bloque no importa. Si validaras en')
        bullet('cada `set`, `puerto` antes que `host` podría fallar sin motivo.')

        section('3. Valores por defecto sensatos')

        def minimo = servidor { host 'x' }
        show('puerto por defecto', minimo.puerto)
        show('seguro por defecto', minimo.seguro)

        section('4. Una clase por tipo de bloque')

        bullet('`ServidorBuilder` y `RutaBuilder` son clases distintas, y por eso')
        bullet('`metodo` sólo existe dentro de `ruta { }`.')
        bullet('Una clase genérica "para todo" pierde esa seguridad.')

        section('5. @DelegatesTo, para el IDE y para @CompileStatic')

        bullet('Sin él, el bloque funciona pero nadie sabe qué se puede escribir')
        bullet('dentro: ni el IDE, ni el compilador, ni quien lee la firma.')
    }

    /**
     * La fuga de ámbito en un DSL anidado.
     */
    static void demoScopeLeak() {
        section('El problema: dentro de `ruta { }` se sigue viendo `host`')

        def servidor = servidor {
            host 'principal.com'
            ruta('/a') {
                metodo 'GET'
                // Esto NO debería compilar, pero compila y hace algo inesperado:
                host 'colado.com'
            }
        }

        show('host del servidor', servidor.host)
        bullet('Una llamada escrita en el bloque de la RUTA ha cambiado el SERVIDOR.')
        bullet('Con OWNER_FIRST o DELEGATE_FIRST, si el delegate no tiene el método,')
        bullet('se busca hacia fuera. Y el builder de fuera sí lo tiene.')

        section('La solución en Groovy: DELEGATE_ONLY')

        def estricto = servidorEstricto {
            host 'principal.com'
            ruta('/a') {
                metodo 'GET'
            }
        }
        show('funciona lo válido', estricto.rutas*.camino)

        try {
            servidorEstricto {
                host 'principal.com'
                ruta('/a') {
                    host 'colado.com'
                }
            }
        } catch (Exception e) {
            show('el intento de fuga', e.class.simpleName)
        }
        bullet('`DELEGATE_ONLY` cierra el ámbito: dentro de `ruta { }` sólo existe')
        bullet('lo que tenga `RutaBuilder`. Es el equivalente al @DslMarker de otros')
        bullet('lenguajes.')

        section('El precio de DELEGATE_ONLY')

        bullet('Tampoco se ven los métodos de la CLASE donde escribes el bloque,')
        bullet('así que un método de ayuda tuyo deja de estar disponible dentro.')
        bullet('Es el compromiso: seguridad frente a comodidad.')

        section('La regla')

        bullet('En cuanto haya DOS niveles de anidamiento, cierra el ámbito.')
        bullet('Sin eso, una errata compila y hace algo distinto de lo que parece.')
    }

    /**
     * Otros builders, y las plantillas.
     */
    static void demoOtherBuilders() {
        section('Los builders que ya vienen con Groovy')

        bullet('MarkupBuilder            XML y HTML            capítulo 23')
        bullet('StreamingMarkupBuilder   lo mismo, sin árbol   capítulo 23')
        bullet('JsonBuilder              JSON                  capítulo 22')
        bullet('NodeBuilder              un árbol genérico')
        bullet('ObjectGraphBuilder       objetos de dominio')
        bullet('SwingBuilder             interfaces de escritorio')

        section('NodeBuilder: un árbol de datos sin clases')

        def arbol = new NodeBuilder().organizacion {
            departamento(nombre: 'IT') {
                persona(nombre: 'Ana', rol: 'lead')
                persona(nombre: 'Luis', rol: 'dev')
            }
            departamento(nombre: 'RRHH') {
                persona(nombre: 'Eva', rol: 'manager')
            }
        }
        show('departamentos', arbol.departamento*.'@nombre')
        show('todas las personas', arbol.depthFirst()
            .findAll { it.name() == 'persona' }*.'@nombre')

        section('Plantillas: cuando el resultado es TEXTO')

        def plantilla = new SimpleTemplateEngine().createTemplate('''\
            Hola $nombre,

            Tu pedido #$numero tiene <% out << lineas.size() %> líneas:
            <% lineas.each { %>  - $it
            <% } %>
            Total: $total €'''.stripIndent())

        String carta = plantilla.make([
            nombre: 'Ana', numero: 1001,
            lineas: ['Libro', 'Lápiz'], total: '36.70',
        ]).toString()

        println carta.readLines().collect { "    $it" }.join('\n')
        bullet('`$variable` interpola y `<% %>` ejecuta código, como en JSP o ERB.')
        bullet('Para plantillas grandes, `StreamingTemplateEngine` no carga el')
        bullet('resultado entero en memoria.')

        section('Cuándo una plantilla y cuándo un builder')

        bullet('Builder: el resultado es una ESTRUCTURA (XML, JSON, un objeto).')
        bullet('Plantilla: el resultado es TEXTO con huecos (un correo, un informe).')
        bullet('Si estás concatenando cadenas con `+` para generar algo, casi seguro')
        bullet('que querías una de las dos.')
    }

    /**
     * Cuándo merece la pena un DSL.
     */
    static void demoWhenWorthIt() {
        section('Las tres preguntas')

        bullet('1. ¿Hay ANIDAMIENTO o elementos repetidos de longitud variable?')
        bullet('2. ¿Lo va a escribir alguien más de una vez?')
        bullet('3. ¿Los argumentos con nombre se quedan cortos de verdad?')
        bullet('Si alguna es "no", probablemente no hace falta un DSL.')

        section('El precio que se paga')

        bullet('Un DSL en código hay que RECOMPILAR para cambiar un valor.')
        bullet('Si la configuración la edita otra persona o cambia por entorno,')
        bullet('un fichero externo (YAML, JSON, .properties) sigue siendo la')
        bullet('respuesta correcta.')
        bullet('Y un DSL sin @DelegatesTo es un agujero negro para el IDE.')

        section('Dónde sí ha ganado, de forma masiva')

        bullet('Gradle: build.gradle es un DSL sobre un modelo de proyecto.')
        bullet('Jenkins: el Jenkinsfile declarativo es exactamente este patrón.')
        bullet('Spock: los bloques given/when/then son otro DSL (capítulo 28).')
        bullet('Y este repositorio: `chapter(…) { demo(…) { } }`, en infra/Demo.groovy.')

        section('Vuelve a leer infra/Demo.groovy ahora')

        bullet('Son cuarenta líneas y tiene las cinco reglas: builder mutable,')
        bullet('resultado inmutable, validación al construir, una clase por bloque')
        bullet('y @DelegatesTo. Es el ejemplo más corto que vas a encontrar.')
    }

    // -- El DSL, con la fuga -----------------------------------------------------------

    static Servidor servidor(
        @DelegatesTo(value = ServidorBuilder, strategy = Closure.DELEGATE_FIRST) Closure bloque) {
        def builder = new ServidorBuilder()
        bloque.delegate = builder
        bloque.resolveStrategy = Closure.DELEGATE_FIRST
        bloque()
        builder.construir()
    }

    // -- El mismo DSL, con el ámbito cerrado -------------------------------------------

    static Servidor servidorEstricto(
        @DelegatesTo(value = ServidorBuilder, strategy = Closure.DELEGATE_ONLY) Closure bloque) {
        def builder = new ServidorBuilder(estricto: true)
        bloque.delegate = builder
        bloque.resolveStrategy = Closure.DELEGATE_ONLY
        bloque()
        builder.construir()
    }

    static class ServidorBuilder {
        private String host
        private int puerto = 80
        private boolean seguro = false
        private final List<Ruta> rutas = []
        boolean estricto = false

        void host(String valor) { host = valor }

        void puerto(int valor) { puerto = valor }

        void seguro(boolean valor) { seguro = valor }

        void ruta(String camino,
                  @DelegatesTo(value = RutaBuilder, strategy = Closure.DELEGATE_ONLY) Closure bloque) {
            def builder = new RutaBuilder(camino: camino)
            bloque.delegate = builder
            bloque.resolveStrategy = estricto ? Closure.DELEGATE_ONLY : Closure.DELEGATE_FIRST
            bloque()
            rutas << builder.construir()
        }

        Servidor construir() {
            // La validación, aquí: así el orden de las líneas del bloque da igual.
            if (!host) throw new IllegalStateException('falta el host')
            if (puerto !in 1..65535) throw new IllegalStateException("puerto inválido: $puerto")
            new Servidor(host: host, puerto: puerto, seguro: seguro, rutas: rutas.asImmutable())
        }
    }

    static class RutaBuilder {
        String camino
        private String metodo = 'GET'
        private final Map<String, String> cabeceras = [:]

        void metodo(String valor) { metodo = valor }

        void cabecera(String nombre, String valor) { cabeceras[nombre] = valor }

        Ruta construir() {
            new Ruta(camino: camino, metodo: metodo, cabeceras: cabeceras.asImmutable())
        }
    }

    @Canonical
    static class Servidor {
        String host
        int puerto
        boolean seguro
        List<Ruta> rutas
    }

    @Canonical
    static class Ruta {
        String camino
        String metodo
        Map<String, String> cabeceras
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia DELEGATE_FIRST por OWNER_FIRST en `servidor` y mira qué deja de funcionar.
//  2. Mueve la validación de `construir()` a `host()` y comprueba que ahora el orden
//     de las líneas del bloque sí importa.
//  3. Haz que `construir()` devuelva el builder y piensa qué podría hacer quien lo
//     reciba.
//  4. Añade un bloque `autenticacion { }` de tercer nivel y comprueba la fuga de ámbito.
