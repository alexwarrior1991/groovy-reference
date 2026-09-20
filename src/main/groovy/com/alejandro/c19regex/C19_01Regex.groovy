package com.alejandro.c19regex

import java.util.regex.Matcher
import java.util.regex.Pattern

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.quoted
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  19.1 · Los tres operadores de expresiones regulares
//
//  QUÉ ES
//    Groovy le da sintaxis propia a las expresiones regulares de Java:
//      ~/patron/         crea un Pattern (compila la expresión)
//      texto =~ /patron/ crea un Matcher (busca dentro)
//      texto ==~ /patron/ devuelve un booleano (¿encaja ENTERO?)
//    Más el literal `/…/`, que evita duplicar las barras invertidas.
//
//  POR QUÉ IMPORTA
//    La diferencia entre `=~` y `==~` es la que más confunde, y las dos se usan
//    constantemente. Y el literal slashy convierte expresiones ilegibles en legibles.
//
//  ERRORES COMUNES
//    · Usar `=~` esperando un booleano (devuelve un Matcher, que es "cierto" si
//      encuentra algo… y ahí está la confusión).
//    · `==~` esperando que busque dentro: exige que encaje la cadena ENTERA.
//    · Recompilar el patrón dentro de un bucle.
// =====================================================================================

class C19_01Regex {

    /**
     * Los tres operadores.
     */
    static void demoThreeOperators() {
        section('`~/…/` compila el patrón')

        def patron = ~/\d{4}-\d{2}-\d{2}/
        show('tipo', patron.getClass().simpleName)
        show('el patrón', patron.pattern())
        bullet('Compilar una vez y reutilizar es lo correcto si va dentro de un bucle.')

        section('`=~` busca dentro, y devuelve un Matcher')

        def m = 'la fecha es 2026-09-20 y ya' =~ /\d{4}-\d{2}-\d{2}/
        show('tipo', m.getClass().simpleName)
        show('¿encontró algo? (Groovy Truth)', m ? 'sí' : 'no')
        show('lo encontrado', m[0])
        bullet('Un Matcher es "cierto" si encuentra algo. Por eso `if (texto =~ /x/)`')
        bullet('se lee tan bien: es el Groovy Truth del capítulo 05.')

        section('`==~` exige que encaje la cadena ENTERA')

        show("'2026-09-20' ==~ /\\d{4}-\\d{2}-\\d{2}/", '2026-09-20' ==~ /\d{4}-\d{2}-\d{2}/)
        show("'la fecha es 2026-09-20' ==~ …", 'la fecha es 2026-09-20' ==~ /\d{4}-\d{2}-\d{2}/)
        bullet('El segundo es FALSE: sobra texto alrededor. Es la diferencia clave.')

        section('La tabla')

        bullet('operador   devuelve   qué hace')
        bullet('────────   ────────   ──────────────────────────────')
        bullet('~/x/       Pattern    compila')
        bullet('=~         Matcher    busca DENTRO (parcial)')
        bullet('==~        boolean    encaja la cadena ENTERA')

        section('Y el equivalente explícito, por si lo prefieres')

        show('Pattern.compile(...).matcher(...).find()',
            Pattern.compile(/\d+/).matcher('abc 123').find())
        show('.matches()', Pattern.compile(/\d+/).matcher('123').matches())
        bullet('`find()` es `=~`; `matches()` es `==~`. La sintaxis de Groovy es la')
        bullet('misma API de Java con menos ruido.')
    }

    /**
     * Grupos.
     */
    static void demoGroups() {
        section('Grupos por número')

        def m = '2026-09-20' =~ /(\d{4})-(\d{2})-(\d{2})/
        show('¿encaja entero?', m.matches())
        show('m[0]', m[0])
        bullet('`m[0]` es una lista: el 0 es lo encontrado y el resto, los grupos.')
        show('año', m[0][1])
        show('mes', m[0][2])
        show('día', m[0][3])

        section('Desempaquetando con asignación múltiple')

        def m2 = '2026-09-20' =~ /(\d{4})-(\d{2})-(\d{2})/
        m2.matches()
        def (_, anio, mes, dia) = m2[0] as List
        show('año, mes, día', "$anio/$mes/$dia")
        bullet('Es el capítulo 02: `def (a, b) = lista`.')

        section('Grupos CON NOMBRE: mucho más legibles')

        def m3 = '2026-09-20' =~ /(?<anio>\d{4})-(?<mes>\d{2})-(?<dia>\d{2})/
        show('¿encaja?', m3.matches())
        show('m3.group("anio")', m3.group('anio'))
        show('m3.group("mes")', m3.group('mes'))
        bullet('`(?<nombre>…)` evita contar paréntesis, que es donde se falla.')

        section('Varias coincidencias')

        def texto = 'ana@ejemplo.com, luis@otro.es, eva@sitio.org'
        def correos = texto =~ /(\w+)@([\w.]+)/

        show('cuántos', correos.count)
        show('todos', (0..<correos.count).collect { correos[it][0] })
        show('sólo los usuarios', (0..<correos.count).collect { correos[it][1] })

        section('Recorrer con each, que es más idiomático')

        List<String> dominios = []
        (texto =~ /(\w+)@([\w.]+)/).each { todo, usuario, dominio -> dominios << dominio }
        show('dominios', dominios)
        bullet('`each` sobre un Matcher pasa la coincidencia y cada grupo como')
        bullet('parámetros distintos. Es la forma más limpia.')
    }

    /**
     * Sustituir.
     */
    static void demoReplacing() {
        section('replaceAll con texto')

        show('quitar espacios de más', '  hola   mundo  '.replaceAll(/\s+/, ' ').trim())
        show('ocultar números', 'tarjeta 4111 1111'.replaceAll(/\d/, '*'))

        section('Con referencias a los grupos')

        show('dar la vuelta a una fecha',
            '2026-09-20'.replaceAll(/(\d{4})-(\d{2})-(\d{2})/, '$3/$2/$1'))
        bullet('`$1`, `$2`… en el reemplazo. Ojo: en una cadena con comilla DOBLE')
        bullet('hay que escaparlos, porque `$1` sería interpolación de Groovy.')

        section('Con un CLOSURE: esto es lo que Java no tiene')

        show('duplicar cada número', 'a1 b22 c3'.replaceAll(/\d+/) { String n -> (n.toInteger() * 2).toString() })
        show('poner en mayúsculas las palabras largas',
            'hola mundo de groovy'.replaceAll(/\w{5,}/) { String p -> p.toUpperCase() })
        bullet('El closure recibe la coincidencia y devuelve el reemplazo. Permite')
        bullet('lógica de verdad dentro de una sustitución.')

        section('Y con grupos, el closure los recibe todos')

        show('reformatear',
            '2026-09-20'.replaceAll(/(\d{4})-(\d{2})-(\d{2})/) { todo, a, m, d -> "$d de $m de $a" })

        section('replaceFirst: sólo la primera')

        show('replaceAll', 'a-b-c'.replaceAll(/-/, '+'))
        show('replaceFirst', 'a-b-c'.replaceFirst(/-/, '+'))

        section('Escapar texto que viene de fuera')

        String loQueEscribioElUsuario = 'a.b'
        show('sin escapar (el punto es comodín)', 'axb'.replaceAll(loQueEscribioElUsuario, 'X'))
        show('escapado', 'axb'.replaceAll(Pattern.quote(loQueEscribioElUsuario), 'X'))
        bullet('`Pattern.quote` convierte el texto en literal. Si la expresión viene')
        bullet('del usuario y no la escapas, tienes una inyección de expresiones.')
    }

    /**
     * En la práctica.
     */
    static void demoPractical() {
        section('Validar (con `==~`)')

        ['ana@ejemplo.com', 'sin-arroba', 'a@b.c'].each { correo ->
            show(quoted(correo), (correo ==~ /^[\w.+-]+@[\w-]+\.[\w.]{2,}$/) ? 'válido' : 'no válido')
        }
        bullet('Aviso: validar correos con una expresión regular es una aproximación.')
        bullet('La expresión "correcta" del RFC ocupa páginas. Para algo serio,')
        bullet('valida el formato básico y confirma con un correo de verdad.')

        section('Extraer datos de un log')

        def lineas = [
            '2026-09-20 10:15:00 ERROR No se pudo conectar',
            '2026-09-20 10:15:01 INFO Reintentando',
            'línea con formato raro',
            '2026-09-20 10:15:05 ERROR Tiempo agotado',
        ]

        def patron = ~/^(\S+) (\S+) (\w+) (.+)$/
        def entradas = lineas.findResults { String linea ->
            def m = patron.matcher(linea)
            m.matches() ? [fecha: m.group(1), hora: m.group(2), nivel: m.group(3), mensaje: m.group(4)] : null
        }

        show('entradas reconocidas', entradas.size())
        show('las que no encajaron', lineas.size() - entradas.size())
        show('sólo los errores', entradas.findAll { it.nivel == 'ERROR' }*.mensaje)
        bullet('`findResults` hace `collect` + quitar los nulos en una pasada.')

        section('Agrupando por nivel')

        show('por nivel', entradas.countBy { it.nivel })

        section('Trocear con una expresión')

        show("split(/[,;]\\s*/)", 'a, b; c,d'.split(/[,;]\s*/))
        bullet('`split` toma una expresión regular, no un carácter. Por eso')
        bullet("`'a.b'.split('.')` devuelve una lista vacía (capítulo 03).")

        section('Compilar una vez fuera del bucle')

        int vueltas = 50_000
        def compilado = ~/\d+/
        long conCompilar = medir { vueltas.times { ('abc 123' =~ /\d+/).find() } }
        long conCompilado = medir { vueltas.times { compilado.matcher('abc 123').find() } }
        show('compilando en cada vuelta', "${conCompilar} ms")
        show('compilado una vez', "${conCompilado} ms")
        bullet('Si la expresión va en un bucle o en un método muy llamado, guárdala')
        bullet('en una constante `static final`.')
    }

    private static long medir(Closure<?> bloque) {
        bloque()
        long t0 = System.nanoTime()
        bloque()
        ((System.nanoTime() - t0) / 1_000_000L) as long
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia un `==~` por `=~` en la validación y mira qué pasa con "a@b.c y más texto".
//  2. Escribe la expresión de la fecha con comillas dobles en vez de con /…/ y cuenta
//     las barras invertidas que necesitas.
//  3. Usa grupos con nombre en el ejemplo del log en vez de group(1), group(2)…
//  4. Prueba `'axb'.replaceAll('a.b', 'X')` sin `Pattern.quote` y explica el resultado.
