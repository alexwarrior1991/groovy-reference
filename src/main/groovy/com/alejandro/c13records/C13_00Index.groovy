package com.alejandro.c13records

import com.alejandro.infra.Chapter
import groovy.transform.CompileStatic

import static com.alejandro.infra.Registry.chapter

/**
 * # Capítulo 13 · Records, sealed y enums
 *
 * Tres formas de decir "esto es un dato con una forma concreta". Juntas cubren casi
 * todo el modelado: un `record` para los datos, un `sealed interface` para las
 * alternativas, y un `enum` para los conjuntos cerrados de etiquetas.
 *
 * ## El patrón que hay que conocer
 * ```
 * sealed interface Resultado permits Exito, Fallo {}
 * record Exito(String mensaje) implements Resultado {}
 * record Fallo(String mensaje, int codigo) implements Resultado {}
 * ```
 * El error como DATO, no como excepción. El capítulo 18 compara los dos enfoques.
 *
 * ## Qué se cubre
 * - Qué te da un `record` y qué le añade Groovy (el constructor de mapa).
 * - El constructor compacto para validar, y `copyWith` para las variantes.
 * - El límite: el campo es final, la lista de dentro no. Record frente a `@Immutable`.
 * - `sealed` + `permits`, y el aviso importante sobre la exhaustividad.
 * - Enums con campos, con una implementación por constante y con las transiciones de
 *   una máquina de estados dentro.
 *
 * ## Lo que hay que llevarse sí o sí
 * - **En Groovy el `switch` sobre un tipo sellado NO comprueba exhaustividad**, al
 *   contrario que en Java 21: si no encaja ningún caso devuelve `null` en silencio.
 *   Pon siempre un `default` que avise.
 * - **Un record con un campo `List` no es inmutable.** El campo no se reasigna, pero
 *   la lista se muta. Congélala o usa `@Immutable`.
 * - **`ordinal()` nunca se persiste.** Cambia en cuanto alguien reordena las
 *   constantes, y los datos de ayer pasan a significar otra cosa. Persiste `name()`.
 * - **El enum estrategia** (una implementación por constante con un método abstracto)
 *   sustituye a un `switch` repartido por diez sitios, y el compilador te obliga a
 *   implementarlo al añadir una constante.
 * - Enum para etiquetas con comportamiento; `sealed` + `record` cuando cada
 *   alternativa tiene datos distintos.
 *
 * ## Errores típicos
 * - `valueOf` con texto de fuera sin capturar la excepción ni normalizar.
 * - Lógica de negocio dentro de un record.
 * - Un `switch` sobre un enum repetido por todo el código.
 *
 * Siguiente paso: capítulo 14, sobrecarga de operadores.
 */
@CompileStatic
class C13_00Index {

    static final Chapter CHAPTER = chapter(
        13,
        'Records, sealed y enums',
        'records con constructor compacto, jerarquías selladas y enums con comportamiento',
    ) {
        demo('Lo que un record te da') { C13_01Records.demoWhatYouGet() }
        demo('El límite: record frente a @Immutable') { C13_01Records.demoLimits() }
        demo('Clases selladas, y la exhaustividad que no hay') { C13_01Records.demoSealed() }
        demo('Enums: lo básico y por qué no persistir ordinal') { C13_02Enums.demoBasics() }
        demo('Enums con campos y con estrategia') { C13_02Enums.demoWithBehaviour() }
        demo('Una máquina de estados en un enum') { C13_02Enums.demoStateMachine() }
    }

    /** Ejecuta el capítulo 13 completo. */
    static void main(String[] args) { CHAPTER.runAll() }
}
