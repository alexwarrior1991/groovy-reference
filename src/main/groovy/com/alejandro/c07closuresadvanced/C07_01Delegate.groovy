package com.alejandro.c07closuresadvanced

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  07.1 · owner, delegate, thisObject y resolveStrategy
//
//  QUÉ ES
//    Cuando dentro de un closure escribes `foo()` o `bar`, y ese nombre no es una
//    variable local ni un parámetro, Groovy tiene que decidir a QUIÉN se lo pide. Los
//    candidatos son tres:
//      thisObject  el objeto donde está escrito el closure
//      owner       quien lo creó: la clase, o el closure de fuera si está anidado
//      delegate    un objeto que puedes poner tú (por defecto, el owner)
//    Y `resolveStrategy` dice en qué ORDEN preguntarles.
//
//  POR QUÉ IMPORTA
//    Es el mecanismo entero de los DSL: `build.gradle`, un `Jenkinsfile`, un
//    `MarkupBuilder` y el `chapter { }` de este repositorio son todos lo mismo: un
//    closure con el `delegate` cambiado.
//
//  ERRORES COMUNES
//    · Dejar la estrategia por defecto (OWNER_FIRST) y no entender por qué el delegate
//      no recibe las llamadas.
//    · Olvidar que cambiar `delegate` MUTA el closure: si se comparte, afecta a todos.
//    · No poner `@DelegatesTo`, y perder el autocompletado y `@CompileStatic`.
// =====================================================================================

class C07_01Delegate {

    /**
     * Los tres candidatos.
     */
    static void demoTheThree() {
        section('Por defecto, delegate y owner son lo mismo')

        def closure = { }
        show('owner', closure.owner.class.simpleName)
        show('delegate', closure.delegate.class.simpleName)
        show('thisObject', closure.thisObject?.class?.simpleName)
        bullet('Sin tocar nada, el delegate ES el owner.')

        section('En un closure anidado, el owner es el closure de fuera')

        def fuera = {
            def dentro = { }
            [owner: dentro.owner.class.simpleName, thisObject: dentro.thisObject?.class?.simpleName]
        }
        show('del closure interior', fuera())
        bullet('`owner` es quien lo creó: aquí, el closure exterior.')
        bullet('`thisObject` salta los closures y da el objeto de verdad.')

        section('Qué significa cada uno')

        bullet('thisObject  el `this` del código: el objeto donde se escribió.')
        bullet('owner       el ámbito que lo creó: la clase o el closure de fuera.')
        bullet('delegate    el que pones tú. Es el que hace posibles los DSL.')
    }

    /**
     * Cambiar el delegate.
     */
    static void demoChangingDelegate() {
        section('Un closure que llama a métodos que él no tiene')

        def receta = {
            añadir 'harina'
            añadir 'agua'
            mezclar()
        }

        // El closure, tal cual, no sabe qué es `añadir`. Se lo damos:
        def masa = new Preparacion()
        receta.delegate = masa
        receta.resolveStrategy = Closure.DELEGATE_FIRST
        receta()

        show('ingredientes', masa.ingredientes)
        show('estado', masa.estado)
        bullet('El MISMO closure, ejecutado contra otro objeto, haría otra cosa.')

        section('Y eso es exactamente un DSL')

        def otraPreparacion = new Preparacion()
        receta.delegate = otraPreparacion
        receta()
        show('otra preparación', otraPreparacion.ingredientes)
        bullet('Gradle hace esto con cada bloque de tu build.gradle.')

        section('Ojo: cambiar el delegate MUTA el closure')

        bullet('Si el closure se comparte o se reutiliza, todos ven el cambio.')
        bullet('Para no pisarlo, clónalo antes:')

        def plantilla = { añadir 'sal' }
        def copiaA = plantilla.clone() as Closure
        def copiaB = plantilla.clone() as Closure
        def a = new Preparacion()
        def b = new Preparacion()
        copiaA.delegate = a; copiaA.resolveStrategy = Closure.DELEGATE_FIRST; copiaA()
        copiaB.delegate = b; copiaB.resolveStrategy = Closure.DELEGATE_FIRST; copiaB()
        show('a', a.ingredientes)
        show('b', b.ingredientes)
        bullet('`rehydrate(delegate, owner, thisObject)` hace lo mismo en un paso.')
    }

    static class Preparacion {
        List<String> ingredientes = []
        String estado = 'sin mezclar'

        void añadir(String ingrediente) { ingredientes << ingrediente }

        void mezclar() { estado = 'mezclado' }
    }

    /**
     * Las cinco estrategias.
     */
    static void demoResolveStrategy() {
        section('Las cinco, y a quién preguntan primero')

        bullet('constante            orden')
        bullet('──────────────────   ──────────────────────────────')
        bullet('OWNER_FIRST (0)      owner → delegate     ← por defecto')
        bullet('DELEGATE_FIRST (1)   delegate → owner')
        bullet('OWNER_ONLY (2)       sólo owner')
        bullet('DELEGATE_ONLY (3)    sólo delegate')
        bullet('TO_SELF (4)          sólo el propio closure')

        section('Cuando los dos tienen el método, la estrategia decide')

        def conflicto = { quienSoy() }
        conflicto.delegate = new Delegado()

        conflicto.resolveStrategy = Closure.OWNER_FIRST
        show('OWNER_FIRST', conflicto())

        conflicto.resolveStrategy = Closure.DELEGATE_FIRST
        show('DELEGATE_FIRST', conflicto())

        conflicto.resolveStrategy = Closure.DELEGATE_ONLY
        show('DELEGATE_ONLY', conflicto())

        section('Y con DELEGATE_ONLY, lo que sólo tiene el owner deja de verse')

        def soloDelegate = { metodoDelOwner() }
        soloDelegate.delegate = new Delegado()
        soloDelegate.resolveStrategy = Closure.DELEGATE_ONLY
        try {
            show('llamando a un método del owner', soloDelegate())
        } catch (Exception e) {
            show('llamando a un método del owner', e.class.simpleName)
        }

        section('Cuál usar')

        bullet('DELEGATE_FIRST para un DSL: quieres que el bloque hable del delegate,')
        bullet('pero que siga viendo los métodos y variables de alrededor.')
        bullet('DELEGATE_ONLY cuando quieras AISLAR el bloque a propósito, para que no')
        bullet('pueda llamar a nada de fuera por accidente.')
        bullet('OWNER_FIRST (el de por defecto) casi nunca es lo que quieres en un DSL.')
    }

    /** El método que "gana" con OWNER_FIRST, por estar en la clase que contiene el closure. */
    static String quienSoy() { 'el owner' }

    static String metodoDelOwner() { 'sólo lo tiene el owner' }

    static class Delegado {
        String quienSoy() { 'el delegate' }
    }

    /**
     * @DelegatesTo: decírselo al compilador.
     */
    static void demoDelegatesTo() {
        section('Sin @DelegatesTo funciona, pero a ciegas')

        bullet('El IDE no autocompleta dentro del bloque.')
        bullet('@CompileStatic no puede comprobarlo y lo rechaza.')
        bullet('Quien lee la firma no sabe qué se puede escribir dentro.')

        section('Con @DelegatesTo, el compilador sabe qué hay dentro')

        def resultado = configurar {
            host = 'ejemplo.com'
            puerto = 8080
            cabecera 'Accept', 'application/json'
        }
        show('configuración', resultado)
        bullet('La firma declara `@DelegatesTo(Configuracion)` y ya está.')

        section('La firma completa, que es lo que hay que copiar')

        bullet('static Configuracion configurar(')
        bullet('        @DelegatesTo(value = Configuracion,')
        bullet('                     strategy = Closure.DELEGATE_FIRST) Closure bloque)')
        bullet('Las dos piezas: a QUIÉN delega y con qué ESTRATEGIA.')

        section('Esto es lo que usa este mismo repositorio')

        bullet('`infra/Demo.groovy` declara `chapter(…, @DelegatesTo(ChapterBuilder)')
        bullet('Closure body)`, y por eso dentro del bloque se puede escribir `demo(…)`.')
        bullet('Ve a leerlo ahora: es el ejemplo más corto que vas a encontrar.')
    }

    static Configuracion configurar(
        @DelegatesTo(value = Configuracion, strategy = Closure.DELEGATE_FIRST) Closure bloque) {
        Configuracion config = new Configuracion()
        bloque.delegate = config
        bloque.resolveStrategy = Closure.DELEGATE_FIRST
        bloque()
        config
    }

    static class Configuracion {
        String host
        int puerto
        Map<String, String> cabeceras = [:]

        void cabecera(String nombre, String valor) { cabeceras[nombre] = valor }

        @Override
        String toString() { "$host:$puerto $cabeceras" }
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Cambia DELEGATE_FIRST por OWNER_FIRST en `configurar` y mira qué deja de funcionar.
//  2. Quita el `clone()` de la demo del delegate compartido y observa el resultado.
//  3. Añade `@CompileStatic` a esta clase: el `configurar { }` seguirá compilando
//     gracias al @DelegatesTo, pero `receta` (sin anotación) no.
//  4. Imprime `owner`, `delegate` y `thisObject` dentro de un closure anidado a tres
//     niveles y dibuja la cadena.
