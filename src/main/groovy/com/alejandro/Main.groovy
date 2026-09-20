package com.alejandro

import com.alejandro.c01basics.C01_00Index
import com.alejandro.c02types.C02_00Index
import com.alejandro.c03strings.C03_00Index
import com.alejandro.c04operators.C04_00Index
import com.alejandro.c05truth.C05_00Index
import com.alejandro.c06closures.C06_00Index
import com.alejandro.c07closuresadvanced.C07_00Index
import com.alejandro.c08lists.C08_00Index
import com.alejandro.c09maps.C09_00Index
import com.alejandro.c10classes.C10_00Index
import com.alejandro.c11traits.C11_00Index
import com.alejandro.c12ast.C12_00Index
import com.alejandro.c13records.C13_00Index
import com.alejandro.c14operators.C14_00Index
import com.alejandro.c15static.C15_00Index
import com.alejandro.c16mop.C16_00Index
import com.alejandro.c17categories.C17_00Index
import com.alejandro.c18exceptions.C18_00Index
import com.alejandro.c19regex.C19_00Index
import com.alejandro.c20gdk.C20_00Index
import com.alejandro.c21files.C21_00Index
import com.alejandro.c22json.C22_00Index
import com.alejandro.c23xml.C23_00Index
import com.alejandro.c24dsl.C24_00Index
import com.alejandro.c25scripts.C25_00Index
import com.alejandro.infra.Chapter
import com.alejandro.infra.Launcher
import groovy.transform.CompileStatic

// =====================================================================================
//  groovy-reference · punto de entrada
//
//  Este fichero es el índice del repositorio: agrega los capítulos y delega en el
//  lanzador. Si vienes a aprender Groovy, NO empieces aquí: empieza por
//  `c01basics/C01_01HelloWorld.groovy` y ve subiendo de número.
//
//  Cómo ejecutar
//  -------------
//      ./mvnw -q exec:java                          menú interactivo
//      ./mvnw -q exec:java -Dexec.args="list"       índice completo
//      ./mvnw -q exec:java -Dexec.args="13"         el capítulo 13 entero
//      ./mvnw -q exec:java -Dexec.args="13.4"       sólo la demo 13.4
//      ./mvnw -q exec:java -Dexec.args="all"        absolutamente todo
//
//  Desde IntelliJ, pulsa ▶ en el `main()` de abajo, o en el de cualquier
//  `CNN_00Index` para ejecutar ese capítulo suelto.
// =====================================================================================

@CompileStatic
class Main {

    /**
     * Los capítulos del recorrido, en orden pedagógico.
     *
     * Es una lista escrita a mano a propósito: el compilador la verifica, no hace falta
     * reflexión ni escanear el classpath, y añadir un capítulo es añadir una línea.
     */
    static final List<Chapter> CHAPTERS = [
        C01_00Index.CHAPTER,
        C02_00Index.CHAPTER,
        C03_00Index.CHAPTER,
        C04_00Index.CHAPTER,
        C05_00Index.CHAPTER,
        C06_00Index.CHAPTER,
        C07_00Index.CHAPTER,
        C08_00Index.CHAPTER,
        C09_00Index.CHAPTER,
        C10_00Index.CHAPTER,
        C11_00Index.CHAPTER,
        C12_00Index.CHAPTER,
        C13_00Index.CHAPTER,
        C14_00Index.CHAPTER,
        C15_00Index.CHAPTER,
        C16_00Index.CHAPTER,
        C17_00Index.CHAPTER,
        C18_00Index.CHAPTER,
        C19_00Index.CHAPTER,
        C20_00Index.CHAPTER,
        C21_00Index.CHAPTER,
        C22_00Index.CHAPTER,
        C23_00Index.CHAPTER,
        C24_00Index.CHAPTER,
        C25_00Index.CHAPTER,
    ].asImmutable()

    static void main(String[] args) {
        new Launcher(CHAPTERS).start(args)
    }
}
