package com.alejandro.c16mop

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  16.1 · El MOP: la metaclase
//
//  QUÉ ES
//    El Meta Object Protocol. En Groovy, `objeto.metodo()` NO es una llamada directa:
//    es una consulta a la METACLASE del objeto, que decide qué ejecutar. Y esa
//    metaclase se puede modificar en ejecución.
//
//  POR QUÉ IMPORTA
//    Es lo que permite el GDK (métodos añadidos a clases de Java que no se pueden
//    tocar), los mocks de Spock, las extensiones de Grails y buena parte de los
//    frameworks de Groovy.
//
//  ERRORES COMUNES
//    · Modificar la metaclase de una clase de la biblioteca en producción: afecta a
//      TODO el proceso, incluidas las librerías de terceros.
//    · Olvidar deshacer el cambio en un test y contaminar los siguientes.
//    · Usarlo donde una interfaz o un trait resolvían el problema.
// =====================================================================================

class C16_01MetaClass {

    /**
     * Qué es la metaclase.
     */
    static void demoWhatIsIt() {
        section('Todo objeto tiene una metaclase')

        def texto = 'hola'
        show('su metaclase', texto.metaClass.getClass().simpleName)
        show('la clase que representa', texto.metaClass.theClass.simpleName)

        section('Se le puede preguntar qué sabe hacer')

        show('¿responde a toUpperCase?', texto.respondsTo('toUpperCase') as boolean)
        show('¿responde a inventado?', texto.respondsTo('inventado') as boolean)
        show('cuántos métodos tiene String', texto.metaClass.methods.size())
        show('algunos del GDK', texto.metaClass.methods*.name.findAll {
            it in ['capitalize', 'tokenize', 'toList', 'reverse']
        }.sort().unique())
        bullet('`capitalize` no está en java.lang.String: lo añade el GDK a través')
        bullet('de este mismo mecanismo.')

        section('Y también por sus propiedades')

        def usuario = new Usuario(nombre: 'Ana')
        show('propiedades', usuario.metaClass.properties*.name.sort())
        show('¿tiene "nombre"?', usuario.hasProperty('nombre') != null)
        show('¿tiene "inventada"?', usuario.hasProperty('inventada') != null)

        section('Llamar por nombre, en ejecución')

        String cual = 'toUpperCase'
        show('texto."$cual"()', texto."$cual"())
        show('texto.invokeMethod(nombre, null)', texto.invokeMethod(cual, null))
        show('leer una propiedad por nombre', usuario['nombre'])
        bullet('Esto es lo que @CompileStatic (capítulo 15) NO deja hacer.')
    }

    static class Usuario {
        String nombre
    }

    /**
     * Añadir métodos en caliente.
     */
    static void demoAddingMethods() {
        section('Añadir un método a una clase propia')

        Producto.metaClass.descripcionCorta = { ->
            "${delegate.nombre} (${delegate.precio}€)"
        }

        def producto = new Producto(nombre: 'Libro', precio: 20)
        show('producto.descripcionCorta()', producto.descripcionCorta())
        bullet('Dentro del closure, `delegate` es el objeto sobre el que se llama.')

        section('Añadir una propiedad')

        Producto.metaClass.getConIva = { -> delegate.precio * 1.21 }
        show('producto.conIva', producto.conIva)
        bullet('Un `getXxx` en la metaclase crea la propiedad `xxx`.')

        section('Añadir un método ESTÁTICO')

        Producto.metaClass.static.gratis = { -> new Producto(nombre: 'Muestra', precio: 0) }
        show('Producto.gratis()', Producto.gratis().nombre)

        section('Y a una clase de la biblioteca de Java')

        String.metaClass.esPalindromo = { ->
            String limpio = delegate.toLowerCase().replaceAll(/[^a-z0-9]/, '')
            limpio == limpio.reverse()
        }
        show("'Anita lava la tina'.esPalindromo()", 'Anita lava la tina'.esPalindromo())
        show("'groovy'.esPalindromo()", 'groovy'.esPalindromo())
        bullet('Esto afecta a TODOS los String del proceso. Es potentísimo y')
        bullet('peligrosísimo a partes iguales.')

        section('Sólo para UNA instancia')

        def especial = new Producto(nombre: 'Único', precio: 100)
        especial.metaClass.saludar = { -> "soy ${delegate.nombre} y sólo yo sé esto" }
        show('especial.saludar()', especial.saludar())

        def normal = new Producto(nombre: 'Normal', precio: 10)
        show('otro producto responde?', normal.respondsTo('saludar') as boolean)
        bullet('Dar metaclase a una instancia le crea una propia. Es lo más contenido')
        bullet('que se puede hacer con el MOP.')

        // Dejamos String como estaba: si no, el resto del repositorio hereda el cambio.
        limpiarString()
    }

    private static void limpiarString() {
        GroovySystem.metaClassRegistry.removeMetaClass(String)
    }

    static class Producto {
        String nombre
        BigDecimal precio
    }

    /**
     * methodMissing y propertyMissing.
     */
    static void demoMissing() {
        section('methodMissing: qué hacer cuando el método no existe')

        def consulta = new ConsultaDinamica()
        show('buscarPorNombre("Ana")', consulta.buscarPorNombre('Ana'))
        show('buscarPorEdad(30)', consulta.buscarPorEdad(30))
        show('buscarPorCiudadYEdad("Madrid", 30)', consulta.buscarPorCiudadYEdad('Madrid', 30))
        bullet('Ninguno de esos métodos existe. Los inventa `methodMissing`.')
        bullet('Es el patrón de los "finders" de Grails y de muchos ORM.')

        section('Y lo que no encaja con el patrón, falla como debe')

        try {
            consulta.hacerAlgoRaro()
        } catch (MissingMethodException e) {
            show('hacerAlgoRaro()', e.class.simpleName)
        }
        bullet('Importante: si no sabes atender la llamada, RELÁNZALA. Si no,')
        bullet('las erratas se convierten en silencio.')

        section('El truco del rendimiento: registrar el método al vuelo')

        bullet('La idea: la primera vez que falta, lo añadimos a la metaclase, y así')
        bullet('las siguientes llamadas ya no pagan la búsqueda fallida.')

        ConCache.vecesQueFaltó = 0
        def contador = new ConCache()
        contador.loQueSea()
        contador.loQueSea()
        contador.loQueSea()
        show('mismo objeto, tres llamadas', ConCache.vecesQueFaltó)
        bullet('¡Tres! El objeto ya tenía resuelta su metaclase cuando se registró')
        bullet('el método, y sigue cayendo en `methodMissing`.')

        ConCache.vecesQueFaltó = 0
        new ConCache().otroMetodo()
        new ConCache().otroMetodo()
        new ConCache().otroMetodo()
        show('objetos NUEVOS, tres llamadas', ConCache.vecesQueFaltó)
        bullet('Aquí sí: sólo la primera pasa por `methodMissing`. Las instancias')
        bullet('creadas DESPUÉS del registro ya ven el método.')

        bullet('Conclusión: el truco funciona, pero no para el objeto que provocó')
        bullet('el registro. Si necesitas que le valga también a él, registra sobre')
        bullet('`this.metaClass` y no sobre el de la clase.')

        section('Una limitación real: no valen en clases internas estáticas')

        bullet('`methodMissing` en una `static class` anidada NO compila: Groovy ya')
        bullet('genera uno sintético ahí para delegar en la clase de fuera. Por eso')
        bullet('las clases de esta demo están al nivel del fichero, no anidadas.')

        section('propertyMissing: lo mismo para las propiedades')

        def config = new ConfiguracionDinamica()
        config.host = 'localhost'
        config.puerto = 8080
        show('config.host', config.host)
        show('config.puerto', config.puerto)
        show('una que no se ha puesto', config.inventada)
        show('todo lo guardado', config.valores)
    }




    /**
     * Interceptar TODAS las llamadas.
     */
    static void demoInterceptable() {
        section('GroovyInterceptable: `invokeMethod` recibe todo')

        def servicio = new ServicioConTrazas()
        show('resultado', servicio.calcular(5))
        show('otro', servicio.saludar('Ana'))
        show('lo que registró', servicio.trazas)
        bullet('TODAS las llamadas pasan por `invokeMethod`, existan o no.')
        bullet('Es la forma de implementar trazas, métricas o seguridad sin tocar')
        bullet('el método original.')

        section('Lo que cuesta')

        bullet('Cada llamada pasa por tu código: es lento.')
        bullet('Incluidas las internas del propio objeto. Cuidado con la recursión.')
        bullet('@CompileStatic lo desactiva por completo.')

        section('Alternativas, casi siempre mejores')

        bullet('Un proxy explícito o @Delegate (capítulo 12).')
        bullet('Un decorador escrito a mano.')
        bullet('Y si es para un test: un mock de Spock (capítulo 28).')
    }

    static class ServicioConTrazas implements GroovyInterceptable {
        final List<String> trazas = []

        int calcular(int n) { n * 2 }

        String saludar(String nombre) { "hola, $nombre" }

        @Override
        Object invokeMethod(String nombre, Object args) {
            // `trazas` se accede como propiedad, así que no vuelve a entrar aquí.
            if (nombre in ['calcular', 'saludar']) {
                trazas << "$nombre(${(args as List).join(', ')})"
            }
            def metodo = ServicioConTrazas.metaClass.getMetaMethod(nombre, args)
            metodo ? metodo.invoke(this, args) : super.invokeMethod(nombre, args)
        }
    }
}


class ConsultaDinamica {
    private static final List<Map> DATOS = [
        [nombre: 'Ana', edad: 30, ciudad: 'Madrid'],
        [nombre: 'Luis', edad: 25, ciudad: 'Bilbao'],
        [nombre: 'Eva', edad: 30, ciudad: 'Madrid'],
    ]

    def methodMissing(String nombre, Object args) {
        def coincidencia = nombre =~ /^buscarPor(.+)$/
        if (!coincidencia) {
            throw new MissingMethodException(nombre, ConsultaDinamica, args as Object[])
        }

        List<String> campos = coincidencia[0][1]
            .split('Y')
            .collect { it.uncapitalize() }

        List<Object> valores = args as List
        DATOS.findAll { Map fila ->
            [campos, valores].transpose().every { par -> fila[par[0]] == par[1] }
        }*.nombre
    }
}

class ConCache {
    static int vecesQueFaltó = 0

    def methodMissing(String nombre, Object args) {
        vecesQueFaltó++
        // Se registra en la metaclase: a partir de ahora el método EXISTE.
        ConCache.metaClass."$nombre" = { -> "resultado de $nombre" }
        "resultado de $nombre"
    }
}

class ConfiguracionDinamica {
    final Map<String, Object> valores = [:]

    def propertyMissing(String nombre) { valores[nombre] }

    def propertyMissing(String nombre, Object valor) { valores[nombre] = valor }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita el `throw` de `methodMissing` en `ConsultaDinamica` y llama a un método
//     con una errata: comprueba que ya no te enteras.
//  2. Añade un método a `Integer.metaClass` y úsalo. Después quítalo con
//     `GroovySystem.metaClassRegistry.removeMetaClass(Integer)`.
//  3. Quita el registro en la metaclase de `ConCache` y cuenta las veces que entra.
//  4. Pon @CompileStatic en `ConsultaDinamica` y mira qué pasa.
