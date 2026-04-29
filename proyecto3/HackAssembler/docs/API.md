# API.md — Documentación de las clases

## `AssemblerException`

Excepción "checked" del proyecto. Lleva opcionalmente número de línea.

| Constructor | Descripción |
|---|---|
| `AssemblerException(String mensaje)` | Sin línea conocida. |
| `AssemblerException(String mensaje, int linea)` | Con número de línea. |

| Método | Descripción |
|---|---|
| `int getLinea()` | Línea (1-indexada) o `-1` si no aplica. |

## `Code`

Tabla pura mnemónico ↔ bits. Métodos estáticos, sin estado.

| Método | Descripción |
|---|---|
| `String comp(String mnem)` | Devuelve 7 bits (`a` + `cccccc`). Lanza `AssemblerException` si el mnemónico no existe. |
| `String dest(String mnem)` | 3 bits. `null`/`""` → `"000"`. |
| `String jump(String mnem)` | 3 bits. `null`/`""` → `"000"`. |
| `String compToMnemonic(String bits)` | 7 bits → mnemónico canónico. Para shifts con `a=0` devuelve la forma con `A`. |
| `String destToMnemonic(String bits)` | 3 bits → mnemónico (`""` para `000`). |
| `String jumpToMnemonic(String bits)` | 3 bits → mnemónico (`""` para `000`). |

## `SymbolTable`

Diccionario `String → int` con predefinidos cargados.

| Método | Descripción |
|---|---|
| `SymbolTable()` | Crea tabla con predefinidos del lenguaje Hack. |
| `void addEntry(String, int)` | Registra símbolo nuevo. Lanza si ya existía. |
| `boolean contains(String)` | ¿Existe el símbolo? |
| `int getAddress(String)` | Devuelve la dirección. Lanza si no existe. |

Predefinidos: `R0..R15` (0..15), `SP`=0, `LCL`=1, `ARG`=2, `THIS`=3, `THAT`=4, `SCREEN`=16384, `KBD`=24576.

## `Parser`

Lectura sintáctica del `.asm`. Iterador con dos pasadas.

| Método | Descripción |
|---|---|
| `Parser(String ruta)` | Lee el archivo, descarta blancos y comentarios. |
| `boolean hasMoreLines()` | ¿Queda al menos una instrucción? |
| `void advance()` | Avanza a la siguiente. |
| `void reset()` | Reinicia el iterador (para segunda pasada). |
| `int currentLineNumber()` | Línea original (1-indexada). |
| `String currentLine()` | Texto crudo actual (sin comentarios ni espacios). |
| `InstructionType instructionType()` | `A_INSTRUCTION`, `C_INSTRUCTION`, `L_INSTRUCTION`. |
| `String symbol()` | Para A o L: lo que sigue a `@` o entre `()`. |
| `String dest()` | Para C: parte antes de `=`, `""` si no hay. |
| `String comp()` | Para C: parte entre `=` y `;` (o todo si no hay). |
| `String jump()` | Para C: parte después de `;`, `""` si no hay. |

## `HackAssembler`

Orquestador `.asm → .hack` y punto de entrada.

| Método | Descripción |
|---|---|
| `static void main(String[])` | Dispatcher: `-d` → desensamblador, default → assembler. |
| `static void assemble(String rutaAsm)` | Genera `<base>.hack` desde `<base>.asm`. |

## `HackDisassembler`

Orquestador `.hack → .asm`.

| Método | Descripción |
|---|---|
| `static void disassemble(String rutaHack)` | Genera `<base>Dis.asm` desde `<base>.hack`. |
