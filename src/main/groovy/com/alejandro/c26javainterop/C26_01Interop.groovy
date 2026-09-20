package com.alejandro.c26javainterop

import com.alejandro.c26javainterop.legacy.ConsumidorDeGroovy
import com.alejandro.c26javainterop.legacy.ProcesadorJava
import com.alejandro.c26javainterop.legacy.UsuarioJava

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  26.1 · Llamar a Java desde Groovy, y al revés
//
//  QUÉ ES
//    Groovy compila a bytecode de la JVM, así que la interoperabilidad con Java es
//    total y en las dos direcciones. Pero hay diferencias de SEMÁNTICA que muerden al
//    cruzar la frontera.
//
//  POR QUÉ IMPORTA
//    Casi ningún proyecto Groovy es sólo Groovy: hay librerías de Java por todas
//    partes, y muchos proyectos mezclan los dos lenguajes en el mismo módulo.
//
//  ESTE CAPÍTULO TIENE JAVA DE VERDAD
//    En `src/main/java/com/alejandro/c26javainterop/legacy/` hay tres clases `.java`
//    que compila javac en este mismo build. No son ejemplos en un comentario.
//
//  ERRORES COMUNES
//    · Esperar que Java vea los métodos que Groovy añade por metaprogramación.
//    · Olvidar que un método de Java puede devolver null sin avisar.
//    · Portar código Java que dependía del despacho estático (capítulo 10).
// =====================================================================================

class C26_01Interop {

    /**
     * Groovy llamando a Java.
     */
    static void demoCallingJava() {
        section('Una clase Java, usada desde Groovy')

        def usuario = new UsuarioJava('Ana', 30)
        show('toString', usuario.toString())

        section('Los getters se ven como PROPIEDADES')

        show('usuario.getNombre()', usuario.getNombre())
        show('usuario.nombre', usuario.nombre)
        bullet('Groovy convierte `getXxx()` en la propiedad `xxx`. Funciona con')
        bullet('cualquier clase Java que siga la convención de JavaBeans.')

        section('Y eso incluye los que no tienen campo detrás')

        show('usuario.apodo', usuario.apodo)
        bullet('`getApodo()` devuelve null y la firma no lo dice. En Java tampoco,')
        bullet('pero allí al menos el IDE suele avisar. Aquí, `?.` es tu red:')
        show('usuario.apodo?.length()', usuario.apodo?.length())

        section('El GDK funciona sobre las colecciones que devuelve Java')

        usuario.addRol('admin')
        usuario.addRol('editor')
        show('roles', usuario.roles)
        show('roles*.toUpperCase()', usuario.roles*.toUpperCase())
        show('findAll', usuario.roles.findAll { it.startsWith('a') })
        bullet('`getRoles()` devuelve una `java.util.List` normal, y el GDK le añade')
        bullet('todos sus métodos. No hay que convertir nada.')

        section('Ojo: esa lista es la INTERNA del objeto Java')

        usuario.roles << 'colado sin pasar por addRol'
        show('los roles ahora', usuario.roles)
        bullet('`getRoles()` devuelve la referencia interna. Es el problema de las')
        bullet('copias defensivas del capítulo 08, visto desde el otro lado.')

        section('LA DIFERENCIA GRANDE: el despacho')

        Object valor = 'soy un texto'

        show('llamando desde Groovy', describirDesdeGroovy(valor))
        bullet('Groovy elige por el tipo REAL: String.')

        show('llamando al método Java desde Groovy', usuario.describir(valor))
        bullet('¡También String! Aunque el método sea de Java, quien resuelve la')
        bullet('llamada es GROOVY, porque la llamada está escrita en Groovy.')

        show('con la llamada escrita DENTRO de Java', usuario.describirDesdeJava(valor))
        bullet('Ahora sí sale Object: `describirDesdeJava` llama a `describir(valor)`')
        bullet('en código Java, y javac fijó la sobrecarga al compilar.')

        section('La regla, que no es la que parece')

        bullet('No manda el lenguaje del MÉTODO: manda el lenguaje del CÓDIGO QUE')
        bullet('HACE LA LLAMADA.')
        bullet('Por eso portar un fichero de Java a Groovy puede cambiar qué')
        bullet('sobrecarga se ejecuta, aunque no toques las clases llamadas.')

        section('Las excepciones comprobadas desaparecen')

        show('sin declarar throws', sinThrows())
        bullet('`operacionQuePuedeFallar` declara `throws IOException` y aquí no')
        bullet('hace falta ni capturarla ni declararla (capítulo 18).')

        section('Varargs')

        def procesador = new ProcesadorJava()
        show('juntar("-", "a", "b")', procesador.juntar('-', 'a', 'b'))
        show('con una lista desparramada', procesador.juntar('-', *['x', 'y', 'z']))
    }

    private static String describirDesdeGroovy(Object o) { describir(o) }

    private static String describir(String s) { 'Groovy dice: String' }

    private static String describir(Object o) { 'Groovy dice: Object' }

    private static String sinThrows() {
        new UsuarioJava('x', 1).operacionQuePuedeFallar(false)
    }

    /**
     * Closures donde Java espera una interfaz.
     */
    static void demoSamCoercion() {
        section('Un closure se convierte solo en una interfaz funcional')

        def procesador = new ProcesadorJava()

        show('filtrar con un closure', procesador.filtrar([1, 2, 3, 4, 5]) { it % 2 == 0 })
        show('transformar con un closure', procesador.transformar(['a', 'bb']) { it.length() })
        show('ejecutar un Runnable', procesador.ejecutar { /* una tarea */ })
        bullet('`Predicate`, `Function` y `Runnable` son interfaces de un solo método,')
        bullet('y Groovy convierte el closure sin que tengas que decir nada.')

        section('Y también las de tu propio código Java')

        bullet('Cualquier interfaz con UN método abstracto acepta un closure. Es lo')
        bullet('que hace que las APIs modernas de Java se usen bien desde Groovy.')

        section('Con `as`, cuando el destino es ambiguo')

        def comparador = { String a, String b -> a.length() <=> b.length() } as Comparator
        show('ordenado', ['ccc', 'a', 'bb'].toSorted(comparador))

        section('Streams de Java: funcionan, pero casi nunca hacen falta')

        show('con Stream', [1, 2, 3, 4].stream().filter { it % 2 == 0 }.toList())
        show('con el GDK', [1, 2, 3, 4].findAll { it % 2 == 0 })
        bullet('El GDK es más corto y no hay que abrir y cerrar el flujo. Usa')
        bullet('Streams cuando quieras paralelismo o una API de Java los exija.')
    }

    /**
     * Java llamando a Groovy.
     */
    static void demoJavaCallingGroovy() {
        section('Una clase Groovy consumida desde Java')

        def pedido = new PedidoGroovy(cliente: 'Ana', lineas: ['libro', 'lápiz'])
        def consumidor = new ConsumidorDeGroovy()

        show('Java leyendo una propiedad', consumidor.leerCliente(pedido))
        show('Java llamando a un método', consumidor.resumir(pedido))
        show('Java sobre la lista', consumidor.contarLineas(pedido))
        bullet('Java ve `getCliente()` porque Groovy lo generó a partir de la')
        bullet('propiedad. Desde Java no hay acceso con punto: es `getCliente()`.')

        section('Los valores por defecto generan varias sobrecargas')

        show('etiqueta()', pedido.etiqueta())
        show('etiqueta("factura")', pedido.etiqueta('factura'))
        show('sobrecargas que ve Java', PedidoGroovy.methods.count { it.name == 'etiqueta' })
        bullet('Groovy genera un método por combinación, así que Java las ve todas.')

        section('Lo que Java NO ve')

        bullet('Los métodos añadidos con `metaClass` (capítulo 16): no existen en')
        bullet('el bytecode, así que javac no los conoce.')
        bullet('Lo que resuelve `methodMissing`: lo mismo.')
        bullet('Las categorías: sólo existen dentro del `use`.')
        bullet('Y los traits se ven como interfaz, pero Java no hereda su')
        bullet('implementación (capítulo 11).')

        section('Por eso la regla para código mixto')

        bullet('Lo que Java vaya a consumir, escríbelo con @CompileStatic y tipos')
        bullet('declarados. Las firmas salen limpias y sin sorpresas.')
        bullet('Guarda la metaprogramación para el código que sólo usa Groovy.')

        section('Cómo lo compila este proyecto')

        bullet('javac necesita ver `PedidoGroovy` para compilar')
        bullet('`ConsumidorDeGroovy.java`, pero el bytecode todavía no existe.')
        bullet('Por eso gmavenplus genera STUBS: ficheros .java con sólo las firmas.')
        bullet('El orden en el pom.xml es: generateStubs → compile → removeStubs.')
        bullet('Es la parte del build que más cuesta configurar, y la razón de que')
        bullet('este capítulo tenga Java de verdad y no en un comentario.')
    }

    /**
     * Las diferencias de semántica, en una tabla.
     */
    static void demoSemanticDifferences() {
        section('Las diferencias que muerden al cruzar')

        bullet('tema                  Java                 Groovy')
        bullet('───────────────────   ──────────────────   ──────────────────────')
        bullet('==                    identidad            equals()')
        bullet('sobrecarga            tipo declarado       tipo en ejecución')
        bullet('excepciones           comprobadas          ninguna lo es')
        bullet('campo sin modificador de paquete           propiedad con accesores')
        bullet('1.5                   double               BigDecimal')
        bullet('7 / 2                 3                    3.5')
        bullet('array literal         {1, 2}               [1, 2] es una LISTA')
        bullet('clase interna         estática por defecto igual, pero ojo con')
        bullet('                                           methodMissing (cap. 16)')

        section('El array, que es el que más despista')

        show('en Groovy, [1,2] es', [1, 2].getClass().simpleName)
        int[] deVerdad = [1, 2]
        show('un array de verdad', deVerdad.getClass().simpleName)
        bullet('Si una API de Java pide `int[]`, declara el tipo o usa `as int[]`.')

        section('Y el ==, que al portar código cambia el resultado')

        Integer a = 1000
        Integer b = 1000
        show('en Groovy, a == b', a == b)
        show('en Java sería', 'false (referencias distintas)')
        show('el equivalente en Groovy', a.is(b))
        bullet('Portar un `==` de Java a Groovy casi siempre MEJORA el código, pero')
        bullet('si alguien dependía de la identidad, cambia el comportamiento.')

        section('La regla al portar de Java a Groovy')

        bullet('1. Compila y pasa los tests: casi todo funciona tal cual.')
        bullet('2. Busca los `==` entre objetos y decide qué querías de verdad.')
        bullet('3. Busca las sobrecargas y comprueba cuál se elige ahora.')
        bullet('4. Y sólo entonces, empieza a quitar ceremonia.')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade un método a `PedidoGroovy.metaClass` y llámalo desde `ConsumidorDeGroovy`:
//     verás que javac ni siquiera compila.
//  2. Haz que `UsuarioJava.getRoles()` devuelva una copia y comprueba que ya no se
//     puede colar un rol desde Groovy.
//  3. Quita `@CompileStatic` de `PedidoGroovy` y mira si Java sigue viéndolo igual.
//  4. Llama a `procesador.filtrar` pasándole una clase anónima de Java en vez de un
//     closure y compara cómo se lee.
