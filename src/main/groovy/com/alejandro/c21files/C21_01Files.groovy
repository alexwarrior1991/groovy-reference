package com.alejandro.c21files

import java.nio.file.Files
import java.nio.file.Path

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.quoted
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  21.1 · Ficheros: File, Path y el GDK
//
//  QUÉ ES
//    El GDK le añade a `java.io.File` y a `java.nio.file.Path` las propiedades y
//    métodos que hacen falta para leer y escribir sin ceremonia: `.text`, `.bytes`,
//    `eachLine`, `withReader`, `withWriter`, `traverse`.
//
//  POR QUÉ IMPORTA
//    Leer un fichero entero es `fichero.text`. Escribirlo, `fichero.text = '...'`. Lo
//    que en Java son diez líneas con try-with-resources aquí es una.
//
//  SEGURIDAD DE LAS DEMOS
//    Todo lo de este capítulo ocurre en el directorio temporal del sistema y se borra
//    al terminar. Ninguna demo toca tu proyecto ni tu disco fuera de ahí.
//
//  ERRORES COMUNES
//    · `.text` sobre un fichero de un giga: lo carga entero en memoria.
//    · Olvidar la codificación y que los acentos lleguen rotos.
//    · No cerrar el recurso (o no usar `withReader`, que lo cierra por ti).
// =====================================================================================

class C21_01Files {

    /**
     * Leer y escribir.
     */
    static void demoReadWrite() {
        enUnDirectorioTemporal { Path dir ->
            section('Escribir: `.text = …`')

            File fichero = new File(dir.toFile(), 'ejemplo.txt')
            fichero.text = 'primera línea\nsegunda línea\ntercera línea\n'
            show('existe', fichero.exists())
            show('tamaño en bytes', fichero.length())

            section('Leer: `.text`')

            show('contenido', quoted(fichero.text.replace('\n', '\\n')))
            show('líneas', fichero.readLines())
            show('número de líneas', fichero.readLines().size())

            section('Añadir al final')

            fichero << 'cuarta línea\n'
            fichero.append('quinta línea\n')
            show('tras añadir dos', fichero.readLines().size())
            bullet('`<<` sobre un File añade; `.text =` SUSTITUYE el contenido entero.')

            section('La codificación importa')

            File conAcentos = new File(dir.toFile(), 'acentos.txt')
            conAcentos.setText('añoración cañí', 'UTF-8')
            show('leído en UTF-8', conAcentos.getText('UTF-8'))
            show('leído en ISO-8859-1', conAcentos.getText('ISO-8859-1'))
            bullet('El segundo sale mal. `.text` usa la codificación por defecto de la')
            bullet('plataforma: en algo serio, dila SIEMPRE explícitamente.')

            section('Bytes, para lo que no es texto')

            File binario = new File(dir.toFile(), 'datos.bin')
            binario.bytes = [0, 1, 2, 255] as byte[]
            show('bytes leídos', binario.bytes)
        }
    }

    /**
     * Leer sin cargarlo todo en memoria.
     */
    static void demoStreaming() {
        enUnDirectorioTemporal { Path dir ->
            File grande = new File(dir.toFile(), 'grande.txt')
            grande.withWriter('UTF-8') { w ->
                (1..1000).each { w.writeLine("línea número $it") }
            }
            show('líneas escritas', grande.readLines().size())

            section('`eachLine`: una línea cada vez')

            int contadas = 0
            grande.eachLine('UTF-8') { String linea -> contadas++ }
            show('contadas', contadas)
            bullet('No carga el fichero entero: va línea a línea. Es LO que hay que')
            bullet('usar con ficheros grandes.')

            section('Con el número de línea')

            List<String> primeras = []
            grande.eachLine('UTF-8', 1) { String linea, int numero ->
                if (numero <= 3) primeras << "$numero: $linea"
            }
            show('las tres primeras', primeras)

            section('`withReader` y `withWriter` cierran solos')

            String primeraLinea = grande.withReader('UTF-8') { r -> r.readLine() }
            show('primera línea', primeraLinea)
            bullet('Cierran el recurso pase lo que pase, incluso si el bloque lanza.')
            bullet('Es el `withCloseable` del capítulo 18, especializado.')

            section('`splitEachLine`: trocear mientras se lee')

            File csv = new File(dir.toFile(), 'datos.csv')
            csv.text = 'ana,30\nluis,25\neva,35\n'
            List<String> nombres = []
            csv.splitEachLine(',') { List<String> campos -> nombres << campos[0] }
            show('nombres', nombres)

            section('`filterLine`: filtrar al vuelo')

            def soloPares = grande.filterLine { String l -> l.endsWith('0') }
            show('líneas acabadas en 0', soloPares.toString().readLines().size())

            section('La regla')

            bullet('`.text` y `readLines()` sólo si el fichero cabe en memoria.')
            bullet('Para todo lo demás: `eachLine`, `withReader` o `splitEachLine`.')
        }
    }

    /**
     * Path, directorios y recorrer.
     */
    static void demoPathsAndDirectories() {
        enUnDirectorioTemporal { Path dir ->
            section('Path tiene los mismos métodos, gracias a groovy-nio')

            Path fichero = dir.resolve('con-path.txt')
            fichero.text = 'escrito a través de Path'
            show('leído', fichero.text)
            show('nombre', fichero.fileName)
            show('¿existe?', Files.exists(fichero))

            section('Crear una estructura de directorios')

            ['uno/a.txt', 'uno/b.log', 'dos/tres/c.txt'].each { String ruta ->
                Path p = dir.resolve(ruta)
                Files.createDirectories(p.parent)
                p.text = "contenido de $ruta"
            }

            section('Listar')

            show('sólo el primer nivel', dir.toFile().listFiles()*.name.sort())
            show('sólo directorios', dir.toFile().listFiles().findAll { it.directory }*.name.sort())

            section('`eachFileRecurse`: todo el árbol')

            List<String> encontrados = []
            dir.toFile().eachFileRecurse { File f -> if (f.file) encontrados << f.name }
            show('ficheros', encontrados.sort())

            section('`traverse`: con filtros')

            List<String> soloTxt = []
            dir.toFile().traverse(nameFilter: ~/.*\.txt/) { File f -> soloTxt << f.name }
            show('sólo .txt', soloTxt.sort())

            List<String> hastaUnNivel = []
            dir.toFile().traverse(maxDepth: 0, type: groovy.io.FileType.FILES) { File f ->
                hastaUnNivel << f.name
            }
            show('sin bajar de nivel', hastaUnNivel.sort())
            bullet('`traverse` acepta nameFilter, maxDepth, type, excludeFilter y más.')

            section('Operaciones sobre ficheros')

            File origen = new File(dir.toFile(), 'uno/a.txt')
            File destino = new File(dir.toFile(), 'copia.txt')
            destino.text = origen.text
            show('copiado', destino.text)
            show('borrado', destino.delete())

            section('El directorio temporal, que es donde estamos')

            show('ruta', dir.toString().replaceAll(/[^\/]+$/, '…'))
            bullet('Todo esto se borra al terminar la demo.')
        }
    }

    /**
     * Un caso completo: CSV a objetos y vuelta.
     */
    static void demoCsvCompleto() {
        enUnDirectorioTemporal { Path dir ->
            section('El fichero de partida, con sus casos límite')

            File csv = new File(dir.toFile(), 'usuarios.csv')
            csv.setText('''\
                nombre,edad,ciudad
                Ana García,30,Madrid
                Luis Pérez,25,Bilbao

                Eva Ruiz,,Valencia
                Marta,cuarenta,Sevilla
                '''.stripIndent(), 'UTF-8')

            show('líneas en bruto', csv.readLines().size())
            bullet('Hay una línea en blanco, una edad vacía y una edad que no es')
            bullet('un número. Es lo normal en un fichero de verdad.')

            section('Parsear, decidiendo qué hacer con cada problema')

            def resultado = leerCsv(csv)
            show('filas correctas', resultado.usuarios.size())
            show('usuarios', resultado.usuarios*.nombre)
            show('problemas', resultado.problemas)

            section('Las decisiones de diseño del parseo')

            bullet('1. Las líneas en blanco se ignoran en silencio: son ruido.')
            bullet('2. Una edad vacía es un dato que FALTA: null, y se acepta.')
            bullet('3. Una edad que no es un número es un ERROR: se registra y se')
            bullet('   descarta la fila, pero NO se aborta el fichero entero.')
            bullet('4. El resultado lleva los problemas como DATOS (capítulo 18), no')
            bullet('   como una excepción.')

            section('Escribir el resultado de vuelta')

            File salida = new File(dir.toFile(), 'limpio.csv')
            escribirCsv(salida, resultado.usuarios)
            show('escrito', salida.readLines())

            section('Lo que este parser NO hace')

            bullet('Comillas con comas dentro: "García, Ana". Eso ya no es partir por')
            bullet('comas, es un parser de CSV de verdad. Usa una librería.')
            bullet('El objetivo aquí es enseñar el flujo, no reescribir OpenCSV.')
        }
    }

    private static Map leerCsv(File fichero) {
        List<String> lineas = fichero.readLines('UTF-8')
        List<String> cabecera = lineas.first().tokenize(',')
        List<Map> usuarios = []
        List<String> problemas = []

        lineas.tail().eachWithIndex { String linea, int i ->
            int numero = i + 2
            if (!linea.trim()) return                    // línea en blanco: se ignora

            List<String> campos = linea.split(',', -1).toList()
            if (campos.size() != cabecera.size()) {
                problemas << "línea $numero: se esperaban ${cabecera.size()} campos"
                return
            }

            String edadBruta = campos[1].trim()
            Integer edad = null
            if (edadBruta) {
                if (!edadBruta.isInteger()) {
                    problemas << "línea $numero: la edad '$edadBruta' no es un número"
                    return
                }
                edad = edadBruta.toInteger()
            }

            usuarios << [nombre: campos[0].trim(), edad: edad, ciudad: campos[2].trim()]
        }

        [usuarios: usuarios, problemas: problemas]
    }

    private static void escribirCsv(File fichero, List<Map> usuarios) {
        fichero.withWriter('UTF-8') { w ->
            w.writeLine('nombre,edad,ciudad')
            usuarios.each { Map u -> w.writeLine("${u.nombre},${u.edad ?: ''},${u.ciudad}") }
        }
    }

    /**
     * Crea un directorio temporal, ejecuta el bloque y lo borra entero.
     */
    private static void enUnDirectorioTemporal(Closure<?> bloque) {
        Path dir = Files.createTempDirectory('groovy-reference-')
        try {
            bloque(dir)
        } finally {
            dir.toFile().deleteDir()
        }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade una línea con comillas y una coma dentro al CSV y mira qué hace el parser.
//  2. Cambia `eachLine` por `.text.readLines()` y piensa qué pasaría con un fichero
//     de dos gigas.
//  3. Quita la codificación explícita de `conAcentos` y ejecuta con LANG=C.
//  4. Usa `traverse` con `excludeNameFilter` para saltarte los `.log`.
