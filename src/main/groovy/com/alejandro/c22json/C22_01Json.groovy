package com.alejandro.c22json

import groovy.json.JsonBuilder
import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.Canonical

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  22.1 · JSON
//
//  QUÉ ES
//    `groovy-json` trae cuatro piezas:
//      JsonSlurper    texto JSON → mapas y listas de Groovy
//      JsonOutput     objetos → texto JSON, y `prettyPrint`
//      JsonBuilder    construir JSON con un DSL
//      JsonGenerator  controlar CÓMO se serializa (fechas, nulos, campos excluidos)
//
//  POR QUÉ IMPORTA
//    Un JSON parseado en Groovy es un mapa normal, así que todo lo del capítulo 09
//    funciona sobre él: `*.`, `findAll`, `collectEntries`, navegación con `?.`.
//    No hace falta ni una clase ni una anotación.
//
//  ERRORES COMUNES
//    · Serializar un objeto con contraseñas dentro sin excluir el campo.
//    · Confiar en que un campo existe sin comprobar (`?.` es tu amigo).
//    · Usar `JsonOutput.toJson` sobre fechas y obtener algo ilegible.
// =====================================================================================

class C22_01Json {

    private static final String RESPUESTA = '''
    {
      "estado": "ok",
      "pagina": 1,
      "usuarios": [
        {"id": 1, "nombre": "Ana", "edad": 30, "roles": ["admin", "editor"], "activo": true},
        {"id": 2, "nombre": "Luis", "edad": 25, "roles": ["lector"], "activo": false},
        {"id": 3, "nombre": "Eva", "edad": 35, "roles": ["editor"], "activo": true}
      ],
      "meta": {"total": 3, "siguiente": null}
    }
    '''

    /**
     * Parsear.
     */
    static void demoParsing() {
        section('JsonSlurper devuelve mapas y listas normales')

        def datos = new JsonSlurper().parseText(RESPUESTA)
        show('tipo', datos.getClass().simpleName)
        show('estado', datos.estado)
        show('página', datos.pagina)
        show('tipo de "usuarios"', datos.usuarios.getClass().simpleName)
        bullet('No hay clases generadas ni anotaciones: es un mapa. Y por eso todo')
        bullet('lo del capítulo 09 funciona sobre él.')

        section('Navegar')

        show('primer usuario', datos.usuarios[0].nombre)
        show('todos los nombres', datos.usuarios*.nombre)
        show('roles aplanados', datos.usuarios*.roles.flatten().unique())
        show('un campo que no existe', datos.noExiste)
        show('una ruta que no existe', datos.meta?.paginacion?.total)

        section('Consultar como cualquier colección')

        show('los activos', datos.usuarios.findAll { it.activo }*.nombre)
        show('edad media', datos.usuarios*.edad.average())
        show('el mayor', datos.usuarios.max { it.edad }.nombre)
        show('indexado por id', datos.usuarios.collectEntries { [it.id, it.nombre] })
        show('agrupado por rol', datos.usuarios
            .collectMany { u -> u.roles.collect { [rol: it, nombre: u.nombre] } }
            .groupBy { it.rol }
            .collectEntries { rol, lista -> [rol, lista*.nombre] })

        section('Los tipos que produce')

        show('número entero', datos.pagina.getClass().simpleName)
        show('número decimal', new JsonSlurper().parseText('{"x": 1.5}').x.getClass().simpleName)
        show('booleano', datos.usuarios[0].activo.getClass().simpleName)
        show('null', datos.meta.siguiente)
        bullet('Los decimales salen como BigDecimal: coherente con el capítulo 02.')

        section('JSON mal formado')

        try {
            new JsonSlurper().parseText('{"roto": ')
        } catch (Exception e) {
            show('parseText de un JSON roto', e.class.simpleName)
        }
        bullet('Lanza. Con datos de fuera, captúralo y devuelve un error tratable.')
    }

    /**
     * Generar.
     */
    static void demoGenerating() {
        section('JsonOutput.toJson sobre estructuras normales')

        def usuario = [nombre: 'Ana', edad: 30, roles: ['admin']]
        show('toJson', JsonOutput.toJson(usuario))

        section('prettyPrint para leerlo')

        println JsonOutput.prettyPrint(JsonOutput.toJson(usuario)).readLines()
            .collect { "    $it" }.join('\n')

        section('Sobre un objeto, usa sus propiedades')

        show('un POGO', JsonOutput.toJson(new Producto(nombre: 'Libro', precio: 20)))
        bullet('Serializa las PROPIEDADES, así que los getters calculados también')
        bullet('salen. Ojo con eso.')

        section('JsonBuilder: un DSL para construirlo')

        def builder = new JsonBuilder()
        builder.pedido {
            id 1001
            cliente {
                nombre 'Ana'
                email 'ana@ejemplo.com'
            }
            lineas([
                [producto: 'Libro', cantidad: 2],
                [producto: 'Lápiz', cantidad: 5],
            ])
            total 45.50
        }
        println builder.toPrettyString().readLines().collect { "    $it" }.join('\n')
        bullet('Es un builder con delegate (capítulo 07): cada nombre desconocido se')
        bullet('convierte en una clave. El capítulo 24 explica el mecanismo.')

        section('Y también sirve para listas')

        def listaBuilder = new JsonBuilder()
        listaBuilder([1, 2, 3].collect { [n: it, cuadrado: it * it] })
        show('lista de objetos', listaBuilder.toString())
    }

    @Canonical
    static class Producto {
        String nombre
        BigDecimal precio

        BigDecimal getConIva() { precio * 1.21 }
    }

    /**
     * JsonGenerator: controlar la serialización.
     */
    static void demoGenerator() {
        section('El problema: lo que NO quieres serializar')

        def usuario = new UsuarioConSecreto(
            nombre: 'Ana',
            contraseña: 'no-deberia-salir',
            ultimoAcceso: java.time.LocalDate.of(2026, 9, 20),
            apodo: null,
        )
        show('sin control', JsonOutput.toJson(usuario))
        bullet('Ha salido la contraseña, la fecha en un formato raro y un null inútil.')

        section('Con un JsonGenerator configurado')

        def generador = new JsonGenerator.Options()
            .excludeFieldsByName('contraseña')
            .excludeNulls()
            .addConverter(java.time.LocalDate) { java.time.LocalDate d -> d.toString() }
            .build()

        show('controlado', generador.toJson(usuario))
        bullet('Tres líneas de configuración y el JSON ya es publicable.')

        section('Las opciones que más se usan')

        bullet('excludeFieldsByName(…)   quitar campos por nombre')
        bullet('excludeFieldsByType(…)   quitar por tipo')
        bullet('excludeNulls()           no incluir los nulos')
        bullet('addConverter(Tipo) { }   cómo serializar un tipo concreto')
        bullet('dateFormat(…)            formato de las fechas antiguas')
        bullet('disableUnicodeEscaping() dejar los acentos tal cual')

        section('Una advertencia de seguridad')

        bullet('Serializar un objeto entero "porque es cómodo" es como acaban las')
        bullet('contraseñas y los tokens en los logs y en las respuestas de una API.')
        bullet('Decide qué sale. Un generador configurado, o un mapa construido a')
        bullet('mano con los campos que de verdad quieres publicar.')

        show('la otra forma: un mapa explícito', JsonOutput.toJson([
            nombre: usuario.nombre,
            ultimoAcceso: usuario.ultimoAcceso.toString(),
        ]))
    }

    static class UsuarioConSecreto {
        String nombre
        String contraseña
        java.time.LocalDate ultimoAcceso
        String apodo
    }

    /**
     * Un caso completo.
     */
    static void demoRoundTrip() {
        section('De JSON a objetos de dominio y vuelta')

        def datos = new JsonSlurper().parseText(RESPUESTA)

        List<Usuario> usuarios = datos.usuarios.collect { Map m ->
            new Usuario(
                id: m.id as int,
                nombre: m.nombre as String,
                edad: m.edad as int,
                roles: (m.roles as List<String>).asImmutable(),
                activo: m.activo as boolean,
            )
        }

        show('objetos creados', usuarios.size())
        show('el primero', usuarios[0])
        bullet('La conversión a objetos es donde se VALIDA. Un mapa acepta cualquier')
        bullet('cosa; el constructor de tu clase, no (capítulo 10).')

        section('Trabajar con ellos')

        show('editores', usuarios.findAll { 'editor' in it.roles }*.nombre)
        show('ordenados por edad', usuarios.toSorted { it.edad }*.nombre)

        section('Y de vuelta a JSON, controlando qué sale')

        def generador = new JsonGenerator.Options().excludeNulls().build()
        String salida = generador.toJson([
            estado  : 'ok',
            usuarios: usuarios.findAll { it.activo }.collect {
                [id: it.id, nombre: it.nombre, roles: it.roles]
            },
        ])
        println JsonOutput.prettyPrint(salida).readLines().collect { "    $it" }.join('\n')

        section('Comprobación: lo que sale se vuelve a parsear')

        def reparseado = new JsonSlurper().parseText(salida)
        show('usuarios en la respuesta', reparseado.usuarios.size())
        show('ninguna contraseña', 'contraseña' in reparseado.usuarios[0].keySet())
    }

    @Canonical
    static class Usuario {
        int id
        String nombre
        int edad
        List<String> roles
        boolean activo
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Quita `excludeFieldsByName('contraseña')` y mira el JSON que sale.
//  2. Añade un getter calculado a `Usuario` y comprueba si aparece en el JSON.
//  3. Parsea un JSON con una clave repetida y mira con cuál se queda.
//  4. Serializa un `LocalDateTime` sin converter y después con uno.
