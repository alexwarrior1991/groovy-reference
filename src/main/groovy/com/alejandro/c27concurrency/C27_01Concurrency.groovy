package com.alejandro.c27concurrency

import groovy.transform.Synchronized
import groovy.transform.WithReadLock
import groovy.transform.WithWriteLock

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  27.1 · Concurrencia
//
//  QUÉ ES
//    Groovy no tiene corrutinas ni un modelo de concurrencia propio: usa el de la JVM.
//    Lo que añade son atajos —`Thread.start { }`, `@Synchronized`, `@WithReadLock`— y
//    la comodidad de pasar closures a `ExecutorService` y `CompletableFuture`.
//
//  POR QUÉ IMPORTA
//    Los hilos virtuales (JDK 21) han cambiado el cálculo: ya no hace falta un pool
//    pequeño y cuidado para tareas de E/S. Y el MOP de Groovy tiene implicaciones de
//    concurrencia que conviene conocer.
//
//  SEGURIDAD DE LAS DEMOS
//    Todas usan tiempos de milisegundos, esperan a sus hilos y cierran sus pools.
//    Ninguna se cuelga ni deja hilos vivos.
//
//  ERRORES COMUNES
//    · Compartir un objeto mutable sin sincronizar y creer que "casi nunca falla".
//    · No cerrar el ExecutorService y dejar la JVM sin terminar.
//    · Usar un pool de tamaño fijo para tareas de E/S con hilos virtuales disponibles.
// =====================================================================================

class C27_01Concurrency {

    /**
     * Hilos.
     */
    static void demoThreads() {
        section('`Thread.start { }`: un hilo en una línea')

        def resultados = Collections.synchronizedList([])
        def hilo = Thread.start { resultados << "desde ${Thread.currentThread().name}" }
        hilo.join()
        show('resultado', resultados)
        bullet('`Thread.start` crea el hilo Y lo arranca. `Thread.startDaemon` hace')
        bullet('lo mismo con un hilo demonio.')

        section('Varios hilos, esperándolos todos')

        def salida = Collections.synchronizedList([])
        List<Thread> hilos = (1..5).collect { int n ->
            Thread.start { sleep(5); salida << n * n }
        }
        hilos*.join()
        show('resultados (en cualquier orden)', salida.toSorted())
        bullet('`hilos*.join()` es el spread del capítulo 04: espera a todos.')

        section('Hilos virtuales (JDK 21+): baratos por millares')

        long t0 = System.nanoTime()
        def contador = new AtomicInteger()
        List<Thread> virtuales = (1..2000).collect {
            Thread.ofVirtual().start { sleep(1); contador.incrementAndGet() }
        }
        virtuales*.join()
        long ms = (System.nanoTime() - t0) / 1_000_000L

        show('hilos virtuales lanzados', virtuales.size())
        show('todos terminaron', contador.get())
        show('tiempo total', "${ms} ms")
        bullet('Dos mil hilos de plataforma serían inviables; virtuales, no cuestan')
        bullet('casi nada porque no ocupan un hilo del sistema mientras esperan.')
        bullet('Es EL cambio de los últimos años para tareas de E/S.')

        section('Lo que NO arreglan los hilos virtuales')

        bullet('Siguen sin servir para cálculo puro: ahí lo que limita es la CPU.')
        bullet('Y siguen necesitando sincronización: no eliminan las carreras.')
    }

    /**
     * El problema: estado compartido.
     */
    static void demoSharedState() {
        section('Un contador sin proteger')

        def roto = new ContadorRoto()
        ejecutarEnParalelo(8, 2000) { roto.incrementar() }
        show('esperado', 16000)
        show('obtenido', roto.valor)
        bullet('`valor++` son TRES operaciones: leer, sumar, escribir. Dos hilos')
        bullet('pueden leer el mismo valor y una de las sumas se pierde.')

        section('Con @Synchronized')

        def sincronizado = new ContadorSincronizado()
        ejecutarEnParalelo(8, 2000) { sincronizado.incrementar() }
        show('obtenido', sincronizado.valor)
        bullet('`@Synchronized` genera el `synchronized` sobre un candado PRIVADO,')
        bullet('no sobre `this`: nadie de fuera puede bloquearte sin querer.')

        section('Con un AtomicInteger, que es mejor idea')

        def atomico = new AtomicInteger()
        ejecutarEnParalelo(8, 2000) { atomico.incrementAndGet() }
        show('obtenido', atomico.get())
        bullet('Sin bloqueos: usa una instrucción atómica del procesador. Para un')
        bullet('contador, es la respuesta correcta.')

        section('Colecciones concurrentes')

        def mapa = new ConcurrentHashMap<String, Integer>()
        ejecutarEnParalelo(4, 500) { mapa.merge('clave', 1) { a, b -> a + b } }
        show('ConcurrentHashMap', mapa.clave)

        def listaNormal = []
        def listaSincronizada = Collections.synchronizedList([])
        ejecutarEnParalelo(4, 500) { listaSincronizada << 1 }
        show('lista sincronizada', listaSincronizada.size())
        bullet('Una `ArrayList` compartida puede perder elementos o lanzar. Usa')
        bullet('`ConcurrentHashMap`, `CopyOnWriteArrayList` o sincroniza.')

        section('@WithReadLock y @WithWriteLock')

        def cache = new CacheConLocks()
        cache.poner('a', 1)
        ejecutarEnParalelo(4, 200) { cache.leer('a') }
        show('lecturas concurrentes permitidas', cache.leer('a'))
        bullet('Varias lecturas a la vez; las escrituras, en exclusiva. Es lo')
        bullet('correcto cuando se lee mucho más de lo que se escribe.')

        section('La regla, por orden de preferencia')

        bullet('1. No compartir estado. Que cada hilo tenga el suyo.')
        bullet('2. Compartir sólo INMUTABLES (@Immutable, capítulo 12).')
        bullet('3. Usar las clases atómicas y las colecciones concurrentes.')
        bullet('4. Y sólo si no queda otra, candados.')
    }

    private static void ejecutarEnParalelo(int hilos, int vueltas, Closure<?> accion) {
        List<Thread> lanzados = (1..hilos).collect {
            Thread.start { vueltas.times { accion() } }
        }
        lanzados*.join()
    }

    static class ContadorRoto {
        int valor = 0

        void incrementar() { valor = valor + 1 }
    }

    static class ContadorSincronizado {
        private int valor = 0

        @Synchronized
        void incrementar() { valor = valor + 1 }

        @Synchronized
        int getValor() { valor }
    }

    static class CacheConLocks {
        private final Map<String, Integer> datos = [:]

        @WithWriteLock
        void poner(String clave, int valor) { datos[clave] = valor }

        @WithReadLock
        Integer leer(String clave) { datos[clave] }
    }

    /**
     * ExecutorService y CompletableFuture.
     */
    static void demoExecutors() {
        section('Un pool que devuelve resultados')

        ExecutorService pool = Executors.newFixedThreadPool(4)
        try {
            def futuros = (1..5).collect { int n ->
                pool.submit({ sleep(5); n * n } as java.util.concurrent.Callable)
            }
            show('resultados', futuros*.get())
        } finally {
            pool.shutdown()
            pool.awaitTermination(5, TimeUnit.SECONDS)
        }
        bullet('El `finally` con `shutdown()` no es opcional: sin él la JVM puede')
        bullet('quedarse esperando a hilos que ya no hacen nada.')

        section('Con hilos virtuales, un pool por tarea')

        try (ExecutorService virtual = Executors.newVirtualThreadPerTaskExecutor()) {
            def futuros = (1..100).collect { int n ->
                virtual.submit({ sleep(1); n } as java.util.concurrent.Callable)
            }
            show('cien tareas', futuros*.get().sum())
        }
        bullet('`newVirtualThreadPerTaskExecutor` crea un hilo virtual por tarea.')
        bullet('Y como implementa AutoCloseable, el try-with-resources lo cierra.')

        section('CompletableFuture: componer tareas')

        def usuario = CompletableFuture.supplyAsync { sleep(5); [id: 1, nombre: 'Ana'] }
        def pedidos = CompletableFuture.supplyAsync { sleep(5); ['p1', 'p2'] }

        def combinado = usuario.thenCombine(pedidos) { Map u, List p ->
            "${u.nombre} tiene ${p.size()} pedidos"
        }
        show('combinando dos llamadas', combinado.get())
        bullet('Las dos tareas corren a la vez y se juntan al final. Es el patrón')
        bullet('de llamar a dos servicios en paralelo.')

        section('Encadenar transformaciones')

        show('cadena', CompletableFuture.supplyAsync { 10 }
            .thenApply { it * 2 }
            .thenApply { "resultado: $it" }
            .get())

        section('Y tratar los fallos')

        show('con excepción', CompletableFuture
            .supplyAsync { throw new IllegalStateException('falló la tarea') }
            .exceptionally { Throwable e -> "recuperado: ${e.cause.message}" }
            .get())
        bullet('Fíjate en `e.cause`: CompletableFuture envuelve la excepción en una')
        bullet('CompletionException. Es un detalle que despista siempre.')

        section('Esperar a varias')

        def tareas = (1..3).collect { int n -> CompletableFuture.supplyAsync { sleep(2); n } }
        CompletableFuture.allOf(tareas as CompletableFuture[]).get()
        show('allOf', tareas*.get())
    }

    /**
     * Paralelizar colecciones, y lo que hay que saber del MOP.
     */
    static void demoParallelAndMop() {
        section('Paralelizar el proceso de una colección')

        List<Integer> datos = (1..200).toList()

        long secuencial = medir { datos.collect { trabajoLento(it) } }
        long paralelo = medir {
            try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
                datos.collect { int n -> pool.submit({ trabajoLento(n) } as java.util.concurrent.Callable) }*.get()
            }
        }

        show('secuencial', "${secuencial} ms")
        show('en paralelo', "${paralelo} ms")
        bullet('Con tareas que ESPERAN, el paralelismo se nota mucho. Con cálculo')
        bullet('puro, el límite es el número de núcleos.')

        section('Con Stream paralelo, si prefieres la API de Java')

        show('parallelStream', (1..10).toList().parallelStream()
            .map { it * 2 }.toList().sum())
        bullet('Funciona, pero comparte el ForkJoinPool común con todo el proceso.')
        bullet('Para E/S es mejor un ejecutor de hilos virtuales propio.')

        section('El MOP y la concurrencia')

        bullet('Modificar una metaclase (capítulo 16) afecta a TODOS los hilos a la')
        bullet('vez, y no está pensado para hacerse en caliente con carga.')
        bullet('Hazlo al arrancar, una vez, antes de que haya concurrencia.')

        section('Y las categorías, al revés')

        def resultado = 'pendiente'
        use(MiCategoria) {
            Thread hilo = Thread.start {
                try {
                    resultado = 'x'.duplicado()
                } catch (MissingMethodException e) {
                    resultado = 'el otro hilo NO ve la categoría'
                }
            }
            hilo.join()
        }
        show('desde otro hilo', resultado)
        bullet('Las categorías viven en un ThreadLocal (capítulo 17): no cruzan')
        bullet('al hilo que lanzas. Es un fallo silencioso muy difícil de ver.')

        section('La lista de comprobación')

        bullet('¿Hay estado compartido mutable? → protégelo o quítalo.')
        bullet('¿Cierras todos los ExecutorService? → try-with-resources.')
        bullet('¿Tocas metaclases con la aplicación en marcha? → no.')
        bullet('¿Usas categorías dentro de hilos? → no funcionan.')
        bullet('¿Tareas de E/S? → hilos virtuales, y olvídate del tamaño del pool.')
        bullet('¿Cálculo puro? → un pool del tamaño de los núcleos.')
    }

    private static int trabajoLento(int n) {
        sleep(2)
        n * 2
    }

    private static long medir(Closure<?> bloque) {
        long t0 = System.nanoTime()
        bloque()
        ((System.nanoTime() - t0) / 1_000_000L) as long
    }
}

@Category(String)
class MiCategoria {
    String duplicado() { this * 2 }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Sube las vueltas de `ContadorRoto` a 100.000 y mira cuánto se pierde.
//  2. Quita `@Synchronized` y comprueba que vuelve el problema.
//  3. Cambia los 2000 hilos virtuales por hilos de plataforma y mide (con cuidado).
//  4. Quita el `shutdown()` del pool y observa si el programa termina.
//  5. Sustituye `exceptionally` por `handle` y compara qué recibe cada uno.
