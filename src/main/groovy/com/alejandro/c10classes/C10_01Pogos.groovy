package com.alejandro.c10classes

import groovy.transform.PackageScope

import static com.alejandro.infra.Console.bullet
import static com.alejandro.infra.Console.section
import static com.alejandro.infra.Console.show

// =====================================================================================
//  10.1 · POGO: propiedades, no campos
//
//  QUÉ ES
//    Un POGO (Plain Old Groovy Object) es una clase normal. La diferencia con Java está
//    en una regla: un campo declarado SIN modificador de acceso no es un campo, es una
//    PROPIEDAD, y Groovy le genera un getter y un setter públicos.
//
//  POR QUÉ IMPORTA
//    Es lo que convierte veinte líneas de Java en una. Y explica por qué `objeto.nombre`
//    no está saltándose el encapsulamiento: está llamando a `getNombre()`.
//
//  ERRORES COMUNES
//    · Poner `private` y sorprenderse de que desaparezcan los accesores.
//    · Creer que `objeto.nombre` accede al campo. Llama al getter, y si tú lo has
//      escrito, al tuyo.
//    · Usar `.@campo` sin saber que salta el getter a propósito.
// =====================================================================================

class C10_01Pogos {

    /**
     * Qué genera Groovy.
     */
    static void demoWhatIsGenerated() {
        section('Una clase de tres líneas')

        bullet('class Usuario {')
        bullet('    String nombre')
        bullet('    int edad')
        bullet('}')

        section('Lo que Groovy genera por debajo')

        def usuario = new Usuario(nombre: 'Ana', edad: 30)
        def metodos = usuario.class.methods*.name.findAll { it.startsWith('get') || it.startsWith('set') }
        show('accesores generados', metodos.sort().unique())
        bullet('Un getter y un setter públicos por cada propiedad.')
        bullet('Y un constructor vacío y otro que acepta un mapa.')

        section('El campo de verdad es privado')

        def campos = usuario.class.declaredFields.findAll { !it.synthetic }
        show('campos declarados', campos.collect { "${java.lang.reflect.Modifier.toString(it.modifiers)} ${it.name}" })
        bullet('El campo es privado; lo público son los accesores. Como en Java,')
        bullet('pero sin escribirlo.')

        section('`objeto.nombre` es `objeto.getNombre()`')

        show('usuario.nombre', usuario.nombre)
        show('usuario.getNombre()', usuario.getNombre())
        usuario.nombre = 'Luis'
        show('tras usuario.nombre = "Luis"', usuario.getNombre())
        bullet('La asignación llama al SETTER. No toca el campo directamente.')

        section('`properties` lo enseña todo')

        show('usuario.properties.keySet()', usuario.properties.keySet().sort())
        bullet('Sale también `class`, que viene de `getClass()`.')
    }

    static class Usuario {
        String nombre
        int edad
    }

    /**
     * El modificador de acceso lo cambia todo.
     */
    static void demoAccessModifiers() {
        section('Sin modificador → PROPIEDAD (con accesores)')
        section('Con `private` → CAMPO (sin accesores)')

        def obj = new ConVisibilidades()
        def accesores = obj.class.methods*.name.findAll { it.startsWith('get') }.sort()
        show('getters generados', accesores - ['getClass'])
        bullet('`propiedad` tiene getter. `campoPrivado` no.')

        section('Pero en Groovy el `private` no es una frontera dura')

        show('obj.campoPrivado', obj.campoPrivado)
        bullet('Groovy lo deja leer de todas formas. El `private` documenta la')
        bullet('intención, pero no la impone como en Java.')
        bullet('Con @CompileStatic (capítulo 15) sí se comprueba.')

        section('@PackageScope: propiedad sólo para el paquete')

        show('obj.deMiPaquete', obj.deMiPaquete)
        def tieneGetter = obj.class.methods*.name.contains('getDeMiPaquete')
        show('¿tiene getter público?', tieneGetter)

        section('La tabla')

        bullet('declaración            qué es         accesores')
        bullet('────────────────────   ────────────   ─────────────')
        bullet('String x               propiedad      getter y setter públicos')
        bullet('private String x       campo          ninguno')
        bullet('public String x        campo público  ninguno')
        bullet('protected String x     campo          ninguno')
        bullet('@PackageScope String x propiedad      de paquete')
        bullet('final String x         propiedad      sólo getter')

        section('`final` quita el setter')

        def inmutable = new ConFinal('fijo')
        show('inmutable.valor', inmutable.valor)
        show('¿tiene setValor?', inmutable.class.methods*.name.contains('setValor'))
    }

    static class ConVisibilidades {
        String propiedad = 'soy propiedad'
        private String campoPrivado = 'soy campo privado'
        @PackageScope String deMiPaquete = 'soy de paquete'
    }

    static class ConFinal {
        final String valor

        ConFinal(String valor) { this.valor = valor }
    }

    /**
     * Escribir tus propios accesores.
     */
    static void demoCustomAccessors() {
        section('Si escribes el getter, gana el tuyo')

        def p = new ConAccesorPropio(nombre: 'ana')
        show('p.nombre', p.nombre)
        show('p.getNombre()', p.getNombre())
        bullet('El acceso con punto llama al TUYO, no al generado.')

        section('Para llegar al campo hay que usar `.@`')

        show('p.@nombre', p.@nombre)
        bullet('`.@` salta el getter y va al campo. Úsalo poco y a conciencia.')

        section('Un setter que valida')

        def cuenta = new Cuenta()
        cuenta.saldo = 100
        show('tras saldo = 100', cuenta.saldo)
        try {
            cuenta.saldo = -5
        } catch (IllegalArgumentException e) {
            show('tras saldo = -5', e.message)
        }
        show('el saldo sigue siendo', cuenta.saldo)

        section('Propiedades CALCULADAS: un getter sin campo')

        def rect = new Rectangulo(ancho: 3, alto: 4)
        show('rect.area', rect.area)
        show('rect.perimetro', rect.perimetro)
        show('¿hay campo "area"?', rect.class.declaredFields*.name.contains('area'))
        bullet('Un getter sin campo detrás YA es una propiedad de sólo lectura.')
        bullet('Es la forma idiomática de exponer un valor derivado.')

        section('Y aparecen en `properties`')

        show('rect.properties.keySet()', rect.properties.keySet().sort())
    }

    static class ConAccesorPropio {
        String nombre

        String getNombre() { nombre?.toUpperCase() }
    }

    static class Cuenta {
        private int saldo = 0

        int getSaldo() { saldo }

        void setSaldo(int nuevo) {
            if (nuevo < 0) throw new IllegalArgumentException('el saldo no puede ser negativo')
            saldo = nuevo
        }
    }

    static class Rectangulo {
        int ancho
        int alto

        int getArea() { ancho * alto }

        int getPerimetro() { 2 * (ancho + alto) }
    }

    /**
     * POGO o mapa: cuándo cada uno.
     */
    static void demoPogoVsMap() {
        section('Un mapa se parece mucho a un objeto')

        def comoMapa = [nombre: 'Ana', edad: 30]
        def comoObjeto = new Usuario(nombre: 'Ana', edad: 30)

        show('mapa.nombre', comoMapa.nombre)
        show('objeto.nombre', comoObjeto.nombre)
        bullet('La sintaxis de acceso es idéntica. Por eso se confunden.')

        section('Pero el mapa no comprueba nada')

        show('mapa.campoQueNoExiste', comoMapa.campoQueNoExiste)
        try {
            show('objeto.campoQueNoExiste', comoObjeto.campoQueNoExiste)
        } catch (Exception e) {
            show('objeto.campoQueNoExiste', e.class.simpleName)
        }
        bullet('Una errata en un mapa devuelve null y sigue. En un objeto, lanza.')

        section('Y el mapa no tiene comportamiento')

        bullet('Un POGO puede validar en el setter, calcular propiedades, tener')
        bullet('métodos con nombre del dominio e implementar interfaces.')

        section('La regla')

        bullet('Datos que ENTRAN o SALEN del programa (JSON, configuración, una')
        bullet('consulta): mapa. Es lo que son.')
        bullet('Datos que VIVEN en el programa y tienen reglas: POGO.')
        bullet('La frontera entre los dos es donde se valida y se convierte.')

        section('Convertir de uno a otro')

        show('mapa → objeto', new Usuario(comoMapa).nombre)
        show('objeto → mapa', comoObjeto.properties.subMap(['nombre', 'edad']))
        bullet('El constructor de mapa y `properties` hacen el viaje de ida y vuelta.')
    }
}

// ── PARA EXPERIMENTAR ────────────────────────────────────────────────────────────────
//  1. Añade `private` a `nombre` en `Usuario` y mira qué deja de compilar.
//  2. Escribe un `setNombre` que ponga la primera letra en mayúscula y comprueba que
//     `usuario.nombre = 'ana'` pasa por él.
//  3. Quita el `getArea()` de `Rectangulo` y comprueba que `area` desaparece de
//     `properties`.
//  4. Haz `new Usuario(nombre: 'Ana', inventado: 1)` y lee el error.
