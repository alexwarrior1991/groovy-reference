package com.alejandro.c03strings

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.quoted
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  03.1 · Las seis formas de escribir una cadena
//
//  QUÉ ES
//    Groovy tiene seis literales de texto, y la elección determina DOS cosas: si hay
//    interpolación y qué hay que escapar.
//
//      'simple'            String, sin interpolación
//      "doble"             String si no hay $; GString si lo hay
//      '''triple simple''' String multilínea, sin interpolación
//      """triple doble"""  multilínea CON interpolación
//      /slashy/            multilínea con interpolación, sin escapar la barra
//      $/dollar slashy/$   como la anterior, sin escapar tampoco la barra invertida
//
//  POR QUÉ IMPORTA
//    Elegir mal obliga a llenar el texto de barras invertidas. La regla práctica: comilla
//    simple por defecto, doble cuando interpolas, slashy para expresiones regulares y
//    rutas, triple para bloques de texto.
//
//  ERRORES COMUNES
//    · Usar comilla doble por costumbre y meter un `$` sin querer.
//    · Escapar barras en una expresión regular escrita con comillas en vez de /…/.
//    · Creer que una comilla doble SIEMPRE produce un GString: sólo si hay `$`.
// =====================================================================================

class C03_01Literals {

    /**
     * Comilla simple frente a comilla doble.
     */
    static void demoSingleVsDouble() {
        section('Comilla simple: String de Java, literal')

        def simple = 'Hola $nombre, cuesta 10$'
        show('valor', simple)
        show('clase', simple.class.simpleName)
        bullet('El `$` no significa nada: no hay interpolación que valga.')

        section('Comilla doble SIN $: sigue siendo un String')

        def dobleSinDolar = "Hola"
        show('clase', dobleSinDolar.class.simpleName)
        bullet('Groovy sólo crea un GString si de verdad hay algo que interpolar.')

        section('Comilla doble CON $: GString')

        def nombre = 'Ana'
        def dobleConDolar = "Hola $nombre"
        show('valor', dobleConDolar)
        show('clase', dobleConDolar.class.simpleName)
        bullet('`GStringImpl`: no es un String, aunque se comporte casi igual (03.3).')

        section('Las dos sintaxis de interpolación')

        def usuario = [nombre: 'Luis', edad: 30]
        show('$variable', "es $nombre")
        show('${expresión}', "el año que viene: ${usuario.edad + 1}")
        bullet('`$x` sólo vale para una variable o una cadena de propiedades: `$a.b.c`.')
        bullet('Para cualquier otra cosa (llamadas, operadores) hacen falta las llaves.')

        show('$a.b sin llaves', "nombre: $usuario.nombre")
        show('con método hace falta ${}', "en mayúsculas: ${usuario.nombre.toUpperCase()}")

        section('Escapar el dólar')

        show('precio', "cuesta 10\$")
        show('o con comilla simple', 'cuesta 10$')
        bullet('Si no hay que interpolar nada, la comilla simple ahorra el escape.')
    }

    /**
     * Cadenas multilínea.
     */
    static void demoMultiline() {
        section('Triple comilla simple: multilínea literal')

        def bloque = '''\
            línea 1
            línea 2'''
        show('líneas', bloque.readLines().size())
        show('contenido', quoted(bloque.replace('\n', '\\n')))
        bullet('La barra invertida al final de la primera línea evita el salto inicial.')

        section('stripIndent() quita la sangría común')

        def sangrado = '''
            primera
            segunda
        '''.stripIndent().trim()
        show('tras stripIndent', quoted(sangrado.replace('\n', '\\n')))
        bullet('Sin esto, el texto arrastra la sangría del código fuente.')
        bullet('`stripMargin()` hace lo mismo tomando `|` como borde:')

        def conMargen = '''
            |primera
            |segunda'''.stripMargin().trim()
        show('tras stripMargin', quoted(conMargen.replace('\n', '\\n')))

        section('Triple comilla doble: multilínea CON interpolación')

        def total = 42
        def informe = """
            |Informe
            |Total: $total
            |Doble: ${total * 2}""".stripMargin().trim()
        println informe.readLines().collect { "    $it" }.join('\n')
        show('clase', informe.class.simpleName)
        bullet('Ojo: tras `stripMargin()` ya es un String, no un GString.')
    }

    /**
     * Slashy strings: para expresiones regulares y rutas.
     */
    static void demoSlashy() {
        section('El problema que resuelven')

        // Para buscar un dígito seguido de un punto, la expresión regular es \d\. —
        // y con comillas hay que escapar cada barra invertida:
        def conComillas = "\\d\\."
        def conSlashy = /\d\./
        show('con comillas', quoted(conComillas))
        show('con slashy', quoted(conSlashy))
        show('¿son iguales?', conComillas == conSlashy)
        bullet('Con comillas hay que duplicar cada `\\`. Con /…/, no.')

        section('Una expresión regular de verdad')

        def fecha = '2026-09-20'
        def patron = /(\d{4})-(\d{2})-(\d{2})/
        def m = fecha =~ patron
        show('¿encaja?', m.matches())
        show('año', m[0][1])
        bullet('Las expresiones regulares son el capítulo 19. Aquí sólo el literal.')

        section('Slashy también interpola')

        def anio = 2026
        show('con interpolación', /año: $anio/)

        section('Lo que NO se puede escribir con slashy')

        bullet('Una barra `/` hay que escaparla: /a\\/b/.')
        bullet('Una cadena vacía: // es un comentario.')
        bullet('Para esos casos, el "dollar slashy": $/…/$')

        def dollarSlashy = $/C:\Users\ana/$
        show('$/ruta de Windows/$', dollarSlashy)
        bullet('Ahí ni la barra ni la barra invertida necesitan escape.')
    }

    /**
     * Cuál elegir: la tabla.
     */
    static void demoWhichToUse() {
        section('La tabla')

        bullet('literal              interpola   multilínea   escapar')
        bullet('──────────────────   ─────────   ──────────   ──────────────────')
        bullet("'simple'             no          no           \\' y \\\\")
        bullet('"doble"              SÍ          no           $ " y \\\\')
        bullet("'''triple'''         no          SÍ           \\\\")
        bullet('"""triple"""         SÍ          SÍ           $ y \\\\')
        bullet('/slashy/             SÍ          SÍ           / y $')
        bullet('$/dollar slashy/$    SÍ          SÍ           casi nada')

        section('La regla práctica')

        bullet('Por defecto: comilla SIMPLE. Es la más barata de leer.')
        bullet('Interpolas algo: comilla doble.')
        bullet('Expresión regular o ruta: slashy.')
        bullet('Bloque de texto: triple, con stripIndent() o stripMargin().')

        section('Por qué la comilla simple por defecto')

        bullet('Deja claro de un vistazo que ahí NO hay interpolación.')
        bullet('Evita el GString y sus sorpresas (03.3, 03.4).')
        bullet('Y un `$` accidental no se convierte en un error raro.')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Escribe "cuesta 10$" sin escapar el dólar y lee el error del compilador.
//  2. Quita el `stripMargin()` del informe y mira cómo sale la sangría.
//  3. Escribe la expresión /(\d{4})-(\d{2})/ con comillas dobles, contando las barras
//     invertidas que necesitas.
//  4. Prueba `"Hola".class` y `"Hola $nombre".class` y comprueba que no son la misma.
