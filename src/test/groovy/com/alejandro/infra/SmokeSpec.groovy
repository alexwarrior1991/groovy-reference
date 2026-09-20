package com.alejandro.infra

import com.alejandro.Main
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.TimeUnit

// =====================================================================================
//  El test de humo: ejecuta TODAS las demos del repositorio.
//
//  Es el test más valioso de la suite, y el más simple. Convierte cientos de ficheros
//  de ejemplos en código VERIFICADO: si una demo lanza una excepción, si un `assert`
//  deja de cumplirse o si alguien rompe un ejemplo al editarlo, `./mvnw test` se pone
//  rojo con el identificador exacto de la demo culpable.
//
//  Detalle importante: llama a `demo.action.call()` y NO a `Registry.runDemo(demo)`.
//  `runDemo` captura las excepciones a propósito, para que una demo rota no corte el
//  recorrido del lanzador; aquí eso escondería justo lo que queremos detectar.
//
//  Va en UNA sola pasada porque ejecutar el repositorio entero cuesta unos segundos:
//  las dos comprobaciones (que no lanza y que imprime algo) se hacen a la vez.
// =====================================================================================

class SmokeSpec extends Specification {

    def 'todas las demos se ejecutan sin lanzar y escriben algo'() {
        given:
        List<String> fallos = []
        List<String> mudas = []
        int ejecutadas = 0

        when:
        long t0 = System.nanoTime()
        Main.CHAPTERS.each { Chapter capitulo ->
            capitulo.demos.findAll { Demo d -> !d.skipInSmokeTest }.each { Demo demo ->
                ejecutadas++
                def (Throwable fallo, String salida) = CapturaDeSalida.capturando { demo.action.call() }

                if (fallo != null) {
                    fallos << "  ${demo.id} · ${demo.title} → ${fallo.class.simpleName}: ${fallo.message}"
                }
                // Una demo que no imprime nada no enseña nada: casi siempre significa
                // que alguien dejó el cuerpo a medias.
                if (!salida.trim()) mudas << "  ${demo.id} · ${demo.title}"
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000L

        and:
        int n = Main.CHAPTERS.size()
        println "Test de humo: ${ejecutadas} demos en ${n} capítulo${n == 1 ? '' : 's'}, ${ms} ms"

        then:
        // El "power assert" de Spock imprime el contenido de la lista cuando falla, así
        // que no hace falta construir el mensaje a mano: sale la demo culpable y su
        // excepción. Capítulo 28.
        fallos == []
        mudas == []
    }

    /**
     * No es una prueba de rendimiento: es un guardián contra una demo que se cuelgue
     * esperando entrada, una red que no existe o un bucle infinito sin acotar.
     */
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    def 'el recorrido completo termina en un tiempo razonable'() {
        when:
        CapturaDeSalida.silenciando {
            Main.CHAPTERS.each { Chapter c ->
                c.demos.findAll { Demo d -> !d.skipInSmokeTest }.each { Demo d ->
                    try { d.action.call() } catch (Throwable ignored) { }
                }
            }
        }

        then:
        noExceptionThrown()
    }
}
