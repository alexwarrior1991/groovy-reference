package com.alejandro.infra

import groovy.transform.CompileStatic

import static com.alejandro.infra.Console.heading
import static com.alejandro.infra.Console.separator
import static com.alejandro.infra.Console.show

// =====================================================================================
//  El lanzador: convierte la lista de capítulos en un programa navegable.
//
//  Sin argumentos abre un menú interactivo; con argumentos ejecuta directamente lo que
//  le pidas. Ver `printUsage()` más abajo para la lista completa de comandos.
// =====================================================================================

@CompileStatic
class Launcher {

    /** Palabras que cierran el menú interactivo. */
    private static final Set<String> QUIT_WORDS = ['q', 'quit', 'exit', 'salir'] as Set

    /** Palabras que vuelven al menú anterior. */
    private static final Set<String> BACK_WORDS = ['b', 'back', 'atras', 'atrás', '0'] as Set

    private final List<Chapter> chapters

    /**
     * Un ÚNICO lector para toda la sesión.
     *
     * Crear un `BufferedReader` nuevo en cada lectura es un error clásico: el primero se
     * lleva a su buffer más de una línea y las siguientes se pierden.
     */
    private BufferedReader entrada

    Launcher(List<Chapter> chapters) { this.chapters = chapters }

    private int getTotalDemos() {
        int total = 0
        for (Chapter c : chapters) total += c.demos.size()
        total
    }

    /** "1 capítulo" / "30 capítulos". Detalle tonto, pero se lee en cada arranque. */
    private String getChapterCount() {
        "${chapters.size()} capítulo" + (chapters.size() == 1 ? '' : 's')
    }

    /** Punto de entrada. `args` viene tal cual de `static void main(String[] args)`. */
    void start(String[] args) {
        if (!args) {
            interactiveMenu()
            return
        }
        String command = args[0].trim().toLowerCase().replaceFirst(/^--/, '')
        switch (command) {
            case ['help', '-h', 'ayuda'] -> printUsage()
            case ['list', '-l', 'indice', 'índice'] -> printIndex()
            case ['all', 'todo'] -> runEverything()
            case ['search', 'buscar'] -> search(args.drop(1).join(' '))
            default -> runTarget(command)
        }
    }

    // -- Ejecución -------------------------------------------------------------------

    /** Ejecuta un capítulo (`"13"`) o una demo concreta (`"13.4"`). */
    private void runTarget(String target) {
        if (target.contains('.')) {
            Integer chapterNumber = target.substring(0, target.indexOf('.')).isInteger()
                ? target.substring(0, target.indexOf('.')).toInteger()
                : null
            Demo demo = chapters.find { Chapter c -> c.number == chapterNumber }?.find(target)
            if (demo == null) {
                println "No existe la demo '$target'."
                println 'Prueba con `list` para ver el índice completo.'
                return
            }
            Registry.runDemo(demo)
        } else {
            Chapter chapter = target.isInteger()
                ? chapters.find { Chapter c -> c.number == target.toInteger() }
                : null
            if (chapter == null) {
                println "No existe el capítulo '$target'. Los capítulos van del 1 al ${chapters.size()}."
                println 'Prueba con `list` para ver el índice completo.'
                return
            }
            chapter.runAll()
        }
    }

    /**
     * Ejecuta absolutamente todo y cronometra cada capítulo.
     *
     * Es la comprobación de que el repositorio entero funciona: si alguna demo se
     * colgase o lanzase, se vería aquí.
     */
    private void runEverything() {
        heading("groovy-reference · ejecutando las $totalDemos demos")
        Map<Chapter, Long> times = new LinkedHashMap<Chapter, Long>()
        long inicio = System.nanoTime()
        for (Chapter chapter : chapters) {
            long t0 = System.nanoTime()
            chapter.runAll()
            times.put(chapter, System.nanoTime() - t0)
        }
        long total = System.nanoTime() - inicio

        heading('Resumen de tiempos')
        times.each { Chapter chapter, Long elapsed ->
            show("${chapter.id} · ${chapter.name}", millis(elapsed))
        }
        separator()
        show("TOTAL ($totalDemos demos en $chapterCount)", millis(total))
    }

    private static String millis(long nanos) { "${(nanos / 1_000_000L) as long} ms" }

    // -- Consultas -------------------------------------------------------------------

    /** Índice completo: todos los capítulos con todas sus demos. */
    void printIndex() {
        heading('groovy-reference · índice de demos')
        chapters.each { Chapter chapter ->
            println ''
            println "  ${chapter.id}. ${chapter.name} — ${chapter.summary}"
            chapter.demos.each { Demo demo ->
                println "        ${demo.id.padRight(7)} ${demo.title}"
            }
        }
        println ''
        println "  $totalDemos demos en ${chapterCount}."
        println '  Ejecuta una con:  ./mvnw -q exec:java -Dexec.args="13.4"'
    }

    /** Busca texto en los títulos de capítulos y demos. */
    private void search(String query) {
        String needle = query.trim()
        if (!needle) {
            println 'Uso: search <texto>   (por ejemplo: search closure)'
            return
        }

        heading("Resultados para \"$needle\"")
        String lower = needle.toLowerCase()
        int hits = 0
        for (Chapter chapter : chapters) {
            List<Demo> matches = chapter.demos.findAll { Demo d ->
                d.title.toLowerCase().contains(lower)
            }
            boolean chapterMatches = chapter.name.toLowerCase().contains(lower) ||
                chapter.summary.toLowerCase().contains(lower)

            if (matches || chapterMatches) {
                println ''
                println "  ${chapter.id}. ${chapter.name}"
                // Si lo que coincide es el capítulo, enseñamos todas sus demos.
                List<Demo> toShow = matches ?: chapter.demos
                toShow.each { Demo d -> println "        ${d.id.padRight(7)} ${d.title}" }
                hits += toShow.size()
            }
        }
        if (hits == 0) println '  Sin resultados.'
    }

    private void printUsage() {
        heading('groovy-reference · cómo ejecutar')
        println '''
  Desde la línea de comandos (el prefijo es siempre
  `./mvnw -q exec:java -Dexec.args="..."`):

      list          índice completo de capítulos y demos
      all           ejecuta TODAS las demos, con tiempos
      13            ejecuta el capítulo 13 entero
      13.4          ejecuta sólo la demo 13.4
      search closure   busca "closure" en los títulos
      help          esta ayuda

  Sin argumentos se abre un menú interactivo.

  Desde IntelliJ: pulsa ▶ en el `main()` de cualquier clase `CNN_00Index`
  para ejecutar ese capítulo, o en `Main` para abrir el menú.'''
    }

    // -- Menú interactivo ------------------------------------------------------------

    /**
     * Menú de dos niveles: capítulo y después demo.
     *
     * Detalle importante: [readCommand] devuelve `null` cuando la entrada estándar está
     * cerrada (CI, una tubería vacía). En ese caso salimos ordenadamente en lugar de
     * girar en un bucle infinito.
     */
    private void interactiveMenu() {
        heading("groovy-reference · $totalDemos demos en $chapterCount")
        println '  Escribe `help` para ver todos los comandos.'

        while (true) {
            printChapterList()
            String line = readCommand(
                "Capítulo (1-${chapters.size()}), demo (13.4), `all`, `search <texto>`, `q` para salir")
            if (line == null) return

            String lower = line.toLowerCase()
            if (!line) continue
            if (lower in QUIT_WORDS) {
                println ''
                println '¡Hasta luego! Sigue practicando.'
                return
            }
            if (lower in ['help', 'ayuda']) { printUsage(); continue }
            if (lower in ['list', 'indice', 'índice']) { printIndex(); continue }
            if (lower in ['all', 'todo']) { runEverything(); continue }
            if (lower.startsWith('search ') || lower.startsWith('buscar ')) {
                search(line.substring(line.indexOf(' ') + 1)); continue
            }
            if (line.contains('.')) { runTarget(line); continue }

            Chapter chapter = line.isInteger()
                ? chapters.find { Chapter c -> c.number == line.toInteger() }
                : null
            if (chapter == null) {
                println "  No reconozco '$line'. Escribe `help` si te pierdes."
            } else if (!chapterMenu(chapter)) {
                return // entrada estándar cerrada dentro del submenú
            }
        }
    }

    /**
     * Submenú de un capítulo.
     *
     * Devuelve `false` si la entrada estándar se cerró y hay que salir del programa.
     */
    private boolean chapterMenu(Chapter chapter) {
        while (true) {
            heading("Capítulo ${chapter.id} · ${chapter.name}")
            println "  ${chapter.summary}"
            println ''
            chapter.demos.eachWithIndex { Demo demo, int index ->
                println "      ${(index + 1).toString().padLeft(2)}. ${demo.title}   [${demo.id}]"
            }

            String line = readCommand(
                "Demo (1-${chapter.demos.size()}), `a` para todas, `0` para volver")
            if (line == null) return false

            String lower = line.toLowerCase()
            if (!line) continue
            if (lower in QUIT_WORDS) return false
            if (lower in BACK_WORDS) return true
            if (lower in ['a', 'all', 'todas']) { chapter.runAll(); continue }

            Demo demo = line.isInteger() ? chapter.demos[line.toInteger() - 1] : null
            if (demo == null) println "  No reconozco '$line'." else Registry.runDemo(demo)
        }
    }

    private void printChapterList() {
        println ''
        separator()
        chapters.each { Chapter chapter ->
            println "  ${chapter.id}. ${chapter.name.padRight(32)} (${chapter.demos.size()} demos)"
        }
        separator()
    }

    /**
     * Lee una orden del usuario.
     *
     * Devuelve el texto introducido, o `null` si no hay entrada interactiva disponible.
     */
    private String readCommand(String hint) {
        println ''
        println hint
        print '> '
        System.out.flush()

        if (entrada == null) {
            entrada = new BufferedReader(new InputStreamReader(System.in, 'UTF-8'))
        }
        String line = entrada.readLine()
        if (line == null) {
            // Pasa al ejecutar en CI o al hacer `echo "" | ./mvnw ...`.
            println ''
            println '  (no hay entrada interactiva disponible)'
            println '  Te dejo el índice; usa -Dexec.args="13.4" para ir directo a una demo.'
            printIndex()
            return null
        }
        line.trim()
    }
}
