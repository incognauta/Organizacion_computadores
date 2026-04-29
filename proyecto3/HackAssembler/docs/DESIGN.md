# DESIGN.md — Decisiones de diseño

## Vista general

El traductor está dividido en **6 clases** con responsabilidad única.
Esta separación es deliberada: la rúbrica del proyecto otorga
bonificación al criterio de modularidad, y permite que cada pieza se
pueda testear y mantener aisladamente.

```
                        ┌───────────────────┐
                        │ HackAssembler.main│
                        └─────────┬─────────┘
                ┌─────────────────┴─────────────────┐
                ▼                                   ▼
   ┌────────────────────────┐        ┌─────────────────────────┐
   │ HackAssembler.assemble │        │ HackDisassembler        │
   │ (.asm → .hack)         │        │ (.hack → .asm)          │
   └─────┬────────┬─────────┘        └────────────┬────────────┘
         │        │                               │
         ▼        ▼                               ▼
   ┌────────┐ ┌─────────────┐                 ┌──────┐
   │ Parser │ │ SymbolTable │                 │ Code │  (uso inverso)
   └────┬───┘ └─────────────┘                 └──────┘
        │
        ▼
     ┌──────┐
     │ Code │
     └──────┘

   AssemblerException la lanza cualquier clase y la captura main().
```

## Clases y responsabilidades

| Clase | Responsabilidad única |
|---|---|
| `Code` | Tabla pura mnemónico ↔ bits. No abre archivos, no parsea. |
| `SymbolTable` | Diccionario `símbolo → dirección`. Carga predefinidos. |
| `Parser` | Lee `.asm`, descarta comentarios/blancos, identifica tipo y campos. |
| `HackAssembler` | Orquesta la traducción `.asm → .hack`. Implementa main. |
| `HackDisassembler` | Orquesta `.hack → .asm`. |
| `AssemblerException` | Excepción con número de línea. |

## Por qué dos pasadas en el ensamblador

Hack permite **referencias hacia adelante**:
```
@LOOP        ← linea 0 usa LOOP
...
(LOOP)       ← linea 10 lo define
```
Cuando el parser ve `@LOOP` en línea 0, todavía no sabe a qué
dirección apunta. Es imposible traducir en una sola pasada sin
"guardar lugar". Solución estándar: dos pasadas.

**Pasada 1**: recolección de etiquetas. Recorre el archivo llevando
un contador `direccionRom`. Cuando encuentra `(LABEL)`, registra
`LABEL → direccionRom` y NO incrementa el contador (las etiquetas no
ocupan ROM). Cuando encuentra A o C, sí incrementa.

**Pasada 2**: traducción. Recorre con `proximaRam = 16`. Para cada
A: si es número, escribe el valor; si es símbolo conocido, usa su
dirección; si es nuevo, lo registra como variable en `proximaRam`
e incrementa. Para cada C: usa `Code` para los tres campos.

## Por qué una sola pasada en el desensamblador

El `.hack` ya tiene las direcciones resueltas. No hay etiquetas ni
variables. Cada línea de 16 bits es **autónoma**: el bit 0 indica
tipo (A o C) y los demás bits dicen el resto. No hay dependencias
entre líneas. Una pasada secuencial basta.

## Formato de salida

- **`.hack`**: una línea por instrucción, exactamente 16 caracteres
  '0'/'1', terminada en LF (`\n`). NO usamos `println` ni
  `BufferedWriter.newLine()` porque en Windows producen CRLF
  (`\r\n`) y algunos emuladores Nand2Tetris JS los rechazan o
  añaden caracteres invisibles. Escribimos byte a byte controlando
  el separador.
- **`.asm`**: igual con LF.

## Convención del shift (decisión documentada)

En la ALU extendida del Proyecto 2, los binarios `0000001` (con
`a=0`) y `1000001` se generan tanto desde `A<<1`/`D<<1` como desde
`M<<1` respectivamente. Para `a=0` la información de "era A o era
D" no está en el binario porque el shifter recibe un solo operando
y la ALU no tiene un bit adicional para distinguirlos.

**Decisión**: el desensamblador, ante el patrón `a=0` con shift,
emite la forma con **A** (no con D). Esto:

1. Es **idempotente**: ensamblar → desensamblar → ensamblar produce
   el mismo binario.
2. **Preserva el comportamiento** del programa: el binario es
   idéntico, así que la ejecución en CPU es exactamente la misma.
3. **No preserva el texto original**: si el programador escribió
   `D=D<<1`, el desensamblador lo mostrará como `D=A<<1`. Esto es
   inevitable sin metadatos adicionales.

Las dos formas (`A<<1`, `D<<1`) son aceptadas por el assembler;
solo la forma con A se emite por el desensamblador. La asimetría
está documentada y es consistente con el `design.txt` del Proyecto 2.

## Por qué `Code` devuelve 7 bits para `comp` (no 6)

El bit `a` y los 6 bits `cccccc` son **una sola decisión**: ambos
los determina el mnemónico de la operación. Devolverlos juntos
evita que el orquestador haga dos consultas a la misma tabla y
mantiene una única fuente de verdad. Es consistente con la ISA
descrita en el libro Nand2Tetris (capítulo 6).

## Por qué el contador de variables NO vive en `SymbolTable`

`SymbolTable` es un diccionario puro `String → int`. Si llevara el
contador `nextRamAddress = 16`, tendría que saber que las variables
empiezan en 16 — eso es **política del lenguaje Hack**, no de "una
tabla de símbolos genérica". Manteniendo la tabla agnóstica, podría
reusarse en otro assembler con otra política sin tocarla.

Trade-off: muchos libros lo hacen al revés (la tabla lleva el
contador). Ambas son válidas; nuestra decisión privilegia la
reusabilidad sobre la conveniencia.

## Manejo de errores

`AssemblerException` es la única excepción de dominio. La lanzan
`Code`, `SymbolTable`, `Parser` y los orquestadores. La captura
`main()` y la convierte en el mensaje "Error en linea N: ..."
que pide el enunciado.

Cuando ocurre un error:
1. Se cierra el archivo de salida.
2. Se elimina el archivo parcial para no dejar `.hack` corrupto.
3. Se imprime el mensaje y se sale con código 1.

## Validaciones

- `@valor` con número fuera del rango `[0, 32767]` → error explícito
  (no truncamiento silencioso). Hack es 15 bits sin signo en tipo A.
- Símbolos inválidos (empiezan con dígito o caracteres ilegales)
  → error.
- Etiqueta `(LABEL)` duplicada → error en pasada 1.
- Mnemónicos `comp`/`dest`/`jump` desconocidos → error.

## Por qué tests sin JUnit

El enunciado pide un solo `HackAssemblerTest.java`. Usar JUnit
requeriría agregar `junit.jar` al classpath y complicar la
compilación. Las pruebas con un `main()` que cuenta PASS/FAIL son
suficientes para sustentar correctness y se compilan con `javac`
puro sin dependencias.

## Cómo extender la ALU con una operación nueva

Si mañana se agregara, por ejemplo, una rotación `<<<1`:

1. Agregar las entradas en `Code.java` (3 líneas en la tabla COMP).
2. Listo.

`Parser`, `SymbolTable`, `HackAssembler`, `HackDisassembler` y
`Main` no cambian. Esta es la prueba operacional de que el diseño
está bien modularizado.
