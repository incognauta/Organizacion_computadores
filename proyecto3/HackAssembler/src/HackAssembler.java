/*********
 * HackAssembler.java – Punto de entrada del traductor. Coordina al
 * Parser, la SymbolTable y la clase Code para transformar un archivo
 * .asm en su equivalente .hack (binario textual de 16 bits por linea).
 *
 * Funciona en dos pasadas sobre el archivo:
 *   Pasada 1: registra las etiquetas (LABEL) en la tabla de simbolos
 *             asociandolas a la direccion de la siguiente instruccion.
 *   Pasada 2: traduce cada instruccion A o C a binario, asignando
 *             direcciones a las variables nuevas a partir de RAM[16].
 *
 * Tambien actua como dispatcher: si el primer argumento es "-d",
 * delega en HackDisassembler para hacer el camino inverso.
 *
 * Uso:
 *   java HackAssembler Prog.asm        -> genera Prog.hack
 *   java HackAssembler -d Prog.hack    -> genera ProgDis.asm
 *
 * Autor 1: Juan Andres Salcedo
 * Autor 2: Juan Manuel Escobar
 *********/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Traductor de Hack assembly a Hack binario. Implementa el algoritmo
 * de dos pasadas descrito en el capitulo 6 de Nand2Tetris.
 */
public class HackAssembler {

    /** Direccion donde empiezan a asignarse las variables de usuario. */
    private static final int RAM_INICIO_VARIABLES = 16;

    /** Maximo valor permitido en una instruccion @ (15 bits sin signo). */
    private static final int MAX_VALOR_A = 32767;

    /**
     * Punto de entrada. Decide entre modo assembler y modo
     * desensamblador segun los argumentos.
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("Uso: java HackAssembler [-d] archivo");
                System.exit(1);
            }

            if (args[0].equals("-d")) {
                if (args.length < 2) {
                    System.err.println("Uso: java HackAssembler -d archivo.hack");
                    System.exit(1);
                }
                HackDisassembler.disassemble(args[1]);
            } else {
                assemble(args[0]);
            }
        } catch (AssemblerException ex) {
            // Mensaje uniforme con numero de linea cuando esta disponible.
            if (ex.getLinea() > 0) {
                System.err.println("Error en linea " + ex.getLinea() + ": " + ex.getMessage());
            } else {
                System.err.println("Error: " + ex.getMessage());
            }
            System.exit(1);
        } catch (IOException ex) {
            System.err.println("Error de E/S: " + ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Traduce un archivo .asm a .hack.
     *
     * @param rutaAsm camino al archivo de entrada
     * @throws IOException        si la lectura/escritura falla
     * @throws AssemblerException si hay error sintactico o semantico
     */
    public static void assemble(String rutaAsm) throws IOException, AssemblerException {
        // Validacion basica de la entrada
        File entrada = new File(rutaAsm);
        if (!entrada.exists() || !entrada.isFile()) {
            throw new AssemblerException(
                "archivo no encontrado: " + rutaAsm);
        }

        // Determinar nombre de salida: cambiar extension .asm -> .hack
        String rutaHack = derivarNombreSalida(rutaAsm);
        File salida = new File(rutaHack);

        Parser parser = new Parser(rutaAsm);
        SymbolTable tabla = new SymbolTable();

        try {
            // -----------------------------------------------------------
            // PASADA 1: recolectar etiquetas
            // -----------------------------------------------------------
            primeraPasada(parser, tabla);

            // -----------------------------------------------------------
            // PASADA 2: traducir y escribir
            // -----------------------------------------------------------
            parser.reset();
            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(
                        new FileOutputStream(salida),
                        StandardCharsets.US_ASCII))) {
                segundaPasada(parser, tabla, bw);
            }
        } catch (AssemblerException | IOException ex) {
            // Si hubo error, eliminar el archivo de salida parcial para
            // no dejar un .hack invalido en el disco.
            if (salida.exists()) {
                salida.delete();
            }
            throw ex;
        }
    }

    /**
     * Primera pasada: recorre el archivo y registra cada etiqueta
     * (LABEL) en la tabla de simbolos asociandola con el numero de
     * instruccion (direccion en ROM) de la SIGUIENTE instruccion real.
     * Las etiquetas no ocupan ROM, son solo punteros.
     */
    private static void primeraPasada(Parser parser, SymbolTable tabla)
            throws AssemblerException {
        int direccionRom = 0;
        while (parser.hasMoreLines()) {
            parser.advance();
            Parser.InstructionType tipo = parser.instructionType();
            if (tipo == Parser.InstructionType.L_INSTRUCTION) {
                String etiqueta = parser.symbol();
                if (!esSimboloValido(etiqueta)) {
                    throw new AssemblerException(
                        "etiqueta invalida: '" + etiqueta + "'",
                        parser.currentLineNumber());
                }
                try {
                    tabla.addEntry(etiqueta, direccionRom);
                } catch (AssemblerException ex) {
                    throw new AssemblerException(
                        ex.getMessage(), parser.currentLineNumber());
                }
            } else {
                // A o C ocupan una direccion en ROM
                direccionRom++;
            }
        }
    }

    /** Contador interno de la siguiente direccion libre para variables. */
    private static int proximaRam;

    /**
     * Segunda pasada: traduce A y C a binario. Asigna direcciones a
     * las variables nuevas a partir de RAM[16]. Escribe el resultado
     * linea a linea con LF puro (sin CRLF) por compatibilidad con el
     * emulador Nand2Tetris.
     */
    private static void segundaPasada(Parser parser, SymbolTable tabla,
                                      BufferedWriter bw)
            throws AssemblerException, IOException {
        proximaRam = RAM_INICIO_VARIABLES;

        while (parser.hasMoreLines()) {
            parser.advance();
            Parser.InstructionType tipo = parser.instructionType();

            if (tipo == Parser.InstructionType.L_INSTRUCTION) {
                // Las etiquetas ya se procesaron en pasada 1; se ignoran.
                continue;
            }

            String binario;
            if (tipo == Parser.InstructionType.A_INSTRUCTION) {
                binario = traducirA(parser, tabla);
            } else {
                binario = traducirC(parser);
            }

            bw.write(binario);
            // LF puro, no usar newLine() porque en Windows seria CRLF.
            bw.write('\n');
        }
    }

    /**
     * Traduce una instruccion tipo A a sus 16 bits.
     * Si el simbolo no es numero ni esta registrado, lo agrega como
     * variable nueva con la siguiente direccion libre en RAM.
     */
    private static String traducirA(Parser parser, SymbolTable tabla)
            throws AssemblerException {
        String sym = parser.symbol();
        int valor;

        if (esNumero(sym)) {
            try {
                valor = Integer.parseInt(sym);
            } catch (NumberFormatException ex) {
                throw new AssemblerException(
                    "valor numerico invalido en @: '" + sym + "'",
                    parser.currentLineNumber());
            }
            if (valor < 0 || valor > MAX_VALOR_A) {
                throw new AssemblerException(
                    "valor fuera de rango (0..32767) en @: " + valor,
                    parser.currentLineNumber());
            }
        } else {
            if (!esSimboloValido(sym)) {
                throw new AssemblerException(
                    "simbolo invalido en @: '" + sym + "'",
                    parser.currentLineNumber());
            }
            if (!tabla.contains(sym)) {
                // Variable nueva: asignar siguiente direccion libre
                tabla.addEntry(sym, proximaRam);
                proximaRam++;
            }
            valor = tabla.getAddress(sym);
        }

        // 16 bits: 0 + 15 bits del valor
        return String.format("%16s", Integer.toBinaryString(valor))
            .replace(' ', '0');
    }

    /**
     * Traduce una instruccion tipo C a sus 16 bits.
     * Formato: 111 a cccccc ddd jjj  (el "a + cccccc" lo da Code.comp)
     */
    private static String traducirC(Parser parser) throws AssemblerException {
        try {
            String comp = Code.comp(parser.comp());
            String dest = Code.dest(parser.dest().isEmpty() ? null : parser.dest());
            String jump = Code.jump(parser.jump().isEmpty() ? null : parser.jump());
            return "111" + comp + dest + jump;
        } catch (AssemblerException ex) {
            throw new AssemblerException(
                ex.getMessage(), parser.currentLineNumber());
        }
    }

    /**
     * @param s cadena
     * @return true si s representa un entero no negativo
     */
    private static boolean esNumero(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Valida que un simbolo cumpla las reglas del lenguaje Hack: no
     * empieza por digito y solo contiene letras, digitos, '_', '.',
     * '$' o ':'. La regla impide ambiguedades como un simbolo "123"
     * que se confundiria con un literal.
     */
    private static boolean esSimboloValido(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        char primero = s.charAt(0);
        if (Character.isDigit(primero)) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean ok = Character.isLetterOrDigit(c)
                || c == '_' || c == '.' || c == '$' || c == ':';
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    /**
     * Cambia la extension .asm a .hack. Si el archivo no termina en
     * .asm, simplemente le agrega .hack.
     */
    private static String derivarNombreSalida(String rutaAsm) {
        if (rutaAsm.toLowerCase().endsWith(".asm")) {
            return rutaAsm.substring(0, rutaAsm.length() - 4) + ".hack";
        }
        return rutaAsm + ".hack";
    }
}
