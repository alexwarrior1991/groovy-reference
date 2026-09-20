package com.alejandro.c03strings

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.quoted
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  03.2 · GString: perezoso, y no es un String
//
//  QUÉ ES
//    Un GString no guarda el texto ya montado: guarda los TROZOS fijos y las
//    EXPRESIONES por separado, y sólo los junta cuando alguien pide su `toString()`.
//
//  POR QUÉ IMPORTA
//    De ahí salen las dos sorpresas que más tiempo cuestan en Groovy:
//      · si la expresión es un objeto mutable, el texto cambia después de escribirlo
//      · un GString NO es igual a un String con el mismo contenido, y como clave de
//        mapa no encuentra nada
//
//  ERRORES COMUNES
//    · Usar un GString como clave de un mapa.
//    · Compararlo con `equals()` contra un String.
//    · Guardar un GString en una estructura y esperar que no cambie.
//    · Pasarlo a una API de Java que espera `String` sin convertirlo.
// =====================================================================================

class C03_02GStringTraps {

    /**
     * Qué hay dentro de un GString.
     */
    static void demoWhatIsInside() {
        section('Un GString guarda los trozos, no el resultado')

        def nombre = 'Ana'
        def edad = 30
        GString saludo = "Hola $nombre, tienes $edad"

        show('valores (las expresiones)', saludo.values)
        show('cadenas (los trozos fijos)', saludo.strings)
        show('toString()', saludo.toString())
        bullet('El texto final se monta al pedirlo, no al escribir la línea.')

        section('Por eso se le llama "perezoso"')

        bullet('`"$a + $b"` no calcula nada hasta que alguien imprime el resultado.')
        bullet('Es lo que permite el truco de la demo siguiente… y la trampa.')
    }

    /**
     * La pereza en acción: el texto cambia después de escribirlo.
     */
    static void demoLaziness() {
        section('Con un objeto mutable, el GString "ve" los cambios')

        def carrito = new StringBuilder('vacío')
        def mensaje = "El carrito está: $carrito"

        show('antes', mensaje.toString())

        carrito.length = 0
        carrito.append('con 3 productos')

        show('después de tocar el objeto', mensaje.toString())
        bullet('La MISMA variable `mensaje`, dos textos distintos. Nadie la reasignó.')

        section('Con un valor inmutable no pasa: se captura la referencia')

        def n = 1
        def texto = "n vale $n"
        n = 99
        show('tras n = 99', texto.toString())
        bullet('Aquí `n` se reasigna: el GString sigue apuntando al 1 original.')
        bullet('La diferencia es MUTAR el objeto frente a REASIGNAR la variable.')

        section('Cuándo esto es útil')

        bullet('Trazas y logs: el texto no se monta si nadie lo imprime.')
        bullet('Plantillas que se rellenan más tarde.')

        section('Cuándo muerde')

        bullet('Guardar un GString en una lista o un mapa y usarlo mucho después.')
        bullet('La solución es siempre la misma: `.toString()` en cuanto lo tengas.')
        show('congelado con toString()', congelar())
    }

    private static String congelar() {
        def cambiante = new StringBuilder('original')
        String fijado = "valor: $cambiante".toString()
        cambiante.length = 0
        cambiante.append('modificado')
        fijado
    }

    /**
     * La trampa grande: GString como clave de mapa.
     */
    static void demoMapKeyTrap() {
        section('equals() entre GString y String da false')

        def nombre = 'Ana'
        GString comoGString = "Hola $nombre"
        String comoString = 'Hola Ana'

        show('gstring.toString()', quoted(comoGString.toString()))
        show('string', quoted(comoString))
        show('gstring == string  (operador)', comoGString == comoString)
        show('gstring.equals(string)', comoGString.equals(comoString))
        show('mismo hashCode()', comoGString.hashCode() == comoString.hashCode())
        bullet('El OPERADOR `==` sí los compara bien: Groovy lo trata aparte.')
        bullet('Pero `equals()` y `hashCode()` no coinciden, y eso es lo que usan')
        bullet('los mapas, los Set y `contains`.')

        section('Con el subíndice [ ] no pasa nada: Groovy convierte la clave')

        Map<Object, String> conSubindice = [:]
        conSubindice[comoGString] = 'guardado'
        show('clase de la clave guardada', conSubindice.keySet().first().class.simpleName)
        show('conSubindice[comoString]', conSubindice[comoString])
        bullet('`mapa[clave] = valor` llama a `putAt`, que hace `toString()` del GString.')
        bullet('Por eso casi nadie se topa con esta trampa… hasta que se topa.')

        section('Con put() sí: la clave se queda GString')

        Map<Object, String> conPut = [:]
        conPut.put(comoGString, 'guardado con put')
        show('clase de la clave guardada', conPut.keySet().first().class.simpleName)
        show('conPut.get(comoString)', conPut.get(comoString))
        show('conPut[comoString]', conPut[comoString])
        bullet('Guardas con una y buscas con la otra: null.')

        conPut.put(comoString, 'guardado con String')
        show('tamaño tras guardar las dos', conPut.size())
        show('las claves, impresas', conPut.keySet().collect { quoted(it.toString()) })
        bullet('DOS entradas con el mismo texto visible. Un bug muy difícil de ver.')

        section('Y el literal de mapa tampoco convierte')

        Map<Object, String> literal = [(comoGString): 'guardado en el literal']
        show('clase de la clave', literal.keySet().first().class.simpleName)
        show('literal[comoString]', literal[comoString])

        section('Los Set y contains() tienen el mismo problema')

        show('([g, s] as HashSet).size()', ([comoGString, comoString] as HashSet).size())
        show('([g] as HashSet).contains(s)', ([comoGString] as HashSet).contains(comoString))
        show('[g].contains(s)', [comoGString].contains(comoString))
        bullet('Los tres usan `equals()`. El texto se ve igual y el resultado dice que no.')

        section('Curiosidad: unique() sí los une')

        show('[g, s].unique().size()', [comoGString, comoString].unique(false).size())
        bullet('Porque `unique` del GDK compara con su propia lógica, no con equals().')
        bullet('Que unos métodos digan una cosa y otros otra es justo lo peligroso.')

        section('La solución, siempre la misma')

        Map<String, String> correcto = [:]
        correcto.put("Hola $nombre".toString(), 'bien')
        show('correcto[comoString]', correcto[comoString])
        bullet('`.toString()` antes de guardar un GString en cualquier colección.')
        bullet('Y declara el tipo: con `Map<String, …>` la conversión se hace sola.')
    }

    /**
     * GString frente a las APIs que esperan String.
     */
    static void demoInteropWithString() {
        section('La mayoría de las veces funciona, porque Groovy convierte')

        def nombre = 'Ana'
        GString g = "Hola $nombre"

        show('g.length()', g.length())
        show('g.toUpperCase()', g.toUpperCase())
        show('g.split(" ")', g.split(' '))
        bullet('El GDK le da a GString casi todos los métodos de String.')

        section('Y al pasarlo a un método que pide String, se convierte')

        show('recibeString(g)', recibeString(g))
        show('tipo que llega', tipoQueLlega(g))
        bullet('La coerción de parámetro SÍ ocurre aquí: GString implementa CharSequence')
        bullet('y Groovy sabe convertirlo. No es el caso de String a número (02.12).')

        section('Dónde NO se convierte: cuando el tipo es Object')

        show('guardadoComoObject(g)', guardadoComoObject(g))
        bullet('Si nadie declara `String`, el GString viaja tal cual. Y entonces')
        bullet('acaba en un mapa, en un Set o en una comparación, y ahí empieza el lío.')

        section('La regla')

        bullet('Un GString es para IMPRIMIR. En cuanto lo guardes, `.toString()`.')
    }

    private static String recibeString(String s) { s.reverse() }

    private static String tipoQueLlega(String s) { s.class.simpleName }

    private static String guardadoComoObject(Object o) { o.class.simpleName }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. En `demoLaziness`, cambia el StringBuilder por una lista y añádele elementos.
//  2. Quita el `.toString()` de `congelar()` y compara la salida.
//  3. Mete tres GString distintos con el mismo texto en un Set y cuenta el tamaño.
//  4. Declara `Map<String, String>` y mete una clave GString: mira de qué clase acaba
//     siendo la clave.
