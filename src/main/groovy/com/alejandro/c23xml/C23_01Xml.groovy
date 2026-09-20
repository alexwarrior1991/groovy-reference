package com.alejandro.c23xml

import groovy.xml.MarkupBuilder
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlParser
import groovy.xml.XmlSlurper
import groovy.xml.XmlUtil

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  23.1 · XML: Slurper, Parser y los builders
//
//  QUÉ ES
//    `groovy-xml` trae dos lectores y dos escritores:
//      XmlSlurper              lee y navega; PEREZOSO y de sólo lectura
//      XmlParser               lee y permite MODIFICAR el árbol
//      MarkupBuilder           escribe XML/HTML con un DSL
//      StreamingMarkupBuilder  lo mismo, sin construir el árbol en memoria
//
//  POR QUÉ IMPORTA
//    Navegar XML en Groovy se parece a navegar un mapa: `raiz.hijo.nieto`. Sin
//    XPath, sin DOM y sin doscientas líneas de ceremonia.
//
//  ERRORES COMUNES
//    · Usar XmlSlurper cuando hay que modificar el árbol (no se puede).
//    · Olvidar `.text()` y comparar un nodo con una cadena.
//    · Parsear XML de fuera sin desactivar las entidades externas (XXE).
// =====================================================================================

class C23_01Xml {

    private static final String CATALOGO = '''<?xml version="1.0" encoding="UTF-8"?>
<catalogo>
  <libro id="1" disponible="true">
    <titulo>Programación en Groovy</titulo>
    <autor>Ana García</autor>
    <precio moneda="EUR">35.50</precio>
    <etiquetas>
      <etiqueta>programación</etiqueta>
      <etiqueta>jvm</etiqueta>
    </etiquetas>
  </libro>
  <libro id="2" disponible="false">
    <titulo>Patrones de diseño</titulo>
    <autor>Luis Pérez</autor>
    <precio moneda="EUR">42.00</precio>
    <etiquetas>
      <etiqueta>arquitectura</etiqueta>
    </etiquetas>
  </libro>
  <libro id="3" disponible="true">
    <titulo>Tests con Spock</titulo>
    <autor>Ana García</autor>
    <precio moneda="USD">28.00</precio>
    <etiquetas/>
  </libro>
</catalogo>'''

    /**
     * Navegar con XmlSlurper.
     */
    static void demoSlurper() {
        section('Navegar es como navegar un mapa')

        def catalogo = new XmlSlurper().parseText(CATALOGO)
        show('nombre de la raíz', catalogo.name())
        show('cuántos libros', catalogo.libro.size())
        show('título del primero', catalogo.libro[0].titulo.text())
        bullet('`.text()` es obligatorio: sin él tienes un NODO, no una cadena.')

        show('sin .text()', catalogo.libro[0].titulo.getClass().simpleName)

        section('Atributos, con @')

        show('id del primero', catalogo.libro[0].@id.text())
        show('moneda del precio', catalogo.libro[0].precio.@moneda.text())
        show('todos los ids', catalogo.libro*.@id*.text())

        section('Todos los de un nivel, de golpe')

        show('todos los títulos', catalogo.libro.titulo*.text())
        show('todos los autores', catalogo.libro.autor*.text().unique())

        section('Filtrar')

        show('disponibles', catalogo.libro
            .findAll { it.@disponible.text() == 'true' }
            .collect { it.titulo.text() })

        show('en euros', catalogo.libro
            .findAll { it.precio.@moneda.text() == 'EUR' }
            .collect { "${it.titulo.text()}: ${it.precio.text()}" })

        show('de Ana García', catalogo.libro
            .findAll { it.autor.text() == 'Ana García' }
            .collect { it.titulo.text() })

        section('Búsqueda en profundidad con `**`')

        show('todas las etiquetas del documento', catalogo.'**'
            .findAll { it.name() == 'etiqueta' }*.text())
        bullet('`**` recorre TODOS los descendientes. Es el `//` de XPath.')

        section('Agregar')

        show('precio total en EUR', catalogo.libro
            .findAll { it.precio.@moneda.text() == 'EUR' }
            .sum { it.precio.text() as BigDecimal })

        show('libros por autor', catalogo.libro
            .groupBy { it.autor.text() }
            .collectEntries { autor, libros -> [autor, libros.size()] })

        section('Un nodo que no existe no lanza')

        show('catalogo.revista', catalogo.revista.size())
        show('catalogo.revista.titulo.text()', "'${catalogo.revista.titulo.text()}'")
        bullet('Devuelve una colección vacía y un texto vacío. Cómodo, pero una')
        bullet('errata en el nombre del nodo pasa inadvertida: comprueba `size()`.')
    }

    /**
     * XmlParser: modificar el árbol.
     */
    static void demoParser() {
        section('XmlParser construye un árbol MODIFICABLE')

        def catalogo = new XmlParser().parseText(CATALOGO)
        show('tipo', catalogo.getClass().simpleName)
        show('cuántos libros', catalogo.libro.size())
        show('primer título', catalogo.libro[0].titulo.text())

        section('Cambiar un valor')

        catalogo.libro[0].precio[0].value = '39.99'
        show('precio cambiado', catalogo.libro[0].precio.text())

        section('Cambiar un atributo')

        catalogo.libro[1].@disponible = 'true'
        show('disponible ahora', catalogo.libro[1].@disponible)

        section('Añadir un nodo')

        catalogo.appendNode('libro', [id: '4', disponible: 'true']) {
            titulo('Groovy avanzado')
            autor('Eva Ruiz')
            precio('30.00', moneda: 'EUR')
        }
        show('libros ahora', catalogo.libro.size())
        show('el nuevo', catalogo.libro[3].titulo.text())

        section('Quitar un nodo')

        def aQuitar = catalogo.libro.find { it.@id == '3' }
        catalogo.remove(aQuitar)
        show('libros tras quitar el 3', catalogo.libro*.@id)

        section('Volver a serializar')

        String salida = XmlUtil.serialize(catalogo)
        show('líneas del XML resultante', salida.readLines().findAll { it.trim() }.size())
        show('contiene el libro nuevo', salida.contains('Groovy avanzado'))

        section('Slurper frente a Parser')

        bullet('                 XmlSlurper          XmlParser')
        bullet('──────────────   ─────────────────   ─────────────────')
        bullet('modificar        NO                  SÍ')
        bullet('memoria          perezoso, menos     árbol completo')
        bullet('atributos        .@attr.text()       .@attr (ya es texto)')
        bullet('velocidad        mejor para leer     mejor para editar')
        bullet('')
        bullet('Regla: si sólo lees, Slurper. Si modificas, Parser.')
    }

    /**
     * Generar XML.
     */
    static void demoBuilders() {
        section('MarkupBuilder: un DSL para escribir XML')

        def escritor = new StringWriter()
        def xml = new MarkupBuilder(escritor)
        xml.setDoubleQuotes(true)

        xml.pedido(id: 1001, fecha: '2026-09-20') {
            cliente {
                nombre('Ana García')
                email('ana@ejemplo.com')
            }
            lineas {
                linea(producto: 'Libro', cantidad: 2, precio: '35.50')
                linea(producto: 'Lápiz', cantidad: 5, precio: '1.20')
            }
            total('77.00')
        }

        println escritor.toString().readLines().collect { "    $it" }.join('\n')
        bullet('Cada nombre de método se convierte en una etiqueta; el mapa, en')
        bullet('atributos; el closure, en los hijos. Es el capítulo 24 en acción.')

        section('Generarlo a partir de datos')

        def usuarios = [
            [nombre: 'Ana', edad: 30, roles: ['admin']],
            [nombre: 'Luis', edad: 25, roles: ['lector', 'editor']],
        ]

        def salida = new StringWriter()
        def b = new MarkupBuilder(salida)
        b.setDoubleQuotes(true)
        b.usuarios(total: usuarios.size()) {
            usuarios.each { Map u ->
                usuario(edad: u.edad) {
                    nombre(u.nombre)
                    u.roles.each { String r -> rol(r) }
                }
            }
        }
        println salida.toString().readLines().collect { "    $it" }.join('\n')
        bullet('El bucle va DENTRO del DSL: es código Groovy normal.')

        section('El escapado es automático')

        def conEscapado = new StringWriter()
        new MarkupBuilder(conEscapado).mensaje('texto con <etiquetas> & "comillas"')
        show('escapado', conEscapado.toString().trim())
        bullet('No hay que escapar a mano, y por eso un builder es más seguro que')
        bullet('concatenar cadenas.')

        section('StreamingMarkupBuilder: sin construir el árbol')

        def streaming = new StreamingMarkupBuilder(encoding: 'UTF-8')
        def documento = streaming.bind {
            mkp.xmlDeclaration()
            informe {
                (1..3).each { n -> fila(numero: n, valor: n * 10) }
            }
        }
        show('resultado', documento.toString().take(70) + '…')
        bullet('Genera según se escribe: sirve para documentos enormes.')
        bullet('`mkp` es el "espacio de nombres" del builder: xmlDeclaration, yield,')
        bullet('yieldUnescaped, comment.')

        section('HTML, que es el mismo builder')

        def html = new StringWriter()
        new MarkupBuilder(html).html {
            head { title('Informe') }
            body {
                h1('Usuarios')
                ul { usuarios.each { Map u -> li("${u.nombre} (${u.edad})") } }
            }
        }
        println html.toString().readLines().collect { "    $it" }.join('\n')
    }

    /**
     * Espacios de nombres y seguridad.
     */
    static void demoNamespacesAndSafety() {
        section('Espacios de nombres')

        String conNamespace = '''<?xml version="1.0"?>
<env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/"
              xmlns:m="http://ejemplo.com/pedidos">
  <env:Body>
    <m:pedido>
      <m:id>1001</m:id>
      <m:total>77.00</m:total>
    </m:pedido>
  </env:Body>
</env:Envelope>'''

        def sobre = new XmlSlurper().parseText(conNamespace)
            .declareNamespace(env: 'http://schemas.xmlsoap.org/soap/envelope/',
                              m: 'http://ejemplo.com/pedidos')

        show('id del pedido', sobre.'env:Body'.'m:pedido'.'m:id'.text())
        show('total', sobre.'env:Body'.'m:pedido'.'m:total'.text())
        bullet('`declareNamespace` y después las claves con prefijo, entrecomilladas.')

        section('Ignorando los espacios de nombres')

        def sinNamespace = new XmlSlurper(false, false).parseText(conNamespace)
        show('con el parser "tonto"', sinNamespace.Body.pedido.id.text())
        bullet('`new XmlSlurper(false, false)` desactiva validación y namespaces.')
        bullet('Cómodo para leer rápido, peligroso si los prefijos importan.')

        section('SEGURIDAD: XXE en XML que viene de fuera')

        bullet('Un XML puede declarar una ENTIDAD EXTERNA que lea un fichero del')
        bullet('servidor o haga una petición de red. Es la vulnerabilidad XXE.')
        bullet('')
        bullet('Groovy 4+ desactiva las entidades externas por defecto en')
        bullet('XmlSlurper y XmlParser, pero si construyes el parser a mano:')
        bullet('')
        bullet('  def s = new XmlSlurper()')
        bullet("  s.setFeature('http://apache.org/xml/features/disallow-doctype-decl', true)")
        bullet("  s.setFeature('http://xml.org/sax/features/external-general-entities', false)")

        def seguro = new XmlSlurper()
        seguro.setFeature('http://apache.org/xml/features/disallow-doctype-decl', true)
        // Sin un ErrorHandler propio, el parser de SAX escribe el fallo por la salida
        // de error ANTES de lanzar. Aquí lo silenciamos para que la demo se lea.
        seguro.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
            @Override
            void fatalError(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
                throw e
            }
        })
        try {
            seguro.parseText('<!DOCTYPE x [<!ENTITY e SYSTEM "file:///etc/passwd">]><x>&e;</x>')
        } catch (Exception e) {
            show('con DOCTYPE prohibido', e.class.simpleName)
        }
        bullet('Con la comprobación activada, el documento se rechaza de entrada.')

        section('La regla')

        bullet('XML de dentro de tu sistema: parsea y ya.')
        bullet('XML que viene de fuera: prohíbe el DOCTYPE, desactiva las entidades')
        bullet('externas y pon un límite de tamaño.')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita un `.text()` y compara el nodo con una cadena: mira qué da.
//  2. Intenta modificar el árbol de un XmlSlurper y lee el error.
//  3. Escribe el catálogo entero con MarkupBuilder a partir de una lista de mapas.
//  4. Añade un tercer espacio de nombres al ejemplo SOAP y navégalo.
