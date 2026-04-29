/*********
 * HackAssemblerTest.java – Pruebas unitarias del traductor y el
 * desensamblador. No depende de JUnit ni de ningun framework externo:
 * se ejecuta con un main() que imprime PASS/FAIL por cada caso, asi
 * el proyecto compila con javac puro sin necesidad de classpath
 * adicional.
 *
 * Cubre:
 *   - Tabla Code: comp, dest, jump (incluyendo shift)
 *   - SymbolTable: predefinidos, agregar, consultar, duplicados
 *   - Parser: limpieza de comentarios, extraccion de campos, shift
 *   - End-to-end: assemble + disassemble + roundtrip
 *
 * Autor 1: Juan Andres Salcedo
 * Autor 2: Juan Manuel Escobar
 *********/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Suite manual de pruebas. Cuenta PASS/FAIL globalmente y termina
 * con un resumen. Devuelve codigo de salida 0 si todo pasa, 1 si
 * algo falla, util para integrar con CI mas adelante.
 */
public class HackAssemblerTest {

    private static int passed = 0;
    private static int failed = 0;
    private static final List<String> fallos = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("=== HackAssembler test suite ===\n");

        // ---------- Code ----------
        testCodeCompBasico();
        testCodeCompConM();
        testCodeCompShift();
        testCodeDest();
        testCodeJump();
        testCodeCompInverso();
        testCodeShiftInversoCanonico();
        testCodeCompInvalidoLanzaError();

        // ---------- SymbolTable ----------
        testSymbolTablePredefinidos();
        testSymbolTableAgregar();
        testSymbolTableDuplicado();
        testSymbolTableNoExiste();

        // ---------- Parser ----------
        testParserComentarios();
        testParserAInstruction();
        testParserCInstruction();
        testParserCSinDest();
        testParserCSinJump();
        testParserLabel();
        testParserShift();
        testParserReset();

        // ---------- End to end ----------
        testEnsambladoSimple();
        testEnsambladoConSimbolos();
        testEnsambladoConShift();
        testDesensamblado();
        testRoundTripShift();
        testValorFueraRangoLanzaError();

        // ---------- Resumen ----------
        System.out.println("\n=== Resumen ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        if (!fallos.isEmpty()) {
            System.out.println("\nFallos:");
            for (String f : fallos) {
                System.out.println("  - " + f);
            }
        }
        System.exit(failed == 0 ? 0 : 1);
    }

    // ====================================================================
    // Code
    // ====================================================================

    private static void testCodeCompBasico() throws Exception {
        check("Code.comp(\"0\")",  "0101010", Code.comp("0"));
        check("Code.comp(\"D+A\")", "0000010", Code.comp("D+A"));
        check("Code.comp(\"D-1\")", "0001110", Code.comp("D-1"));
    }

    private static void testCodeCompConM() throws Exception {
        check("Code.comp(\"M\")",   "1110000", Code.comp("M"));
        check("Code.comp(\"D+M\")", "1000010", Code.comp("D+M"));
    }

    private static void testCodeCompShift() throws Exception {
        check("Code.comp(\"A<<1\")", "0000001", Code.comp("A<<1"));
        check("Code.comp(\"D<<1\")", "0000001", Code.comp("D<<1"));
        check("Code.comp(\"M<<1\")", "1000001", Code.comp("M<<1"));
        check("Code.comp(\"A>>1\")", "0000011", Code.comp("A>>1"));
        check("Code.comp(\"D>>1\")", "0000011", Code.comp("D>>1"));
        check("Code.comp(\"M>>1\")", "1000011", Code.comp("M>>1"));
    }

    private static void testCodeDest() throws Exception {
        check("Code.dest(null)", "000", Code.dest(null));
        check("Code.dest(\"M\")",   "001", Code.dest("M"));
        check("Code.dest(\"AMD\")", "111", Code.dest("AMD"));
    }

    private static void testCodeJump() throws Exception {
        check("Code.jump(null)",  "000", Code.jump(null));
        check("Code.jump(\"JMP\")", "111", Code.jump("JMP"));
        check("Code.jump(\"JEQ\")", "010", Code.jump("JEQ"));
    }

    private static void testCodeCompInverso() throws Exception {
        check("compToMnemonic(0000010)", "D+A", Code.compToMnemonic("0000010"));
        check("compToMnemonic(1000010)", "D+M", Code.compToMnemonic("1000010"));
    }

    private static void testCodeShiftInversoCanonico() throws Exception {
        // Convencion documentada: a=0 + shift -> A (no D)
        check("compToMnemonic(0000001) -> A<<1", "A<<1", Code.compToMnemonic("0000001"));
        check("compToMnemonic(0000011) -> A>>1", "A>>1", Code.compToMnemonic("0000011"));
        check("compToMnemonic(1000001) -> M<<1", "M<<1", Code.compToMnemonic("1000001"));
    }

    private static void testCodeCompInvalidoLanzaError() {
        try {
            Code.comp("D+++Q");
            recordFail("Code.comp invalido", "esperaba excepcion, no la hubo");
        } catch (AssemblerException ex) {
            recordPass("Code.comp invalido lanza AssemblerException");
        }
    }

    // ====================================================================
    // SymbolTable
    // ====================================================================

    private static void testSymbolTablePredefinidos() throws Exception {
        SymbolTable t = new SymbolTable();
        check("R0",     0,     t.getAddress("R0"));
        check("R15",    15,    t.getAddress("R15"));
        check("SP",     0,     t.getAddress("SP"));
        check("LCL",    1,     t.getAddress("LCL"));
        check("SCREEN", 16384, t.getAddress("SCREEN"));
        check("KBD",    24576, t.getAddress("KBD"));
    }

    private static void testSymbolTableAgregar() throws Exception {
        SymbolTable t = new SymbolTable();
        t.addEntry("LOOP", 10);
        check("LOOP en tabla", true, t.contains("LOOP"));
        check("LOOP=10",       10,   t.getAddress("LOOP"));
    }

    private static void testSymbolTableDuplicado() {
        try {
            SymbolTable t = new SymbolTable();
            t.addEntry("R0", 99);  // R0 ya esta predefinido
            recordFail("SymbolTable duplicado", "no lanzo excepcion");
        } catch (AssemblerException ex) {
            recordPass("SymbolTable detecta duplicado");
        }
    }

    private static void testSymbolTableNoExiste() {
        try {
            SymbolTable t = new SymbolTable();
            t.getAddress("NoExiste");
            recordFail("SymbolTable no existe", "no lanzo excepcion");
        } catch (AssemblerException ex) {
            recordPass("SymbolTable lanza error si simbolo no existe");
        }
    }

    // ====================================================================
    // Parser
    // ====================================================================

    private static void testParserComentarios() throws Exception {
        String contenido =
            "// comentario al inicio\n" +
            "\n" +
            "@5 // comentario al final\n" +
            "   \n" +
            "D=A\n";
        String tmp = escribirTmp("comentarios.asm", contenido);
        Parser p = new Parser(tmp);

        p.advance();
        check("primera no-blanco", "@5", p.currentLine());
        p.advance();
        check("segunda no-blanco", "D=A", p.currentLine());
        check("hasMore false al final", false, p.hasMoreLines());
        new File(tmp).delete();
    }

    private static void testParserAInstruction() throws Exception {
        String tmp = escribirTmp("ainst.asm", "@LOOP\n@123\n");
        Parser p = new Parser(tmp);
        p.advance();
        check("type A", Parser.InstructionType.A_INSTRUCTION, p.instructionType());
        check("symbol", "LOOP", p.symbol());
        p.advance();
        check("symbol numero", "123", p.symbol());
        new File(tmp).delete();
    }

    private static void testParserCInstruction() throws Exception {
        String tmp = escribirTmp("cinst.asm", "D=A+1;JMP\n");
        Parser p = new Parser(tmp);
        p.advance();
        check("type C", Parser.InstructionType.C_INSTRUCTION, p.instructionType());
        check("dest", "D",   p.dest());
        check("comp", "A+1", p.comp());
        check("jump", "JMP", p.jump());
        new File(tmp).delete();
    }

    private static void testParserCSinDest() throws Exception {
        String tmp = escribirTmp("nodest.asm", "0;JMP\n");
        Parser p = new Parser(tmp);
        p.advance();
        check("dest vacio", "",    p.dest());
        check("comp",      "0",    p.comp());
        check("jump",      "JMP",  p.jump());
        new File(tmp).delete();
    }

    private static void testParserCSinJump() throws Exception {
        String tmp = escribirTmp("nojump.asm", "M=D+1\n");
        Parser p = new Parser(tmp);
        p.advance();
        check("dest", "M",   p.dest());
        check("comp", "D+1", p.comp());
        check("jump vacio", "", p.jump());
        new File(tmp).delete();
    }

    private static void testParserLabel() throws Exception {
        String tmp = escribirTmp("label.asm", "(LOOP)\n@LOOP\n");
        Parser p = new Parser(tmp);
        p.advance();
        check("type L", Parser.InstructionType.L_INSTRUCTION, p.instructionType());
        check("label", "LOOP", p.symbol());
        new File(tmp).delete();
    }

    private static void testParserShift() throws Exception {
        String tmp = escribirTmp("shift.asm", "D=A<<1\nAM=M>>1\n");
        Parser p = new Parser(tmp);
        p.advance();
        check("comp shift left A", "A<<1", p.comp());
        p.advance();
        check("dest AM", "AM", p.dest());
        check("comp shift right M", "M>>1", p.comp());
        new File(tmp).delete();
    }

    private static void testParserReset() throws Exception {
        String tmp = escribirTmp("reset.asm", "@1\n@2\n");
        Parser p = new Parser(tmp);
        p.advance();
        p.advance();
        p.reset();
        p.advance();
        check("reset vuelve al primero", "@1", p.currentLine());
        new File(tmp).delete();
    }

    // ====================================================================
    // End-to-end: assemble, disassemble, roundtrip
    // ====================================================================

    private static void testEnsambladoSimple() throws Exception {
        // D=A; D=D+1
        String asm = "@5\nD=A\nD=D+1\n";
        String tmpAsm = escribirTmp("simple.asm", asm);
        HackAssembler.assemble(tmpAsm);

        String hack = leer(tmpAsm.replace(".asm", ".hack"));
        String esperado =
            "0000000000000101\n" +     // @5
            "1110110000010000\n" +     // D=A
            "1110011111010000\n";      // D=D+1
        check("ensamblado simple", esperado, hack);

        new File(tmpAsm).delete();
        new File(tmpAsm.replace(".asm", ".hack")).delete();
    }

    private static void testEnsambladoConSimbolos() throws Exception {
        // Suma desde @i hasta @sum, con etiquetas
        String asm =
            "@i\nM=1\n" +
            "@sum\nM=0\n" +
            "(LOOP)\n" +
            "@i\nD=M\n" +
            "@10\nD=D-A\n" +
            "@END\nD;JGT\n" +
            "@sum\nM=D+M\n" +
            "@LOOP\n0;JMP\n" +
            "(END)\n" +
            "@END\n0;JMP\n";
        String tmpAsm = escribirTmp("simbolos.asm", asm);
        HackAssembler.assemble(tmpAsm);
        String hack = leer(tmpAsm.replace(".asm", ".hack"));

        // Validacion minima: cada linea es 16 chars + LF, y conserva
        // el numero de instrucciones reales del archivo fuente.
        // El asm tiene 16 instrucciones (las dos etiquetas no cuentan).
        String[] lineas = hack.split("\n");
        check("16 instrucciones", 16, lineas.length);
        boolean todas16 = true;
        for (String l : lineas) {
            if (l.length() != 16) {
                todas16 = false;
                break;
            }
        }
        check("todas las lineas son 16 bits", true, todas16);

        new File(tmpAsm).delete();
        new File(tmpAsm.replace(".asm", ".hack")).delete();
    }

    private static void testEnsambladoConShift() throws Exception {
        // D=A<<1 -> 1110 0000 0010 0000
        // D=M<<1 -> 1111 0000 0010 0000
        // AM=M>>1 -> 1111 0000 1110 1000... espera, recalculo:
        //   prefijo 111
        //   a=1 cccccc=000011  (M>>1)
        //   ddd = AM = 101
        //   jjj = 000
        //   = 1111 0000 1110 1000... no: 111 1 000011 101 000 = 1111000011101000
        String asm = "D=A<<1\nD=M<<1\nAM=M>>1\n";
        String tmpAsm = escribirTmp("shift.asm", asm);
        HackAssembler.assemble(tmpAsm);
        String hack = leer(tmpAsm.replace(".asm", ".hack"));

        String esperado =
            "1110000001010000\n" +
            "1111000001010000\n" +
            "1111000011101000\n";
        check("ensamblado con shift", esperado, hack);

        new File(tmpAsm).delete();
        new File(tmpAsm.replace(".asm", ".hack")).delete();
    }

    private static void testDesensamblado() throws Exception {
        // Tomamos el binario equivalente a "@5\nD=A\n"
        String hack =
            "0000000000000101\n" +
            "1110110000010000\n";
        String tmpHack = escribirTmp("dis.hack", hack);
        HackDisassembler.disassemble(tmpHack);
        String asm = leer(tmpHack.replace(".hack", "Dis.asm"));

        String esperado = "@5\nD=A\n";
        check("desensamblado", esperado, asm);

        new File(tmpHack).delete();
        new File(tmpHack.replace(".hack", "Dis.asm")).delete();
    }

    private static void testRoundTripShift() throws Exception {
        // Ensamblar, desensamblar, re-ensamblar; el binario final
        // debe ser identico al primero (la convencion canonica
        // garantiza idempotencia).
        String asm = "D=A<<1\nM=M>>1\n";
        String tmpAsm = escribirTmp("rt.asm", asm);
        HackAssembler.assemble(tmpAsm);
        String hack1 = leer(tmpAsm.replace(".asm", ".hack"));

        // Renombrar para disassemble
        File hackFile = new File(tmpAsm.replace(".asm", ".hack"));
        File hackRenamed = new File("rtbin.hack");
        hackFile.renameTo(hackRenamed);

        HackDisassembler.disassemble("rtbin.hack");
        String asm2 = leer("rtbinDis.asm");

        // Re-ensamblar
        File rtAsm = new File("rt2.asm");
        try (FileWriter fw = new FileWriter(rtAsm)) {
            fw.write(asm2);
        }
        HackAssembler.assemble("rt2.asm");
        String hack2 = leer("rt2.hack");

        check("roundtrip preserva binario", hack1, hack2);

        new File(tmpAsm).delete();
        hackRenamed.delete();
        new File("rtbinDis.asm").delete();
        rtAsm.delete();
        new File("rt2.hack").delete();
    }

    private static void testValorFueraRangoLanzaError() throws Exception {
        String asm = "@40000\n";
        String tmpAsm = escribirTmp("rango.asm", asm);
        try {
            HackAssembler.assemble(tmpAsm);
            recordFail("valor fuera de rango", "no lanzo excepcion");
        } catch (AssemblerException ex) {
            recordPass("valor fuera de rango detectado");
        }
        new File(tmpAsm).delete();
        File hack = new File(tmpAsm.replace(".asm", ".hack"));
        if (hack.exists()) hack.delete();
    }

    // ====================================================================
    // Helpers
    // ====================================================================

    private static <T> void check(String nombre, T esperado, T obtenido) {
        if ((esperado == null && obtenido == null)
                || (esperado != null && esperado.equals(obtenido))) {
            recordPass(nombre);
        } else {
            recordFail(nombre,
                "esperado=[" + esperado + "] obtenido=[" + obtenido + "]");
        }
    }

    private static void recordPass(String nombre) {
        passed++;
        System.out.println("PASS: " + nombre);
    }

    private static void recordFail(String nombre, String detalle) {
        failed++;
        fallos.add(nombre + " :: " + detalle);
        System.out.println("FAIL: " + nombre + " :: " + detalle);
    }

    private static String escribirTmp(String nombre, String contenido) throws IOException {
        File f = new File(nombre);
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(contenido);
        }
        return f.getAbsolutePath();
    }

    private static String leer(String ruta) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            int c;
            while ((c = br.read()) != -1) {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }
}
