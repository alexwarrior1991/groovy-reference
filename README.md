# groovy-reference

Una referencia **práctica** de Groovy: 204 demos ejecutables, comentadas en español,
ordenadas de `println` a los DSLs.

No es un tutorial que se lee una vez ni una chuleta de sintaxis. Es un proyecto que se
**ejecuta, se lee y se rompe a propósito** para entender por qué el resultado cambia.

- **30 capítulos**, del 01 (sintaxis básica) al 30 (ejercicios).
- **204 demos** que se pueden lanzar una a una desde la línea de comandos o el IDE.
- **190 tests** que verifican que todo funciona, incluido un test de humo que ejecuta
  **todas** las demos en cada `./mvnw test`.
- Comentarios que explican **por qué**, no sólo qué: errores comunes, trampas,
  alternativas y cuándo NO usar cada cosa.

---

## Arranque en 60 segundos

```bash
git clone <la-url-de-este-repositorio>
cd groovy-reference

./mvnw -q exec:java -Dexec.args="list"    # el índice completo
./mvnw -q exec:java -Dexec.args="1"       # el capítulo 1 entero
./mvnw -q exec:java                       # menú interactivo
```

La primera vez, Maven descarga el wrapper y las dependencias: tarda un par de minutos.
Las siguientes, segundos.

En Windows, sustituye `./mvnw` por `mvnw.cmd`.

### Desde IntelliJ IDEA

1. `File → Open` y selecciona la carpeta del proyecto (Maven lo importa solo).
2. Pulsa ▶ en el `main()` de `src/main/groovy/com/alejandro/Main.groovy` → menú interactivo.
3. O pulsa ▶ en el `main()` de cualquier `CNN_00Index` → ese capítulo entero.

---

## Cómo ejecutar

El prefijo es siempre `./mvnw -q exec:java`:

| Qué quieres | Comando |
|---|---|
| Menú interactivo | `./mvnw -q exec:java` |
| Índice de capítulos y demos | `… -Dexec.args="list"` |
| **Todas** las demos, con tiempos | `… -Dexec.args="all"` |
| Un capítulo entero | `… -Dexec.args="13"` |
| **Una sola demo** | `… -Dexec.args="13.4"` |
| Buscar por título | `… -Dexec.args="search closure"` |
| Ayuda | `… -Dexec.args="help"` |

Ejecutar una demo suelta (`-Dexec.args="7.3"`) es lo que más vas a usar: vas al ejemplo,
lo lees, lo tocas y lo vuelves a lanzar.

**Sin Maven de por medio** (arranque instantáneo, útil si vas a iterar mucho):

```bash
./mvnw -q package -DskipTests
./target/dist/bin/groovy-reference 7.3
```

---

## El mapa de los 30 capítulos

🟢 básico · 🟡 hay matices · 🔴 conviene volver más de una vez

| # | Capítulo | Demos | | Qué te llevas |
|---|---|---|---|---|
| 01 | Sintaxis básica | 8 | 🟢 | script frente a clase, paréntesis y `return` opcionales, el `Binding` |
| 02 | Variables y tipos | 16 | 🟢 | `def`, **decimales exactos**, coerción, el desbordamiento que no ocurre |
| 03 | **Cadenas y GString** | 12 | 🟡 | los seis literales, interpolación **perezosa**, la clave de mapa |
| 04 | Operadores | 14 | 🟡 | `==` es `equals`, `is()`, `<=>`, `?:`, los tres spread, `in`, `<<` |
| 05 | Groovy Truth y control | 10 | 🟡 | qué es "cierto", el `switch` que clasifica, por qué `each` no corta |
| 06 | **Closures** | 9 | 🟡 | `it`, la última posición, el cierre y la trampa del bucle |
| 07 | **Closures avanzados** | 8 | 🔴 | `delegate`, `resolveStrategy`, `curry`, `memoize`, `trampoline` |
| 08 | Listas | 12 | 🟡 | la trampa de `remove`, qué muta, `collect`/`findAll`/`inject` |
| 09 | Mapas y rangos | 12 | 🟡 | `[variable: 1]`, `mapa.class`, los `get` que insertan, rangos |
| 10 | Clases y POGOs | 8 | 🟢 | propiedades frente a campos, **multimétodos**, inicialización |
| 11 | Herencia y traits | 7 | 🟡 | **traits con estado**, conflictos, el contador roto |
| 12 | **Transformaciones AST** | 10 | 🟡 | `@Canonical`, `@Immutable`, `@Delegate`, `@Builder` y el catálogo |
| 13 | Records, sealed y enums | 6 | 🟢 | constructor compacto, la exhaustividad que no hay, enum estrategia |
| 14 | Sobrecarga de operadores | 6 | 🟡 | una clase `Dinero` completa, `getAt`, `call`, `iterator` |
| 15 | Tipado estático | 4 | 🔴 | `@TypeChecked` frente a `@CompileStatic`, medido |
| 16 | **Metaprogramación (MOP)** | 4 | 🔴 | `metaClass`, `methodMissing`, `GroovyInterceptable` |
| 17 | Categorías y extensiones | 3 | 🔴 | `use { }`, los tres límites, los módulos de extensión |
| 18 | Excepciones | 4 | 🟢 | ninguna es checked, `withCloseable`, el error como dato |
| 19 | Expresiones regulares | 4 | 🟡 | `=~` frente a `==~`, grupos con nombre, `replaceAll` con closure |
| 20 | El GDK | 5 | 🟢 | `with` frente a `tap`, fechas, y cómo encontrar el método |
| 21 | Ficheros e IO | 4 | 🟡 | `.text`, `eachLine`, `traverse`, un CSV completo (todo en temporales) |
| 22 | JSON | 4 | 🟢 | `JsonSlurper`, `JsonGenerator` y qué NO publicar |
| 23 | XML | 4 | 🟡 | `XmlSlurper` frente a `XmlParser`, `MarkupBuilder`, XXE |
| 24 | **Builders y DSLs** | 5 | 🔴 | el mecanismo, las cinco reglas y **la fuga de ámbito** |
| 25 | Scripts y Binding | 4 | 🟡 | `GroovyShell`, `@Field`, un motor de reglas y la seguridad |
| 26 | Interoperabilidad Java | 4 | 🟡 | quién resuelve el despacho, stubs, **con `.java` de verdad** |
| 27 | Concurrencia | 4 | 🔴 | hilos virtuales, estado compartido, `CompletableFuture` |
| 28 | **Testing con Spock** | 5 | 🟡 | `where:`, power assert, `Mock`/`Stub`/`Spy`, **con Spec reales** |
| 29 | Trampas y rendimiento | 5 | 🔴 | las diecisiete trampas juntas y dónde se va el tiempo |
| 30 | **Ejercicios** | 3 | 🟢🟡🔴 | enunciado, pistas, solución explicada y variante difícil |

Los capítulos 03 y 12 son a los que más se vuelve. Los 07, 16 y 24 son los que más
cuesta que "hagan clic": son el mismo mecanismo visto tres veces.

---

## Cuatro rutas de estudio

Elige la tuya; ninguna necesita las otras.

### 🚀 Exprés — «necesito escribir Groovy esta semana»
`01 → 02 → 03 → 04 → 05 → 06 → 08 → 09 → 10`

Nueve capítulos, unas tres horas. Con esto se escribe Groovy correcto y legible:
sintaxis, tipos, texto, operadores, closures, colecciones y clases.

### 📚 Completa — «quiero aprenderlo bien»
`01 → 30`, en orden.

Cada capítulo da por sabido el anterior. Calcula dos o tres capítulos por sesión.

### ☕ Vengo de Java
`02 → 03 → 04 → 05 → 06 → 10 → 11 → 12 → 15 → 26`

Empieza por lo que funciona DISTINTO: los decimales exactos, el `==` que es `equals`,
el Groovy Truth, los closures, las propiedades, los traits y las transformaciones AST.
El 26 cierra el círculo: cómo conviven los dos lenguajes en el mismo módulo, con
clases `.java` de verdad compiladas en este mismo build.

### 🛠️ Gradle, Jenkins y DSLs
`06 → 07 → 12 → 16 → 17 → 24 → 25`

Si has llegado a Groovy por un `build.gradle` o un `Jenkinsfile`, esto es lo que hay
debajo: closures, `delegate`, anotaciones que generan código, metaprogramación y el
capítulo 24, donde se construye un DSL desde cero. El 25 explica cómo se ejecuta el
código que escribe el usuario, y por qué eso da miedo.

---

## Cómo experimentar (esto es lo importante)

Leer estos ficheros enseña la mitad; **romperlos** enseña la otra mitad.

**La receta de tres pasos:**

1. Ejecuta una demo: `./mvnw -q exec:java -Dexec.args="7.3"`.
2. Abre su fichero, cambia una línea, guarda.
3. Vuelve a ejecutar el mismo comando y compara la salida.

**Cosas que merece la pena romper a propósito:**

- Cambia `DELEGATE_FIRST` por `OWNER_FIRST` en un DSL y mira qué deja de funcionar.
- Pon `@CompileStatic` en el capítulo 16 y cuenta cuántas líneas dejan de compilar.
- Quita un `asImmutable()` y repite el ataque por el getter del capítulo 08.
- Usa un GString como clave con `put` en vez de con `[ ]` y busca la entrada perdida.
- Quita el `@Override` de un método y cámbiale una letra al nombre.

**Cada fichero de contenido termina con un bloque `// ── PARA EXPERIMENTAR ──`** con
dos a seis ideas concretas.

---

## Los temas que exigen más práctica (y por qué)

| Tema | Por qué cuesta | Dónde está |
|---|---|---|
| **El Groovy Truth** | No es sintaxis: cambia lo que significa `if (x)`. Lo difícil es acordarse de que `0`, `""` y `[]` son falsos justo cuando eso importa. | 05, 29 |
| **GString** | Es perezoso y **no** es un `String`. El mismo texto puede cambiar solo, y como clave de mapa no se encuentra. | 03, 29 |
| **`delegate` y `resolveStrategy`** | Es el mecanismo de todos los DSL, y la estrategia por defecto casi nunca es la que quieres. La fuga de ámbito compila y no avisa. | 07, 24 |
| **El MOP** | Potentísimo y global: modificar una metaclase afecta a todo el proceso. Y `@CompileStatic` lo apaga entero. | 16, 15 |
| **Las transformaciones AST** | Hay más de treinta y cada una tiene sus opciones. Lo difícil es saber qué genera cada una exactamente. | 12 |
| **`@CompileStatic`** | Da seguridad y velocidad a cambio de quitarte media mitad dinámica del lenguaje. Dónde ponerlo es una decisión de diseño. | 15 |
| **Multimétodos** | Código idéntico al de Java elige otra sobrecarga. Y quien resuelve no es el lenguaje del método, sino el de la llamada. | 10, 26 |

---

## Estructura y convenciones

```
src/
├── main/
│   ├── groovy/com/alejandro/
│   │   ├── Main.groovy                 el lanzador: registra los 30 capítulos
│   │   ├── infra/                      Demo.groovy, Console.groovy, Launcher.groovy
│   │   ├── c01basics/
│   │   │   ├── C01_00Index.groovy      el índice del capítulo + su main()
│   │   │   ├── C01_01HelloWorld.groovy
│   │   │   └── …
│   │   ├── …
│   │   └── c30exercises/
│   └── java/com/alejandro/c26javainterop/legacy/    3 clases Java de verdad
└── test/groovy/com/alejandro/
    ├── infra/                          registro, humo y lanzador
    ├── c28spock/
    └── c30exercises/
```

- **Paquetes numerados** (`c08lists`): el orden alfabético del árbol de ficheros
  **es** el orden pedagógico.
- **Un `main()` por capítulo**, en su `CNN_00Index`. Groovy permitiría uno por fichero,
  pero un centenar de `main` de ceremonia contradice el Groovy idiomático que el
  repositorio pretende enseñar.
- **Identificadores en inglés** (paquetes, ficheros, métodos `demoXxx`), para poder
  cruzar cada capítulo con [groovy-lang.org](https://groovy-lang.org/documentation.html).
  **Comentarios y salida en español.** Los modelos de dominio también
  (`Usuario`, `Carrito`, `Pedido`): se leen mejor.
- **Sin acentos en los identificadores.** El nombre de una clase acaba siendo el nombre
  de un fichero, y un carácter no ASCII rompe la generación de stubs de Java en cuanto
  la plataforma no está en UTF-8. Acentos en comentarios, cadenas y salida; nunca en
  nombres de tipos.
- **Cada fichero de contenido** lleva la misma cabecera — *Qué es · Por qué importa ·
  Errores comunes* — y termina con `// ── PARA EXPERIMENTAR ──`.
- **Ninguna demo lanza excepciones ni se cuelga.** Lo que enseña errores los captura y
  los imprime; los ficheros se crean en temporales y se borran; los hilos se esperan y
  los pools se cierran.

### Añadir tu propia demo

```groovy
// 1. En cualquier clase del capítulo, un método estático sin parámetros:
static void demoLoMio() {
    section('Lo mío')
    show('resultado', 2 + 2)
}

// 2. Regístralo en el CNN_00Index de ese capítulo:
demo('Lo mío') { C08_01Basics.demoLoMio() }
```

Nada más: el identificador (`8.13`) se genera solo por orden de declaración, y el test
de humo lo ejecutará a partir de ese momento.

---

## Dependencias y JDK

```xml
<dependency>org.apache.groovy:groovy</dependency>            <!-- el núcleo -->
<dependency>org.apache.groovy:groovy-json</dependency>       <!-- capítulo 22 -->
<dependency>org.apache.groovy:groovy-xml</dependency>        <!-- capítulo 23 -->
<dependency>org.apache.groovy:groovy-templates</dependency>  <!-- capítulo 24 -->
<dependency>org.apache.groovy:groovy-datetime</dependency>   <!-- capítulo 20 -->
<dependency>org.apache.groovy:groovy-nio</dependency>        <!-- capítulo 21 -->

<dependency>org.spockframework:spock-core</dependency>       <!-- capítulo 28 -->
```

Groovy **5.1.1**, Maven **3.9.11** (incluido en el wrapper), JDK **21**.

Existe `groovy-all`, que trae los 22 módulos de golpe. Aquí se declaran uno a uno a
propósito: es lo correcto en un proyecto real, y de paso el `pom.xml` documenta qué
capítulo necesita qué.

**Para usar otro JDK**, cambia una línea del `pom.xml`:

```xml
<groovyReference.jdk>25</groovyReference.jdk>
```

21 es el mínimo (el capítulo 27 usa hilos virtuales) y el LTS más extendido.

> **Sobre `byte-buddy` y `objenesis`:** son opcionales de Spock y hay que pedirlas a
> mano. Sin ellas, `Mock(UnaInterfaz)` funciona pero `Mock(UnaClase)` falla **en
> ejecución**. El capítulo 28 lo usa.

---

## Tests

```bash
./mvnw test                            # los 190 tests
./mvnw test -Dtest=CarritoSpec         # un solo Spec
./mvnw verify                          # compila Groovy + Java y ejecuta los tests
```

El informe queda en `target/surefire-reports/`.

Qué hay dentro:

- **`infra/SmokeSpec`** — ejecuta **las 204 demos** y falla si alguna lanza o no
  imprime nada. Es lo que convierte 104 ficheros de ejemplos en código *verificado*.
- **`infra/RegistrySpec`** — los 30 capítulos están numerados sin huecos, no hay
  identificadores repetidos y nada está en blanco.
- **`infra/LauncherSpec`** — el lanzador entiende sus comandos (contra capítulos de
  mentira, para que no se rompa al añadir uno nuevo).
- **`c28spock/`** — el código del capítulo 28, con sus 29 tests: tablas `where:`,
  interacciones y los cinco tipos de doble.
- **`c30exercises/`** — un Spec por ejercicio, validando la solución propuesta. Si
  rompes una al experimentar, te enteras.

### En cada push y cada pull request

`.github/workflows/build.yml` ejecuta en GitHub Actions lo mismo que ejecutas tú:
`./mvnw verify` (compilación mixta Groovy + Java y los 190 tests) y después el
recorrido completo de las 204 demos desde el lanzador. Si algún test falla, el informe
queda como artefacto descargable de la ejecución.

---

## Problemas frecuentes

<details>
<summary><b>El menú interactivo no aparece / el programa sale solo</b></summary>

Falta la entrada estándar. Con `exec:java` funciona porque el programa corre dentro de
la JVM de Maven y hereda su `stdin`. Si ejecutas desde CI o con una tubería vacía, el
lanzador lo detecta, imprime el índice y sale limpiamente en lugar de colgarse.
Usa `-Dexec.args="13.4"` para ir directo a una demo.
</details>

<details>
<summary><b>Se ven interrogaciones en vez de acentos</b></summary>

Es la codificación. El proyecto ya fuerza UTF-8 en `.mvn/jvm.config`
(`-Dfile.encoding`, `-Dstdout.encoding`, `-Dstderr.encoding`), pero el terminal también
tiene que estar en UTF-8. En Windows: `chcp 65001`.

Matiz poco conocido: desde el JDK 19, `System.out` **no** usa `file.encoding`, sino la
codificación nativa de la consola. Por eso hacen falta las tres propiedades, y por eso
están en `jvm.config` y no en la configuración del plugin: `exec:java` hereda la JVM de
Maven, no crea una nueva.
</details>

<details>
<summary><b><code>Picked up JAVA_TOOL_OPTIONS: …</code> en la salida</b></summary>

No es un error: es la JVM avisando de que hay una variable de entorno definida. Sale
por la salida de error y no afecta a nada.
</details>

<details>
<summary><b>Maven no encuentra el JDK, o se queja de la versión</b></summary>

Cambia `<groovyReference.jdk>` en el `pom.xml` al que tengas instalado (21 o superior).
El capítulo 27 usa hilos virtuales, así que por debajo de 21 no compila.
</details>

<details>
<summary><b><code>./mvnw: Permission denied</code></b></summary>

`chmod +x mvnw`. En Windows usa `mvnw.cmd`.
</details>

<details>
<summary><b>La primera ejecución tarda muchísimo</b></summary>

Está descargando Maven y las dependencias. Sólo pasa una vez. El wrapper usa
`distributionType=only-script`, así que no hay ningún `.jar` versionado en el
repositorio.
</details>

<details>
<summary><b>Un cambio no se ve al ejecutar</b></summary>

`exec:java` no recompila por sí solo. Usa `./mvnw -q compile exec:java -Dexec.args="…"`,
o el script de `target/dist/bin/` después de un `package`.
</details>

<details>
<summary><b>Quiero un botón ▶ por cada fichero, no sólo por capítulo</b></summary>

Groovy sí lo permite: a diferencia de otros lenguajes de la JVM, cada clase puede tener
su propio `static void main`. Si lo quieres, añade al final de la clase:

```groovy
static void main(String[] args) { demoLoQueSea() }
```

No se hace por defecto porque un centenar de `main` de ceremonia contradice el Groovy
idiomático que el repositorio pretende enseñar. Hay uno por capítulo.
</details>

---

## Por dónde empezar, en una línea

```bash
./mvnw -q exec:java -Dexec.args="1"
```

Y después, el capítulo 30: los ejercicios son donde de verdad se aprende.
