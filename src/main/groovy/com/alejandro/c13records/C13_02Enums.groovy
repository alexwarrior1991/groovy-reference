package com.alejandro.c13records

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  13.2 · Enums con comportamiento
//
//  QUÉ ES
//    Un enum de Groovy es el de Java, con la sintaxis relajada: puede tener campos,
//    constructor, métodos, e incluso una implementación distinta por constante.
//
//  POR QUÉ IMPORTA
//    Un enum con comportamiento sustituye a una cadena de `if` sobre una cadena de
//    texto, y el compilador comprueba que no te inventas un valor.
//
//  ERRORES COMUNES
//    · Persistir `ordinal()`: cambia en cuanto alguien reordena las constantes.
//    · `valueOf` con un texto que viene de fuera, sin capturar la excepción.
//    · Un `switch` sobre el enum en diez sitios, en vez de un método en el propio enum.
// =====================================================================================

/** Lo mínimo. */
enum Dia { LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO }

/** Con campos y constructor. */
enum Prioridad {
    BAJA(1, 'puede esperar'),
    MEDIA(5, 'esta semana'),
    ALTA(10, 'hoy')

    final int peso
    final String descripcion

    Prioridad(int peso, String descripcion) {
        this.peso = peso
        this.descripcion = descripcion
    }

    boolean esUrgente() { peso >= 10 }
}

/** Con una implementación por constante: el "enum estrategia". */
enum Operacion {
    SUMA('+') {
        @Override
        BigDecimal aplicar(BigDecimal a, BigDecimal b) { a + b }
    },
    RESTA('-') {
        @Override
        BigDecimal aplicar(BigDecimal a, BigDecimal b) { a - b }
    },
    MULTIPLICACION('*') {
        @Override
        BigDecimal aplicar(BigDecimal a, BigDecimal b) { a * b }
    },
    DIVISION('/') {
        @Override
        BigDecimal aplicar(BigDecimal a, BigDecimal b) {
            b == 0 ? null : a / b
        }
    }

    final String simbolo

    Operacion(String simbolo) { this.simbolo = simbolo }

    abstract BigDecimal aplicar(BigDecimal a, BigDecimal b)

    static Operacion porSimbolo(String simbolo) {
        values().find { it.simbolo == simbolo }
    }
}

/** Una máquina de estados dentro del propio enum. */
enum EstadoPedido {
    NUEVO, PAGADO, ENVIADO, ENTREGADO, CANCELADO

    private static final Map<EstadoPedido, List<EstadoPedido>> TRANSICIONES = [
        (NUEVO)    : [PAGADO, CANCELADO],
        (PAGADO)   : [ENVIADO, CANCELADO],
        (ENVIADO)  : [ENTREGADO],
        (ENTREGADO): [],
        (CANCELADO): [],
    ]

    boolean puedePasarA(EstadoPedido destino) { destino in TRANSICIONES[this] }

    List<EstadoPedido> siguientesPosibles() { TRANSICIONES[this] }

    boolean esFinal() { TRANSICIONES[this].isEmpty() }
}

class C13_02Enums {

    /**
     * Lo básico.
     */
    static void demoBasics() {
        section('Lo que trae gratis')

        show('values()', Dia.values()*.name())
        show('valueOf("LUNES")', Dia.valueOf('LUNES'))
        show('name()', Dia.LUNES.name())
        show('ordinal()', Dia.MIERCOLES.ordinal())

        section('Coerción desde una cadena: Groovy la hace con `as`')

        show("'MARTES' as Dia", 'MARTES' as Dia)
        bullet('Es `valueOf` por debajo, así que lanza igual si no existe.')

        section('valueOf con algo que viene de fuera')

        try {
            Dia.valueOf('lunes')
        } catch (IllegalArgumentException e) {
            show("valueOf('lunes')  (minúsculas)", e.class.simpleName)
        }
        bullet('Distingue mayúsculas. Con datos de fuera, envuélvelo:')
        show('conversión tolerante', desdeTexto('lunes'))
        show('con algo que no existe', desdeTexto('nosecuantos'))

        section('ordinal(): por qué NO debe persistirse')

        show('MIERCOLES.ordinal() hoy', Dia.MIERCOLES.ordinal())
        bullet('Si mañana alguien mete DOMINGO al principio, ese 2 pasa a ser otro día.')
        bullet('Y los datos guardados de ayer empiezan a significar otra cosa.')
        bullet('Persiste SIEMPRE `name()`, que es estable y además se lee.')

        section('En un switch y en una colección')

        show('switch', tipoDeDia(Dia.SABADO))
        show('findAll', Dia.values().findAll { it in [Dia.SABADO, Dia.DOMINGO] }*.name())
        show('como clave de mapa', [(Dia.LUNES): 'reunión'][Dia.LUNES])
        bullet('Un EnumMap sería aún más eficiente para claves de enum.')
    }

    private static Dia desdeTexto(String texto) {
        Dia.values().find { it.name().equalsIgnoreCase(texto?.trim()) }
    }

    private static String tipoDeDia(Dia d) {
        switch (d) {
            case [Dia.SABADO, Dia.DOMINGO] -> 'fin de semana'
            default -> 'laborable'
        }
    }

    /**
     * Enums con campos y comportamiento.
     */
    static void demoWithBehaviour() {
        section('Con campos y constructor')

        Prioridad.values().each { p ->
            show(p.name(), "peso ${p.peso}, ${p.descripcion}, ¿urgente? ${p.esUrgente()}")
        }

        section('Ordenar y filtrar por el campo')

        show('de mayor a menor peso', Prioridad.values().toSorted { -it.peso }*.name())
        show('las urgentes', Prioridad.values().findAll { it.esUrgente() }*.name())

        section('Una implementación por constante: el enum estrategia')

        def a = 10.0
        def b = 4.0
        Operacion.values().each { op ->
            show("$a ${op.simbolo} $b", op.aplicar(a, b))
        }

        section('Y una búsqueda por el símbolo')

        show("porSimbolo('*')", Operacion.porSimbolo('*'))
        show('aplicándola', Operacion.porSimbolo('*').aplicar(3.0, 7.0))
        show('un símbolo que no existe', Operacion.porSimbolo('^'))

        section('Esto sustituye a un switch repartido por el código')

        bullet('Sin el enum estrategia harías `switch (op) { case "+": … }` en cada')
        bullet('sitio donde se opera. Al añadir una operación habría que buscarlos')
        bullet('todos. Aquí el compilador te obliga: `aplicar` es abstracto.')

        section('La división entre cero, tratada')

        show('10 / 0', Operacion.DIVISION.aplicar(10.0, 0.0))
        bullet('Devuelve null en vez de lanzar. Es una decisión de diseño, no un')
        bullet('descuido: el capítulo 18 discute cuándo cada cosa.')
    }

    /**
     * Una máquina de estados.
     */
    static void demoStateMachine() {
        section('Las transiciones, dentro del propio enum')

        EstadoPedido.values().each { estado ->
            show(estado.name(), "puede ir a ${estado.siguientesPosibles()*.name()}")
        }

        section('Comprobar una transición')

        show('NUEVO → PAGADO', EstadoPedido.NUEVO.puedePasarA(EstadoPedido.PAGADO))
        show('NUEVO → ENTREGADO', EstadoPedido.NUEVO.puedePasarA(EstadoPedido.ENTREGADO))
        show('ENTREGADO es final', EstadoPedido.ENTREGADO.esFinal())

        section('Un recorrido completo')

        show('camino válido', recorrer([EstadoPedido.PAGADO, EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO]))
        show('camino inválido', recorrer([EstadoPedido.ENVIADO]))

        section('Por qué esto va en el enum y no fuera')

        bullet('Las reglas viven al lado de los valores a los que se aplican.')
        bullet('Añadir un estado obliga a decidir sus transiciones en el mismo sitio.')
        bullet('Y nadie puede construir un estado que no esté en la lista.')

        section('Enum frente a sealed + record')

        bullet('enum              un conjunto FIJO de valores SIN datos propios')
        bullet('sealed + record   un conjunto fijo de formas, cada una CON sus datos')
        bullet('Si cada alternativa necesita campos distintos, es sealed + record.')
        bullet('Si son sólo etiquetas con comportamiento, es un enum.')
    }

    private static String recorrer(List<EstadoPedido> pasos) {
        EstadoPedido actual = EstadoPedido.NUEVO
        for (EstadoPedido siguiente : pasos) {
            if (!actual.puedePasarA(siguiente)) {
                return "rechazado: de $actual no se puede ir a $siguiente"
            }
            actual = siguiente
        }
        "terminó en $actual"
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade una constante a `Operacion` sin implementar `aplicar` y lee el error.
//  2. Reordena las constantes de `Dia` y mira cómo cambian todos los `ordinal()`.
//  3. Añade el estado DEVUELTO a `EstadoPedido` y decide sus transiciones.
//  4. Convierte `EstadoPedido` a `sealed interface` + records y compara los dos.
