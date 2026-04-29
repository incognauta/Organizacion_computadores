# USER_GUIDE.md — Guía de uso

## Requisitos

- JDK 8 o superior. Verifica con:
  ```
  javac -version
  java -version
  ```


## Compilación

Desde la raíz del proyecto:

```
cd proyecto3/HackAssembler/src
javac *.java
```

Se generan los `.class` en la misma carpeta. No hay que incluirlos
en el repositorio.

## Modo assembler: `.asm` → `.hack`

```
java HackAssembler MiPrograma.asm
```

Genera `MiPrograma.hack` en el mismo directorio.

Si la traducción es exitosa, el programa **no imprime nada** y sale
con código 0.

Si hay un error, imprime un mensaje del tipo:

```
Error en linea 7: comp invalido: 'D++Q'
```

y sale con código 1. El archivo `.hack` parcial se elimina
automáticamente.

### Ejemplo

Archivo `Suma.asm`:

```
// Suma 1+2+3+...+10 y deja el resultado en R0
@i
M=1
@sum
M=0
(LOOP)
@i
D=M
@10
D=D-A
@END
D;JGT
@sum
M=M+D
@i
M=M+1
@LOOP
0;JMP
(END)
@sum
D=M
@R0
M=D
(FIN)
@FIN
0;JMP
```

Ejecuta:

```
java HackAssembler Suma.asm
```

Genera `Suma.hack` con una línea de 16 bits por instrucción real.

## Modo desensamblador: `.hack` → `.asm`

```
java HackAssembler -d MiPrograma.hack
```

Genera `MiProgramaDis.asm` (nota el sufijo `Dis` para no pisar el
fuente original).

## Instrucciones de shift

El traductor reconoce las siguientes formas:

| Sintaxis | Significado |
|---|---|
| `dest=A<<1` | dest recibe A desplazado 1 bit a la izquierda |
| `dest=D<<1` | dest recibe D desplazado 1 bit a la izquierda |
| `dest=M<<1` | dest recibe M (=RAM[A]) desplazado 1 bit a la izquierda |
| `dest=A>>1` | dest recibe A desplazado 1 bit a la derecha |
| `dest=D>>1` | dest recibe D desplazado 1 bit a la derecha |
| `dest=M>>1` | dest recibe M desplazado 1 bit a la derecha |

`dest` puede ser cualquier combinación de `A`, `M`, `D`.

**Nota**: `A<<1` y `D<<1` producen el **mismo binario**. El
desensamblador siempre los muestra como `A<<1` por convención. Esto
no cambia el comportamiento del programa.

## Pruebas

```
cd proyecto3/HackAssembler/test
javac -cp ../src HackAssemblerTest.java
java  -cp ".;../src" HackAssemblerTest        # Windows
java  -cp ".:../src" HackAssemblerTest        # Linux/Mac
```

Salida esperada al final:

```
=== Resumen ===
Passed: NN
Failed: 0
```

## Errores comunes

| Mensaje | Causa | Solución |
|---|---|---|
| `archivo no encontrado` | Ruta mal escrita | Verifica el nombre |
| `valor fuera de rango (0..32767)` | `@N` con `N>32767` | Hack solo soporta 15 bits sin signo en tipo A |
| `simbolo invalido en @` | Empieza por dígito o tiene caracteres no permitidos | Usa solo letras, dígitos, `_`, `.`, `$`, `:`, sin empezar por dígito |
| `comp invalido` | Mnemónico no existe en la tabla | Revisa la sintaxis (espacios no importan, `D + A` = `D+A`) |
| `simbolo duplicado` | Etiqueta `(LABEL)` definida dos veces | Renombra una de las dos |

## Validación del binario en Nand2Tetris

Después de generar `Prog.hack`, puedes cargarlo en el emulador
oficial de Nand2Tetris (CPU Emulator) o en la CPU implementada en
el Proyecto 2 para verificar que se ejecuta correctamente.
