# Changelog

## [2.0.0] - 2026-04-28

**Proyecto 3** — Traductor y desensamblador del assembler Hack.

### Added
- `proyecto3/HackAssembler/src/AssemblerException.java`: Excepción
  propia con número de línea para reportes uniformes "Error en linea N".
- `proyecto3/HackAssembler/src/Code.java`: Tabla de traducción
  mnemónico ↔ bits para los campos `comp`, `dest` y `jump`. Incluye
  las extensiones de shift (`<<1`, `>>1`) y soporta traducción inversa
  para el desensamblador.
- `proyecto3/HackAssembler/src/SymbolTable.java`: Diccionario
  símbolo → dirección con los predefinidos del lenguaje Hack
  (R0–R15, SP, LCL, ARG, THIS, THAT, SCREEN, KBD).
- `proyecto3/HackAssembler/src/Parser.java`: Lector sintáctico que
  descarta comentarios y blancos, identifica tipo de instrucción
  (A, C, L) y extrae los campos correspondientes.
- `proyecto3/HackAssembler/src/HackAssembler.java`: Orquestador
  `.asm → .hack` con algoritmo de dos pasadas. Punto de entrada del
  programa.
- `proyecto3/HackAssembler/src/HackDisassembler.java`: Orquestador
  `.hack → .asm` de una sola pasada. Aplica convención canónica
  para shifts ambiguos (`a=0` → forma con A).
- `proyecto3/HackAssembler/test/HackAssemblerTest.java`: Suite de
  pruebas manuales con `main()` que imprime PASS/FAIL. Cubre Code,
  SymbolTable, Parser y end-to-end (assemble, disassemble, roundtrip).
- Documentación en `proyecto3/HackAssembler/docs/`:
  `DESIGN.md`, `API.md`, `USER_GUIDE.md`.
- Archivos `.md5` por cada `.java` para verificación de integridad.

### Tested
- Traducción de instrucciones tipo A con valores y símbolos.
- Traducción de instrucciones tipo C completas, sin dest, sin jump.
- Detección de etiquetas en pasada 1 y referencias hacia adelante.
- Asignación automática de variables desde RAM[16].
- Traducción de las 6 formas del shift (`A/D/M` × `<<1/>>1`).
- Roundtrip: ensamblar → desensamblar → re-ensamblar produce
  binario idéntico.
- Validaciones de error: `@N` fuera de rango, símbolos inválidos,
  etiquetas duplicadas, mnemónicos inexistentes.

## [1.0.0] - 2026-04-27

**Estado:** Todos los puntos completados ✅

### Added
- `Shifter.hdl`: Implementación del circuito de desplazamiento de 1 bit a la 
  izquierda (direction=0) y derecha (direction=1) con salida `result` que 
  captura el bit desplazado fuera del extremo correspondiente.

- `ALU.hdl`: Implementación de la ALU del computador Hack extendida con soporte 
  para las instrucciones Shift Left y Shift Right. La ALU detecta el modo shift 
  cuando `zx=nx=zy=ny=0` y `no=1`, usando `f` para determinar la dirección.

- `Memory.hdl`: Implementación del mapa de memoria del computador Hack.
  - Direcciones `0x0000–0x3FFF` → RAM16K
  - Direcciones `0x4000–0x5FFF` → Screen
  - Dirección  `0x6000`         → Keyboard (solo lectura)

- `CPU.hdl`: Implementación de la CPU del computador Hack con soporte para 
  instrucciones tipo A y tipo C, incluyendo la ALU extendida con Shifter, 
  registros A y D, lógica de salto y Program Counter.

- `Computer.hdl`: Implementación del computador Hack completo integrando 
  CPU, Memory y ROM32K.

- `design.txt`: Especificación completa del formato de instrucciones para 
  Shift Left (`<<`) y Shift Right (`>>`). Incluye:
  - Formato binario de instrucciones tipo C
  - Mapeo de bits de control a la ALU
  - Condiciones de activación del Shifter
  - Ejemplos de uso en assembler

### Tested
- `Shifter.hdl`: Validado manualmente con los casos de la Tabla 1 del enunciado.
- `ALU.hdl`: Validado con `ALU-basic.tst` — todos los casos pasan correctamente.
- `Memory.hdl`: Validado con `Memory.tst` del proyecto 5 de Nand2Tetris.
- `CPU.hdl`: Validado con `CPU.tst` del proyecto 5 de Nand2Tetris.
- `Computer.hdl`: Validado con `ComputerAdd.tst` y `ComputerMax.tst` del 
  proyecto 5 de Nand2Tetris.
- `design.txt`: Validado con checksums MD5 y revisión técnica.