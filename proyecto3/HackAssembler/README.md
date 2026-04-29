# HackAssembler

**Organización de computadores**
**Proyecto 3**
**2026-1**

**Integrantes proyecto 3**
Juan Andres Salcedo
Juan Manuel Escobar

Traductor (assembler) y desensamblador del lenguaje Hack extendido con
las instrucciones de shift (`<<1` y `>>1`) que se agregaron a la ALU
en el Proyecto 2.

## Componentes

- **HackAssembler**: traduce un archivo `.asm` a su binario `.hack`.
- **HackDisassembler**: traduce un `.hack` de vuelta a `.asm`.

## Compilación

Sin dependencias externas. Solo se requiere un JDK (8 o superior):

```
cd src
javac *.java
```

## Uso

Modo assembler:

```
java HackAssembler Prog.asm
```
Genera `Prog.hack`.

Modo desensamblador:

```
java HackAssembler -d Prog.hack
```
Genera `ProgDis.asm`.

## Pruebas

```
cd test
javac -cp ../src HackAssemblerTest.java
java  -cp .:../src HackAssemblerTest
```

(En Windows usa `;` en vez de `:` en el classpath.)

La suite imprime `PASS`/`FAIL` por cada caso y un resumen al final.
Devuelve código de salida 0 si todo pasa, 1 si algo falla.

## Documentación

- [docs/DESIGN.md](docs/DESIGN.md) — decisiones de diseño y arquitectura.
- [docs/API.md](docs/API.md) — API pública de cada clase.
- [docs/USER_GUIDE.md](docs/USER_GUIDE.md) — guía de uso paso a paso.
